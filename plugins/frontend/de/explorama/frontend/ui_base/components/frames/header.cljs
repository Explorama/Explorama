(ns de.explorama.frontend.ui-base.components.frames.header
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [add-class export-ignore-class]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [input-field button]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [reagent.core :as reagent]
            [taoensso.timbre :refer [warn]]))

(def parameter-definition
  {:title {:type [:string :derefable]
           :required true
           :desc "The fixed prefix of the frame title"}
   :user-title {:type [:string :derefable]
                :required false
                :desc "The user editable suffix of the frame title"}
   :icon {:type [:keyword :string]
          :desc "An additional icon in frame title. It depends on css classes of window, where the icon is visible"}
   :icon-params {:type :map
                 :desc "Parameters for icon-component"}
   :extra-props {:type :map
                 :desc "Properties which will be added to parent container. For example it is useful for setting the drag properties"}
   :on-info {:type :function
             :desc "Will be triggered when clicked on info tool-item"}
   :on-maximize {:type :function
                 :desc "Will be triggered when maximize tool-item is clicked"}
   :on-minimize {:type :function
                 :desc "Will be triggered when minimize tool-item is clicked"}
   :on-normalize {:type :function
                  :desc "Will be triggered when normalize tool-item is clicked"}
   :on-close {:type :function
              :desc "Will be triggered when close tool-item is clicked"}
   :on-copy {:type :function
             :desc "Will be triggered when copy tool-item is clicked"}
   :on-filter {:type :function
               :desc "Will be triggered when filter tool-item is clicked"}
   :filter-extra-props {:type :map
                        :desc "Extra-Properties for the filter tool-item."}
   :title-read-only? {:type [:boolean :derefable]
                      :desc "If true the title cannot be edited by double clicking."}
   :comment-desc {:type :map
                  :definition :header-item
                  :desc "Props for the comment button, see the extra item definition below"}
   :burger-desc  {:type :map
                  :definition :header-item
                  :desc "Props for the burger menu, see the extra item definition below"}
   :on-comment {:type :function
                :desc "Will be triggered when comment tool-item is clicked"}
   :is-commented? {:type [:boolean :derefable]
                   :desc "Defines if the current frame has a comment."}
   :on-title-set {:type [:function]
                  :desc "The function triggered when the user sets the title. If not set, the title is not editable."}
   :title-validator {:type [:function :regexp]
                     :desc "A predicate or a regexp deciding if the title is valid. The regexp has to match any part for the title to be accepted. If not set to a function or a regexp, any title is accepted."}
   :is-maximized? {:type [:boolean :derefable]
                   :desc "For automatically managing which icons are visible"}
   :is-minimized? {:type [:boolean :derefable]
                   :desc "For automatically managing which icons are visible"}
   :extra-items {:type :vector
                 :definition :header-item
                 :desc "Additional items which should be visible at beginning of toolitems. Example <Item>: {:tooltip \"Save\" :icon :save :on-click #(js/alert \"save..\")}"}
   :double-click-resizes? {:type [:boolean :derefable]
                           :desc "Enable maximizing/normalizing on double click. Ignored if title editing is enabled."}
   :info-tooltip {:type [:string :derefable]
                  :desc "Tooltip for info icon"}
   :minimize-tooltip {:type [:string :derefable]
                      :desc "Tooltip for minimize icon"}
   :maximize-tooltip {:type [:string :derefable]
                      :desc "Tooltip for maximize icon"}
   :normalize-tooltip {:type [:string :derefable]
                       :desc "Tooltip for normalize icon"}
   :close-tooltip {:type [:string :derefable]
                   :desc "Tooltip for close icon"}
   :copy-tooltip {:type [:string :derefable]
                  :desc "Tooltip for copy icon"}
   :filter-tooltip {:type [:string :derefable]
                    :desc "Tooltip for filter icon"}
   :comment-tooltip {:type [:string :derefable]
                     :desc "Tooltip for comment icon"}})
(def extra-item-definition
  {:icon {:type [:keyword :string]
          :desc "An additional icon in frame title. It depends on css classes of window, where the icon is visible"}
   :tooltip {:type [:string :derefable]
             :desc "Tooltip for icon"}
   :on-click {:type :function
              :desc "Will be triggered when icon is clicked"}
   :extra-props {:type :map
                 :desc "Parameters for the parent component, which is a ui-base button"}
   :icon-extra-props {:type :map
                      :desc "Parameters for icon component"}})
(def sub-definitions {:header-item extra-item-definition})
(def specification (parameters->malli parameter-definition sub-definitions))
(def default-parameters {:extra-props {}
                         :close-tooltip "Close"
                         :info-tooltip "Info"
                         :maximize-tooltip "Maximize"
                         :minimize-tooltip "Minimize"
                         :normalize-tooltip "Normalize"
                         :comment-tooltip "Comment"
                         :filter-tooltip "Filter"
                         :copy-tooltip "Copy"})

(def window-header-class "header")
(def window-title-class "title")
(def window-custom-title-class "custom_title")
(def window-tools-class "tools")
(def window-tools-divider-class "divider")

(defn- toolitem [{icon-key :icon
                  tooltip-text :tooltip
                  :keys [on-click extra-props]}]
  (let [tooltip-text (val-or-deref tooltip-text)]
    [button (cond-> {:on-click #(when (fn? on-click)
                                  (on-click %))
                     :disabled-event-bubble? true
                     :title tooltip-text
                     :variant :tertiary
                     :start-icon icon-key}
              (map? extra-props) (merge extra-props))]))

(defn- add-extra-items [acc extra-items]
  (reduce (fn [acc item-desc]
            (conj acc (with-meta
                        [toolitem item-desc]
                        {:key (str ::c item-desc)})))
          acc
          extra-items))

(def separator
  [:span {:class window-tools-divider-class}])

(defn- frame-icons [{:keys [on-maximize on-minimize on-normalize on-close
                            is-maximized? is-minimized? extra-items
                            minimize-tooltip maximize-tooltip normalize-tooltip close-tooltip
                            on-copy copy-tooltip
                            on-filter filter-tooltip filter-extra-props
                            on-comment comment-tooltip is-commented?
                            on-info info-tooltip on-burger
                            comment-desc filter-desc copy-desc info-desc
                            burger-desc info-burger]}]
  (when (or on-close on-maximize on-minimize on-normalize on-info)
    (let [maximize-desc
          {:icon :win-maximize
           :tooltip maximize-tooltip
           :on-click on-maximize}
          minimize-desc
          {:icon :win-minimize
           :tooltip minimize-tooltip
           :on-click on-minimize}
          normalize-desc
          {:icon :win-multiple
           :tooltip normalize-tooltip
           :on-click on-normalize}
          comment-desc* (or comment-desc
                            {:icon :file-text
                             :tooltip comment-tooltip
                             :on-click on-comment})
          filter-desc* (or filter-desc
                           {:icon :filter
                            :extra-props filter-extra-props
                            :tooltip filter-tooltip
                            :on-click on-filter})
          copy-desc* (or copy-desc
                         {:icon :copy
                          :tooltip copy-tooltip
                          :on-click on-copy})
          info-desc* (or info-desc
                         {:icon :info-square
                          :on-click on-info
                          :tooltip info-tooltip})
          burger-desc* (or burger-desc
                           {:icon :menu
                            :on-click on-burger
                            :tooltip info-burger})]
      (cond-> [:div {:class [export-ignore-class window-tools-class]
                     :on-double-click (fn [e]
                                        (.preventDefault e)
                                        (.stopPropagation e))}]
        (or on-info
            info-desc)
        (conj [toolitem info-desc*])
        (and extra-items (vector? extra-items))
        (add-extra-items extra-items)
        (or on-burger
            burger-desc)
        (conj [toolitem burger-desc*])
        (or on-filter
            filter-desc)
        (conj [toolitem filter-desc*])
        (or on-comment
            comment-desc)
        (conj [toolitem comment-desc*])
        (or on-copy
            copy-desc)
        (conj [toolitem copy-desc*])

        (and (or on-minimize on-close)
             (or extra-items on-filter on-comment on-copy))
        (conj separator)

        (and on-minimize
             on-normalize
             is-maximized?)
        (conj [toolitem minimize-desc]
              [toolitem normalize-desc])
        (and on-normalize
             on-maximize
             is-minimized?)
        (conj [toolitem normalize-desc]
              [toolitem maximize-desc])
        (and on-minimize
             on-maximize
             (not is-minimized?)
             (not is-maximized?))
        (conj [toolitem minimize-desc]
              [toolitem maximize-desc])
        on-close
        (conj [toolitem {:icon :close
                         :tooltip close-tooltip
                         :on-click on-close}])))))

(defn ^:export header [params]
  (warn "The ui-base header component is deprecated - please do not use it")
  (let [editing? (reagent/atom false)
        current-user-title (-> params :user-title val-or-deref reagent/atom)]
    (fn [params]
      (let [params (merge default-parameters params)]
        [error-boundary {:validate-fn #(validate "header" specification params)}
         (let [{:keys [title user-title
                       extra-props icon-params
                       is-maximized? is-minimized?
                       on-maximize on-normalize
                       double-click-resizes?
                       on-title-set title-validator
                       is-commented? title-read-only?]
                ico :icon} params
               title (val-or-deref title)
               user-title (val-or-deref user-title)
               double-click-resizes? (val-or-deref double-click-resizes?)
               is-maximized? (val-or-deref is-maximized?)
               is-minimized? (val-or-deref is-minimized?)
               is-commented? (val-or-deref is-commented?)
               validator-pred
               (cond (regexp? title-validator) (partial re-find title-validator)
                     (fn? title-validator) title-validator
                     :else (constantly true))
               finish-editing #(do (reset! editing? false)
                                   (on-title-set @current-user-title))]
           [:div (cond-> (or extra-props {})
                   :always
                   (update :class add-class window-header-class)
                   (and on-title-set (not @editing?))
                   (assoc :on-double-click #(when (or (nil? title-read-only?)
                                                      (and title-read-only?
                                                           (not @title-read-only?)))
                                              (reset! editing? true)))
                   (and double-click-resizes? on-maximize on-normalize
                        (not on-title-set))
                   (assoc :on-double-click (if (or is-maximized? is-minimized?)
                                             #(on-normalize %)
                                             #(on-maximize %))))
            (when ico
              [icon (cond-> {:icon ico
                             :extra-class "frame-icon"}
                      (map? icon-params)
                      (merge icon-params))])
            [:span {:class window-title-class
                    :title (str title user-title)}
             title]
            [:span {:class window-custom-title-class}
             (if @editing?
               [:input {:value @current-user-title
                        :auto-focus true
                        :on-blur finish-editing
                        :on-mouse-down #(.stopPropagation %)
                        :on-key-down
                        (fn [e]
                          (case (.-which e)
                            13 (finish-editing)
                            27 (do (reset! editing? false)
                                   (reset! current-user-title user-title))
                            nil))
                        :on-change
                        #(let [elem (aget % "nativeEvent" "target")
                               value (aget elem "value")]
                           (when (validator-pred value)
                             (reset! current-user-title value)))}]
               (reset! current-user-title user-title))]
            [frame-icons (assoc params
                                :is-maximized? is-maximized?
                                :is-minimized? is-minimized?
                                :is-commented? is-commented?)]])]))))
