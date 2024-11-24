(ns de.explorama.frontend.woco.frame.interaction.connection
  (:require [clojure.set :refer [intersection]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :refer [reg-event-fx]]
            [taoensso.timbre :refer-macros [debug]]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.path :as path]))

(defn merge-options [source-options target-options]
  (let [target-option-map (into {} (map (fn [{t :type :as item}]
                                          [t item])
                                        target-options))
        source-option-map (into {} (map (fn [{t :type :as item}]
                                          [t item])
                                        source-options))
        set-types (intersection (set (keys target-option-map))
                                (set (keys source-option-map)))]
    (debug "merge-options" target-option-map source-option-map set-types)
    (reduce (fn [acc set-type]
              (let [target-option-map-element (get target-option-map set-type)
                    source-option-map-element (get source-option-map set-type)]
                (debug "merge-options acc" acc)
                (cond (and (:children target-option-map-element)
                           (:children source-option-map-element))
                      (conj acc (assoc target-option-map-element
                                       :children
                                       (vec (intersection (set (:children source-option-map-element))
                                                          (set (:children target-option-map-element))))))
                      (and target-option-map-element source-option-map-element)
                      (conj acc target-option-map-element)
                      :else acc)))
            []
            set-types)))


(defn create-connection-options [commit-move-data
                                 {source-type :type
                                  source-options :options
                                  :as source-desc}
                                 {target-type :type
                                  target-options :options
                                  :as target-desc}]
  (cond (= source-type :block)
        {}
        (= source-type :exclude)
        (assoc (update target-desc
                       :options
                       #(filterv (fn [{action-type :type}]
                                   (not ((set source-options) action-type)))
                                 %))
               :event [:de.explorama.frontend.woco.operations/action commit-move-data])
        (and source-options
             target-options)
        (assoc target-desc
               :options (merge-options source-options target-options)
               :event [:de.explorama.frontend.woco.operations/action commit-move-data])
        :else
        (assoc (or target-desc source-desc)
               :event [:de.explorama.frontend.woco.operations/action commit-move-data])))

#_{:type :options ; :connect | :options
   :frame-id "uuid"
   :event [:some-event]
   :options [{:label "Intersection by"
              :type :woco.operations/intersection
              :children [{:label "Identifier"
                          :params {:by "Identifier"}}]}]}
(reg-event-fx
 ::connection-negotiation-result
 (fn [{db :db} [_ drop-event
                {connection-negotiation-id :connection-negotiation-id
                 source-or-target :type
                 frame-id :frame-id}
                options]]
   (let [options (assoc options :frame-id frame-id)
         val (get-in db (path/connection-negotiation connection-negotiation-id))
         [source-desc target-desc] (cond (and (= :source source-or-target)
                                              (:target val))
                                         [options (:target val)]
                                         (and (= :target source-or-target)
                                              (:source val))
                                         [(:source val) options]
                                         :else [])
         clean-up (fn [db]
                    (path/dissoc-in db (path/connection-negotiation connection-negotiation-id)))]
     (cond (not (and source-desc target-desc))
           {:db (assoc-in db
                          (conj (path/connection-negotiation connection-negotiation-id) source-or-target)
                          (assoc options
                                 :frame-id
                                 frame-id))}
           (or (= (:type target-desc) :cancel)
               (= (:type source-desc) :cancel))
           {:db (clean-up db)}

           (and (= (:type target-desc) :connect)
                (get-in source-desc [:frame-id :drag-and-drop?]))
           {:dispatch [:de.explorama.frontend.woco.operations/action
                       :override
                       {:source-frame-id (:frame-id source-desc)
                        :target-frame-id (:frame-id target-desc)
                        :connection-negotiation-id connection-negotiation-id
                        :source-event (:event source-desc)
                        :target-event (:event target-desc)}
                       nil]
            :db (clean-up db)}

           (or (= (:type target-desc) :connect)
               (= (:type source-desc) :connect))
           {:dispatch [:de.explorama.frontend.woco.frame.api/connect
                       (:frame-id source-desc)
                       (:frame-id target-desc)]
            :db (clean-up db)}

           :else
           {:dispatch (fi/call-api
                       :context-menu-event-vec
                       (:drop-event drop-event)
                       (-> (update (create-connection-options drop-event source-desc target-desc)
                                   :options conj
                                   {:label (i18n/translate db :contextmenu-operations-move), :icon :move-window, :type :move})
                           (assoc :trigger-source {:type :connection-neg}))
                       {:source-frame-id (:frame-id source-desc)
                        :target-frame-id (:frame-id target-desc)
                        :connection-negotiation-id connection-negotiation-id
                        :source-event (:event source-desc)
                        :target-event (:event target-desc)})
            :db (clean-up db)}))))

(reg-event-fx
 ::connection-negotiation
 (fn [{db :db} [_ {:keys [from to drop-event commit-move-data]}]]
   (when (and (or (= evts/content-type (get-in db (path/frame-type from)))
                  (:drag-and-drop? from))
              (or (= evts/content-type (get-in db (path/frame-type to)))
                  (= evts/consumer-type (get-in db (path/frame-type to))))
              (get-in db (path/frame-data-consumer to)))
     (debug ::connection-negotiation from to drop-event commit-move-data)
     (let [connection-negotiation-id (str (random-uuid))]
       {:dispatch-n [[(if (:drag-and-drop? from)
                        (get-in from [:drag-infos :event])
                        (get-in db (path/frame-event from)))
                      evts/connection-negotiation {:type :source
                                                   :frame-id from
                                                   :connected-frame-id to
                                                   :coupled? (get-in db (path/frame-couple-with from))
                                                   :result [::connection-negotiation-result
                                                            {:commit-move-data commit-move-data
                                                             :drop-event drop-event}
                                                            {:commit-move-data commit-move-data
                                                             :frame-id from
                                                             :connection-negotiation-id connection-negotiation-id
                                                             :type :source}]
                                                   :connection-id connection-negotiation-id
                                                   :drag-infos (:drag-infos from)}]
                     [(get-in db (path/frame-event to)) evts/connection-negotiation {:type :target
                                                                                     :frame-id to
                                                                                     :connected-frame-id from
                                                                                     :coupled? (get-in db (path/frame-couple-with to))
                                                                                     :result [::connection-negotiation-result
                                                                                              {:commit-move-data commit-move-data
                                                                                               :drop-event drop-event}
                                                                                              {:frame-id to
                                                                                               :connection-negotiation-id connection-negotiation-id
                                                                                               :type :target}]
                                                                                     :connection-id connection-negotiation-id}]]}))))