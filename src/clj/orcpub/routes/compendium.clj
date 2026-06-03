(ns orcpub.routes.compendium
  (:require [datomic.api :as d]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [orcpub.dnd.e5.compendium :as comp]))

(defn search [{:keys [db] :as request}]
  (let [{:keys [query content-type filters]} (:body request)
        results (d/q '[:find [(pull ?e [*]) ...]
                       :in $ ?type
                       :where [?e :orcpub.compendium/type ?type]]
                     db (or content-type :spell))
        filtered (comp/search results query (or filters {}))]
    {:status 200
     :body {:results (take 50 filtered)
            :total (count filtered)}}))

(defn get-entry [{:keys [db] :as request}]
  (let [entry-type (keyword (get-in request [:path-params :type]))
        entry-key (keyword (get-in request [:path-params :key]))
        result (d/q '[:find (pull ?e [*]) .
                      :in $ ?type ?key
                      :where [?e :orcpub.compendium/type ?type]
                             [?e :orcpub.compendium/key ?key]]
                    db entry-type entry-key)]
    (if result
      {:status 200 :body result}
      {:status 404 :body {:error "Not found"}})))

(defn list-by-type [{:keys [db] :as request}]
  (let [content-type (keyword (get-in request [:path-params :type]))
        results (d/q '[:find [(pull ?e [:orcpub.compendium/key
                                        :orcpub.compendium/name
                                        :orcpub.compendium/level
                                        :orcpub.compendium/cr
                                        :orcpub.compendium/school
                                        :orcpub.compendium/rarity
                                        :orcpub.compendium/source]) ...]
                       :in $ ?type
                       :where [?e :orcpub.compendium/type ?type]]
                     db content-type)]
    {:status 200 :body results}))
