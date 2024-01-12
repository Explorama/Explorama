(ns de.explorama.frontend.knowledge-editor.main-view
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [de.explorama.frontend.ui-base.components.common.core :refer [tooltip
                                                                          virtualized-list]]
            [de.explorama.frontend.knowledge-editor.config :as config :refer [height
                                                                              width]]
            [de.explorama.frontend.knowledge-editor.journal :as journal]
            [de.explorama.frontend.knowledge-editor.markdown-helper :refer [link-pattern
                                                                            split-pattern]]
            [de.explorama.frontend.knowledge-editor.path :as path]
            [de.explorama.frontend.knowledge-editor.store :refer [dummy-contexts
                                                                  dummy-events
                                                                  dummy-figures
                                                                  filtered-contexts
                                                                  filtered-events
                                                                  filtered-figures]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button
                                                                            button-group
                                                                            input-field
                                                                            textarea]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [taoensso.timbre :refer [error warn]]))

(def ^:private editor-value (reagent/atom nil))

(defn- date-now-iso []
  (-> (js/Date.)
      .toISOString
      (subs 0 19)))

(defn print-states []
  (println (str @dummy-events))
  (println (str @dummy-contexts))
  (println (str @dummy-figures)))

(def ^:private default-event ":prop/id :auto\n:prop/title \"New Title\"\n:date/creation :auto\n:context/type \"entry\"\n:datasource/name \"datasource\"\n\"Some text\"")
(def ^:private default-context ":prop/id :auto\n:prop/name \"New Title\"\n:date/creation :auto\n:context/type \"entry\"\n:datasource/name \"datasource\"\n\"Some text\"")
(def ^:private default-journal "")

(re-frame/reg-sub
 ::details-view
 (fn [db [_ frame-id]]
   (get-in db (path/details frame-id))))

(re-frame/reg-sub
 ::canvas-dialog
 (fn [db [_ frame-id]]
   (get-in db (path/canvas-dialog frame-id))))

(re-frame/reg-event-db
 ::close-canvas-dialog
 (fn [db [_ frame-id]]
   (path/dissoc-in db (path/canvas-dialog frame-id))))

(re-frame/reg-event-db
 ::save-current-figure
 (fn [db [_ frame-id]]
   (assoc-in db
             (path/canvas-dialog frame-id)
             {:type :save-dialog
              :frame-id frame-id
              :payload (get-in db (path/canvas-content frame-id))})))

(re-frame/reg-event-db
 ::investigate-figure-element
 (fn [db [_ frame-id id]]
   (assoc-in db
             (path/canvas-dialog frame-id)
             {:type :investigate-figure-element
              :frame-id frame-id
              :id id
              :element-type (get-in db (conj (path/canvas-content frame-id)
                                             id
                                             :type))
              :color (get-in db (conj (path/canvas-content frame-id)
                                      id
                                      :color))
              :element-id (get-in db (conj (path/canvas-content frame-id)
                                           id
                                           :element-id))})))

(re-frame/reg-event-db
 ::investigate-figure-connection
 (fn [db [_ frame-id id]]
   (assoc-in db
             (path/canvas-dialog frame-id)
             {:type :investigate-figure-connection
              :frame-id frame-id
              :id id
              :color (get-in db (conj (path/canvas-content frame-id)
                                      id
                                      :color))})))

(defn- dialog-header [child]
  [:div {:style {:margin 10
                 :display :flex
                 :flex-direction :row
                 :justify-content :space-between}}
   child])

(defn- dialog-body [child]
  [:div {:style {:margin 10
                 :display :flex
                 :flex-direction :column
                 :justify-content :space-between}}
   child])

(re-frame/reg-event-db
 ::close-details
 (fn [db [_ frame-id]]
   (path/dissoc-in db (path/details frame-id))))

(re-frame/reg-event-db
 ::details-edit
 (fn [db [_ frame-id value]]
   (assoc-in db (conj (path/details frame-id) :edit?) value)))

(re-frame/reg-event-db
 ::create-event
 (fn [db [_ view-type frame-id]]
   (assoc-in db (path/details frame-id) {:mode :create
                                         :type view-type
                                         :edit? true})))

(re-frame/reg-event-db
 ::create-journal
 (fn [db [_ view-type frame-id]]
   (assoc-in db (path/details frame-id) {:mode :create
                                         :type view-type
                                         :edit? true})))

(re-frame/reg-event-db
 ::edit-element
 (fn [db [_ view-type frame-id event-id edit?]]
   (assoc-in db (path/details frame-id) {:id event-id
                                         :type view-type
                                         :edit? edit?})))

(defn- editor-text->generic [reduce-fn old-value event]
  (let [content (edn/read-string (str "[" event "]"))
        valid? (and (seq content)
                    (even? (count (pop content)))
                    (every? (fn [token]
                              (if (vector? token)
                                (every? #(or (string? %)
                                             (keyword? %)) token)
                                (or (string? token)
                                    (keyword? token))))
                            (pop content))
                    (string? (peek content)))]
    (if valid?
      (let [attrs (partition-all 2 (pop content))
            text (peek content)]
        (try
          (reduce-fn attrs text)
          (catch js/Error e
            (warn "Failed to migrate" e)
            old-value)))
      old-value)))

(defn- editor-text->event-reduce-fn [attrs text]
  (reduce (fn [acc [key value]]
            (let [nkey (name key)
                  nskey (namespace key)]
              (cond (and (= key :prop/id)
                         (= value :auto))
                    (assoc acc :id (str (random-uuid)))
                    (and (= key :prop/id)
                         (string? value))
                    (assoc acc :id value)
                    (and (= key :prop/title)
                         (= value :journal-auto))
                    (update acc :properties conj [nkey "string" (str "journal from " (date-now-iso))])
                    (and (= key :date/creation)
                         (= value :auto))
                    (assoc acc :date (date-now-iso))
                    (and (= key :date/creation)
                         (string? value))
                    (assoc acc :date value)
                    (and (= nskey "prop")
                         (string? value))
                    (update acc :properties conj [nkey "string" value])
                    (and (= nskey "prop")
                         (int? value))
                    (update acc :properties conj [nkey "integer" value])
                    (and (= nskey "prop")
                         (float? value))
                    (update acc :properties conj [nkey "decimal" value])
                    (= nskey "context")
                    (update acc :contexts conj [nkey value])
                    (and (= nskey "datasource")
                         (= nkey "name"))
                    (assoc acc :datasource value)
                    :else
                    (do (error "Failed to migrate" nkey nskey value)
                        acc))))
          {:properties []
           :contexts []
           :notes text}
          attrs))

(defn- normalize-id [val]
  (-> val
      (str/lower-case)
      (str/replace #"\ " "-")
      (str/replace #"[^a-z0-9-]" "")))

(defn- editor-text->context-reduce-fn [attrs text]
  (let [attrs-map (into {} attrs)]
    (when (or (not (:context/type attrs-map))
              (not (:prop/name attrs-map)))
      (throw (ex-info "Not a valid context" {:context/type (:context/type attrs-map)
                                             :prop/name (:prop/name attrs-map)})))
    (reduce (fn [acc [key value]]
              (let [nkey (name key)
                    nskey (namespace key)]
                (cond (and (= key :prop/id)
                           (= value :auto))
                      (assoc acc :id (str "context-"
                                          (normalize-id (:context/type attrs-map))
                                          "-"
                                          (normalize-id (:prop/name attrs-map))))
                      (and (= key :prop/id)
                           (string? value))
                      (assoc acc :id value)
                      (and (= key :date/creation)
                           (= value :auto))
                      (assoc acc :date (date-now-iso))
                      (and (= key :date/creation)
                           (string? value))
                      (assoc acc :date value)
                      (and (= nskey "prop")
                           (string? value))
                      (update acc :properties conj [nkey "string" value])
                      (and (= nskey "prop")
                           (int? value))
                      (update acc :properties conj [nkey "integer" value])
                      (and (= nskey "prop")
                           (float? value))
                      (update acc :properties conj [nkey "decimal" value])
                      (= nskey "context")
                      (update acc :contexts conj [nkey value])
                      (and (= nskey "datasource")
                           (= nkey "name"))
                      (assoc acc :datasource value)
                      :else
                      (do (js/error.log "Failed to migrate" nkey nskey value)
                          acc))))
            {:properties []
             :contexts []
             :notes text}
            attrs)))

(defn- figure-view [frame-id width height figures]
  [:div {:style {:margin-left 25
                 :margin-right 25
                 :margin-top 15}}
   [:div {:style {:display :flex
                  :flex-direction :row
                  :border-bottom "1px solid grey"
                  :font-weight :bold
                  :margin-bottom 10}}
    [:div {:style {:padding 3
                   :width 35}}
     "ID"]
    [:div {:style {:padding 3
                   :width "21.5%"}}
     "date"]
    [:div {:style {:padding 3
                   :width "28.5%"}}
     "title"]
    [:div {:style {:padding 3
                   :width "21.5%"}}
     "events"]
    [:div {:style {:padding 3
                   :width "16.5%"}}
     "actions"]]
   [virtualized-list {:rows figures
                      :width width
                      :height height
                      :row-height 30
                      :row-renderer (fn [key index style row]
                                      (let [{:keys [id date title data]} row]
                                        (reagent/as-element
                                         [:div {:style (merge style
                                                              {:display :flex
                                                               :flex-direction :row})
                                                :on-double-click (fn [e]
                                                                   (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/open-figure frame-id row]))}
                                          [:div {:style {:padding 3
                                                         :width 35}}
                                           [tooltip {:text id}
                                            [:span (str "#" index)]]]
                                          [:div {:style {:padding 3
                                                         :width "20%"
                                                         :overflow :clip
                                                         :white-space :nowrap}}
                                           date]
                                          [:div {:style {:padding 3
                                                         :width "27%"
                                                         :overflow :clip
                                                         :white-space :nowrap}}
                                           title]
                                          [:div {:style {:padding 3
                                                         :width "20%"
                                                         :overflow :clip
                                                         :white-space :nowrap}}
                                           (count data)]
                                          [:div {:style {:padding 0
                                                         :width "15%"
                                                         :overflow :clip
                                                         :white-space :nowrap}}
                                           [button {:aria-label "View"
                                                    :start-icon :eye
                                                    :variant :tertiary
                                                    :on-click (fn []
                                                                (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/open-figure frame-id row]))}]]])))}]])

(re-frame/reg-event-fx
 ::open-neighborhood-graph
 (fn [{db :db} [_ frame-id element-type id mode]]
   (let [element (case element-type
                   :context (get @dummy-contexts id)
                   :event (get @dummy-events id))
         {:keys [contexts]} element
         contexts-num (count contexts)
         root-id (str (random-uuid))
         new-data (reduce (fn [acc [type name]]
                            (let [element-id (str (random-uuid))
                                  con-id (str (random-uuid))]
                              (assoc acc
                                     element-id {:id element-id
                                                 :type :context
                                                 :state-id (str (random-uuid))
                                                 :element-id (normalize-id (str "context-" type "-" name))
                                                 :color config/default-color-contexts-2
                                                 :pos (if (< (/ contexts-num 2)
                                                             (count acc))
                                                        (let [posf (Math/sin (* Math/PI (/ (mod (count acc)
                                                                                                (/ contexts-num 2))
                                                                                           (/ contexts-num 2))))]
                                                          [(* 1500 posf) (* 1500 (- 1 posf))])
                                                        (let [posf (Math/sin (* Math/PI (/ (mod (count acc)
                                                                                                (/ contexts-num 2))
                                                                                           (/ contexts-num 2))))]
                                                          [(* -1500 posf) (* -1500 (- 1 posf))]))}
                                     con-id {:id con-id
                                             :state-id (str (random-uuid))
                                             :type :connection
                                             :color config/default-color-connection
                                             :from root-id
                                             :to element-id})))
                          {root-id {:id root-id
                                    :type :context
                                    :state-id (str (random-uuid))
                                    :element-id id
                                    :color config/default-color-contexts
                                    :pos [0 0]}}
                          contexts)]
     {:db (if (= mode :add)
            (update-in db (path/canvas-content frame-id) merge new-data)
            (-> (assoc-in db (path/canvas-content-root frame-id) {:id (str (random-uuid))
                                                                  :title (str (:id element) " (NB)(unsaved)")
                                                                  :data new-data})
                (update-in (path/canvas-parked frame-id) conj (get-in db (path/canvas-content-root frame-id)))))
      :dispatch [:de.explorama.frontend.knowledge-editor.canvas/update frame-id]})))

(defn- event->viewer [element-type frame-id {:keys [properties contexts date notes id]}]
  (let [properties (into {} (map (fn [[name :as values]]
                                   [name values])
                                 properties))
        contexts (into {} (map (fn [[name :as values]]
                                 [name values])
                               contexts))
        td-style {:style {:padding 3}}
        htr-style {:style {:text-align :left
                           :padding 3}}
        tr-style {}]
    [:<>
     [:h3 (if (get properties "title")
            (get-in properties ["title" 2])
            "Text")]
     [journal/edit-viewer frame-id notes]
     [:h3 "Attributes"]
     [:div.fulltext__container {:style {:margin-top 15
                                        :margin-bottom 15}}
      (into
       [:table {:style {:border-collapse :collapse}}
        [:colgroup
         [:col {:style {:width (/ (- (/ width 2) 120) 2)}}]
         [:col {:style {:width (/ (- (/ width 2) 120) 2)}}]]
        [:tr {:style {:border-bottom "1px solid grey"}}
         [:th htr-style "Name"] [:th htr-style "Value"]]]
       (as-> [[:tr tr-style
               [:td td-style "id"]
               [:td td-style id]]
              [:tr tr-style
               [:td td-style "date"]
               [:td td-style (str/replace (or date "") #"T" " ")]]] $
         (reduce (fn [acc [_ [name value]]]
                   (conj acc
                         [:tr tr-style
                          [:td td-style name]
                          [:td td-style (str value)]]))
                 $
                 contexts)
         (reduce (fn [acc [_ [name _ value]]]
                   (conj acc
                         [:tr tr-style
                          [:td td-style name]
                          [:td td-style value]]))
                 $
                 (dissoc properties "title"))))]
     [:<>
      [:h3 "Generated figures"]
      [:div {:style {:display :flex
                     :flex-direction :row
                     :justify-content :space-between
                     :margin-top 15
                     :margin-bottom 15}}
       [:span {:style {:cursor :pointer
                       :font-weight :bold
                       :margin-right 10}}
        "Neighborhood"]
       [:div
        [button {:label "Add"
                 :size :small
                 :on-click (fn []
                             (re-frame/dispatch [::open-neighborhood-graph frame-id element-type id :add]))}]
        [button {:label "New Figure"
                 :size :small
                 :on-click (fn []
                             (re-frame/dispatch [::open-neighborhood-graph frame-id element-type id :create]))}]]]
      [:h3 "Related figures"]
      [figure-view frame-id
       (- (/ width 2) 160)
       (- height 180) (reagent/atom (->> (filter (fn [[_ {data :data}]]
                                                   (some (fn [[_ {{event-id :id} :event}]]
                                                           (= event-id id))
                                                         data))
                                                 @dummy-figures)
                                         (mapv second)))]]]))

(defn- generic->editor-text [{:keys [properties contexts date notes id datasource]}]
  (->> (as-> [(str ":prop/id \"" id "\"")
              (str ":date/creation \"" date "\"")
              (str ":datasource/name \"" datasource "\"")] $
         (reduce (fn [acc [name type value]]
                   (conj acc (str (keyword "prop" name) " " (condp = type
                                                              "string" (str "\"" value "\"")
                                                              "integer" value
                                                              "decimal" value))))
                 $ properties)
         (reduce (fn [acc [name value]]
                   (conj acc (str (keyword "context" name) " " (str "\"" value "\""))))
                 $ contexts)
         (conj $ (str "\"" notes "\"")))
       (str/join "\n")))

(defn- journal->event [text]
  (editor-text->event-reduce-fn (let [attributes (loop [matches (re-seq link-pattern text)
                                                        result []]
                                                   (if-let [match (first matches)]
                                                     (let [[type name]
                                                           (split-pattern match)]
                                                       (if (= type "title")
                                                         (recur (rest matches)
                                                                (conj result
                                                                      [(keyword "prop" type) name]))
                                                         (recur (rest matches)
                                                                (conj result
                                                                      [(keyword "context" type) name]))))
                                                     result))]

                                  (into (cond-> [[:prop/id :auto]
                                                 [:date/creation :auto]
                                                 [:context/type "journal"]
                                                 [:context/user "Sören Stöhrmann"]
                                                 [:datasource/name "Journal"]]
                                          (not (some (fn [[type]] (= :prop/title type)) attributes))
                                          (conj [:prop/title :journal-auto]))
                                        attributes))
                                text))

(re-frame/reg-event-db
 ::save-event
 (fn [db [_ frame-id element-type content]]
   (let [old-value (get (condp = element-type
                          :event @dummy-events
                          :journal @dummy-events
                          :context @dummy-contexts)
                        (get-in db (conj (path/details frame-id) :id)))
         new-event (condp = element-type
                     :event (editor-text->generic editor-text->event-reduce-fn old-value content)
                     :journal (journal->event content)
                     :context (editor-text->generic editor-text->context-reduce-fn old-value content))]
     (swap! (if (#{:event :journal} element-type)
              dummy-events
              dummy-contexts)
            assoc (:id new-event) new-event)
     (path/dissoc-in db (path/details frame-id)))))

(defn- editor [init-val frame-id]
  (reset! editor-value init-val)
  (fn [_ frame-id]
    [textarea {:value @editor-value
               :max-length 2056
               :extra-class "input--w100 h-full textarea-h-full"
               :on-change (fn [new-val]
                            (reset! editor-value new-val))}]))

(defn- details-dialog [frame-id]
  (let [{details-id :id
         element-type :type
         mode :mode
         edit? :edit?
         :as detail-dialog}
        @(re-frame/subscribe [::details-view frame-id])]
    (when detail-dialog
      [:<>
       [:div {:style {:top 0
                      :left 0
                      :position :absolute
                      :backgroundColor :grey
                      :opacity 0.5
                      :width (/ width 2)
                      :height height}}]
       [:div {:style {:position :absolute
                      :backgroundColor :white
                      :opacity 1
                      :top 50
                      :left 50
                      :width (- (/ width 2) 100)
                      :height (- height 100)}}
        [dialog-body
         [:<>
          [dialog-header
           [:<>
            [button {:label "Back"
                     :variant :back
                     :start-icon :previous
                     :size :normal
                     :on-click (fn []
                                 (re-frame/dispatch [::close-details frame-id])
                                 (reset! editor-value nil))}]
            (if edit?
              [button {:label "view"
                       :on-click (fn []
                                   (re-frame/dispatch [::details-edit frame-id false]))}]
              [button {:label "edit"
                       :on-click (fn []
                                   (re-frame/dispatch [::details-edit frame-id true]))}])
            [button {:label "Save"
                     :on-click (fn []
                                 (re-frame/dispatch [::save-event frame-id element-type @editor-value])
                                 (reset! editor-value nil))}]]]
          (cond
            (and edit? (not= element-type :journal)) [:div {:style {:width (- (/ width 2) 120)
                                                                    :height (- height 170)}}
                                                      [editor (cond @editor-value @editor-value
                                                                    (and (= mode :create)
                                                                         (= element-type :event))
                                                                    default-event
                                                                    (and (= mode :create)
                                                                         (= element-type :context))
                                                                    default-context
                                                                    :else
                                                                    (generic->editor-text (get (if (= element-type :event)
                                                                                                 @dummy-events
                                                                                                 @dummy-contexts)
                                                                                               details-id)))
                                                       frame-id]]
            (and (not edit?) (not= element-type :journal)) [event->viewer
                                                            element-type
                                                            frame-id
                                                            (if (= element-type :event)
                                                              (editor-text->generic editor-text->event-reduce-fn
                                                                                    (get @dummy-events details-id)
                                                                                    @editor-value)
                                                              (editor-text->generic editor-text->context-reduce-fn
                                                                                    (get @dummy-contexts details-id)
                                                                                    @editor-value))]
            (and edit? (= element-type :journal)) [:div {:style {:width (- (/ width 2) 120)
                                                                 :height (- height 170)}}
                                                   [journal/editor frame-id
                                                    (or @editor-value
                                                        default-journal)
                                                    editor-value]]
            (and (not edit?) (= element-type :journal)) [:div {:style {:width (- (/ width 2) 120)
                                                                       :height (- height 170)}}
                                                         [journal/edit-viewer frame-id (or @editor-value
                                                                                           (get @dummy-events details-id))]])]]]])))

(re-frame/reg-event-fx
 ::persist-figure
 (fn [{db :db} [_ frame-id {:keys [title payload]}]]
   (let [id (str (random-uuid))]
     (swap! dummy-figures assoc id {:id id
                                    :title title
                                    :date (date-now-iso)
                                    :data payload}))
   {:db (-> (path/dissoc-in db (path/canvas-dialog frame-id))
            (assoc-in (path/canvas-content-title frame-id) title))}))

(defn- figure-save-dialog [frame-id]
  (let [title (reagent/atom nil)]
    (fn [frame-id]
      (let [{payload :payload} @(re-frame/subscribe [::canvas-dialog frame-id])]
        [:<>
         [dialog-header
          [:<>
           [button {:label "Back"
                    :variant :back
                    :start-icon :previous
                    :size :normal
                    :on-click (fn []
                                (reset! title nil)
                                (re-frame/dispatch [::close-canvas-dialog frame-id]))}]
           [button {:label "Save"
                    :on-click (fn []
                                (re-frame/dispatch [::persist-figure frame-id
                                                    {:title @title
                                                     :payload payload}])
                                (reset! title nil))}]]]
         [input-field {:value title
                       :label "Title"
                       :aria-label "Title"
                       :on-change #(reset! title %)}]]))))

(defn- investigate-element [element-type frame-id data]
  (let [title (reagent/atom nil)
        color (reagent/atom nil)]
    (fn [element-type frame-id data]
      (let [{element-id :element-id
             id :id
             current-color :color}
            @(re-frame/subscribe [::canvas-dialog frame-id])]
        [:<>
         [dialog-header
          [:<>
           [button {:label "Back"
                    :variant :back
                    :start-icon :previous
                    :size :normal
                    :on-click (fn []
                                (reset! color nil)
                                (reset! title nil)
                                (re-frame/dispatch [::close-canvas-dialog frame-id]))}]
           [input-field {:type :color
                         :extra-class "input--w2"
                         :aria-label "Color picker"
                         :value (or @color current-color)
                         :on-change (fn [new-color]
                                      (reset! color new-color))
                         :on-blur (fn []
                                    (when-not (= @color current-color)
                                      (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/update-state-id
                                                          id
                                                          frame-id
                                                          (str (random-uuid))
                                                          {:color @color}])))}]]]
         [event->viewer
          element-type
          frame-id
          (get @data element-id)]]))))

(defn- investigate-connection [frame-id]
  (let [label (reagent/atom nil)
        color (reagent/atom nil)]
    (fn [frame-id]
      (let [{current-label :label
             id :id
             current-color :color}
            @(re-frame/subscribe [::canvas-dialog frame-id])]
        [:<>
         [dialog-header
          [:<>
           [button {:label "Back"
                    :variant :back
                    :start-icon :previous
                    :size :normal
                    :on-click (fn []
                                (reset! color nil)
                                (reset! label nil)
                                (re-frame/dispatch [::close-canvas-dialog frame-id]))}]
           [input-field {:type :color
                         :extra-class "input--w2"
                         :aria-label "Color picker"
                         :value (or @color current-color)
                         :on-change (fn [new-color]
                                      (reset! color new-color))
                         :on-blur (fn []
                                    (when-not (= @color current-color)
                                      (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/update-state-id
                                                          id
                                                          frame-id
                                                          (str (random-uuid))
                                                          {:color @color}])))}]]]
         [input-field {:extra-class "input--w100"
                       :aria-label "Color picker"
                       :label "Label"
                       :value (or @label current-label)
                       :on-change (fn [new-label]
                                    (reset! label new-label))
                       :on-blur (fn []
                                  (when-not (= @label current-label)
                                    (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/update-state-id
                                                        id
                                                        frame-id
                                                        (str (random-uuid))
                                                        {:label @label}])))}]]))))

(defn- canvas-dialog [frame-id]
  (let [{dialog-type :type
         element-type :element-type
         :as dialog?}
        @(re-frame/subscribe [::canvas-dialog frame-id])]
    (when dialog?
      [:<>
       [:div {:style {:top 0
                      :left (/ width 2)
                      :position :absolute
                      :backgroundColor :grey
                      :opacity 0.5
                      :width (/ width 2)
                      :height height}}]
       [:div {:style {:position :absolute
                      :backgroundColor :white
                      :opacity 1
                      :top 50
                      :left (+ (/ width 2) 50)
                      :width (- (/ width 2) 100)
                      :height (- height 100)}}
        [dialog-body
         (cond
           (= dialog-type :save-dialog) [figure-save-dialog frame-id]
           (and (= dialog-type :investigate-figure-element)
                (= element-type :event))
           [investigate-element element-type frame-id dummy-events]
           (and (= dialog-type :investigate-figure-element)
                (= element-type :context))
           [investigate-element element-type frame-id dummy-contexts]
           (= dialog-type :investigate-figure-connection) [investigate-connection frame-id])]]])))

(defn- element-view [frame-id view-type]
  [:div {:style {:margin-left 25
                 :margin-right 25
                 :margin-top 15}}
   [:div {:style {:display :flex
                  :flex-direction :row
                  :border-bottom "1px solid grey"
                  :font-weight :bold
                  :margin-bottom 10}}
    [:div {:style {:padding 3
                   :width 35}}
     "ID"]
    [:div {:style {:padding 3
                   :width "21.5%"}}
     "date"]
    [:div {:style {:padding 3
                   :width "28.5%"}}
     (if (= :event view-type)
       "title"
       "name")]
    [:div {:style {:padding 3
                   :width "21.5%"}}
     "type"]
    [:div {:style {:padding 3
                   :width "15%"}}
     "actions"]]
   [virtualized-list {:rows (vec (vals (if (= :event view-type)
                                         (if (nil? @filtered-events)
                                           @dummy-events
                                           @filtered-events)
                                         (if (nil? @filtered-contexts)
                                           @dummy-contexts
                                           @filtered-contexts))))
                      :width (/ width 2)
                      :height (- height 180)
                      :row-height 30
                      :row-renderer (fn [key index style row]
                                      (let [{:keys [id date]} row
                                            style-cell {:style {:padding 3
                                                                :width "20%"
                                                                :overflow :clip
                                                                :white-space :nowrap}}
                                            style-cell-buttons {:style {:padding 0
                                                                        :width "15%"
                                                                        :overflow :clip
                                                                        :white-space :nowrap}}
                                            style-cell-title {:style {:padding 3
                                                                      :width "27%"
                                                                      :overflow :clip
                                                                      :white-space :nowrap}}]
                                        (reagent/as-element
                                         [:div {:style (merge style
                                                              {:display :flex
                                                               :flex-direction :row})
                                                :on-double-click (fn [e]
                                                                   (re-frame/dispatch [::edit-element view-type frame-id id false]))}
                                          [:div {:style {:padding 3
                                                         :width 35}}
                                           [tooltip {:text id}
                                            [:span (str "#" index)]]]
                                          [:div style-cell
                                           [tooltip {:text date}
                                            [:span date]]]
                                          (if-let [title (some (fn [[name _ title]]
                                                                 (when (or (= name "title")
                                                                           (= name "name"))
                                                                   title))
                                                               (get row :properties))]
                                            [:div style-cell-title
                                             [tooltip {:text title}
                                              [:span title]]]
                                            [:div style-cell
                                             ""])
                                          [:div style-cell
                                           (some (fn [[name arch-type]]
                                                   (when (= name "type")
                                                     [tooltip {:text arch-type}
                                                      [:span arch-type]]))
                                                 (get row :contexts))]
                                          [:div style-cell-buttons
                                           [button {:aria-label "View"
                                                    :start-icon :eye
                                                    :variant :tertiary
                                                    :on-click (fn []
                                                                (re-frame/dispatch [::edit-element view-type frame-id id false]))}]
                                           [button {:start-icon :edit
                                                    :aria-label "edit"
                                                    :variant :tertiary
                                                    :on-click (fn []
                                                                (re-frame/dispatch [::edit-element view-type frame-id id true]))}]
                                           [button {:start-icon :copy
                                                    :aria-label "add"
                                                    :variant :tertiary
                                                    :on-click (fn []
                                                                (if (= :event view-type)
                                                                  (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/add-event frame-id row [0 0]])
                                                                  (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/add-context frame-id row [0 0]])))}]]])))}]])

(defn main-view [frame-id]
  (let [input-field-val (reagent/atom "")
        active-tab (reagent/atom "tab-events")]
    (fn []
      [:div {:style {:width (/ width 2)
                     :height height
                     :overflow :auto
                     :border-right "1px solid grey"}}
       [:div {:style {:padding 5
                      :padding-bottom 15
                      :border-bottom "1px solid grey"
                      :display :flex
                      :flex-direction :row
                      :justify-content :space-between
                      :margin-bottom 15
                      :width (/ width 2)}}
        [button-group {:items [{:label "Events"
                                :id "tab-events"
                                :on-click (fn [v]
                                            (reset! active-tab "tab-events"))}
                               {:label "Contexts"
                                :id "tab-contexts"
                                :on-click (fn [v]
                                            (reset! active-tab "tab-contexts"))}
                               {:label "Figure"
                                :id "tab-figure"
                                :on-click (fn [v]
                                            (reset! active-tab "tab-figure"))}]
                       :active-item active-tab}]
        (when (#{"tab-events" "tab-contexts"} @active-tab)
          [button {:label (condp = @active-tab
                            "tab-events" "Add Event"
                            "tab-contexts" "Add Context")
                   :on-click (fn []
                               (condp = @active-tab
                                 "tab-events" (re-frame/dispatch [::create-event :event frame-id])
                                 "tab-contexts" (re-frame/dispatch [::create-event :context frame-id])))}])
        [button {:label "Add Journal" :on-click (fn []
                                                  (re-frame/dispatch [::create-journal :journal frame-id]))}]]
       [:div {:style {:margin-left 15
                      :margin-right 15}}
        [input-field {:value input-field-val
                      :aria-label "Search field"
                      :on-change #(let [filtered-values (condp = @active-tab
                                                          "tab-events" filtered-events
                                                          "tab-contexts" filtered-contexts
                                                          "tab-figure" filtered-figures)
                                        all-values (condp = @active-tab
                                                     "tab-events" dummy-events
                                                     "tab-contexts" dummy-contexts
                                                     "tab-figure" dummy-figures)]
                                    (reset! input-field-val %)
                                    (if (str/blank? %)
                                      (reset! filtered-values nil)
                                      (reset! filtered-values
                                              (let [value (str/lower-case %)]
                                                (->> (filter (cond (and (str/starts-with? value "//prop/")
                                                                        (or (= "tab-events" @active-tab)
                                                                            (= "tab-contexts" @active-tab)))
                                                                   (let [r (str/split (subs value 7) "/")]
                                                                     (cond (= 2 (count r))
                                                                           (fn [[_ {:keys [properties]}]]
                                                                             (some (fn [[name _ type]]
                                                                                     (and (= (first r) (str/lower-case name))
                                                                                          (str/includes? (str/lower-case type)
                                                                                                         (second r))))
                                                                                   properties))
                                                                           (= 1 (count r))
                                                                           (fn [[_ {:keys [properties]}]]
                                                                             (some (fn [[name]]
                                                                                     (str/includes? (str/lower-case name)
                                                                                                    (first r)))
                                                                                   properties))
                                                                           :else
                                                                           (fn [[_ {:keys [properties]}]]
                                                                             (seq properties))))
                                                                   (and (str/starts-with? value "//context/")
                                                                        (or (= "tab-events" @active-tab)
                                                                            (= "tab-contexts" @active-tab)))
                                                                   (let [r (str/split (subs value 10) "/")]
                                                                     (cond (= 2 (count r))
                                                                           (fn [[_ {:keys [contexts]}]]
                                                                             (some (fn [[type name]]
                                                                                     (and (= (first r) (str/lower-case type))
                                                                                          (str/includes? (str/lower-case name)
                                                                                                         (second r))))
                                                                                   contexts))
                                                                           (= 1 (count r))
                                                                           (fn [[_ {:keys [contexts]}]]
                                                                             (some (fn [[type]]
                                                                                     (str/includes? (str/lower-case type)
                                                                                                    (first r)))
                                                                                   contexts))
                                                                           :else
                                                                           (fn [[_ {:keys [contexts]}]]
                                                                             (seq contexts))))
                                                                   :else
                                                                   (condp = @active-tab
                                                                     "tab-events" (fn [[_ {:keys [id date datasource]}]]
                                                                                    (or (str/includes? (str/lower-case datasource)
                                                                                                       value)
                                                                                        (str/includes? (str/lower-case date) value)
                                                                                        (str/includes? id value)))
                                                                     "tab-contexts" (fn [[_ {:keys [datasource id date]}]]
                                                                                      (or (str/includes? (str/lower-case datasource) value)
                                                                                          (str/includes? (str/lower-case date) value)
                                                                                          (str/includes? id value)))
                                                                     "tab-figure" (fn [[_ {:keys [title id date]}]]
                                                                                    (or (str/includes? (str/lower-case title) value)
                                                                                        (str/includes? (str/lower-case date) value)
                                                                                        (str/includes? id value)))))
                                                             @all-values)
                                                     (into {}))))))}]]
       (condp = @active-tab
         "tab-events" [element-view frame-id :event]
         "tab-contexts" [element-view frame-id :context]
         "tab-figure" [figure-view frame-id (/ width 2) (- height 180) (vec (vals (if (nil? @filtered-figures)
                                                                                    @dummy-figures
                                                                                    @filtered-figures)))])
       [details-dialog frame-id]
       [canvas-dialog frame-id]])))
