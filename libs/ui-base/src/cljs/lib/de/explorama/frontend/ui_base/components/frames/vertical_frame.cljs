(ns de.explorama.frontend.ui-base.components.frames.vertical-frame
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:ignore-child-events? {:type [:boolean :derefable]
                          :desc "If true all child-events will be ignored. Use :force-on-ignore to define events which should be fired in this case"}
   :force-on-ignore {:type :map
                     :desc "Dom-node properties which can be used to define event-handlers. This will be used when :ignore-child-events? is true for whole vertical-frame"}
   :propagation-overlay-z-index {:type :number
                                 :desc "z-index of hidden overlay. For propagating of events overlayer has to be in front of all childs"}
   :frame-class? {:type [:boolean :vector]
                  :desc "If true adds window-css class to parent node. If it is a vector, all entries + the window-css class will be added to parent node"}
   :extra-props {:type :map
                 :desc "Properties which will be added to parent container. For example it is useful for setting the drop properties"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:ignore-child-events? false
                         :propagation-overlay-z-index 1000
                         :frame-class? false})

(def explorama-window-class "frame")

(defn- add-css-classes [frame-class? css-classes]
  (cond (and (vector? css-classes)
             (true? frame-class?))
        (conj css-classes explorama-window-class)
        (and (vector? css-classes)
             (vector? frame-class?))
        (apply conj css-classes explorama-window-class frame-class?)
        (true? frame-class?)
        [explorama-window-class]
        (vector? frame-class?)
        (apply conj [explorama-window-class] frame-class?)
        :else css-classes))

(defn- event-overlay-container [{:keys [force-on-ignore propagation-overlay-z-index]}]
  [:div (update (or force-on-ignore {})
                :style
                merge
                {:height "100%"
                 :width "100%"
                 :opacity 0
                 :position :absolute
                 :top 0
                 :left 0
                 :z-index propagation-overlay-z-index})])

(defn ^:export vertical-frame [& childs]
  (let [params (first childs)
        childs (if (map? params) (rest childs) childs)
        params (if (map? params) params {})
        params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "vertical-frame" specification params)}
     (let [{:keys [frame-class? ignore-child-events? extra-props]}
           params
           ignore-child-events? (val-or-deref ignore-child-events?)]
       (cond-> (if (or extra-props frame-class?)
                 (apply conj
                        [:div (cond-> (or extra-props {})
                                frame-class?
                                (assoc :class (add-css-classes frame-class? (:class extra-props))))]
                        childs)
                 (apply conj [:<>] childs))
         ignore-child-events? (conj [event-overlay-container params])))]))