(ns orcpub.routes.play
  (:require [clojure.edn :as edn]
            [datomic.api :as d]
            [io.pedestal.interceptor :refer [interceptor]]
            [orcpub.dnd.e5.play :as play]
            [orcpub.errors :as errors]
            [orcpub.websocket :as ws]))

(def validate-character-ownership
  (interceptor
   {:name ::validate-character-ownership
    :enter (fn [ctx]
             (let [db (get-in ctx [:request :db])
                   character-id (Long/parseLong (get-in ctx [:request :path-params :character-id]))
                   username (get-in ctx [:request :username])
                   owner (d/q '[:find ?owner .
                                :in $ ?eid
                                :where [?eid :orcpub.entity.strict/owner ?owner]]
                              db character-id)]
               (if (= owner username)
                 (assoc-in ctx [:request :character-id] character-id)
                 (assoc ctx :response {:status 403 :body {:error "Not authorized"}}))))}))

(defn- read-play-state [db character-id]
  (let [entity (d/entity db character-id)]
    {::play/current-hp (:orcpub.dnd.e5.play/current-hp entity)
     ::play/temp-hp (:orcpub.dnd.e5.play/temp-hp entity)
     ::play/max-hp-modifier (:orcpub.dnd.e5.play/max-hp-modifier entity)
     ::play/spell-slots-used (some-> (:orcpub.dnd.e5.play/spell-slots-used entity) edn/read-string)
     ::play/pact-slots-used (:orcpub.dnd.e5.play/pact-slots-used entity)
     ::play/resources-used (some-> (:orcpub.dnd.e5.play/resources-used entity) edn/read-string)
     ::play/conditions (some-> (:orcpub.dnd.e5.play/conditions entity) edn/read-string)
     ::play/custom-conditions (some-> (:orcpub.dnd.e5.play/custom-conditions entity) edn/read-string)
     ::play/death-save-successes (:orcpub.dnd.e5.play/death-save-successes entity)
     ::play/death-save-failures (:orcpub.dnd.e5.play/death-save-failures entity)
     ::play/equipped-items (some-> (:orcpub.dnd.e5.play/equipped-items entity) edn/read-string)
     ::play/attuned-items (some-> (:orcpub.dnd.e5.play/attuned-items entity) edn/read-string)
     ::play/active-rites (some-> (:orcpub.dnd.e5.play/active-rites entity) edn/read-string)
     ::play/blood-curses-used (:orcpub.dnd.e5.play/blood-curses-used entity)
     ::play/prepared-spells (some-> (:orcpub.dnd.e5.play/prepared-spells entity) edn/read-string)
     ::play/hit-dice-used (some-> (:orcpub.dnd.e5.play/hit-dice-used entity) edn/read-string)
     ::play/mode (:orcpub.dnd.e5.play/mode entity)}))

(defn- persist-play-state! [conn character-id play-state]
  (d/transact conn
              [{:db/id character-id
                :orcpub.dnd.e5.play/current-hp (::play/current-hp play-state)
                :orcpub.dnd.e5.play/temp-hp (::play/temp-hp play-state)
                :orcpub.dnd.e5.play/max-hp-modifier (::play/max-hp-modifier play-state)
                :orcpub.dnd.e5.play/spell-slots-used (pr-str (::play/spell-slots-used play-state))
                :orcpub.dnd.e5.play/pact-slots-used (or (::play/pact-slots-used play-state) 0)
                :orcpub.dnd.e5.play/resources-used (pr-str (::play/resources-used play-state))
                :orcpub.dnd.e5.play/conditions (pr-str (::play/conditions play-state))
                :orcpub.dnd.e5.play/custom-conditions (pr-str (::play/custom-conditions play-state))
                :orcpub.dnd.e5.play/death-save-successes (or (::play/death-save-successes play-state) 0)
                :orcpub.dnd.e5.play/death-save-failures (or (::play/death-save-failures play-state) 0)
                :orcpub.dnd.e5.play/equipped-items (pr-str (::play/equipped-items play-state))
                :orcpub.dnd.e5.play/attuned-items (pr-str (::play/attuned-items play-state))
                :orcpub.dnd.e5.play/active-rites (pr-str (::play/active-rites play-state))
                :orcpub.dnd.e5.play/blood-curses-used (or (::play/blood-curses-used play-state) 0)
                :orcpub.dnd.e5.play/prepared-spells (pr-str (::play/prepared-spells play-state))
                :orcpub.dnd.e5.play/hit-dice-used (pr-str (::play/hit-dice-used play-state))
                :orcpub.dnd.e5.play/mode (or (::play/mode play-state) :play)}]))

(defn- broadcast-state-change!
  "If the character belongs to a campaign, broadcast the state change via WebSocket."
  [db character-id play-state]
  (when-let [campaign-id (d/q '[:find ?c .
                                 :in $ ?char
                                 :where [?c :orcpub.dnd.e5.campaign/character-ids ?char]]
                               db character-id)]
    (ws/broadcast-to-campaign! (str campaign-id)
                               {:type :play-state-update
                                :character-id character-id
                                :state play-state})))

(defn get-play-state [{:keys [db character-id] :as request}]
  (let [state (read-play-state db character-id)]
    {:status 200 :body state}))

(defn take-damage [{:keys [db conn character-id body] :as request}]
  (let [{:keys [amount]} body
        state (read-play-state db character-id)
        new-state (play/take-damage state nil amount)]
    (persist-play-state! conn character-id new-state)
    (broadcast-state-change! db character-id new-state)
    {:status 200 :body new-state}))

(defn heal-hp [{:keys [db conn character-id body] :as request}]
  (let [{:keys [amount]} body
        state (read-play-state db character-id)
        new-state (play/heal state nil amount)]
    (persist-play-state! conn character-id new-state)
    (broadcast-state-change! db character-id new-state)
    {:status 200 :body new-state}))

(defn set-temp-hp [{:keys [db conn character-id body] :as request}]
  (let [{:keys [amount]} body
        state (read-play-state db character-id)
        new-state (play/set-temp-hp state amount)]
    (persist-play-state! conn character-id new-state)
    {:status 200 :body new-state}))

(defn use-spell-slot [{:keys [db conn character-id body] :as request}]
  (let [{:keys [level max-slots]} body
        state (read-play-state db character-id)
        new-state (play/use-spell-slot state level (or max-slots {}))]
    (persist-play-state! conn character-id new-state)
    {:status 200 :body new-state}))

(defn use-resource [{:keys [db conn character-id body] :as request}]
  (let [{:keys [resource-key max-val]} body
        state (read-play-state db character-id)
        new-state (play/use-resource state resource-key (or max-val 1))]
    (persist-play-state! conn character-id new-state)
    {:status 200 :body new-state}))

(defn toggle-equipment [{:keys [db conn character-id body] :as request}]
  (let [{:keys [item-key equipped?]} body
        state (read-play-state db character-id)
        new-state (play/toggle-equipment state item-key equipped?)]
    (persist-play-state! conn character-id new-state)
    {:status 200 :body new-state}))

(defn do-short-rest [{:keys [db conn character-id body] :as request}]
  (let [{:keys [hit-dice-to-spend resource-defs]} body
        state (read-play-state db character-id)
        new-state (play/short-rest state nil (or hit-dice-to-spend []) (or resource-defs []))]
    (persist-play-state! conn character-id new-state)
    {:status 200 :body new-state}))

(defn do-long-rest [{:keys [db conn character-id body] :as request}]
  (let [{:keys [resource-defs]} body
        state (read-play-state db character-id)
        new-state (play/long-rest state nil (or resource-defs []))]
    (persist-play-state! conn character-id new-state)
    {:status 200 :body new-state}))

(defn set-condition [{:keys [db conn character-id body] :as request}]
  (let [{:keys [condition active?]} body
        state (read-play-state db character-id)
        new-state (play/set-condition state condition active?)]
    (persist-play-state! conn character-id new-state)
    {:status 200 :body new-state}))

(defn record-death-save [{:keys [db conn character-id body] :as request}]
  (let [{:keys [roll]} body
        state (read-play-state db character-id)
        new-state (play/record-death-save state roll)]
    (persist-play-state! conn character-id new-state)
    {:status 200 :body new-state}))

(defn set-prepared-spells [{:keys [db conn character-id body] :as request}]
  (let [{:keys [class-key spell-keys max-prepared]} body
        state (read-play-state db character-id)
        new-state (play/set-prepared-spells state class-key (or spell-keys []) (or max-prepared 99))]
    (persist-play-state! conn character-id new-state)
    {:status 200 :body new-state}))

(defn toggle-rite [{:keys [db conn character-id body] :as request}]
  (let [{:keys [weapon-key rite-key damage-type hp-cost activate?]} body
        state (read-play-state db character-id)
        new-state (if activate?
                    (play/activate-rite state weapon-key rite-key damage-type (or hp-cost 0))
                    (play/deactivate-rite state weapon-key))]
    (persist-play-state! conn character-id new-state)
    {:status 200 :body new-state}))

(defn use-blood-curse [{:keys [db conn character-id body] :as request}]
  (let [{:keys [max-uses amplify? hp-cost]} body
        state (read-play-state db character-id)
        new-state (play/use-blood-curse state (or max-uses 1) amplify? (or hp-cost 0))]
    (persist-play-state! conn character-id new-state)
    {:status 200 :body new-state}))
