(ns de.explorama.frontend.woco.welcome
  (:require [de.explorama.frontend.ui-base.components.frames.core :refer [loading-screen]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [card button]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [de.explorama.frontend.woco.api.registry :as registry]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.db :as woco-db]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.scale :as scale]
            [de.explorama.frontend.woco.copyright :as cr]
            [de.explorama.frontend.woco.util.api :refer [sub-error-boundary]]))

(defn activate-page [db active?]
  (assoc-in db path/welcome-active? active?))

(defn welcome-active? [db]
  (get-in db path/welcome-active? true))

(re-frame/reg-sub
 ::welcome-active?
 (fn [db _]
   (welcome-active? db)))

(re-frame/reg-sub
 ::welcome-callback?
 (fn [db _]
   (get-in db path/welcome-callback)))

(re-frame/reg-event-db
 ::welcome-callback
 (fn [db [_ callback-fx]]
   (assoc-in db path/welcome-callback callback-fx)))

(re-frame/reg-event-db
 ::welcome-active
 (fn [db [_ active?]]
   (activate-page db active?)))

(re-frame/reg-event-db
 ::welcome-loading
 (fn [db [_ loading?]]
   (assoc-in db path/welcome-loading? loading?)))

(re-frame/reg-sub
 ::welcome-loading?
 (fn [db _]
   (get-in db path/welcome-loading? false)))

(re-frame/reg-event-fx
 ::check-and-dispatch
 (fn [{db :db} [_ event]]
   {:fx [(if (woco-db/all-done? db)
           [:dispatch event]
           [:dispatch-later {:ms config/welcome-check-delay
                             :dispatch [::check-and-dispatch event]}])]}))

(defn dismiss-page [db callback-events]
  (let [all-init-done? (woco-db/all-done? db)
        dispatch-fn (if all-init-done?
                      (fn [e] [:dispatch e])
                      (fn [e] [:dispatch-later {:ms config/welcome-check-delay
                                                :dispatch [::check-and-dispatch e]}]))
        base-effects (if all-init-done?
                       []
                       [[:dispatch [::welcome-loading true]]
                        [:dispatch-later {:ms config/welcome-check-delay
                                          :dispatch [::check-and-dispatch [::welcome-loading false]]}]])]
    {:fx (into base-effects
               (comp (filter some?)
                     (map dispatch-fn))
               (concat [[::welcome-active false]
                        [:de.explorama.frontend.woco.workspace.background/reset]] callback-events))}))

(defn- translate [text-key default-value]
  (let [translation @(re-frame/subscribe [::i18n/translate text-key false])]
    (if (empty? translation) default-value translation)))

(defn- dismiss-welcome [callback-events]
  (re-frame/dispatch [:de.explorama.frontend.woco.api.welcome/dismiss-page callback-events]))

(re-frame/reg-sub
 ::tool-desc
 (fn [db [_ tool-id]]
   (get-in db (path/tool-desc tool-id))))

(re-frame/reg-event-fx
 ::start-tool
 (fn [{db :db} [_ tool-id {:keys [prevent-default?]}]]
   (let [action (get-in db (conj (path/tool-desc tool-id) :action))
         action-event (when action
                        (conj action nil {:overwrites {:behavior {:force :grid}}}))]
     (cond (and (seq (get-in db path/frames))
                action)
           {:dispatch (if prevent-default?
                        action-event
                        [(get-in db path/welcome-callback)
                         action-event])}
           (and (seq (get-in db path/frames))
                (not tool-id))
           {:dispatch [(get-in db path/welcome-callback)]}
           action
           {:dispatch action-event}))))

(re-frame/reg-event-fx
 ::start-product-tour
 (fn [{db :db} [_]]
   {:dispatch [(get-in db path/welcome-callback)
               [:de.explorama.frontend.woco.product-tour/start-tour]]}))

(defn cards []
  (let [start-new-workspace-text
        (translate :start-new-workspace "Start with a new workspace.")]
    [:div.welcome__section
     [:h2 start-new-workspace-text]
     [:div.grid.grid-cols-3.gap-16
      [card {:type :button
             :icon :search
             :icon-params {:size :3xl}
             :on-click #(dismiss-welcome [[::start-tool "tool-search"]])
             :title [:h2 (translate :welcome-card-search-title "Search")]
             :content (translate :welcome-card-search-desc "Start your analysis with the definition of a data set.")}]
      [card {:type :button
             :icon :atlas
             :icon-params {:size :3xl}
             :on-click #(dismiss-welcome [[::start-tool "data-atlas"]])
             :title [:h2 (translate :welcome-card-data-atlas-title "Data Atlas")]
             :content (translate :welcome-card-data-atlas-desc "Get an overview of the available data.")}]
      [card {:type :button
             :icon :tempimport
             :icon-params {:size :3xl}
             :on-click #(dismiss-welcome [[::start-tool "expdb-temp-import" {:prevent-default? true}]])
             :title [:h2 (translate :welcome-card-import-title "Import")]
             :content (translate :welcome-card-import-desc "Use your data for analysis.")}]]]))

(defn tips []
  (let [welcome-page-help-title (translate :welcome-page-help "Help")
        welcome-tips-and-tricks-title (translate :welcome-tips-and-tricks-title "Tips and Tricks")
        product-tour-title (translate :welcome-page-product-tour "Product tour")]
    [:div.welcome__section
     [:h2 welcome-page-help-title]
     [:div.help__section
      [:div.grid.grid-cols-8.gap-16.w-full
       [:div.col-span-1
        [card {:type :button
               :icon "icon-tour"
               :icon-position :end
               :show-divider? false
               :orientation :vertical
               :title product-tour-title
               :on-click #(dismiss-welcome [[::start-product-tour]])}]]
       [:div.col-span-7
        [card {:type :carousel
               :full-height? true
               :auto-slide? true
               :slide-timeout-ms config/welcome-tips-interval-ms
               :slide-direction :left
               :items (mapv #(hash-map :title welcome-tips-and-tricks-title
                                       :content (translate (keyword (str "welcome-tips-and-tricks-text-" %)) "Just a few seconds..."))
                            (range 17))}]]]]]))

(re-frame/reg-sub
 ::welcome-sections
 :<- [::registry/lookup-category :welcome-section]
 (fn [welcome-sections _]
   (sort-by (comp :order second) welcome-sections)))

(defn page []
  (when @(re-frame/subscribe [::welcome-active?])
    (let [welcome-text-template
          (translate :welcome-text-template "Welcome to %s!")
          welcome-close-text
          (translate :welcome-close "Close overview")
          welcome-sections
          @(re-frame/subscribe [::welcome-sections])]
      [:div.welcome__page
       {:style {:z-index 1, :position :absolute}}
       [loading-screen {:show? (re-frame/subscribe [::welcome-loading?])
                        :message
                        (translate :welcome-loading-message
                                   "Finishing loading")
                        :tip-title
                        (translate :welcome-loading-tip-title
                                   "Almost done")
                        :tip
                        (translate :welcome-loading-tip
                                   "Just a few seconds...")}]
       (into
        [:div.welcome__panel
         [:div.welcome__header
          [:div.welcome__text
           (format welcome-text-template config/system-name)]
          [button {:label welcome-close-text
                   :extra-class "welcome__close"
                   :variant :tertiary
                   :start-icon :close
                   :on-click #(dismiss-welcome [])}]]]
        (map (fn [[section-key section]]
               ^{:key section-key} [(:render-fn section)]))
        welcome-sections)
       [cr/links :right]])))
