(ns de.explorama.backend.configuration.persistence.i18n.core
  (:require [clojure.set :as set]
            [de.explorama.backend.configuration.ac-api :as ac-api]
            [de.explorama.backend.configuration.persistence.labels.core :as persistence]
            [de.explorama.backend.frontend-api :as fapi]
            [de.explorama.shared.configuration.ws-api :as ws-api]
            #?(:cljs [i18n.translations :refer [translations]]
               :clj [server-i18n.translations :refer [load-translations]])
            [malli.core :as m]
            [taoensso.timbre :refer [error]]))

(def ^:private
  translation-desc
  [:map-of
   :string
   [:map {:closed true}
    [:de-DE {:optional true} [:string {:min 1 :max 255}]]
    [:en-GB {:optional true} [:string {:min 1 :max 255}]]]])

(def ^:private accepted-languages #{:de-DE :en-GB})

(def ^:private blacklist #{"custom" "all"})

(defonce langfile (atom {}))

(defn read-langfile []
  (or
   (reset! langfile #?(:cljs translations
                       :clj (load-translations)))
   {}))

(defn- validate-language [lang]
  (when-not (some #{lang} accepted-languages)
    (error (str "invalid language; accepted languages are: " accepted-languages))
    {:status :failed
     :msg :language-not-valid
     :data {:language lang
            :reason (str "The only accepted languages are: " accepted-languages)}}))

(defn- validate-with-blacklist
  [labels]
  (let [label-values (->> labels
                          vals
                          (mapv vals)
                          flatten
                          set)
        blacklisted (set/intersection blacklist label-values)]
    (when (not-empty blacklisted)
      (error (str "some label values are blacklisted: " blacklisted))
      {:status :failed
       :msg :blacklisted-labels
       :data {:labels blacklisted
              :reason "Some label values are blacklisted."}})))

(defn- validate-label-mapping
  "If the label mapping is valid returns nil.
   Otherwise returning a map containing a status, msg and reason."
  [labels]
  (when-not (m/validate translation-desc labels)
    (error "mapping not valid; only mappings from String to Map are accepted (possible languages are :de-DE + :en-GB)")
    {:status :failed
     :msg :mapping-not-valid
     :data {:labels labels
            :reason "Only mappings from String to Map are accepted (possible languages are :de-DE + :en-GB)."}}))

(defn- validate-labels
  "If the label mapping is valid returns nil.
   Otherwise returning a map containing a status, msg and reason."
  [labels lang]
  (let [select-language-fn
        (fn [input] (reduce
                     (fn [res [key val]]
                       (if-let [translation (get val lang)]
                         (assoc res key translation)
                         res))
                     {}
                     input))
        labels (select-language-fn labels)
        existing-labels (select-language-fn (persistence/read-labels))
        intersecting-attributes (set/intersection (-> existing-labels keys set)
                                                  (-> labels keys set))
        differently-labeled-attributes (filter
                                        #(not (= (get labels %) (get existing-labels %)))
                                        intersecting-attributes)
        intersecting-labels (set/intersection (-> existing-labels vals set)
                                              (-> labels vals set))
        labels-with-different-attributes (set/difference intersecting-labels
                                                         (->> intersecting-attributes
                                                              (mapv #(get existing-labels %))
                                                              (set)))]
    (cond
      (not-empty differently-labeled-attributes)
      (do
        (error "some attributes already have different labels")
        {:status :failed
         :msg :labels-already-exist
         :data {:labels labels
                :relevant-attributes differently-labeled-attributes
                :reason "Some attributes already have different labels assigned to them."}})
      (not-empty labels-with-different-attributes)
      (do
        (error "some attributes already have different labels")
        {:status :failed
         :msg :duplicate-labels
         :data {:relevant-labels labels-with-different-attributes
                :reason "Some labels are already assigned to a different attribute."}}))))

(defn get-labels [lang]
  (let [language-validation-result (validate-language lang)]
    (if (nil? language-validation-result)
      (reduce
       (fn [res [key val]]
         (if-let [translation (or (get val lang)
                                  (get val :en-GB))]
           (assoc res key translation)
           res))
       {}
       (persistence/read-labels))
      language-validation-result)))

(defn set-labels [labels]
  (let [labels-validation-result (or (validate-with-blacklist labels)
                                     (validate-label-mapping labels)
                                     (validate-labels labels :en-GB)
                                     (validate-labels labels :de-DE))]
    (if (nil? labels-validation-result)
      {:status :success
       :data (persistence/write-labels labels)}
      labels-validation-result)))

(defn get-translations [lang]
  (let [language-validation-result (validate-language lang)]
    (if (nil? language-validation-result)
      (letfn [(select-lang [[k v]]
                (when (map? v)
                  (if-let [translation (get v lang)]
                    (vector k translation)
                    (vector k (into {} (mapv select-lang v))))))]
        (into {}
              (mapv select-lang @langfile)))
      language-validation-result)))

(defn init []
  (persistence/new-instance (fn [_ _ _ _]
                              (fapi/broadcast [ws-api/update-labels]))))