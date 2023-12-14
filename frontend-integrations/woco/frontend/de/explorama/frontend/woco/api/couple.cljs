(ns de.explorama.frontend.woco.api.couple
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [debug]]
            [de.explorama.frontend.woco.frame.size-position :refer [resize-info set-frame-position]]
            [de.explorama.frontend.woco.path :as path]))

 ;number of frames, which can be coupled
(def couple-with-limit 2)

(defn gen-with [old-with source target]
  ;Removes last, if limit is reached
  (let [with (or old-with [])
        with-size (count with)
        removed (when (= couple-with-limit with-size)
                  (peek (vec (remove #{source target} with))))
        with (filterv #(and (not= % source)
                            (not= % target))
                      (if (= couple-with-limit with-size)
                        (vec (pop with))
                        with))]
    {:with (if (empty? with)
             [target source]
             (-> (vec (cons target with)) ;target at first
                 (conj source))) ; source at last
     :removed removed}))

(defn- on-couple-vec [db source-frame-id target-frame-id couple-infos]
  (when-let [on-couple-event (get-in (fi/call-api :frame-instance-api-db-get db target-frame-id)
                                     [:couple-infos
                                      :on-couple])]
    [on-couple-event source-frame-id target-frame-id couple-infos]))

(defn- on-decouple-vec [db source-frame-id target-frame-id couple-infos]
  (when-let [on-decouple-event (get-in (fi/call-api :frame-instance-api-db-get db target-frame-id)
                                       [:couple-infos
                                        :on-decouple])]
    [on-decouple-event source-frame-id target-frame-id couple-infos]))


(defn- on-action-vec [db source-frame-id target-frame-id action-desc]
  (when-let [on-action-event (get-in (fi/call-api :frame-instance-api-db-get db target-frame-id)
                                     [:couple-infos
                                      :on-action])]
    [on-action-event source-frame-id target-frame-id action-desc]))

(defn calc-coupling [db target-frame-id with couple-infos ignore-on-couple? no-event-logging?]
  (let [[width height] (get-in db (path/frame-size target-frame-id))
        [left top] (get-in db (path/frame-coords target-frame-id))
        target-idx (.indexOf (or with [])
                             target-frame-id)
        top (if (> target-idx 0)
              (- top
                 (* height target-idx))
              top)]
    (reduce (fn [{:keys [idx db disp]} pair-frame-id]
              (let [top (+ top (* idx height))
                    [pwidth pheight] (get-in db (path/frame-size pair-frame-id))
                    [pfwidth pfheight] (get-in db (path/frame-full-size pair-frame-id))
                    frame-no-event-logging? (get-in db (path/frame-no-event-logging pair-frame-id))
                    resize? (and
                             (not= target-frame-id pair-frame-id)
                             (or (not= width pwidth)
                                 (not= height pheight)))
                    delta-w (- width pwidth)
                    delta-h (- height pheight)
                    resize-infos (when resize?
                                   (resize-info delta-w delta-h width height (+ pfwidth delta-w) (+ pfheight delta-h)))
                    new-position [left top]]
                (set-frame-position pair-frame-id new-position)
                {:idx (inc idx)
                 :disp (cond-> disp
                         resize? (conj [:de.explorama.frontend.woco.frame.view.core/resize-stop resize-infos])
                         (not ignore-on-couple?) (conj (on-couple-vec db target-frame-id pair-frame-id couple-infos)))
                 :db (-> db
                         (update-in (path/frame-couple-infos pair-frame-id) merge couple-infos)
                         (assoc-in (path/frame-size pair-frame-id)
                                   [width height])
                         (assoc-in (path/frame-full-size pair-frame-id)
                                   [(+ pfwidth delta-w)  (+ pfheight delta-h)])
                         (assoc-in (path/frame-coords pair-frame-id)
                                   new-position))}))
            {:idx 0
             :disp []
             :db db}
            with)))

(defn decouple [db removed-frame-id]
  (let [{:keys [with]} (get-in db (path/frame-couple-infos removed-frame-id))
        dissoc-all? (= 2 (count with))
        with (vec (remove #{removed-frame-id} with))
        db (path/dissoc-in db (path/frame-couple-infos removed-frame-id))]
    (reduce (fn [ndb pair-frame-id]
              (if dissoc-all?
                (path/dissoc-in ndb (path/frame-couple-infos pair-frame-id))
                (let [old-with (get-in ndb (path/frame-couple-with pair-frame-id))
                      new-with (vec (remove #{removed-frame-id} old-with))]
                  (assoc-in
                   ndb
                   (path/frame-couple-with pair-frame-id)
                   new-with))))
            db
            (or with []))))

(defn switch-positions [db source target]
  (let [s-coords (get-in db (path/frame-coords source))
        t-coords (get-in db (path/frame-coords target))]
    (set-frame-position target s-coords)
    (set-frame-position source t-coords)
    (-> db
        (assoc-in (path/frame-coords target)
                  s-coords)
        (assoc-in (path/frame-coords source)
                  t-coords))))

(re-frame/reg-event-fx
 ::recalc-positions
 (fn [{db :db} [_ target-frame-id no-event-logging?]]
   (let [{:keys [with] :as couple-infos} (get-in db (path/frame-couple-infos target-frame-id))
         {db :db disp :disp}
         (calc-coupling db
                        target-frame-id
                        with
                        couple-infos
                        true
                        no-event-logging?)]
     {:db db
      :dispatch-n (or disp [])})))

(re-frame/reg-event-fx
 ::submit-couple-action
 (fn [{db :db} [_ source-frame-id action-desc]]
   (let [with (get-in db (path/frame-couple-with source-frame-id))]
     {:dispatch-n (mapv (fn [pair-frame-id]
                          (on-action-vec db source-frame-id pair-frame-id action-desc))
                        (remove #{source-frame-id} with))})))

(re-frame/reg-sub
 ::couple-infos
 (fn [db [_ frame-id]]
   (get-in db (path/frame-couple-infos frame-id))))

(defn couple-with [db frame-id]
  (get-in db (path/frame-couple-with frame-id)))

(re-frame/reg-sub
 ::couple-with
 (fn [db [_ frame-id]]
   (couple-with db frame-id)))

(re-frame/reg-event-fx
 ::couple
 (fn [{db :db} [_ source-frame-id target-frame-id couple-infos no-event-logging?]]
   (let [{target-with :with} (get-in db (path/frame-couple-infos target-frame-id))
         {:keys [removed with]} (gen-with target-with source-frame-id target-frame-id)
         with (if removed
                (vec (remove #{removed} with))
                with)
         db (cond-> db
              removed (-> (decouple removed)
                          (switch-positions source-frame-id removed)))
         couple-infos (-> (or couple-infos {})
                          (assoc :with with))
         {db :db disp :disp}
         (calc-coupling db
                        target-frame-id
                        with
                        couple-infos
                        no-event-logging?
                        no-event-logging?)]
     {:db db
      :dispatch-n (cond-> (or disp [])
                    removed (conj
                             (on-decouple-vec db
                                              source-frame-id
                                              removed
                                              couple-infos))
                    (not no-event-logging?)
                    (conj [:de.explorama.frontend.woco.event-logging/log-frame-event target-frame-id ::couple])
                    (not no-event-logging?)
                    (conj [:de.explorama.frontend.woco.event-logging/log-frame-event source-frame-id ::couple]))})))

(re-frame/reg-event-fx
 ::decouple
 (fn [{db :db} [_ frame-id no-event-logging?]]
   (debug "decouple" frame-id)
   (let [{:keys [with] :as couple-infos} (get-in db (path/frame-couple-infos frame-id))
         db (cond-> db
              with (decouple frame-id))]
     {:db db
      :dispatch-n (cond-> (mapv (fn [pair-frame-id]
                                  (on-decouple-vec db
                                                   frame-id
                                                   pair-frame-id
                                                   couple-infos))
                                (if no-event-logging?
                                  []
                                  with))
                    (not no-event-logging?)
                    (conj [:de.explorama.frontend.woco.event-logging/log-frame-event frame-id]))})))