(ns de.explorama.shared.data-format.core-test
  (:require
   [de.explorama.shared.data-format.filter :as lib.filter]
   [de.explorama.shared.data-format.dates]
   [de.explorama.shared.data-format.data-instance :as di]
   [de.explorama.shared.data-format.operations-test :as opt]
   [de.explorama.shared.data-format.operations :as op]
   [de.explorama.shared.data-format.core :as dfl.core]
   [malli.core :as m]
   [malli.error :as me]
   #?(:clj  [clojure.test :as t]
      :cljs [cljs.test :as t :include-macros true])))

(def data-fixture [{"date" "1997-07-27", "fulltext" "33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"}
                   {"date" "1997-07-27", "fulltext" "nicht-drin-text"}
                   {"date" "2020-07-27", "fulltext" "33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"}])

(t/deftest test-filter-data
  (t/testing
   "[Suche: Textsuche beachtet keine Groß-/ und Kleinschreibung](http://10.1.30.14/issues/6073)"
    ;; "Die Direktsuche und die freien Textattribute, wie Notes und Comments, in der komplexen Suche beachten bei einem
    ;; eingegebenen Wort nicht die Groß-/ und Kleinschreibung."
    (t/is (= (de.explorama.shared.data-format.core/filter-data
               ;; filtering for "BoMB" in notes should result in case-insensitive matches
              [:and #:de.explorama.shared.data-format.filter{:op :includes, :prop "notes", :value "BoMB"}]
              [{"notes" "bombing"}
               {"notes" "no-bmb"}])
             [{"notes" "bombing"}]))
    (t/testing "German Umlauts"
      (let [all-data [{"notes" "Im Frühjahr blühen Weidenkätzchen"}
                      {"notes" "Schmusekätzchen sind keine Kratzbürsten."}
                      {"notes" "Kätzchenbilder sind der Sinn und Nutzen des Internet."}
                      {"notes" "Wer sich nicht nach der Decke streckt, dem bleiben nachts die Füße unbedeckt."}]
            kätzchen-data #{{"notes" "Im Frühjahr blühen Weidenkätzchen"}
                            {"notes" "Schmusekätzchen sind keine Kratzbürsten."}
                            {"notes" "Kätzchenbilder sind der Sinn und Nutzen des Internet."}}]
        (t/is (= (set (de.explorama.shared.data-format.core/filter-data
                       [:and #:de.explorama.shared.data-format.filter{:op :includes, :prop "notes", :value "Kätzchen"}]
                       all-data))
                 kätzchen-data))
        (t/is (= (set (de.explorama.shared.data-format.core/filter-data
                       [:and #:de.explorama.shared.data-format.filter{:op :includes, :prop "notes", :value "KÄTZCHEN"}]
                       all-data))
                 kätzchen-data))
        (t/is (= (set (de.explorama.shared.data-format.core/filter-data
                       [:and #:de.explorama.shared.data-format.filter{:op :includes, :prop "notes", :value "kätzchen"}]
                       all-data))
                 kätzchen-data))
        (t/is (empty? (de.explorama.shared.data-format.core/filter-data
                       [:and #:de.explorama.shared.data-format.filter{:op :includes, :prop "notes", :value "kaetzchen"}]
                       all-data)))))

    (t/testing "German ß<->ẞ (latter is the capital sharp s)"
      (let [all-data [{"notes" "GROẞE VERÄNDERUNGEN STEHEN BEVOR"}
                      {"notes" "große veränderungen stehen bevor"}
                      {"notes" "GROSSE VERÄNDERUNGEN STEHEN BEVOR"}
                      {"notes" "Wer sich nicht nach der Decke streckt, dem bleiben nachts die Füße unbedeckt."}]
            große-data #{{"notes" "GROẞE VERÄNDERUNGEN STEHEN BEVOR"}
                         {"notes" "große veränderungen stehen bevor"}
                         ;; When searching for "ß" or "ẞ", we do not return "ss". This would be wrong.
                         #_{"notes" "GROSSE VERÄNDERUNGEN STEHEN BEVOR"}}]
        (t/is (= (set (de.explorama.shared.data-format.core/filter-data
                       [:and #:de.explorama.shared.data-format.filter{:op :includes, :prop "notes", :value "GROẞE"}]
                       all-data))
                 große-data))
        (t/is (= (set (de.explorama.shared.data-format.core/filter-data
                       [:and #:de.explorama.shared.data-format.filter{:op :includes, :prop "notes", :value "große"}]
                       all-data))
                 große-data))
        (t/is (= (set (de.explorama.shared.data-format.core/filter-data
                       [:and #:de.explorama.shared.data-format.filter{:op :includes, :prop "notes", :value "GROSSE"}]
                       all-data))
                 #{{"notes" "GROSSE VERÄNDERUNGEN STEHEN BEVOR"}})))))

  (t/is (= [{"date" "1997-07-27", "fulltext" "33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"}
            {"date" "1997-07-27", "fulltext" "nicht-drin-text"}]
           (dfl.core/filter-data [:and
                                  {:de.explorama.shared.data-format.filter/op :>=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/full-date, :de.explorama.shared.data-format.filter/value "1997-07-27"}
                                  {:de.explorama.shared.data-format.filter/op :<=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/full-date, :de.explorama.shared.data-format.filter/value "1997-07-27"}]
                                 data-fixture)))

  (t/is (= [{"date" "1997-07-27", "fulltext" "33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"}
            {"date" "2020-07-27", "fulltext" "33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"}]
           (dfl.core/filter-data [:and {:de.explorama.shared.data-format.filter/op :in, :de.explorama.shared.data-format.filter/prop "fulltext", :de.explorama.shared.data-format.filter/value
                                        #{"33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"
                                          "9281063a53d401d75ddf4e0003c72a642f061d907480f768daa5fb88161a3875"
                                          "44b98b02d264c98164d4a629adc946a9b22305f25b1f823909a8afbe28bfd846"
                                          "ac9f6dd080352c274687e482ce8ee114fdd96084a7f58559e2aae7a154dd815e"
                                          "deca73b70efaa4629b121d9b54eff2b7203148a6ea582afb6e4826364914d26"
                                          "511acd880f8c8f08b5a3550a6cd4038e2a6aa1347ae956ed9b2589d25eba1a92"
                                          "483c7ca0655a94804fdb027fe8282cda81c761b602f34d14fa7478357538736b"}}]
                                 data-fixture)))

  (t/is (= [{"date" "1997-07-27", "fulltext" "33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"}]
           (dfl.core/filter-data [:and [:and
                                        {:de.explorama.shared.data-format.filter/op :>=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/full-date, :de.explorama.shared.data-format.filter/value "1997-07-27"}
                                        {:de.explorama.shared.data-format.filter/op :<=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/full-date, :de.explorama.shared.data-format.filter/value "1997-07-27"}]
                                  {:de.explorama.shared.data-format.filter/op :in, :de.explorama.shared.data-format.filter/prop "fulltext", :de.explorama.shared.data-format.filter/value
                                   #{"33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"
                                     "9281063a53d401d75ddf4e0003c72a642f061d907480f768daa5fb88161a3875"
                                     "44b98b02d264c98164d4a629adc946a9b22305f25b1f823909a8afbe28bfd846"
                                     "ac9f6dd080352c274687e482ce8ee114fdd96084a7f58559e2aae7a154dd815e"
                                     "deca73b70efaa4629b121d9b54eff2b7203148a6ea582afb6e4826364914d26"
                                     "511acd880f8c8f08b5a3550a6cd4038e2a6aa1347ae956ed9b2589d25eba1a92"
                                     "483c7ca0655a94804fdb027fe8282cda81c761b602f34d14fa7478357538736b"}}]
                                 data-fixture)))
  (t/is (= [{"date" "1997-07-27", "fulltext" "33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"}
            {"date" "1997-07-27", "fulltext" "nicht-drin-text"}
            {"date" "2020-07-27", "fulltext" "33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"}]
           (dfl.core/filter-data [:or [:and
                                       {:de.explorama.shared.data-format.filter/op :>=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/full-date, :de.explorama.shared.data-format.filter/value "1997-07-27"}
                                       {:de.explorama.shared.data-format.filter/op :<=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/full-date, :de.explorama.shared.data-format.filter/value "1997-07-27"}]
                                  {:de.explorama.shared.data-format.filter/op :in, :de.explorama.shared.data-format.filter/prop "fulltext", :de.explorama.shared.data-format.filter/value
                                   #{"33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"
                                     "9281063a53d401d75ddf4e0003c72a642f061d907480f768daa5fb88161a3875"
                                     "44b98b02d264c98164d4a629adc946a9b22305f25b1f823909a8afbe28bfd846"
                                     "ac9f6dd080352c274687e482ce8ee114fdd96084a7f58559e2aae7a154dd815e"
                                     "deca73b70efaa4629b121d9b54eff2b7203148a6ea582afb6e4826364914d26"
                                     "511acd880f8c8f08b5a3550a6cd4038e2a6aa1347ae956ed9b2589d25eba1a92"
                                     "483c7ca0655a94804fdb027fe8282cda81c761b602f34d14fa7478357538736b"}}]
                                 data-fixture))))

(defn data-tile-lookup [{id :di/identifier}]
  (case id
    "foo"
    [{:identifier "foo" "bucket" "default"}]
    "bar"
    [{:identifier "bar" "bucket" "default"}]))

(defn data-tile-retrieval [missing-data-tiles]
  (into {}
        (map (fn [{id :identifier :as data-tile}]
               (case id
                 "foo"
                 [data-tile [{"id" "bar1" "event-type" "bar"}]]
                 "bar"
                 [data-tile [{"id" "foo1" "event-type" "foo"}]]))
             missing-data-tiles)))

(def data-instance
  {:di/data-tile-ref {"di1" {:di/identifier "foo"}
                      "di2" {:di/identifier "bar"}}
   :di/operations [:union nil
                   [:filter "f1" "di1"]
                   [:filter "f2" "di2"]]
   :di/filter {"f1" [:and]
               "f2" [:and]}})

(t/deftest test-api
  (t/testing "diid tests"
    (t/is (= (vec (dfl.core/transform data-instance
                                      data-tile-lookup
                                      data-tile-retrieval))
             [{"id" "bar1" "event-type" "bar"}
              {"id" "foo1" "event-type" "foo"}])))
  (t/testing "malli schema"
    (when-not (m/validate di/data-instance data-instance)
      (println (-> (m/explain di/data-instance data-instance)
                   (me/humanize))))
    (t/is (m/validate di/data-instance data-instance))))

(t/deftest test-check-datapoint
  (t/testing "simple check no date"
    (t/is (dfl.core/check-datapoint nil
                                    {"a" "a"
                                     "b" "b"
                                     "date" "2023-04-14"}))
    (t/is (dfl.core/check-datapoint [:and #:de.explorama.shared.data-format.filter{:op :=, :prop "a", :value "a"}]
                                    {"a" "a"
                                     "b" "b"
                                     "date" "2023-04-14"}))
    (t/is (not (dfl.core/check-datapoint [:and #:de.explorama.shared.data-format.filter{:op :=, :prop "a", :value "foo"}]
                                         {"a" "a"
                                          "b" "b"
                                          "date" "2023-04-14"}))))
  (t/testing "simple check with date"
    (t/is (dfl.core/check-datapoint [:and
                                     #:de.explorama.shared.data-format.filter{:op :=, :prop "a", :value "a"}
                                     #:de.explorama.shared.data-format.filter{:op :=, :prop :de.explorama.shared.data-format.dates/month, :value 4}]
                                    {"a" "a"
                                     "b" "b"
                                     "date" "2023-04-14"}))
    (t/is (dfl.core/check-datapoint [:and
                                     #:de.explorama.shared.data-format.filter{:op :=, :prop "a", :value "a"}
                                     #:de.explorama.shared.data-format.filter{:op :>=, :prop :de.explorama.shared.data-format.dates/full-date, :value "2014-02-03"}]
                                    {"a" "a"
                                     "b" "b"
                                     "date" "2023-04-14"}))
    (t/is (dfl.core/check-datapoint [:and
                                     #:de.explorama.shared.data-format.filter{:op :=, :prop "a", :value "a"}
                                     #:de.explorama.shared.data-format.filter{:op :>, :prop :de.explorama.shared.data-format.dates/year, :value 2012}]
                                    {"a" "a"
                                     "b" "b"
                                     "date" "2023-04-14"}))
    (t/is (not (dfl.core/check-datapoint [:and
                                          #:de.explorama.shared.data-format.filter{:op :=, :prop "a", :value "a"}
                                          #:de.explorama.shared.data-format.filter{:op :<, :prop :de.explorama.shared.data-format.dates/full-date, :value "2014-02-03"}]
                                         {"a" "a"
                                          "b" "b"
                                          "date" "2023-04-14"})))))
