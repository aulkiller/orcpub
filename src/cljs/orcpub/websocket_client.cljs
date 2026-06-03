(ns orcpub.websocket-client
  (:require [cljs.reader :as reader]
            [re-frame.core :refer [dispatch]]))

(defonce ws-state (atom {:socket nil :reconnect-attempts 0 :campaign-id nil}))

(def max-reconnect-delay 30000)

(defn- reconnect-delay []
  (min max-reconnect-delay
       (* 1000 (Math/pow 2 (:reconnect-attempts @ws-state)))))

(defn- ws-url [token campaign-id role]
  (let [proto (if (= "https:" js/window.location.protocol) "wss:" "ws:")
        host js/window.location.host]
    (str proto "//" host "/ws?token=" token "&campaign=" campaign-id "&role=" (name role))))

(defn send! [msg]
  (when-let [socket (:socket @ws-state)]
    (when (= 1 (.-readyState socket))
      (.send socket (pr-str msg)))))

(defn- on-message [event]
  (let [msg (reader/read-string (.-data event))]
    (case (:type msg)
      :pong nil
      :connected (dispatch [::connected (:username msg)])
      :play-state-update (dispatch [::play-state-update (:character-id msg) (:state msg)])
      :initiative-update (dispatch [::initiative-update (:order msg)])
      :member-status (dispatch [::member-status (:username msg) (:online? msg)])
      (js/console.warn "Unknown WS message:" (pr-str msg)))))

(defn- on-close [token campaign-id role _event]
  (swap! ws-state assoc :socket nil)
  (dispatch [::disconnected])
  (js/setTimeout
   (fn [] (connect! token campaign-id role))
   (reconnect-delay))
  (swap! ws-state update :reconnect-attempts inc))

(defn- on-open [_event]
  (swap! ws-state assoc :reconnect-attempts 0)
  (dispatch [::connection-status :connected])
  ;; Start keepalive ping every 30s
  (js/setInterval #(send! {:type :ping}) 30000))

(defn connect!
  "Connect to WebSocket for a campaign. Call when entering a campaign view."
  [token campaign-id role]
  (when-let [old (:socket @ws-state)]
    (when (< (.-readyState old) 2) (.close old)))
  (let [url (ws-url token campaign-id role)
        socket (js/WebSocket. url)]
    (set! (.-onopen socket) on-open)
    (set! (.-onmessage socket) on-message)
    (set! (.-onclose socket) (partial on-close token campaign-id role))
    (set! (.-onerror socket) #(js/console.error "WS error" %))
    (swap! ws-state assoc :socket socket :campaign-id campaign-id)))

(defn disconnect!
  "Close WebSocket connection. Call when leaving campaign view."
  []
  (when-let [socket (:socket @ws-state)]
    (when (< (.-readyState socket) 2)
      (.close socket 1000 "User disconnected")))
  (reset! ws-state {:socket nil :reconnect-attempts 0 :campaign-id nil}))

;; Re-frame events for WS status
(re-frame.core/reg-event-db
 ::connected
 (fn [db [_ username]] (assoc db :ws-status :connected :ws-username username)))

(re-frame.core/reg-event-db
 ::disconnected
 (fn [db _] (assoc db :ws-status :disconnected)))

(re-frame.core/reg-event-db
 ::connection-status
 (fn [db [_ status]] (assoc db :ws-status status)))

(re-frame.core/reg-event-db
 ::play-state-update
 (fn [db [_ character-id state]]
   (assoc-in db [:campaign-characters character-id :play-state] state)))

(re-frame.core/reg-event-db
 ::initiative-update
 (fn [db [_ order]] (assoc db :initiative-order order)))

(re-frame.core/reg-event-db
 ::member-status
 (fn [db [_ username online?]]
   (assoc-in db [:campaign-members username :online?] online?)))
