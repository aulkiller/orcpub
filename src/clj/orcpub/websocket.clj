(ns orcpub.websocket
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [buddy.sign.jwt :as jwt]
            [orcpub.config :as config]
            [io.pedestal.log :as log])
  (:import [org.eclipse.jetty.websocket.server JettyWebSocketServlet
            JettyWebSocketServletFactory JettyWebSocketCreator]
           [org.eclipse.jetty.websocket.api Session]
           [org.eclipse.jetty.servlet ServletContextHandler ServletHolder]
           [java.util.concurrent ConcurrentHashMap]))

;; Connection registry: campaign-id → #{session-info}
(defonce connections (ConcurrentHashMap.))

;; Session → metadata map
(defonce session-meta (ConcurrentHashMap.))

(defn- parse-token [query-string]
  (some->> (str/split (or query-string "") #"&")
           (some #(when (str/starts-with? % "token=")
                    (subs % 6)))))

(defn- validate-jwt [token]
  (try
    (jwt/unsign token (config/signature) {:alg :hs256})
    (catch Exception _ nil)))

(defn register-session! [session username campaign-id character-id role]
  (let [info {:session session :username username :campaign-id campaign-id
              :character-id character-id :role role}]
    (.put session-meta session info)
    (.compute connections campaign-id
              (reify java.util.function.BiFunction
                (apply [_ _k existing]
                  (conj (or existing #{}) info))))))

(defn unregister-session! [session]
  (when-let [info (.remove session-meta session)]
    (.compute connections (:campaign-id info)
              (reify java.util.function.BiFunction
                (apply [_ _k existing]
                  (let [remaining (disj (or existing #{}) info)]
                    (when (seq remaining) remaining)))))))

(defn broadcast-to-campaign!
  "Send a message to all sessions subscribed to a campaign."
  [campaign-id message]
  (let [msg (pr-str message)]
    (doseq [{:keys [session]} (get connections campaign-id)]
      (try
        (when (.isOpen session)
          (.. session getRemote (sendString msg)))
        (catch Exception e
          (log/warn :msg "WS send failed" :error (.getMessage e)))))))

(defn send-to-session!
  "Send a message to a specific session."
  [session message]
  (when (.isOpen session)
    (.. session getRemote (sendString (pr-str message)))))

(defn- handle-message [session raw-message]
  (try
    (let [msg (edn/read-string raw-message)
          info (.get session-meta session)]
      (case (:type msg)
        :ping (send-to-session! session {:type :pong})
        :subscribe-character
        (let [updated (assoc info :character-id (:character-id msg))]
          (.put session-meta session updated))
        (log/info :msg "Unknown WS message type" :type (:type msg))))
    (catch Exception e
      (log/warn :msg "WS message parse error" :error (.getMessage e)))))

(defn- make-ws-listener []
  (let [session-atom (atom nil)]
    (reify org.eclipse.jetty.websocket.api.WebSocketListener
      (onWebSocketConnect [_ session]
        (reset! session-atom session)
        (let [query (.. session getUpgradeRequest getQueryString)
              token (parse-token query)
              claims (validate-jwt token)]
          (if claims
            (let [params (str/split (or query "") #"&")
                  campaign-id (some #(when (str/starts-with? % "campaign=") (subs % 9)) params)
                  role (if (some #(str/starts-with? % "role=dm") params) :dm :player)]
              (register-session! session (:username claims) campaign-id nil role)
              (send-to-session! session {:type :connected :username (:username claims)}))
            (do
              (log/warn :msg "WS auth failed — closing")
              (.close session 4001 "Unauthorized")))))
      (onWebSocketText [_ message]
        (handle-message @session-atom message))
      (onWebSocketBinary [_ _payload _offset _len])
      (onWebSocketClose [_ status-code reason]
        (when-let [s @session-atom]
          (unregister-session! s)))
      (onWebSocketError [_ error]
        (log/warn :msg "WS error" :error (.getMessage error))
        (when-let [s @session-atom]
          (unregister-session! s))))))

(defn- make-ws-servlet []
  (proxy [JettyWebSocketServlet] []
    (configure [^JettyWebSocketServletFactory factory]
      (.setIdleTimeout factory (java.time.Duration/ofMinutes 5))
      (.setCreator factory
                   (reify JettyWebSocketCreator
                     (createWebSocket [_ _req _resp]
                       (make-ws-listener)))))))

(defn add-websocket-handler!
  "Add WebSocket endpoint to an existing Jetty server.
   Call after Pedestal creates the server but before/after start."
  [server]
  (let [handlers (.getHandlers server)]
    (doseq [handler handlers]
      (when (instance? ServletContextHandler handler)
        (let [holder (ServletHolder. ^jakarta.servlet.Servlet (make-ws-servlet))]
          (.addServlet ^ServletContextHandler handler holder "/ws")
          (org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer/configure
           handler nil)))))
  server)
