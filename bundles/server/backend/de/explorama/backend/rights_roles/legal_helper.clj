(ns de.explorama.backend.rights-roles.legal-helper
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [de.explorama.backend.configuration.middleware.i18n :as mi18n]
            [de.explorama.backend.rights-roles.config :as config]
            [taoensso.timbre :refer [debug]]))

(def available-languages [:en-GB :de-DE]) ; TODO I18n-Rework do it dynamically

(def all-lang-variations
  (into {}
        (mapcat (fn [lang]
                  (let [lang-name (name lang)
                        [short] (str/split lang-name  #"-")]
                    [[short lang]
                     [lang-name lang]]))
                available-languages)))

(def impressum-folder-path (str config/legal-folder "/impressum"))
(def privacy-folder-path (str config/legal-folder "/privacy"))
(def terms-of-use-folder-path (str config/legal-folder "/terms-of-use"))
(def accessibility-folder-path (str config/legal-folder "/accessibility"))

(defonce legal-dict (atom {}))

(defn- file->language-key [^java.io.File file]
  (let [filename (.getName file)
        [lang] (str/split filename #"\.")]
    (keyword lang)))

(defn- add-legal-files [acc legalkey files]
  (reduce (fn [acc file]
            (let [lang-key (file->language-key file)
                  content (edn/read-string (slurp file))]
              (assoc-in acc [legalkey lang-key] content)))
          acc
          files))

(defn init-legal-dict []
  (debug "init legal dictonary")
  (let [impressum-files (.listFiles (io/file impressum-folder-path))
        privacy-files (.listFiles (io/file privacy-folder-path))
        terms-of-use-files (.listFiles (io/file terms-of-use-folder-path))
        accessibility-files (.listFiles (io/file accessibility-folder-path))]
    (reset! legal-dict
            (-> {}
                (add-legal-files :impressum impressum-files)
                (add-legal-files :privacy privacy-files)
                (add-legal-files :terms-of-use terms-of-use-files)
                (add-legal-files :accessibility accessibility-files)))))
(defn- parse-accepts
  "Parse client capabilities and associated q-values"
  [accepts-string]
  (map
   (fn [[_ name q]]
     {:name name :q (Float/parseFloat (or q "1"))})
   (re-seq #"([^,;\s]+)[^,]*?(?:;\s*q=(0(?:\.\d{0,3})?|1(?:\.0{0,3})?))?" accepts-string)))

(defn req->language-key [{:keys [headers]}]
  (let [accept-language (get headers "accept-language")
        parsed-accept (when (seq accept-language)
                        (parse-accepts accept-language))]
    (or (some (fn [{:keys [name]}]
                (get all-lang-variations name))
              parsed-accept)
        :en-GB)))

(defn- type->legal-desc [type lang]
  (get-in @legal-dict [type lang]
          [:div {:class "explorama-overlay-content animation-fade-in-up"} "Missing"]))

(defn- close-button [type-id]
  [:button {:class (str/join " " ["btn-tertiary"
                                  "btn-icon"
                                  "explorama-overlay-close"])
            :onClick (str "hideLegal('" type-id "');")}
   [:span {:class (str/join " "
                            ["icon-close"])}]])

(defn- show-button [type-id label]
  [:a.btn-link.text-xs
   {:href "#"
    :onClick (str "showLegal('" type-id "');")}
   label])

(defonce cached-hiccup (atom {}))

(defn request->hiccup-desc [req]
  (let [language-key (req->language-key req)]
    (cond config/show-legal?
          (if-let [desc (get @cached-hiccup language-key)]
            desc
            (let [{:keys [terms-of-use-label impressum-label privacy-label]} (mi18n/get-translations language-key)
                  hiccup-desc (reduce (fn [acc [type type-id]]
                                        (let [type-desc (type->legal-desc type language-key)]
                                          (conj acc
                                                [:div {:id type-id
                                                       :class "hidden-legal"}
                                                 [:div {:class "explorama-overlay", :style "display: flex;", :id "overlay"}
                                                  type-desc
                                                  (close-button type-id)]])))
                                      [:div
                                       [:div {:class (str/join " "
                                                               ["absolute"
                                                                "center-x"
                                                                "bottom-8"])}
                                        (show-button "terms"
                                                     (str terms-of-use-label " | "))
                                        (show-button "privacy"
                                                     (str privacy-label " | "))
                                        (show-button "impressum"
                                                     impressum-label)]]
                                      [[:terms-of-use "terms"]
                                       [:privacy "privacy"]
                                       [:impressum "impressum"]])]
              (swap! cached-hiccup assoc language-key hiccup-desc)
              hiccup-desc))
          config/show-accessibility?
          (if-let [desc (get @cached-hiccup language-key)]
            desc
            (let [{:keys [accessibility-label]} (mi18n/get-translations language-key)
                  hiccup-desc (reduce (fn [acc [type type-id]]
                                        (let [type-desc (type->legal-desc type language-key)]
                                          (conj acc
                                                [:div {:id type-id
                                                       :class "hidden-legal"}
                                                 [:div {:class "explorama-overlay", :style "display: flex;", :id "overlay"}
                                                  type-desc
                                                  (close-button type-id)]])))
                                      [:div
                                       [:div {:class (str/join " "
                                                               ["absolute"
                                                                "center-x"
                                                                "bottom-8"])}
                                        (show-button "accessibility"
                                                     accessibility-label)]]
                                      [[:accessibility "accessibility"]])]
              (swap! cached-hiccup assoc language-key hiccup-desc)
              hiccup-desc)))))
