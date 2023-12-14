(ns de.explorama.shared.data-transformer.mapping
  (:require [clojure.string :as str]
            [de.explorama.shared.data-transformer.generator :as gen]
            [de.explorama.shared.data-transformer.util :as util]
            [taoensso.timbre :refer [warn]]))

(declare resolve*)

(defn- row-access
  ([row default key]
   (get row key default))
  ([row key]
   (get row key)))

(defn- resolve-convert [_ [desc] row]
  (let [behavior (cond (or (= 2 (count desc))
                           (= 3 (count desc)))
                       :split-by
                       (fn? (first desc))
                       :function
                       :else
                       (throw (ex-info "Unrecognizable convert behavior" {:convert desc})))]
    (case behavior
      :split-by (let [result (str/split (row-access row (first desc))
                                        (re-pattern (second desc)))]
                  (if (empty? result)
                    (nth desc 2)
                    result))
      :function ((first desc) row))))

(defn- resolve-id-generate [e [[prefix type] desc] row]
  (let [base (resolve* e desc row)
        value (if (vector? base)
                (str/join "-" base)
                base)]
    (str prefix "-"
         (cond (= type :hash)
               (hash value)
               (= type :text)
               value))))

(defn- resolve-or [_ desc _]
  (first desc))

(defn- resolve-fields [_ desc row]
  (mapv (partial row-access row (second desc)) (first desc)))

(defn- resolve-field [_ desc row]
  (row-access row (second desc) (first desc)))

(defn- resolve-date-schema [e [schema field] row]
  ;TODO r1/dates how to handle time
  (->> (resolve* e field row)
       (util/parse (util/formatter schema))
       (util/unparse (util/formatter util/date-format))))

(defn- resolve-position [e desc row]
  (str/split (resolve* e (first desc) row) #","))

(defn- resolve-lat-long [e desc row]
  [(resolve* e (first desc) row)
   (resolve* e (second desc) row)])

(defn- string-conversion [value]
  (if (vector? value)
    (mapv str value)
    value))

;TODO r1/mapping support more layouts than german and english.
(def ^:private complex-integer-schema-en #"^[\-]{0,1}([1-9]\d{0,2}(,?\d{3})*|0)$")
(def ^:private complex-integer-schema-de #"^[\-]{0,1}([1-9]\d{0,2}(.?\d{3})*|0)$")

(defn- integer-conversion [value]
  (cond (vector? value)
        (mapv #?(:clj #(Integer/parseInt %)
                 :cljs #(js/parseInt %))
              value)
        (string? value)
        (let [value (str/trim value)
              value (loop [patterns
                           [[:complex-en complex-integer-schema-en]
                            [:complex-de complex-integer-schema-de]]]
                      (let [pattern (first patterns)]
                        (if (nil? patterns)
                          [:empty ""]
                          (if-let [value (re-find (second pattern) value)]
                            [(first pattern)
                             (if (vector? value) (first value) value)]
                            (recur (rest patterns))))))
              value (case (first value)
                      :complex-en (str/replace (second value) #"," "")
                      :complex-de (str/replace (second value) #"\." "")
                      :empty nil)]
          (if (nil? value)
            nil
            #?(:clj (Integer/parseInt value)
               :cljs (js/parseInt value))))
        :else
        (throw (ex-info "Can't convert to integer value wrong input type" {:value value}))))

;TODO r1/mapping support more layouts than german and english.
(def ^:private complex-decimal-schema-en #"^[\-]{0,1}([1-9]\d{0,2}(,?\d{3})*|0)(\.\d+)+$")
(def ^:private complex-decimal-schema-de #"^[\-]{0,1}([1-9]\d{0,2}(.?\d{3})*|0)(\,\d+)+$")

(defn- decimal-conversion [value]
  (cond (vector? value)
        (mapv #?(:clj #(Double/parseDouble %)
                 :cljs #(js/parseFloat %))
              value)
        (string? value)
        (let [value (str/trim value)
              value (loop [patterns
                           [[:complex-en complex-decimal-schema-en]
                            [:complex-de complex-decimal-schema-de]]]
                      (let [pattern (first patterns)]
                        (if (nil? patterns)
                          [:empty ""]
                          (if-let [value (re-find (second pattern) value)]
                            [(first pattern)
                             (if (vector? value) (first value) value)]
                            (recur (rest patterns))))))
              value (case (first value)
                      :complex-en (str/replace (second value) #"," "")
                      :complex-de
                      (-> (str/replace (second value) #"\." "")
                          (str/replace #"\," "."))
                      :empty nil)]
          (if (nil? value)
            nil
            #?(:clj (Double/parseDouble value)
               :cljs (js/parseFloat value))))
        :else
        (throw (ex-info "Can't convert to decimal value wrong input type" {:value value}))))

(defn- boolean-conversion [value]
  (if (vector? value)
    (mapv #(if (#{"TRUE" "true" "True" "1"} %)
             true
             false)
          value)
    (if (#{"TRUE" "true" "True" "1"} value)
      true
      false)))

(defn- location-conversion [value]
  (if (vector? value)
    (mapv decimal-conversion value)
    (throw (ex-info "Don't know how to create location" {:value value}))))

(defn- resolve-id-rand [_ [type] _]
  (let [type (or type :uuid)]
    (case type
      :uuid #?(:clj (str (java.util.UUID/randomUUID))
               :cljs (str (random-uuid))))))

(def ^:private funcs
  (#?(:cljs clj->js :clj identity)
   {:convert resolve-convert
    :id-generate resolve-id-generate
    :id-rand resolve-id-rand
    :fields resolve-fields
    :field resolve-field
    :value resolve-or
    :date-schema resolve-date-schema
    :position resolve-position
    :lat-lon resolve-lat-long}))

(def ^:private funcs-convert
  (#?(:cljs clj->js :clj identity)
   {"string" string-conversion
    "integer" integer-conversion
    "decimal" decimal-conversion
    "boolean" boolean-conversion
    "location" location-conversion}))

(defn- resolve*
  ([e desc row]
   (resolve* e desc row "string"))
  ([e desc row type-convert]
   (when desc
     (try
       ((#?(:cljs aget :clj get) funcs-convert type-convert)
        ((#?(:cljs aget :clj get)
          funcs
          #?(:cljs (name (first desc))
             :clj (first desc)))
         e (rest desc) row))
       (catch #?(:clj Throwable :cljs :default) exc
         (throw (ex-info "Failed to resolve data" (cond-> {:desc desc
                                                           :row-number e
                                                           :row row
                                                           :msg (ex-message exc)
                                                           :execution (:exception exc)
                                                           :type-convert type-convert
                                                           :data (ex-data exc)}
                                                    (:old-row-numer row)
                                                    (assoc :row-number (:old-row-number row)
                                                           :transformed-row-number e)))))))))
(defn- gen-contexts [g {items :items} data]
  (vec
   (persistent!
    (reduce (fn [acc {features :features}]
              (reduce (fn [acc {contexts :contexts}]
                        (reduce (fn [acc {:keys [global-id name type]}]
                                  (reduce-kv (fn [acc row-num row]
                                               (let [name (resolve* row-num name row)]
                                                 (reduce (fn [acc name]
                                                           (let [global-id-desc (if (and (= :id-generate (first global-id))
                                                                                         (= :name (get global-id 2)))
                                                                                  (conj (pop global-id) [:value name])
                                                                                  global-id)
                                                                 gid (resolve* row-num global-id-desc row)
                                                                 type (resolve* row-num type row)
                                                                 new-context {:name name
                                                                              :type type}]
                                                             (when (and (get acc gid)
                                                                        (not= new-context (get acc gid)))
                                                               (throw (ex-info "Mulitple contexts with the same id and different content"
                                                                               {:desc {:global-id global-id
                                                                                       :name name
                                                                                       :type type}
                                                                                :row-number row-num
                                                                                :current-context new-context
                                                                                :id gid
                                                                                :previous-context (get acc gid)})))
                                                             (if (and gid name type)
                                                               (conj! acc (gen/context g gid type name))
                                                               acc)))
                                                         acc
                                                         (if (vector? name)
                                                           name
                                                           [name]))))
                                             acc
                                             (vec data)))
                                acc
                                contexts))
                      acc
                      features))
            (transient #{})
            items))))

(defn- gen-datasource [g {{:keys [name global-id]} :datasource} data]
  (when (not= :default (first name))
    (warn "There is only support for one datasource per file"))
  (gen/datasource g
                  (resolve* 1 global-id (first data))
                  (resolve* 1 name (first data))))

(defn- gen-generic [g row-num descs row gen-func attrs & [types]]
  (mapv (fn [desc]
          (apply gen-func
                 g
                 (mapv (fn [attr]
                         (let [type (get types attr "string")
                               type (if (keyword? type)
                                      (resolve* row-num (get desc type) row "string")
                                      type)]
                           (resolve* row-num (attr desc) row type)))
                       attrs)))
        descs))

(defn- gen-context-ref [g row-num descs row]
  (persistent!
   (reduce (fn [acc desc]
             (let [{:keys [global-id name rel-type rel-name]} desc
                   rel-type (resolve* row-num rel-type row "string")
                   rel-name (resolve* row-num rel-name row "string")]
               (if (and (= :id-generate (first global-id))
                        (= :name (get global-id 2)))
                 (let [name (resolve* row-num name row "string")]
                   (reduce (fn [acc name]
                             (conj! acc
                                    (gen/context-ref g
                                                     (resolve* row-num (conj (pop global-id) [:value name])
                                                               row "string")
                                                     rel-type rel-name)))
                           acc
                           (if (vector? name)
                             name
                             [name])))
                 (conj! acc
                        (gen/context-ref g
                                         (resolve* row-num global-id row "string")
                                         rel-type
                                         rel-name)))))
           (transient [])
           descs)))

(defn- gen-features [g row-num features row]
  (persistent!
   (reduce (fn [acc feature]
             (try
               (conj! acc
                      (gen/feature g
                                   (resolve* row-num (:global-id feature) row)
                                   (gen-generic g row-num (:facts feature) row gen/fact [:name :type :value] {:value :type})
                                   (gen-generic g row-num (:locations feature) row gen/location [:point] {:point "location"})
                                   (gen-context-ref g row-num (:contexts feature) row)
                                   (gen-generic g row-num (:dates feature) row gen/date [:type :value])
                                   (gen-generic g row-num (:texts feature) row gen/text [identity])))
               (catch #?(:clj Throwable :cljs :default) exc
                 (swap! (gen/state g) update :errors (fnil conj [])
                        {:row-num row-num
                         :msg (ex-message exc)
                         :data (ex-data exc)})
                 acc)))
           (transient [])
           features)))

(defn- gen-items [g {items :items} data]
  (persistent!
   (reduce (fn [acc {:keys [global-id features]}]
             (reduce-kv (fn [acc row-num row]
                          (conj! acc
                                 (gen/item g
                                           (resolve* row-num global-id row)
                                           (gen-features g row-num features row))))
                        acc
                        (vec data)))
           (transient [])
           items)))

(defn mapping [g {desc :mapping} data]
  (gen/data g
            (gen-contexts g desc data)
            (gen-datasource g desc data)
            (gen-items g desc data)))
