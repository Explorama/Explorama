(ns data-format-lib.simplified-view-test
  (:require #?(:clj  [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer-macros [deftest is]])
            [data-format-lib.simplified-view :as sv]
            [de.explorama.shared.common.test-data :as td]))

(def ^:private di-filter
  [:and
   [:or
    #:data-format-lib.filter{:op    :=
                             :prop  td/category-1
                             :value (td/category-val 1 "A")}
    #:data-format-lib.filter{:op    :=
                             :prop  td/category-1
                             :value (td/category-val 1 "B")}
    #:data-format-lib.filter{:op    :=
                             :prop  td/category-1
                             :value (td/category-val 1 "C")}
    #:data-format-lib.filter{:op    :=
                             :prop  td/category-1
                             :value (td/category-val 1 "D")}
    #:data-format-lib.filter{:op   :=
                             :prop td/category-1
                             :value (td/category-val 1 "E")}
    #:data-format-lib.filter{:op    :=
                             :prop  td/category-1
                             :value (td/category-val 1 "F")}]
   [:or
    #:data-format-lib.filter{:op :<, :prop td/fact-1, :value 0}
    #:data-format-lib.filter{:op :>, :prop td/fact-1, :value 2}]])

(def ^:private simplified-di-filter
  [#:data-format-lib.filter{::sv/op   :in
                            :prop td/category-1
                            :value
                            [(td/category-val 1 "A")
                             (td/category-val 1 "B")
                             (td/category-val 1 "C")
                             (td/category-val 1 "D")
                             (td/category-val 1 "E")
                             (td/category-val 1 "F")]}
   #:data-format-lib.filter{::sv/op :not-in-range, :prop td/fact-1, :value [0 2]}])

(def ^:private local-filter
  [:and
   [:or
    #:data-format-lib.filter{:op :=, :prop td/datasource, :value td/datasource-a}]
   [:or #:data-format-lib.filter{:op :=, :prop td/country, :value td/country-a}]
   [:and
    #:data-format-lib.filter{:op    :>=
                             :prop  :data-format-lib.dates/year
                             :value 2015}
    #:data-format-lib.filter{:op    :<=
                             :prop  :data-format-lib.dates/year
                             :value 2016}]
   [:or
    #:data-format-lib.filter{:op    :=
                             :prop  td/org
                             :value (td/org-val 1)}]
   [:and
    #:data-format-lib.filter{:op :>=, :prop td/fact-1, :value 1}
    #:data-format-lib.filter{:op :<=, :prop td/fact-1, :value 8}]
   [:or
    #:data-format-lib.filter{:op    :=
                             :prop  "title"
                             :value "Naledi: A Baby Elephant's Tale"}]])

(def ^:private simplified-local-filter
  [#:data-format-lib.filter{::sv/op :=, :prop td/datasource, :value td/datasource-a}
   #:data-format-lib.filter{::sv/op :=, :prop td/country, :value td/country-a}
   #:data-format-lib.filter{::sv/op    :in-range
                            :prop  :data-format-lib.dates/year
                            :value [2015 2016]}
   #:data-format-lib.filter{::sv/op    :=
                            :prop  td/org
                            :value (td/org-val 1)}
   #:data-format-lib.filter{::sv/op :in-range, :prop td/fact-1, :value [1 8]}
   #:data-format-lib.filter{::sv/op    :=
                            :prop  "title"
                            :value "Naledi: A Baby Elephant's Tale"}])

(def ^:private operations
  #:di{:operations
       [:filter
        "dc1c0015a3160ffb0dbd726768682a0a763eb5ed1b9d0b6b01e2aaaf7d7bf162"
        "fac339cf46c65ecc11d0ba96153ecaeb0d73bc637bbbf87d53ba05cb91959083"]
       :filter
       {"dc1c0015a3160ffb0dbd726768682a0a763eb5ed1b9d0b6b01e2aaaf7d7bf162"
        di-filter}})

(def ^:private simplified-conjunction-filter
  (into simplified-di-filter simplified-local-filter))

(deftest simple-flatten-filter-test
  (is (= simplified-conjunction-filter
         (#'sv/flatten-filter [:and di-filter local-filter]))))

(deftest simple-simplified-filter-test
  (is (= {:incomplete? false, :di-filter-primitives simplified-di-filter, :local-filter-primitives simplified-local-filter}
         (sv/simplified-filter operations local-filter))))

(def ^:private additional-di-filter
  [:and
   #:data-format-lib.filter{:op :>=, :prop td/fact-1, :value 5}
   #:data-format-lib.filter{:op :<=, :prop td/fact-1, :value 321}])

(def ^:private simplified-additional-di-filter
  #:data-format-lib.filter{::sv/op :in-range, :prop td/fact-1, :value [5 321]})

(def ^:private nested-filter-desc
  [:filter
   "a33d5a94f8d702089258a230e2656571db4611d7e7847b2220491fdc81a9091f"
   [:filter
    "dc1c0015a3160ffb0dbd726768682a0a763eb5ed1b9d0b6b01e2aaaf7d7bf162"
    "fac339cf46c65ecc11d0ba96153ecaeb0d73bc637bbbf87d53ba05cb91959083"]])

(def ^:private compound-operations
  #:di{:operations
       [:difference
        nil
        nested-filter-desc
        [:filter
         "dc1c0015a3160ffb0dbd726768682a0a763eb5ed1b9d0b6b01e2aaaf7d7bf162"
         "fac339cf46c65ecc11d0ba96153ecaeb0d73bc637bbbf87d53ba05cb91959083"]]
       :filter
       {"dc1c0015a3160ffb0dbd726768682a0a763eb5ed1b9d0b6b01e2aaaf7d7bf162"
        di-filter
        "a33d5a94f8d702089258a230e2656571db4611d7e7847b2220491fdc81a9091f"
        additional-di-filter}})

(deftest compound-simplified-filter-test
  (is (= {:incomplete? true, :di-filter-primitives nil, :local-filter-primitives simplified-local-filter}
         (sv/simplified-filter compound-operations local-filter))))

(def ^:private nested-filter-operations
  #:di{:operations
       nested-filter-desc
       :filter
       {"dc1c0015a3160ffb0dbd726768682a0a763eb5ed1b9d0b6b01e2aaaf7d7bf162"
        di-filter
        "a33d5a94f8d702089258a230e2656571db4611d7e7847b2220491fdc81a9091f"
        additional-di-filter}})

(deftest nested-simplified-filter-test
  (is (= {:incomplete? false
          :di-filter-primitives (-> [simplified-additional-di-filter]
                                    (into simplified-di-filter))
          :local-filter-primitives simplified-local-filter}
         (sv/simplified-filter nested-filter-operations local-filter))))
