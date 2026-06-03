(ns orcpub.dnd.e5.play-subs
  (:require [re-frame.core :refer [reg-sub]]
            [orcpub.dnd.e5.play :as play]))

(reg-sub ::play-state (fn [db _] (:play-state db)))

(reg-sub ::current-hp :<- [::play-state] (fn [s _] (or (::play/current-hp s) 0)))

(reg-sub ::temp-hp :<- [::play-state] (fn [s _] (or (::play/temp-hp s) 0)))

(reg-sub ::max-hp-modifier :<- [::play-state] (fn [s _] (or (::play/max-hp-modifier s) 0)))

(reg-sub ::spell-slots-used :<- [::play-state] (fn [s _] (or (::play/spell-slots-used s) {})))

(reg-sub ::pact-slots-used :<- [::play-state] (fn [s _] (or (::play/pact-slots-used s) 0)))

(reg-sub ::resources-used :<- [::play-state] (fn [s _] (or (::play/resources-used s) [])))

(reg-sub ::conditions :<- [::play-state] (fn [s _] (or (::play/conditions s) #{})))

(reg-sub ::custom-conditions :<- [::play-state] (fn [s _] (or (::play/custom-conditions s) [])))

(reg-sub ::death-saves
         :<- [::play-state]
         (fn [s _]
           {:successes (or (::play/death-save-successes s) 0)
            :failures (or (::play/death-save-failures s) 0)}))

(reg-sub ::equipped-items :<- [::play-state] (fn [s _] (or (::play/equipped-items s) #{})))

(reg-sub ::attuned-items :<- [::play-state] (fn [s _] (or (::play/attuned-items s) #{})))

(reg-sub ::active-rites :<- [::play-state] (fn [s _] (or (::play/active-rites s) [])))

(reg-sub ::blood-curses-used :<- [::play-state] (fn [s _] (or (::play/blood-curses-used s) 0)))

(reg-sub ::hit-dice-used :<- [::play-state] (fn [s _] (or (::play/hit-dice-used s) [])))

(reg-sub ::is-play-mode? :<- [::play-state] (fn [s _] (= :play (::play/mode s))))

(reg-sub ::roll-history (fn [db _] (or (:roll-history db) [])))

(reg-sub ::rules-lookup (fn [db _] (:rules-lookup db)))
