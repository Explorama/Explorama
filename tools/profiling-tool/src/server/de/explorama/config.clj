(ns de.explorama.config
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [config.core :refer [env]]
            [taoensso.timbre :refer [error]]))

(defn first-non-nil
  "Returns the first non-nil parameter if there is any.
   Otherwise returns nil."
  [& params]
  (first (drop-while nil? params)))

(defn eval-type
  "Evaluates the value to the specific type.
   Only needed for edn and encrypted edn strings."
  [expected-type possible-values env-name value default-value use-default-on-fail?]
  (let [possible-values (set possible-values)
        evaled-value (case expected-type
                       :edn-string (cond
                                     (string? value) (edn/read-string value)
                                     use-default-on-fail? default-value
                                     :else value)
                       :edn-file (cond
                                   (and (string? value) (seq value)) (try
                                                                       (-> (slurp value)
                                                                           (edn/read-string))
                                                                       (catch Exception e
                                                                         (if use-default-on-fail?
                                                                           (error e
                                                                                  "Reading edn-file failed using default as fallback."
                                                                                  {:given value
                                                                                   :default default-value
                                                                                   :config env-name})
                                                                           (error e
                                                                                  "Reading edn-file failed"
                                                                                  {:config env-name
                                                                                   :given value}))
                                                                         (when use-default-on-fail?
                                                                           default-value)))
                                   use-default-on-fail? default-value
                                   :else value)
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
                                    (Integer/parseInt value)
                                    (catch Exception e
                                      (if use-default-on-fail?
                                        (error e
                                               "Parsing integer failed using default as fallback."
                                               {:given value
                                                :default default-value
                                                :config env-name})
                                        (error e
                                               "Parsing integer failed"
                                               {:config env-name
                                                :given value}))
                                      (when use-default-on-fail?
                                        default-value))))
                       :double (if (number? value)
                                 value
                                 (try
                                   (Double/parseDouble value)
                                   (catch Exception e
                                     (if use-default-on-fail?
                                       (error e
                                              "Parsing double failed using default as fallback."
                                              {:given value
                                               :default default-value
                                               :config env-name})
                                       (error e
                                              "Parsing double failed"
                                              {:config env-name
                                               :given value}))
                                     (when use-default-on-fail?
                                       default-value))))
                       value)]
    (cond
      (and (seq possible-values)
           (possible-values evaled-value)) evaled-value
      (seq possible-values) (throw (ex-info (str "Given value is not valid allowed: " possible-values)
                                            {:given value
                                             :allowed possible-values}))
      :else evaled-value)))

(defn visibility-valid? [visibility]
  (if (#{:internal :external} visibility)
    visibility
    (throw (ex-info "Invalid visibility flag used on expected of set. #{:internal :external}"
                    {:given visibility
                     :expected #{:internal :external}}))))

(defmacro defconfig
  "This is a helper macro to define configurations.
   Its similar to (def name definition) so the usable config will be defined by name.
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
   - :visibility => deciding where the config will be visible (:internal, :external). 
                    External means it will be visible in the conf-template file and can be changed by the customer.
   - :post-read-fn => Applied to the parsed config value which returns a new value. 
                      Usefull when you need to do some transformation, for example transform from days to milliseconds.
                      (fn [days]
                        (.toMillis java.util.concurrent.TimeUnit/DAYS
                                   days))
   - :values => Defines a set of valid values for the config. 
                Gets checked after parsing.
                Example might be available persistence backends: #{:redis :file :in-memory}

  required keys:
  - :doc
  - :visibility
  - :type only when :env is used"
  [name definition]
  (let [{env-name :env
         env-default :default
         use-default? :default-on-fail?
         documentation :doc
         visibility :visibility
         env-type :type
         post-read-fn :post-read-fn
         possible-values :values} definition
        post-read-fn (or post-read-fn (fn [v] v))
        meta-map {:is-config? true
                  :default env-default
                  :env env-name
                  :type env-type
                  :possible-vals possible-values
                  :visibility (visibility-valid? visibility)
                  :doc documentation}]
    (when-not (seq documentation)
      (throw (ex-info (str "No documentation defined for config " name) {})))
    (when (and env-name (not env-type))
      (throw (ex-info (str "No type defined for config " name) {})))
    `(def ~(with-meta name meta-map)
       (~post-read-fn
        (eval-type ~env-type
                   ~possible-values
                   ~env-name
                   (if ~env-name
                     (env ~env-name ~env-default)
                     ~env-default)
                   ~env-default
                   ~use-default?)))))

(defconfig config-dir
  {:env :explorama-confdir
   :default "."
   :type :string
   :doc "Defines where the configuration directory is.
         Gets set by the control.sh."})

(defconfig thread-pool
  {:env :explorama-data-service-thread-pool
   :type :integer
   :default 24
   :doc "Defines how many threads are available for a pool. 
         There might exist multiple diffrent pools."})
