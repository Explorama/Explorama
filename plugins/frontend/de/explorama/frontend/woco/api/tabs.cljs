(ns de.explorama.frontend.woco.api.tabs
  "API used to reg-/de-register tabs"
  (:require [de.explorama.frontend.ui-base.utils.subs :refer [is-derefable?]]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.tabs :as tabs-impl]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.sidebar :as sidebar]
            [malli.core :as m]
            [malli.error :as me]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [error]]))


(def ^:private tab-schema
  [:map
   ;; id to identify tab specific content
   [:context-id [:or string? keyword? number? map?]]
   [:content-context keyword?]
   [:origin keyword?]
   [:label [:fn (fn [label]
                  (or (string? label)
                      (keyword? label)
                      (is-derefable? label)))]]
   [:export-fn {:optional true} fn?]
   [:on-before-show {:optional true} fn?]
   [:on-show {:optional true} fn?]
   [:on-render {:optional true} fn?]
   [:on-close {:optional true} fn?]
   [:active? {:optional true} boolean?]])

(def tab?
  (m/validator tab-schema))

(defn tab-content-size [db]
  (let [{width :inner-width height :inner-height} (get-in db path/scale-info)
        sidebar-width (sidebar/sidebar-width db)
        tab-header-height 60
        global-top (+ config/explorama-header-height tab-header-height)]
    {:width (- width sidebar-width)
     :height (- height global-top)
     :left 0
     ;top is just tab-header-heigt, because the tab containers are not absolute = app-header must not be respected
     :top tab-header-height}))

(re-frame/reg-sub
 ::tab-content-size
 tab-content-size)

(defn register [tab-desc]
  (if (tab? tab-desc)
    (tabs-impl/register tab-desc)
    (error "Failed to register tab - invalid desc"
           {:desc tab-desc
            :explain (-> tab-schema
                         (m/explain tab-desc)
                         (me/humanize))}))
  nil)

(re-frame/reg-event-fx
 ::register
 (fn [{db :db} [_ tab-desc]]
   (register tab-desc)))

(re-frame/reg-event-fx
 ::deregister
 (fn [{db :db} [_ context-id]]
   (tabs-impl/deregister context-id)))
