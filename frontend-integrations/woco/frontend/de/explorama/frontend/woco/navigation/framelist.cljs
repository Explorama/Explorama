(ns de.explorama.frontend.woco.navigation.framelist
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.woco.frame.view.header :as header]
            [de.explorama.frontend.woco.navigation.resources :as resources]
            [de.explorama.frontend.woco.screenshot.core :as screenshot]
            [de.explorama.frontend.woco.screenshot.util :as screenshot-util]
            [taoensso.timbre :refer [debug]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [loading-message button checkbox]]
            [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]))

(re-frame/reg-event-db
 ::toggle
 (fn [db [_ flag]]
   (cond-> db
     (boolean? flag) (assoc-in path/show-framelist? flag)
     (nil? flag) (update-in path/show-framelist? not))))

(re-frame/reg-sub
 ::show?
 (fn [db]
   (get-in db path/show-framelist? false)))

(re-frame/reg-event-db
 ::show-frame-screenshots
 (fn [db [_ flag]]
   (cond-> db
     (boolean? flag) (assoc-in path/show-frame-screenshots? flag)
     (nil? flag) (update-in path/show-frame-screenshots? not))))

(re-frame/reg-sub
 ::show-frame-screenshots?
 (fn [db]
   (get-in db path/show-frame-screenshots? false)))

(defn- frame-icon [frame-icon color]
  [:div.frame__icon {:style {:background-color (resources/frame-color->css-rgb (get @resources/frame-colors color [173 181 189]))}}
   [icon {:icon (keyword frame-icon)
          :color :white}]])

(defonce previews (r/atom {}))
(defonce requesting-preview (r/atom #{}))


(defn- clear-previews []
  (reset! previews {})
  (reset! requesting-preview #{}))

(defn- remove-from-preview [{:keys [id]}]
  (swap! previews dissoc id)
  (swap! requesting-preview disj id))

(defn- refresh-previews [frames timeout]
  (doseq [{:keys [id] :as frame} frames]
    (debug "Refresh preview for" id)
    (swap! requesting-preview conj id)
    (js/setTimeout #(screenshot/make-frame-screenshot {:frame-id id
                                                       :callback-fn (fn [r]
                                                                      (cond
                                                                        (screenshot-util/base64-valid? r)
                                                                        (do
                                                                          (swap! previews assoc id r)
                                                                          (swap! requesting-preview disj id))

                                                                        (js/document.getElementById (config/frame-dom-id id))
                                                                        (refresh-previews [frame] 0)

                                                                        :else
                                                                        (swap! requesting-preview disj id)))})

                   timeout)))


(defn- frame-item [frame _ show-frame-screenshots? navigation-framelist-preview-download-tooltip]
  (r/create-class
   {:display-name "framelist item"
    :reagent-render (fn [{:keys [di id color-group]} fr-icon show-frame-screenshots?]
                      (let [component-preview (get @previews id)
                            title (header/full-title id)]
                        [:div.framelist__entry {:on-click #(re-frame/dispatch [:de.explorama.frontend.woco.navigation.control/focus id nil true])}
                         [:div.entry__content
                          [frame-icon fr-icon (when di color-group)]
                          [:div.frame__title title]
                          (when (and component-preview show-frame-screenshots?)
                            [tooltip {:text navigation-framelist-preview-download-tooltip
                                      :extra-class "button__download"}
                             [button {:start-icon :download
                                      :variant :secondary
                                      :size :small
                                      :on-click #(do
                                                   (.preventDefault %)
                                                   (.stopPropagation %)
                                                   (screenshot-util/download-base64-img component-preview (str title ".png")))}]])]
                         (when show-frame-screenshots?
                           [:div.preview__image
                            [loading-message {:show? (not (boolean component-preview))}]
                            [:img {:src component-preview
                                   :on-drag-start #(.preventDefault %)
                                   :draggable false}]])]))
    :component-did-update (fn [this [_ _ _ old-show-frame-screenshots?]]
                            (let [[_ frame _ new-show-frame-screenshot?] (r/argv this)]
                              (when (not= old-show-frame-screenshots? new-show-frame-screenshot?)
                                (if new-show-frame-screenshot?
                                  (refresh-previews [frame] 1000)
                                  (remove-from-preview frame)))))
    :component-did-mount (fn []
                           (when show-frame-screenshots?
                             ;; timeout for maybe getting the final vis with data
                             (refresh-previews [frame] 3000)))
    :component-will-unmount #(remove-from-preview frame)}))


(defn framelist [toggle-framelist?]
  (let [frames @(re-frame/subscribe [:de.explorama.frontend.woco.frame.api/frames-positions (fn [{ftype :type}]
                                                                                     (not= ftype :frame/custom-type))])
        toolbar-items @(re-frame/subscribe [:de.explorama.frontend.woco.tools/tool-minimap-icons])
        show-frame-screenshots? @(re-frame/subscribe [::show-frame-screenshots?])
        pending-requests? (boolean (not-empty @requesting-preview))
        {:keys [navigation-framelist-preview-toggle-tooltip
                navigation-framelist-preview-toggle-label
                navigation-framelist-preview-download-tooltip
                navigation-framelist-preview-refresh-tooltip]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :navigation-framelist-preview-toggle-tooltip
                              :navigation-framelist-preview-toggle-label
                              :navigation-framelist-preview-download-tooltip
                              :navigation-framelist-preview-refresh-tooltip])]
    [:div.viewport__controls__framelist
     [:div.framelist__buttons
      [tooltip {:text navigation-framelist-preview-toggle-tooltip}
       [checkbox {:checked? show-frame-screenshots?
                  :as-toggle? true
                  :label-params {:extra-class "input--w10"}
                  :disabled? pending-requests?
                  :label navigation-framelist-preview-toggle-label
                  :on-change #(do
                                (clear-previews)
                                (re-frame/dispatch [::show-frame-screenshots (not show-frame-screenshots?)]))}]]
      (when show-frame-screenshots?
        [tooltip {:text navigation-framelist-preview-refresh-tooltip}
         [button {:extra-class (cond-> "btn-minimap"
                                 toggle-framelist? (str " active"))
                  :disabled? pending-requests?
                  :on-click #(do
                               (clear-previews)
                               (refresh-previews frames 0))
                  :start-icon :reset}]])]
     (reduce (fn [acc {:keys [id di] :as frame}]
               (cond-> acc
                 id
                 (conj
                  (with-meta
                    [frame-item frame (if (= config/details-view-vertical-namespace (:vertical id))
                                        config/details-view-icon
                                        (get toolbar-items (:vertical id)))
                     show-frame-screenshots? navigation-framelist-preview-download-tooltip]
                    {:key (str "frame-list___" id di)}))))
             [:div.framelist__entries]
             (sort-by (fn [fr]
                        (or (get-in fr config/framelist-order)
                            "zzzzzzzzz"))
                      frames))]))

(defn toggle [toggle-framelist?]
  (let [navigation-framelist @(re-frame/subscribe [::i18n/translate :navigation-framelist])]
    {:id "viewport-framelist"
     :title navigation-framelist
     :icon :window-list
     :on-click #(re-frame/dispatch [::toggle])
     :active? toggle-framelist?}))