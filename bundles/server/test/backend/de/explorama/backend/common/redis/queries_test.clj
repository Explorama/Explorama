(ns de.explorama.backend.redis.queries-test
  (:require [clojure.test :refer [deftest testing is]]
            [de.explorama.backend.redis.queries :refer [query-builder NUMBER STRING]])
  (:import de.explorama.backend.redis.CollectionParser))

(deftest query-builder-test
  (testing "Simple replacement"
    (is (= (query-builder ["MATCH (n)"
                           "WHERE n.id = %%0"
                           "RETURN n"]
                          [5])
           "MATCH (n) WHERE n.id = 5 RETURN n"))
    (is (= (query-builder ["MATCH (n)"
                           "WHERE n.id = %%0"
                           "RETURN n"]
                          ["5"])
           "MATCH (n) WHERE n.id = \"5\" RETURN n"))
    (is (= (query-builder ["MATCH (n)"
                           "WHERE n.id = %%0"
                           "RETURN n"]
                          ["5\"5"])
           "MATCH (n) WHERE n.id = \"5\\\"5\" RETURN n")))
  (testing "Simple replacement with IN"
    (is (= (query-builder ["MATCH (n)"
                           "WHERE n.id IN %%0"
                           "RETURN n"]
                          [[1 2 3 4]])
           "MATCH (n) WHERE n.id IN [1, 2, 3, 4] RETURN n"))
    (is (= (query-builder ["MATCH (n)"
                           "WHERE n.id IN %%0"
                           "RETURN n"]
                          [["1" "2" "3" "4\"5"]])
           "MATCH (n) WHERE n.id IN [\"1\", \"2\", \"3\", \"4\\\"5\"] RETURN n")))
  (testing "Simple replacement with IN and types"
    (is (= (query-builder ["MATCH (n)"
                           "WHERE n.id IN %%0"
                           "AND"
                           "n.gid IN %%1"
                           "RETURN n"]
                          [(with-meta ["1" "2" "3" "4" "5"] {:type NUMBER})
                           (with-meta [1 2 3 4 5] {:type STRING})])
           "MATCH (n) WHERE n.id IN [1, 2, 3, 4, 5] AND n.gid IN [\"1\", \"2\", \"3\", \"4\", \"5\"] RETURN n"))))

(deftest parse-collection-test
  (testing "Simple collection"
    (is (= (CollectionParser/parse "[{__qr__name: Protesters (Bolivia), __qr__type: organisation}, {__qr__name: Caiza \"D\", __qr__type: district}, {__qr__name: Bolivia, __qr__type: country}, {__qr__name: Jose Maria Linares, __qr__type: city}, {__qr__name: Correo del Sur, __qr__type: publisher}, {__qr__name: Peaceful protest, __qr__type: tag}, {__qr__name: South America, __qr__type: region}, {__qr__name: MAS-IPSP: Movement for Socialism-Political Instrument for the Sovereignty of the Peoples, __qr__type: organisation}, {__qr__name: Potosi, __qr__type: province}, {__qr__name: NULL, __qr__type: location}]")
           [{"__qr__name" "Protesters (Bolivia)"
             "__qr__type" "organisation"}
            {"__qr__name" "Caiza \"D\""
             "__qr__type" "district"}
            {"__qr__name" "Bolivia"
             "__qr__type" "country"}
            {"__qr__name" "Jose Maria Linares"
             "__qr__type" "city"}
            {"__qr__name" "Correo del Sur"
             "__qr__type" "publisher"}
            {"__qr__name" "Peaceful protest"
             "__qr__type" "tag"}
            {"__qr__name" "South America"
             "__qr__type" "region"}
            {"__qr__name" "MAS-IPSP: Movement for Socialism-Political Instrument for the Sovereignty of the Peoples"
             "__qr__type" "organisation"}
            {"__qr__name" "Potosi"
             "__qr__type" "province"}
            {"__qr__name" "NULL"
             "__qr__type" "location"}])))
  (testing "Simple collection - Virgin Islands, U.S."
    (is (= (CollectionParser/parse "[{__qr__name: Protesters (Bolivia), __qr__type: organisation}, {__qr__name: Caiza \"D\", __qr__type: district}, {__qr__name: Virgin Islands, U.S., __qr__type: country}, {__qr__name: Jose Maria Linares, __qr__type: city}, {__qr__name: Correo del Sur, __qr__type: publisher}, {__qr__name: Peaceful protest, __qr__type: tag}, {__qr__name: South America, __qr__type: region}, {__qr__name: MAS-IPSP: Movement for Socialism-Political Instrument for the Sovereignty of the Peoples, __qr__type: organisation}, {__qr__name: Potosi, __qr__type: province}, {__qr__name: NULL, __qr__type: location}]")
           [{"__qr__name" "Protesters (Bolivia)"
             "__qr__type" "organisation"}
            {"__qr__name" "Caiza \"D\""
             "__qr__type" "district"}
            {"__qr__name" "Virgin Islands, U.S."
             "__qr__type" "country"}
            {"__qr__name" "Jose Maria Linares"
             "__qr__type" "city"}
            {"__qr__name" "Correo del Sur"
             "__qr__type" "publisher"}
            {"__qr__name" "Peaceful protest"
             "__qr__type" "tag"}
            {"__qr__name" "South America"
             "__qr__type" "region"}
            {"__qr__name" "MAS-IPSP: Movement for Socialism-Political Instrument for the Sovereignty of the Peoples"
             "__qr__type" "organisation"}
            {"__qr__name" "Potosi"
             "__qr__type" "province"}
            {"__qr__name" "NULL"
             "__qr__type" "location"}])))
  (testing "Simple collection - comma separated"
    (is (=  (CollectionParser/parse "[{__qr__n: Japan, __qr__t: country}, {__qr__n: Increased risk, __qr__t: incident Level Description}, {__qr__n: Reg. Kanto, Tokai, Tohoku,, __qr__t: region}, {__qr__n: closed, __qr__t: Event status}]")
            [{"__qr__t" "country"
              "__qr__n" "Japan"}
             {"__qr__t" "incident Level Description"
              "__qr__n" "Increased risk"}
             {"__qr__t" "region"
              "__qr__n" "Reg. Kanto, Tokai, Tohoku,"}
             {"__qr__t" "Event status"
              "__qr__n" "closed"}]))))
