(ns de.explorama.backend.data-atlas.match
  (:require [clojure.string :as str]))

(defn normalize-query
  "Normalize the search string `query`.
   nil and empty strings are normalized to nil, everything else is just
   lower cased."
  [query]
  (when (seq query)
    (str/lower-case query)))

(defn match?
  "Decide if the normalized search string `query` matches the string `s`.
   A search string matches a string if it is contained in it when comparing
   characters case insensitively."
  [^String s ^String query]
  (and (string? s)
       (str/includes? (str/lower-case s) query)))
