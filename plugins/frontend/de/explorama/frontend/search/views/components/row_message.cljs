(ns de.explorama.frontend.search.views.components.row-message
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.search.views.validation :as validation]))

(defn row-message [frame-id path attr-desc]
  (let [{:keys [req-attrs-valid? req-attrs-num-valid? req-attrs-infotext
                req-attrs-num-infotext] :as info}
        @(re-frame/subscribe [::validation/row-valid-infos frame-id path attr-desc])]
    [:<>
     (cond
       (not req-attrs-num-valid?)
       [:div.form__message req-attrs-num-infotext]
       (not req-attrs-valid?)
       [:div.form__message req-attrs-infotext])]))
