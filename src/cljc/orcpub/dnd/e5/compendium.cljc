(ns orcpub.dnd.e5.compendium
  (:require [clojure.string :as str]))

(def content-types #{:spell :monster :item :class-feature :racial-trait :feat})

(def spell-schools #{:abjuration :conjuration :divination :enchantment
                     :evocation :illusion :necromancy :transmutation})

(def monster-types #{:aberration :beast :celestial :construct :dragon :elemental
                     :fey :fiend :giant :humanoid :monstrosity :ooze :plant :undead})

(def item-types #{:weapon :armor :potion :ring :rod :scroll :staff :wand :wondrous})

(def rarities #{:common :uncommon :rare :very-rare :legendary :artifact})

(defn match-query? [entry query]
  (let [q (str/lower-case (or query ""))]
    (or (str/blank? q)
        (str/includes? (str/lower-case (or (:name entry) "")) q)
        (str/includes? (str/lower-case (or (:description entry) "")) q))))

(defn match-filters? [entry filters]
  (every? (fn [[k v]]
            (or (nil? v)
                (= v (get entry k))
                (and (set? v) (contains? v (get entry k)))))
          filters))

(defn search [entries query filters]
  (->> entries
       (filter #(match-query? % query))
       (filter #(match-filters? % filters))))
