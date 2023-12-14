(ns de.explorama.frontend.configuration.configs.config-types.mouse
  (:require [de.explorama.frontend.configuration.path :as path]
            [de.explorama.shared.common.configs.mouse :as shortcuts-mouse]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]))

(def ^:private options {:select {:label :mouse-select
                                 :value :select}
                        :panning {:label :mouse-pan
                                  :value :panning}})

(defn translate-options [translations]
  (mapv (fn [v]
          (update v :label translations))
        (vals options)))

(defn changes? [db]
  (boolean (get-in db path/mouse-layout-temporary)))

(re-frame/reg-sub
 ::changes?
 (fn [db]
   (changes? db)))

(defn- current-value-mouse [translations button-num current-values]
  (-> (get options
           (some (fn [{button :button action :action}]
                   (when (= button-num button)
                     action))
                 current-values))
      (update :label translations)))

(re-frame/reg-sub
 ::temporary-value-internal
 (fn [db [_ mouse-button]]
   (get-in db (conj path/mouse-layout-temporary mouse-button))))

(re-frame/reg-sub
 ::last-applied-value-internal
 (fn [db [_ mouse-button]]
   (get-in db (conj path/mouse-layout-last-applied mouse-button))))

(re-frame/reg-sub
 ::temporary-value
 (fn [[_ mouse-button]]
   [(re-frame/subscribe [::temporary-value-internal mouse-button])
    (re-frame/subscribe [::last-applied-value-internal mouse-button])
    (fi/call-api [:user-preferences :preference-sub] shortcuts-mouse/pref-key shortcuts-mouse/mouse-default)
    (re-frame/subscribe [::i18n/translate-multi :mouse-pan :mouse-select])])
 (fn [[temp-value last-values current-values translations] [_ mouse-button]]
   (or temp-value
       last-values
       (current-value-mouse translations mouse-button current-values))))

(re-frame/reg-event-db
 ::temporary-value
 (fn [db [_ mouse-button new-value]]
   (assoc-in db (conj path/mouse-layout-temporary mouse-button) new-value)))

(defn- update-current-value-event-vec [current-values temp-values]
  (->> (mapv (fn [{b :button :as desc}]
               (if-let [temp-val (get temp-values b)]
                 (assoc desc :action (:value temp-val))
                 desc))
             current-values)
       (fi/call-api [:user-preferences :save-event-vec] shortcuts-mouse/pref-key)))

(re-frame/reg-event-fx
 ::submit
 (fn [{db :db}]
   (let [current-values (fi/call-api [:user-preferences :preference-db-get] db shortcuts-mouse/pref-key shortcuts-mouse/mouse-default)
         dispatch (update-current-value-event-vec
                   (if (empty? current-values)
                     shortcuts-mouse/mouse-default
                     current-values)
                   (get-in db path/mouse-layout-temporary))]
     {:db (-> (path/dissoc-in db path/mouse-layout-temporary)
              (assoc-in path/mouse-layout-last-applied
                        (get-in db path/mouse-layout-temporary)))
      :dispatch dispatch})))
