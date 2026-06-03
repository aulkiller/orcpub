(ns orcpub.routes.campaign
  (:require [datomic.api :as d]
            [io.pedestal.interceptor :refer [interceptor]]
            [orcpub.dnd.e5.campaign :as campaign]))

(defn create-campaign [{:keys [conn username body] :as request}]
  (let [{:keys [name]} body
        code (campaign/generate-invite-code)
        tx @(d/transact conn [{:db/id "new-campaign"
                               :orcpub.campaign/name name
                               :orcpub.campaign/owner username
                               :orcpub.campaign/invite-code code
                               :orcpub.campaign/members []}])]
    {:status 201
     :body {:db/id (d/resolve-tempid (:db-after tx) (:tempids tx) "new-campaign")
            :invite-code code}}))

(defn list-campaigns [{:keys [db username] :as request}]
  (let [campaigns (d/q '[:find [(pull ?c [*]) ...]
                          :in $ ?owner
                          :where (or [?c :orcpub.campaign/owner ?owner]
                                     [?c :orcpub.campaign/members ?owner])]
                        db username)]
    {:status 200 :body campaigns}))

(defn get-campaign [{:keys [db] :as request}]
  (let [id (Long/parseLong (get-in request [:path-params :id]))
        campaign (d/pull db '[*] id)]
    {:status 200 :body campaign}))

(defn generate-invite [{:keys [conn db] :as request}]
  (let [id (Long/parseLong (get-in request [:path-params :id]))
        code (campaign/generate-invite-code)]
    @(d/transact conn [[:db/add id :orcpub.campaign/invite-code code]])
    {:status 200 :body {:code code}}))

(defn join-campaign [{:keys [conn db username body] :as request}]
  (let [{:keys [code character-id]} body
        campaign (d/q '[:find ?c .
                        :in $ ?code
                        :where [?c :orcpub.campaign/invite-code ?code]]
                      db code)]
    (if campaign
      (do
        @(d/transact conn [[:db/add campaign :orcpub.campaign/members username]
                           [:db/add campaign :orcpub.campaign/character-ids character-id]])
        {:status 200 :body {:status :joined :campaign-id campaign}})
      {:status 404 :body {:error "Invalid invite code"}})))

(defn leave-campaign [{:keys [conn db username] :as request}]
  (let [id (Long/parseLong (get-in request [:path-params :id]))]
    @(d/transact conn [[:db/retract id :orcpub.campaign/members username]])
    {:status 200 :body {:status :left}}))

(defn remove-member [{:keys [conn db] :as request}]
  (let [id (Long/parseLong (get-in request [:path-params :id]))
        member (get-in request [:path-params :member])]
    @(d/transact conn [[:db/retract id :orcpub.campaign/members member]])
    {:status 200 :body {:status :removed}}))

(def check-campaign-owner
  (interceptor
   {:name ::check-campaign-owner
    :enter (fn [ctx]
             (let [db (get-in ctx [:request :db])
                   id (Long/parseLong (get-in ctx [:request :path-params :id]))
                   username (get-in ctx [:request :username])
                   owner (:orcpub.campaign/owner (d/entity db id))]
               (if (= owner username)
                 ctx
                 (assoc ctx :response {:status 403 :body {:error "Not campaign owner"}}))))}))
