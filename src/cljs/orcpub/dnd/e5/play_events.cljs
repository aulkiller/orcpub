(ns orcpub.dnd.e5.play-events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [orcpub.dnd.e5.play :as play]
            [orcpub.dice :as dice]))

(defn play-api-url [character-id action]
  (str "/api/play/" character-id "/" action))

(reg-event-fx
 ::enter-play-mode
 (fn [{:keys [db]} [_ character-id]]
   {:db (assoc-in db [:play-state :mode] :play)
    :http {:method :post
           :url (play-api-url character-id "state")
           :on-success [::load-play-state]}}))

(reg-event-db
 ::load-play-state
 (fn [db [_ response]]
   (assoc db :play-state (or response (play/init-play-state {})))))

(reg-event-fx
 ::take-damage
 (fn [{:keys [db]} [_ character-id amount]]
   (let [new-state (play/take-damage (:play-state db) nil amount)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "damage")
             :body {:amount amount}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::heal
 (fn [{:keys [db]} [_ character-id amount]]
   (let [new-state (play/heal (:play-state db) nil amount)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "heal")
             :body {:amount amount}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::set-temp-hp
 (fn [{:keys [db]} [_ character-id amount]]
   (let [new-state (play/set-temp-hp (:play-state db) amount)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "temp-hp")
             :body {:amount amount}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::use-spell-slot
 (fn [{:keys [db]} [_ character-id level max-slots]]
   (let [new-state (play/use-spell-slot (:play-state db) level max-slots)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "spell-slot")
             :body {:level level :max-slots max-slots}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::restore-spell-slot
 (fn [{:keys [db]} [_ character-id level]]
   (let [new-state (play/restore-spell-slot (:play-state db) level)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "spell-slot")
             :body {:level level :restore? true}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::use-resource
 (fn [{:keys [db]} [_ character-id resource-key max-val]]
   (let [new-state (play/use-resource (:play-state db) resource-key max-val)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "resource")
             :body {:resource-key resource-key :max-val max-val}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::toggle-equipment
 (fn [{:keys [db]} [_ character-id item-key equipped?]]
   (let [new-state (play/toggle-equipment (:play-state db) item-key equipped?)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "equipment")
             :body {:item-key item-key :equipped? equipped?}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::toggle-attunement
 (fn [{:keys [db]} [_ character-id item-key attune?]]
   (let [new-state (play/toggle-attunement (:play-state db) item-key attune?)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "equipment")
             :body {:item-key item-key :attune? attune?}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::short-rest
 (fn [{:keys [db]} [_ character-id hit-dice-to-spend resource-defs]]
   (let [new-state (play/short-rest (:play-state db) nil hit-dice-to-spend resource-defs)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "short-rest")
             :body {:hit-dice-to-spend hit-dice-to-spend :resource-defs resource-defs}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::long-rest
 (fn [{:keys [db]} [_ character-id resource-defs]]
   (let [new-state (play/long-rest (:play-state db) nil resource-defs)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "long-rest")
             :body {:resource-defs resource-defs}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::set-condition
 (fn [{:keys [db]} [_ character-id condition active?]]
   (let [new-state (play/set-condition (:play-state db) condition active?)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "condition")
             :body {:condition condition :active? active?}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::record-death-save
 (fn [{:keys [db]} [_ character-id roll-value]]
   (let [new-state (play/record-death-save (:play-state db) roll-value)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "death-save")
             :body {:roll roll-value}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::set-prepared-spells
 (fn [{:keys [db]} [_ character-id class-key spell-keys max-prepared]]
   (let [new-state (play/set-prepared-spells (:play-state db) class-key spell-keys max-prepared)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "prepared")
             :body {:class-key class-key :spell-keys spell-keys :max-prepared max-prepared}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::activate-rite
 (fn [{:keys [db]} [_ character-id weapon-key rite-key damage-type hp-cost]]
   (let [new-state (play/activate-rite (:play-state db) weapon-key rite-key damage-type hp-cost)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "rite")
             :body {:weapon-key weapon-key :rite-key rite-key :damage-type damage-type :hp-cost hp-cost :activate? true}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::deactivate-rite
 (fn [{:keys [db]} [_ character-id weapon-key]]
   (let [new-state (play/deactivate-rite (:play-state db) weapon-key)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "rite")
             :body {:weapon-key weapon-key :activate? false}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-fx
 ::use-blood-curse
 (fn [{:keys [db]} [_ character-id max-uses amplify? hp-cost]]
   (let [new-state (play/use-blood-curse (:play-state db) max-uses amplify? hp-cost)]
     {:db (assoc db :play-state new-state)
      :http {:method :post
             :url (play-api-url character-id "blood-curse")
             :body {:max-uses max-uses :amplify? amplify? :hp-cost hp-cost}
             :on-failure [::revert-play-state (:play-state db)]}})))

(reg-event-db
 ::revert-play-state
 (fn [db [_ previous-state]]
   (assoc db :play-state previous-state)))

;; Dice rolling — client-side only, no server call
(reg-event-db
 ::roll-dice
 (fn [db [_ notation advantage?]]
   (let [parsed (dice/parse-notation notation)
         result (cond
                  (= advantage? :advantage) (dice/roll-with-advantage (:modifier parsed))
                  (= advantage? :disadvantage) (dice/roll-with-disadvantage (:modifier parsed))
                  :else (dice/roll-full parsed))
         entry (assoc result :notation notation :id (random-uuid) :timestamp (js/Date.))]
     (update db :roll-history (fn [h] (vec (take 50 (cons entry (or h [])))))))))

(reg-event-db
 ::open-rules-lookup
 (fn [db [_ entry-type entry-key]]
   (assoc db :rules-lookup {:entry-type entry-type :entry-key entry-key :open? true})))

(reg-event-db
 ::close-rules-lookup
 (fn [db _]
   (assoc db :rules-lookup nil)))
