(ns de.explorama.backend.common.calculations.data-acs-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.common.calculations.data-acs :as acs]
            [de.explorama.shared.common.unification.time :as time]))

(defn unparse-date-str [date]
  (cond (vector? date)
        (mapv #(time/unparse acs/formatter %) date)
        date
        (time/unparse acs/formatter date)
        :else nil))

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
  (testing "date-min"
    (is (= "1997-10-10"
           (unparse-date-str (acs/date-min "1997-10-10" "1998-08-01" "1998-08-07"))))
    (is (= "1999-12-31"
           (unparse-date-str (acs/date-min "1999-12-31" "2000-01-01"))))
    (is (= "1990-03-29"
           (unparse-date-str (acs/date-min  "1990-03-29")))))
  (testing "date-max"
    (is (= "1998-08-07"
           (unparse-date-str (acs/date-max "1997-10-10" "1998-08-01" "1998-08-07"))))
    (is (= "2000-01-01"
           (unparse-date-str (acs/date-max "1999-12-31" "2000-01-01"))))
    (is (= "1990-03-29"
           (unparse-date-str (acs/date-max  "1990-03-29")))))
  (testing "year-min"
    (is (= 1997
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
  (testing "handle-min-max"
    (is (= ["1997-10-10" "1998-08-01"]
           (unparse-date-str (acs/handle-min-max [acs/date-min acs/date-max] "1998-08-01" ["1997-10-10" "1997-12-02"]))))
    (is (= ["1997-10-10" "1997-12-02"]
           (unparse-date-str (acs/handle-min-max [acs/date-min acs/date-max] nil ["1997-10-10" "1997-12-02"]))))
    (is (= ["1996-08-01" "1997-12-02"]
           (unparse-date-str (acs/handle-min-max [acs/date-min acs/date-max] "1996-08-01" ["1997-10-10" "1997-12-02"]))))
    (is (= ["1997-10-10" "1997-12-02"]
           (unparse-date-str (acs/handle-min-max [acs/date-min acs/date-max] "1997-10-11" ["1997-10-10" "1997-12-02"]))))))
