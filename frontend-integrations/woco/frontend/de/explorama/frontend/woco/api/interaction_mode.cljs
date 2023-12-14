(ns de.explorama.frontend.woco.api.interaction-mode
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.woco.api.product-tour :as product-tour]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.path :as wp]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [debug]]
            [vimsical.re-frame.cofx.inject :as inject]))

(defn force-read-only? [db frame-id]
  (fi/call-api :flags-db-get db frame-id :force-read-only?))

(defn pending-state
  "Primary to detect that an project with read-only is loading, because normal interaction mode is set after project is loading"
  ([db]
   (get-in db wp/pending-interaction-mode :normal))
  ([db value]
   (assoc-in db wp/pending-interaction-mode value)))

(defn interaction-mode
  "Given one arg, return the interaction mode.  Given two args, assoc a new
  interaction mode and return the new db."
  ([db]
   (get-in db wp/interaction-mode :normal))
  ([db value]
   (assoc-in db wp/interaction-mode value)))

(defn product-tour-read-only? [db {:keys [component additional-info]}]
  (let [current-step (product-tour/current-step db)]
    (and (not (:ignore-action? current-step))
         (not (product-tour/component-active? current-step
                                              component
                                              additional-info)))))

(defn read-only? [db additional-infos]
  (or
   (= :read-only
      (interaction-mode db))
   (force-read-only? db (:frame-id additional-infos))
   (product-tour-read-only? db additional-infos)))

(defn normal? [db & {:keys [frame-id]}]
  (and
   (= :normal (interaction-mode db))
   (not (force-read-only? db frame-id))))

(re-frame/reg-event-db
 ::set-normal
 (fn [db _]
   (interaction-mode db :normal)))

(re-frame/reg-event-db
 ::set-read-only
 (fn [db _]
   (interaction-mode db :read-only)))

(re-frame/reg-event-db
 ::set-no-render
 (fn [db _]
   (interaction-mode db :no-render)))

(re-frame/reg-event-fx
 ::set-render
 (fn [{db :db} [_ render-done-event origin]]
   (debug "set-render from origin" origin)
   {:db
    (-> db
        (interaction-mode :normal)
        (assoc-in [:woco :render-frames-callback] render-done-event)
        (assoc-in [:woco :frames-to-render] (->> (filterv (fn [[_ {frame-type :type}]]
                                                            (= frame-type evts/content-type))
                                                          (get-in db wp/frames))
                                                 (mapv first))))
    :dispatch [:de.explorama.frontend.woco.frame.api/render-done nil config/default-namespace]}))

(defn render? [db]
  (not (= :no-render
          (interaction-mode db))))

(re-frame/reg-sub
 ::render?
 (fn [db _]
   (render? db)))

(re-frame/reg-sub
 ::normal?
 (fn [db [_ {:keys [component additional-info] :as additional-infos}]]
   (let [current-step (product-tour/current-step db)]
     (and (normal? db additional-infos)
          (product-tour/component-active? current-step
                                          component
                                          additional-info)))))

(re-frame/reg-sub
 ::read-only?
 (fn [db [_ additional-infos]]
   (read-only? db additional-infos)))

(re-frame/reg-sub
 ::current
 (fn [db [_ {:keys [frame-id]}]]
   (if (force-read-only? db frame-id)
     :read-only
     (interaction-mode db))))

(defn check-inter-mode [db frame-type additional-infos okay-fn & [force?]]
  (when (or (not (and (read-only? db additional-infos)
                      (#{:frame/content-type :frame/custom-type} frame-type)))
            force?)
    (okay-fn)))

;for project loading
(re-frame/reg-event-db
 ::set-pending-normal
 (fn [db _]
   (pending-state db :normal)))

;for project loading
(re-frame/reg-event-db
 ::set-pending-read-only
 (fn [db _]
   (pending-state db :read-only)))

(defn pending-read-only? [db]
  (= :read-only (pending-state db)))

(re-frame/reg-sub
 ::pending-read-only?
 (fn [db _]
   (pending-read-only? db)))

(def ro-interceptor
  "An interceptor that inhibits the execution of any further interceptors/handlers
  in the chain if read-only is enabled.  Any interceptors that come before this
  one in the chain will execute both their :before and :after functions, so that
  any setup/teardown mechanism still works.

  If you imagine the interceptor chain like this:
    +---------------+       +---------------+      +---------------+
    | interceptor-0 |       | interceptor-1 |      | handler       |
    |               |       |               |      |               |
  --->  :before   ----------->  :before  ----------->  :before   -----+
    |               |       |               |      |               |  |
    |               |       |               |      |               |  |
  <---  :after    <-----------  :after   <-----------  (:after)  <----+
    |               |       |               |      |               |
    +---------------+       +---------------+      +---------------+

  then if interceptor-1 is the ro-interceptor and read-only is enabled, it
  short-circuits:
    +---------------+       +---------------+      +---------------+
    | interceptor-0 |       | ro-interceptor|      | handler       |
    |               |       |               |      |               |
  --->  :before   ----------->  :before  ---- X X - >  :before   - - -+
    |               |       |      |        |  X   |               |
    |               |       |      V        |  X   |               |  |
  <---  :after    <-----------  :after   <--- X X- -  (:after)  <- - -
    |               |       |               |      |               |
    +---------------+       +---------------+      +---------------+"
  (let [interaction-mode-icpt
        (re-frame/inject-cofx ::inject/sub
                              ^:ignore-dispose [::current])]
    (re-frame/->interceptor :id ::ro-interceptor
                            :before (comp (fn [{{mode ::current} :coeffects
                                                :as context}]
                                            (if (= mode :read-only)
                                              (assoc context :queue [])
                                              context))
                                          (:before interaction-mode-icpt)))))

(set! (.-uiInterceptor js/window)
      ro-interceptor)
