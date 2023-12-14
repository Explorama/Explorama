(ns de.explorama.frontend.woco.filter-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [cljsjs.moment]
            [de.explorama.frontend.woco.frame.filter.util :as util]
            [de.explorama.shared.common.test-data :as td]))

(def app-state {:selected-ui               {td/country      {:std [{:value td/country-a
                                                                    :label  td/country-a}]}
                                            td/org {:std [{:value "Foo"
                                                           :label "Foo"}
                                                          {:value "Bar"
                                                           :label "Bar"}]}
                                            td/fact-1   {:std [1 4]}
                                            "date"         {:year [2004 2016]}}
                :selected-ui-attributes [["date" :year]
                                         [td/fact-1 :std]
                                         [td/country :std]]
                :data-acs  [[td/fact-1 {:std {:type :number :vals #{1 4}}}]
                            [td/country {:std {:type :string :vals #{td/country-a "Foo" "Bar"}}}]
                            ["date" {:year {:type :year :vals #{2004 2016}}}]]})


(def ui-description {:selected-ui          {td/country {:std [{:value td/country-a
                                                               :label  td/country-a}]}
                                            td/fact-1      {:std [1 4]}
                                            "date"            {:year [2004 2016]}}
                     :selected-ui-attributes [["date" :year]
                                              [td/fact-1 :std]
                                              [td/country :std]]})

(def filter-desc [:and
                  [:and
                   {:data-format-lib.filter/op :>=, :data-format-lib.filter/prop :data-format-lib.dates/year, :data-format-lib.filter/value 2004}
                   {:data-format-lib.filter/op :<=, :data-format-lib.filter/prop :data-format-lib.dates/year, :data-format-lib.filter/value 2016}]
                  [:and
                   {:data-format-lib.filter/op :>=, :data-format-lib.filter/prop td/fact-1, :data-format-lib.filter/value 1}
                   {:data-format-lib.filter/op :<=, :data-format-lib.filter/prop td/fact-1, :data-format-lib.filter/value 4}]
                  [:or
                   {:data-format-lib.filter/op :=, :data-format-lib.filter/prop td/country, :data-format-lib.filter/value td/country-a}]])


(deftest translate-ui-desc-to-filter-desc
  (testing "Translate UI app state as a filter description."
    (is (= (util/ui-app-state->filter-desc ui-description (:data-acs app-state)) filter-desc))))

(deftest translate-filter-desc-to-ui-desc
  (testing "Translate filter description as UI app state."
    (is (= (util/filter-desc->ui-desc filter-desc) ui-description))))


(def filter-date [:and
                  [:and
                   {:data-format-lib.filter/op :>=, :data-format-lib.filter/prop :data-format-lib.dates/year, :data-format-lib.filter/value 2004}
                   {:data-format-lib.filter/op :<=, :data-format-lib.filter/prop :data-format-lib.dates/year, :data-format-lib.filter/value 2016}]
                  [:and
                   {:data-format-lib.filter/op :>=, :data-format-lib.filter/prop :data-format-lib.dates/full-date, :data-format-lib.filter/value "2004-01-31"}
                   {:data-format-lib.filter/op :<=, :data-format-lib.filter/prop :data-format-lib.dates/full-date, :data-format-lib.filter/value "2015-01-31"}]])


(deftest translate-date-filter-desc-to-date-ui-desc
  (testing "Filter description to UI description date transformation"
    (let [date-desc (util/filter-desc->ui-desc filter-date)
          {:keys [selected-ui]} date-desc
          start-date (get-in selected-ui ["date" :std :start-date])
          end-date (get-in selected-ui ["date" :std :end-date])
          years (get-in selected-ui ["date" :year])]
      (is (.isSame start-date "2004-01-31"))
      (is (.isSame end-date "2015-01-31"))
      (is (= (first years) 2004))
      (is (= (second years) 2016)))))
