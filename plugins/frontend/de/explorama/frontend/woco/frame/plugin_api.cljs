(ns de.explorama.frontend.woco.frame.plugin-api
  "Plugin API
   
   A Vertical API provides a map with all the required api endpoints consisting of
   keywords and functions.

   The functions has to return a subscription vector pointing to the right subscription
   vector based on the provided frame-id."
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.path :as path]))

(defn derefable?
  [x] (implements? IDeref x))

(def ^:private apis [::loading?
                     ::toolbar
                     ::settings
                     ::burger
                     ::custom-items
                     ::frame-header
                     ::loading-screen
                     ::warn-screen
                     ::stop-screen
                     ::product-tour
                     ::frame
                     ::filter
                     ::legend
                     ::notifications
                     ::parent-container-id
                     ::component-did-mount
                     ::optimize-frame-performance
                     ::drop-area-extra?
                     ::ignore-child-events?])

;TODO r1/malli use the schema or remove it
#_(def ^:private schemas
    "Malli schema definitions for every api part
   wrapped in one map"
    {::api
     [:map
      (mapv #(vector % fn?) apis)]

     ::filter-status
     [:map
      [:show-button? derefable?]
      [:disabled? derefable?]
      [:invalid? derefable?]
      [:filter-active? derefable?]
      [:tooltip-fn derefable?]] ; [frame-id loading?] -> str

     ::loading?
     [:and boolean?]

     ::settings
     [:map
      [:show-button? derefable?]
      [:ignored-events-count int?]
      [:toggle-fn fn?]]; [frame-id event] -> nil

     ::burger
     [:map
      [:show-button? derefable?]
      [:ignored-events-count int?]
      [:toggle-fn fn?]]; [frame-id event] -> nil

     ::frame-header
     [:map
      [:frame-title-sub derefable?]
      [:frame-title-prefix-sub derefable?] ; [frame-id vertical-count-number]
      [:duplicate-fn fn?] ; [frame-id]
      [:minimize-event fn?]
      [:maximize-event fn?]
      [:normalize-event fn?]
      [:close-fn fn?]] ; [frame-id done-fn]

     ::frame-header-context-menu
     :TODO

     ::loading-screen
     [:map
      [:show? derefable?]
      [:cancellable? derefable?]
      [:cancel-fn fn?]
      [:loading-screen-message-sub derefable?]
      [:loading-screen-tip-sub derefable?]
      [:loading-screen-tip-titel-sub derefable?]]

     ::warn-screen
     [:map
      [:show? derefable?]
      [:title-sub fn?]
      [:message-1-sub fn?]
      [:message-2-sub fn?]
      [:recommendation-sub fn?]
      [:stop-sub fn?]
      [:proceed-sub fn?]
      [:stop-fn fn?] ; [frame-id event]
      [:proceed-fn fn?]]  ; [frame-id event]

     ::stop-screen
     [:map
      [:show? derefable?]
      [:title-sub fn?]
      [:message-1-sub fn?]
      [:message-2-sub fn?]
      [:stop-sub fn?]
      [:ok-fn fn?]] ; [frame-id event]

     ::product-tour
     [:map
      [:params [:map]]]

     ::parent-container-id
     [:and derefable?]

     ::filter
     [:map
      [:data-ac-path fn?] ;[frame-id] -> [:path :to :data-acs]
      [:submit-event fn?]] ;[frame-id] -> [event-vector]



     ::notifications
     :TODO})

(re-frame/reg-event-db
 ::register
 (fn [db [_ vertical desc]]
   (assoc-in db
             (path/vertical-plugin-api {:vertical vertical})
             desc)))

(defn api [db vertical access-key]
  (let [access-path (cond-> (path/vertical-plugin-api {:vertical vertical})
                      access-key (conj access-key))]

    (get-in db access-path)))

(re-frame/reg-sub
 ::api
 (fn [db [_ frame-id path]]
   (cond-> (get-in db (path/vertical-plugin-api frame-id))
     (vector? path)
     (get-in path)
     (keyword? path)
     (get path))))

(doseq [api apis
        :let [access-key (-> api name keyword)]]
  (re-frame/reg-sub
   api
   (fn [[_ frame-id]]
     [(re-frame/subscribe [::api frame-id])])
   (fn [[vertical-api] _]
     (get vertical-api access-key))))
