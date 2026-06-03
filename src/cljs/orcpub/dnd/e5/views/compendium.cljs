(ns orcpub.dnd.e5.views.compendium
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [orcpub.dnd.e5.compendium-events :as events]))

(defn search-bar []
  (let [query (r/atom "")
        content-type (r/atom :spell)]
    (fn []
      [:div.compendium-search
       [:select.compendium-type-select
        {:value (name @content-type)
         :on-change #(let [t (keyword (.. % -target -value))]
                       (reset! content-type t)
                       (dispatch [::events/set-content-type t]))}
        [:option {:value "spell"} "Spells"]
        [:option {:value "monster"} "Monsters"]
        [:option {:value "item"} "Items"]]
       [:input.compendium-search-input
        {:type "text" :placeholder "Search..."
         :value @query
         :on-change #(reset! query (.. % -target -value))
         :on-key-press (fn [e]
                         (when (= 13 (.-charCode e))
                           (dispatch [::events/search @query @content-type {}])))}]
       [:button.play-button
        {:on-click #(dispatch [::events/search @query @content-type {}])}
        "Search"]])))

(defn result-item [{:keys [orcpub.compendium/key orcpub.compendium/name
                           orcpub.compendium/level orcpub.compendium/school
                           orcpub.compendium/cr orcpub.compendium/rarity]}]
  [:div.compendium-result
   {:on-click #(dispatch [::events/get-entry
                           (or (:orcpub.compendium/type %) :spell) key])}
   [:span.result-name name]
   (when level [:span.result-meta (str "Level " level)])
   (when school [:span.result-meta (clojure.core/name school)])
   (when cr [:span.result-meta (str "CR " cr)])
   (when rarity [:span.result-meta (clojure.core/name rarity)])])

(defn result-list []
  (let [results @(subscribe [:compendium-results])
        loading? @(subscribe [:compendium-loading?])]
    [:div.compendium-results
     (if loading?
       [:div.loading "Searching..."]
       (if (seq results)
         (for [r results]
           ^{:key (:orcpub.compendium/key r)}
           [result-item r])
         [:div.no-results "No results. Try a different search."]))]))

(defn detail-panel []
  (let [detail @(subscribe [:compendium-detail])]
    (when detail
      [:div.compendium-detail-backdrop
       {:on-click #(dispatch [::events/close-detail])}
       [:div.compendium-detail
        {:on-click #(.stopPropagation %)}
        [:div.detail-header
         [:h2 (:orcpub.compendium/name detail)]
         [:button.rules-close {:on-click #(dispatch [::events/close-detail])} "×"]]
        [:div.detail-body
         (when-let [d (:orcpub.compendium/description detail)]
           [:p d])
         (when-let [ct (:orcpub.compendium/casting-time detail)]
           [:p [:strong "Casting Time: "] ct])
         (when-let [r (:orcpub.compendium/range detail)]
           [:p [:strong "Range: "] r])
         (when-let [d (:orcpub.compendium/duration detail)]
           [:p [:strong "Duration: "] d])
         (when-let [c (:orcpub.compendium/components detail)]
           [:p [:strong "Components: "] c])
         (when-let [s (:orcpub.compendium/source detail)]
           [:p.detail-source (str "Source: " s)])]]])))

(defn compendium-page []
  [:div.compendium-page
   [:h1 "Compendium"]
   [search-bar]
   [result-list]
   [detail-panel]])
