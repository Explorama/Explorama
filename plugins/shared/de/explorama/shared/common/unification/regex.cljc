(ns de.explorama.shared.common.unification.regex)

(defn search-pattern [search-term]
  #?(:clj
     (. java.util.regex.Pattern
        (compile search-term java.util.regex.Pattern/CASE_INSENSITIVE))
     :cljs (js/RegExp. search-term "i")))
