(ns de.explorama.frontend.reporting.views.text-module
  (:require cljsjs.quill
            [re-frame.core :refer [dispatch reg-event-db reg-sub subscribe]]
            [reagent.core :as r]
            [de.explorama.frontend.common.i18n :as i18n]
            [cuerdas.core :as cuerdas]
            [clojure.string :refer [replace]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.reporting.config :as config]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [taoensso.timbre :refer [error]]))

;workaround for handling maxlength, because there is no property in quill for it
(defn handle-max-length [instance callback-fn delta _ source]
  (try
    (when (= source "user")
      (let [^number text-length (.getLength instance)]
        (when (> (dec text-length)
                 config/report-text-module-max-length)
          (let [ops (js->clj (array-seq (aget delta "ops")))
                ^number insert-idx (-> (first ops)
                                       (get "retain" 0))
                ^number insert-length (reduce (fn [^number r o]
                                                (let [insert (get o "insert")
                                                      ^number insert-length (if (seq insert)
                                                                              (count insert)
                                                                              0)]
                                                  (seq insert)
                                                  (+ r insert-length)))
                                              0
                                              ops)
                ^number after-insert-length (max 0 (- text-length insert-idx insert-length 1))
                ^number text-left (- config/report-text-module-max-length insert-idx after-insert-length)
                ^number delete-idx (+ insert-idx text-left)
                ^number delete-length (- insert-length text-left)]

            (when (and (>= delete-idx 0)
                       (>= delete-length 0))
              (.deleteText instance delete-idx delete-length))))))
    (catch :default e
      (error "Handle max-length failed" e)))
  (when (fn? callback-fn)
    (callback-fn)))

(defn- paste-handler [e]
  (let [clipboard-data (or (aget e "clipboardData")
                           (aget js/window "clipboardData"))
        raw-text (->
                  clipboard-data
                  (.getData "text/html")
                  (replace #"<[^>]*>" ""))]
    (when (and (string? raw-text)
               (>= (count raw-text)
                   config/report-text-module-max-length))
      (fi/call-api :notify-event-dispatch
                   {:type :w
                    :vertical :reporting
                    :category {:misc :report-text-module}
                    :message (cuerdas/format @(subscribe [::i18n/translate :max-paste-characters-message])
                                             {:num (i18n/localized-number config/report-text-module-max-length)})})
      (.stopPropagation e)
      (.preventDefault e))))

(defn default-editor-config-toolbar [id]
  {:theme "snow"
   :formats ["bold" "italic" "underline" "strike" "blockquote" "list" "color" "background" "size"]
   :modules {:history {:maxStack 40}
             :toolbar (str "#" id)}})

(def default-editor-config-no-toolbar
  {:theme "snow"
   :formats ["bold" "italic" "underline" "strike" "blockquote" "list" "color" "background" "size"]
   :modules {:history {:maxStack 40}
             :toolbar false}})

(defn- setup-quill [instance parents tile-idx {:keys [content]}]
  (let [{:keys [toolbar no-toolbar]} @parents
        {:keys [body header]} toolbar
        toolbar-instance (js/Quill. (str "#" (aget body "id"))
                                    (clj->js (default-editor-config-toolbar (aget header "id"))))
        no-toolbar-instance (js/Quill. (str "#" (aget no-toolbar "id"))
                                       (clj->js default-editor-config-no-toolbar))]
    ;;TODO r1/reporting optimize We should use just one instance here and toggle of the toolbar instead of an extra instance and sync the content
    (.on toolbar-instance "text-change" (partial handle-max-length
                                                 toolbar-instance
                                                 #(dispatch [::edit-content tile-idx toolbar-instance])))
    (.on no-toolbar-instance "text-change" (partial handle-max-length
                                                    no-toolbar-instance
                                                    #(dispatch [::edit-content tile-idx no-toolbar-instance])))
    (.setContents toolbar-instance (.parse js/JSON content))
    (.setContents no-toolbar-instance (.parse js/JSON content))
    (.addEventListener (aget toolbar-instance "root")
                       "paste"
                       paste-handler)
    (.addEventListener (aget no-toolbar-instance "root")
                       "paste"
                       paste-handler)
    (reset! instance {:toolbar toolbar-instance
                      :no-toolbar no-toolbar-instance})))

(reg-event-db
 ::edit-content
 (fn [db [_ tile-idx instance]]
   (let [content (when instance (.stringify js/JSON (.getContents instance)))
         blank? (when (seq content)
                  (re-matches  #"^\{\"ops\":\[\{\"insert\":\"[ (\\t)(\\n)(\\v)(\\f)(\\r)]+\"\}\]\}$" content))]
     (-> db
         (update-in (dr-path/creation-module-desc tile-idx) assoc-in [:state :blank?] blank?)
         (update-in (dr-path/creation-module-desc tile-idx) assoc-in [:state :content] content)))))

(reg-sub
 ::content
 (fn [db [_ tile-idx]]
   (get-in db (conj (dr-path/creation-module-desc tile-idx) :state :content))))

(reg-event-db
 ::keep-open
 (fn [db [_ tile-idx]]
   (assoc-in db dr-path/creation-selected-text-module tile-idx)))

(reg-sub
 ::keep-open
 (fn [db]
   (get-in db dr-path/creation-selected-text-module)))

(defn- header-buttons []
  [:<>
   [:button.ql-bold]
   [:button.ql-italic]
   [:button.ql-underline]
   [:button.ql-strike]
   [:button.ql-blockquote]
   [:button.ql-list {:value "ordered"}]
   [:button.ql-list {:value "bullet"}]
   [:select.ql-size
    [:option {:value "small"}]
    [:option]
    [:option {:value "large"}]
    [:option {:value "huge"}]]
   [:select.ql-color]
   [:select.ql-background]])

(defn text-module [tile-idx state focus]
  (let [parents (r/atom {})
        focus?  (r/atom focus)
        instances (r/atom nil)
        content (subscribe [::content tile-idx])]
    (r/create-class
     {:display-name (str "text-module-" tile-idx)
      :component-did-mount (fn []
                             (setup-quill instances parents tile-idx state))
      :component-did-update (fn [this argv]
                              (when (not= argv (r/argv this))
                                (.setContents (get @instances :no-toolbar) (.parse js/JSON @content))))
      :reagent-render (fn [tile-idx _]
                        (let [keep-open? (= tile-idx @(subscribe [::keep-open]))
                              show-toolbar? (or keep-open? @focus?)]
                          [:<>
                           ;; Quill instance with designated header div
                           [:div {:on-mouse-leave #(do (reset! focus? false)
                                                       (when content
                                                         (.setContents (get @instances :no-toolbar)
                                                                       (.parse js/JSON @content))))
                                  :on-click #(dispatch [::keep-open tile-idx])
                                  :style {:display (if show-toolbar? "flex" "none") ;TODO r1/css create a class or use one
                                          :flex-flow "column"
                                          :width "100%"
                                          :height "250px"}}
                            [:div {:ref #(swap! parents assoc-in [:toolbar :header] %)
                                   :id (str "text-module-editor-toolbar-header-" tile-idx)
                                   :style {:flex "0 1 auto"
                                           :width "100%"}}
                             [header-buttons]]
                            [:div {:ref #(swap! parents assoc-in [:toolbar :body] %)
                                   :id (str "text-module-editor-toolbar-body-" tile-idx)
                                   :style {:flex "1 1 auto"
                                           :width "100%"}}]]
                           ;; Quill instance without header
                           [:div {:on-mouse-enter #(do (reset! focus? true)
                                                       (when content
                                                         (.setContents (get @instances :toolbar)
                                                                       (.parse js/JSON @content))))
                                  :style {:display (if show-toolbar? "none" "block")
                                          :width "100%"
                                          :height "250px"}}
                            [:div {:ref #(swap! parents assoc :no-toolbar %)
                                   :id (str "text-module-editor-no-toolbar-" tile-idx)
                                   :style {:width "100%"}}]]]))})))

;;;  ------  Read only view --------

(def default-read-only-config
  {:theme "bubble"
   :readOnly true
   :formats ["bold" "italic" "underline" "strike" "blockquote" "list" "color" "background" "size"]
   :modules {:history {:maxStack 40}
             :toolbar false}})

(defn- read-only-setup-quill [parent module-desc]
  (let [content (get module-desc :content)
        instance (js/Quill. (str "#" (aget @parent "id"))
                            (clj->js default-read-only-config))]
    (.setContents instance (.parse js/JSON content))))

(defn read-only-text-module [tile-idx module-desc]
  (let [parent (r/atom nil)]
    (r/create-class
     {:display-name (str "ro-text-module-" tile-idx)
      :component-did-mount (fn []
                             (read-only-setup-quill parent module-desc))
      :reagent-render (fn [tile-idx _]
                        [:div {:ref #(reset! parent %)
                               :id (str "ro-text-module-text-" tile-idx)
                               :style {:height "auto"
                                       :min-height "100%"
                                       :width "100%"}}])})))