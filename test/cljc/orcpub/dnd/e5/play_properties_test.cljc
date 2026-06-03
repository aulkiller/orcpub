(ns orcpub.dnd.e5.play-properties-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [orcpub.dnd.e5.play :as play]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.play-generators :as pgen]
            [orcpub.dice :as dice]))

(def built-char-stub {::char5e/max-hit-points 50 ::char5e/total-hit-dice 5})

;; PBT-03: HP bounds invariant
(defspec hp-never-exceeds-max 100
  (prop/for-all [state pgen/gen-play-state
                 amount pgen/gen-hp-delta]
    (let [bc {::char5e/max-hit-points 200}
          max-hp (+ 200 (or (::play/max-hp-modifier state) 0))
          after-heal (play/heal state bc amount)]
      (<= (::play/current-hp after-heal) max-hp))))

;; PBT-03: HP never below 0 after damage
(defspec hp-never-below-zero 100
  (prop/for-all [state pgen/gen-play-state
                 amount pgen/gen-hp-delta]
    (let [after (play/take-damage state built-char-stub amount)]
      (>= (::play/current-hp after) 0))))

;; PBT-03: Spell slot used never exceeds max
(defspec spell-slot-bounded 100
  (prop/for-all [level pgen/gen-spell-level
                 max-count (gen/choose 1 9)]
    (let [state (play/init-play-state built-char-stub)
          max-slots {level max-count}
          after (reduce (fn [s _] (play/use-spell-slot s level max-slots))
                        state
                        (range (+ max-count 5)))]
      (<= (get-in after [::play/spell-slots-used level] 0) max-count))))

;; PBT-03: Death save bounds
(defspec death-save-bounded 100
  (prop/for-all [rolls (gen/vector (gen/choose 1 20) 1 10)]
    (let [state (assoc (play/init-play-state built-char-stub) ::play/current-hp 0)
          after (reduce play/record-death-save state rolls)]
      (and (<= (::play/death-save-successes after) 3)
           (<= (::play/death-save-failures after) 3)))))

;; PBT-04: Long rest idempotence
(defspec long-rest-idempotent 50
  (prop/for-all [state pgen/gen-play-state]
    (let [once (play/long-rest state built-char-stub [])
          twice (play/long-rest once built-char-stub [])]
      (= once twice))))

;; PBT-02: Rite activate/deactivate round-trip restores max-hp-modifier
(defspec rite-round-trip 50
  (prop/for-all [weapon-key pgen/gen-weapon-key
                 rite-key pgen/gen-rite-key
                 hp-cost (gen/choose 1 10)]
    (let [state (play/init-play-state built-char-stub)
          original-mod (::play/max-hp-modifier state)
          activated (play/activate-rite state weapon-key rite-key :fire hp-cost)
          deactivated (play/deactivate-rite activated weapon-key)]
      (= original-mod (::play/max-hp-modifier deactivated)))))

;; PBT-03: Dice results within bounds
(defspec dice-within-bounds 200
  (prop/for-all [sides pgen/gen-die-size
                 num (gen/choose 1 10)]
    (let [result (dice/roll-full {:num num :sides sides :modifier 0})]
      (every? #(and (>= (:result %) 1) (<= (:result %) sides))
              (:dice result)))))

;; PBT-03: Attunement max 3
(defspec attunement-max-three 50
  (prop/for-all [items (gen/vector pgen/gen-weapon-key 1 8)]
    (let [state (play/init-play-state built-char-stub)
          after (reduce #(play/toggle-attunement %1 %2 true) state items)]
      (<= (count (::play/attuned-items after)) 3))))
