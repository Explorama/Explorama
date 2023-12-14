(ns de.explorama.frontend.woco.api.link-info
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.path :as path]))

(re-frame/reg-event-db
 ::read-location
 (fn [db _]
   (let [query-string (aget js/window "location" "search")
         params (js/URLSearchParams. query-string)
         param-iter (.entries params)
         url-infos (loop [acc {}]
                     (let [[elem-key elem-value]
                           (-> (.next param-iter)
                               (aget "value")
                               js->clj)]
                       (if-not elem-key
                         acc
                         (recur (assoc acc
                                       (keyword elem-key)
                                       elem-value)))))]

     (assoc-in db path/url-info url-infos))))

(defn url-info [db url-key]
  (get-in db (conj path/url-info url-key)))

(re-frame/reg-sub
 ::url-info
 (fn [db [_ url-key]]
   (url-info db url-key)))

(defn url-infos [db keys]
  (select-keys (get-in db path/url-info)
               keys))

(re-frame/reg-sub
 ::url-infos
 (fn [db [_ keys]]
   (url-infos db keys)))

(defn remove-url-info [db url-key]
  (update-in db path/url-info dissoc url-key))

(re-frame/reg-event-db
 ::remove-url-info
 (fn [db [_ url-key]]
   (remove-url-info db url-key)))

(re-frame/reg-event-db
 ::clear-url-info
 (fn [db _]
   (update db :woco dissoc :url-info)))