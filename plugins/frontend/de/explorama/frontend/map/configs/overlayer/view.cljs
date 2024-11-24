(ns de.explorama.frontend.map.configs.overlayer.view
  (:require [clojure.string :as str]
            [de.explorama.shared.data-format.aggregations :as dfl-agg]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [select]]
            [de.explorama.frontend.ui-base.utils.select :as select-util]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.common.views.legend :refer [attribute-select coloring min-max-display
                                                               init-color-assign setting-section]]
            [de.explorama.frontend.map.config :as config]
            [de.explorama.frontend.map.paths :as geop]
            [re-frame.core :as re-frame]))

;; =============  Feature Layer  =============================================== 

(re-frame/reg-sub
 ::feature-layers-options
 (fn [db _]
   (let [feature-layers (get-in db geop/feature-color-layers)]
     (mapv (fn [[id {:keys [name]}]]
             {:label name
              :value id})
           feature-layers))))

(re-frame/reg-sub
 ::feature-layer-hint
 (fn [[_ feature-layer-id]]
   [(re-frame/subscribe [:de.explorama.frontend.map.map.core/feature-layer-config feature-layer-id])])
 (fn [[feature-layer-config] _]
   (get feature-layer-config :hint)))

(re-frame/reg-sub
 ::feature-layer-grouping-attributes
 (fn [[_ feature-layer-id]]
   [(re-frame/subscribe [:de.explorama.frontend.map.map.core/feature-layer-config feature-layer-id])])
 (fn [[feature-layer-config] _]
   (sort
    (get feature-layer-config :grouping-attributes))))

(defn- feature-layer-comp [{:keys [layout on-change-layout]}]
  (let [{:keys [read-only? feature-layer-id]} (val-or-deref layout)
        feature-label-select @(re-frame/subscribe [::i18n/translate :feature-layer-select])
        grouping-label-select @(re-frame/subscribe [::i18n/translate :feature-grouping-select])

        all-feature-options @(re-frame/subscribe [::feature-layers-options])
        current-feature-hint @(re-frame/subscribe [::feature-layer-hint feature-layer-id])
        grouping-attributes @(re-frame/subscribe [::feature-layer-grouping-attributes feature-layer-id])

        attribute-labels @(fi/call-api [:i18n :get-labels-sub])]
    [:div
     [select
      {:extra-class "input--w100"
       :is-clearable? false
       :disabled? read-only?
       :label feature-label-select
       :hint current-feature-hint
       :options all-feature-options
       :values (select-util/selected-option :value all-feature-options feature-layer-id)
       :on-change #(on-change-layout :feature-layer-id (get % :value))}]
     [:div.explorama__form__input {:style  {:padding-left 0}}
      grouping-label-select ": "
      (->>  grouping-attributes
            (mapv #(get attribute-labels % %))
            (str/join ", "))]]))

;; =============  Extrema  =============================================== 

(defn- extrema-comp [{:keys [layout on-change-layout]}]
  (let [{:keys [extrema read-only?]} (val-or-deref layout)
        drop-down-label @(re-frame/subscribe [::i18n/translate :designer-layer-heatmap-drop-down-type])
        hint-label @(re-frame/subscribe [::i18n/translate :heatmap-type-hint])
        extrema-options [{:value :local
                          :label @(re-frame/subscribe [::i18n/translate :designer-layer-heatmap-weighted])}
                         {:value :global
                          :label @(re-frame/subscribe [::i18n/translate :designer-layer-heatmap-point-density])}]
        extrema (keyword extrema)]
    [:div
     [select
      {:label drop-down-label
       :is-clearable? false
       :hint hint-label
       :disabled? read-only?
       :options extrema-options
       :extra-class "input--w100"
       :filter-key :value
       :values (select-util/selected-option :value extrema-options extrema)
       :on-change #(let [extrema-val (get % :value)]
                     (on-change-layout :extrema extrema-val)
                     (when (= extrema-val :global)
                       (on-change-layout :attributes nil)
                       (on-change-layout :attribute-type nil)))}]]))

;; =============  Aggregation  =============================================== 

(defn- aggregate-method-comp [{:keys [layout on-change-layout] :as props}]
  (let [{:keys [id aggregate-method-name read-only? attributes attribute-type]} (val-or-deref layout)
        aggregate-method (keyword aggregate-method-name)
        label @(re-frame/subscribe [::i18n/translate :designer-color-coding-aggregate-method])
        possible-methods @(re-frame/subscribe [:de.explorama.frontend.map.configs.overlayer.core/aggregate-methods attribute-type])]
    (when (some (fn [attr]
                  (not (get dfl-agg/descs attr)))
                attributes)
      [:div
       [select
        {:label label
         :is-clearable? false
         :options possible-methods
         :disabled? read-only?
         :extra-class "input--w14"
         :filter-key :value
         :values (select-util/selected-option :value possible-methods aggregate-method)
         :on-change (fn [{new-method :value}]
                      (on-change-layout (fn [layout]
                                          (let [{old-method-attribute? :need-attribute?
                                                 :as old-desc}
                                                (get dfl-agg/descs (keyword (get layout :aggregate-method-name)))
                                                {new-method-attribute? :need-attribute?
                                                 :as new-desc} (get dfl-agg/descs new-method)

                                                reset-attribute? (or (and (seq old-desc)
                                                                          (not old-method-attribute?)
                                                                          (seq new-desc)
                                                                          new-method-attribute?)
                                                                     ;Old method was string, new one is number => reset attribute
                                                                     (and (nil? old-desc)
                                                                          (seq new-desc)
                                                                          (not= attribute-type "number"))
                                                                     ;New method was string, old was number => reset attribute
                                                                     (and (seq old-desc)
                                                                          (nil? new-desc)
                                                                          (not= attribute-type "string")))]
                                            (cond-> layout
                                              :always
                                              (assoc :aggregate-method-name (name new-method))

                                              (and (not new-method-attribute?) (seq new-desc))
                                              (assoc :attributes [(name new-method)])
                                              
                                              reset-attribute?
                                              (assoc :attributes []))))))}]])))

;; =============  Movement  =============================================== 
(defn attr->display-name [attr]
  (let [labels @(fi/call-api [:i18n :get-labels-sub])]
    (get labels attr attr)))

(defn- movement-comp [{:keys [layout on-change-layout]}]
  (let [{:keys [source target read-only?]} (val-or-deref layout)
        source-label @(re-frame/subscribe [::i18n/translate :designer-layer-movement-source])
        target-label @(re-frame/subscribe [::i18n/translate :designer-layer-movement-target])
        geo-acs @(re-frame/subscribe [:de.explorama.frontend.map.acs/geo-acs])
        geo-options (mapv (fn [{:keys [name]}]
                            {:label (attr->display-name name)
                             :value name})
                          geo-acs)]
    [:div
     [select
      {:label source-label
       :is-clearable? false
       :disabled? read-only?
       :options geo-options
       :extra-class "input--w100"
       :filter-key :value
       :values (select-util/selected-option :value geo-options source)
       :on-change #(on-change-layout :source (get % :value))}]
     [select
      {:label target-label
       :is-clearable? false
       :disabled? read-only?
       :options geo-options
       :extra-class "input--w100"
       :filter-key :value
       :values (select-util/selected-option :value geo-options target)
       :on-change #(on-change-layout :target (get % :value))}]]))

;; =============  Layer Select  =============================================== 

(defn- to-feature [on-change attribute-type]
  (let [all-feature-options @(re-frame/subscribe [::feature-layers-options])]
    (init-color-assign on-change true ["#fb8d02"])
    (on-change :feature-layer-id (get (first all-feature-options) :value))
    (on-change :aggregate-method-name (name (get config/default-aggregate-method attribute-type)))))

(defn- to-heatmap [on-change]
  (init-color-assign on-change false ["#14bd55" "#f4ed19" "#f82e2c"])
  (on-change :extrema :local))

(defn- to-movement [on-change]
  (let [geo-acs @(re-frame/subscribe [:de.explorama.frontend.map.acs/geo-acs])
        value (get (first geo-acs) :name)]
    (init-color-assign on-change false ["#babab9"])
    (on-change :source value)
    (on-change :target value)))

(defn- default-overlayer-settings [{:keys [layout on-change-layout]} new-type]
  (let [{:keys [attributes attribute-type]} (val-or-deref layout)
        attribute-type (or attribute-type :number)]
    (when-not attributes
      (on-change-layout :attribute-type attribute-type))
    (case new-type
      :feature (to-feature on-change-layout attribute-type)
      :heatmap (to-heatmap on-change-layout)
      :movement (to-movement on-change-layout))))

(defn- layer-type-comp [{:keys [layout on-change-layout] :as props}]
  (let [{:keys [layer-type read-only?]} (val-or-deref layout)
        drop-down-label @(re-frame/subscribe [::i18n/translate :designer-layer-type])
        type (keyword layer-type)
        type (if (= type :country)
               :feature
               type)
        type-options [#_;TODO r1/map there are currently not feature layers available
                      {:value :feature
                       :label @(re-frame/subscribe [::i18n/translate :designer-layer-type-feature])}
                      {:value :heatmap
                       :label @(re-frame/subscribe [::i18n/translate :designer-layer-type-heatmap])}
                      {:value :movement
                       :label @(re-frame/subscribe [::i18n/translate :designer-layer-type-movement])}]]
    [:div
     [select
      {:label drop-down-label
       :is-clearable? false
       :disabled? read-only?
       :options type-options
       :extra-class "input--w100"
       :values (select-util/selected-option :value type-options type)
       :on-change #(do (default-overlayer-settings props (get % :value))
                       (on-change-layout :layer-type (get % :value)))}]]))



;; =============  View  =============================================== 

(defn view [{:keys [layout naming? layer-select? feature-layer? extrema? attributes? coloring?
                    movement? collapsible? default-open]
             :or {naming? true
                  layer-select? true
                  feature-layer? true
                  extrema? true
                  attributes? true
                  coloring? true
                  movement? true
                  default-open #{:layer-select :feature-layer :heatmap}
                  collapsible? true}
             :as props}]
  (let [{:keys [layer-type extrema]} (val-or-deref layout)
        parent (if collapsible?
                 :div.section__collapsible__group
                 :<>)]
    [parent
     (when (or layer-select? naming?)
       [setting-section (assoc props
                               :label @(re-frame/subscribe [::i18n/translate :general-settings-group])
                               :default-open? (or (default-open :naming)
                                                  (default-open :layer-select)))
          ;; (when naming? [naming props])
        (when layer-select? [layer-type-comp props])])
     (when (and feature-layer?
                (#{:country "country" :feature "feature"} layer-type))
       [setting-section (assoc props
                               :label @(re-frame/subscribe [::i18n/translate :feature-layer-group])
                               :default-open? (default-open :feature-layer))
        [feature-layer-comp props]])
     (when (and extrema?
                (#{:heatmap "heatmap"} layer-type))
       [setting-section (assoc props
                               :label @(re-frame/subscribe [::i18n/translate :designer-layer-heatmap])
                               :default-open? (default-open :heatmap))
        [extrema-comp props]])
     (when (and attributes?
                layer-type
                (not (#{:feature "feature"} layer-type))
                (not= extrema :global))
       [setting-section (assoc props
                               :label @(re-frame/subscribe [::i18n/translate :attribute-settings-group])
                               :default-open? (default-open :attributes))
        [attribute-select (assoc props
                                 :extra-attributes (when (#{:movement "movement"} layer-type)
                                                     {:number-of-events
                                                      {:std {:type :number :node-type "Fact" :characteristics? false}
                                                       :general-group? true}}))]])
     (when (and coloring?
                (#{:country "country" :feature "feature"} layer-type))
       [setting-section (assoc props
                               :label @(re-frame/subscribe [::i18n/translate :color-settings-group])
                               :default-open? (default-open :coloring))
        [:<>
         [attribute-select (assoc props
                                  :extra-attributes {:number-of-events
                                                     {:std {:type :number :node-type "Fact" :characteristics? false}
                                                      :general-group? true}})]
         [min-max-display props]
         [aggregate-method-comp props]
         [coloring (assoc props
                          :show-attribute-select? false
                          :selectable-color-limit config/selectable-color-limit)]]])
     (when (and movement?
                (#{:movement "movement"} layer-type))
       [setting-section (assoc props
                               :label @(re-frame/subscribe [::i18n/translate :designer-layer-movement])
                               :default-open? (default-open :attributes))
        [movement-comp props]])]))