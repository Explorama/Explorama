(ns de.explorama.frontend.search.views.komplexe-suche-view
  (:require [re-frame.core :refer [reg-sub reg-event-db reg-event-fx
                                   subscribe dispatch]]
            [de.explorama.frontend.search.config :as config]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.frames.core :refer [loading-screen]]
            [de.explorama.frontend.search.views.main-search.core :refer [free-view]]
            [de.explorama.frontend.search.views.components.frame-notifications :refer [frame-notifications]]
            [de.explorama.frontend.search.backend.traffic-lights :as traffic-lights-backend]
            [de.explorama.frontend.search.views.validation :as validation]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [reagent.core :as r]
            [de.explorama.frontend.search.data.di :as data-di]
            [de.explorama.frontend.search.path :as spath]
            [vimsical.re-frame.fx.track :as track]))

(defn loading-states [frame-id]
  (let [di-creation-failed? (false? @(subscribe [::data-di/di-creation-success? frame-id]))]
    [loading-screen (cond-> {:show? di-creation-failed?
                             :message-state :error
                             :tip (subscribe [::i18n/translate :creation-error-tip])
                             :buttons [{:label (subscribe [::i18n/translate :ok])
                                        :on-click #(dispatch [::data-di/confirm-failed-di-creation frame-id])}]}
                      di-creation-failed?
                      (assoc :message (subscribe [::i18n/translate :di-creation-error])))]))

(reg-event-fx
 ::register-valid-search-track
 (fn [cofx [_ frame-id]]
   {::track/register
    {:id [:valid-search-track frame-id]
     :subscription [::validation/search-formdata-valid? frame-id]
     :event-fn (fn [valid?]
                 (when (not valid?)
                   [::traffic-lights-backend/update-traffic-lights frame-id nil nil]))}}))

(reg-event-fx
 ::dispose-valid-search-track
 (fn [cofx [_ frame-id]]
   {::track/dispose
    {:id [:valid-search-track frame-id]}}))

(def product-tour-impl
  {:component :search})

(reg-sub
 ::title-prefix
 (fn [db [_ frame-id vertical-count-number]]
   (let [vertical-label (i18n/translate db :search-label)]
     (str vertical-label " " vertical-count-number))))

(reg-event-fx
 ::no-event
 (fn [_ _]
   {}))

(def frame-header-impl
  {:frame-icon :search
   :frame-title-sub (fn [frame-id] (atom nil))
   :frame-title-prefix-sub (fn [frame-id vertical-count-number]
                             (subscribe [::title-prefix frame-id vertical-count-number]))
   :can-change-title? true
   :on-minimize-event (fn [frame-id])
   :on-maximize-event (fn [frame-id])
   :on-normalize-event (fn [frame-id])
   :on-close-fn (fn [_ done-fn]
                  (done-fn))})

(defn container [frame-id]
  (let [{:keys [is-minimized?]} @(fi/call-api :frame-sub frame-id)]
    (when-not is-minimized?
      [:<>
       [loading-states frame-id]
       [free-view frame-id]
       [frame-notifications frame-id]])))

(defn view [frame-id _]
  (r/create-class
   {:reagent-render      (fn [frame-id vertical-frame-props]
                           [container frame-id vertical-frame-props])
    :component-did-mount (fn []
                           (dispatch (fi/call-api :render-done-event-vec frame-id config/default-namespace))
                           (dispatch [::register-valid-search-track frame-id]))
    :component-will-unmount (fn []
                              (dispatch [::dispose-valid-search-track frame-id]))}))
