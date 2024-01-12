(ns de.explorama.frontend.aggregation-vpl.core
  (:require [clojure.string :as str]
            [data-format-lib.operations :refer [functions]]
            [de.explorama.frontend.aggregation-vpl.path :as gp]
            [de.explorama.frontend.aggregation-vpl.render.common :as rc]
            [de.explorama.frontend.aggregation-vpl.render.core :as renderer]
            [de.explorama.frontend.aggregation-vpl.render.instance-interface :as ri]
            [de.explorama.frontend.aggregation-vpl.render.workspaces.core :as rw]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.ui-base.components.formular.core :as uibf]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [taoensso.timbre :refer [error warn]]))

;PATHS
(defn- root [frame-id]
  [:aggregations frame-id])
(defn- status [frame-id]
  (conj (root frame-id) :status))
(defn- host [frame-id]
  (conj (root frame-id) :host))
(defn- width [frame-id]
  (conj (root frame-id) :width))
(defn- height [frame-id]
  (conj (root frame-id) :height))
(defn- open [frame-id]
  (conj (root frame-id) :open))
(defn- details [frame-id]
  (conj (root frame-id) :details))

(defn- options [frame-id]
  (conj (root frame-id) :options))
(defn- selected-option [frame-id]
  (conj (root frame-id) :select-option))

(defn- details-for [frame-id feature-id key]
  (conj (root frame-id) :details-for feature-id key))
(defn- details-for-heal-event [frame-id aspect]
  (conj (details-for frame-id :any :heal-event) aspect))

(defn- select-option [value]
  {:label (name value)
   :value value})

(defn- host-id [frame-id]
  (str "aggregation" frame-id))

(defonce ^:private instances (atom {}))

;TODO r10/vpl only temporary
(def ^:private width- 600)
(def ^:private height- 508)

(re-frame/reg-sub
 ::status
 (fn [db [_ frame-id]]
   (get-in db (status frame-id) :new)))

(re-frame/reg-sub
 ::host
 (fn [db [_ frame-id]]
   (get-in db (host frame-id))))

(re-frame/reg-sub
 ::width
 (fn [db [_ frame-id]]
   (get-in db (width frame-id) width-)))

(re-frame/reg-sub
 ::height
 (fn [db [_ frame-id]]
   (get-in db (height frame-id) height-)))

(re-frame/reg-sub
 ::open
 (fn [db [_ frame-id]]
   (get-in db (open frame-id))))

(re-frame/reg-event-fx
 ::init
 (fn [{db :db} [_ frame-id]]
   (swap! instances
          assoc
          frame-id
          (renderer/init frame-id
                         (host-id frame-id)
                         width-
                         height-
                         ;TODO r10/vpl this is goose specific
                         {:dim-infos (get-in db (gp/dim-info-temp frame-id))
                          :dis (get-in db (gp/data-instances-temp frame-id))
                          :data-acs (get-in db (gp/data-acs-temp frame-id))}))
   {}))

(re-frame/reg-event-db
 ::open
 (fn [db [_ frame-id]]
   (-> (update-in db (open frame-id) not))))

(defn- render-guard [status]
  (= :new
     status))

(re-frame/reg-sub
 ::details
 (fn [db [_ frame-id]]
   (get-in db (details frame-id))))

(re-frame/reg-event-db
 ::details
 (fn [db [_ frame-id payload]]
   (assoc-in db (details frame-id) payload)))

(re-frame/reg-sub
 ::options
 (fn [db [_ frame-id]]
   (get-in db (options frame-id))))

(re-frame/reg-event-db
 ::options
 (fn [db [_ frame-id payload]]
   (assoc-in db (options frame-id) payload)))

(re-frame/reg-sub
 ::selected-option
 (fn [db [_ frame-id]]
   (get-in db (selected-option frame-id))))

(re-frame/reg-event-db
 ::selected-option
 (fn [db [_ frame-id payload]]
   (assoc-in db (selected-option frame-id) payload)))

(re-frame/reg-sub
 ::dis-and-dim-infos
 (fn [db [_ frame-id]]
   ;TODO r10/vpl this is goose specific
   [(get-in db (gp/data-instances-temp frame-id))
    (get-in db (gp/dim-info-temp frame-id))
    (get-in db (gp/data-acs-temp frame-id))]))

(re-frame/reg-event-fx
 ::dis
 (fn [{db :db} [_ frame-id]]
   (when-let [instance (get @instances frame-id)]
     (swap! instance
            assoc
            ;TODO r10/vpl this is goose specific
            :current-dis
            (get-in db (gp/data-instances-temp frame-id))
            :dim-infos
            (get-in db (gp/dim-info-temp frame-id))
            :data-acs
            (get-in db (gp/dim-info-temp frame-id))))))

(re-frame/reg-event-fx
 ::reg-track
 (fn [_ [_ frame-id]]
   {:goose.tracks/register
    {:id [::dis-and-dim-infos frame-id]
     :subscription [::dis-and-dim-infos frame-id]
     :event-fn (fn [_]
                 [:goose.render.core/update frame-id])}}))

(re-frame/reg-event-fx
 ::dispose-track
 (fn [_ [_ frame-id]]
   {:goose.tracks/dispose
    {:id [::dis-and-dim-infos frame-id]}}))

(defn- optional-wrapper [state]
  [uibf/checkbox
   {:checked? @state
    :as-toggle? true,
    :label "optional",
    :on-change #(swap! state not)}])

(re-frame/reg-sub
 ::input-value
 (fn [db [_ frame-id key opts obj]]
   (get-in db
           (details-for frame-id (:id obj) key)
           (:default opts))))

(re-frame/reg-event-db
 ::input-value
 (fn [db [_ frame-id key obj new-value]]
   (assoc-in db (details-for frame-id (:id obj) key) new-value)))

(defn- input [frame-id obj opts]
  (let [optional-value (reagent/atom false)]
    (fn [frame-id obj opts]
      [:div
       (when (:optional opts)
         (optional-wrapper optional-value))
       [uibf/input-field
        {:disabled? (or (and (:optional opts)
                             (not @optional-value))
                        (not (:optional opts)))
         :type :number
         :value (re-frame/subscribe [::input-value frame-id key opts obj])
         :on-change #(re-frame/dispatch [::input-value frame-id key obj %])}]])))

(re-frame/reg-sub
 ::boolean-value
 (fn [db [_ frame-id key opts obj]]
   (get-in db
           (details-for frame-id (:id obj) key)
           (:default opts))))

(re-frame/reg-event-db
 ::boolean-change
 (fn [db [_ frame-id key obj new-value]]
   (assoc-in db (details-for frame-id (:id obj) key) new-value)))

(defn- boolean-select [frame-id key opts obj]
  [uibf/checkbox
   {:checked? (re-frame/subscribe [::boolean-value frame-id key opts obj])
    :as-toggle? true,
    :label "",
    :on-change #(re-frame/dispatch [::boolean-change frame-id key obj %])}])

(re-frame/reg-sub
 ::data-acs
 (fn [db [_ frame-id opts obj {:keys [matrix]}]]
   (let [con (:connection obj)
         start-x (aget con "startX")
         end-x (aget con "endX")
         di-idx (set (map (fn [x]
                            (:di-idx (get-in matrix [x 0])))
                          (range start-x (inc end-x))))
         ;TODO r10/vpl this is goose specific
         frame-data-acs (get-in db (conj (gp/data-acs-temp frame-id)))
         merged-acs (reduce (fn [acc values]
                              (into acc (map select-option values)))
                            #{}
                            (map (fn [i] (keys (get frame-data-acs i))) di-idx))]
     (vec (sort-by :label
                   (into (map select-option ["year" "month"])
                         merged-acs))))))

(defn- multi-select [frame-id key opts obj additional-info]
  [uibf/select
   {:options (cond (vector? (:values opts))
                   (mapv select-option (:values opts))
                   (#{:ac-contexts :ac-numbers} (:values opts))
                   @(re-frame/subscribe [::data-acs frame-id opts obj additional-info]))
    :values (re-frame/subscribe [::select-values frame-id key obj opts])
    :is-multi? true
    :on-change #(re-frame/dispatch [::select-change frame-id key obj %])}])

(re-frame/reg-sub
 ::select-values
 (fn [db [_ frame-id key obj opts]]
   (if (:default opts)
     (get-in db (details-for frame-id (:id obj) key)
             (select-option (:default opts)))
     (get-in db (details-for frame-id (:id obj) key) []))))

(re-frame/reg-event-db
 ::select-change
 (fn [db [_ frame-id key obj new-value]]
   (assoc-in db (details-for frame-id (:id obj) key) new-value)))

(defn- select [frame-id key opts obj additional-info]
  [uibf/select
   {:options (cond (vector? (:values opts))
                   (mapv select-option (:values opts))
                   (#{:ac-contexts :ac-numbers} (:values opts))
                   @(re-frame/subscribe [::data-acs frame-id opts obj additional-info]))
    :values (re-frame/subscribe [::select-values frame-id key obj opts])
    :is-multi? false
    :on-change #(re-frame/dispatch [::select-change frame-id key obj %])}])

(re-frame/reg-event-db
 ::heal-event-force-type-add
 (fn [db [_ frame-id]]
   (update-in db
              (details-for-heal-event frame-id :force-type)
              (fnil conj [])
              {:attribute nil
               :type nil})))

(re-frame/reg-event-db
 ::heal-event-force-type-rm
 (fn [db [_ frame-id idx]]
   (update-in db
              (details-for-heal-event frame-id :force-type)
              (fn [force-type]
                (into (subvec force-type 0 idx)
                      (subvec force-type (inc idx)))))))

(re-frame/reg-event-db
 ::heal-event-force-type-change-type
 (fn [db [_ frame-id idx new-value]]
   (assoc-in db
             (conj (details-for-heal-event frame-id :force-type)
                   idx
                   :type)
             new-value)))

(re-frame/reg-event-db
 ::heal-event-force-type-change-attribute
 (fn [db [_ frame-id idx new-value]]
   (assoc-in db
             (conj (details-for-heal-event frame-id :force-type)
                   idx
                   :attribute)
             new-value)))

(re-frame/reg-sub
 ::heal-event-force-type
 (fn [db [_ frame-id]]
   (get-in db (details-for-heal-event frame-id :force-type))))

(re-frame/reg-sub
 ::heal-event-force-type-attributes
 (fn [db [_ frame-id]]
   []))

(defn- heal-event-force-type-comp [frame-id key op-key opts obj additional-info]
  (-> [:div {}]
      (into (map-indexed
             (fn [idx {:keys [attribute type]}]
               [:div {:style {:display :flex}}
                [uibf/select
                 {:options @(re-frame/subscribe [::heal-event-force-type-attributes frame-id])
                  :values (when attribute (select-option attribute))
                  :is-multi? false
                  :on-change #(re-frame/dispatch [::heal-event-force-type-change-attribute frame-id idx])}]
                [uibf/select
                 {:options (mapv select-option [:integer :double :string])
                  :values (when type (select-option type))
                  :is-multi? false
                  :on-change #(re-frame/dispatch [::heal-event-force-type-change-type frame-id key obj %])}]
                [uibf/button {:label "-", :on-click #(re-frame/dispatch [::heal-event-force-type-rm frame-id idx])}]])
             @(re-frame/subscribe [::heal-event-force-type frame-id])))
      (conj [:div {}
             [uibf/button {:label "+", :on-click #(re-frame/dispatch [::heal-event-force-type-add frame-id])}]])))

(re-frame/reg-sub
 ::heal-event-addon
 (fn [db [_ frame-id aspect1 aspect2 default option?]]
   (get-in db (conj (details-for-heal-event frame-id :addon) aspect1 aspect2) (if option?
                                                                                (select-option default)
                                                                                default))))

(re-frame/reg-event-db
 ::heal-event-addon
 (fn [db [_ frame-id aspect1 aspect2 new-value]]
   (assoc-in db (conj (details-for-heal-event frame-id :addon) aspect1 aspect2) new-value)))

(defn- heal-event-addons-comp [frame-id key op-key opts obj additional-info]
  [:div {:style {:display :flex
                 :flex-direction :column}}
   [:div {:style {:display :flex}}
    [uibf/input-field
     {:label "Datasource"
      :value (re-frame/subscribe [::heal-event-addon frame-id :datasource :value "datasource" false])
      :max-length 40
      :extra-class "input--w10",
      :on-change #(re-frame/dispatch [::heal-event-addon frame-id :datasource :value %])}]]
   [:div {:style {:display :flex}}
    [uibf/select
     {:options (mapv select-option [:event-type :indicator-type :custom-type])
      :values (re-frame/subscribe [::heal-event-addon frame-id :type :name :aggregation true])
      :is-multi? false
      :extra-class "input--w10",
      :on-change #(re-frame/dispatch [::heal-event-addon frame-id :type :name %])}]
    [uibf/input-field
     {:extra-class "input--w10",
      :max-length 40
      :value (re-frame/subscribe [::heal-event-addon frame-id :type :value "Aggregation" false])
      :on-change #(re-frame/dispatch [::heal-event-addon frame-id :type :value "Aggregation"])}]]])

(re-frame/reg-sub
 ::heal-event-workaround
 (fn [db [_ frame-id gran]]
   (get-in db (conj (details-for-heal-event frame-id :workaround) gran) 1)))

(re-frame/reg-event-db
 ::heal-event-workaround
 (fn [db [_ frame-id gran new-value]]
   (assoc-in db (conj (details-for-heal-event frame-id :workaround) gran) new-value)))

(defn- heal-event-workaround-comp [frame-id key op-key opts obj additional-info]
  [:div {:style {:display :flex}}
   [uibf/input-field
    {:label "month"
     :type :number
     :min 1
     :max 12
     :extra-class "input--w4",
     :value (re-frame/subscribe [::heal-event-workaround frame-id :month])
     :on-change #(re-frame/dispatch [::heal-event-workaround frame-id :month %])}]
   [uibf/input-field
    {:label "day"
     :type :number
     :min 1
     :max 31
     :extra-class "input--w4",
     :value (re-frame/subscribe [::heal-event-workaround frame-id :day])
     :on-change #(re-frame/dispatch [::heal-event-workaround frame-id :day %])}]
   "Currently only internal - dont touch it"])

(re-frame/reg-sub
 ::heal-event-force-descs
 (fn [db [_ frame-id idx]]
   (get-in db (conj (details-for-heal-event frame-id :descs) idx) "")))

(re-frame/reg-event-db
 ::heal-event-force-descs
 (fn [db [_ frame-id idx new-value]]
   (assoc-in db (conj (details-for-heal-event frame-id :descs) idx) new-value)))

(defn- heal-event-descs-comp [frame-id key op-key opts obj additional-info]
  (let [{:keys [matrix]} additional-info
        end-x (count matrix)
        datasets (reduce (fn [acc i]
                           (let [element (peek (get matrix i))
                                 connection (-> element
                                                :connection)
                                 [start-x end-x] (if connection
                                                   [(aget connection "startX")
                                                    (aget connection "endX")]
                                                   [i i])]
                             (assoc acc
                                    [start-x end-x]
                                    (assoc element
                                           :sx start-x
                                           :ex end-x))))
                         {}
                         (range end-x))]
    (into [:div {:style {:display :flex
                         :flex-direction :column}}]
          (map-indexed (fn [idx [_ {:keys [op-name sx ex]}]]
                         [:div {:style {:display :flex}}
                          [uibf/input-field
                           {:label (str op-name " (" sx "- " ex ")")
                            :max-length 80
                            :extra-class "input--w10",
                            :value @(re-frame/subscribe [::heal-event-force-descs frame-id idx])
                            :on-change #(re-frame/dispatch [::heal-event-force-descs frame-id idx %])}]])
                       datasets))))

(defn- custom-comp [frame-id key opts obj additional-info]
  (let [op-key (get-in obj [:meta-data :key])]
    (case [op-key key]
      [:heal-event :force-type] (heal-event-force-type-comp frame-id key op-key opts obj additional-info)
      [:heal-event :addons] (heal-event-addons-comp frame-id key op-key opts obj additional-info)
      [:heal-event :workaround] (heal-event-workaround-comp frame-id key op-key opts obj additional-info)
      [:heal-event :descs] (heal-event-descs-comp frame-id key op-key opts obj additional-info)
      (do (error "error no custom comp")
          [:div
           "error no custom comp"]))))

(defn- reagent-canvas [frame-id]
  (let [host-ref (atom nil)]
    (reagent/create-class {:display-name (host-id frame-id)
                           :reagent-render
                           (fn [frame-id]
                             (let [status @(re-frame/subscribe [::status frame-id])
                                   host (host-id frame-id)
                                   width @(re-frame/subscribe [::width frame-id])
                                   height @(re-frame/subscribe [::height frame-id])
                                   open @(re-frame/subscribe [::open frame-id])]
                               (when (and status height width host open)
                                 [:canvas.aggregation-canvas
                                  {:key host
                                   :ref #(reset! host-ref %)
                                   :id host
                                   :style {:width width
                                           :height height
                                           :right (- width)
                                           :position :fixed}
                                   :on-drag-enter #() ;For Woco handling drop-target
                                   :on-drag-leave #() ;For Woco handling drop-target
                                   :on-mouse-up #()
                                   :on-mouse-enter #()
                                   :on-mouse-leave #()
                                   :on-click #(re-frame/dispatch (fi/call-api :frame-bring-to-front-event-vec frame-id))}])))
                           :component-did-mount
                           (fn [this]
                             (let [status @(re-frame/subscribe [::status frame-id])
                                   host (host-id frame-id)
                                   container (js/document.getElementById host)]
                               (when (nil? container)
                                 (warn "updating nil canvas" host))
                               (when (render-guard status)
                                 (re-frame/dispatch [::init frame-id]))))
                           :should-component-update
                           (fn [_ _ _]
                             (let [status @(re-frame/subscribe [::status frame-id])]
                               (render-guard status)))
                           :component-did-update
                           (fn [this old-argv]
                             (let [status @(re-frame/subscribe [::status frame-id])
                                   host (host-id frame-id)
                                   container (js/document.getElementById host)]
                               (when (nil? container)
                                 (warn "updating nil canvas" host))
                               (when (render-guard status)
                                 (re-frame/dispatch [::init frame-id]))))
                           :component-will-unmount
                           (fn [this])})))

(re-frame/reg-event-db
 ::start-apply
 (fn [db [_ frame-id]]
   (assoc-in db (details frame-id)
             {:op-name "Create Event"
              :op-type :close
              :meta-data (-> functions :heal-event meta)
              :additional-info
              {:matrix (:matrix @(ri/state (get @instances frame-id)))}})))

(defn- next-xy [x y bounds-x]
  (if (<= bounds-x (inc x))
    [0 (inc y)]
    [(inc x) y]))

(defn- check-bounds [visited-xy]
  (every? true? visited-xy))

(defn- custom-transformation [db frame-id feature-id key k attributes]
  (case [key k]
    [:heal-event :descs] (let [descs (get-in db (details-for-heal-event frame-id :descs))]
                           (mapv (fn [[_ value]]
                                   {:attribute value})
                                 (sort-by (fn [[idx]]) descs)))
    [:heal-event :addons] (let [{{datasource :value} :datasource
                                 {tvalue :value
                                  {tname :value} :name} :type}
                                (get-in db (details-for-heal-event frame-id :addon))]
                            [{:attribute "datasource" :value (or datasource "datasource")}
                             {:attribute "notes" :value (or tvalue "Aggregation")}
                             {:attribute (or tname "aggregation") :value (or tvalue "Aggregation")}])
    [:heal-event :workaround] (let [{:keys [day month year]
                                     :or {day "01"
                                          month "01"
                                          year "2022"}}
                                    (get-in db (details-for-heal-event frame-id :workaround))]
                                {"date" {:day day
                                         :month month
                                         :year year}})
    [:heal-event :force-type] (let [types (get-in db (details-for-heal-event frame-id :force-type))]
                                (mapv (fn [{{a :value} :attribute {t :value} :type}]
                                        {:attribute a
                                         :new-type t})
                                      types))
    [:heal-event :generate-ids] {:policy :uuid}
    [:group-by :forced-groups] nil
    (do (error "error no custom transformation" key k)
        nil)))

(defn- create-attributes [db frame-id feature-id key attributes]
  (reduce-kv (fn [acc k {:keys [type default hidden] :as attribute}]
               (if (and (not default)
                        hidden)
                 acc
                 (assoc acc k
                        (case type
                          :select (get-in db (conj (details-for frame-id feature-id k) :value) default)
                          :multi-select (mapv :value (get-in db (details-for frame-id feature-id k) default))
                          :boolean (get-in db (details-for frame-id feature-id k) default)
                          :input (get-in db (details-for frame-id feature-id k) default)
                          :number (get-in db (details-for frame-id feature-id k) default)
                          :custom (custom-transformation db frame-id feature-id key k attributes)))))
             {}
             attributes))

(re-frame/reg-event-fx
 ::apply
 (fn [{db :db} [_ frame-id]]
   (let [matrix (:matrix @(ri/state (get @instances frame-id)))
         bounds-x (count matrix)
         bounds-y (mapv (fn [idx]
                          (count (get matrix idx)))
                        (range (count matrix)))
         result
         (loop [x 0
                y 0
                result (mapv (fn [_] nil) (range (count matrix)))
                visited-xy (mapv (fn [_] nil) (range (count matrix)))
                dis {}]
           (if (check-bounds visited-xy)
             {:ops (into [:heal-event (create-attributes db frame-id :any :heal-event (-> functions :heal-event meta (get-in [:steering :attributes])))]
                         (filter identity result))
              :dis dis}
             (let [element (get-in matrix [x y])
                   [nx ny] (next-xy x y bounds-x)
                   visited-xy (if (<= (get bounds-y x) y)
                                (assoc visited-xy x true)
                                visited-xy)]
               (cond (get visited-xy x)
                     (recur nx
                            ny
                            result
                            visited-xy
                            dis)
                     (= :dataset (:op-type element))
                     (recur nx
                            ny
                            (assoc result x (str "di" x))
                            visited-xy
                            (assoc dis
                                   (str "di" x)
                                   (get-in db (conj (gp/data-instances-temp frame-id) (:di-idx element)))))
                     :else
                     (let [{:keys [connection id]
                            {:keys [key]
                             {attributes :attributes} :steering}
                            :meta-data}
                           element
                           start-x (aget connection "startX")
                           end-x (aget connection "endX")]
                       (cond (and (= :op (:op-type element))
                                  (= start-x end-x))
                             (recur nx
                                    ny
                                    (update result x
                                            (fn [current-value]
                                              [key (create-attributes db frame-id id key attributes)
                                               current-value]))
                                    visited-xy
                                    dis)
                             (and (= :op (:op-type element))
                                  (= start-x x)
                                  (not= start-x end-x))
                             (recur nx
                                    ny
                                    (let [new-value-x (into [key (create-attributes db frame-id id key attributes)]
                                                            (->> (map (fn [idx]
                                                                        (get result idx))
                                                                      (range start-x (inc end-x)))
                                                                 (filter identity)))]
                                      (vec (map-indexed (fn [idx value]
                                                          (cond (= idx x)
                                                                new-value-x
                                                                (<= (inc start-x) idx end-x)
                                                                nil
                                                                :else value))
                                                        result)))
                                    visited-xy
                                    dis)
                             :else
                             (recur nx
                                    ny
                                    result
                                    visited-xy
                                    dis)))))))]
     (js/console.log "DI/OP" result)
     {:db (assoc-in db (details frame-id) nil)
      ;TODO r10/vpl this is goose specific
      :dispatch [:goose.operations.tasks/execute-wrapper
                 frame-id
                 :aggregate
                 result]})))

(re-frame/reg-event-fx
 ::add-option-to-col
 (fn [{db :db} [_ frame-id {:keys [col key meta]}]]
   (let [{{x :x} :additional-info} col
         instance (get @instances frame-id)
         app (ri/app instance)
         {:keys [matrix]} @(ri/state instance)
         latest-element (peek (get matrix x))
         connection (get latest-element :connection)]
     (if-not connection
       (swap! (ri/state instance) update-in [:matrix x] conj (rc/create-op {:op-name (name key)
                                                                            :meta-data meta}
                                                                           (rc/create-con x x)))
       (let [start (aget connection "startX")
             end (aget connection "endX")]
         (swap! (ri/state instance) update :matrix
                (fn [matrix]
                  (reduce (fn [matrix x]
                            (update matrix
                                    x
                                    conj
                                    (rc/create-op {:op-name (name key)
                                                   :meta-data meta}
                                                  (rc/create-con start end))))
                          matrix
                          (range start (inc end)))))))
     (swap! (ri/state instance) dissoc :drag-obj)
     (rw/reset-workspace-mutable instance)
     (rw/draw-workspace-mutable instance)
     (rc/render app))
   {:db (-> (assoc-in db (options frame-id) nil)
            (assoc-in (selected-option frame-id) nil))}))

(defn- add-operation [frame-id option {selected-key :key}]
  (reduce (fn [acc [category-key functions]]
            (conj acc
                  [:div
                   (name category-key)
                   (into [:div {:style {:display :flex}}]
                         (map (fn [[function-key function]]
                                (let [meta-data (meta function)
                                      function-name (name function-key)]
                                  [:div {:style {:margin-left 3
                                                 :margin-right 3
                                                 :width 75
                                                 :background-color (if (= selected-key function-key)
                                                                     "#333333"
                                                                     "#336633")
                                                 :border {:border :solid
                                                          :border-width "1px 1px"}}
                                         :on-click #(re-frame/dispatch [::selected-option frame-id {:key function-key
                                                                                                    :meta meta-data}])}
                                   function-name
                                   [uibf/button {:label "add"
                                                 :on-click #(re-frame/dispatch [::add-option-to-col frame-id {:col option
                                                                                                              :key function-key
                                                                                                              :meta meta-data}])}]]))
                              functions))]))
          [:div]
          (group-by (fn [[_ function]]
                      (or ((meta function) :category)
                          :special))
                    (filter (fn [[_ function]]
                              (not (:internal (meta function))))
                            functions))))

(defn- operation-details [table-style tr-style frame-id {:keys [op-name meta-data op-type additional-info] :as details}]
  (when (#{:dataset} op-type)
    [:div
     [:div {:style {:font-weight 800}} "General"]
     [:table table-style
      [:colgroup
       [:col {:style {:width 120}}]
       [:col {:style {:width "80%"}}]]
      [:thead [:tr
               [:th]
               [:th]]]
      [:tbody {:style {:border :solid
                       :border-width "1px 0"}}
       [:tr tr-style
        [:td "Datasouces"]
        [:td (str/join ", " (get-in details [:dim-info :datasources]))]]
       [:tr tr-style
        [:td "Years"]
        [:td {:style {:overflow-wrap :anywhere}} (str/join ", " (get-in details [:dim-info :years]))]]
       [:tr tr-style
        [:td "Countries"]
        [:td {:style {:overflow-wrap :anywhere}} (str/join ", " (get-in details [:dim-info :countries]))]]]]
     [:div {:style {:font-weight 800}} "Data instance ..."
      #_(:di details)]])
  (when (#{:options} op-type)
    [:div
     [:div {:style {:font-weight 800}} "General"]
     [:table table-style
      [:colgroup
       [:col {:style {:width 120}}]
       [:col {:style {:width "80%"}}]]
      [:thead [:tr
               [:th]
               [:th]]]
      (into [:tbody]
            (comp (map (fn [[k v]]
                         (when (not (#{:steering :key} k))
                           [:tr tr-style
                            [:td (name k)]
                            [:td v]])))
                  (filter identity))
            meta-data)]
     [:div {:style {:font-weight 800}} "Attributes"]
     [:table table-style
      [:colgroup
       [:col {:style {:width 120}}]
       [:col {:style {:width "80%"}}]]
      [:thead [:tr
               [:th]
               [:th]]]
      (into [:tbody]
            (comp (map (fn [[k v]]
                         [:tr tr-style
                          [:td (name k)]
                          [:td (name (:type v))]]))
                  (filter identity))
            (get-in meta-data [:steering :attributes]))]])
  (when (#{:op :close} op-type)
    [:div
     [:div {:style {:font-weight 800
                    :margin-bottom "5px"}} "General"]
     [:table table-style
      [:colgroup
       [:col {:style {:width 120}}]
       [:col {:style {:width "80%"}}]]
      [:thead [:tr
               [:th]
               [:th]]]
      (into [:tbody]
            (comp (map (fn [[k v]]
                         (when (not (#{:steering :key} k))
                           [:tr tr-style
                            [:td (name k)]
                            [:td v]])))
                  (filter identity))
            meta-data)]
     [:div {:style {:font-weight 800}} "Attributes"]
     [:table table-style
      [:colgroup
       [:col {:style {:width 120}}]
       [:col {:style {:width "80%"}}]]
      [:thead [:tr
               [:th]
               [:th]]]
      (into [:tbody]
            (comp (map (fn [[k v]]
                         (when (not (:hidden v))
                           [:tr tr-style
                            [:td (name k)]
                            [:td
                             (case (:type v)
                               :multi-select [multi-select frame-id k v details additional-info]
                               :select [select frame-id k v details additional-info]
                               :custom [custom-comp frame-id k v details additional-info]
                               :boolean [boolean-select frame-id k v details]
                               :number [input frame-id k v details])]])))
                  (filter identity))
            (get-in meta-data [:steering :attributes]))]]))

(defn view [frame-id]
  (let [open @(re-frame/subscribe [::open frame-id])
        {:keys [op-name op-type] :as details}
        @(re-frame/subscribe [::details frame-id])
        {:as options}
        @(re-frame/subscribe [::options frame-id])
        selected-option
        @(re-frame/subscribe [::selected-option frame-id])]
    (js/console.log "##VIEW## details view" details)
    (js/console.log "##VIEW## options" options)
    [:<>
     [:div.aggregations__placeholder {:style {:right -25 :position :fixed :z-index 1
                                              :display :flex}}
      [:div {:style {:width 25 :height 25 :background-color "#555" :font-size 18
                     :color "#FFF" :font-weight "bold" :padding "0px 8px 0px 8px"
                     :cursor :pointer}
             :on-click #(re-frame/dispatch [::open frame-id])}
       ">"]
      (when open
        [uibf/button {:label "Apply", :on-click #(re-frame/dispatch [::start-apply frame-id])}])]
     (when open
       [reagent-canvas frame-id])
     (when (or details options)
       (let [width @(re-frame/subscribe [::width frame-id])
             height @(re-frame/subscribe [::height frame-id])
             tr-style {:style {:border :solid
                               :border-width "1px 0"
                               :border-color "#AAAAAA"
                               :margin-bottom "1px"}}
             table-style {:style {:border-collapse :collapse
                                  :margin-bottom "5px"}}]
         [:div {:style {:width width
                        :height height
                        :right (- width)
                        :position :fixed
                        :display :flex
                        :flex-wrap :nowrap
                        :align-content :center
                        :justify-content :center
                        :align-items :center}}
          [:<>
           [:div {:style {:position :inherit
                          :width 600
                          :height 508
                          :background-color "#000"
                          :opacity 0.2
                          :z-index 0}}]
           [:div {:style {:top 32
                          :left 100
                          :width 450
                          :height 400
                          :z-index 1}}
            [:div {:style {:width "100%"
                           :height 32
                           :background-color "#546b90"
                           :color "#fff"
                           :display :flex
                           :align-content :center
                           :justify-content :flex-start
                           :align-items :center
                           :padding-left 25
                           :font-weight 600}}
             (when details
               (str "details for operation " (name op-name)))]
            [:div {:style {:width "100%"
                           :height 368
                           :background-color "#fff"}}
             [:div {:style {:width "100%"
                            :height 318
                            :background-color "#fff"
                            :border-bottom "3px solid #888"
                            :overflow-y :scroll}}
              (when details
                (operation-details table-style tr-style frame-id details))
              (when options
                (add-operation frame-id options selected-option))]
             [:div {:style {:bottom 0
                            :width "100%"
                            :height 50
                            :background-color "#fff"
                            :display :flex
                            :align-content :center
                            :justify-content :flex-start
                            :align-items :center
                            :padding-left 25}}
              (when (and details (#{:options :dataset} op-type))
                [uibf/button {:label "ok", :on-click #(re-frame/dispatch [::details frame-id nil])}])
              (when (and details (#{:op} op-type))
                [uibf/button {:label "ok", :on-click #(re-frame/dispatch [::details frame-id nil])}])
              (when (and details (#{:close} op-type))
                [:<>
                 [uibf/button {:label "apply", :on-click #(re-frame/dispatch [::add-option frame-id])}]
                 [uibf/button {:label "back", :on-click #(re-frame/dispatch [::details frame-id nil])}]])
              (when options
                [:<>
                 [uibf/button {:label "apply",
                               :disabled? (nil? selected-option)
                               :on-click #(re-frame/dispatch [::add-option-to-col frame-id (assoc selected-option
                                                                                                  :col options)])}]
                 [uibf/button {:label "back", :on-click #(re-frame/dispatch [::options frame-id nil])}]])]]]]]))]))

