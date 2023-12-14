(ns de.explorama.frontend.search.data.acs
  (:require [re-frame.core :refer [->interceptor reg-event-db reg-sub]]
            [de.explorama.frontend.search.config :as config]
            [de.explorama.frontend.search.path :as path]))

(defn frame-id [path]
  (get path (count config/search-pre-path)))

(defn requesting=? [db frame-id]
  (> (get-in db (path/requesting frame-id) 0)
     0))
(defn requesting= [db frame-id value]
  (assoc-in db (path/requesting frame-id) value))

(defn create-data-instance=? [db frame-id]
  (get-in db (path/create-data-instance frame-id)))
(defn create-data-instance= [db frame-id value]
  (assoc-in db (path/create-data-instance frame-id) value))

(reg-sub
 ::is-requesting?
 (fn [db [_ frame-id]]
   (requesting=? db frame-id)))

(reg-event-db
 ::set-create-data-instance
 (fn [db [_ frame-id]]
   (create-data-instance= db frame-id true)))

(defn attr-type [db attr-desc]
  (keyword (get-in db (conj path/attribute-types attr-desc))))

(reg-sub
 ::attr-type
 (fn [db [_ attr-desc]]
   (attr-type db attr-desc)))

(reg-sub
 ::unavailable-types?
 (fn [db [_ attributes]]
   (some #{:notes :external-ref} (map #(attr-type db %) attributes))))

(reg-sub
 ::enabled-datasources
 (fn [db]
   (get-in db path/search-enabled-datasources)))

(reg-sub
 ::temporary-datasources
 (fn [db]
   (:temp (get-in db path/search-bucket-datasources {:temp []}))))

(def requesting-acs-interceptor
  (->interceptor :id ::requesting-acs
                 :before (fn [{{[_ frame-id] :event
                                db           :db} :coeffects
                               :as                context}]
                           (if (and (requesting=? db frame-id)
                                    (create-data-instance=? db frame-id))
                             (assoc context :queue [])
                             context))
                 :after nil))

(defn attribute-name->attr-desc [db attribute-name]
  (let [attr-descs (keys (get-in db path/attribute-types))]
    (first
     (filter (fn [[attr-name _]]
               (= attribute-name attr-name))
             attr-descs))))
