(ns de.explorama.frontend.woco.product-tour
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.path :as path]))

(def ^:private mosaic-tour
  {:needed-component [:search :mosaic]
   :steps [{:title :product-tour-title-o-search
            :description [[:msg
                           [:translation :product-tour-desc-o-search-1]
                           [:icon {:icon :search}]
                           [:translation :product-tour-desc-o-search-2]]]
            :component :toolbar
            :additional-info :search
            :auto-next? true
            :step 1}
           {:title :product-tour-title-e-search
            :description [[:msg
                           [:translation :product-tour-desc-d-search-1]
                           [:img "img/search_button.svg"]
                           [:translation :product-tour-desc-d-search-2]]]
            :component :search
            :additional-info :search
            :auto-next? true
            :step 2}
           {:title :product-tour-title-o-mosaic
            :description [[:msg
                           [:translation :product-tour-desc-o-mosaic-1]
                           [:icon {:icon :mosaic2}]
                           [:translation :product-tour-desc-o-mosaic-2]]]
            :component :toolbar
            :additional-info :mosaic
            :auto-next? true
            :step 3}
           {:title :product-tour-title-m-mosaic
            :description [[:msg
                           [:translation :product-tour-desc-m-mosaic-1]
                           [:translation [:preference-panning :and]]
                           [:translation :product-tour-desc-m-mosaic-2]]]
            :component :mosaic
            :additional-info :move
            :additional-minor-steps [:navigation]
            :auto-next? true
            :step 4}
           {:title :product-tour-title-c-search
            :description [[:translation :product-tour-desc-c-search]]
            :component :search
            :additional-info :connect
            :additional-minor-steps [:move :navigation]
            :auto-next? true
            :step 5}
           {:title :product-tour-title-s-mosaic
            :description [[:msg
                           [:translation :product-tour-desc-s-mosaic-1]
                           [:icon {:icon :sort
                                   :color :teal}]
                           [:translation :product-tour-desc-s-mosaic-2]]]
            :component :mosaic
            :additional-info :sort-by
            :additional-minor-steps [:navigation]
            :auto-next? true
            :step 6}
           {:title :product-tour-title-cl-mosaic
            :description [[:msg
                           [:translation :product-tour-desc-cl-mosaic-1]
                           [:icon {:icon :info-square
                                   :color :teal}]
                           [:translation :product-tour-desc-cl-mosaic-2]]]
            :component :mosaic
            :additional-info :settings-info
            :additional-minor-steps [:navigation]
            :auto-next? true
            :step 7}
           {:title :product-tour-title-o-details
            :description [[:msg
                           [:translation :product-tour-desc-pz-mosaic-1]
                           [:translation [:preference-panning :or]]
                           [:translation :product-tour-desc-pz-mosaic-2]
                           [:translation :product-tour-desc-o-details]]]
            :component :mosaic
            :additional-info :details-view
            :additional-minor-steps [:navigation]
            :auto-next? true
            :step 8}
           {:title :product-tour-title-c-details
            :description [[:translation :product-tour-desc-c-details]]
            :component :woco-details-view
            :additional-info :close
            :additional-minor-steps [:navigation]
            :auto-next? true
            :step 9}
           {:title :product-tour-title-d-vis
            :description [[:msg
                           [:translation :product-tour-desc-d-vis-1]
                           [:img "img/direct_vis.svg"]
                           [:translation :product-tour-desc-d-vis-2]]]
            :component :search
            :additional-info :direct-vis
            :additional-minor-steps [:navigation]
            :auto-next? true
            :step 10}
           {:title :product-tour-title-os-project
            :description [[:msg
                           [:translation :product-tour-desc-os-project-1]
                           [:img "img/project-button-save.svg"]
                           [:translation :product-tour-desc-os-project-2]]]
            :component :project-action
            :additional-info :save
            :additional-minor-steps [:navigation]
            :auto-next? true
            :step 11}
           {:title :product-tour-title-s-project
            :description [[:translation :product-tour-desc-s-project]]
            :component :projects
            :additional-info :save-project
            :additional-minor-steps [:navigation]
            :auto-next? true
            :step 12}
           {:title :product-tour-title-op-overview
            :description [[:msg
                           [:translation :product-tour-desc-op-overviews-1]
                           [:icon {:icon :folder-open}]
                           [:translation :product-tour-desc-op-overviews-2]]]
            :component :header-tools
            :additional-info :projects
            :additional-minor-steps [:navigation]
            :auto-next? true
            :step 13}]})

(def ^:private vis-product-desc
  {:needed-component [:search [:visualization-charts :map :visualization-table]]
   :steps [{:title :product-tour-title-o-search
            :description [[:msg
                           [:translation :product-tour-desc-o-search-1]
                           [:icon {:icon :search}]
                           [:translation :product-tour-desc-o-search-2]]]
            :component :toolbar
            :additional-info :search
            :auto-next? true
            :step 1}
           {:title :product-tour-title-e-search
            :description [[:msg
                           [:translation :product-tour-desc-d-search-1]
                           [:img "img/search_button.svg"]
                           [:translation :product-tour-desc-d-search-2]]]
            :component :search
            :additional-info :search
            :auto-next? true
            :step 2}
           {:title :product-tour-title-o-vis
            :description [[:translation :product-tour-desc-o-vis]]
            :component :toolbar
            :additional-info :*
            :auto-next? true
            :step 3}
           {:title :product-tour-title-m-vis
            :description [[:translation :product-tour-desc-m-vis]]
            :component :*
            :additional-info :move
            :auto-next? true
            :step 4}
           {:title :product-tour-title-c-search-vis
            :description [[:translation :product-tour-desc-c-search-vis]]
            :component :search
            :additional-info :connect
            :additional-minor-steps [:move]
            :auto-next? true
            :step 5}
           {:title :product-tour-title-d-vis
            :description [[:msg
                           [:translation :product-tour-desc-d-vis-1]
                           [:img "img/direct_vis.svg"]
                           [:translation :product-tour-desc-d-vis-2]]]
            :component :search
            :additional-info :direct-vis
            :auto-next? true
            :step 6}
           {:title :product-tour-title-os-project
            :description [[:msg
                           [:translation :product-tour-desc-os-project-1]
                           [:img "img/project-button-save.svg"]
                           [:translation :product-tour-desc-os-project-2]]]
            :component :project-action
            :additional-info :save
            :auto-next? true
            :step 7}
           {:title :product-tour-title-s-project
            :description [[:translation :product-tour-desc-s-project]]
            :component :projects
            :additional-info :save-project
            :auto-next? true
            :step 8}
           {:title :product-tour-title-op-overview
            :description [[:msg
                           [:translation :product-tour-desc-op-overviews-1]
                           [:icon {:icon :folder-open}]
                           [:translation :product-tour-desc-op-overviews-2]]]
            :component :header-tools
            :additional-info :projects
            :auto-next? true
            :step 9}]})

(def ^:private no-search-desc
  {:needed-component []
   :steps [{:title :product-tour-title-n-search
            :description [[:translation :product-tour-desc-n-search]]
            :component :toolbar
            :additional-info :*
            :step 1}
           {:title :product-tour-title-os-project
            :description [[:msg
                           [:translation :product-tour-desc-os-project-1]
                           [:img "img/project-button-save.svg"]
                           [:translation :product-tour-desc-os-project-2]]]
            :component :project-action
            :additional-info :save
            :auto-next? true
            :step 2}
           {:title :product-tour-title-s-project
            :description [[:translation :product-tour-desc-s-project]]
            :component :projects
            :additional-info :save-project
            :auto-next? true
            :step 3}
           {:title :product-tour-title-op-overview
            :description [[:msg
                           [:translation :product-tour-desc-op-overviews-1]
                           [:icon {:icon :folder-open}]
                           [:translation :product-tour-desc-op-overviews-2]]]
            :component :header-tools
            :additional-info :projects
            :auto-next? true
            :step 4}]})

(def ^:private all-tours [mosaic-tour vis-product-desc no-search-desc])

(defn- viable-tour? [all-tools-ids product-tour]
  (let [needed-tools (:needed-component product-tour)]
    (every? (fn [n-tool]
              (if (vector? n-tool)
                (some all-tools-ids n-tool)
                (all-tools-ids n-tool)))
            needed-tools)))

(defn num-of-max-steps [db]
  (:step (peek (get-in db path/product-tour-steps))))

(defn start-product-tour [db]
  (let [all-tools-ids (->> (get-in db path/tool-descs)
                           vals
                           (map :component)
                           (filter identity)
                           set)
        filter-fn (partial viable-tour? all-tools-ids)
        found-steps (->> all-tours
                         (filter filter-fn)
                         first
                         :steps)]
    (-> db
        (assoc-in path/product-tour-steps found-steps)
        (assoc-in path/product-tour-all-steps found-steps))))

(defn next-step [db next-component]
  (let [updated-db-list (update-in db path/product-tour-steps (comp vec rest))]
    {:db (if-not (empty? (get-in updated-db-list path/product-tour-steps))
           (update-in updated-db-list
                      (conj path/product-tour-steps 0)
                      (fn [{:keys [component] :as step}]
                        (if (and (= component :*)
                                 next-component)
                          (assoc step :component next-component)
                          step)))
           updated-db-list)
     :dispatch [::check-done-tour]}))

(defn previous-step [db original-prev-decs?]
  (let [{step-num :step
         component :component
         additional-info :additional-info
         minor-steps :additional-minor-steps}
        (get-in db path/product-tour-current-step)
        prev-step-num (dec step-num)
        {:keys [description] :as prev-step}
        (some #(when (= (:step %)
                        prev-step-num)
                 %)
              (get-in db path/product-tour-all-steps))]
    {:db (update-in db
                    path/product-tour-steps
                    (fn [steps]
                      (into [(if original-prev-decs?
                               prev-step
                               (assoc prev-step
                                      :auto-next? false
                                      :component component
                                      :description (conj description
                                                         [:msg
                                                          [:icon {:icon :check}]
                                                          [:translation :product-tour-step-done]])
                                      :additional-info additional-info
                                      :additional-minor-steps minor-steps
                                      :next-active-sub nil
                                      :ignore-action? true))]
                            steps)))}))

(defn cancel-tour [db]
  {:db (assoc-in db path/product-tour-steps nil)
   :fx [[:dispatch [:de.explorama.frontend.woco.api.notifications/notify
                    {:type :i
                     :vertical :woco
                     :category {:misc :product-tour}
                     :message (i18n/translate db
                                              :product-tour-closed)}]]
        [:dispatch (fi/call-api [:user-preferences :save-event-vec]
                                "start-tour" false)]]})

(re-frame/reg-event-db
 ::start-tour
 (fn [db _]
   (start-product-tour db)))

(re-frame/reg-event-fx
 ::check-done-tour
 (fn [{db :db} _]
   (let [done? (empty? (get-in db path/product-tour-steps))]
     (when done?
       {:fx [[:dispatch [:de.explorama.frontend.woco.api.notifications/notify
                         {:type :s
                          :vertical :woco
                          :category {:misc :product-tour}
                          :message (i18n/translate db
                                                   :product-tour-done)}]]
             [:dispatch (fi/call-api [:user-preferences :save-event-vec]
                                     "start-tour" false)]]}))))

(re-frame/reg-sub
 ::running?
 (fn [db]
   (-> (get-in db path/product-tour-steps nil)
       seq
       boolean)))

(comment
  (re-frame/dispatch [::check-done-tour])
  (re-frame/dispatch [:de.explorama.frontend.woco.api.notifications/notify
                      {:type :s
                       :vertical :woco
                       :category {:misc :product-tour}
                       :message @(re-frame/subscribe [::i18n/translate :product-tour-done])}]))
