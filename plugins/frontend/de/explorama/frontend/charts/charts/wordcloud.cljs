(ns de.explorama.frontend.charts.charts.wordcloud
  (:require ["react-d3-cloud" :as ReactD3Cloud]
            ["seedrandom"]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [input-field
                                                                            select]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [de.explorama.frontend.charts.charts.utils :as cutils]
            [de.explorama.frontend.charts.config :as config]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.charts.util.queue :as queue-util]
            [de.explorama.shared.charts.ws-api :as ws-api]))

(def d3-cloud (reagent/adapt-react-class ReactD3Cloud))

(defonce chart-id-prefix "vis_wordcloud-")
(def margin 25)

(def range-min 10)
(def range-max 90)

(defn- calc-height [frame-id new-height]
  (let [{padding-height :height} (fi/call-api :frame-content-padding-raw)]
    (- new-height
       padding-height
       margin)))

(re-frame/reg-sub
 ::selected-attributes
 (fn [db [_ frame-id]]
   (get-in db (path/attributes frame-id 0) [])))

(re-frame/reg-event-fx
 ::change-attributes
 (fn [{db :db} [_ frame-id selection]]
   (let [n-db (assoc-in db
                        (path/attributes frame-id 0)
                        selection)]
     {:db n-db
      :dispatch-n (cutils/req-datasets n-db frame-id)})))

(re-frame/reg-sub
 ::stemming-attributes
 (fn [db [_ frame-id]]
   (get-in db
           (path/stemming-attributes frame-id 0))))

(re-frame/reg-event-fx
 ::change-stemming-attributes
 (fn [{db :db} [_ frame-id attribute-selections]]
   (let [n-db (assoc-in db
                        (path/stemming-attributes frame-id 0)
                        attribute-selections)]
     {:db n-db
      :dispatch-n (cutils/req-datasets n-db frame-id)})))

(re-frame/reg-sub
 ::stopping-attributes
 (fn [db [_ frame-id]]
   (get-in db
           (path/stopping-attributes frame-id 0)
           ws-api/default-stopping-attrs)))

(re-frame/reg-event-fx
 ::change-stopping-attributes
 (fn [{db :db} [_ frame-id attribute-selections]]
   (let [n-db (assoc-in db
                        (path/stopping-attributes frame-id 0)
                        attribute-selections)]
     {:db n-db
      :dispatch-n (cutils/req-datasets n-db frame-id)})))

(re-frame/reg-sub
 ::min-occurence
 (fn [db [_ frame-id]]
   (get-in db (path/min-occurence frame-id 0) 1)))

(re-frame/reg-event-fx
 ::change-min-occurence
 (fn [{db :db} [_ frame-id new-min-occurence]]
   (let [n-db (assoc-in db
                        (path/min-occurence frame-id 0)
                        new-min-occurence)]
     {:db n-db
      :dispatch-n (cutils/req-datasets n-db frame-id)})))

(re-frame/reg-sub
 ::search-selection
 (fn [db [_ frame-id]]
   (get-in db
           (path/search-selection frame-id)
           {:label (i18n/translate db :wordcloud-search-characteristics-label)
            :value :characteristics})))

(re-frame/reg-event-fx
 ::change-search-selection
 (fn [{db :db} [_ frame-id {search-val :value
                            :as search-selection}]]
   (let [new-attribute-selection (cond
                                   (= search-val :all) [{:value :all}]
                                   (= search-val :notes) [{:value "notes"}]
                                   (= search-val :char) [{:value :characteristics}]
                                   :else [])]
     {:db (assoc-in db
                    (path/search-selection frame-id)
                    search-selection)
      :dispatch [::change-attributes frame-id new-attribute-selection]})))

(defn render-done [frame-id]
  (re-frame/dispatch
   (fi/call-api :render-done-event-vec frame-id (str config/default-namespace " - wordcloud"))))

(defn left-col [frame-id]
  (let [all-label @(re-frame/subscribe [::i18n/translate :wordcloud-search-all-label])
        char-only-label @(re-frame/subscribe [::i18n/translate :wordcloud-search-characteristics-label])
        notes-label @(re-frame/subscribe [::i18n/translate :wordcloud-search-notes-only-label])
        selected-attribute-label @(re-frame/subscribe [::i18n/translate :wordcloud-search-selected-attributes-label])
        search-selection-label (re-frame/subscribe [::i18n/translate :wordcloud-search-label])

        options [{:value :all
                  :label all-label}
                 {:value :char
                  :label char-only-label}
                 {:value :notes
                  :label notes-label}
                 {:value :selected-attrs
                  :label selected-attribute-label}]
        selected-option @(re-frame/subscribe [::search-selection frame-id])
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id})]
    [select {:name (str "chart-search-selection_" frame-id)
             :disabled? read-only?
             :options options
             :values selected-option
             :parent-extra-class "explorama__form--flex"
             :extra-class "input--w100"
             :on-change (fn [selection]
                          (re-frame/dispatch [::change-search-selection frame-id selection]))
             :is-clearable? false
             :label search-selection-label}]))

(defn right-col [frame-id]
  (let [{selected-search :value} @(re-frame/subscribe [::search-selection frame-id])
        attribute-options @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/wordcloud-attr-options frame-id])
        selected-attributes @(re-frame/subscribe [::selected-attributes frame-id])
        select-attribute-label (re-frame/subscribe [::i18n/translate :wordcloud-select-attribute-label])
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id})]
    (when (= :selected-attrs selected-search)
      [select {:name (str "chart-attribute-selection_" frame-id)
               :disabled? read-only?
               :options attribute-options
               :values selected-attributes
               :label select-attribute-label
               :on-change (fn [selections]
                            (re-frame/dispatch [::change-attributes frame-id selections]))
               :is-multi? true
               :parent-extra-class "explorama__form--flex"
               :extra-class "input--w100"}])))

(defn advanced-left-col [frame-id]
  (let [min-occurence-label (re-frame/subscribe [::i18n/translate :wordcloud-min-occurence-label])
        current-min-occurence (re-frame/subscribe [::min-occurence frame-id])
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id})]
    [input-field {:label min-occurence-label
                  :disabled? read-only?
                  :value current-min-occurence
                  :extra-class "input--w20"
                  :type :number
                  :min 1
                  :on-change (fn [new-val]
                               (re-frame/dispatch [::change-min-occurence frame-id new-val]))}]))

(defn advanced-right-col [frame-id]
  (let [stopping-attributes-label (re-frame/subscribe [::i18n/translate :wordcloud-stopping-attributes-label])
        stopping-info (re-frame/subscribe [::i18n/translate :wordcloud-stopping-info])
        stemming-attributes-label (re-frame/subscribe [::i18n/translate :wordcloud-stemming-attributes-label])
        stemming-info (re-frame/subscribe [::i18n/translate :wordcloud-stemming-info])
        all-attribute-options @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/wordcloud-attr-options frame-id])
        {selected-search :value} @(re-frame/subscribe [::search-selection frame-id])
        selected-attributes (set (map :value @(re-frame/subscribe [::selected-attributes frame-id])))
        filtered-options (if (#{:all :characteristics} selected-search)
                           all-attribute-options
                           (filterv
                            #(selected-attributes (:value %))
                            all-attribute-options))
        selected-stopping-attributes @(re-frame/subscribe [::stopping-attributes frame-id])
        selected-stemming-attributes @(re-frame/subscribe [::stemming-attributes frame-id])
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id})]
    [:<>
     [select {:is-multi? true
              :is-clearable? true
              :extra-class "input--w20"
              :label stopping-attributes-label
              :disabled? read-only?
              :options filtered-options
              :values selected-stopping-attributes
              :hint stopping-info
              :on-change (fn [selections]
                           (re-frame/dispatch [::change-stopping-attributes frame-id selections]))}]
     [select {:is-multi? true
              :is-clearable? true
              :extra-class "input--w20"
              :label stemming-attributes-label
              :disabled? read-only?
              :options filtered-options
              :values selected-stemming-attributes
              :hint stemming-info
              :on-change (fn [selections]
                           (re-frame/dispatch [::change-stemming-attributes frame-id selections]))}]]))

(defn- props-map [frame-id {:keys [size] :as vis-desc}]
  (let [data @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
        {[width height] :size} @(fi/call-api :frame-sub frame-id)
        scale-info @(fi/call-api :scale-info-sub)]
    {:scale-info scale-info
     :data data
     :height (if size (second size) height)
     :width (if size (first size) width)
     :render-done render-done
     :type path/wordcloud-id-key}))

;! remove this and put it into the ui-base
(defn- options-hidden [translate-function & hidden-elements]
  (let [state (reagent/atom false)]
    (fn [translate-function & hidden-elements]
      [:div.collapsible
       [:div {:class (cond-> "content"
                       @state
                       (str " open"))}
        (into [:<>] hidden-elements)]
       [:a {:href "#"
            :on-click #(swap! state not)}
        [:div.collapsible__bar
         [:div.label
          [:span
           (if @state (translate-function :settings-show-less)
               (translate-function :settings-show-more))]]]]])))

(defn chart-data-selection [frame-id _]
  (reagent/create-class
   {:reagent-render (fn [frame-id props]
                      [:<>
                       [left-col frame-id]
                       [right-col frame-id]
                       [options-hidden
                        (fn [label]
                          @(re-frame/subscribe [::i18n/translate label]))
                        [advanced-left-col frame-id]
                        [advanced-right-col frame-id]]])}))

(defn chart-component [frame-id props]
  (reagent/create-class
   {:display-name chart-id-prefix
    :component-did-mount (fn []
                           (render-done frame-id))
    :should-component-update cutils/should-update?
    :component-did-update (fn [this]
                            #_(let [[_ frame-id props] (reagent/argv this)]
                                {:keys [height width render-done]} @props))

    :component-will-unmount (fn [])
    :component-did-catch cutils/component-did-catch
    :reagent-render (fn [_ props]
                      (let [{:keys [height width]
                             [prop-data] :data} props
                            [min-val max-val data] (when (vector? prop-data)
                                                     prop-data)
                            data (mapv (fn [[text val]]
                                         {:text (i18n/attribute-label text)
                                          :value val})
                                       data)
                            rnd-gen (js/Math.seedrandom. (str data))
                            new-height (calc-height frame-id height)]
                        (when data
                          [d3-cloud {:data data
                                     :width width
                                     :height height
                                     :rotate (fn [_] 0)
                                     :random (fn [_]
                                               (.double rnd-gen))
                                     :font "Open Sans"
                                     :on-word-mouse-over (fn [e word]
                                                           #_(js/console.info "MouseOver" {:event e}
                                                                              :word word))
                                     :font-size (fn [word]
                                                  (let [word-val (aget word "value")
                                                        normalized-value (if (= min-val max-val)
                                                                           range-max
                                                                           (+ (* (- range-max
                                                                                    range-min)
                                                                                 (/ (- word-val min-val)
                                                                                    (- max-val min-val)))
                                                                              range-min))]
                                                    (* (js/Math.log2 normalized-value)
                                                       (cond
                                                         (< new-height 600) 5
                                                         (< new-height 1000) 10
                                                         :else 15))))
                                     :padding 2}])))}))

(defn settings-panel [frame-id]
  (let [[data] @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
        {[width height] :size} @(fi/call-api :frame-sub frame-id)
        props (reagent/atom {:data data
                             :height height
                             :width width
                             :render-done render-done
                             :type path/wordcloud-id-key})]
    [chart-data-selection frame-id props]))



(defn content [frame-id vis-desc]
  (reagent/create-class
   {:display-name chart-id-prefix
    :component-did-mount (fn []
                           (let [[data] @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
                                 loading? @(re-frame/subscribe [::queue-util/loading? frame-id])]
                             (when (and vis-desc (not loading?) (empty? data))
                               (re-frame/dispatch [:de.explorama.frontend.charts.vis-state/restore-vis-desc frame-id vis-desc]))
                             (when (empty? data)
                               (render-done frame-id))))
    :reagent-render (fn [frame-id vis-desc]
                      (let [[data] @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
                            {:keys [width height] :as props} (props-map frame-id vis-desc)]
                        [:div {:style {:width width
                                       :height height}}
                         (when (not-empty data)
                           [chart-component frame-id props])]))}))

(def chart-desc {path/chart-desc-id-key path/wordcloud-id-key
                 path/chart-desc-label-key :wordcloud-chart-label
                 path/chart-desc-selector-class-key "chart__wordcloud"
                 path/chart-desc-content-key content
                 path/chart-desc-settings-key settings-panel
                 path/chart-desc-multiple-key false
                 path/chart-desc-icon-key :charts-wordcloud2
                 path/chart-desc-settings-update-key
                 (fn [frame-id])})