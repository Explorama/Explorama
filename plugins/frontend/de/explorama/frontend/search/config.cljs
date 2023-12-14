(ns de.explorama.frontend.search.config
  (:require [re-frame.core :as re-frame]
            [clojure.string :as st]
            [de.explorama.frontend.ui-base.utils.specification :as spec]
            [taoensso.timbre :refer-macros [debug]]))

(def debug?
  ^boolean goog.DEBUG)

(def search-pre-path [:search :search-form])

(def filter-pre-path [:search :filter-form])

(def timelog-day-calc? false)

(def data-instance-pre-path [:search :data-instance])
(def default-namespace :search)
(def default-vertical-str (name default-namespace))
(def tool-name "tool-search")

(def header-height 32)   ;TODO r1/woco dynamic from somewhere 
(def search-bar-height 50)

(def search-bar-request-delay 500) ;ms

(def search-change-request-delay 3000) ;ms

(def direct-search-request-delay (aget js/window "EXPLORAMA_SUCHE_DIRECT_REQUEST_DELAY")) ;ms

(def max-show-all (aget js/window "EXPLORAMA_MAX_SHOW_ALL"))

(def max-related-options (aget js/window "EXPLORAMA_RELATED_MAX_OPTIONS"))

(def geo-config (js->clj (aget js/window "EXPLORAMA_SUCHE_GEO_CONFIG") :keywordize-keys true))

(def default-direct-vis-options "mosaic")

(def search-parameter-config (atom {}))

(defn normalize-attr [attr]
  (if (keyword? attr)
    (st/lower-case (name attr))
    (st/lower-case attr)))

(defn conf-for-attr [attr]
  (when attr
    (let [attr (normalize-attr attr)
          default-def (get @search-parameter-config "default")]
      (get @search-parameter-config attr
           default-def))))

(re-frame/reg-event-db
 ::set-search-parameter-config
 (fn [db [_ conf]]
   (debug "Search-parameter-config arrived" conf)
   (reset! search-parameter-config (if (map? conf)
                                     (reduce (fn [res [attr c]]
                                               (assoc res (normalize-attr attr)
                                                      c))
                                             {}
                                             conf)
                                     {}))
   db))
