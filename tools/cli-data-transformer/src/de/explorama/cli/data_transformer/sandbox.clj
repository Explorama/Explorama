(ns de.explorama.cli.data-transformer.sandbox
  (:require [clj-time.coerce]
            [clj-time.format]
            [de.explorama.cli.data-transformer.cli-helper :refer [exit]]
            [sci.core :as sci]))

(defn- copied-ns [ns-symbol]
  (let [fns (sci/create-ns ns-symbol nil)]
    (reduce (fn [ns-map [var-name var]]
              (let [m (meta var)
                    no-doc (:no-doc m)
                    doc (:doc m)
                    arglists (:arglists m)]
                (if no-doc ns-map
                    (assoc ns-map var-name
                           (sci/new-var (symbol var-name) @var
                                        (cond-> {:ns fns
                                                 :name (:name m)}
                                          (:macro m) (assoc :macro true)
                                          doc (assoc :doc doc)
                                          arglists (assoc :arglists arglists)))))))
            {}
            (ns-publics ns-symbol))))

;needs to be a fully qualified symbol and maybe required eg: 'de.explorama.shared.data-transformer.util.core 
(def ^:private
  public-ns-list ['taoensso.timbre
                  'clj-time.format
                  'clj-time.coerce])

(def ^:private
  sci-context (sci/init {:namespaces (reduce (fn [acc ns-symbol]
                                               (assoc acc
                                                      ns-symbol
                                                      (copied-ns ns-symbol)))
                                             {}
                                             public-ns-list)}))

(defn eval-mapping
  "Evaluates a given file within a sandbox."
  [mapping-file extra-files & params]
  (doseq [extra-file extra-files]
    (sci/eval-string (slurp extra-file)
                     sci-context))
  (let [desc (sci/eval-string (slurp mapping-file) sci-context)]
    (cond
      (fn? desc) (desc (apply hash-map params))
      (map? desc) desc
      :else (exit 1 "Mapping doesn't return a description."))))