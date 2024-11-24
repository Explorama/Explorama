(ns de.explorama.frontend.ui-base.components.common.tooltip
  (:require [de.explorama.frontend.ui-base.components.common.error-boundary :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [clojure.string :as clj-str]
            [reagent.core :as r]
            ["react-tooltip-lite"]))

(def parameter-definition
  {:text {:type [:string :component :derefable]
          :required true
          :desc "Content of the tooltip"}
   :direction {:type :keyword
               :characteristics [:up :down :left :right]
               :desc "Direction where the tooltip opens"}
   :alignment {:type :keyword
               :characteristics [:start :middle :end]
               :desc "Alignment of the tooltip where it opens"}
   :color {:type :string
           :desc "Font color of tooltip text"}
   :tag-name {:type :string
              :desc "tag name of surrounding html tag"}
   :hover-delay {:type :number
                 :desc "The number of milliseconds to determine hover intent"}
   :mouse-out-delay {:type :number
                     :desc "The number of milliseconds to determine hover-end intent"}
   :on-toggle {:type :function
               :desc "If passed, this is called when the visibility of the tooltip changes"}
   :event-on  {:type :keyword
               :characteristics [:click :dbl-click :mouse-out :mouse-over
                                 :mouse-move :mouse-up :mouse-down :mouse-wheel
                                 :key-down :key-press :key-up :blur :change]
               :desc "Event on which the tooltip will be shown"}
   :event-off  {:type :keyword
                :characteristics [:click :dbl-click :mouse-out :mouse-over
                                  :mouse-move :mouse-up :mouse-down :mouse-wheel
                                  :key-down :key-press :key-up :blur :change]
                :desc "Event on which the tooltip will be hidden"}
   :event-toggle {:type :keyword
                  :characteristics [:click :dbl-click :mouse-out :mouse-over
                                    :mouse-move :mouse-up :mouse-down :mouse-wheel
                                    :key-down :key-press :key-up :blur :change]
                  :desc "Event on which the tooltip will be toggle"}
   :use-hover? {:type :boolean
                :desc "Whether to use hover to show/hide the tip"}
   :arrow? {:type :boolean
            :desc "Whether or not to have an arrow on the tooltip"}
   :arrow-size {:type :number
                :desc "Number in pixels of the size of the arrow"}
   :distance {:type :number
              :desc "The distance from the tooltip to the target"}
   :extra-class {:type :string
                 :desc "Css class added to the rendered wrapper"}
   :extra-style {:type :map
                 :desc "Style overrides for the target wrapper"}
   :is-open? {:type [:boolean :derefable]
              :desc "Overrides the open/close state for the tooltip."}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:direction :up
                         :alignment :middle
                         :tag-name "div"
                         :hover-delay 500
                         :mouse-out-delay 200
                         :arrow? true
                         :arrow-size 10
                         :use-hover? true})

(def tooltip-comp (r/adapt-react-class (aget js/ReactToolTipLite "default")))

(defn- keyword->event-name [k]
  (case k
    :click "onClick"
    :dbl-click "onDblClick"
    :mouse-out "onMouseOut"
    :mouse-over "onMouseOver"
    :mouse-move "onMouseMove"
    :mouse-up "onMouseUp"
    :mouse-down "onMouseDown"
    :mouse-wheel "onWheel"
    :key-down "onKeyDown"
    :key-press "onKeyPress"
    :key-up "onKeyUp"
    :blur "onBlur"
    :change "onChange"
    nil))

(defn ^:export tooltip [params & childs]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "tooltip" specification params)}
     (let [{:keys [text direction alignment
                   color use-hover?
                   on-toggle event-on event-off event-toggle
                   extra-class extra-style tag-name
                   hover-delay mouse-out-delay
                   arrow? arrow-size distance is-open?]}
           params
           text (val-or-deref text)
           event-on (keyword->event-name event-on)
           event-off (keyword->event-name event-off)
           event-toggle (keyword->event-name event-toggle)
           is-open? (val-or-deref is-open?)
           extra-class (if extra-class
                         (str "tooltip-wrapper " extra-class)
                         (str "tooltip-wrapper"))]
       (cond (and (empty? childs)
                  (or (nil? text)
                      (and (string? text)
                           (clj-str/blank? text))))
             [:<>]
             (and childs
                  (or (nil? text)
                      (and (string? text)
                           (clj-str/blank? text))))
             (apply conj [:<>] childs)
             :else
             (let [split-lines (when (string? text)
                                 (clj-str/split-lines text))]
               (apply conj
                      [tooltip-comp (cond-> {}
                                      text (assoc :content (cond
                                                             (and (string? text)
                                                                  split-lines
                                                                  (< 1 (count split-lines)))
                                                           ;; handle line breakes, because per css it's sometimes broken here
                                                             (r/as-element (reduce (fn [acc t]
                                                                                     (conj acc t [:br]))
                                                                                   [:<>]
                                                                                   split-lines))
                                                             :else text))
                                      direction (assoc :direction (cond-> direction
                                                                    (keyword? direction) (name)
                                                                    (and alignment (keyword? alignment))
                                                                    (str "-" (name alignment))))

                                      hover-delay (assoc :hover-delay hover-delay)
                                      mouse-out-delay (assoc :mouse-out-delay mouse-out-delay)
                                      extra-class (assoc :class-name extra-class)
                                      on-toggle (assoc :on-toggle on-toggle)
                                      event-on (assoc :event-on event-on)
                                      event-off (assoc :event-off event-off)
                                      event-toggle (assoc :event-toggle event-toggle)
                                      distance (assoc :distance distance)
                                      (boolean? use-hover?) (assoc :useHover use-hover?)
                                      (boolean? arrow?) (assoc :arror arrow?)
                                      (boolean? is-open?) (assoc :isOpen is-open?)
                                      arrow-size (assoc :arrow-size arrow-size)
                                      color (assoc :color color)
                                      tag-name (assoc :tag-name tag-name)
                                      extra-style (assoc :styles extra-style))]
                      childs))))]))