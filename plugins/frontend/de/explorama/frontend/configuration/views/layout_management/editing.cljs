(ns de.explorama.frontend.configuration.views.layout-management.editing
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.views.legend :refer [attr->display-name coloring
                                                               gen-attr-options setting-section sort-options]]
            [de.explorama.frontend.configuration.config :as config-config]
            [de.explorama.frontend.ui-base.components.formular.core :refer [input-group]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.shared.common.configs.platform-specific :as config-platform]
            [re-frame.core :refer [subscribe]]
            [reagent.core :as r]))

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

(defn- init-card-fields [fields old-assignments]
  (let [old-count (count old-assignments)]
    (if (<= old-count fields)
      (apply conj
             (or old-assignments [])
             (map (fn [_] (vector "else" ""))
                  (range 0 (- fields old-count))))
      (subvec old-assignments 0 fields))))

(defn- change-card-scheme [on-change-layout new-schema-id fields]
  (on-change-layout (fn [o]
                      (-> o
                          (assoc :card-scheme new-schema-id)
                          (update :field-assignments (partial init-card-fields fields))))))

(defn- img-options [opts]
  (mapv (fn [{:keys [value label tooltip-value]}]
          {:label [:img
                   {:width 15
                    :height 15
                    :style {:filter "brightness(0.5)"}
                    :key value
                    :src label}]
           :tooltip-value tooltip-value
           :value value})
        opts))

(def ^:private field-image-options
  (into [{:value "notes"
          :tooltip (subscribe [::i18n/translate :icon-tooltip-notes])
          :icon :mosaic-note}
         {:value "speech-bubble"
          :tooltip (subscribe [::i18n/translate :icon-tooltip-speech-bubble])
          :icon :comment-empty}
         {:value "organisation"
          :tooltip (subscribe [::i18n/translate :icon-tooltip-organisation])
          :icon :mosaic-group}
         {:value "location"
          :tooltip (subscribe [::i18n/translate :icon-tooltip-location])
          :icon :mosaic-pin}
         {:value "else"
          :tooltip (subscribe [::i18n/translate :icon-tooltip-else])
          :icon :mosaic-info}]
        (map (fn [icon]
               {:value icon
                :tooltip (subscribe [::i18n/translate (keyword (str "icon-tooltip-" icon))])
                :icon (keyword (str "mosaic-" icon))})
             ["calendar" "clock" "drop" "flame" "globe2" "health" "map" "percentage"
              "rain" "star" "transfer" "charts" "city" "coin" "euro" "globe" "heart"
              "leaf" "sun" "search" "circle"])))

(defn- scheme-field [on-change-layout idx ico attribute attribute-options read-only?]
  (let [field-label @(subscribe [::i18n/translate :field-label])
        labels @(fi/call-api [:i18n :get-labels-sub])]
    [input-group
     {:label (str field-label " " (inc idx))
      :items [{:type :icon-select
               :id (str "field-icon-" idx)
               :component-props {:options field-image-options
                                 :value ico
                                 :on-change #(on-change-layout [:field-assignments idx 0] %)}}
              {:type :select
               :id (str "field-attribute-" idx)
               :component-props {:is-multi? false
                                 :is-grouped? true
                                 :disabled? (boolean read-only?)
                                 :group-selectable? false
                                 :tooltip-key :tooltip
                                 :check-key :tooltip
                                 :extra-class "input--w100"
                                 :options attribute-options
                                 :values {:label (attr->display-name attribute labels) :value attribute}
                                 :menu-height 280
                                 :on-change #(on-change-layout [:field-assignments idx 1]
                                                               (get-in % [:value :attr-name]))}}]}]))

(defn- build-field-options [global-ac-attribute-types frame-ac-attribute-types field-assignments translate-multi]
  (let [used-attributes (set (map second field-assignments))
        fields-blacklist #{"year" "month" "day"} ;;currently not supported by mosaic, maybe we should do it
        add-condition-fn (fn [attr-name _]
                           (and (not (used-attributes attr-name))
                                (not (fields-blacklist attr-name))))
        field-options (gen-attr-options global-ac-attribute-types frame-ac-attribute-types add-condition-fn translate-multi nil)]
    (sort-options field-options)))

(defn- gen-scheme-fields [{:keys [layout on-change-layout ac-attribute-types translate-multi] :as props} read-only?]
  (let [{:keys [field-assignments]} (val-or-deref layout)
        ac-attribute-types (val-or-deref ac-attribute-types)
        global-ac-attribute-types (val-or-deref (ac-attribute-types :global))
        frame-ac-attribute-types (val-or-deref (ac-attribute-types :frame))
        ac-options (build-field-options global-ac-attribute-types frame-ac-attribute-types field-assignments translate-multi)]
    (reduce (fn [acc [idx [ico attribute]]]
              (conj acc
                    [scheme-field on-change-layout idx ico attribute ac-options read-only?]))
            [:<>]
            (map-indexed vector field-assignments))))

(def field-types [{:id "scheme-1" :url "img/mosaic/eventcard-icon-8.svg" :fields 8}
                  {:id "scheme-2" :url "img/mosaic/eventcard-icon-6.svg" :fields 6}
                  {:id "scheme-3" :url "img/mosaic/eventcard-icon-4.svg" :fields 4}])

(defn- card-scheme [{:keys [id url]} _]
  (let [radio-id (str "config-layout-fields" id url (random-uuid))]
    (fn [{:keys [active? id url fields on-change-layout]} read-only?]
      [:li.card__layout
       [:div.explorama__ecd__radio-container
        [:input.explorama__ecd__radio
         {:type "radio"
          :disabled read-only?
          :value id
          :id radio-id
          :checked active?
          :on-change #(when-not active?
                        (change-card-scheme on-change-layout id fields))}]
        [:label.explorama__ecd__radio
         {:for radio-id
          :style {:background-image (str "url(\""
                                         config-platform/explorama-asset-origin
                                         url
                                         "\")")}}]]])))

(defn- card-schemes [{:keys [layout] :as props}]
  (let [{seleted-card-schema :card-scheme read-only? :read-only?} (val-or-deref layout)]
    [:div
     (reduce (fn [acc {:keys [id url fields]}]
               (conj
                acc
                [card-scheme (assoc props
                                    :active? (= id seleted-card-schema)
                                    :fields fields
                                    :id id
                                    :url url)
                 read-only?]))

             [:ul.edc__layouts]
             field-types)
     [gen-scheme-fields props read-only?]]))


(defn- editing-view-impl [{:keys [fields? coloring? collapsible? default-open]
                           :or {fields? true
                                coloring? true
                                default-open #{}
                                collapsible? true}
                           :as props}]

  (let [parent (if collapsible?
                 :div.section__collapsible__group
                 :<>)
        {:keys [color-settings-group field-settings-group]}
        @(subscribe [::i18n/translate-multi
                     :general-settings-group
                     :color-settings-group
                     :field-settings-group
                     :share-settings-group])]
    [parent
    ;;  (when naming?
    ;;    [setting-section (assoc props
    ;;                            :label general-settings-group
    ;;                            :default-open? (default-open :naming))
    ;;     [naming props]])
     (when coloring?
       [setting-section (assoc props
                               :label color-settings-group
                               :default-open? (default-open :coloring))
        [coloring (assoc props :selectable-color-limit config-config/selectable-color-limit)]])
     (when fields?
       [setting-section (assoc props
                               :label field-settings-group
                               :default-open? (default-open :fields))
        [card-schemes props]])]))

(defn editing-view [{:keys [on-unmount request-characteristics layout]}]
  (r/create-class
   {:display-name "editing-view"
    :component-did-mount (fn []
                           (when-let [attrs (:attributes (val-or-deref layout))]
                             (when (fn? request-characteristics)
                               (request-characteristics attrs))))

    :component-will-unmount #(when (fn? on-unmount)
                               (on-unmount))
    :reagent-render editing-view-impl}))