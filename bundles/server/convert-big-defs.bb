(defn convert-file [source temp target varname]
  (clojure.java.io/make-parents temp)
  (spit temp (slurp source :encoding "UTF-8") :encoding "UTF-8")

  (def transform-translations
    (str "\n(defn spit-var [] (spit \"" target "\" (pr-str " varname ") :encoding \"UTF-8\"))\n(spit-var)"))

  (spit temp transform-translations :encoding "UTF-8" :append true)

  (babashka.process/shell "bb" temp))

(convert-file "../../assets/i18n/translations.cljc"
              "temp/translations.bb" "temp/translations.edn"   "translations")

(println "Done converting big defs.")