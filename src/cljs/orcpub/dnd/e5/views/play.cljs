(ns orcpub.dnd.e5.views.play
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [orcpub.dnd.e5.play :as play]
            [orcpub.dnd.e5.play-events :as events]
            [orcpub.dnd.e5.play-subs :as subs]
            [orcpub.dnd.e5.views.dice :refer [dice-roller]]))

(defn hp-controls [character-id]
  (let [amount (r/atom "")]
    (fn []
      [:div.hp-controls
       [:input.hp-input {:type "number" :placeholder "Amount" :value @amount
                         :on-change #(reset! amount (.. % -target -value))}]
       [:button.play-button.damage-btn
        {:on-click #(when-let [n (js/parseInt @amount)]
                      (dispatch [::events/take-damage character-id n])
                      (reset! amount ""))} "Damage"]
       [:button.play-button.heal-btn
        {:on-click #(when-let [n (js/parseInt @amount)]
                      (dispatch [::events/heal character-id n])
                      (reset! amount ""))} "Heal"]])))

(defn hp-tracker [character-id max-hp]
  (let [current @(subscribe [::subs/current-hp])
        temp @(subscribe [::subs/temp-hp])
        max-mod @(subscribe [::subs/max-hp-modifier])
        effective-max (+ max-hp max-mod)
        death-saves @(subscribe [::subs/death-saves])]
    [:div.hp-tracker
     [:div.hp-display
      [:span.hp-current current]
      [:span.hp-separator "/"]
      [:span.hp-max effective-max]
      (when (pos? temp) [:span.hp-temp (str "+" temp " temp")])]
     [hp-controls character-id]
     (when (zero? current)
       [:div.death-saves
        [:div.death-successes
         (str "Successes: ")
         (for [i (range 3)]
           ^{:key i} [:span.save-circle {:class (when (> (:successes death-saves) i) "filled")}])]
        [:div.death-failures
         (str "Failures: ")
         (for [i (range 3)]
           ^{:key i} [:span.save-circle.failure {:class (when (> (:failures death-saves) i) "filled")}])]
        [:button.play-button {:on-click #(let [roll (inc (rand-int 20))]
                                           (dispatch [::events/record-death-save character-id roll]))}
         "Roll Death Save"]])]))

(defn spell-slot-row [character-id level max-count used-count]
  [:div.spell-slot-row
   [:span.slot-level (str "Level " level)]
   [:div.slot-circles
    (for [i (range max-count)]
      ^{:key i}
      [:span.slot-circle
       {:class (when (< i used-count) "used")
        :on-click #(if (< i used-count)
                     (dispatch [::events/restore-spell-slot character-id level])
                     (dispatch [::events/use-spell-slot character-id level {level max-count}]))}])]])

(defn spell-slot-tracker [character-id spell-slots]
  (let [used @(subscribe [::subs/spell-slots-used])]
    [:div.spell-slot-tracker
     [:h3 "Spell Slots"]
     (for [[level max-count] (sort spell-slots)]
       ^{:key level}
       [spell-slot-row character-id level max-count (get used level 0)])]))

(defn resource-item [character-id {:keys [key name max reset-on]} used]
  [:div.resource-item
   [:span.resource-name name]
   [:span.resource-count (str (- max used) "/" max)]
   [:button.play-button.sm
    {:on-click #(dispatch [::events/use-resource character-id key max])
     :disabled (>= used max)} "Use"]
   [:span.resource-reset (clojure.core/name reset-on)]])

(defn resource-panel [character-id resource-defs]
  (let [used-map @(subscribe [::subs/resources-used])
        used-lookup (into {} (map (juxt :key :used) used-map))]
    [:div.resource-panel
     [:h3 "Resources"]
     (for [res resource-defs]
       ^{:key (:key res)}
       [resource-item character-id res (get used-lookup (:key res) 0)])]))

(defn conditions-panel [character-id]
  (let [active @(subscribe [::subs/conditions])
        custom @(subscribe [::subs/custom-conditions])
        new-condition (r/atom "")]
    (fn []
      [:div.conditions-panel
       [:h3 "Conditions"]
       [:div.condition-grid
        (for [c (sort play/standard-conditions)]
          ^{:key c}
          [:label.condition-toggle
           {:class (when (contains? active c) "active")}
           [:input {:type "checkbox" :checked (contains? active c)
                    :on-change #(dispatch [::events/set-condition character-id c
                                           (not (contains? active c))])}]
           (clojure.core/name c)])]
       (when (seq custom)
         [:div.custom-conditions
          (for [c custom] ^{:key c} [:span.custom-tag c])])
       [:div.add-condition
        [:input {:type "text" :placeholder "Custom..." :value @new-condition
                 :on-change #(reset! new-condition (.. % -target -value))}]
        [:button.play-button.sm
         {:on-click #(when (seq @new-condition)
                       (dispatch [::events/set-condition character-id (keyword @new-condition) true])
                       (reset! new-condition ""))} "+"]]])))

(defn equipment-panel [character-id equipment-list]
  (let [equipped @(subscribe [::subs/equipped-items])
        attuned @(subscribe [::subs/attuned-items])]
    [:div.equipment-panel
     [:h3 "Equipment"]
     (for [{:keys [key name attunement?]} equipment-list]
       ^{:key key}
       [:div.equipment-item
        [:label.equip-toggle
         [:input {:type "checkbox" :checked (contains? equipped key)
                  :on-change #(dispatch [::events/toggle-equipment character-id key
                                          (not (contains? equipped key))])}]
         name]
        (when attunement?
          [:label.attune-toggle
           [:input {:type "checkbox" :checked (contains? attuned key)
                    :on-change #(dispatch [::events/toggle-attunement character-id key
                                            (not (contains? attuned key))])}]
           "⚡"])])]))

(defn rest-actions [character-id resource-defs]
  [:div.rest-actions
   [:button.play-button.rest-btn
    {:on-click #(dispatch [::events/short-rest character-id [] resource-defs])}
    "Short Rest"]
   [:button.play-button.rest-btn
    {:on-click #(dispatch [::events/long-rest character-id resource-defs])}
    "Long Rest"]])

(defn play-mode-view [character-id built-char]
  (let [max-hp (or (:max-hit-points built-char) 0)
        spell-slots (or (:spell-slots built-char) {})
        resource-defs (or (:resources built-char) [])
        equipment-list (or (:equipment built-char) [])]
    [:div.play-mode
     [:div.play-column-left
      [hp-tracker character-id max-hp]
      [spell-slot-tracker character-id spell-slots]
      [resource-panel character-id resource-defs]
      [rest-actions character-id resource-defs]]
     [:div.play-column-right
      [dice-roller]
      [conditions-panel character-id]
      [equipment-panel character-id equipment-list]]]))
