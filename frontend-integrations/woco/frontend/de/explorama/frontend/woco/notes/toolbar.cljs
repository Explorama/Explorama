(ns de.explorama.frontend.woco.notes.toolbar
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.util :refer [handle-param]]
            [de.explorama.frontend.woco.notes.api :refer [clean-format format-text note-background]]
            [de.explorama.frontend.woco.notes.states :refer [get-bg-color get-formatting
                                                             in-edit-mode? in-hover-mode?]]))

(defn- find-icon-color [current-hex color-palette]
  (some (fn [{:keys [icon-color hex brightness]}]
          (when (= current-hex hex)
            {:color icon-color
             :brightness brightness}))
        color-palette))

(defn- icon-props [op color-palette frame-id]
  (let [{:keys [color brightness]}
        (-> (cond
              (= "note" op)
              (or @(get-bg-color frame-id)
                  config/note-default-bgcolor)
              (= "color" op)
              (or @(get-formatting frame-id op)
                  config/note-default-font-color)
              (= "background" op)
              (or @(get-formatting frame-id op)
                  config/note-default-bgcolor)
              :else
              @(get-formatting frame-id op))
            (find-icon-color color-palette))]
    {:color color
     :brightness brightness
     :color-important? true}))

(defn- color-items [icon op f color-palette]
  (mapv (fn [{:keys [hex label icon-color brightness]}]
          (cond-> {:op op
                   :value hex
                   :label label
                   :icon-props {:color icon-color
                                :brightness brightness
                                :color-important? true}
                   :f f}
            icon (assoc :icon icon)))
        color-palette))

(def ^:private toolbar-ops
  [{:icon :color-circle
    :icon-props (partial icon-props "note" config/bg-color-values)
    :group-label :note-background-color-group
    :items (color-items :mosaic-circle nil note-background config/bg-color-values)
    :context-menu-extra-classes "select-option-list grid grid-cols-3-fr"}
   {:divider? true}
   {:icon :bold
    :op "bold"
    :label :note-bold
    :active? #(get-formatting % "bold")
    :f format-text}
   {:icon :italics
    :op "italic"
    :label :note-italic
    :active? #(get-formatting % "italic")
    :f format-text}
   {:icon :underlined
    :op "underline"
    :label :note-unterline
    :active? #(get-formatting % "underline")
    :f format-text}
   {:icon :strikethrough
    :op "strike"
    :label :note-strike
    :active? #(get-formatting % "strike")
    :f format-text}
   {:icon :text-color
    :icon-props (partial icon-props "color" config/font-color-values)
    :group-label :note-font-color-group
    :items (color-items :mosaic-circle "color" format-text config/font-color-values)
    :context-menu-extra-classes "select-option-list grid grid-cols-3-fr"}
   {:icon :highlight-color
    :icon-props (partial icon-props "background" config/bg-color-values)
    :group-label :note-highlight-color-group
    :items (color-items :mosaic-circle "background" format-text config/bg-color-values)
    :context-menu-extra-classes "select-option-list grid grid-cols-3-fr"}
   {:icon :font-size
    :group-label :note-size-group
    :items [{:icon :custom-font-size :icon-props {:size 12} :op "size" :value "small" :label :note-font-small :f format-text}
            {:icon :custom-font-size :icon-props {:size 16} :op "size" :value false :label :note-font-normal :f format-text}
            {:icon :custom-font-size :icon-props {:size 24} :op "size" :value "large" :label :note-font-large :f format-text}
            {:icon :custom-font-size :icon-props {:size 32} :op "size" :value "huge" :label :note-font-huge :f format-text}]}
   {:icon (fn [frame-id]
            (case @(get-formatting frame-id "align")
              "center" :align-center
              "right" :align-right
              "justify" :align-justified
              :align-left))
    :group-label :note-align-group
    :items [{:icon :align-left :op "align" :value false :label :note-align-left :f format-text}
            {:icon :align-center :op "align" :value "center" :label :note-align-center :f format-text}
            {:icon :align-right :op "align" :value "right" :label :note-align-right :f format-text}
            {:icon :align-justified :op "align" :value "justify" :label :note-align-justify :f format-text}]}
   {:icon (fn [frame-id]
            (case @(get-formatting frame-id "list")
              "ordered" :list-numbered
              :list-bulleted))
    :group-label :note-list-group
    :items [{:icon :list-numbered :op "list" :value "ordered" :label :note-numbering :f format-text}
            {:icon :list-bulleted :op "list" :value "bullet" :label :note-bulletpoints :f format-text}]}
   {:divider? true}
   {:icon :remove-formatting
    :op "clean" :label :note-remove-formatting :f clean-format}])

(defn- ql-simple-item [frame-id
                       {:keys [divider? active?
                               group-label items f op label value icon-props context-menu-extra-classes]
                        item-icon :icon}]
  (cond
    divider? :divider
    (and group-label items)
    (cond-> {:label group-label
             :icon (handle-param item-icon frame-id)
             :on-click (fn [_]
                         {:items (mapv (partial ql-simple-item frame-id)
                                       items)
                          :extra-class context-menu-extra-classes})

             :id (str ::group group-label)
             :active? (handle-param active? frame-id)}
      icon-props (assoc :icon-props icon-props))
    :else
    (cond-> {:label (or label value op)
             :icon item-icon
             :on-click (if value
                         #(f frame-id op value)
                         #(f frame-id op))
             :id (str ::op op value)
             :active? (handle-param active? frame-id)}
      icon-props (assoc :icon-props icon-props))))

(defn get-items [frame-id]
  (if-let [_read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                     {:frame-id frame-id})]
    []
    (mapv (partial ql-simple-item frame-id)
          toolbar-ops)))

(def toolbar-impl
  {:show? (fn [frame-id show-flag]
            (or show-flag
                (and (not show-flag)
                     (in-edit-mode? frame-id))))
   :apply-extra-style? (fn [frame-id]
                         (not (in-edit-mode? frame-id)))
   :on-duplicate-fn (fn [frame-id]
                      (re-frame/dispatch [:de.explorama.frontend.woco.notes.core/duplicate-note-frame frame-id]))
   :items get-items})