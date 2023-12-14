(ns de.explorama.frontend.configuration.views.data-management.geographic-attributes
  (:require [re-frame.core :as re-frame :refer [reg-event-db reg-sub dispatch subscribe]]
            [de.explorama.frontend.configuration.path :as path]
            [clojure.set :refer [difference]]
            [de.explorama.frontend.common.views.legend :refer [attr->display-name]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button select]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.configuration.configs.config-types.geographic :as geo-configs]
            [de.explorama.frontend.configuration.data.core :as data]
            [de.explorama.frontend.configuration.configs.persistence :as persistence]
            [de.explorama.shared.configuration.data-management.geographic-attributes-config :as ga-config]))

(def ^:private required-geo-attribute? (into #{} ga-config/initial-geographic-attributes))

(reg-sub
 ::geographic-attribute-value
 (fn [db]
   (get-in db path/geographic-attribute-value [])))

(reg-event-db
 ::set-geographic-attribute-value
 (fn [db [_ options]]
   (assoc-in db path/geographic-attribute-value options)))

(reg-sub
 ::current-geographic-attributes
 (fn [db]
   (get-in db path/current-geographic-attributes [])))

(reg-event-db
 ::set-current-geographic-attributes
 (fn [db [_ attributes]]
   (assoc-in db path/current-geographic-attributes attributes)))

(reg-event-db
 ::add-geo-attr-frame
 (fn [db [_ active?]]
   (-> db
       (assoc-in path/add-geo-attr-frame? active?))))

(reg-sub
 ::add-geo-attr-frame?
 (fn [db _]
   (get-in db path/add-geo-attr-frame?)))


(defn add-geographic-attribute-dialog []
  (let [selected-option @(subscribe [::geographic-attribute-value])
        add-attribute-label @(subscribe [::i18n/translate :config-add-attribute-label])
        attribute-label @(subscribe [::i18n/translate :attribute-label])
        cancel-label @(subscribe [::i18n/translate :cancel-label])
        current-geo-attributes  @(subscribe [::current-geographic-attributes])
        context-attributes (into #{} (keys
                                      @(subscribe [:de.explorama.frontend.configuration.data.core/context-attributes])))
        selectable-attributes (difference context-attributes (into #{} current-geo-attributes))
        labels @(fi/call-api [:i18n :get-labels-sub])
        options (into [] (sort-by :label (map (fn [attr] {:label (attr->display-name attr labels), :value attr})
                                              selectable-attributes)))]
    [dialog {:title add-attribute-label
             :message
             [:<>
              [:div.row
               [:div.col-11
                [:div.explorama__form__input.explorama__form--info
                 {;:class  "explorama__form--info"
                  :style {:margin-bottom "0px"}}
                 [select {:options options
                          :label attribute-label
                          :values selected-option
                          :on-change #(re-frame/dispatch [::set-geographic-attribute-value %])}]]]]]
             :show? true
             :hide-fn #(do (re-frame/dispatch [::add-geo-attr-frame false])
                           (re-frame/dispatch [::set-geographic-attribute-value nil]))
             :yes {:label add-attribute-label
                   :disabled? (= 0 (count selected-option))
                   :on-click #(do (re-frame/dispatch [::set-current-geographic-attributes
                                                      (conj current-geo-attributes (:value selected-option))])
                                  (re-frame/dispatch [::add-geo-attr-frame false])
                                  (re-frame/dispatch [::set-geographic-attribute-value nil]))}
             :no {:label cancel-label
                  :variant :secondary}}]))


(defn geo-attribute-element [idx geo-attributes read-only?]
  (let [max-index (dec (count geo-attributes))
        attr (nth geo-attributes idx)
        first-slide? (= idx 0)
        last-slide? (= idx max-index)
        required? (required-geo-attribute? attr)
        swap (fn
               [items i j]
               (assoc items i (items j) j (items i)))
        remove (fn
                 [v idx]
                 (into (subvec v 0 idx) (subvec v (inc idx))))
        labels @(fi/call-api [:i18n :get-labels-sub])]
    [:div.card__element.clickable
     [:div.order__controls
      [button {:start-icon :arrow-up
               :extra-class "order__up"
               :on-click #(let [reordered-geo-attributes (swap geo-attributes idx (dec idx))]
                            (dispatch [::set-current-geographic-attributes
                                       reordered-geo-attributes]))
               :disabled?
               (or first-slide? read-only?)}
       [icon {:icon :arrow-up}]]
      [button {:start-icon :arrow-down
               :extra-class "order__down"
               :on-click #(let [reordered-geo-attributes (swap geo-attributes idx (inc idx))]
                            (dispatch [::set-current-geographic-attributes
                                       reordered-geo-attributes]))
               :disabled? (or last-slide? read-only?)}]]
     [:div.card__content
      [:div.title__bar
       [:h3 (attr->display-name attr labels)]
       [button {:start-icon :close
                :on-click #(let [pruned-geo-attributes (remove geo-attributes idx)]
                             (dispatch [::set-current-geographic-attributes
                                        pruned-geo-attributes]))
                :disabled? (or read-only? required?)}]]]]))

(defn footer []
  (let [{:keys [save-label
                config-geo-attributes-saved-msg
                config-geo-attr-save-failed-msg]}
        @(re-frame/subscribe [::i18n/translate-multi :save-label :config-geo-attributes-saved-msg
                              :config-geo-attr-save-failed-msg])
        prev-geo-attributes  @(subscribe [::data/geographic-attributes])
        geo-attributes @(subscribe [::current-geographic-attributes])]
    [:div.footer
     [button {:start-icon :save
              :size :big
              :disabled? (=  prev-geo-attributes geo-attributes)
              :variant :primary
              :on-click #(dispatch [::persistence/save-and-commit
                                    geo-configs/config-type
                                    ga-config/geographic-attributes-id
                                    {:id ga-config/geographic-attributes-id
                                     ga-config/geographic-attributes-key geo-attributes}
                                    {:trigger-action :list-entries
                                     :success-callback
                                     (fn []
                                       (dispatch (fi/call-api :notify-event-vec
                                                              {:type :success
                                                               :category {:config :save}
                                                               :message config-geo-attributes-saved-msg})))
                                     :failed-callback
                                     (fn []
                                       (dispatch (fi/call-api :notify-event-vec
                                                              {:type :error
                                                               :category {:config :save}
                                                               :message config-geo-attr-save-failed-msg})))}])
              :label save-label}]]))


(defn geo-attributes []
  (let [current-geo-attributes @(subscribe [::current-geographic-attributes])
        add-attribute-label @(re-frame/subscribe [::i18n/translate :config-add-attribute-label])
        read-only? false]
    [:<>
     [:div.content
      [:div.card__list__ordered
       (for [idx (range (count current-geo-attributes))]
         ^{:key (str "geo-attributes-" (nth current-geo-attributes idx))}
         [geo-attribute-element
          idx
          current-geo-attributes
          read-only?])
       [:div.card__button.card__element {:on-click #(dispatch [::add-geo-attr-frame true])}
        [icon {:icon :plus}]
        add-attribute-label]]]
     (when @(subscribe [::add-geo-attr-frame?])
       [add-geographic-attribute-dialog])
     [footer]]))
