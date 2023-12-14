(ns de.explorama.frontend.mosaic.views.frame-header
  (:require [clojure.string :as str]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.mosaic.path :as gp]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::request-pending?
 (fn [db [_ frame-id]]
   (get-in db (gp/data-request-pending frame-id) false)))

(re-frame/reg-sub
 ::data-instance
 (fn [db [_ frame-id]]
   (get-in db (gp/data-instance frame-id))))

(re-frame/reg-sub
 ::loading?
 (fn [[_ frame-id] _]
   [(re-frame/subscribe [::ddq/finished? frame-id])
    (re-frame/subscribe [::data-instance frame-id])
    (re-frame/subscribe [::request-pending? frame-id])])
 (fn [[can-close-frame? _ request-pending?] _]
   (or (not can-close-frame?)
       request-pending?)))

(defn loading-impl [frame-id]
  (re-frame/subscribe [::loading? frame-id]))

(defn simple-api-sub [sub-vec]
  (fn [frame-id]
    (re-frame/subscribe [sub-vec frame-id])))

(re-frame/reg-sub
 ::title
 (fn [db [_ frame-id]]
   (let [;relevant on copy group for setting the group-name as title
         custom-title (get-in db (gp/custom-title frame-id))
         top-level-element (get-in db (gp/container-path frame-id))
         years (get-in top-level-element [:frame :selected-years])
         countries (get-in top-level-element [:frame :selected-countries])
         datasources (get-in top-level-element [:frame :selected-datasources])]
     (cond-> ""
       custom-title (str " - " custom-title)
       (and (and datasources years countries)
            (not-empty datasources)
            (not-empty years)
            (not-empty countries))
       (str (str/join " " [" -" datasources years countries]))))))


(re-frame/reg-sub
 ::get-counts
 (fn [db [_ frame-id]]
   (let [{:keys [local-count global-count]} (get-in db (gp/top-level frame-id))]
     (when (or local-count global-count)
       {:global global-count :local local-count}))))

(re-frame/reg-sub
 ::title-prefix
 (fn [db [_ frame-id vertical-count-number]]
   (let [vertical-label (i18n/translate db :vertical-label-mosaic)
         {:keys [local-count global-count all-count]} (get-in db (gp/top-level frame-id))
         event-count
         (cond
           local-count local-count
           global-count global-count
           :else all-count)
         event-count-str (i18n/localized-number event-count)]
     (cond-> (str vertical-label " " vertical-count-number)
       (zero? event-count)
       (str " - 0 Events")
       (pos? event-count)
       (str " - " event-count-str " Events")))))

(re-frame/reg-event-fx
 ::no-event
 (fn [_ _]
   {}))

(def frame-header-impl
  {:frame-icon :mosaic2
   :frame-title-sub
   (simple-api-sub ::title)

   :frame-title-prefix-sub
   (fn [frame-id vertical-count-number]
     (re-frame/subscribe [::title-prefix frame-id vertical-count-number]))

   :on-minimize-event (fn [_frame-id]
                        [::no-event])
   :on-maximize-event (fn [frame-id]
                        [:de.explorama.frontend.mosaic.interaction.resize/trigger-resize frame-id])
   :on-normalize-event (fn [frame-id]
                         [:de.explorama.frontend.mosaic.interaction.resize/trigger-resize frame-id])
   :on-close-fn (fn [_ done-fn]
                  (done-fn))

   :can-change-title? true})

(re-frame/reg-sub
 ::ignored-events-count
 (fn [db [_ frame-id]]
   (get-in db (gp/scatter-plot-ignored-events (gp/top-level frame-id)))))

(re-frame/reg-sub
 ::true-sub?
 (fn [_ _]
   true))

