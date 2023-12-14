(ns de.explorama.frontend.common.event-logging.util
  (:require [cljs.reader :as edn]
            [clojure.data :as cd]))

(defn- step-fn [inj-origin action-desc-fn [origin frame-id action]
                old-desc new-desc db]
  (when (= inj-origin origin)
    (let [[only-old only-new both] (cd/diff old-desc new-desc)
          base-op (cond (nil? old-desc)
                        :create
                        (and (seq only-old)
                             (seq only-new))
                        :both
                        (seq only-old)
                        :deselect
                        (seq only-new)
                        :select
                        :else nil)]
      (if (nil? base-op)
        nil
        (if-let [desc (action-desc-fn base-op action both only-old only-new new-desc db)]
          (assoc desc
                 :window-id frame-id)
          nil)))))

(defn events->steps [inj-origin action-desc events _ db]
  (loop [events events
         i 1
         temp {}
         acc {}]
    (if (empty? events)
      acc
      (let [[head [origin frame-id action event-params]]
            (first events)
            parsed-event-params (edn/read-string event-params)
            acc (if-let [desc (step-fn inj-origin action-desc
                                       [origin frame-id action]
                                       (get temp [origin frame-id action])
                                       parsed-event-params db)]
                  (assoc acc i (assoc desc :head head))
                  acc)
            temp (assoc temp [origin frame-id action] parsed-event-params)]
        (recur (rest events)
               (inc i)
               temp
               acc)))))

(defn base-desc [action & [settings settings-sub]]
  (cond-> {:action action}
    settings
    (assoc :settings settings)
    settings-sub
    (assoc :settings-sub settings-sub)))

(defn not-creating-and-target? [target base-op both only-new]
  (and (not= base-op :create)
       (= target (or (:action only-new)
                     (:action both)))))

(defn access-attrs [both only-new path & [default-value]]
  (or (get-in only-new path)
      (get-in both path)
      default-value))

(defn- create-frame-event-synthetic [frame-id]
  ["woco" frame-id "create-frame" "" 1])

(defn pre-process-events
  ([valid-frame-origin-fn? exceptions-exist exceptions-close add-multiple-exceptions events]
   (let [frames (reduce (fn [acc [origin frame-id event-name _ _]] ;[vertical frame-id event-name event-params event-version]
                          (if (= "woco" origin)
                            (cond (and (or (and (= "frame" event-name)
                                                (valid-frame-origin-fn? frame-id))
                                           (exceptions-exist origin frame-id event-name))
                                       (not ((:ignored-frames acc) frame-id)))
                                  (update acc :exist conj frame-id)

                                  (or (and (= "close-frame" event-name)
                                           (valid-frame-origin-fn? frame-id))
                                      (exceptions-close origin frame-id event-name))
                                  (-> (update acc :exist disj frame-id)
                                      (update :ignored-frames conj frame-id))
                                  :else
                                  acc)
                            (update acc :ignored-frames conj frame-id)))
                        {:exist #{}
                         :ignored-frames #{}}
                        events)
         frame-exists? (:exist frames)
         result (reduce (fn [{:keys [added? result] :as acc} [vertical frame-id event-name :as event]]
                          (let [id [vertical frame-id event-name]]
                            (if (and (frame-exists? frame-id)
                                     (or
                                      (and (set? add-multiple-exceptions)
                                           (add-multiple-exceptions event-name))
                                      (not (added? id))))
                              {:result (conj result event)
                               :added? (conj added? id)}
                              acc)))
                        {:result []
                         :added? #{}}
                        (reverse events))
         result (update result :result (fn [events]
                                         (:events
                                          (reduce (fn [{:keys [events frames]} [_ frame-id :as event]]
                                                    (if (frames frame-id)
                                                      {:events (conj events event)
                                                       :frames frames}
                                                      {:events (conj events
                                                                     (create-frame-event-synthetic frame-id)
                                                                     event)
                                                       :frames (conj frames frame-id)}))
                                                  {:events []
                                                   :frames #{}}
                                                  (reverse events)))))]
     (assoc result :frames frames)))
  ([valid-frame-origin-fn? exceptions-exist exceptions-close events]
   (pre-process-events valid-frame-origin-fn? exceptions-exist exceptions-close nil events)))