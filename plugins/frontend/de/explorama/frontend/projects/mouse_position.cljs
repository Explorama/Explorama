(ns de.explorama.frontend.projects.mouse-position
  (:require "crypto-js"
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.tubes :as tubes]
            [de.explorama.frontend.projects.path :as pp]
            [de.explorama.frontend.projects.tracks :as track]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [react-dom :as react-dom]
            [reagent.core :as r]
            [taoensso.timbre :refer [debug]]))

(def cursor-size 25)
(def text-padding 3.5)
(def font-size "0.75rem")
(defonce ^:private mouse-positions (r/atom {}))
(defonce ^:private page->workspace-fn (r/atom nil))
(defonce ^:private workspace->page-fn (r/atom nil))

(defonce ^:private canvas-text-measure (js/document.createElement "canvas"))

(aset (.getContext canvas-text-measure "2d")
      "font" (str font-size " Open Sans"))

(re-frame/reg-sub
 ::loaded-project
 (fn [db _]
   (get-in db [:projects :loaded-project])))

(re-frame/reg-event-db
 ::toggle-cursors
 (fn [db]
   (update-in db pp/show-cursors (fnil not true))))

(re-frame/reg-sub
 ::show-cursors?
 (fn [db]
   (get-in db pp/show-cursors true)))

(re-frame/reg-event-fx
 ::register-position-tracker
 (fn [_ _]
   {::track/register
    {:id ::workspace-position
     :subscription (fi/call-api :workspace-position-sub-vec)
     :event-fn (fn [{wsp-zoom :z wsp-x :x wsp-y :y
                     :as wsp-pos}]
                 (reset! page->workspace-fn
                         (partial fi/call-api :page->workspace-db-get
                                  wsp-x wsp-y wsp-zoom))
                 (reset! workspace->page-fn
                         (fn [coords]
                           (fi/call-api :workspace-position->page-position-fn
                                        wsp-pos
                                        coords
                                        true)))
                 nil)}}))

(re-frame/reg-event-fx
 ::dispose-position-tracker
 (fn [_ _]
   (reset! page->workspace-fn nil)
   (reset! workspace->page-fn nil)
   {::track/dispose
    {:id ::workspace-position}}))

(defn- handle-mouse-move [event]
  (let [mouse-position [(aget event "pageX")
                        (aget event "pageY")]
        ws-mouse-position (when-let [page->workspace-fn @page->workspace-fn]
                            (page->workspace-fn mouse-position))]
    (when ws-mouse-position
      (tubes/dispatch-to-server [:de.explorama.frontend.projects.mouse-position/update ws-mouse-position]))))

(defn add-mouse-handler []
  (debug "Add mouse move handler")
  (re-frame/dispatch [::register-position-tracker])
  (.addEventListener js/document "mousemove" handle-mouse-move))

(defn remove-mouse-handler []
  (debug "Remove mouse move handler")
  (re-frame/dispatch [::dispose-position-tracker])
  (.removeEventListener js/document "mousemove" handle-mouse-move))

(defn receive-mouse-position-update [username mouse-pos]
  (swap! mouse-positions
         assoc
         username
         mouse-pos))

(defn remove-mouse-position [username]
  (swap! mouse-positions dissoc username))

(re-frame/reg-event-fx
 ws-api/receive-mouse-position-update
 (fn [_ [_ username mouse-pos]]
   (receive-mouse-position-update username mouse-pos)
   {}))

(re-frame/reg-event-fx
 ws-api/remove-mouse-pos
 (fn [_ [_ username]]
   (remove-mouse-position username)
   {}))

(defn reset-mouse-positions []
  (reset! mouse-positions {}))

(defn- portal-comp [props & childs]
  (r/create-class
   {:display-name "context-menu portal"
    :reagent-render
    (fn [{:keys [z-index]} & childs]
      (let [z-index (val-or-deref z-index)]
        (apply conj
               [:div {:style {:position :absolute
                              :left 0
                              :top 0
                              :z-index z-index
                              :width "100%"
                              :height "100%"
                              :pointer-events :none}}]
               childs)))}))

(defn- portal [props childs]
  (let [portal-target-elem js/document.body]
    (react-dom/createPortal
     (r/as-element
      [portal-comp props childs])
     portal-target-elem)))

(defn username-to-color [username]
  (let [md5-str (-> (js/CryptoJS.MD5 username)
                    (.toString js/CryptoJS.enc.Hex))
        r (-> md5-str (subs 0 2) (js/parseInt 16))
        g (-> md5-str (subs 2 4) (js/parseInt 16))
        b (-> md5-str (subs 4 6) (js/parseInt 16))
        brightness (+ (* r 0.299) (* g 0.587) (* b 0.114))]
    [(str "rgba(" r "," g "," b ", 0.6)")
     (if (< 180 brightness)
       "#000000"
       "#FFFFFF")]))
(defn- calc-degree [{:keys [width height left top]}
                    x y]
  (cond
    (and (< y top)
         (> x width)) [315 :left :bottom]
    (and (> y height)
         (> x width)) [45 :left :top]
    (and (> y height)
         (< x left)) [135 :right :top]
    (and (< y top)
         (< x left)) [225 :right :bottom]
    (< y top) [270 :center :bottom]
    (> x width) [0 :left :center]
    (> y height) [90 :center :top]
    (< x left) [180 :right :center]))

(defn- calc-ws-position [x y]
  (let [{:keys [width height left top]
         :as ws-rect} (fi/call-api :workspace-rect-fn)
        degree (calc-degree ws-rect x y)
        cursor-bottom-padding 5
        x (cond
            (< left x (- width cursor-size)) x
            (> left x) left
            (< (- width cursor-size) x) (- width cursor-size))
        y (cond
            (< top y height) y
            (>= top y) top
            (< height y) (+ height (- cursor-size cursor-bottom-padding)))]
    {:x x
     :y y
     :width width
     :degree degree}))

(defn- get-text-metrics [text text-padding]
  (let [canvas-context (.getContext canvas-text-measure "2d")
        metrics (.measureText canvas-context text)]
    {:width (+ (aget metrics "width") (* text-padding 2))
     :height (+ (js/parseInt (aget canvas-context "font")) (* text-padding 2))}))

(defn- calc-center-offset [name-text-width cursor-size x width]
  (let [half-text-width (/ (- name-text-width cursor-size) 2)]
    (cond
      (<= x half-text-width) (- (- half-text-width
                                   (- half-text-width x)))
      (> (+ x half-text-width cursor-size) width) (- (+ (- half-text-width
                                                           (- width (+ x half-text-width)))
                                                        cursor-size))
      :else (- half-text-width))))

(defn- display-pos [username pos]
  (let [workspace->page-fn @workspace->page-fn
        [x y] (cond-> pos
                workspace->page-fn
                (workspace->page-fn))
        name-display @(fi/call-api :name-for-user-sub username)
        [color font-color] (username-to-color username)
        {:keys [x y width]
         [degree left-position top-position] :degree} (calc-ws-position x y)
        {name-text-width :width
         name-text-height :height} (get-text-metrics name-display text-padding)]
    [:div {:style {:position :absolute
                   :left x
                   :top y}}
     (if degree
       [:span {:style {:transform (str "rotate(" degree "deg)")
                       :display :block}}
        [icon {:icon :indicator-arrow
               :custom-color "#808080"
               :size cursor-size}]]
       [:svg#Layer_1
        {:style {:width cursor-size
                 :height cursor-size}
         :y "0px"
         :x "0px"
         :xmlSpace "preserve"
         :xmlns "http://www.w3.org/2000/svg"
         :xmlnsXlink "http://www.w3.org/1999/xlink"
         :version "1.1"
         :viewBox "1064.7701 445.5539 419.8101 717.0565"
         :enable-background "new 1064.7701 445.5539 419.8101 717.0565"}
        [:polygon
         {:fill "grey"
          :stroke "black"
          :stroke-width "5"
          :points
          "1283.1857,1127.3097 1406.1421,1077.6322 1314.2406,850.1678 1463.913,852.7823 1093.4828,480.8547   1085.4374,1005.6964 1191.2842,899.8454 "}]])
     [:div {:style {:white-space :nowrap
                    :line-height 1
                    :border-radius 2
                    :position :absolute
                    :background color
                    :padding text-padding
                    :font-size font-size
                    :color font-color
                    :left (cond
                            (and (= left-position :left)
                                 (= top-position :center)) (- -5 name-text-width)
                            (and (= left-position :right)
                                 (= top-position :center)) 30
                            :else (calc-center-offset name-text-width cursor-size x width))
                    :top (case top-position
                           :bottom 30
                           :top -30
                           :center (- (/ cursor-size 2)
                                      (/ name-text-height 2))
                           nil)}}
      name-display]]))

(defn view []
  (when @(re-frame/subscribe [::show-cursors?])
    (let [mouse-pos @mouse-positions]
      [error-boundary
       [portal
        {:z-index 50000}
        (for [[username pos] mouse-pos]
          (with-meta
            [display-pos username pos]
            {:key (str "user-mouse-position-" username)}))]])))