(ns de.explorama.frontend.ui-base.utils.select)

(defn- some-helper-fn [filter-key searched-value value]
  (when (= (get value filter-key)
           searched-value)
    value))

(defn ^:export selected-option
  "Finds an option in option-vector

| Parameter    | description  |
| ------------- | ------------- |
| `options`  | options vector like [{:label \"t\" :value 10} ..] |
| `selected-value`  | value to find |
| `filter-key`  | The key in options-entry to check. Default: :label |
   
   Example:
  ```clojure
    => (selected-option [{:label \"a\" :value 1} {:label \"b\" :value 2}] \"b\")
    => {:label \"b\" :value 2}
   
    => (selected-option :v [{:label \"a\" :v 1} {:label \"b\" :v 2}] 1)
    => {:label \"a\" :v 1}
   
    => (selected-option [{:label \"a\" :value 1} {:label \"b\" :value 2}] \"c\")
    => {}
  ```
   "
  ([options selected-value]
   (selected-option :label options selected-value))
  ([filter-key options selected-value]
   (or (some (partial some-helper-fn filter-key selected-value)
             options)
       {})))

(defn ^:export selected-options
  "Finds multiple options in option-vector

| Parameter    | description  |
| ------------- | ------------- |
| `options`  | options vector like [{:label \"t\" :value 10} ..] |
| `selected-values`  | values to find |
| `filter-key`  | The key in options-entry to check. Default: :label |
   
   Example:
  ```clojure
    => (selected-options [{:label \"a\" :value 1} {:label \"b\" :value 2}] [\"b\"])
    => [{:label \"b\" :value 2}]
   
    => (selected-options :v [{:label \"a\" :v 1} {:label \"b\" :v 2}] [1 2])
    => [{:label \"a\" :v 1} {:label \"b\" :v 2}]
   
    => (selected-options [{:label \"a\" :value 1} {:label \"b\" :value 2}] [\"c\"])
    => []
  ```
   "
  ([options selected-values]
   (selected-options :label options selected-values))
  ([filter-key options selected-values]
   (filterv identity
            (map (fn [sel-val]
                   (some (partial some-helper-fn filter-key sel-val)
                         options))
                 selected-values))))

(defn ^:export normalize
  "Normalize an option which means an mapping from option->value also when option is already an value

| Parameter    | description  |
| ------------- | ------------- |
| `option`  | options-map like {:label \"t\" :value 10} |
| `k`  | The key of `option` which represents the value. Default: :value |
| `res-nil?`  | return nil, when `k` is not in `option` |
   
   Example:
  ```clojure
    => (normalize {:label \"a\" :value 1})
    => 1
   
    => (normalize {:label \"a\" :v 2} :v)
    => 2
   
    => (normalize {:label \"a\" :value 1} :v)
    => {:label \"a\" :value 1}
    
    => (normalize {:label \"a\" :value 1} :v true)
    => nil
    
    => (normalize 4)
    => 4
   "
  ([option]
   (normalize option :value))
  ([option k]
   (normalize option k false))
  ([option k res-nil?]
   (get option k (when-not res-nil? option))))

(defn ^:export to-option
  "Creates an options-map

| Parameter    | description  |
| ------------- | ------------- |
| `val`  | The :value value :value <val> |
| `lab`  | The :label value :label <lab> |
| `val-as-keyword?`  | If true, `val` will transformed to keyword |
   
   Example:
  ```clojure
    => (to-option 1)
    => {:value 1 :label 1}
   
    => (to-option 1 \"a\")
    => {:value 1 :label \"a\"}
   
    => (to-option \"my-kw\" \"a\" true)
    => {:value :my-kw :label \"a\"}
   "
  ([val]
   (to-option val val false))
  ([val lab]
   (to-option val lab false))
  ([val lab val-as-keyword?]
   {:value (if val-as-keyword?
             (keyword val)
             val)
    :label lab}))

(defn ^:export vals->options
  "Makes a list of values to a list of options. This can be used as options for the select-component.

| Parameter    | description  |
| ------------- | ------------- |
| `values`  | values to transform to options |
   
   Example:
  ```clojure
    => (vals->options [1 2 3])
    => [{:value 1, :label 1} {:value 2, :label 2} {:value 3, :label 3}]
   
    => (vals->options [\"a\" \"b\"])
    => [{:value \"a\" :label \"a\"} {:value \"b\" :label \"b\"}]
   "
  [values]
  (mapv #(if (vector? %)
           (apply to-option %)
           (to-option %))
        values))
