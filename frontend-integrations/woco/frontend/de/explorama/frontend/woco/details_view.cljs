(ns de.explorama.frontend.woco.details-view
  (:require [clojure.set :refer [difference]]
            [clojure.string :refer [index-of join]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button section]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon] :as comp-misc]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.info :as frame-info]
            [de.explorama.frontend.woco.frame.view.header :refer [full-title]]
            [de.explorama.frontend.woco.frame.view.product-tour :refer [preference-panning-desc]]
            [de.explorama.frontend.woco.path :as path]))

(defn details-view-frame-id
  "Generates an frame-id for event-logging, because an frame-id is necessary there"
  [db]
  {:frame-id "woco-details-view"
   :workspace-id (get-in db (path/workspace-id))
   :vertical "woco"})

(def compare-limit 4)
(def event-limit 50)
(def title-attribute "date")

(re-frame/reg-event-fx
 ::open-details-view
 (fn [{db :db} [_ add?]]
   (let [show? (get-in db path/show-details-view)
         sidebar? (or
                   (not add?)
                   (and add?
                        (not show?)))]
     {:fx [(when sidebar?
             [:dispatch (fi/call-api :sidebar-create-event-vec
                                     {:module "details-view-sidebar"
                                      :title [::i18n/translate :details-view-title]
                                      :id "details-view"
                                      :position :right
                                      :close-event [::switch]
                                      :width 25})])
           (when sidebar?
             [:dispatch [::switch]])]})))

(defn- gen-compare [old-compare new-event-id add? first-element?]
  (let [compare (or old-compare [])
        compare-size (count compare)
        compare (filterv #(not= % new-event-id)
                         (if (and add?
                                  (not first-element?)
                                  (= compare-limit compare-size))
                           (vec (pop compare))
                           compare))]
    (cond (not add?)
          compare
          (and (not-empty compare)
               first-element?
               (= compare-size 1))
          (assoc compare 0 new-event-id)
          (or (and (not first-element?)
                   (< compare-size compare-limit))
              (and first-element? (= 0 compare-size)))
          (conj compare new-event-id)
          :else
          compare)))

(re-frame/reg-sub
 ::events-details
 (fn [db [_ ids]]
   (cond-> (get-in db path/details-view-events)
     ids (select-keys ids))))

(re-frame/reg-event-fx
 ::switch
 (fn [{db :db} [_]]
   (let [updated-db (update-in db path/show-details-view not)
         show-details (get-in updated-db path/show-details-view)]
     {:db updated-db
      :fx [(when-not show-details
             [:dispatch [:de.explorama.frontend.woco.api.product-tour/next-step :woco-details-view :close]])]})))

(re-frame/reg-event-fx
 ::remove-all-events
 (fn [{db :db} [_ {:keys [no-event-logging?]}]]

   (let [events (get-in db path/details-view-events)]
     {:db (-> db
              (assoc-in path/details-view-compare-events [])
              (assoc-in path/details-view-events {}))
      :fx (conj (mapv
                 (fn [[event-id {:keys [source-frame-id di remove-event]}]]
                   [:dispatch (conj remove-event source-frame-id di event-id)])
                 events)
                (when-not no-event-logging?
                  [:dispatch [:de.explorama.frontend.woco.event-logging/log-event
                              (details-view-frame-id db)
                              "remove-all"
                              {}]]))})))

(re-frame/reg-event-fx
 ::remove-event
 (fn [{db :db} [_ frame-id remove-event-id]]
   (let [db (-> db
                (update-in path/details-view-compare-events #(gen-compare % remove-event-id false false))
                (update-in path/details-view-events dissoc remove-event-id))]
     {:db db
      :fx [(when (and (seq (get-in db path/details-view-events))
                      (empty? (get-in db path/details-view-compare-events)))
             [:dispatch [::add-to-comparison {:new-event-id (first (keys (get-in db path/details-view-events)))
                                              :frame-id frame-id}]])]})))

(re-frame/reg-sub
 ::event-details
 (fn [db [_ event-id]]
   (get-in db (path/event-details event-id))))

(re-frame/reg-sub
 ::newest-element
 (fn [db]
   (get-in db path/details-view-newest)))

(re-frame/reg-sub
 ::to-compare
 (fn [db]
   (get-in db path/details-view-compare-events [])))

(re-frame/reg-sub
 ::compare-attributes
 (fn [db]
   (let [compare-items (get-in db path/details-view-compare-events [])
         ignore-attributes #{"id"}]
     (-> (reduce (fn [r event-id]
                   (apply conj r (keys (get-in db (conj (path/event-details event-id)
                                                        :event-data)))))
                 #{}
                 compare-items)
         (difference ignore-attributes)
         (vec)
         (->> (sort-by #(cond (= "notes" %)
                              "0000000"
                              (= "annotation" %)
                              "0000001"
                              :else
                              (str %))))))))

(re-frame/reg-event-fx
 ::add-to-comparison
 (fn [{db :db} [_ {:keys [new-event-id first-element? new-compare frame-id no-event-logging?]}]]
   (let [db (if new-compare
              (assoc-in db path/details-view-compare-events new-compare)
              (update-in db path/details-view-compare-events #(gen-compare % new-event-id true first-element?)))
         new-compare (get-in db path/details-view-compare-events [])]
     {:db db
      :dispatch-n [(when-not no-event-logging?
                     [:de.explorama.frontend.woco.event-logging/log-event
                      (details-view-frame-id db)
                      "set-comparison"
                      new-compare])]})))

(re-frame/reg-event-fx
 ::remove-from-comparison
 (fn [{db :db} [_ {:keys [remove-event-id new-compare frame-id no-event-logging?]}]]
   (let [db (if new-compare
              (assoc-in db path/details-view-compare-events new-compare)
              (update-in db path/details-view-compare-events
                         (fn [old-compare]
                           (let [new-compare (gen-compare old-compare remove-event-id false false)]
                             (cond
                               (and new-compare (not-empty new-compare))
                               new-compare
                               (not-empty (get-in db path/details-view-events))
                               [(->> (get-in db path/details-view-events {})
                                     (sort-by #(get-in % [1 :event-data "date"]))
                                     (ffirst))]
                               :else [])))))
         new-compare (get-in db path/details-view-compare-events [])]
     {:db db
      :dispatch-n [(when-not no-event-logging?
                     [:de.explorama.frontend.woco.event-logging/log-event
                      (details-view-frame-id db)
                      "remove-from-comparison"
                      {:new-compare new-compare
                       :remove-event-id remove-event-id}])]})))

(re-frame/reg-sub
 ::event-source-groups
 (fn [db]
   (let [frames (->> (get-in db path/details-view-events {})
                     (vals)
                     (reduce (fn [acc {:keys [source-frame-id di]}]
                               (conj
                                acc
                                {:frame-id source-frame-id
                                 :published-frame-id (get-in db (path/frame-published-by source-frame-id))
                                 :di di}))
                             #{}))]
     (reduce (fn [r {:keys [frame-id di published-frame-id]}]
               (assoc r
                      frame-id
                      (when (= di (frame-info/api-value db frame-id :di))
                        (or (get-in db (path/frame-header-color frame-id))
                            (when published-frame-id
                              (get-in db (path/frame-header-color published-frame-id)))))))
             {}
             frames))))

(re-frame/reg-event-fx
 ::focus-event-in-frame
 (fn [{db :db} [_ frame-id event-id]]
   (let [vertical (keyword (:vertical frame-id))
         focus-event (fi/call-api :service-target-db-get db :focus-event vertical)]
     {:fx [(when (seq focus-event)
             [:dispatch (conj focus-event frame-id event-id)])]})))

;https://mathiasbynens.be/demo/url-regex @gruber v2
(def http-regex #"\b((?:[a-z][\w-]+:(?:/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'\".,<>?«»“”‘’]))")

(defn- source-frame-icon [event-source-groups source-icon source-frame-id]
  [:div
   [icon {:icon source-icon
          :size :medium
          :color (get event-source-groups source-frame-id :blue)}]])

(defn sidebar-details-tabs [frame-id]
  (r/create-class
   {:reagent-render
    (fn [frame-id]
      (let [{:keys [top-events-top-label
                    remove-all
                    details-view-add-comparison
                    details-view-remove
                    details-view-remove-comparison
                    aria-detail-view-focus]}
            @(re-frame/subscribe [::i18n/translate-multi
                                  :top-events-top-label
                                  :remove-all
                                  :details-view-add-comparison
                                  :details-view-remove
                                  :details-view-remove-comparison
                                  :aria-detail-view-focus])
            event-source-groups @(re-frame/subscribe [::event-source-groups])
            events @(re-frame/subscribe [::events-details])
            compare-events @(re-frame/subscribe [::to-compare])
            compare-count (count compare-events)
            read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                     {:frame-id frame-id})
            is-active? (set compare-events)]
        [:<>
         [section
          {:label (str top-events-top-label " (" (count events) ")")}
          [:div.details-view
           [:div.section__cards
            (reduce (fn [ul [event-id {:keys [source-icon source-frame-id di event-data remove-event]}]]
                      (conj ul
                            [:li.disabled {:id (str event-id)}
                             [:div.card__text
                              [:div.title.flex [source-frame-icon event-source-groups source-icon source-frame-id]
                               [:div (get event-data "date")]]]
                             [:div.card__actions
                              [button {:variant :tertiary
                                       :start-icon :focus
                                       :title aria-detail-view-focus
                                       :on-click (fn [e]
                                                   (.stopPropagation e)
                                                   (when-not (nil? (full-title source-frame-id))
                                                     (re-frame/dispatch [:de.explorama.frontend.woco.navigation.control/focus source-frame-id])
                                                     (re-frame/dispatch [::focus-event-in-frame source-frame-id event-id])))}]
                              (if (is-active? event-id)
                                [button {:variant :tertiary
                                         :title details-view-remove-comparison
                                         :start-icon :unpin
                                         :disabled? (or read-only? (>= 1 compare-count))
                                         :aria-label :close
                                         :on-click #(re-frame/dispatch [::remove-from-comparison {:frame-id frame-id
                                                                                                  :remove-event-id event-id}])}]
                                [button {:variant :tertiary
                                         :title details-view-add-comparison
                                         :start-icon :pin
                                         :disabled? (or read-only?
                                                        (>= (count compare-events)
                                                            compare-limit))
                                         :on-click #(re-frame/dispatch [::add-to-comparison {:frame-id frame-id
                                                                                             :new-event-id event-id}])}])
                              [button {:variant :tertiary
                                       :title details-view-remove
                                       :start-icon :close
                                       :disabled? read-only?
                                       :on-click (fn [e]
                                                   (.stopPropagation e)
                                                   (re-frame/dispatch [::remove-event frame-id event-id])
                                                   (when (vector? remove-event)
                                                     (re-frame/dispatch (conj remove-event source-frame-id di event-id))))}]]]))
                    [:ul]
                    (sort-by #(get-in % [1 :event-data "date"])
                             events))]]
          (when (seq compare-events)
            [button {:label remove-all
                     :size :small
                     :disabled? read-only?
                     :variant :tertiary
                     :on-click #(re-frame/dispatch [::remove-all-events {}])
                     :start-icon :close}])]]))
    :component-did-update (fn []
                            (let [newest-element @(re-frame/subscribe [::newest-element])
                                  newest-element (js/document.getElementById newest-element)]
                              (when newest-element
                                (.scrollIntoView newest-element))))}))

(defn- prepare-string-content [value]
  (loop [result [value]]
    (let [text (peek result)
          match (re-find http-regex text)]
      (if match
        (let [cur-url (first match)
              index (index-of text cur-url)
              before (subs text 0 index)
              after (subs text (+ index (count cur-url)))]
          (recur (conj (pop result)
                       before
                       [:a {:href cur-url
                            :target "_blank"}
                        cur-url]
                       after)))
        result))))

(defn- annotation->display [annotation-desc]
  (let [{author "author" editor "editor" content "content"} annotation-desc
        author (when author @(fi/call-api :name-for-user-sub author))
        editor (when editor @(fi/call-api :name-for-user-sub editor))
        author-label @(re-frame/subscribe [::i18n/translate :comment-author-label])
        editor-label @(re-frame/subscribe [::i18n/translate :comment-editor-label])]
    (if (= author editor)
      {:tooltip (str content "\n(" editor-label ": " author ")")
       :content (str content)}
      {:tooltip (str content "\n(" author-label ": " author " ," editor-label ": " editor ")")
       :content (str content)})))

(defn prepare-content [attr value]
  (let [loading-label @(re-frame/subscribe [::i18n/translate :details-view-loading-external-ref])
        lang @(re-frame/subscribe [::i18n/current-language])
        value (reduce
               (fn [acc value]
                 (cond
                   (string? value)
                   (into acc (prepare-string-content value))
                   (vector? value)
                   (conj acc (str value))
                   (number? value)
                   (conj acc (i18n/localized-number value lang))
                   (and (= attr "annotation")
                        (map? value))
                   (conj acc (annotation->display value))
                   :else
                   (conj acc value)))
               []
               (if (vector? value)
                 value
                 [value]))]
    (cond
      (and (seq value)
           (every? #{"loading"} value))
      loading-label
      (some #(and (vector? %)
                  (= :a (first %)))
            value)
      value
      (= attr "annotation")
      (first value)
      :else (join ", " value))))

(defn- source-infos [frame-id event-source-groups source-icon source-frame-id]
  (let [frame-title (full-title source-frame-id)
        closed-title @(re-frame/subscribe [::i18n/translate :frame-closed])
        frame-title (or frame-title closed-title)]
    [:div.entry__header
     [:div.entry__title {:title frame-title}
      frame-title]]))

(defn sidebar-table-body [frame-id compare-items]
  (let [events  @(re-frame/subscribe [::events-details compare-items])
        all-attr @(re-frame/subscribe [::compare-attributes])
        event-source-groups @(re-frame/subscribe [::event-source-groups])
        labels @(fi/call-api [:i18n :get-labels-sub])
        attr->display-name (fn [attr] (get labels attr attr))
        source-label @(re-frame/subscribe [::i18n/translate :table-column-source])]
    (reduce (fn [acc attr]
              (conj acc
                    (reduce (fn [acc [_ {:keys [event-data]}]]
                              (let [content (prepare-content attr (get event-data attr))
                                    {:keys [^string tooltip ^string content]
                                     :or {^string content content}} (when (map? content) content)]
                                (conj acc (into [:td {:title (when tooltip tooltip)}]
                                                content))))
                            [:tr
                             [:td
                              [:div (attr->display-name attr)]]]
                            events)))
            [:tbody
             (reduce (fn [acc [_ {:keys [source-icon source-frame-id]}]]
                       (conj acc [:td
                                  [source-infos frame-id event-source-groups source-icon source-frame-id]]))

                     [:tr
                      [:td
                       [:div source-label]]]
                     events)]
            all-attr)))

(defn- head-item [frame-id event-id compare-count]
  (let [event-details @(re-frame/subscribe [::event-details event-id])
        title (get-in event-details [:event-data title-attribute] "")
        remove-label @(re-frame/subscribe [::i18n/translate :details-view-remove-comparison])
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id})]
    [:th
     [:span title]
     (when (< 1 compare-count)
       [button {:variant :tertiary
                :title remove-label
                :start-icon :unpin
                :aria-label :close
                :disabled? read-only?
                :extra-class "explorama__button--small fl-r"
                :on-click #(re-frame/dispatch [::remove-from-comparison {:frame-id frame-id
                                                                         :remove-event-id event-id}])}])]))

(defn- table-head [frame-id compare-count compare-items]
  [:thead
   (reduce (fn [p event-id]
             (conj p
                   [head-item frame-id event-id compare-count]))
           [:tr
            [:th]]
           compare-items)])

(defn- sidebar-compare-view [frame-id]
  (let [compare-items @(re-frame/subscribe [::to-compare])
        compare-count (count compare-items)]
    [:div.fulltext__container {:style {:heigth "100%"}}
     (when (< 0 compare-count)
       (let [col-width (str (/ config/details-view-compare-width-percent compare-count)
                            "%")]
         [:table
          [:colgroup
           [:col {:style {:width config/details-view-attr-col-width}}]
           (map (fn [i]
                  (with-meta
                    [:col {:style {:width col-width}}]
                    {:key (str "woco-details-tabs" i)}))
                (range 0 compare-count))]
          [table-head frame-id compare-count compare-items]
          [sidebar-table-body frame-id compare-items]]))]))

(defn- product-tour-step-display []
  (let [current-step (re-frame/subscribe [:de.explorama.frontend.woco.api.product-tour/current-step])
        current-language (re-frame/subscribe [::i18n/current-language])
        steps-label (re-frame/subscribe [::i18n/translate :product-tour-steps-label])
        next-button-label (re-frame/subscribe [::i18n/translate :product-tour-next-button-label])
        back-button-label (re-frame/subscribe [::i18n/translate :product-tour-back-button-label])
        max-steps (re-frame/subscribe [:de.explorama.frontend.woco.api.product-tour/max-steps])
        sidebar-width @(re-frame/subscribe [:de.explorama.frontend.woco.sidebar/sidebar-width])
        {workspace-width :width} @(re-frame/subscribe [:de.explorama.frontend.woco.page/workspace-rect])
        left-offset (- workspace-width
                       sidebar-width
                       250 ;step width
                       10)]
    [comp-misc/product-tour-step {:component :woco-details-view
                                  :top config/userbar-height
                                  :offset-left left-offset
                                  :current-step current-step
                                  :language current-language
                                  :steps-label steps-label
                                  :next-button-label next-button-label
                                  :back-button-label back-button-label
                                  :val-sub-fn (fn [keyword-vector]
                                                (if
                                                 (and (vector? keyword-vector)
                                                      (= (first keyword-vector) :preference-panning)) (preference-panning-desc (second keyword-vector))
                                                 @(re-frame/subscribe (if (vector? keyword-vector)
                                                                        keyword-vector
                                                                        [::i18n/translate keyword-vector]))))
                                  :prev-fn (fn [_]
                                             (re-frame/dispatch [:de.explorama.frontend.woco.api.product-tour/previous-step]))
                                  :next-fn (fn [{:keys [component additional-info]}]
                                             (re-frame/dispatch [:de.explorama.frontend.woco.api.product-tour/next-step component additional-info]))
                                  :cancel-fn (fn [_]
                                               (re-frame/dispatch [:de.explorama.frontend.woco.api.product-tour/cancel-product-tour]))
                                  :max-steps max-steps}]))

(defn sidebar-view [frame-id _]
  [:<>
   [product-tour-step-display]
   [sidebar-details-tabs frame-id]
   [:div.content.details-view
    [sidebar-compare-view frame-id]]])

;;;;;;;;; API ;;;;;;;;;;;;;;;

(def ^:private details-view-api-path [:details-view-events])
(defn- event-details [event-id]
  (conj details-view-api-path event-id))
(def ^:private event-data-key :event-data)

(defn can-add? [db]
  (> event-limit
     (count (get-in db path/details-view-events))))

(defn- valid-details-view-entry? [frame-id di event-id event-data source-icon remove-event]
  (boolean (and frame-id di event-id event-data source-icon remove-event)))

(defn update-event-data [db event-id new-event-data merge?]
  (if (get-in db (event-details event-id))
    (if merge?
      (update-in db
                 (conj (event-details event-id) event-data-key)
                 merge
                 new-event-data)
      (assoc-in db
                (conj (event-details event-id) event-data-key)
                new-event-data))
    db))

(defn remove-from-details-view [db event-id]
  (cond-> db
    (and event-id (get-in db (event-details event-id)))
    (update-in details-view-api-path
               dissoc
               event-id)))

(defn remove-frame-events-from-details-view [db frame-id]
  (when-let [events (get-in db details-view-api-path)]
    (reduce (fn [{:keys [db dispatch-n] :as r}
                 [event-id {:keys [source-frame-id] :as e}]]
              (cond-> r
                (= frame-id source-frame-id)
                (assoc
                 :db (remove-from-details-view db event-id)
                 :dispatch-n (conj dispatch-n
                                   [:de.explorama.frontend.woco.api.compare/remove-event {:frame-id frame-id}
                                    :event-id event-id]))))
            {:db db
             :dispatch-n []}
            events)))

(defn add-to-details-view [db frame-id di event-id event-data source-icon remove-event]
  (cond-> db
    (and db (valid-details-view-entry? frame-id di event-id event-data source-icon remove-event))
    (->
     (assoc-in (event-details event-id)
               {:source-frame-id frame-id
                :di di
                event-data-key event-data
                :source-icon source-icon
                :remove-event remove-event})
     (assoc-in path/details-view-newest (str event-id)))))
