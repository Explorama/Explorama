(ns de.explorama.backend.expdb.persistence.common
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(defn- add-contexts [event contexts]
  (merge event
         (reduce (fn [acc [type name]]
                   (cond (vector? (get acc type))
                         (update acc type conj name)
                         (get acc type)
                         (assoc acc type [(get acc type) name])
                         :else
                         (assoc acc type name)))
                 {}
                 contexts)))

(defn- add-facts [event facts]
  (merge event
         (reduce (fn [acc [name _type value]]
                   (cond (vector? (get acc name))
                         (update acc name conj value)
                         (get acc name)
                         (assoc acc name [(get acc name) value])
                         :else
                         (assoc acc name value)))
                 {}
                 facts)))

(defn db-event->explorama [[global-id
                            [_
                             [[_ date]]
                             [ds]
                             facts
                             contexts
                             locations
                             notes :as event]]]
  (cond-> {"id" global-id
           "date" date
           "datasource" ds}
    (seq locations)
    (assoc "location" locations)
    (seq facts)
    (add-facts facts)
    (seq contexts)
    (add-contexts contexts)
    (seq notes)
    (assoc "notes" (str/join "\n" notes))))

(defn db->explorama [[dt-key {data :data}]]
  [dt-key
   (mapv db-event->explorama data)])

(defn merge-data-tiles-meta [old-data-tiles new-data-tiles new-datatiles-keys
                             merged-data-tiles-data]
  (map (fn [new-datatiles-key old-data-tile [_ merged-data-tiles-data]]
         (if-not old-data-tile
           [new-datatiles-key (assoc (select-keys (get new-data-tiles new-datatiles-key)
                                                  [:hash :key :acs :attributes :ranges])
                                     :count (count (:data merged-data-tiles-data)))]
           [new-datatiles-key (let [a old-data-tile
                                    b (get new-data-tiles new-datatiles-key)]
                                (assert (and (= (:key b) (:key a))
                                             (= (:hash b) (:hash a)))
                                        "Corrupted")
                                {:attributes (merge (:attributes a)
                                                    (:attributes b))
                                 :acs (merge-with set/union (:acs b) (:acs a))
                                 :count (count (:data merged-data-tiles-data))
                                 :ranges (merge-with (fn [old new]
                                                       (-> (update old 0 min (first new))
                                                           (update 1 max (second new))))
                                                     (:ranges a)
                                                     (:ranges b))
                                 :key (:key a)
                                 :hash (:hash a)})]))
       new-datatiles-keys
       old-data-tiles
       merged-data-tiles-data))

(defn merge-data-tiles-data [old-data-tiles new-data-tiles new-datatiles-keys]
  (map (fn [new-datatiles-key old-data-tile]
         (if-not old-data-tile
           [new-datatiles-key (select-keys (get new-data-tiles new-datatiles-key)
                                           [:data :key :hash])]
           [new-datatiles-key (let [a old-data-tile
                                    b (get new-data-tiles new-datatiles-key)]
                                (assert (and (= (:key b) (:key a))
                                             (= (:hash b) (:hash a)))
                                        "Corrupted")
                                {:data (merge (:data a)
                                              (:data b))
                                 :key (:key a)
                                 :hash (:hash a)})
            old-data-tile
            (get new-data-tiles new-datatiles-key)]))
       new-datatiles-keys
       old-data-tiles))