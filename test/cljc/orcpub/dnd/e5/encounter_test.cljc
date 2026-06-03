(ns orcpub.dnd.e5.encounter-test
  (:require [clojure.test :refer [deftest testing is]]
            [orcpub.dnd.e5.encounter :as enc]))

(deftest party-thresholds-test
  (testing "4 level-5 party"
    (let [t (enc/party-thresholds [5 5 5 5])]
      (is (= 1000 (:easy t)))
      (is (= 2000 (:medium t)))
      (is (= 3000 (:hard t)))
      (is (= 4400 (:deadly t))))))

(deftest calculate-difficulty-test
  (testing "trivial encounter"
    (let [result (enc/calculate-difficulty [{:cr "1" :num 2}] [5 5 5 5])]
      (is (= 400 (:xp result)))
      (is (= :trivial (:difficulty result)))))

  (testing "deadly encounter"
    (let [result (enc/calculate-difficulty [{:cr "10" :num 2}] [5 5 5 5])]
      (is (= 11800 (:xp result)))
      (is (= :deadly (:difficulty result))))))

(deftest roll-initiative-test
  (testing "returns sorted participants"
    (let [participants [{:name "A" :dex-mod 2} {:name "B" :dex-mod 0}]
          result (enc/roll-initiative participants)]
      (is (= 2 (count result)))
      (is (every? :initiative result))
      (is (>= (:initiative (first result)) (:initiative (second result)))))))
