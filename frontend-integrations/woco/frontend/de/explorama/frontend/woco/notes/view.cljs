(ns de.explorama.frontend.woco.notes.view
  (:require [clojure.string :refer [includes? split replace]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [toolbar-ignore-class]]
            [de.explorama.frontend.ui-base.utils.interop :refer [safe-aget]]
            [cuerdas.core :as cuerdas]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [taoensso.timbre :refer [error]]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.notes.api :as notes-api :refer [update-formatting]]
            [de.explorama.frontend.woco.notes.states :refer [create-instance destroy-instance
                                                             get-bg-color get-instance get-text
                                                             in-edit-mode? in-hover-mode? notes-id set-edit-mode set-hover-mode]]
            [de.explorama.frontend.woco.workspace.states :as wws]))

(defn- handle-max-length [instance delta _ source]
  (try
    (let [text-length (.getLength instance)]
      (when (and (= source "user")
                 (> (dec text-length)
                    config/notes-max-length))
        (let [ops (js->clj (array-seq (aget delta "ops")))
              insert-idx (-> (first ops)
                             (get "retain" 0))
              insert-length (reduce (fn [r o]
                                      (let [insert (get o "insert")
                                            insert-length (if (seq insert)
                                                            (count insert)
                                                            0)]
                                        (seq insert)
                                        (+ r insert-length)))
                                    0
                                    ops)
              after-insert-length (max 0 (- text-length insert-idx insert-length 1))
              text-left (- config/notes-max-length insert-idx after-insert-length)
              delete-idx (+ insert-idx text-left)
              delete-length (- insert-length text-left)]

          (when (and (>= delete-idx 0)
                     (>= delete-length 0))
            (.deleteText instance delete-idx delete-length)))))
    (catch :default e
      (error "Handle max-length failed" e))))

(defn- check-blur [e frame-id]
  (let [target (safe-aget e "relatedTarget")
        target-id (when target (or (safe-aget target "id") ""))]
    (not (and target
              (includes?
               target-id
               (str frame-id))
              (not (includes?
                    target-id
                    "copy-label"))))))

(defn did-mount [frame-id read-only? bg-color]
  (let [project-loading? @(fi/call-api [:project-loading-sub])
        init-text @(get-text frame-id)
        instance (create-instance frame-id)]
    (when init-text
      (.setContents instance (.parse js/JSON init-text)))
    (.on instance "content-change" (partial handle-max-length instance))
    (.on instance "selection-change"
         (fn [range _ _]
           (when range
             (update-formatting instance frame-id))))

    (when-not read-only?
      (.addEventListener (aget instance "root")
                         "paste"
                         (fn [e]
                           (let [clipboard-data (or (aget e "clipboardData")
                                                    (aget js/window "clipboardData"))
                                 raw-text (->
                                           clipboard-data
                                           (.getData "text/html")
                                           (replace #"<[^>]*>" ""))]
                             (when (and (string? raw-text)
                                        (>= (count raw-text)
                                            config/notes-max-length))
                               (fi/call-api :notify-event-dispatch
                                            {:type :w
                                             :vertical :woco
                                             :category {:misc :notes}
                                             :message (cuerdas/format @(re-frame/subscribe [::i18n/translate :max-paste-characters-message])
                                                                      {:num (i18n/localized-number config/notes-max-length)})})
                               (.stopPropagation e)
                               (.preventDefault e)))))
      (.addEventListener (aget instance "root")
                         "blur"
                         (fn [e]
                           (if (check-blur e frame-id)
                             (do
                               (set-edit-mode nil)
                               (.blur instance)
                               (re-frame/dispatch [::notes-api/save-annotation frame-id nil nil true]))
                             (do
                               (.stopPropagation e)
                               (.preventDefault e))))))
    (.enable instance (not read-only?))
    (when (and (not project-loading?)
               (not read-only?))
      (.focus instance)
      (set-edit-mode frame-id))
    (re-frame/dispatch (fi/call-api :render-done-event-vec frame-id config/notes-vertical-str))))

(def ^:private ignore-focus-classes #{toolbar-ignore-class})

(defn note-panel [frame-id read-only? bg-color]
  (let [div-id (notes-id frame-id)
        id (config/frame-dom-id frame-id)
        on-focus (fn [e]
                   (when (= id (aget e "target" "id"))
                     (set-hover-mode frame-id)))
        on-focus-lost (fn [e]
                        (when (and (= id (aget e "target" "id"))
                                   (or (not (aget e "relatedTarget"))
                                       (and (not= js/document.body (aget e "relatedTarget" "parentNode"))
                                            (not (some ignore-focus-classes
                                                       (split (aget e "relatedTarget" "className") #" "))))))
                          (set-hover-mode nil)))]
    (r/create-class
     {:display-name "editor"
      :reagent-render (fn [frame-id read-only? bg-color]
                        [:div {:id div-id
                               :on-mouse-up (fn [ev]
                                              (when-not read-only?
                                                (let [instance (get-instance frame-id)]
                                                  (.focus @instance)
                                                  (set-edit-mode frame-id))))
                               :on-mouse-down (fn [ev]
                                                (when-not read-only?
                                                  (let [instance (get-instance frame-id)]
                                                    (.stopPropagation ev)
                                                    (when-not (in-edit-mode? frame-id)
                                                      (.preventDefault ev)
                                                      (.blur @instance)))))}])

      :component-did-mount (fn []
                             (when-let [dom-node (js/document.getElementById id)]
                               (.addEventListener dom-node "mouseenter" on-focus true)
                               (.addEventListener dom-node "mouseleave" on-focus-lost true))
                             (did-mount frame-id read-only? bg-color))
      :component-will-unmount (fn []
                                (when-let [dom-node (js/document.getElementById id)]
                                  (.removeEventListener dom-node "mouseenter" on-focus true)
                                  (.removeEventListener dom-node "mouseleave" on-focus-lost true))
                                (destroy-instance frame-id))})))

(defn view [_frame-id _props]
  (let [is-dragging (atom nil)]
    (fn [frame-id {:keys [drag-props ignore-child-interactions?]}]
      [error-boundary
       (let [read-only? @(fi/call-api [:interaction-mode :pending-read-only-sub?]
                                      {:frame-id frame-id})
             bg-color @(get-bg-color frame-id)
             {:keys [selected?]} @(fi/call-api :frame-sub frame-id)]
         ^{:key (str ::note-panel frame-id)}
         [:<>
          [:div {:style {:z-index 3
                         :position :absolute
                         :top 0
                         :right 0
                         :width 45}}
           (when (and (not read-only?)
                      (not ignore-child-interactions?)
                      (not selected?)
                      (not (@wws/temporary-selection frame-id))
                      (or (in-hover-mode? frame-id)
                          (in-edit-mode? frame-id)))
             [button {:on-click (fn [e]
                                  (.stopPropagation e)
                                  (.preventDefault e)
                                  (re-frame/dispatch (fi/call-api :frame-close-event-vec frame-id)))
                      :disabled? (and (not (in-hover-mode? frame-id))
                                      (not (in-edit-mode? frame-id)))
                      :disabled-event-bubble? true
                      :title @(re-frame/subscribe [::i18n/translate :close])
                      :start-icon :close
                      :variant :tertiary
                      :extra-class "note-remove"}])]
          [note-panel frame-id read-only? bg-color]
          (when (and (not read-only?)
                     (not (in-edit-mode? frame-id)))
            [:div (-> drag-props
                      (assoc :on-drag-start (fn [e]
                                              (when-let [drag-fn (:on-drag-start drag-props)]
                                                (drag-fn e)
                                                (reset! is-dragging true))))
                      (assoc :on-mouse-up (fn [e]
                                            (when-let [mouse-up-fn (:on-mouse-up drag-props)]
                                              (mouse-up-fn e))
                                            (when-not (or @is-dragging
                                                          (aget e "ctrlKey")
                                                          selected?
                                                          ignore-child-interactions?
                                                          (@wws/temporary-selection frame-id))
                                              (set-edit-mode frame-id)
                                              (let [instance (get-instance frame-id)]
                                                (.focus @instance)))
                                            (reset! is-dragging false)))
                      (assoc :style {:z-index 2
                                     :position :absolute
                                     :opacity 0
                                     :top 0
                                     :left 0
                                     :width "100%"
                                     :height "100%"}))])])])))