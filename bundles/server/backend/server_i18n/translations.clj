(ns server-i18n.translations
  (:require [clojure.edn :as end]))

(defn load-translations []
  (end/read-string (slurp "resources/translations.edn" :encoding "UTF-8") ))