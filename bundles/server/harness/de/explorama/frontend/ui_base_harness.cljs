(ns de.explorama.frontend.ui-base-harness
  "Render harness for verifying ui_base component styling: mounts ui_base
   components in all catalog variants, then serializes computed styles of
   every node into #computed-styles so `chromium --dump-dom` captures them.

   Catalog rule: for each component, render one instance per combination of
   the variant axes its parameter-definition declares as :characteristics,
   with a stable :data-harness id per instance. Required params are filled
   with fixed literal values so the render is deterministic."
  (:require [reagent.dom :as rdom]
            [clojure.string :as str]
            [de.explorama.frontend.ui-base.utils.subs :as ui-subs]
            [de.explorama.frontend.ui-base.components.misc.chip :refer [chip]]
            [de.explorama.frontend.ui-base.components.misc.hint :refer [hint]]
            [de.explorama.frontend.ui-base.components.common.tooltip :refer [tooltip]]
            [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
            [de.explorama.frontend.ui-base.components.formular.button-group :refer [button-group]]
            [de.explorama.frontend.ui-base.components.formular.card :refer [card]]
            [de.explorama.frontend.ui-base.components.formular.checkbox :refer [checkbox]]
            [de.explorama.frontend.ui-base.components.formular.collapsible-list :refer [collapsible-list]]
            [de.explorama.frontend.ui-base.components.formular.input-field :refer [input-field]]
            [de.explorama.frontend.ui-base.components.formular.textarea :refer [textarea]]
            [de.explorama.frontend.ui-base.components.formular.radio :refer [radio]]
            [de.explorama.frontend.ui-base.components.formular.select :refer [select]]))

(def ^:private noop (fn [& _]))

(defn- kw-part [k]
  (if k (name k) "default"))

(defn- section [title & children]
  (into [:div {:style {:padding "16px" :border-bottom "1px solid #ccc"}}
         [:h3 title]]
        children))

(defn- instance [id & body]
  (into [:div {:data-harness id
               :style {:margin "6px"
                       :display "inline-block"
                       :vertical-align "top"
                       :min-width "120px"}}]
        body))

;; ---------------------------------------------------------------- chip
(defn- chip-section []
  (section "chip"
           (concat
            (for [variant [:primary :secondary]
                  size [:extra-small :small :normal :big]
                  brightness [nil :light :dark]]
              ^{:key (str variant size brightness)}
              [instance (str "chip-" (name variant) "-" (name size) "-" (kw-part brightness))
               [chip (cond-> {:variant variant :size size :label "Chip"}
                       brightness (assoc :brightness brightness))]])
            ;; :color axis (6 characteristics), swept against variant and
            ;; brightness at fixed :normal size.
            (for [variant [:primary :secondary]
                  color [:blue :green :orange :red :teal :yellow]
                  brightness [nil :light :dark]]
              ^{:key (str "color" variant color brightness)}
              [instance (str "chip-color-" (name variant) "-" (name color)
                             "-" (kw-part brightness))
               [chip (cond-> {:variant variant :size :normal :color color :label "Chip"}
                       brightness (assoc :brightness brightness))]]))))

;; ---------------------------------------------------------------- button
(defn- button-section []
  (section "button"
           (for [variant [:primary :secondary :tertiary :back]
                 btype [:normal :warning]
                 size [:small :normal :big]
                 disabled? [false true]]
             ^{:key (str variant btype size disabled?)}
             [instance (str "button-" (name variant) "-" (name btype) "-" (name size)
                            "-" (if disabled? "disabled" "enabled"))
              [button {:variant variant :type btype :size size
                       :disabled? disabled? :label "Label" :on-click noop}]])))

;; ---------------------------------------------------------------- button-group
(def ^:private bg-items
  [{:id "one" :label "One" :on-click noop}
   {:id "two" :label "Two" :on-click noop}
   {:id "three" :label "Three" :on-click noop}])

(defn- button-group-section []
  (section "button-group"
           ^{:key "plain"}
           [instance "button-group-plain"
            [button-group {:items bg-items}]]
           ^{:key "active"}
           [instance "button-group-active"
            [button-group {:items bg-items :active-item "two"}]]
           ^{:key "disabled"}
           [instance "button-group-disabled"
            [button-group {:items bg-items :disabled? true}]]))

;; ---------------------------------------------------------------- card
(def ^:private carousel-items
  [{:title "Slide 1" :content "First slide content"}
   {:title "Slide 2" :content "Second slide content"}])

(defn- card-section []
  (section "card"
           (concat
            [^{:key "text"}
             [instance "card-text"
              [card {:type :text :content "Some text card content."}]]
             ^{:key "childs"}
             [instance "card-childs"
              [card {:type :childs} [:p "Child paragraph"]]]
             ^{:key "carousel"}
             [instance "card-carousel"
              [:div {:style {:width "260px" :height "120px"}}
               [card {:type :carousel :items carousel-items :auto-slide? false}]]]]
            (for [orientation [:horizontal :vertical]
                  icon-position [:start :end]
                  disabled? [false true]]
              ^{:key (str "btn" orientation icon-position disabled?)}
              [instance (str "card-button-" (name orientation) "-" (name icon-position)
                             "-" (if disabled? "disabled" "enabled"))
               [card {:type :button
                      :title "Card title"
                      :content "Card content"
                      :icon :cog
                      :orientation orientation
                      :icon-position icon-position
                      :disabled? disabled?
                      :on-click noop}]]))))

;; ---------------------------------------------------------------- checkbox
(defn- checkbox-section []
  (section "checkbox"
           (for [box-position [:left :right]
                 as-toggle? [false true]
                 checked? [false true]
                 disabled? [false true]]
             (let [id (str "checkbox-" (name box-position) "-"
                           (if as-toggle? "toggle" "box") "-"
                           (if checked? "on" "off") "-"
                           (if disabled? "disabled" "enabled"))]
               ^{:key id}
               [instance id
                [checkbox {:id id
                           :box-position box-position
                           :as-toggle? as-toggle?
                           :checked? checked?
                           :disabled? disabled?
                           :label "Label"
                           :on-change noop}]]))))

;; ---------------------------------------------------------------- collapsible-list
(defn- collapsible-list-section []
  (section "collapsible-list"
           ^{:key "cl"}
           [instance "collapsible-list-default"
            [:div {:style {:width "280px" :height "160px"}}
             [collapsible-list
              {:items [{:id "p1" :label "Parent 1"}
                       {:id "p2" :label "Parent 2"}]
               :default-open-id "p1"
               :collapse-items-fn (fn [pid]
                                    [{:id (str pid "-c1") :label "Child 1"}
                                     {:id (str pid "-c2") :label "Child 2"}])
               :on-click noop}]]]))

;; ---------------------------------------------------------------- input-field
(defn- input-value [type]
  (case type
    :number 42
    :color "#3366ff"
    "Value"))

(defn- input-field-section []
  (section "input-field"
           (for [type [:text :number :color :password]
                 disabled? [false true]
                 compact? [false true]]
             (let [id (str "input-field-" (name type) "-"
                           (if disabled? "disabled" "enabled") "-"
                           (if compact? "compact" "regular"))]
               ^{:key id}
               [instance id
                [input-field {:id id
                              :type type
                              :label "Label"
                              :placeholder "placeholder"
                              :value (input-value type)
                              :disabled? disabled?
                              :compact? compact?
                              :on-change noop
                              :on-blur noop}]]))))

;; ---------------------------------------------------------------- textarea
(defn- textarea-section []
  (section "textarea"
           (for [borderless? [false true]
                 disabled? [false true]]
             (let [id (str "textarea-"
                           (if borderless? "borderless" "bordered") "-"
                           (if disabled? "disabled" "enabled"))]
               ^{:key id}
               [instance id
                [textarea {:id id
                           :label "Label"
                           :placeholder "placeholder"
                           :value "Text content"
                           :borderless? borderless?
                           :disabled? disabled?
                           :on-change noop}]]))))

;; ---------------------------------------------------------------- radio
;; :label-variant :img is skipped: it requires an image :src, which introduces
;; async image loading (nondeterministic paint). Only :simple is rendered.
(defn- radio-section []
  (section "radio"
           (for [strong? [false true]
                 checked? [false true]
                 disabled? [false true]]
             (let [id (str "radio-"
                           (if strong? "strong" "plain") "-"
                           (if checked? "on" "off") "-"
                           (if disabled? "disabled" "enabled"))]
               ^{:key id}
               [instance id
                [radio (cond-> {:id id
                                :name "radio-group"
                                :label "Label"
                                :aria-label "aria"
                                :checked? checked?
                                :disabled? disabled?
                                :on-change noop}
                         strong? (assoc :strong-label "Strong"))]]))))

;; ---------------------------------------------------------------- select
(def ^:private select-options
  [{:label "Alpha" :value "a"}
   {:label "Beta" :value "b"}
   {:label "Gamma" :value "c"}])

(defn- select-values [is-multi?]
  (if is-multi?
    (vec (take 2 select-options))
    (first select-options)))

(def ^:private select-grouped-options
  [{:label "Group A" :options [{:label "Alpha" :value "a"}
                               {:label "Beta" :value "b"}]}
   {:label "Group B" :options [{:label "Gamma" :value "c"}]}])

(defn- select-extra
  "Static-render-safe single-flag variants: grouped options, caption, hint,
   start-icon, mark-invalid? (with a selection not present in options)."
  [suffix params]
  (let [id (str "select-" suffix)]
    ^{:key id}
    [instance id
     [:div {:style {:width "220px"}}
      [select (merge {:options select-options
                      :values (first select-options)
                      :aria-label "aria"
                      :label "Label"
                      :on-change noop}
                     params)]]]))

(defn- select-section []
  (section "select"
           (concat
            (for [is-multi? [false true]
                  is-searchable? [false true]
                  disabled? [false true]]
              (let [id (str "select-"
                            (if is-multi? "multi" "single") "-"
                            (if is-searchable? "search" "nosearch") "-"
                            (if disabled? "disabled" "enabled"))]
                ^{:key id}
                [instance id
                 [:div {:style {:width "220px"}}
                  [select {:options select-options
                           :values (select-values is-multi?)
                           :aria-label "aria"
                           :label "Label"
                           :is-multi? is-multi?
                           :is-searchable? is-searchable?
                           :disabled? disabled?
                           :on-change noop}]]]))
            [(select-extra "grouped" {:options select-grouped-options
                                      :is-grouped? true})
             (select-extra "caption" {:caption "Caption text"})
             (select-extra "hint" {:hint "Hint text"})
             (select-extra "start-icon" {:start-icon :search})
             (select-extra "invalid" {:values {:label "Zed" :value "z"}
                                      :mark-invalid? true})])))

;; ---------------------------------------------------------------- hint
(defn- hint-section []
  (section "hint"
           (for [variant [:default :info :warning :error]
                 with-title? [false true]]
             (let [id (str "hint-" (name variant) "-"
                           (if with-title? "titled" "plain"))]
               ^{:key id}
               [instance id
                [hint (cond-> {:variant variant
                               :content "Hint content"}
                        with-title? (assoc :title "Hint title"))]]))))

;; ---------------------------------------------------------------- tooltip
;; The tooltip popup only appears on hover (manual); the static capture covers
;; the wrapped trigger element only.
(defn- tooltip-section []
  (section "tooltip"
           ^{:key "t-span"}
           [instance "tooltip-span"
            [tooltip {:text "Tooltip text" :direction :up}
             [:span "Hover me"]]]
           ^{:key "t-chip"}
           [instance "tooltip-chip"
            [tooltip {:text "Tooltip text" :direction :down}
             [chip {:label "Chip" :variant :primary}]]]
           ^{:key "t-button"}
           [instance "tooltip-button"
            [tooltip {:text "Tooltip text" :direction :right}
             [:span [button {:label "Button" :on-click noop}]]]]))

;; ---------------------------------------------------------------- tabs (static)
;; No ui_base component wraps tabs styling (owned directly by woco/tabs.cljs
;; and its direct-builder consumer sites); render the DOM shape as plain
;; hiccup with those classes so the gate still covers it. Structure derived
;; from the real usage sites:
;; - plugins/frontend/de/explorama/frontend/woco/tabs.cljs (.tabs__navigation
;;   .app-tabs > .tab[.active] > div.label; icon spans inside tabs)
;; - plugins/frontend/de/explorama/frontend/configuration/views/data_management
;;   /overview.cljs and 4 other direct-builder sites (.tabs__navigation
;;   (full-width, now a plain "grow" on each .tab) > .tab[.active] with text)
;; - .scrollable/.scroll-button/.tabs shape from _tabs.scss selectors only
;;   (no live usage site found in plugins/bundles — candidate-dead, kept here
;;   only for gate parity; classes below are this static section's own,
;;   duplicated from the (now-deleted) sheet like the other 3 variants).
;; `tabs__navigation`/`tab`/`active` are kept as literal DOM classes to match
;; real markup (base/_themes.scss's forced-colors block selects them
;; directly — see woco/tabs.cljs's migration comment); app-tabs/full-width/
;; scrollable/scroll-button/disabled have no such dependency and are fully
;; replaced by utility classes.
(def ^:private tabs-navigation-class
  "flex flex-row z-1 bg-(--bg) shadow-md")
(def ^:private tabs-navigation-app-tabs-class
  "max-w-[calc((100%/2)-68px-44px)] overflow-hidden")
(def ^:private tab-base-class
  "group flex items-center justify-center gap-1 py-2 px-4 text-center font-bold [transition:background-color_.1s_ease,color_.1s_ease,box-shadow_.25s_ease]")
(def ^:private tab-default-class
  "text-(--text-secondary) cursor-pointer hover:bg-(--bg-hover) hover:text-(--link)")
(def ^:private tab-active-class
  "active text-(--text) bg-(--bg) cursor-default shadow-[inset_0_-2px_0_0_var(--text)]")
(def ^:private tab-full-width-class "grow")
(def ^:private tab-icon-default-class "[transition:background-color_.1s_ease] bg-(--icon-secondary) group-hover:bg-(--link)")
(def ^:private tab-icon-active-class "[transition:background-color_.1s_ease] bg-(--icon)")
(def ^:private scroll-button-base-class
  "flex items-center justify-center p-2 cursor-pointer [transition:background-color_.1s_ease]")
(def ^:private scroll-button-enabled-class "group hover:bg-(--bg-hover)")
(def ^:private scroll-button-disabled-class "pointer-events-none")
(def ^:private scroll-button-icon-enabled-class
  "[transition:background-color_.1s_ease] bg-(--icon-secondary) group-hover:bg-(--icon)")
(def ^:private scroll-button-icon-disabled-class
  "[transition:background-color_.1s_ease] bg-(--icon-disabled)")
(def ^:private scrollable-tabs-inner-class "flex grow flex-row overflow-x-hidden scroll-smooth")
(def ^:private scrollable-tab-extra-class "min-w-max")

(defn- tab-el-class [active? & [full-width?]]
  (str (when full-width? (str tab-full-width-class " "))
       tab-base-class " "
       (if active? tab-active-class tab-default-class)))

(defn- tab-el [label active? & [icon? full-width?]]
  [:div.tab {:class (tab-el-class active? full-width?)}
   (when icon? [:span.icon-chevron-down {:class (if active?
                                                   tab-icon-active-class
                                                   tab-icon-default-class)}])
   [:div.label label]])

(defn- tabs-section []
  (section "tabs"
           ^{:key "plain"}
           [instance "tabs-static-plain"
            [:div.tabs__navigation {:class tabs-navigation-class}
             [tab-el "Tab one" true true]
             [tab-el "Tab two" false true]
             [tab-el "Tab three" false]]]
           ^{:key "app-tabs"}
           [instance "tabs-static-app-tabs"
            [:div {:style {:width "420px"}}
             [:div.tabs__navigation {:class (str tabs-navigation-class " " tabs-navigation-app-tabs-class)}
              [tab-el "Workspace" true]
              [tab-el "Project" false]]]]
           ^{:key "full-width"}
           [instance "tabs-static-full-width"
            [:div {:style {:width "420px"}}
             [:div.tabs__navigation {:class tabs-navigation-class}
              [tab-el "Topics" true false true]
              [tab-el "Datasources" false false true]]]]
           ^{:key "scrollable"}
           [instance "tabs-static-scrollable"
            [:div {:style {:width "420px"}}
             [:div.tabs__navigation {:class tabs-navigation-class}
              [:div {:class (str scroll-button-base-class " " scroll-button-enabled-class)}
               [:span.icon-chevron-down {:class scroll-button-icon-enabled-class}]]
              [:div {:class scrollable-tabs-inner-class}
               [:div.tab {:class (str scrollable-tab-extra-class " " (tab-el-class true))}
                [:div.label "Tab one"]]
               [:div.tab {:class (str scrollable-tab-extra-class " " (tab-el-class false))}
                [:div.label "Tab two"]]
               [:div.tab {:class (str scrollable-tab-extra-class " " (tab-el-class false))}
                [:div.label "Tab three"]]]
              [:div {:class (str scroll-button-base-class " " scroll-button-disabled-class)}
               [:span.icon-chevron-down {:class scroll-button-icon-disabled-class}]]]]]))

(def ^:private sections
  [["chip" chip-section]
   ["button" button-section]
   ["button-group" button-group-section]
   ["card" card-section]
   ["checkbox" checkbox-section]
   ["collapsible-list" collapsible-list-section]
   ["input-field" input-field-section]
   ["textarea" textarea-section]
   ["radio" radio-section]
   ["select" select-section]
   ["hint" hint-section]
   ["tooltip" tooltip-section]
   ["tabs" tabs-section]])

(defn- only-filter []
  ;; ?only=chip,button renders just those sections (debug aid). No param = all.
  (let [q (.-search js/location)
        m (re-find #"[?&]only=([^&]+)" q)]
    (when m
      (set (str/split (js/decodeURIComponent (second m)) #",")))))

(defn- app []
  (let [only (only-filter)]
    (into [:div#harness-root]
          (for [[nm f] sections
                :when (or (nil? only) (contains? only nm))]
            ^{:key nm} [f]))))

(defn- serialize-computed-styles
  "Returns the JSON-stringified computed-style dump of every node under
   #harness-root, as a plain string (no DOM write). Used both for the settle
   loop's snapshot comparisons and for the final write."
  []
  (let [nodes (.querySelectorAll js/document "#harness-root *")
        ;; Node key: <data-harness>|<tag>|<index within that data-harness
        ;; subtree (document order)>. Using a per-subtree relative index (not
        ;; the global DOM index) keeps every key stable when the catalog grows:
        ;; new sections/instances only add new keys instead of shifting all
        ;; downstream ones.
        counters (atom {})
        entries (for [i (range (.-length nodes))
                      :let [n (aget nodes i)
                            cs (js/getComputedStyle n)
                            harness-id (or (some-> (.closest n "[data-harness]")
                                                   (.getAttribute "data-harness"))
                                           "")
                            local-idx (get (swap! counters update harness-id
                                                  (fnil inc -1))
                                           harness-id)]]
                  [(str harness-id "|" (.-tagName n) "|" local-idx)
                   (into {} (for [j (range (.-length cs))
                                  :let [prop (.item cs j)]]
                              [prop (.getPropertyValue cs prop)]))])]
    (js/JSON.stringify (clj->js (into {} entries)))))

(defn- write-pre! [id text]
  (let [pre (.createElement js/document "pre")]
    (set! (.-id pre) id)
    (set! (.-textContent pre) text)
    (.appendChild (.-body js/document) pre)))

;; Settle loop: a single-shot capture at a fixed delay can race Tailwind's
;; transition utilities (e.g. the 150ms button/tab hover transitions) if a
;; capture happens to run while one is still interpolating, serializing
;; mid-transition color/box-shadow values into the dump. Instead, poll the
;; serialized styles string every `settle-interval-ms` and only write
;; #computed-styles once two consecutive snapshots are byte-identical, which
;; proves no transition/layout is still in flight. `harness_capture.sh`'s
;; dump-dom step waits on the presence of #computed-styles, so this is safe as
;; long as we honor that contract: the pre must not exist until settled.
(def ^:private settle-initial-delay-ms 500)
(def ^:private settle-interval-ms 250)
(def ^:private settle-max-attempts 20) ;; ~5s after the initial delay

(defn- settle-loop! [attempt prev-snapshot]
  (let [snapshot (serialize-computed-styles)]
    (cond
      (= snapshot prev-snapshot)
      (write-pre! "computed-styles" snapshot)

      (>= attempt settle-max-attempts)
      (do
        (.error js/console
                (str "ui-base-harness: styles did not settle after "
                     settle-max-attempts " attempts; capturing unsettled state"))
        (write-pre! "settle-warning" "UNSETTLED")
        (write-pre! "computed-styles" snapshot))

      :else
      (js/setTimeout #(settle-loop! (inc attempt) snapshot) settle-interval-ms))))

(defn ^:export init []
  ;; ui_base logs an error for every keyword aria-label unless a translation
  ;; function is registered; supply an identity one (keyword -> name string).
  (ui-subs/set-translation-fn (fn [word-key] (atom (name word-key))))
  (rdom/render [app] (.getElementById js/document "app"))
  ;; wait a tick for reagent to flush, then start the settle loop
  (js/setTimeout #(settle-loop! 0 nil) settle-initial-delay-ms))

;; Auto-run at load time. The :simple bundle is a single self-contained script
;; placed after <div id="app"> in the body, so the namespace evaluates
;; synchronously once #app exists -- no reliance on the exported global
;; surviving the webpack module wrapper.
(defonce ^:private started
  (do (init) true))
