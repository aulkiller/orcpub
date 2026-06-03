(ns orcpub.compendium.sync
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [datomic.api :as d]
            [io.pedestal.log :as log]))

(def open5e-base "https://api.open5e.com/v1")

(defn- fetch-page [url]
  (let [resp (http/get url {:as :json :throw-exceptions false})]
    (when (= 200 (:status resp))
      (:body resp))))

(defn- fetch-all [endpoint]
  (loop [url (str open5e-base "/" endpoint "/?format=json&limit=100")
         all []]
    (if-not url
      all
      (let [{:keys [results next]} (fetch-page url)]
        (recur next (into all results))))))

(defn- spell->entity [s]
  {:orcpub.compendium/type :spell
   :orcpub.compendium/key (keyword (:slug s))
   :orcpub.compendium/name (:name s)
   :orcpub.compendium/level (or (:level_int s) 0)
   :orcpub.compendium/school (keyword (str/lower-case (or (:school s) "evocation")))
   :orcpub.compendium/description (:desc s)
   :orcpub.compendium/casting-time (:casting_time s)
   :orcpub.compendium/range (:range s)
   :orcpub.compendium/duration (:duration s)
   :orcpub.compendium/components (:components s)
   :orcpub.compendium/concentration (boolean (:concentration s))
   :orcpub.compendium/source (or (:document__title s) "Open5e")})

(defn- monster->entity [m]
  {:orcpub.compendium/type :monster
   :orcpub.compendium/key (keyword (:slug m))
   :orcpub.compendium/name (:name m)
   :orcpub.compendium/cr (:cr m)
   :orcpub.compendium/monster-type (keyword (str/lower-case (or (:type m) "humanoid")))
   :orcpub.compendium/size (:size m)
   :orcpub.compendium/hp (:hit_points m)
   :orcpub.compendium/ac (:armor_class m)
   :orcpub.compendium/description (pr-str (select-keys m [:actions :special_abilities :legendary_actions]))
   :orcpub.compendium/source (or (:document__title m) "Open5e")})

(defn sync-spells! [conn]
  (log/info :msg "Syncing spells from Open5e...")
  (let [spells (fetch-all "spells")
        entities (mapv spell->entity spells)]
    (doseq [batch (partition-all 100 entities)]
      (d/transact conn (vec batch)))
    (log/info :msg (str "Synced " (count entities) " spells"))
    {:synced (count entities) :type :spell}))

(defn sync-monsters! [conn]
  (log/info :msg "Syncing monsters from Open5e...")
  (let [monsters (fetch-all "monsters")
        entities (mapv monster->entity monsters)]
    (doseq [batch (partition-all 100 entities)]
      (d/transact conn (vec batch)))
    (log/info :msg (str "Synced " (count entities) " monsters"))
    {:synced (count entities) :type :monster}))
