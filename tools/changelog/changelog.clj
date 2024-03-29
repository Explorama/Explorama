(ns changelog
  (:require [clojure.string :as str]))

(def ^:private allow-tags ["[fix]"
                           "[feature]"
                           "Fixes" ; Only for the first time
                           ])

(let [changelog (slurp "CHANGELOG.md" :encoding "UTF-8")
      changelog (->> (str/split changelog #"\n")
                     (filter #(some (fn [t] (str/includes? % t)) allow-tags))
                     (str/join "\n"))]

  (spit "CHANGELOG.md"
        (str changelog "\n")
        :encoding "UTF-8"))
