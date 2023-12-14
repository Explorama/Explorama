(ns de.explorama.backend.charts.data.wordcloud
  (:require #?(:clj [clj-fuzzy.stemmers :refer [lancaster lovins porter]])
            #?(:clj [peco.core :as peco])
            [clojure.string :as string]
            [data-format-lib.filter]
            [de.explorama.backend.charts.config :as config]
            [de.explorama.backend.charts.data.helper :refer [attribute-value
                                                             chart-options]]
            [de.explorama.shared.common.config :as config-shared]
            [taoensso.tufte :as tufte]))

(def tokenizer-stopping #?(:clj (peco/tokenizer [:lower-case
                                                 :remove-days
                                                 :remove-months
                                                 :remove-stop-words])
                           :cljs (fn [& params] nil)))

(def tokenizer-no-stopping #?(:clj (peco/tokenizer [:lower-case
                                                    :remove-days
                                                    :remove-months])
                              :cljs (fn [& params] nil)))

(def stemming #?(:clj (memoize (case config/explorama-charts-stemming-algorithm
                                 :porter porter
                                 :lancaster lancaster
                                 :lovins lovins))
                 ;;TODO r1/charts implement stemming for cljs
                 :cljs (fn [& params] nil)))

(defn- apply-nlp
  "Applies stemming and/or stop-word-removal tokenize and returns a list of tags."
  [attr-value use-stemming? use-stopping?]
  (tufte/p ::apply-nlp
           (let [string-value (if (vector? attr-value)
                                (string/join ". " attr-value)
                                attr-value)
                 tokenized (filter
                            seq
                            (tufte/p ::tokenize
                                     (if use-stopping?
                                       (tokenizer-stopping string-value)
                                       (tokenizer-no-stopping string-value))))]
             (if use-stemming?
               (tufte/p ::stemming
                        (map
                         stemming
                         tokenized))
               tokenized))))

(defn- add-frequencies [counts coll]
  (tufte/p ::add-frequencies
           (reduce (fn [acc w]
                     (let [[p-w p-v] (when (vector? w) w)
                           w (or p-w w)
                           v (or p-v 1)]
                       (assoc! acc w (+ (get acc w 0) v))))
                   counts
                   coll)))

(defn- add-tags [acc attr-name attr-val attribute stopping-attributes stemming-attributes valid-attributes]
  (tufte/p ::add-tags
           (let [use-stemming? (stemming-attributes attr-name)
                 use-stopping? (stopping-attributes attr-name)
                 nlp? (or use-stemming? use-stopping?)]
             (cond
               (not (valid-attributes attr-name)) acc
               (not (seq attr-val)) acc
               :else
               (cond-> acc
                 (= attribute :all) (assoc! attr-name (inc (get acc attr-name 0)))
                 nlp? (add-frequencies (apply-nlp attr-val use-stemming? use-stopping?))
                 (and (not nlp?)
                      (vector? attr-val)) (add-frequencies (frequencies attr-val))
                 (and (not nlp?)
                      (not (vector? attr-val))) (assoc! attr-val (inc (get acc attr-val 0))))))))

(defn- freq-map->tag-list
  "Converts the freqency-map to a tag-list data.
   This is specific to the ui-lib used to display the data."
  [frequency-map]
  (into []
        (sort-by second frequency-map)))

(defn- datapoint->tags
  "Create a list of tags based on the user-selection and datapoint."
  [acc valid-attributes datapoint attributes stopping-attributes stemming-attributes]
  (tufte/p ::generate-datapoint-tags
           (reduce (fn [acc attribute]
                     (if (#{:all :characteristics} attribute)
                       (loop [[attr val] (first datapoint)
                              rest-attrs (rest datapoint)
                              cur-acc acc]
                         (if attr
                           (recur
                            (first rest-attrs)
                            (rest rest-attrs)
                            (add-tags cur-acc
                                      attr
                                      val
                                      attribute
                                      stopping-attributes
                                      stemming-attributes
                                      valid-attributes))
                           cur-acc))
                       (let [attr-value (attribute-value attribute datapoint)]
                         (add-tags
                          acc
                          attribute attr-value
                          attribute
                          stopping-attributes
                          stemming-attributes
                          valid-attributes))))
                   acc
                   attributes)))

(defn- data->frequency-map [min-occurence
                            data
                            valid-attributes
                            attributes
                            stopping-attributes
                            stemming-attributes]

  (cond->> (tufte/p ::generate-tag-frequency
                    (persistent!
                     (reduce
                      (fn [acc datapoint]
                        (tufte/p ::add-one-datapoint-tags
                                 (datapoint->tags acc
                                                  valid-attributes
                                                  datapoint
                                                  attributes
                                                  stopping-attributes
                                                  stemming-attributes)))
                      (transient {})
                      data)))
    min-occurence (filter #(>= (second %) min-occurence))
    :always (sort-by second >)
    :always (take 100)
    :always (into {})))

(defn wordcloud-dataset
  "Represent all tags based-on the user selection and data.

   attributes => list of attributes to be used
   special-values: :all => attribute-name + attribute-values
                   :characteristics => all attribute-values

   stopping-attributes => set of attribute-names for which word-stop should be used
   stemming-attributes => set of attribute-names for which stemming should be used

   min-occurence => min number this tag got mentioned

   Returns:
   [min-val
    max-val
    [<tupel of word, occurence>]]"
  [data attributes stopping-attributes stemming-attributes min-occurence]
  (tufte/profile
   {:when config-shared/explorama-profile-time}
   (tufte/p ::wordcloud-data-set
            (let [valid-attributes (tufte/p
                                    ::valid-attributes
                                    (set (map :value
                                              (get (chart-options) :wordcloud-attrs))))
                  frequency-map (data->frequency-map min-occurence
                                                     data
                                                     valid-attributes
                                                     attributes
                                                     stopping-attributes
                                                     stemming-attributes)
                  frequencies-vals (vals frequency-map)
                  min-val (tufte/p ::find-min-val (when (seq frequencies-vals)
                                                    (apply min frequencies-vals)))
                  max-val (tufte/p ::find-max-val (when (seq frequencies-vals)
                                                    (apply max frequencies-vals)))]
              [min-val
               max-val
               (tufte/p ::create-final-data-struct
                        (freq-map->tag-list
                         frequency-map))]))))