(ns de.explorama.frontend.map.components.frame-notifications
  (:require [clojure.string :as clj-str]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.map.paths :as path]
            [re-frame.core :as re-frame]))


(defn- add-overlay-names [db not-supported-ops]
  (let [layers (get-in db path/layers-path)]
    (into #{} (mapv (fn [{:keys [layer-id] :as entry}]
                      (if-not layer-id
                        entry
                        (if-let [layer-name (some (fn [{:keys [id name]}]
                                                    (when (and (= layer-id id) name)
                                                      name))
                                                  layers)]
                          (assoc entry :notification-text (str "Overlay " layer-name))
                          entry)))
                    not-supported-ops))))

(defn not-supported-redo-ops-event [db frame-id invalid-operations]
  (fi/call-api :frame-notifications-not-supported-redo-ops-event-vec
               frame-id
               (add-overlay-names db invalid-operations)))

(defn- build-message [frame-id {:keys [show-undo? not-supported-redo-ops clear-notification-fn undo-fn]}]
  (when (coll? not-supported-redo-ops)
    (let [load-warning-screen-not-follow-recommendation @(re-frame/subscribe [::i18n/translate :load-warning-screen-not-follow-recommendation])
          redo-not-possible-single @(re-frame/subscribe [::i18n/translate :redo-not-possible-single])
          redo-not-possible-multi @(re-frame/subscribe [::i18n/translate :redo-not-possible-multi])
          undo-button-label @(re-frame/subscribe [::i18n/translate :undo-button-label])]

      (cond-> {:message (str (if (= 1 (count not-supported-redo-ops))
                               redo-not-possible-single
                               redo-not-possible-multi)
                             (->> (map (fn [{:keys [notification-text op]}]
                                         (cond (and (= op :active-layer-attributes)
                                                    notification-text)
                                               notification-text))
                                       not-supported-redo-ops)
                                  (clj-str/join ", ")))}
        show-undo?
        (assoc :actions [{:label load-warning-screen-not-follow-recommendation
                          :on-click clear-notification-fn}
                         {:label undo-button-label
                          :start-icon :back
                          :variant :secondary
                          :on-click undo-fn}])))))

(def frame-notifications-impl
  {:show? true
   :build-message build-message
   :undo-path-fn path/undo-connection-update-event})