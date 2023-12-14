(ns de.explorama.frontend.ui-base.overview.renderer.db
  (:require [re-frame.core :as rf]
            [clojure.string :as cljstr]))

(def sections-root :sections)
(def components-key "components")
(def functions-key "functions")

(defn section-path [section]
  [sections-root section])

(defn category-path [section category]
  (conj (section-path section)
        category))
(defn component-path [section category component-name]
  (conj (category-path section category)
        component-name))
(defn component-infos-path [section category component-name]
  (conj (component-path section category component-name)
        :infos))
(defn component-examples-path [section category component-name]
  (conj (component-path section category component-name)
        :examples))

(rf/reg-event-db
 ::add-component-infos
 (fn [db [_ category component-name {:keys [section is-sub?]
                                     :or {section components-key}
                                     :as infos}]]
   (let  [base-path (component-infos-path section category component-name)]
     (if is-sub?
       (update-in db (conj base-path :sub) #(conj (or % []) infos))
       (assoc-in db base-path infos)))))

(rf/reg-sub
 ::component-infos
 (fn [db [_ section category component-name]]
   (get-in db (component-infos-path section category component-name))))

(rf/reg-event-db
 ::add-component
 (fn [db [_ category component-name {:keys [section title code]
                                     :or {section components-key}
                                     :as component}]]
   (update-in db
              (component-examples-path section category component-name)
              (fn [old]
                (let [title (if title
                              title
                              (str "Example " (inc (count (filterv #(and (cljstr/starts-with? (:title %)
                                                                                              "Example")
                                                                         (not= code (:code %)))
                                                                   (or old []))))))]
                  (conj (filterv #(not= (:title %) title)
                                 (or old []))
                        (assoc component :title title)))))))

(rf/reg-event-db
 ::reset-component
 (fn [db [_ category component]]
   (if (get-in db (component-path components-key category component))
     (update-in db
                (category-path components-key category)
                dissoc component)
     db)))

(rf/reg-sub
 ::sections
 (fn [db]
   (vec (sort (keys (get db sections-root))))))

(rf/reg-sub
 ::categories
 (fn [db [_ section]]
   (vec (sort (keys (get-in db (section-path section)))))))

(rf/reg-sub
 ::components
 (fn [db [_ section category]]
   (vec (sort (keys (get-in db (category-path section category)))))))

(rf/reg-sub
 ::examples
 (fn [db [_ section category component]]
   (get-in db (component-examples-path section category component))))

(rf/reg-event-db
 ::select-component
 (fn [db [_ section category component]]
   (assoc db
          :current-view
          [section category component])))

(defn default-curr [db]
  (try
    (if (cljstr/includes? js/window.location.search "section")
      (cljstr/split (cljstr/replace js/window.location.search
                                    #"(\?)|(section\=)|(category\=)|(component\=)"
                                    "")
                    "&")
      (let [sections (get db sections-root [])
            [section] (first sections)
            category (if (get-in db (category-path section "formular"))
                       "formular"
                       (first (sort (keys (get-in db (section-path section) [])))))
            [comp] (first (sort (get-in db (category-path section category) [])))]
        [section category comp]))
    (catch :default e
      nil)))

(rf/reg-sub
 ::current-component
 (fn [db]
   (get db :current-view (default-curr db))))