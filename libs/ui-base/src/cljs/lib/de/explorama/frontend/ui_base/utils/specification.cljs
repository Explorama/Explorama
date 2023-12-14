(ns de.explorama.frontend.ui-base.utils.specification
  (:require [malli.core :as m]
            [malli.error :as me]
            [taoensso.timbre :refer [error info]]))

;;; malli validation

(defonce ^:private validate? (atom false))

(defn ^:export enable-validation
  "Enable validation in the UI-Base."
  []
  (reset! validate? true)
  (info "UI-Base validation activated!"))

(defn ^:export validate
  "Validate anything with against a Malli specification. Any error will be printed to the console.
   If validation is not enabled, this function will return nil.
   The parameters are in order:
   component - a string, which will be used in the error message for you to indentify the origin
   spec      - the Malli specification
   params    - the thing to validate
   force?    - [optional] if true, it will validate the params even with disabled validation"
  ([component spec params]
   (validate component spec params false))
  ([component spec params force?]
   (when
    (and (or force? @validate?)
         (not (m/validate spec params)))
     (let [{:keys [id label title]} params]
       (error "\n"
              component "\n"
              params "\n"
              (-> (m/explain spec params)
                  (me/humanize)))))))

;;; generate malli

(defn- types [ptype printable?]
  (case ptype
    :derefable (if printable?
                 '[:fn (fn [x] (implements? IDeref x))]
                 [:fn {:error/message "should be derefable"} (fn [x] (implements? IDeref x))])
    :date (if printable?
            '[:fn (fn [x] (instance? js/RegExp x))]
            [:fn {:error/message "should be a date"} (fn [x] (instance? js/Date x))])
    :regexp (if printable?
              '[:fn (fn [x] (instance? js/RegExp x))]
              [:fn {:error/message "should be a regexp"} (fn [x] (instance? js/RegExp x))])
    :nested-vector (if printable?
                     `[~':vector ~'vector?]
                     [:vector vector?])
    :all 'any?
    :string 'string?
    :number 'number?
    :boolean 'boolean?
    :integer 'integer?
    :map 'map?
    :vector 'vector?
    :component 'vector?
    :function 'fn?
    ptype))

(defn- get-type-validation [ptypes printable?]
  (if (coll? ptypes)
    (reduce
     (fn [res ptype]
       (conj res (types ptype printable?)))
     [:or]
     ptypes)
    (types ptypes printable?)))

(defn- get-malli-ref-vec [ref-key ptype printable?]
  (let [as-vec? (if (vector? ptype)
                  (some #{:vector} ptype)
                  (= ptype :vector))
        as-nested-vec? (if (vector? ptype)
                         (some #{:nested-vector} ptype)
                         (= ptype :nested-vector))
        derefable? (when (vector? ptype) (some #{:derefable} ptype))
        deref-type (types :derefable printable?)
        base-ref (cond
                   (and printable? as-nested-vec?)
                   `[~':vector [~':vector [~':ref ~(str ":" ref-key)]]]
                   (and (not printable?) as-nested-vec?)
                   [:vector [:vector [:ref (str ":" ref-key)]]]
                   (and printable? as-vec?)
                   `[~':vector [~':ref ~(str ":" ref-key)]]
                   (and (not printable?) as-vec?)
                   [:vector [:ref (str ":" ref-key)]]
                   printable?
                   `[~':ref ~(str ":" ref-key)]
                   (not printable?)
                   [:ref (str ":" ref-key)])]
    (cond
      (and printable? derefable?)
      `[~:or ~deref-type ~base-ref] ;display order switched for better formatting
      derefable?
      [:or base-ref deref-type] ;correct order
      :else
      base-ref)))

(defn- get-malli-map [parameters printable?]
  (reduce
   (fn [res [k {:keys [required require-cond characteristics] ptype :type pdefinition :definition}]]
     (as-> [k] parameter
       (if (and (= required true) (nil? require-cond))
         parameter
         (conj parameter {:optional true}))
       (cond
         (and characteristics (vector? ptype) (not-empty (remove #{:string :keyword} ptype)))
         (conj parameter (conj (get-type-validation (remove #{:string :keyword} ptype) printable?)
                               (into [:enum] characteristics)))
         characteristics
         (conj parameter (into [:enum] characteristics))
         pdefinition
         (conj parameter (get-malli-ref-vec pdefinition ptype printable?))
         :else
         (conj parameter (get-type-validation ptype printable?)))
       (conj res parameter)))
   []
   (sort-by #(not= true (get-in % [1 :required])) parameters)))

(defn- get-malli-or-functions [groups printable?]
  (reduce
   (fn [res [k members]]
     (let [member-keys (keys members)]
       (conj res
             (if printable?
               `[~':fn
                 {~':error/message (~'str "A " ~k " parameter is required!")}
                 (~'fn ~'[input] (~'some ~'identity (~'select-keys ~'input ~member-keys)))]
               [:fn
                {:error/message (str "A " k " parameter is required!")}
                (fn [input] (some identity (select-keys input member-keys)))]))))
   []
   groups))

(defn- get-malli-require-when-funtions [parameters printable?]
  (reduce
   (fn [res [k desc]]
     (if-let [[ckey cvalue] (get desc :require-cond)]
       (let [any-value? (= cvalue :*)
             vec-value? (vector? cvalue)]
         (conj res
               (cond
                 (and any-value? printable?)
                 `[~':fn
                   {~':error/message (~'str ~k " is required, when " ~ckey " is used!")}
                   (~'fn ~'[input] (~'if (~'get ~'input ~ckey) (~'get ~'input ~k) ~'true))]
                 (and vec-value? printable?)
                 `[~':fn
                   {~':error/message (~'str ~k " is required, when " ~ckey " is in " ~(set cvalue) "!")}
                   (~'fn ~'[input] (~'if ((~'set ~cvalue) (~'get ~'input ~ckey)) (~'get ~'input ~k) ~'true))]
                 printable?
                 `[~':fn
                   {~':error/message (~'str ~k " is required, when " ~ckey " is " ~cvalue "!")}
                   (~'fn ~'[input] (~'if (~'= (~'get ~'input ~ckey) ~cvalue) (~'get ~'input ~k) ~'true))]
                 any-value?
                 [:fn
                  {:error/message (str k " is required, when " ckey " is used!")}
                  (fn [input] (if (get input ckey) (get input k) true))]
                 vec-value?
                 [:fn
                  {:error/message (str k " is required, when " ckey " is in " (set cvalue) "!")}
                  (fn [input] (if ((set cvalue) (get input ckey)) (get input k) true))]
                 :else
                 [:fn
                  {:error/message (str k " is required, when " ckey " is " cvalue "!")}
                  (fn [input] (if (= (get input ckey) cvalue) (get input k) true))])))
       res))
   []
   parameters))

(defn ^:export parameters->malli
  "Generates a Malli specification based on the parameters. A map can contain two different parts:
   1. A map specification of each parameter
   2. A set of functions to reflect the relation 'at least one of the optional keys is required'
   Sub-components wil be recursivly generated by the same function."
  ([parameters sub-paramertes]
   (parameters->malli parameters sub-paramertes false))
  ([parameters sub-parameters printable?]
   (let [or-groups (-> (group-by #(get-in % [1 :required]) parameters)
                       (dissoc true false nil))
         cond-parameters (filter (fn [[_ v]] (get v :require-cond)) parameters)
         registry (if printable?
                    "<see item parameters>"
                    (reduce (fn [res [k v]]
                              (assoc res (str ":" k) (parameters->malli v {} false)))
                            {}
                            sub-parameters))
         map-parent [:map (cond-> {:closed true}
                            (not-empty sub-parameters)
                            (assoc (if printable? ':registry :registry) registry))]]
     (if (and (empty? cond-parameters) (empty? or-groups))
       (into map-parent (get-malli-map parameters printable?))
       (-> [:and]
           (conj (into map-parent (get-malli-map parameters printable?)))
           (into (get-malli-or-functions or-groups printable?))
           (into (get-malli-require-when-funtions cond-parameters printable?)))))))

(defn ^:export parameters->malli-str
  "Format the generated Malli specification. The regex matches:
  ```
   \\[:fn[^)]+\\)\\)\\]      All custom functions from the 'types' function above to prevent line breaks 
   \\[:enum[^\\]]+\\]       All enums to prevent line breaks
   \\[input\\]             All arguments of custom functions from 'get-malli-functions' to prevent line breaks
   \\(fn                  All custom functions not covered by the first case to indent and line break
   \\(                    All '(' to keep them and enable the previous case
   \\[                    All '[' to line break and indent the new line 
   \\]                    All ']' to keep track of the whitespaces to indent properly
   [^\\[|^\\|^\\(]]+        Everything else since re-seq would get rid of it otherwise
  ```
   Afterwards the function just loops over everything, keeps tracks of whitespaces and inserts basic line breaks and indentation."
  [parameters util-parameters]
  (let [inputs (->> (parameters->malli parameters util-parameters true)
                    str
                    (re-seq #"\[:fn[^)]+\)\)\]|\[:enum[^\]]+\]|\[input\]|\(fn|\(|\[|\]|[^\[|^\]|^\(]+"))]
    (loop [res ""
           whitespaces 0
           remainder inputs]
      (if (empty? remainder)
        (apply str res)
        (let [current-element (first remainder)
              padding-fn #(str
                           (when (not= res "") "\n")
                           (apply str (take %1 (repeat " "))) %2)
              new-remainder (drop 1 remainder)]
          (cond
            (= current-element "(fn")
            (recur (str res (padding-fn (+ whitespaces 3) current-element)) whitespaces new-remainder)
            (= current-element "[")
            (recur (str res (padding-fn whitespaces current-element)) (+ whitespaces 2) new-remainder)
            (= current-element "]")
            (recur (str res current-element) (- whitespaces 2) new-remainder)
            :else
            (recur (str res current-element) whitespaces new-remainder)))))))
