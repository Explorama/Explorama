(ns de.explorama.frontend.mosaic.render.tracks
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [taoensso.timbre :refer [debug]]))

(re-frame/reg-sub
 ::canvas
 (fn [db [_ path]]
   (get-in db path)))

(re-frame/reg-event-fx
 ::reg-track
 (fn [_ [_ path]]
   (debug "reg-track - " path)
   {:de.explorama.frontend.mosaic.tracks/register
    {:id [::canvas path]
     :subscription [::canvas path]
     :event-fn (fn [_]
                 [:de.explorama.frontend.mosaic.render.core/update path])}}))

(re-frame/reg-event-fx
 ::dispose-track
 (fn [_ [_ path]]
   (debug "dispose-track - " path)
   {:de.explorama.frontend.mosaic.tracks/dispose
    {:id [::canvas path]}}))

(re-frame/reg-event-fx
 ::reg-data-acs-track
 (fn [_ [_ path]]
   (debug "reg-data-acs-track - " path)
   {:de.explorama.frontend.mosaic.tracks/register
    {:id [::data-acs path]
     :subscription [:de.explorama.frontend.mosaic.data.di-acs/data-acs path]
     :event-fn (fn [_]
                 [:de.explorama.frontend.mosaic.render.core/update path])}}))

(re-frame/reg-event-fx
 ::dispose-data-acs-track
 (fn [_ [_ path]]
   (debug "dispose-data-acs-track - " path)
   {:de.explorama.frontend.mosaic.tracks/dispose
    {:id [::data-acs path]}}))

(re-frame/reg-sub
 ::theme
 (fn [db _]
   (fi/call-api :config-theme-db-get db)))

(re-frame/reg-event-fx
 ::reg-theme-track
 (fn [_ [_ path]]
   (debug "reg-theme-track - " path)
   {:de.explorama.frontend.mosaic.tracks/register
    {:id [::theme path]
     :subscription [::theme]
     :event-fn (fn [_]
                 [:de.explorama.frontend.mosaic.render.core/update path])}}))

(re-frame/reg-event-fx
 ::dispose-theme-track
 (fn [_ [_ path]]
   (debug "dispose-theme-track - " path)
   {:de.explorama.frontend.mosaic.tracks/dispose
    {:id [::theme path]}}))
