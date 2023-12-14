(ns de.explorama.profiling-tool.data.map)

(def ^:private color-scheme {:name "Scale-1-5"
                             :id "colorscale2"
                             :color-scale-numbers 5
                             :colors {:0 "#4fb34f"
                                      :1 "#0292b5"
                                      :2 "#033579"
                                      :3 "#fb8d02"
                                      :4 "#e33b3b"}})

(def fact1-layout {:name "fact1 Layout"
                        :id "fact1-layout"
                        :type :marker
                        :attributes ["fact1" "fact1"]
                        :attribute-type "number"
                        :value-assigned [[0 1]
                                         [1 6]
                                         [6 20]
                                         [20 51]
                                         [51 1000000]]
                        :color-scheme color-scheme
                        :full-opacity? true
                        :card-scheme "scheme-1"
                        :field-assignments [["else" "date"]
                                            ["else" "datasource"]
                                            ["else" "context-2"]
                                            ["else" "fact1"]
                                            ["notes" "notes"]
                                            ["else" "country"]
                                            ["organisation" "organisation"]
                                            ["location" "location"]]})

(def context-2-layout {:id "baselayout4"
                        :name "Base Layout 4"
                        :type :marker
                        :attributes ["context-2"]
                        :attribute-type "string"
                        :value-assigned  [[] ["*"] [] [] []]
                        :color-scheme color-scheme
                        :full-opacity? true
                        :card-scheme "scheme-3"
                        :field-assignments [["else" "datasource"]
                                            ["else" "date"]]})

(def not-usable-layout {:id "unusable"
                        :name "unusable"
                        :type :marker
                        :attributes ["foo"]
                        :attribute-type "string"
                        :value-assigned  [[] ["*"] [] [] []]
                        :color-scheme color-scheme
                        :full-opacity? true
                        :card-scheme "scheme-3"
                        :field-assignments [["else" "datasource"]
                                            ["else" "date"]]})

(def all-raw-marker-layouts [fact1-layout 
                             context-2-layout])

(def raw-heatmap-layer-global {:name "fact1 heatmap point density"
                               :id "fact1-heatmap"
                               :type :heatmap
                               :attributes ["fact1" "fact1"]
                               :extrema :global})
(def raw-heatmap-layer-local {:name "fact1 heatmap weighted"
                              :id "fact1-heatmap-weighted"
                              :type :heatmap
                              :attributes ["fact1" "fact1"]
                              :extrema :local})

(def raw-movement-layer {:name "fact1 movement"
                         :id "fact1-movement"
                         :type :movement
                         :attributes ["fact1" "fact1"]
                         :source "country"
                         :target "location"})

(def fact1-country-sum-layer
  {:name "fact1 country sum"
   :id "fact1-country-sum"
   :type :feature
   :attributes ["fact1" "fact1"]
   :attribute-type "number"
   :color-scheme color-scheme
   :value-assigned [[0 1]
                    [1 6]
                    [6 20]
                    [20 51]
                    [51 1000000]]
   :feature-layer-id "country-coloring"
   :aggregate-method :sum})

(def country-num-of-events
  {:name "country number-of-events"
   :id "country-number-of-events"
   :type :feature
   :attributes ["number-of-events"]
   :attribute-type "number"
   :color-scheme color-scheme
   :value-assigned [[0 1]
                    [1 6]
                    [6 20]
                    [20 51]
                    [51 1000000]]
   :feature-layer-id "country-coloring"
   :aggregate-method :number-of-events})

(def fact1-province-sum-layer
  {:name "fact1 province sum"
   :id "fact1-province-sum"
   :type :feature
   :attributes ["fact1"]
   :attribute-type "number"
   :color-scheme color-scheme
   :value-assigned [[0 1]
                    [1 6]
                    [6 20]
                    [20 51]
                    [51 1000000]]
   :feature-layer-id "divisions-coloring"
   :aggregate-method :sum})

(def province-num-of-events
  {:name "province number-of-events"
   :id "province-number-of-events"
   :type :feature
   :attributes ["number-of-events"]
   :attribute-type "number"
   :color-scheme color-scheme
   :value-assigned [[0 1]
                    [1 6]
                    [6 20]
                    [20 51]
                    [51 1000000]]
   :feature-layer-id "divisions-coloring"
   :aggregate-method :number-of-events})

(def all-raw-feature-layers [raw-heatmap-layer-global
                             raw-heatmap-layer-local
                             raw-movement-layer
                             fact1-country-sum-layer
                             country-num-of-events
                             fact1-province-sum-layer
                             province-num-of-events])
