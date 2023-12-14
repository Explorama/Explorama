(ns de.explorama.frontend.search.views.components.traffic-light
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.misc.core :as misc-core]
            [de.explorama.frontend.search.path :as path]
            [clojure.string :as string]
            [de.explorama.shared.common.data.attributes :as attrs]))

(def light-config {:red {:color :red
                         :message (re-frame/subscribe [::i18n/translate :traffic-light-red-message])
                         :tooltip (re-frame/subscribe [::i18n/translate :traffic-light-red-tooltip])}
                   :yellow {:color :yellow
                            :message (re-frame/subscribe [::i18n/translate :traffic-light-yellow-message])
                            :tooltip (re-frame/subscribe [::i18n/translate :traffic-light-yellow-tooltip])}
                   :green {:color :green
                           :message (re-frame/subscribe [::i18n/translate :traffic-light-green-message])
                           :tooltip (re-frame/subscribe [::i18n/translate :traffic-light-green-tooltip])}
                   :pending {:color :green
                             :message (re-frame/subscribe [::i18n/translate :traffic-light-pending-message])}
                   :empty   {:color :green
                             :message (re-frame/subscribe [::i18n/translate :traffic-light-empty-message])}
                   nil {:message (re-frame/subscribe [::i18n/translate :traffic-light-none-message])}})

(defn message-color [frame-id
                     {:keys [message color tooltip]}
                     {:keys [success status size reason]}]
  (let [size (i18n/localized-number size)]
    (cond (or (nil? success)
              (= :pending status))
          {:message @message
           :color color
           :tooltip tooltip}
          (and success
               (= size "0"))
          (get light-config :empty)
          success
          {:message (cond-> @message
                      size
                      (str " (" size " Events)"))
           :color color
           :tooltip tooltip}
          (and (not success)
               (= reason :result-limit))
          {:message (cond-> @(re-frame/subscribe [::i18n/translate :traffic-light-result-too-large])
                      size
                      (str  " ( > " size " Events)"))
           :color color
           :tooltip (re-frame/subscribe [::i18n/translate :traffic-light-result-too-large-tooltip])}
          (and (not success)
               (= reason :data-tile-limit))
          {:message (re-frame/subscribe [::not-selected frame-id])
           :color :grey
           :tooltip (re-frame/subscribe [::i18n/translate :traffic-light-data-tile-too-large-tooltip])}
          (and (not success)
               (= reason :unknown))
          {:message @(re-frame/subscribe [::i18n/translate :unknown-error])})))

(re-frame/reg-sub
 ::not-selected
 (fn [db [_ frame-id]]
   (let [attributes (get-in db (path/frame-attributes frame-id))
         current-selection (set (keys (get-in db (path/frame-search-rows frame-id))))
         values
         (->> (conj (filter (fn [[_ label]]
                              (= label attrs/datasource-node))
                            attributes)
                    [attrs/year-attr attrs/date-node]
                    [attrs/country-attr attrs/context-node])
              (remove current-selection)
              (map first)
              sort)]
     (string/replace
      (i18n/translate db :required-attributes-infotext-one-add)
      "<p>"
      (str (string/join ", " (butlast values))
           " "
           (or (i18n/translate db :traffic-light-criteria-or false)
               "or")
           " "
           (last values))))))

(defn traffic-light
  ([frame-id current-light] (traffic-light frame-id current-light {}))
  ([frame-id
    {status :status :as current-light}
    base-params]
   (let [active? (not (or (nil? status)
                          (= :pending status)))
         {:keys [message color tooltip]}
         (message-color frame-id
                        (get light-config status)
                        current-light)]
     [misc-core/traffic-light (cond-> base-params
                                (and active? color)
                                (assoc :color color)
                                (and tooltip active?)
                                (assoc :hint-text tooltip)
                                message
                                (assoc :label message))])))
