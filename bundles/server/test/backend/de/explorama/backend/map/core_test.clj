(ns de.explorama.backend.map.core-test
  (:require [clojure.test :refer :all]
            [de.explorama.backend.map.locations :as loc]))

(deftest replace-me
  (is (= 1 1)))

(def location-benin
  {:global-id "context-country-Benin"
   :iso-3361-2 "BJ"
   :iso-3361-3 "BEN"
   :lat 9.30769
   :lng 2.3158339999999953
   :loc-id "loc-9.30769-2.3158339999999953"
   :name "Benin"})

(def location-asia
  {:global-id "context-country-Asia"
   :iso-3361-2 nil
   :iso-3361-3 nil
   :lat 51.725
   :lng 94.443611
   :loc-id "loc-51.725-94.443611"
   :name "Asia"})

(deftest locations
  (testing "lookup-vountry-name"
    (is (= location-benin
           (loc/lookup "Benin")))
    (is (= nil
           (loc/lookup nil)))
    #_(is (= "" ;got {:iso-3361-2 "DE", :name "Germany", :lat 51.165691, :lng 10.451526000000058, :iso-3361-3 "DEU", :global-id "context-country-Germany", :loc-id "loc-51.165691-10.451526000000058"}
             (loc/lookup "")))
    (is (= location-asia
           (loc/lookup "Asia")))))