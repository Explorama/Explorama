(ns replace-multi-in-file
  (:require [clojure.string :as str]))

(def replacements (->> (first *command-line-args*)
                       slurp
                       read-string))

(def file (second *command-line-args*))

(println replacements)
(println file)

(defn replace-multi-in-file [replacements file]
  (spit file (reduce (fn [content [old new]]
                       (println old new)
                       (str/replace content old new))
                     (slurp file)
                     replacements)
        :encoding "UTF-8"))

(replace-multi-in-file replacements file)