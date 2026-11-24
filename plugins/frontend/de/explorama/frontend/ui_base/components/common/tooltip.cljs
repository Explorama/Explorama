(ns de.explorama.frontend.ui-base.components.common.tooltip
  (:require [clojure.string :as clj-str]
            [de.explorama.frontend.ui-base.components.common.error-boundary :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.floating :as floating]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [react-dom :as react-dom]
            [reagent.core :as r]))

;; `tooltip-wrapper`, `tooltip-popup` and `tooltip-arrow` are literal markers:
;; sheets this component does not own select on them structurally
;; (temp_domain, projects_domain, dashboards_domain, the input-group rules in
;; tailwind.css, and the forced-colors block in themes.css).
(def ^:private wrapper-class
  "tooltip-wrapper [&_button:disabled]:pointer-events-none")

(def ^:private popup-class
  (str "tooltip-popup fixed z-[30000] min-w-[100px] w-max max-w-[50em] "
       "px-[1em] py-[0.5em] text-xs text-center text-white bg-gray-900 "
       "rounded-xs shadow-sm drop-shadow-[0_0_1px_var(--tooltip-shadow-color)]"))

(def ^:private arrow-class
  "tooltip-arrow absolute z-[30001] w-0 h-0 border-solid border-transparent")

(def ^:private arrow-side-class
  {:top    "border-t-[10px] border-x-[10px] border-b-0 border-t-gray-900"
   :bottom "border-b-[10px] border-x-[10px] border-t-0 border-b-gray-900"
   :left   "border-l-[10px] border-y-[10px] border-r-0 border-l-gray-900"
   :right  "border-r-[10px] border-y-[10px] border-l-0 border-r-gray-900"})

(def ^:private arrow-static-side
  {:top :bottom :bottom :top :left :right :right :left})

(def ^:private arrow-nudge
  {:top "translateY(-1px)" :bottom "translateY(1px)"
   :left "translateX(-1px)" :right "translateX(1px)"})

(def parameter-definition
  {:text {:type [:string :component :derefable]
          :required true
          :desc "Content of the tooltip"}
   :direction {:type :keyword
               :characteristics [:up :down :left :right]
               :desc "Direction where the tooltip opens"}
   :color {:type [:string :keyword]
           :desc "Font color of tooltip text"}
   :tag-name {:type :string
              :desc "tag name of surrounding html tag"}
   :hover-delay {:type :number
                 :desc "The number of milliseconds to determine hover intent"}
   :mouse-out-delay {:type :number
                     :desc "The number of milliseconds to determine hover-end intent"}
   :use-hover? {:type :boolean
                :desc "Whether hover listeners are attached to the trigger; false means the tooltip can never be shown"}
   :distance {:type :number
              :desc "The distance from the tooltip to the target"}
   :extra-class {:type :string
                 :desc "Css class added to the rendered wrapper"}
   :extra-style {:type :map
                 :desc "Style overrides for the target wrapper"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:direction :up
                         :tag-name "div"
                         :hover-delay 500
                         :mouse-out-delay 200
                         :distance floating/default-distance
                         :use-hover? true})

(defn- text->content [text]
  (let [lines (when (string? text) (clj-str/split-lines text))]
    (if (< 1 (count lines))
      (reduce (fn [acc line] (conj acc line [:br])) [:<>] lines)
      text)))

(defn- blank-text? [text]
  (or (nil? text)
      (and (string? text) (clj-str/blank? text))))

(defn- popup [params]
  (r/with-let [pos (r/atom nil)
               els (atom {})
               latest (atom params)
               teardown (atom nil)
               stop! (fn []
                       (when-let [t @teardown]
                         (t)
                         (reset! teardown nil)))
               start! (fn []
                        (let [{:keys [popup-el arrow-el]} @els
                              {:keys [trigger-el]} @latest]
                          (when (and trigger-el popup-el)
                            (stop!)
                            (reset! teardown
                                    (floating/auto-update!
                                     trigger-el popup-el
                                     (fn []
                                       (let [{:keys [direction distance]} @latest]
                                         (-> (floating/compute-position!
                                              trigger-el popup-el
                                              {:placement (floating/direction->placement direction)
                                               :distance distance
                                               :arrow-el arrow-el})
                                             (.then #(reset! pos %))
                                             (.catch (fn [err]
                                                       (js/console.warn "tooltip: compute-position! failed" err)))))))))))
               ;; Ref callbacks must keep a stable identity: an inline fn is a
               ;; new value on every render, which makes React detach and
               ;; re-attach the ref and restart autoUpdate in a loop.
               arrow-ref (fn [el] (swap! els assoc :arrow-el el))
               popup-ref (fn [el]
                           (swap! els assoc :popup-el el)
                           (if el (start!) (stop!)))]
    (reset! latest params)
    (let [{:keys [text color direction]} params
          {:keys [x y placement arrow-x arrow-y]} @pos
          side (floating/placement->side (or placement (floating/direction->placement direction)))]
      [:div {:class popup-class
             :ref popup-ref
             :style (cond-> {:left (str (or x 0) "px")
                             :top (str (or y 0) "px")}
                      (nil? @pos) (assoc :visibility "hidden")
                      color (assoc :color (if (keyword? color) (name color) color)))}
       (text->content text)
       [:div {:class (str arrow-class " " (arrow-side-class side))
              :ref arrow-ref
              :style (cond-> {(arrow-static-side side) "-10px"
                              :transform (arrow-nudge side)}
                       arrow-x (assoc :left (str arrow-x "px"))
                       arrow-y (assoc :top (str arrow-y "px")))}]])
    (finally
      (stop!))))

(defn- tooltip- [params childs]
  (r/with-let [open? (r/atom false)
               trigger (atom nil)
               timers (atom {})
               trigger-ref (fn [el] (reset! trigger el))
               clear! (fn [k]
                        (when-let [t (get @timers k)]
                          (js/clearTimeout t)
                          (swap! timers dissoc k)))
               schedule! (fn [k delay f]
                           (clear! k)
                           (swap! timers assoc k
                                  (js/setTimeout (fn []
                                                   (swap! timers dissoc k)
                                                   (f))
                                                 delay)))]
    (let [{:keys [text direction color tag-name use-hover?
                  hover-delay mouse-out-delay distance
                  extra-class extra-style]} params
          text (val-or-deref text)]
      (apply conj
             [(keyword (or tag-name "div"))
              (cond-> {:class (cond-> wrapper-class
                                extra-class (str " " extra-class))
                       :ref trigger-ref}
                extra-style (assoc :style extra-style)
                use-hover? (assoc :on-mouse-enter (fn []
                                                    (clear! :hide)
                                                    (schedule! :show hover-delay #(reset! open? true)))
                                  :on-mouse-leave (fn []
                                                    (clear! :show)
                                                    (schedule! :hide mouse-out-delay #(reset! open? false)))))
              (when @open?
                (react-dom/createPortal
                 (r/as-element [popup {:text text
                                       :color color
                                       :direction direction
                                       :distance distance
                                       :trigger-el @trigger}])
                 js/document.body))]
             childs))
    (finally
      (doseq [t (vals @timers)]
        (js/clearTimeout t)))))

(defn ^:export tooltip [params & childs]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "tooltip" specification params)}
     (let [text (val-or-deref (:text params))]
       (cond
         (and (blank-text? text) (empty? childs)) [:<>]
         (blank-text? text) (apply conj [:<>] childs)
         :else [tooltip- params childs]))]))
