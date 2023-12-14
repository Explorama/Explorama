(ns de.explorama.profiling-tool.data.search
  (:require [de.explorama.profiling-tool.data.core :refer [data-a-ds-name data-b-ds-name]]))

(def empty-formdata [])

(def empty-formdata-di {:di/data-tile-ref
                        {"ref1"
                         {:di/identifier "search", :formdata "[]"}},
                        :di/operations
                        [:filter
                         "f-1"
                         "ref1"],
                        :di/filter
                        {"f-1"
                         [:and]}})

(def empty-formdata-data-tile-ref (get-in empty-formdata-di [:di/data-tile-ref
                                                             "ref1"]))

(def notes-formdata [[["datasource" "Datasource"] {:values [data-a-ds-name], :timestamp 1675781762612, :valid? true}]
                     [["notes" "Notes"] {:value "bom", :cond {:value :includes, :label "includes"}, :advanced true, :timestamp 1675781766503, :valid? true}]
                     [["country" "Context"] {:all-values? true, :cond {:value "=", :label "="}, :advanced true, :timestamp 1675781775352, :valid? true}]])


(def location-formdata [[["location" "Context"]
                         {:values [27.74684318726848
                                   -29.663688167665683
                                   68.99213473869747
                                   34.070892162020904],
                          :timestamp 1632748363263,
                          :valid? true}]])

(def location-formdata-di {:di/data-tile-ref
                           {"ref1"
                            {:di/identifier "search",
                             :formdata
                             "[[[\"location\" \"Context\"] {:values [27.74684318726848 -29.663688167665683 68.99213473869747 34.070892162020904], :timestamp 1632748363263, :valid? true}]]"}},
                           :di/operations
                           [:filter
                            "f-1"
                            "ref1"],
                           :di/filter
                           {"f-1"
                            [:and
                             #:data-format-lib.filter{:op :in-geo-rect,
                                                      :prop "location",
                                                      :value
                                                      [27.74684318726848
                                                       -29.663688167665683
                                                       68.99213473869747
                                                       34.070892162020904]}]}})

(def location-formdata-data-tile-ref (get-in location-formdata-di [:di/data-tile-ref
                                                                   "ref1"]))

(def data-a-formdata [[["datasource" "Datasource"]
                       {:values [data-a-ds-name], :timestamp 1632748363263, :valid? true}]])

(def data-a-formdata-di {:di/data-tile-ref
                         {"ref1"
                          {:di/identifier "search",
                           :formdata
                           (str "[[[\"datasource\" \"Datasource\"] {:values [\"" data-a-ds-name "\"], :timestamp 1632748363263, :valid? true}]]")}},
                         :di/operations
                         [:filter
                          "f-1"
                          "ref1"],
                         :di/filter
                         {"f-1"
                          [:and]}})

(def data-a-formdata-data-tile-ref (get-in data-a-formdata-di [:di/data-tile-ref
                                                               "ref1"]))

(def fact1-formdata [[["fact1" "Fact"]
                      {:timestamp 1674219048345
                       :advanced true
                       :cond {:value "=", :label "="}
                       :all-values? true}]])

(def fact1-formdata-di {:di/data-tile-ref
                        {"ref1"
                         {:di/identifier "search",
                          :formdata
                          "[[[\"fact1\" \"Fact\"] {:timestamp 1674219048345, :advanced true, :cond {:value \"=\", :label \"=\"}, :all-values? true}]]"}},
                        :di/operations
                        [:filter
                         "f-1"
                         "ref1"],
                        :di/filter
                        {"f-1"
                         [:and
                          [:or
                           #:data-format-lib.filter{:op :non-empty,
                                                    :prop "fact1",
                                                    :value nil}]]}})

(def country-50-formdata-di {:di/data-tile-ref
                             {"ref1"
                              {:di/identifier "search",
                               :formdata
                               (str "[[[\"datasource\" \"Datasource\"] {:values [\"" data-a-ds-name "\"], :timestamp 1632748363263, :valid? true}]
                            [[\"country\" \"Context\"] {:values [\"country-50\"], :timestamp 1632748363268, :valid? true}]]")}},
                             :di/operations
                             [:filter
                              "f-1"
                              "ref1"],
                             :di/filter
                             {"f-1"
                              [:and
                               [:or
                                #:data-format-lib.filter{:op :non-empty,
                                                         :prop "fact1",
                                                         :value nil}]]}})

(def data-a-100k+-data-formdata-di
  {:di/data-tile-ref
   {"di-1"
    {:di/identifier "search",
     :formdata
     (str "[[[\"datasource\" \"Datasource\"] {:values [\"" data-a-ds-name "\"], :timestamp 1632748363263, :valid? true}]
      [[\"country\" \"Context\"] {:values [\"country-20\",\"country-30\",\"country-40\",\"country-50\",\"country-60\",\"country-70\",\"country-80\"], :timestamp 1632748363268, :valid? true}]]")}},
   :di/operations
   [:filter
    "f-1"
    "di-1"],
   :di/filter
   {"f-1"
    [:and
     [:or
      #:data-format-lib.filter{:op :non-empty,
                               :prop "fact1",
                               :value nil}]]}})

(def fact1-formdata-data-tile-ref (get-in fact1-formdata-di [:di/data-tile-ref
                                                             "ref1"]))

(def organisation-attr ["organisation" "Context"])
(def country-attr ["country" "Context"])

(def fact1-local-filter [:and
                         [:and
                          {:data-format-lib.filter/op :>=
                           :data-format-lib.filter/prop "fact1"
                           :data-format-lib.filter/value 5}
                          {:data-format-lib.filter/op :<=
                           :data-format-lib.filter/prop "fact1"
                           :data-format-lib.filter/value 500}]])


(def context-2-local-filter [:and
                             [:or
                              {:data-format-lib.filter/op :=
                               :data-format-lib.filter/prop "context-2"
                               :data-format-lib.filter/value "context2-100"}]])
