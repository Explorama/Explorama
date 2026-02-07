(require '[babashka.process :refer [shell]]
         '[clojure.string :as str])

(loop [lines (-> (shell {:out :string} "clj -M:test-ci --timeout 360000")
                 :out
                 (str/split #"\n"))
       result ""
       toggle false]
  (if (empty? lines)
    (spit "report.xml" result :encoding "UTF-8")
    (cond (= "### report start ###" (first lines))
          (recur (rest lines) result true)
          (= "### report end ###" (first lines))
          (recur (rest lines) result false)
          toggle
          (recur (rest lines) (str result "\n" (first lines)) toggle)
          :else
          (recur (rest lines) result toggle))))

