(ns de.explorama.frontend.configuration.config)

(def debug?
  ^boolean goog.DEBUG)

(def default-namespace :configuration)
(def default-vertical-str (name default-namespace))
(def tool-name "tool-settings")

(def save-message-life-time-ms 1500)

(def selectable-color-limit 15)
(def direct-search-unified :de.explorama.frontend.search.direct-search/unified)

(def min-role-title-length 2)

(def create-layout-selection {:fields [[:0 ["else" "date"]]
                                       [:2 ["else" "datasource"]]
                                       [:2 ["location" "country"]]
                                       [:3 ["notes" "notes"]]]
                              :field-assignments [["else" "date"]
                                                  ["else" "datasource"]
                                                  ["location" "country"]
                                                  ["notes" "notes"]]
                              :attribute-type :number
                              :color-scheme {:color-scale-numbers 1
                                             :colors {:0 "#aaaadd"}}
                              :value-assigned [[##-Inf ##Inf]]
                              :card-scheme "scheme-3"})

(def export-datasource-reference-max-length 200)
(def export-custom-description-max-length 160)