(ns de.explorama.frontend.map.event-logging
  (:require [ajax.core :as ajax]
            [cljs.reader :as edn]
            [clojure.string :as str]
            [de.explorama.frontend.common.event-logging.util :as log-util
             :refer [base-desc not-creating-and-target? access-attrs]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.map.config :as config]
            [de.explorama.frontend.map.event-replay :as e-replay]
            [de.explorama.frontend.map.paths :as geop]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug error]]))

(def event-version 1.0)

(re-frame/reg-event-fx
 ::event-log-success
 (fn [_ _]
   #_{:dispatch [:woco.log/info (str "Event-logging success" resp)]}
   {}))

(re-frame/reg-event-fx
 ::event-log-failure
 (fn [_ [_ resp]]
   {:dispatch (fi/call-api :error-event-vec (str "Event-logging failed" resp))}))

(re-frame/reg-event-fx
 ::log-event
 [(fi/ui-interceptor)]
 (fn [{db :db}
      [_ frame-id event-name description]]
   (when-let [log-fn (fi/call-api :service-target-db-get db :project-fns :event-log)]
     (log-fn db
             config/default-vertical-str
             frame-id
             event-name
             description
             event-version))))

(re-frame/reg-event-fx
 ::log-pseudo-init
 (fn [{db :db} _]
   (let [workspace-id (fi/call-api :workspace-id-db-get db)
         pseudo-frame-id {:workspace-id workspace-id
                          :vertical "map"}]
     {:dispatch [::log-event pseudo-frame-id "init-map" nil]})))

(re-frame/reg-event-fx
 ::ui-wrapper
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id event-name event-params]]
   (if-let [event-func (e-replay/event-func event-name)]
     (merge-with into
                 (event-func db frame-id nil event-params)
                 {:fx [[:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id event-name event-params]]]})
     (do
       (debug "no event-function found for " [event-name event-version])
       nil))))

(re-frame/reg-sub
 ::protocol-step-layer-name
 (fn [db [_ layer-ids]]
   (let [all-layers (get-in db geop/layers-path)
         layer-names (mapv
                      (fn [layer-id]
                        (or (->> all-layers
                                 (filter #(= layer-id (:id %)))
                                 first
                                 :name)
                            layer-id))
                      layer-ids)]
     (apply str (interpose ", " layer-names)))))

(defn action-desc [base-op action both _ only-new]
  (cond (= "operation" action)
        (cond (not-creating-and-target? :base-layer base-op both only-new)
              (base-desc :geomap-protocol-action-base-layer-change
                         (access-attrs both only-new [:payload :base-layer]))
              (not-creating-and-target? :overlayer base-op both only-new)
              (base-desc :geomap-protocol-action-overlayer-active
                         (str/join ", "
                                   (access-attrs both only-new [:payload :overlayers])))
              (not-creating-and-target? :feature-layer base-op both only-new)
              (base-desc :geomap-protocol-action-feature-layers-active
                         (str/join ", "
                                   (map (fn [[_ {name :name}]] name)
                                        (access-attrs both only-new [:payload :feature-layers]))))
              (not-creating-and-target? :marker base-op both only-new)
              (base-desc :geomap-protocol-action-marker-settings
                         (str/join ", "
                                   (map (fn [{name :name}] name)
                                        (access-attrs both only-new [:payload :marker-layouts]))))

              (or (= :init-di (:action only-new))
                  (= :init-di (:action both)))
              (base-desc :geomap-protocol-action-load-data)

              (= :copy-frame (:action only-new))
              (base-desc :geomap-protocol-action-copy-frame)

              :else nil)))

(def events->steps (partial log-util/events->steps config/default-vertical-str action-desc))