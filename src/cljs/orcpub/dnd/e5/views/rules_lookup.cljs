(ns orcpub.dnd.e5.views.rules-lookup
  (:require [re-frame.core :refer [subscribe dispatch]]
            [orcpub.dnd.e5.play-events :as events]
            [orcpub.dnd.e5.play-subs :as subs]))

(defn rules-lookup-modal []
  (let [lookup @(subscribe [::subs/rules-lookup])]
    (when (:open? lookup)
      [:div.rules-modal-backdrop
       {:on-click #(dispatch [::events/close-rules-lookup])}
       [:div.rules-modal
        {:on-click #(.stopPropagation %)}
        [:div.rules-modal-header
         [:h3 (name (or (:entry-key lookup) ""))]
         [:button.rules-close {:on-click #(dispatch [::events/close-rules-lookup])} "×"]]
        [:div.rules-modal-body
         [:p "Loading content from compendium..."]
         [:p.rules-placeholder "(Full compendium integration in Unit 3)"]]]])))
