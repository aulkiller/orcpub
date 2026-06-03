(ns orcpub.dice
  (:require [clojure.spec.alpha :as spec]
            [orcpub.common :as common]))

(def dice-sides [4 6 8 10 12 20 100])

(defn die-roll [sides]
  (inc (rand-int sides)))

(defn roll-n [num sides]
  (repeatedly num #(die-roll sides)))

(defn dice-roll [{:keys [num sides drop-num modifier]}]
  (apply +
         (or modifier 0)
         (drop (or drop-num 0)
               (sort (roll-n num sides)))))

(defn dice-string [die die-count modifier]
  (str die "d" die-count (common/mod-str modifier)))

(defn die-mean-round-down [die]
  (int (Math/floor (/ (apply + (range 1 (inc die))) die))))

(defn die-mean-round-up [die]
  (int (Math/ceil (/ (apply + (range 1 (inc die))) die))))

(defn dice-mean-round-down [num sides modifier]
  (int (Math/floor (+ modifier (* num (/ (apply + (range 1 (inc sides))) sides))))))

(def dice-regex #"(\d+)?d(\d+)\s?([+-])?\s?(\d+)?")

(defn parse-int [s]
  (when s
    #?(:cljs (js/parseInt s))
    #?(:clj (Integer/valueOf s))))

(defn dice-roll-text [dice-text]
  (when-let [[_ num-str sides-str plus-minus-str mod-str :as match]
           (re-matches dice-regex dice-text)]
    (let [num (or (parse-int num-str) 1)
          sides (parse-int sides-str)
          plus-minus (if (= "-" plus-minus-str)
                       -1
                       1)
          raw-mod (or (parse-int mod-str) 0) 
          mod (* raw-mod plus-minus)
          rolls (roll-n num sides)
          total (apply + mod rolls)]
      {:total total
       :rolls rolls
       :mod mod
       :raw-mod raw-mod
       :plus-minus plus-minus})))

(defn dice-roll-text-2 [dice-text]
  (when-let [[_ num-str sides-str plus-minus-str mod-str :as match]
           (re-matches dice-regex dice-text)]
    (let [num (or (parse-int num-str) 1)
          sides (parse-int sides-str)
          plus-minus (if (= "-" plus-minus-str)
                       -1
                       +1)
          raw-mod (or (parse-int mod-str) +0)
          mod (* raw-mod plus-minus)
          rolls (roll-n num sides)
          total (apply + mod rolls)]
      ;(prn (str "total:" total " roll:" rolls " plus-minus:" plus-minus-str " raw-mod:" raw-mod " dice-text:" dice-text))
      (if (boolean (re-find  #"(20)" (str rolls)))
        (str " ***  NATURAL 20  ***  " rolls " " plus-minus-str " " raw-mod " = " total)
        (str rolls " " plus-minus-str " " raw-mod " = " total)))))

(spec/def ::num pos-int?)
(spec/def ::sides pos-int?)
(spec/def ::drop-num pos-int?)
(spec/def ::modifier pos-int?)
(spec/def ::roll-args (spec/keys :req-un [::num ::sides]
                                 :opt-un [::drop-num ::modifier]))
(spec/fdef
 dice-roll
 :args (spec/cat :x ::roll-args)
 :ret pos-int?)

;; --- Play Mode Extensions ---

(defn parse-notation
  "Parse XdY+Z notation into a map. Returns nil if invalid."
  [notation]
  (when-let [[_ num-str sides-str plus-minus-str mod-str]
             (re-matches dice-regex (str notation))]
    {:num (or (parse-int num-str) 1)
     :sides (parse-int sides-str)
     :modifier (* (or (parse-int mod-str) 0)
                  (if (= "-" plus-minus-str) -1 1))}))

(defn roll-full
  "Roll dice and return full result with individual die values."
  [{:keys [num sides modifier] :or {num 1 modifier 0}}]
  (let [results (vec (roll-n num sides))
        total (+ modifier (apply + results))]
    {:dice (mapv (fn [r] {:sides sides :result r}) results)
     :modifier modifier
     :total total}))

(defn roll-with-advantage
  "Roll 2d20, keep highest, add modifier."
  [modifier]
  (let [r1 (die-roll 20)
        r2 (die-roll 20)
        kept (max r1 r2)]
    {:dice [{:sides 20 :result r1} {:sides 20 :result r2}]
     :kept kept
     :modifier (or modifier 0)
     :total (+ kept (or modifier 0))
     :advantage? true}))

(defn roll-with-disadvantage
  "Roll 2d20, keep lowest, add modifier."
  [modifier]
  (let [r1 (die-roll 20)
        r2 (die-roll 20)
        kept (min r1 r2)]
    {:dice [{:sides 20 :result r1} {:sides 20 :result r2}]
     :kept kept
     :modifier (or modifier 0)
     :total (+ kept (or modifier 0))
     :disadvantage? true}))
