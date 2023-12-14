(ns de.explorama.shared.common.configs.provider
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [de.explorama.scope :refer [current-env]]
            [de.explorama.shared.common.configs.provider-impl :as config-impl]
            [de.explorama.shared.common.unification.misc :refer [cljc-parse-double
                                                                 cljc-parse-int]]
            [taoensso.timbre :refer [error warn info]]))

#?(:clj (def client-configs (atom {})))

(def ^:private valid-types
  {:client #{:edn-string :string :keyword :integer :double :boolean}
   :all #{:edn-string :string :keyword :integer :double :boolean}
   :server #{:edn-file :edn-string :string :keyword :integer :double :boolean}})

(defn- eval-type [config-name expected-type possible-values value default-value use-default-on-fail? fallback? fallback]
  (cond fallback?
        fallback
        (and fallback?
             (fn? fallback))
        (fallback)
        :else
        (let [possible-values (set possible-values)
              evaled-value (case expected-type
                             :edn-string (cond
                                           (string? value) (edn/read-string value)
                                           use-default-on-fail? default-value
                                           :else value)
                             #?(:clj
                                :edn-file)
                             #?(:clj
                                (cond
                                  (or (and (string? value) (not (str/blank? value)))
                                      (vector? value))
                                  (try
                                    (let [value (if (vector? value)
                                                  (reduce (fn [acc part]
                                                            (str acc (if (= :config-dir part)
                                                                       config-impl/config-dir
                                                                       part)))
                                                          ""
                                                          value)
                                                  value)]
                                      (-> (slurp value)
                                          (edn/read-string)))
                                    (catch #?(:clj Throwable :cljs :default) _e
                                      (if use-default-on-fail?
                                        (error "Reading edn-file failed using default as fallback."
                                               {:given value
                                                :default default-value
                                                :config config-name})
                                        (error "Reading edn-file failed"
                                               {:config config-name
                                                :given value}))
                                      (cond use-default-on-fail?
                                            default-value
                                            fallback
                                            (do
                                              (warn "Using fallback value."
                                                    {:default default-value
                                                     :config config-name
                                                     :fallback fallback})
                                              fallback)
                                            :else
                                            value)))
                                  use-default-on-fail? default-value
                                  :else value))
                             :keyword (cond
                                        (keyword? value) value
                                        (and (string? value) (seq value)) (if (str/starts-with? value ":")
                                                                            (edn/read-string value)
                                                                            (keyword value))
                                        use-default-on-fail? default-value
                                        :else
                                        (throw (ex-info "Given value can't be casted to a keyword."
                                                        {:given value})))
                             :integer (if (number? value)
                                        value
                                        (try
                                          (cljc-parse-int value)
                                          (catch #?(:clj Throwable :cljs :default) _e
                                            (if use-default-on-fail?
                                              (error "Parsing integer failed using default as fallback."
                                                     {:given value
                                                      :default default-value
                                                      :config config-name})
                                              (error "Parsing integer failed"
                                                     {:config config-name
                                                      :given value}))
                                            (when use-default-on-fail?
                                              default-value))))
                             :double (if (number? value)
                                       value
                                       (try
                                         (cljc-parse-double value)
                                         (catch #?(:clj Throwable :cljs :default) _e
                                           (if use-default-on-fail?
                                             (error "Parsing double failed using default as fallback."
                                                    {:given value
                                                     :default default-value
                                                     :config config-name})
                                             (error "Parsing double failed"
                                                    {:config config-name
                                                     :given value}))
                                           (when use-default-on-fail?
                                             default-value))))
                             :boolean value
                             value)]
          (cond
            (and (seq possible-values)
                 (possible-values evaled-value)) evaled-value
            (seq possible-values) (throw (ex-info (str "Given value is not valid allowed: " possible-values)
                                                  {:given value
                                                   :allowed possible-values}))
            :else evaled-value))))

(defn defconfig
  "This is a helper function to define configurations.
   definition is a simple map with the following keys: 

   - :env => the Environment variable that should be used eg: :explorama-profile-time
   - :default => default value which should be used when no other value is available
   - :type => what type the config is, possible values are #{:edn-string :edn-file :keyword :integer :double}
              based on the type parsing/validation happens 
              edn-string => read as edn, 
              edn-file => value used as file-path, slurp and read
              keyword => make a keyword, handles also strings starting with : by using edn/read-string
              integer/double => parsing the value if not a number already
   - :default-on-fail? => if the parsing/reading fails this then allows just to use the default value
   - :doc => Documentation string for the config, gets also used when generating the conf-template file
   - :post-read-fn => Applied to the parsed config value which returns a new value. 
                      Usefull when you need to do some transformation, for example transform from days to milliseconds.
                      (fn [days]
                        (.toMillis java.util.concurrent.TimeUnit/DAYS
                                   days))
   - :values => Defines a set of valid values for the config. 
                Gets checked after parsing.
                Example might be available persistence backends: #{:expdb :in-memory}
   - :scope => Defines the scope of the variable: #{:client :server :all}. Default is :server.
               Keep in mind certian types are not applicable for client side configs.
   
   - :overwritable? => Defines it the server is allowed to overwrite client configs. Default is false.
                       This only applies for the server client bundles and not electron or browser.

  required keys:
  - :doc
  - :type only when :env is used"
  [definition]
  (let [{env-name :env
         name :name
         env-default :default
         use-default? :default-on-fail?
         documentation :doc
         env-type :type
         post-read-fn :post-read-fn
         possible-values :values
         overwritable? :overwritable?
         scope :scope
         fallback :fallback
         :or {scope :server}}
        definition
        post-read-fn (or post-read-fn identity)
        fallback? #?(:clj false
                     :cljs (= env-type :edn-file))
        name (or name env-name)]
    (info "Defining config" name env-type scope)
    (when-not (seq documentation)
      (throw (ex-info (str "No documentation defined for config " env-name) {})))
    (when (and env-name (not env-type))
      (throw (ex-info (str "No type defined for config " env-name) {})))
    (when-not name
      (throw (ex-info (str "No name defined for config") definition)))
    (when-not ((valid-types :server) env-type)
      (warn "Undefined env type" name env-type scope))
    (when (and (or (= current-env :all)
                   (= current-env scope))
               ((valid-types current-env) env-type))
      (let [value
            (post-read-fn
             (eval-type name
                        env-type
                        possible-values
                        (config-impl/lookup name env-default)
                        env-default
                        use-default?
                        fallback?
                        fallback))]
        #?(:clj
           (when (and overwritable?
                      (= scope :all)
                      (= current-env :server))
             (swap! client-configs assoc name value)))
        value))))

#?(:clj (def load-logging-config config-impl/load-logging-config)
   :cljs (defn load-logging-config [_]))
