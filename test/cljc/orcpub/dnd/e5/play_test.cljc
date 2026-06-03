(ns orcpub.dnd.e5.play-test
  (:require [clojure.test :refer [deftest testing is are]]
            [orcpub.dnd.e5.play :as play]
            [orcpub.dnd.e5.character :as char5e]))

(def built-char {::char5e/max-hit-points 45})
(def base-state (play/init-play-state built-char))

(deftest init-play-state-test
  (is (= 45 (::play/current-hp base-state)))
  (is (= 0 (::play/temp-hp base-state)))
  (is (= 0 (::play/max-hp-modifier base-state)))
  (is (= {} (::play/spell-slots-used base-state)))
  (is (= #{} (::play/conditions base-state)))
  (is (= :play (::play/mode base-state))))

(deftest take-damage-test
  (testing "basic damage"
    (let [state (play/take-damage base-state built-char 10)]
      (is (= 35 (::play/current-hp state)))))

  (testing "temp HP absorbs damage first"
    (let [state (-> base-state (assoc ::play/temp-hp 5))
          after (play/take-damage state built-char 8)]
      (is (= 0 (::play/temp-hp after)))
      (is (= 42 (::play/current-hp after)))))

  (testing "HP cannot go below 0"
    (let [state (play/take-damage base-state built-char 999)]
      (is (= 0 (::play/current-hp state)))))

  (testing "reaching 0 HP adds unconscious"
    (let [state (play/take-damage base-state built-char 45)]
      (is (= 0 (::play/current-hp state)))
      (is (contains? (::play/conditions state) :unconscious)))))

(deftest heal-test
  (testing "basic healing"
    (let [state (-> base-state (assoc ::play/current-hp 20))
          after (play/heal state built-char 10)]
      (is (= 30 (::play/current-hp after)))))

  (testing "cannot exceed max HP"
    (let [state (-> base-state (assoc ::play/current-hp 40))
          after (play/heal state built-char 100)]
      (is (= 45 (::play/current-hp after)))))

  (testing "healing from 0 resets death saves"
    (let [state (-> base-state
                    (assoc ::play/current-hp 0
                           ::play/death-save-successes 2
                           ::play/death-save-failures 1
                           ::play/conditions #{:unconscious}))
          after (play/heal state built-char 5)]
      (is (= 5 (::play/current-hp after)))
      (is (= 0 (::play/death-save-successes after)))
      (is (= 0 (::play/death-save-failures after)))
      (is (not (contains? (::play/conditions after) :unconscious))))))

(deftest temp-hp-test
  (testing "set temp HP (takes higher)"
    (let [state (play/set-temp-hp base-state 10)]
      (is (= 10 (::play/temp-hp state)))))

  (testing "does not downgrade existing"
    (let [state (-> base-state (assoc ::play/temp-hp 15))
          after (play/set-temp-hp state 10)]
      (is (= 15 (::play/temp-hp after))))))

(deftest spell-slot-test
  (testing "use spell slot"
    (let [state (play/use-spell-slot base-state 1 {1 4 2 3})]
      (is (= 1 (get-in state [::play/spell-slots-used 1])))))

  (testing "cannot exceed max"
    (let [state (-> base-state (assoc ::play/spell-slots-used {1 4}))
          after (play/use-spell-slot state 1 {1 4})]
      (is (= 4 (get-in after [::play/spell-slots-used 1])))))

  (testing "restore spell slot"
    (let [state (-> base-state (assoc ::play/spell-slots-used {1 3}))
          after (play/restore-spell-slot state 1)]
      (is (= 2 (get-in after [::play/spell-slots-used 1]))))))

(deftest condition-test
  (testing "add condition"
    (let [state (play/set-condition base-state :poisoned true)]
      (is (contains? (::play/conditions state) :poisoned))))

  (testing "remove condition"
    (let [state (-> base-state (assoc ::play/conditions #{:poisoned}))
          after (play/set-condition state :poisoned false)]
      (is (not (contains? (::play/conditions after) :poisoned))))))

(deftest death-save-test
  (testing "natural 20 restores 1 HP"
    (let [state (-> base-state (assoc ::play/current-hp 0
                                      ::play/death-save-failures 2))
          after (play/record-death-save state 20)]
      (is (= 1 (::play/current-hp after)))
      (is (= 0 (::play/death-save-failures after)))))

  (testing "natural 1 counts as 2 failures"
    (let [state (-> base-state (assoc ::play/current-hp 0))
          after (play/record-death-save state 1)]
      (is (= 2 (::play/death-save-failures after)))))

  (testing "10+ is success"
    (let [state (-> base-state (assoc ::play/current-hp 0))
          after (play/record-death-save state 10)]
      (is (= 1 (::play/death-save-successes after)))))

  (testing "below 10 is failure"
    (let [state (-> base-state (assoc ::play/current-hp 0))
          after (play/record-death-save state 5)]
      (is (= 1 (::play/death-save-failures after))))))

(deftest crimson-rite-test
  (testing "activate reduces max HP"
    (let [state (play/activate-rite base-state :longsword :rite-of-flame :fire 4)]
      (is (= -4 (::play/max-hp-modifier state)))
      (is (= 1 (count (::play/active-rites state))))))

  (testing "deactivate restores max HP"
    (let [state (-> base-state
                    (play/activate-rite :longsword :rite-of-flame :fire 4)
                    (play/deactivate-rite :longsword))]
      (is (= 0 (::play/max-hp-modifier state)))
      (is (empty? (::play/active-rites state))))))

(deftest blood-curse-test
  (testing "normal use decrements"
    (let [state (play/use-blood-curse base-state 3 false 0)]
      (is (= 1 (::play/blood-curses-used state)))))

  (testing "amplified use costs HP"
    (let [state (play/use-blood-curse base-state 3 true 4)]
      (is (= 1 (::play/blood-curses-used state)))
      (is (= 41 (::play/current-hp state)))))

  (testing "cannot exceed max uses"
    (let [state (-> base-state (assoc ::play/blood-curses-used 3))
          after (play/use-blood-curse state 3 false 0)]
      (is (= 3 (::play/blood-curses-used after))))))

(deftest equipment-toggle-test
  (testing "equip adds to set"
    (let [state (play/toggle-equipment base-state :shield true)]
      (is (contains? (::play/equipped-items state) :shield))))

  (testing "unequip removes from set"
    (let [state (-> base-state (assoc ::play/equipped-items #{:shield}))
          after (play/toggle-equipment state :shield false)]
      (is (not (contains? (::play/equipped-items after) :shield))))))

(deftest attunement-test
  (testing "max 3 attuned items"
    (let [state (-> base-state (assoc ::play/attuned-items #{:a :b :c}))
          after (play/toggle-attunement state :d true)]
      (is (= 3 (count (::play/attuned-items after)))))))

(deftest long-rest-test
  (testing "restores HP and resets all"
    (let [state (-> base-state
                    (assoc ::play/current-hp 10
                           ::play/spell-slots-used {1 3 2 2}
                           ::play/pact-slots-used 2
                           ::play/blood-curses-used 3
                           ::play/death-save-successes 2
                           ::play/resources-used [{:key :ki :used 5}]))
          after (play/long-rest state built-char [])]
      (is (= 45 (::play/current-hp after)))
      (is (= {} (::play/spell-slots-used after)))
      (is (= 0 (::play/pact-slots-used after)))
      (is (= 0 (::play/blood-curses-used after)))
      (is (= 0 (::play/death-save-successes after)))
      (is (every? #(= 0 (:used %)) (::play/resources-used after))))))

(deftest long-rest-idempotence-test
  (testing "long rest applied twice equals applied once"
    (let [state (-> base-state (assoc ::play/current-hp 10))
          once (play/long-rest state built-char [])
          twice (play/long-rest once built-char [])]
      (is (= once twice)))))
