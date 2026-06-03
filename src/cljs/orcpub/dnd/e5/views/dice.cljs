(ns orcpub.dnd.e5.views.dice
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [orcpub.dnd.e5.play-events :as play-events]
            [orcpub.dnd.e5.play-subs :as play-subs]))

(defn quick-roll-button [label notation]
  [:button.play-button.dice-btn
   {:on-click #(dispatch [::play-events/roll-dice notation nil])}
   label])

(defn dice-roller []
  (let [input (r/atom "")
        advantage (r/atom nil)]
    (fn []
      (let [history @(subscribe [::play-subs/roll-history])]
        [:div.dice-roller
         [:div.dice-quick-buttons
          [quick-roll-button "d4" "1d4"]
          [quick-roll-button "d6" "1d6"]
          [quick-roll-button "d8" "1d8"]
          [quick-roll-button "d10" "1d10"]
          [quick-roll-button "d12" "1d12"]
          [quick-roll-button "d20" "1d20"]
          [quick-roll-button "d100" "1d100"]]
         [:div.dice-custom
          [:input.dice-input
           {:type "text"
            :placeholder "2d6+3"
            :value @input
            :on-change #(reset! input (.. % -target -value))
            :on-key-press (fn [e]
                            (when (= 13 (.-charCode e))
                              (dispatch [::play-events/roll-dice @input @advantage])
                              (reset! input "")))}]
          [:button.play-button
           {:on-click (fn []
                        (when (seq @input)
                          (dispatch [::play-events/roll-dice @input @advantage])
                          (reset! input "")))}
           "Roll"]]
         [:div.dice-advantage
          [:label
           [:input {:type "radio" :name "adv" :checked (nil? @advantage)
                    :on-change #(reset! advantage nil)}] " Normal"]
          [:label
           [:input {:type "radio" :name "adv" :checked (= :advantage @advantage)
                    :on-change #(reset! advantage :advantage)}] " Advantage"]
          [:label
           [:input {:type "radio" :name "adv" :checked (= :disadvantage @advantage)
                    :on-change #(reset! advantage :disadvantage)}] " Disadvantage"]]
         [:div.dice-history
          (for [roll (take 10 history)]
            ^{:key (:id roll)}
            [:div.dice-result
             [:span.dice-notation (:notation roll)]
             [:span.dice-total (:total roll)]
             [:span.dice-detail
              (str "(" (clojure.string/join ", "
                        (map :result (:dice roll))) ")"
                   (when (not= 0 (:modifier roll))
                     (str (if (pos? (:modifier roll)) "+" "") (:modifier roll))))]])]]))))
