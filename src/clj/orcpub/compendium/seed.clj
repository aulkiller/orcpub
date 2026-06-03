(ns orcpub.compendium.seed
  (:require [datomic.api :as d]
            [orcpub.dnd.e5.spells :as spells5e]
            [orcpub.dnd.e5.monsters :as monsters5e]
            [orcpub.dnd.e5.compendium.blood-hunter :as blood-hunter]
            [clojure.string :as str]
            [io.pedestal.log :as log]))

(defn- spell->compendium [spell]
  (let [k (or (:key spell) (keyword (str/replace (str/lower-case (:name spell)) #"\s+" "-")))]
    {:orcpub.compendium/key k
     :orcpub.compendium/type :spell
     :orcpub.compendium/name (:name spell)
     :orcpub.compendium/level (or (:level spell) 0)
     :orcpub.compendium/school (keyword (str/lower-case (or (:school spell) "evocation")))
     :orcpub.compendium/description (or (:description spell) (:summary spell) "")
     :orcpub.compendium/casting-time (or (:casting-time spell) "")
     :orcpub.compendium/range (or (:range spell) "")
     :orcpub.compendium/duration (or (:duration spell) "")
     :orcpub.compendium/components (str (when (get-in spell [:components :verbal]) "V")
                                        (when (get-in spell [:components :somatic]) ", S")
                                        (when (get-in spell [:components :material])
                                          (str ", M (" (get-in spell [:components :material-component]) ")")))
     :orcpub.compendium/concentration (boolean (:concentration spell))
     :orcpub.compendium/source "SRD"}))

(defn- monster->compendium [monster]
  (let [k (or (:key monster) (keyword (str/replace (str/lower-case (:name monster)) #"\s+" "-")))]
    {:orcpub.compendium/key k
     :orcpub.compendium/type :monster
     :orcpub.compendium/name (:name monster)
     :orcpub.compendium/cr (str (or (:challenge monster) "0"))
     :orcpub.compendium/monster-type (keyword (str/lower-case (or (:type monster) "humanoid")))
     :orcpub.compendium/size (or (:size monster) "Medium")
     :orcpub.compendium/hp (or (:hit-points monster) 0)
     :orcpub.compendium/ac (or (:armor-class monster) 10)
     :orcpub.compendium/description (pr-str (select-keys monster [:traits :actions :legendary-actions]))
     :orcpub.compendium/source "SRD"}))

(defn- all-spells []
  (let [ns-var (find-var 'orcpub.dnd.e5.spells/spell-map)]
    (if (and ns-var (map? @ns-var))
      (vals @ns-var)
      ;; Fallback: gather from letter vars
      (mapcat #(deref (find-var (symbol "orcpub.dnd.e5.spells" (str (char %) "-spells"))))
              (range (int \a) (inc (int \z)))))))

(defn- all-monsters []
  (or (:monsters-raw (deref (find-var 'orcpub.dnd.e5.monsters/monsters-raw)))
      @(find-var 'orcpub.dnd.e5.monsters/monsters-raw)))

(defn- class-feature->compendium [entry]
  {:orcpub.compendium/key (:key entry)
   :orcpub.compendium/type :class-feature
   :orcpub.compendium/name (:name entry)
   :orcpub.compendium/level (or (:level entry) 1)
   :orcpub.compendium/description (or (:description entry) "")
   :orcpub.compendium/source (or (:source entry) "Homebrew")})

(defn seed-compendium!
  "Seed the compendium with existing SRD spell and monster data.
   Safe to call multiple times — uses :db.unique/identity for upsert."
  [conn]
  (log/info :msg "Seeding compendium data...")
  (let [spells (try (all-spells) (catch Exception _ []))
        monsters (try (all-monsters) (catch Exception _ []))
        spell-entities (mapv spell->compendium (filter :name spells))
        monster-entities (mapv monster->compendium (filter :name monsters))
        bh-entities (mapv class-feature->compendium blood-hunter/all-entries)]
    (when (seq spell-entities)
      (doseq [batch (partition-all 50 spell-entities)]
        (try
          @(d/transact conn (vec batch))
          (catch Exception e
            (log/warn :msg "Spell batch failed" :error (.getMessage e))))))
    (when (seq monster-entities)
      (doseq [batch (partition-all 50 monster-entities)]
        (try
          @(d/transact conn (vec batch))
          (catch Exception e
            (log/warn :msg "Monster batch failed" :error (.getMessage e))))))
    (when (seq bh-entities)
      (doseq [batch (partition-all 50 bh-entities)]
        (try
          @(d/transact conn (vec batch))
          (catch Exception e
            (log/warn :msg "Blood Hunter batch failed" :error (.getMessage e))))))
    (log/info :msg (str "Compendium seeded: " (count spell-entities) " spells, "
                        (count monster-entities) " monsters, "
                        (count bh-entities) " Blood Hunter entries"))
    {:spells (count spell-entities)
     :monsters (count monster-entities)
     :blood-hunter (count bh-entities)}))
