(ns de.explorama.frontend.ui-base.components.formular.card
  (:require [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref translate-label]]
            [taoensso.timbre :refer-macros [error]]
            [reagent.core :as r]))

(def parameter-definition
  {:type {:type :keyword
          :required true
          :characteristics [:text :button :carousel :childs]
          :desc "Type of the card. Due to the type different params are supported. For type :childs you can use the card component as parent component"}
   :items {:type [:derefable :vector]
           :desc "[:carousel] - Items for carousel. Datastructure must be a vector. A single item can be a component, string or map of :title and :content"}
   :auto-slide? {:type :boolean
                 :desc "[:carousel] - If true the items will be switch automatically related to defined :slide-direction and :slide-timeout-ms"}
   :slide-direction {:type :keyword
                     :characteristics [:left :right]
                     :desc "[:carousel] - Direction for auto-slide"}
   :aria-label-next {:type [:keyword :string :derefable]
                     :desc "[:carousel] - Label for the next button"}
   :aria-label-previous {:type [:keyword :string :derefable]
                         :desc "[:carousel] - Label for the previous butto"}
   :slide-timeout-ms {:type :number
                      :desc "[:carousel] - Timeout in ms for auto-slide"}
   :extra-class {:type [:vector :string]
                 :desc "[:childs] - Add some extra classes to the parent"}
   :extra-style {:type :map
                 :desc "[:childs] - Add some extra styling to the parent"}
   :content {:type [:derefable :string :component]
             :desc "[:button :text] - card content"}
   :show-divider? {:type :boolean
                   :desc "[:button] - If true divider is displayed between icon and title/content"}
   :title {:type [:derefable :string :component]
           :desc "[:button] - card title"}
   :aria-label {:type [:derefable :string :keyword]
                :desc "[:button] - The aria label, which is used if there is no title/content of type string. If both are stings, this parameter takes priority."}
   :orientation {:type :keyword
                 :characteristics [:horizontal :vertical]
                 :desc "[:button] - Defines the content orientation."}
   :full-height? {:type :boolean
                  :desc "If true uses 100% of available height. Currently only works for carousel type."}
   :disabled? {:type [:derefable :boolean]
               :required false
               :desc "[:button] - Only for :type :button - If true, the card will be grayed out and the on-click will not be triggered "}
   :icon {:type [:string :keyword]
          :desc "[:button] - Set an icon. Its recommanded to use a keyword. If its a string it has to be an css class, if its a keyword the css-class will get from icon-collection. See icon-collection to which are provided"}
   :icon-position {:type :keyword
                   :characteristics [:start :end]
                   :desc "[:button] - Defines the position of icon"}
   :icon-params {:type :map
                 :desc "[:button] - Parameters for icon-component"}
   :on-click {:type :function
              :default-fn-str "(fn [event])"
              :desc "[:button] - Will be triggered, if user clicks on card"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:type :text
                         :orientation :horizontal
                         :icon-params {:size :xxl}
                         :aria-label-next :aria-carousel-next
                         :aria-label-previous :aria-carousel-previous
                         :show-divider? true
                         :icon-position :start
                         :full-height? false
                         :auto-slide? false
                         :slide-direction :right
                         :slide-timeout-ms 10000
                         :disabled? false})

(defn- divider [{:keys [show-divider? title content]
                 icon-key :icon}]
  (when (and icon-key show-divider? (or title content))
    [:span.divider]))

(defn- icon-impl [{:keys [icon-params]
                   icon-key :icon}]
  (when icon-key
    [icon (assoc icon-params :icon icon-key)]))

(defn- childs-impl [{:keys [extra-style extra-class]} childs]
  (apply conj
         [:div.card (cond-> {}
                      extra-class (assoc :class extra-class)
                      extra-style (assoc :style extra-style))]
         childs))

(defn- text-impl [{:keys [content]}]
  (let [content (val-or-deref content)]
    [:div.card
     (if (string? content)
       [:p content]
       content)]))

(defn- button-impl [{:keys [title content disabled? orientation
                            icon-position aria-label
                            on-click]
                     :as params}]
  (let [disabled? (val-or-deref disabled?)
        title (val-or-deref title)
        aria-label (translate-label aria-label)
        content (val-or-deref content)
        vertical? (= orientation :vertical)
        show-content? (or title content)
        icon-start-position? (= icon-position :start)]
    [:button.btn-card {:type "button"
                       :class (cond-> []
                                disabled? (conj "disabled")
                                vertical? (conj "vertical"))
                       :aria-label (cond
                                     aria-label aria-label
                                     (string? title) title
                                     (string? content) content
                                     :else "")
                       :on-click (fn [e]
                                   (when (and (not disabled?)
                                              (fn? on-click))
                                     (on-click e)))}
     (when icon-start-position?
       [:<>
        [icon-impl params]
        [divider (assoc params :title title :content content)]])

     (when show-content?
       [(if vertical?
          :<>
          :div.column)
        (if (string? title)
          [:h4 title]
          title)
        content])
     (when-not icon-start-position?
       [:<>
        [divider (assoc params :title title :content content)]
        [icon-impl params]])]))

(defn- carousel-impl [{:keys [auto-slide? slide-direction slide-timeout-ms full-height?]}]
  (let [slide-idx (r/atom 0)
        slide-timer (r/atom nil)
        next-slide-fn #(swap! slide-idx (fn [idx] (inc idx)))
        prev-slide-fn #(swap! slide-idx (fn [idx] (dec idx)))
        auto-fn (if (= slide-direction :right)
                  next-slide-fn
                  prev-slide-fn)
        reset-timer #(swap! slide-timer (fn [timer]
                                          (js/clearInterval timer)
                                          (js/setInterval auto-fn slide-timeout-ms)))]
    (r/create-class
     {:component-did-mount #(when auto-slide? (reset-timer))
      :component-will-unmount #(swap! slide-timer js/clearInterval)
      :reagent-render
      (fn [{:keys [items aria-label-next aria-label-previous]}]
        (let [items (val-or-deref items)
              item-count (count items)
              current-idx (mod @slide-idx item-count)
              {:keys [title content] :as item} (get items current-idx)]
          [:div.btn-card.justify-between {:class (cond-> []
                                                   (map? item)
                                                   (conj "col-start-2" "col-end-9")
                                                   full-height? (conj "h-full"))}
           [button {:variant :tertiary
                    :aria-label (translate-label aria-label-previous)
                    :start-icon :prev
                    :on-click (fn [_]
                                (reset-timer)
                                (prev-slide-fn))}]
           (cond
             (string? item)
             [:h4 item]
             (map? item)
             [:div.text-center
              [:h4.mb-8 title]
              [:p.text-center content]]
             :else item)
           [button {:variant :tertiary
                    :aria-label (translate-label aria-label-next)
                    :start-icon :next
                    :on-click (fn [_]
                                (reset-timer)
                                (next-slide-fn))}]]))})))

(defn ^:export card [params & childs]
  (let [{card-type :type :as params}
        (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "card" specification params)}
     (case card-type
       :text [text-impl params]
       :childs [childs-impl params childs]
       :button [button-impl params]
       :carousel [carousel-impl params]
       (do (error "Card type not supported")
           "Error"))]))
