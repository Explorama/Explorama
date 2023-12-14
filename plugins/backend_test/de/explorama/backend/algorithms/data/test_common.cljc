(ns de.explorama.backend.algorithms.data.test-common
  (:require [de.explorama.shared.common.test-data :as td]))

(def data
  [{"date" "1999-10-13" td/fact-1 0
    "notes" "stuff"}
   {"date" "1999-10-14" td/fact-1 3
    "notes" "stuff"}
   {"date" "1999-11-13" td/fact-1 1
    "notes" "stuff"}
   {"date" "1999-12-31" td/fact-1 3
    "notes" "stuff"}
   {"date" "2000-01-05" td/fact-1 5
    "notes" "stuff"}
   {"date" "2000-05-06" td/fact-1 8
    "notes" "stuff"}
   {"date" "2002-01-13" td/fact-1 2
    "notes" "stuff"}
   {"date" "2002-02-13" td/fact-1 3
    "notes" "stuff"}
   {"date" "2002-02-14" td/fact-1 4
    "notes" "stuff"}
   {"date" "2002-02-15" td/fact-1 6
    "notes" "stuff"}
   {"date" "2001-02-15" td/fact-1 6
    "notes" "stuff"}])

(def data-2
  [{"date" "1999-10-13" td/fact-1 0 "some-number" 0
    "notes" "stuff"}
   {"date" "1999-11-13" td/fact-1 1 "some-number" 2
    "notes" "stuff"}
   {"date" "1999-12-31"                "some-number" 2
    "notes" "stuff"}
   {"date" "2000-01-05" td/fact-1 3
    "notes" "stuff"}
   {"date" "2000-05-06" td/fact-1 1
    "notes" "stuff"}
   {"date" "2002-01-13"                "some-number" 2
    "notes" "stuff"}
   {"date" "2002-02-14" td/fact-1 4 "some-number" 3
    "notes" "stuff"}
   {"date" "2002-02-14" td/fact-1 5 "some-number" 3
    "notes" "stuff"}
   {"date" "2002-02-15" td/fact-1 6 "some-number" 4
    "notes" "stuff"}
   {"date" "1999-10-14" td/fact-1 3 "some-number" 4
    "notes" "stuff"}
   {"date" "2001-02-15" td/fact-1 6 "some-number" 7
    "notes" "stuff"}
   {"date" "2002-02-13" td/fact-1 3 "some-number" 8
    "notes" "stuff"}])

(def data-3
  [{"date" "1999-10-13" td/fact-1 0 "some-number" 0 "some-number-2" 0
    "notes" "stuff"}
   {"date" "1999-11-13" td/fact-1 1 "some-number" 2 "some-number-2" 3
    "notes" "stuff"}
   {"date" "1999-12-31"                "some-number" 2 "some-number-2" 2
    "notes" "stuff"}
   {"date" "2000-01-05" td/fact-1 3                 "some-number-2" 7
    "notes" "stuff"}
   {"date" "2000-05-06" td/fact-1 1
    "notes" "stuff"}
   {"date" "2002-01-13"                "some-number" 2 "some-number-2" 9
    "notes" "stuff"}
   {"date" "2002-02-14" td/fact-1 4 "some-number" 3
    "notes" "stuff"}
   {"date" "2002-02-14" td/fact-1 5 "some-number" 3 "some-number-2" 3
    "notes" "stuff"}
   {"date" "2002-02-15" td/fact-1 6 "some-number" 4
    "notes" "stuff"}
   {"date" "1999-10-14" td/fact-1 3 "some-number" 4 "some-number-2" 0
    "notes" "stuff"}
   {"date" "2001-02-15" td/fact-1 6 "some-number" 7 "some-number-2" 1
    "notes" "stuff"}
   {"date" "2002-02-13" td/fact-1 3 "some-number" 8 "some-number-2" 0
    "notes" "stuff"}])