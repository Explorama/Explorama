#_{:clj-kondo/ignore [:namespace-name-mismatch]}
(ns convert-big-defs
  (:require [babashka.process :as babashka.process]
            [clojure.java.io :as io]))

(defn convert-file [source temp target varname]
  (clojure.java.io/make-parents temp)
  (clojure.java.io/make-parents target)
  (spit temp (slurp source :encoding "UTF-8") :encoding "UTF-8")

  (let [transform-translations
        (str "\n(defn spit-var [] (spit \"" target "\" (pr-str " varname ") :encoding \"UTF-8\"))\n(spit-var)")]

    (spit temp transform-translations :encoding "UTF-8" :append true)
    (babashka.process/shell "bb" temp)))

(convert-file "../../assets/i18n/translations.cljc"
              "temp/translations.bb" "resources/converted/translations.edn" "translations")
(convert-file "../../assets/data/dummy_data_roadmap.cljc"
              "temp/dummy-data-roadmap.bb" "resources/converted/dummy-data-roadmap.edn" "data")
(convert-file "../../assets/data/dummy_data_netflix.cljc"
              "temp/dummy-data-netflix.bb" "resources/converted/dummy-data-netflix.edn" "data")

(println "Done converting big defs.")