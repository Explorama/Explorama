(ns de.explorama.backend.common.calculations.data-acs-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.common.calculations.data-acs :as acs]))

(defn parse-date-str [date]
  (if (vector? date)
    (mapv acs/date<- date)
    (acs/date<- date)))

(deftest data-acs
  (testing "handle-string"
    (is (= #{"foo"}
           (acs/handle-string "foo" nil)))
    (is (= #{nil}
           (acs/handle-string nil nil)))
    (is (= #{nil}
           (acs/handle-string nil "foo")))
    (is (= #{"foo"}
           (acs/handle-string "foo" "bar")))
    (is (= #{""}
           (acs/handle-string "" "")))
    (is (= #{"foo"}
           (acs/handle-string "foo" "foo")))
    (is (= #{"foo"}
           (acs/handle-string "foo" #{"foo"})))
    (is (= #{"Benin" "foo"}
           (acs/handle-string "foo" #{"Benin"})))
    (is (= #{"org1" "org2" "org3"}
           (acs/handle-string "org3"  #{"org1" "org2" "org3"}))))
  #_;TODO r1/tests fix this test
    (testing "date-min"
      (is (= (parse-date-str "1997-10-10")
             (acs/date-min "1997-10-10" "1998-08-01" "1998-08-07")))
      (is (= (parse-date-str "0000-01-01")
             (acs/date-min "0001-01-01" "0000-01-01" "0000-10-01" "1000-01-01" "0000-01-10")))
      (is (= (parse-date-str "1999-12-31")
             (acs/date-min "1999-12-31" "2000-01-01")))
      (is (= (parse-date-str "1990-03-29")
             (acs/date-min  "1990-03-29"))))
  #_;TODO r1/tests fix this test
    (testing "date-max"
      (is (= (parse-date-str "1998-08-07")
             (acs/date-max "1997-10-10" "1998-08-01" "1998-08-07")))
      (is (= (parse-date-str "1000-01-01")
             (acs/date-max "0001-01-01" "0000-01-01" "0000-10-01" "1000-01-01" "0000-01-10")))
      (is (= (parse-date-str "2000-01-01")
             (acs/date-max "1999-12-31" "2000-01-01")))
      (is (= (parse-date-str "1990-03-29")
             (acs/date-max  "1990-03-29"))))
  (testing "year-min"
    (is (=  1997
            (acs/year-min "1997-10-10" "1998-08-01" "1998-08-07")))
    (is (= 0000
           (acs/year-min "0001-01-01" "0000-01-01" "0000-10-01" "1000-01-01" "0000-01-10")))
    (is (= 1999
           (acs/year-min "1999-12-31" "2000-01-01")))
    (is (= 1990
           (acs/year-min  "1990-03-29"))))
  (testing "year-max"
    (is (= 1998
           (acs/year-max "1997-10-10" "1998-08-01" "1998-08-07")))
    (is (= 1000
           (acs/year-max "0001-01-01" "0000-01-01" "0000-10-01" "1000-01-01" "0000-01-10")))
    (is (= 2000
           (acs/year-max "1999-12-31" "2000-01-01")))
    (is (= 1990
           (acs/year-max  "1990-03-29"))))
  #_;TODO r1/tests fix this test
  (testing "handle-min-max"
    (is (= (parse-date-str ["1997-10-10" "1998-08-01"])
           (acs/handle-min-max [acs/date-min acs/date-max] "1998-08-01" ["1997-10-10" "1997-12-02"])))
    (is (= ["1997-10-10" "1997-12-02"]
           (acs/handle-min-max [acs/date-min acs/date-max] nil ["1997-10-10" "1997-12-02"])))
    (is (= (parse-date-str ["1996-08-01" "1997-12-02"])
           (acs/handle-min-max [acs/date-min acs/date-max] "1996-08-01" ["1997-10-10" "1997-12-02"])))
    (is (= (parse-date-str ["1997-10-10" "1997-12-02"])
           (acs/handle-min-max [acs/date-min acs/date-max] "1997-10-11" ["1997-10-10" "1997-12-02"])))
    (is (= (parse-date-str ["0000-01-01" "1000-01-01"])
           (acs/handle-min-max [acs/date-min acs/date-max] "0001-01-01" ["0000-01-01" "0000-10-01" "1000-01-01" "0000-01-10"])))))