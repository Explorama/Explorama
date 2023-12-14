(ns de.explorama.profiling-tool.data.mosaic
  (:require [de.explorama.profiling-tool.config :refer [test-frame-id]]))

(def raw-layouts {"baselayout1"
                  {:value-assigned [[0 1] [1 6] [6 20] [20 51] [51 1000000]],
                   :name "Base Layout 1",
                   :color-scheme
                   {:name "Scale-2-5",
                    :id "colorscale3",
                    :color-scale-numbers 5,
                    :colors
                    {:0 "#babab9",
                     :1 "#5c5b5b",
                     :2 "#50678a",
                     :3 "#fb8d02",
                     :4 "#e33b3b"}},
                   :datasources ["Data-A"],
                   :id "baselayout1",
                   :field-assignments
                   [["else" "date"]
                    ["else" "datasource"]
                    ["else" "event-type"]
                    ["else" "fact1"]
                    ["notes" "notes"]
                    ["else" "country"]
                    ["organisation" "context1"]
                    ["location" "location"]],
                   :default? true,
                   :timestamp 1627662367930,
                   :attribute-type "number",
                   :attributes ["fact1" "fact1"],
                   :card-scheme "scheme-1"},
                  "baselayout2"
                  {:value-assigned [[] ["*"] [] [] []],
                   :name "Base Layout 2",
                   :color-scheme
                   {:name "Neutral-1-5",
                    :id "colorscale1",
                    :color-scale-numbers 5,
                    :colors
                    {:0 "#0093dd",
                     :1 "#005ca1",
                     :2 "#28166f",
                     :3 "#801d77",
                     :4 "#dd137b"}},
                   :id "baselayout2",
                   :field-assignments
                   [["else" "date"]
                    ["organisation" "context1"]
                    ["notes" "notes"]
                    ["else" "country"]],
                   :default? true,
                   :timestamp 1627662367930,
                   :attribute-type "string",
                   :attributes ["country"],
                   :card-scheme "scheme-2"},
                  "baselayout3"
                  {:value-assigned [[] ["*"] [] [] []],
                   :name "Base Layout 3",
                   :color-scheme
                   {:name "Scale-1-5",
                    :id "colorscale2",
                    :color-scale-numbers 5,
                    :colors
                    {:0 "#4fb34f",
                     :1 "#0292b5",
                     :2 "#033579",
                     :3 "#fb8d02",
                     :4 "#e33b3b"}},
                   :id "baselayout3",
                   :field-assignments [["else" "datasource"] ["else" "date"]],
                   :default? true,
                   :timestamp 1627662367930,
                   :attribute-type "string",
                   :attributes ["country"],
                   :card-scheme "scheme-3"},
                  "baselayout4"
                  {:value-assigned [[] ["*"] [] [] []],
                   :name "Base Layout 4",
                   :color-scheme
                   {:name "Scale-2-5",
                    :id "colorscale3",
                    :color-scale-numbers 5,
                    :colors
                    {:0 "#babab9",
                     :1 "#5c5b5b",
                     :2 "#50678a",
                     :3 "#fb8d02",
                     :4 "#e33b3b"}},
                   :id "baselayout4",
                   :field-assignments [["else" "datasource"] ["else" "date"]],
                   :default? true,
                   :timestamp 1627662367930,
                   :attribute-type "string",
                   :attributes ["event-type"],
                   :card-scheme "scheme-3"},
                  "baselayout5"
                  {:value-assigned [[90 101] [70 90] [30 70] [10 30] [0 10]],
                   :name "Base Layout 5",
                   :color-scheme
                   {:name "Scale-1-5",
                    :id "colorscale2",
                    :color-scale-numbers 5,
                    :colors
                    {:0 "#4fb34f",
                     :1 "#0292b5",
                     :2 "#033579",
                     :3 "#fb8d02",
                     :4 "#e33b3b"}},
                   :datasources ["Data-B"],
                   :id "baselayout5",
                   :field-assignments
                   [["else" "Fact2"]
                    ["organisation" "indicator"]
                    ["location" "country"]
                    ["else" "date"]],
                   :default? true,
                   :timestamp 1627662367930,
                   :attribute-type "number",
                   :attributes ["Rank"],
                   :card-scheme "scheme-2"}})

(def default-sort-grp  {:by :name, :direction :asc, :attr nil, :method nil})
(def sort-grp-by-fact1-min {:by :aggregate, :direction :asc, :attr "fact1", :method :min})
(def sort-grp-by-fact1-max {:by :aggregate, :direction :asc, :attr "fact1", :method :max})
(def sort-grp-by-fact1-sum {:by :aggregate, :direction :asc, :attr "fact1", :method :sum})
(def sort-grp-by-event-count {:by :event-count, :direction :asc, :attr nil, :method nil})

(def sort-by-fact1-asc {:by "fact1", :direction :asc})
(def sort-by-fact1-desc {:by "fact1", :direction :desc})
(def sort-by-context-2-asc {:by "context-2", :direction :asc})
(def sort-by-context-2-desc {:by "context-2", :direction :desc})
(def sort-by-country-asc {:by "country", :direction :asc})
(def sort-by-country-desc {:by "country", :direction :desc})
