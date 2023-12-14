(ns de.explorama.backend.common.conf-d-template
  "used in alias to generate the conf.d template file."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def conf-d-folder "conf.d.template")

(defn- env-name->export-env [env-name]
  (-> env-name
      (name)
      (str/upper-case)
      (str/replace #"-" "_")))

(defn- default-val->val-string [value-type default-val]
  (cond
    (boolean? default-val) default-val
    :else
    (case value-type
      :edn-string (str/replace (str default-val)
                               #"\""
                               "\\\"")
      :edn-file default-val
      :integer default-val
      default-val)))

(defn- config->rc-line [{doc :doc
                         default-val :default
                         value-type :type
                         env-name :env
                         possible-vals :possible-vals}]
  (let [doc-string (str/join "\n"
                             (map (fn [l]
                                    (str "# " (str/trim l)))
                                  (str/split-lines doc)))
        default-val-string (default-val->val-string value-type default-val)
        possible-val-string (str/join ", " possible-vals)]
    (cond-> ""
      doc-string (str doc-string "\n")
      type (str "# type " value-type "\n")
      (seq possible-val-string) (str "# values: " possible-val-string "\n")
      :always (str "# export " (env-name->export-env env-name) "=" (pr-str default-val-string)))))

(defn- configs->rc-file-external [file-name all-configs]
  (let [target-file (io/file conf-d-folder file-name)
        external-only (->> all-configs
                           (filter (fn [{:keys [visibility env]}]
                                     (and (= visibility :external) env)))
                           (sort-by :env))]
    (io/make-parents target-file)
    (spit target-file (str/join "\n\n"
                                (map config->rc-line external-only)))))

(defn- configs->rc-file-internal [file-name all-configs]
  (let [external-only (->> all-configs
                           (filter (fn [{:keys [visibility env]}]
                                     (and (= visibility :internal) env)))
                           (sort-by :env))]
    (spit file-name (str "#######################################################################\n"
                         "# THESE CONFIGS ARE FOR INTERNAL USE ONLY \n"
                         "# TO CHANGE DEFAULTS ADD THE EXPORT TO THE CONFIG FILE WITH THE CHANGE.\n"
                         "#######################################################################\n\n\n"
                         (str/join "\n\n"
                                   (map config->rc-line external-only))))))

(defn -main [& args]
  (let [file-name (first args)
        namespaces (rest args)
        all-configs (reduce (fn [acc n]
                              (require (symbol n))
                              (let [all-vars (vals (ns-publics (symbol n)))]
                                (into acc
                                      (comp
                                       (filter (fn [var]
                                                 (:is-config? (meta var))))
                                       (map (fn [var]
                                              (select-keys (meta var)
                                                           [:name :doc
                                                            :default :visibility
                                                            :type :env :possible-vals]))))
                                      all-vars)))
                            []
                            namespaces)]
    (println "Generate conf.d-template" file-name)
    (configs->rc-file-external file-name all-configs)
    (configs->rc-file-internal file-name all-configs)))

(comment

  (-main "map.rc"
         "map.config"
         "de.explorama.shared.abac.config"
         "de.explorama.backend.config"))
