(ns de.explorama.frontend.configuration.configs.config-types.woco
  (:require [de.explorama.frontend.configuration.path :as path]
            [de.explorama.shared.common.configs.woco :as config-woco]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]))

(defn changes? [db]
  (boolean (get-in db path/woco-temporary)))

(re-frame/reg-sub
 ::changes?
 (fn [db]
   (changes? db)))

(re-frame/reg-sub
 ::temporary-value-internal
 (fn [db [_ config]]
   (get-in db (conj path/woco-temporary config))))

(re-frame/reg-sub
 ::last-applied-value-internal
 (fn [db [_ config]]
   (get-in db (conj path/woco-last-applied config))))

(re-frame/reg-sub
 ::temporary-value
 (fn [[_ config]]
   [(re-frame/subscribe [::temporary-value-internal config])
    (re-frame/subscribe [::last-applied-value-internal config])
    (condp = config
      config-woco/new-window-pref-key
      (fi/call-api [:user-preferences :preference-sub]
                   config-woco/new-window-pref-key
                   config-woco/new-window-default)
      config-woco/published-window-pref-key
      (fi/call-api [:user-preferences :preference-sub]
                   config-woco/published-window-pref-key
                   config-woco/published-window-default)
      config-woco/published-windows-pref-key
      (fi/call-api [:user-preferences :preference-sub]
                   config-woco/published-windows-pref-key
                   config-woco/published-windows-default))])
 (fn [[temp-value last-values current-values] _]
   (or temp-value
       last-values
       current-values)))

(re-frame/reg-event-db
 ::temporary-value
 (fn [db [_ config new-value]]
   (assoc-in db (conj path/woco-temporary config) new-value)))

(re-frame/reg-event-fx
 ::submit
 (fn [{db :db}]
   (let [current-values (get-in db path/woco-temporary)
         dispatch (mapv (fn [[pref-key new-value]]
                          [:dispatch (fi/call-api [:user-preferences :save-event-vec]
                                                  pref-key new-value)])
                        current-values)]
     {:db (-> (path/dissoc-in db path/woco-temporary)
              (assoc-in path/woco-last-applied current-values))
      :fx dispatch})))
