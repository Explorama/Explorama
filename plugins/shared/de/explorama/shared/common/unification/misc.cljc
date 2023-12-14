(ns de.explorama.shared.common.unification.misc
  #?(:clj (:require [jsonista.core :as json])
     :cljs (:require [cljs.reader :as edn]))
  #?(:clj (:import (java.text NumberFormat)
                   (java.util Locale))))

(def cljc-read-string #?(:clj read-string
                         :cljs edn/read-string))

(def cljc-parse-int #?(:clj #(Integer/parseInt %)
                       :cljs js/parseInt))

(def cljc-parse-double #?(:clj #(Double/parseDouble %)
                          :cljs js/parseFloat))

(def cljc-bigdec #?(:clj bigdec
                    :cljs js/Number))

(def cljc-uuid #?(:clj (fn [] (str (java.util.UUID/randomUUID)))
                  :cljs (fn [] (str (random-uuid)))))

(defn cljc-number-locale [lang]
  #?(:clj (case lang
            :de-DE  (Locale. "de" "DE")
            (Locale. "en" "GB"))
     :cljs (js/Intl.NumberFormat. (case lang
                                    :de-DE "de-DE"
                                    "en-GB"))))

(defn cljc-format-number [instance number]
  #?(:clj (.format (NumberFormat/getInstance instance) (cljc-bigdec number))
     :cljs (.format instance number)))

(def cljc-json->keys-edn #?(:clj (fn [data] (json/read-value data
                                                             json/keyword-keys-object-mapper))
                            :cljs (fn [data] (js->clj (js/JSON.parse data) :keywordize-keys true))))

(def cljc-max-int #?(:clj Integer/MAX_VALUE
                     :cljs js/Number.MAX_SAFE_INTEGER))

(def cljc-min-int #?(:clj Integer/MIN_VALUE
                     :cljs js/Number.MIN_SAFE_INTEGER))


(def cljc-json->edn #?(:clj json/read-value
                       :cljs (fn [data] (js->clj (js/JSON.parse data)))))

(def cljc-edn->json #?(:clj json/write-value-as-string
                       :cljs (fn [data] (js/JSON.stringify (clj->js data)))))

(def double-negativ-infinity #?(:clj java.lang.Double/NEGATIVE_INFINITY
                                :cljs js/Number.NEGATIVE_INFINITY))

(def double-positiv-infinity #?(:clj java.lang.Double/POSITIVE_INFINITY
                                :cljs js/Number.POSITIVE_INFINITY))
