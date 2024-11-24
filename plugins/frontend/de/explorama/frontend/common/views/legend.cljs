(ns de.explorama.frontend.common.views.legend
  (:require ["react-beautiful-dnd" :refer [DragDropContext Droppable Draggable]]
            [clojure.string :refer [lower-case]]
            [de.explorama.shared.data-format.aggregations :as dfl-agg]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button input-field section select]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [hint
                                                                        icon]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.shared.common.interval.validation :as iv]
            [goog.string :as gstr]
            [re-frame.core :as re-frame]
            [react-dom :as react-dom]
            [reagent.core :as r]))

(def ^:private drag-drop-context (r/adapt-react-class DragDropContext))
(def ^:private droppable (r/adapt-react-class Droppable))
(def ^:private draggable (r/adapt-react-class Draggable))

(def number-default-values [[0 2]
                            [2 8]
                            [8 20]
                            [20 50]
                            [50 100]
                            [100 200]
                            [200 300]
                            [300 400]
                            [400 500]
                            [500 1000]
                            [1000 10000]
                            [10000 100000]
                            [100000 500000]
                            [500000 1000000]
                            [1000000 100000000]])

(def string-default-values ["" "" "" "" ["*"]
                            "" "" "" "" ""
                            "" "" "" "" ""])

(def number-types #{:number "number" :integer "integer" :decimal "decimal"})
(def string-types #{:string "string"})
(def date-types #{:date :year :month :day})
(def location-types #{:location "location"})

(defn setting-section [{:keys [label collapsible? default-open?]
                        :or {collapsible? true
                             default-open? true}}
                       & childs]
  (-> (if collapsible?
        [section {:label label
                  :default-open? default-open?}]
        [:<>
         [:h3 label]])
      (conj (apply conj
                   (if collapsible?
                     [:div.section__collapsible__content]
                     [:<>])
                   childs))))

(defn type->display-type [type]
  (cond
    (number-types type) "number"
    (string-types type) "text"
    (date-types type) "date"
    (location-types type) "location"
    :else nil))

(defn naming [{:keys [layout on-change-layout translate]}]
  [:div
   [input-field
    {:extra-class "input--w100"
     :value (:name (val-or-deref layout))
     :label @(translate :legend-layout-name)
     :max-length 30
     :on-change #(on-change-layout :name %)}]])

(defn- gen-layout-title [name colors]
  [:div.flex.justify-between.align-items-center
   {:title name}
   [:label.input-w-64.text-truncate name]
   (reduce
    (fn [res [_ color]]
      (conj res
            [:div.select__colorswatch
             {:style {:background-color color}}]))
    [:div.flex.align-items-center.gap-2]
    colors)])

(defn- vec-remove [coll i]
  (vec (concat (subvec coll 0 i)
               (subvec coll (inc i)))))

(defn- vec-add
  [coll i elem]
  (concat (subvec coll 0 i)
          [elem] (subvec coll i)))

(defn- vec-move
  [coll current-idx target-idx]
  (if (= current-idx target-idx)
    coll
    (into [] (vec-add (vec-remove coll current-idx)
                      target-idx (nth coll current-idx)))))

(defn- recalc-color-idx [colors]
  (reduce (fn [acc [new-idx [_ v]]]
            (assoc acc
                   (keyword (str new-idx))
                   (or v "#000000")))
          {}
          (map-indexed vector
                       (sort-by (fn [[k]]
                                  (js/parseInt (name k)))
                                colors))))

(defn- remove-color-scale-ref [on-change-layout]
  (on-change-layout #(update % :color-scheme dissoc :id :name :identity :color-scale-numbers)))

(defn- remove-color-assign [on-change-layout idx]
  (remove-color-scale-ref on-change-layout)
  (on-change-layout (fn [o]
                      (-> o
                          (update :value-assigned vec-remove idx)
                          (update-in [:color-scheme :colors] dissoc (keyword (str idx)))
                          (update-in [:color-scheme :colors] recalc-color-idx)))))

(defn- random-hex-color []
  (let [rnd (rand-int 16rEEEEEE)] ;;prevent white-like colors
    (if (> rnd 16r222222) ;;prevent black-like colors
      (str "#"
           (.toString rnd 16))
      (random-hex-color))))

(defn add-color-assign [on-change-layout is-range? & [color]]
  (remove-color-scale-ref on-change-layout)
  (on-change-layout (fn [o]
                      (-> o
                          (update :value-assigned #(conj (or % [])
                                                         (or
                                                          (when (and is-range?
                                                                     (count number-default-values))
                                                            (get number-default-values (count %)))
                                                          (if is-range?
                                                            (or (last %) [0 1])
                                                            []))))
                          (update-in [:color-scheme :colors] #(do
                                                                (assoc %
                                                                       (keyword (str (count %)))
                                                                       (or color (random-hex-color)))))))))

(defn init-color-assign [on-change-layout is-range? colors]
  (on-change-layout (fn [o]
                      (dissoc o :value-assigned :color-scheme)))
  (doseq [color colors]
    (add-color-assign on-change-layout is-range? color)))

(defn- init-value-assigned [layout new-attribute-type]
  (update layout
          :value-assigned
          #(vec (take (count %)
                      (if (string-types new-attribute-type)
                        string-default-values
                        number-default-values)))))

(defn- color-assign [idx color value-assigned on-change-layout selectable-options read-only? translate-multi]
  (let [{:keys [legend-all-matching legend-current-data legend-all-data color-picker]}
        @(translate-multi
          :color-picker
          :legend-all-matching
          :legend-current-data
          :legend-all-data)
        assigned-values (get value-assigned idx "")
        in-frame-options (set (:frame selectable-options))
        star-in-use? (some (fn [vals] (some (fn [val] (= "*" val)) vals))
                           value-assigned)]
    [:div.color__assignments__row {:style {:display "block"}}
     [:div.align-items-center.flex.gap-8
      [:div.drag-handle
       [icon {:icon :drag-indicator}]]
      [input-field {:type :color
                    :extra-class "color__block input--w2"
                    :aria-label color-picker
                    :value color
                    :disabled? (boolean read-only?)
                    :on-change (fn [new-color]
                                 (remove-color-scale-ref on-change-layout)
                                 (on-change-layout [:color-scheme :colors (keyword (str idx))]
                                                   new-color))}]
      [select
       {:is-clearable? true
        :disabled? (boolean read-only?)
        :on-change #(on-change-layout [:value-assigned idx]
                                      (mapv :label %))

        :options [{:label legend-all-matching
                   :options (if-not star-in-use?
                              [{:label "*"}]
                              [])}
                  {:label legend-current-data
                   :options (mapv (fn [v] {:label v})
                                  (:frame selectable-options))}
                  {:label legend-all-data
                   :options (into []
                                  (comp (filter (fn [v] (not (in-frame-options v))))
                                        (map (fn [v] {:label v})))
                                  (:global selectable-options))}]
        :label-params {:style {:display :inline-block}}
        :values (mapv (fn [attr]
                        {:label attr})
                      assigned-values)
        :extra-class "w-full"
        :group-selectable? false
        :check-key :label
        :is-grouped? true
        :is-multi? true}]
      [button {:start-icon :close
               :disabled? (boolean read-only?)
               :on-click (partial remove-color-assign on-change-layout idx)
               :variant :secondary}]]]))



(defn- color-range [idx color value-assigned on-change-layout read-only? translate-multi]
  (let [interval (get value-assigned idx [])
        illegal-interval? (iv/illegal-interval? interval)
        [from to] interval
        overlaps (iv/interval-overlaps idx value-assigned)
        from-invalid? (boolean (some (fn [[info]] (#{:contains-interval :contained-in :start-overlaps-with :duplicate} info)) overlaps))
        to-invalid? (boolean (some (fn [[info]] (#{:contains-interval :contained-in :end-overlaps-with :duplicate} info)) overlaps))
        {:keys [legend-negative-infinity legend-infinity color-picker close range-start range-end]}
        @(translate-multi :legend-negative-infinity :legend-infinity :color-picker :close :range-start :range-end)]
    [:div.color__assignments__row {:style {:display "block"}}
     [:div.align-items-center.flex.gap-8
      [:div.drag-handle
       [icon {:icon :drag-indicator}]]
      [input-field {:type :color
                    :disabled? (boolean read-only?)
                    :extra-class "color__block input--w2"
                    :aria-label color-picker
                    :value color
                    :on-change (fn [new-color]
                                 (remove-color-scale-ref on-change-layout)
                                 (on-change-layout [:color-scheme :colors (keyword (str idx))]
                                                   new-color))}]
      (let [display-from (if (= from ##-Inf) "" from)]
        [input-field
         (cond-> {:disabled? (boolean read-only?)
                  :value display-from
                  :invalid? (or from-invalid? illegal-interval?)
                  :no-error-hiding? true
                  :on-clear (fn [_]
                              (on-change-layout [:value-assigned idx 0] ##-Inf))
                  :aria-label range-start
                  :type :number
                  :on-change (fn [input-val]
                               (on-change-layout [:value-assigned idx 0] (or input-val ##-Inf)))}
           (= display-from "")
           (assoc :placeholder legend-negative-infinity))])
      [icon {:icon :minus}]
      (let [display-to (if (= to ##Inf) "" to)]
        [input-field
         (cond-> {:disabled? (boolean read-only?)
                  :value display-to
                  :invalid? (or to-invalid? illegal-interval?)
                  :no-error-hiding? true
                  :on-clear (fn [_]
                              (on-change-layout [:value-assigned idx 1] ##Inf))
                  :aria-label range-end
                  :type :number
                  :on-change (fn [input-val]
                               (on-change-layout [:value-assigned idx 1] (or input-val ##Inf)))}
           (= display-to "")
           (assoc :placeholder legend-infinity))])
      [button {:start-icon :close
               :aria-label close
               :disabled? (boolean read-only?)
               :on-click (partial remove-color-assign on-change-layout idx)
               :variant :secondary}]]]))

(defn- drag-container [provided item indexed-colors is-range? value-assigned on-change-layout selectable-options read-only? translate-multi]
  [:div.drag-container
   (merge
    (js->clj (aget provided "draggableProps"))
    (js->clj (aget provided "dragHandleProps"))
    {:ref (aget provided "innerRef"),
     :style
     (aget provided "draggableProps" "style")})
   (let [idx (aget item "content")
         [_ color] (get indexed-colors idx)]
     (with-meta
       (if is-range?
         [color-range idx color value-assigned on-change-layout read-only? translate-multi]
         [color-assign idx color value-assigned on-change-layout selectable-options read-only? translate-multi])
       {:key (str ::color-range "-" idx)}))])

(defn- create-draggable-fn [item is-range? indexed-colors value-assigned on-change-layout selectable-options read-only? translate-multi]
  (fn [provided snapshot]
    (if (aget snapshot "isDragging")
      (react-dom/createPortal (r/as-element
                               [drag-container provided item indexed-colors is-range? value-assigned on-change-layout
                                selectable-options read-only? translate-multi])
                              js/document.body)
      (r/as-element [drag-container provided item indexed-colors is-range? value-assigned on-change-layout selectable-options read-only? translate-multi]))))

(defn droppable-rows [elems-list is-range? indexed-colors value-assigned on-change-layout selectable-options read-only? translate-multi]
  [droppable
   {:droppable-id "droppable"}
   (fn
     [provided _]
     (r/as-element
      [:div
       {:ref (aget provided "innerRef")}
       (map-indexed
        (fn
          [index item]
          [draggable
           {:key (aget item "id"), :draggable-id (aget item "id"), :index index}
           (create-draggable-fn item is-range? indexed-colors value-assigned on-change-layout selectable-options read-only? translate-multi)])
        elems-list)
       (aget provided "placeholder")]))])

(defn color-list [is-range? value-assigned on-change-layout selectable-options read-only? translate-multi colors]

  (let [{:keys [inclusive exclusive]}
        @(translate-multi :inclusive
                          :exclusive)
        sorted-colors (sort-by (fn [[k]] (js/parseInt (name k)))
                               colors)
        indexed-colors (into {} (map-indexed vector sorted-colors))
        elems-list (clj->js (map (fn [idx]  {:id (str "item-" idx ""),  :content idx})
                                 (keys indexed-colors)))]
    [:div.color__assignments
     (when is-range?
       [:div.color__assignments__row
        [:div.color__value
         [:div.color__assignment__header__left.input--w5
          inclusive]
         [icon {:icon       :minus
                :color      :gray
                :brightness 5}]
         [:div.color__assignment__header__right.input--w5
          exclusive]]])
     [drag-drop-context
      {:onDragEnd
       (fn [result]
         (when (aget result "destination")
           (let [result (js->clj result)
                 source-idx (get-in result ["source" "index"])
                 target-idx (get-in result ["destination" "index"])]
             (on-change-layout (fn [o]
                                 (let [current-colors (into [] (vals (sort-by (fn [[k]] (js/parseInt (name k)))
                                                                              (get-in o [:color-scheme :colors]))))
                                       reordered-colors (vec-move current-colors source-idx target-idx)]
                                   (-> o
                                       (assoc :value-assigned (vec-move (:value-assigned o) source-idx target-idx))
                                       (assoc-in [:color-scheme :colors] (zipmap (map #(-> % str keyword)
                                                                                      (range (count reordered-colors)))
                                                                                 reordered-colors)))))))))}
      (droppable-rows elems-list is-range? indexed-colors value-assigned on-change-layout selectable-options read-only? translate-multi)]]))

(defn- color-ranges [{:keys [layout on-change-layout ac-vals selectable-color-limit translate translate-multi error-status-callback]}]
  (let [{:keys [attribute-type read-only? attributes color-scheme value-assigned]} (val-or-deref layout)
        {:keys [colors]} color-scheme
        {:keys [legend-add-color
                legend-interval-overlap-error
                legend-illegal-interval-error
                legend-missing-value-error
                legend-interval-single-gap-warning
                legend-interval-gaps-warning]}
        @(translate-multi :legend-add-color
                          :legend-interval-overlap-error
                          :legend-illegal-interval-error
                          :legend-missing-value-error
                          :legend-interval-single-gap-warning
                          :legend-interval-gaps-warning)
        is-range? (number-types attribute-type)
        already-used-values (set (flatten value-assigned))
        selectable-options (when (and ac-vals (not is-range?))
                             (val-or-deref (ac-vals attributes already-used-values)))
        [has-interval-errors?
         has-illegal-interval?
         missing-value?] (if is-range?
                           [(iv/interval-overlaps? value-assigned)
                            (some iv/illegal-interval? value-assigned)
                            nil]
                           [nil nil (every? empty? value-assigned)])
        gaps (when (and is-range?
                        (not (or has-interval-errors?
                                 has-illegal-interval?)))
               (iv/check-for-gaps value-assigned))]

    (when error-status-callback
      (error-status-callback (or has-interval-errors?
                                 has-illegal-interval?
                                 missing-value?))
      (cond-> (color-list is-range? value-assigned on-change-layout selectable-options read-only? translate-multi colors)
        has-illegal-interval?
        (conj
         [:div.color__assignments__row.empty
          [:span.color__block
           [icon {:icon :error
                  :color :gray}]]
          legend-illegal-interval-error])
        has-interval-errors?
        (conj
         [:div.color__assignments__row.empty
          [:span.color__block
           [icon {:icon :error
                  :color :gray}]]
          legend-interval-overlap-error])
        missing-value?
        (conj
         [:div.color__assignments__row.empty
          [:span.color__block
           [icon {:icon :error
                  :color :gray}]]
          legend-missing-value-error])
        (not-empty gaps)
        (conj [:div
               [hint {:variant :warning, :content (if (= 1 (count gaps))
                                                    legend-interval-single-gap-warning
                                                    legend-interval-gaps-warning)}]])
        (and (< (count colors)
                selectable-color-limit)
             (not read-only?))
        (conj
         [:div.color__assignments__row.empty
          {:on-click #(when attribute-type
                        (add-color-assign on-change-layout (and attributes is-range?)))}
          [:span.color__block
           [icon {:icon :plus
                  :color :gray}]]
          legend-add-color])))))

(defn- change-color-scale [on-change-layout new-color-scale]
  (on-change-layout
   (fn [o]
     (let [is-number-type? (number-types (:attribute-type o))]
       (-> o
           (assoc :color-scheme new-color-scale)
           (update :value-assigned
                   (fn [old]
                     (let [ncount (count (get new-color-scale :colors))
                           ocount (count old)]
                       (if (< ncount ocount)
                         (subvec old 0 ncount)
                         (apply conj (or old [])
                                (map (fn [i]
                                       (if is-number-type?
                                         (or (get number-default-values i)
                                             (last old)
                                             (vector 0 1))
                                         (get string-default-values i)))
                                     (range ocount ncount))))))))))))



(defn- change-attributes [on-change-layout new-options request-characteristics]
  (let [{new-attribute-type :type new-attribute :attr-name} (:value (last new-options))]
    (on-change-layout
     (fn [o]
       (let [new (cond
                   (> (count (:attributes o))
                      (count new-options))
                   (assoc o :attributes (mapv #(get-in % [:value :attr-name] (:value %))
                                              new-options))
                   (= (name new-attribute-type)
                      (:attribute-type o))
                   (update o :attributes conj new-attribute)
                   :else
                   (-> o
                       (assoc :attributes (if new-attribute
                                            [new-attribute]
                                            []))
                       (assoc :attribute-type (name new-attribute-type))
                       (init-value-assigned new-attribute-type)))]
         (request-characteristics (:attributes new))
         new)))))

(defn- tag-box [attr-type]
  (let [ico (cond
              (number-types attr-type) :type-number
              (date-types attr-type) :type-date
              (location-types attr-type) :type-number
              :else :type-string)
        bg-class (cond
                   (number-types attr-type) "icon__color__datatype-1"
                   (date-types attr-type) "icon__color__datatype-3"
                   (location-types attr-type) "icon__color__datatype-4"
                   :else "icon__color__datatype-2")]
    [icon {:icon ico
           :extra-class bg-class}]))

(defn attr->display-name [attr labels]
  (or (get labels attr)
      (when-let [agg-label (get-in dfl-agg/descs [attr :label])]
        @(re-frame/subscribe [::i18n/translate agg-label]))
      attr))

(defn gen-attr-options [global-ac-attribute-types frame-ac-attribute-types add-condition-fn translate-multi extra-attribtues]
  (let [{:keys [legend-current-data legend-all-data legend-general-attributes]}
        @(translate-multi
          :legend-current-data
          :legend-all-data
          :legend-general-attributes)
        labels @(fi/call-api [:i18n :get-labels-sub])]
    (reduce (fn [acc [attr-name {{:keys [type] :as std} :std
                                 general-group? :general-group?} :as attr-desc]]
              (let [add-condition (add-condition-fn attr-name std)
                    attr-label (attr->display-name attr-name labels)
                    type-str (type->display-type type)
                    tooltip-type (when type-str
                                   (str "[" type-str "] "))
                    attr-label-comp [:div.select-value-tagged.tags-right
                                     [:label attr-label]
                                     (when type-str
                                       [tag-box type])]
                    option-desc {:label attr-label-comp
                                 :tooltip (str tooltip-type attr-label)
                                 :value (assoc std
                                               :attr-name attr-name
                                               :attr-label attr-label)}]

                (cond-> acc
                  (and add-condition
                       general-group?)
                  (update-in [0 :options] conj (assoc-in option-desc [:value :context] :general))
                  (and add-condition
                       (and (get frame-ac-attribute-types attr-name)
                            (not general-group?)
                            (seq frame-ac-attribute-types)))
                  (update-in [1 :options] conj (assoc-in option-desc [:value :context] :frame))
                  (and add-condition
                       (and (not (get frame-ac-attribute-types attr-name))
                            (not general-group?)))
                  (update-in [2 :options] conj (assoc-in option-desc [:value :context] :global)))))
            [{:label legend-general-attributes
              :tooltip legend-general-attributes
              :options []}
             {:label legend-current-data
              :tooltip legend-current-data
              :options []}
             {:label legend-all-data
              :tooltip legend-all-data
              :options []}]
            (merge frame-ac-attribute-types global-ac-attribute-types extra-attribtues))))

(defn sort-options [attr-options]
  (-> attr-options
      (update-in [0 :options] (fn [opts]
                                (vec (sort-by (fn [{{:keys [attr-label]} :value}]
                                                (lower-case attr-label))
                                              opts))))
      (update-in [1 :options] (fn [opts]
                                (vec (sort-by (fn [{{:keys [attr-label]} :value}]
                                                (lower-case attr-label))
                                              opts))))
      (update-in [2 :options] (fn [opts]
                                (vec (sort-by (fn [{{:keys [attr-label]} :value}]
                                                (lower-case attr-label))
                                              opts))))))

(defn- build-attribute-options [global-ac-attribute-types frame-ac-attribute-types attributes translate-multi extra-attribtues]
  (let [attributes (set attributes)
        nodes-blacklist #{"Notes"}
        add-condition-fn (fn [attr-name {:keys [type node-type characteristics?]}]
                           (and (not (nodes-blacklist node-type))
                                (or (number-types type)
                                    (string-types type))
                                (or characteristics? (number-types type))
                                (not (attributes attr-name))))
        attr-options (gen-attr-options global-ac-attribute-types frame-ac-attribute-types add-condition-fn translate-multi extra-attribtues)]
    (sort-options attr-options)))

(defn attribute-select [{:keys [layout on-change-layout ac-attribute-types extra-attributes request-characteristics translate-multi translate] :as props}]
  (let [{:keys [read-only? attributes]} (val-or-deref layout)
        global-ac-attribute-types (val-or-deref (ac-attribute-types :global))
        frame-ac-attribute-types (val-or-deref (ac-attribute-types :frame))
        ac-options (build-attribute-options global-ac-attribute-types frame-ac-attribute-types attributes translate-multi extra-attributes)
        labels @(fi/call-api [:i18n :get-labels-sub])]
    [select
     {:label @(translate :legend-attribute)
      :hint @(translate :layout-attribute-hint)
      :is-clearable? false
      :on-change #(change-attributes on-change-layout % request-characteristics)
      :disabled? (boolean read-only?)
      :options ac-options
      :values (mapv (fn [attr]
                      {:label (attr->display-name attr labels)
                       :value attr})
                    attributes)
      :menu-row-height 35
      :group-selectable? false
      :tooltip-key :tooltip
      :check-key :tooltip
      :is-grouped? true
      :is-multi? true
      :extra-class "input--w100"}]))

(defn min-max-display [{:keys [layout ac-attribute-types ac-vals]}]
  (let [{:keys [attributes attribute-type]} (val-or-deref layout)
        frame-ac-attribute-types (val-or-deref (ac-attribute-types :frame))
        ac-vals (val-or-deref (ac-vals attributes nil))
        min-max-vals (if (seq frame-ac-attribute-types)
                       (:frame ac-vals)
                       (:global ac-vals))
        min (first min-max-vals)
        max (peek min-max-vals)
        {min-label :min
         max-label :max} @(re-frame/subscribe [::i18n/translate-multi :min :max])]
    (when (and (= attribute-type "number") min max)
      [:div {:class ["flex"
                     "flex-wrap"
                     "align-items-center"
                     "gap-x-4"
                     "py-4"
                     "text-xs"]}
       [:span min-label ": " (i18n/localized-number min)
        (gstr/unescapeEntities " &#8211; ")]
       [:span max-label ": " (i18n/localized-number max)]])))

(defn coloring [{:keys [layout color-scales on-change-layout show-attribute-select?]
                 :or {show-attribute-select? true}
                 :as props}]
  (let [{:keys [read-only? color-scheme]} (val-or-deref layout)
        {:keys [id]} color-scheme
        color-scales (val-or-deref color-scales)
        color-scale-options (->> (vals color-scales)
                                 (sort-by :name)
                                 (mapv (fn [{:keys [id name colors]}]
                                         {:label [gen-layout-title name colors]
                                          :tooltip-value name
                                          :value id})))
        selected-color-scale (some #(when (= id (:value %))
                                      %)
                                   color-scale-options)
        {:keys [color-scale-custom-coloring-label
                color-scale-custom-coloring-tooltip]}
        @(re-frame/subscribe [::i18n/translate-multi :color-scale-custom-coloring-label :color-scale-custom-coloring-tooltip])]
    [:div
     (when show-attribute-select?
       [:<>
        [attribute-select props]
        [min-max-display props]])
     [select {:label (re-frame/subscribe [::i18n/translate :color-scale-label])
              :is-clearable? false
              :on-change #(if-let [new-color-scale (get color-scales (:value %))]
                            (change-color-scale on-change-layout new-color-scale)
                            (remove-color-scale-ref on-change-layout))
              :tooltip-key :tooltip-value
              :options color-scale-options
              :values (if (and id selected-color-scale)
                        selected-color-scale
                        {:label color-scale-custom-coloring-label
                         :tooltip-value color-scale-custom-coloring-tooltip
                         :value nil})
              :disabled? (boolean read-only?)
              :menu-row-height 35
              :extra-class "input--w100"}]
     [color-ranges props]]))