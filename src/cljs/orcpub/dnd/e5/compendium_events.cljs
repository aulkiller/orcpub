(ns orcpub.dnd.e5.compendium-events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]))

(reg-event-fx
 ::search
 (fn [{:keys [db]} [_ query content-type filters]]
   {:db (assoc db :compendium-loading? true)
    :http {:method :post
           :url "/api/compendium/search"
           :body {:query query :content-type content-type :filters filters}
           :on-success [::search-results]
           :on-failure [::search-error]}}))

(reg-event-db
 ::search-results
 (fn [db [_ response]]
   (-> db
       (assoc :compendium-results (:results response))
       (assoc :compendium-total (:total response))
       (assoc :compendium-loading? false))))

(reg-event-db
 ::search-error
 (fn [db _]
   (assoc db :compendium-loading? false)))

(reg-event-fx
 ::get-entry
 (fn [{:keys [db]} [_ entry-type entry-key]]
   {:http {:method :get
           :url (str "/api/compendium/" (name entry-type) "/" (name entry-key))
           :on-success [::entry-loaded]}}))

(reg-event-db
 ::entry-loaded
 (fn [db [_ entry]]
   (assoc db :compendium-detail entry)))

(reg-event-db
 ::close-detail
 (fn [db _]
   (dissoc db :compendium-detail)))

(reg-event-db
 ::set-content-type
 (fn [db [_ t]]
   (assoc db :compendium-type t)))
