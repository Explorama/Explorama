(ns de.explorama.abac.core-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [de.explorama.backend.abac.pap :as pap]
            [de.explorama.backend.abac.pdp :as pdp]
            [de.explorama.backend.abac.policy-repository :as pr]
            [de.explorama.backend.abac.repository-adapter.redis-policy-repository :as redis]))

(defn dummy-fixture [f]
  (pr/set-backend)
  (f))

(defn redis-fixture [f]
  (let [server-conn {:host "192.168.206.131"
                     :port 6379
                     :password "TODO"}]
    (pr/set-backend)
    (f)
    (redis/delete-key-path {:pool {}
                            :spec server-conn}
                           "abac/test")))

(use-fixtures :each dummy-fixture)

(deftest a-test
  (testing (is (= {:user-attributes {}, :data-attributes {}, :id :de.explorama.search.events/load-search, :name "Search"}
                  (pap/register-feature :de.explorama.search.events/load-search "Search" nil))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"
                                                    :user {:name "Admin"
                                                           :role "admin"
                                                           :username "admin"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"
                                                    :user {:name "Admin"
                                                           :username "admin"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"})))))
  (testing (is (= false (:valid? (pdp/call-request {}))))))

(deftest with-user-attributes
  (pr/init-repository {:de.explorama.search.events/load-search {:user-attributes {:role ["dummy" "admin" "data-scientist" "domain-expert"]}
                                                                :data-attributes {}, :id :de.explorama.search.events/load-search, :name "Load-Search"}
                       :de.explorama.search.events/exec-search {:user-attributes {:role ["admin"]}
                                                                :data-attributes {}, :id :de.explorama.search.events/exec-search, :name "Exec-Search"}
                       :de.explorama.search.events/delete-search {:user-attributes {:role ["data-scientist" "domain-expert"]}
                                                                  :data-attributes {}, :id :de.explorama.search.events/delete-search, :name "Delete-Search"}}
                      true)
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"
                                                   :user {:name "Admin"
                                                          :role "admin"
                                                          :username "admin"}})))))
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"
                                                   :user {:name "Admin"
                                                          :role "domain-expert"
                                                          :username "admin"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/exec-search"
                                                    :user {:name "Admin"
                                                           :role "data-scientist"
                                                           :username "admin"}})))))
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/exec-search"
                                                   :user {:name "Admin"
                                                          :role "admin"
                                                          :username "admin"}})))))
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/delete-search"
                                                   :user {:name "Admin"
                                                          :role "data-scientist"
                                                          :username "admin"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/delete-search"
                                                    :user {:name "Admin"
                                                           :role "admin"
                                                           :username "admin"}}))))))

(deftest with-data-attributes
  (pr/init-repository {:config.data/read-config-a {:user-attributes {}
                                                   :data-attributes {:id ["a"]}}}
                      true)
  (testing (is (= true
                  (:valid?
                   (pdp/call-request {:feature "config.data/read-config-a"
                                      :user {}
                                      :data {:id "a"}})))))
  (testing (is (= false
                  (:valid?
                   (pdp/call-request {:feature "config.data/read-config-a"
                                      :user    {}
                                      :data    {:id "b"}}))))))

(deftest with-data-and-user-attributes
  (pr/init-repository {:config.data/read-config-a {:user-attributes {:role ["admin"]}
                                                   :data-attributes {:id ["a"]}}}
                      true)
  (testing (is (= true
                  (:valid?
                   (pdp/call-request {:feature "config.data/read-config-a"
                                      :user    {:name     "Admin"
                                                :role     "admin"
                                                :username "admin"}
                                      :data    {:id "a"}})))))
  (testing (is (= false
                  (:valid?
                   (pdp/call-request {:feature "config.data/read-config-a"
                                      :user    {:name     "Dummy"
                                                :role     "dummy"
                                                :username "dDummy"}
                                      :data    {:id "a"}}))))))

(deftest with-optional-user-attributes
  (pr/init-repository {:de.explorama.search.events/load-search   {:user-attributes {:attributes {:role ["dummy" "admin" "data-scientist" "domain-expert"]}
                                                                                    :optional   [:role]}
                                                                  :data-attributes {}
                                                                  :id              :de.explorama.search.events/load-search
                                                                  :name            "Load-Search"}
                       :de.explorama.search.events/exec-search   {:user-attributes {:attributes {:role ["admin"]}
                                                                                    :optional   [:role]}
                                                                  :data-attributes {}
                                                                  :id              :de.explorama.search.events/exec-search
                                                                  :name            "Exec-Search"}
                       :de.explorama.search.events/delete-search {:user-attributes {:attributes {:role ["data-scientist" "domain-expert"]}
                                                                                    :optional   [:role]}
                                                                  :data-attributes {}
                                                                  :id              :de.explorama.search.events/delete-search
                                                                  :name            "Delete-Search"}}
                      true)
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"
                                                   :user    {:name     "Admin"
                                                             :role     "admin"
                                                             :username "admin"}})))))
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"
                                                   :user    {:name     "Admin"
                                                             :role     "domain-expert"
                                                             :username "admin"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/exec-search"
                                                    :user    {:name     "Admin"
                                                              :role     "data-scientist"
                                                              :username "admin"}})))))
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/exec-search"
                                                   :user    {:name     "Admin"
                                                             :role     "admin"
                                                             :username "admin"}})))))
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/delete-search"
                                                   :user    {:name     "Admin"
                                                             :role     "data-scientist"
                                                             :username "admin"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/delete-search"
                                                    :user    {:name     "Admin"
                                                              :role     "admin"
                                                              :username "admin"}}))))))

(deftest with-optional-required-user-attributes
  (pr/init-repository {:de.explorama.search.events/load-search    {:user-attributes {:attributes {:role     ["dummy" "admin" "data-scientist" "domain-expert"]
                                                                                                  :username ["admin"]}
                                                                                     :required   [:role]
                                                                                     :optional   [:username]}
                                                                   :data-attributes {}
                                                                   :id              :de.explorama.search.events/load-search
                                                                   :name            "Load-Search"}
                       :de.explorama.search.events/execute-search {:user-attributes {:attributes {:role     ["dummy" "admin" "data-scientist" "domain-expert"]
                                                                                                  :username ["admin"]
                                                                                                  :ip-range ["0.0.0.0"]}
                                                                                     :required   [:role]
                                                                                     :optional   [:username :ip-range]}
                                                                   :data-attributes {}
                                                                   :id              :de.explorama.search.events/load-search
                                                                   :name            "Load-Search"}}
                      true)
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"
                                                   :user    {:name     "Admin"
                                                             :role     "admin"
                                                             :username "admin"}})))))
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"
                                                   :user    {:name     "Admin"
                                                             :role     "dummy"
                                                             :username "admin"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"
                                                    :user    {:name     "Admin"
                                                              :role     "dummy"
                                                              :username "dummy"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"
                                                    :user    {:name     "Admin"
                                                              :role     "captain"
                                                              :username "admin"}})))))
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/execute-search"
                                                   :user    {:name     "Admin"
                                                             :role     "dummy"
                                                             :username "dummy"
                                                             :ip-range "0.0.0.0"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/execute-search"
                                                    :user    {:name     "Admin"
                                                              :role     "captain"
                                                              :username "dummy"
                                                              :ip-range "0.0.0.0"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/execute-search"
                                                    :user    {:name     "Admin"
                                                              :role     "dummy"
                                                              :username "dummy"
                                                              :ip-range "1.0.0.0"}}))))))

(deftest with-wildcard-value
  (pr/init-repository {:de.explorama.search.events/load-search    {:user-attributes {:attributes {:role     ["dummy" "admin" "data-scientist" "domain-expert" "*"]
                                                                                                  :username ["admin"]}
                                                                                     :required   [:role]
                                                                                     :optional   [:username]}
                                                                   :data-attributes {}
                                                                   :id              :de.explorama.search.events/load-search
                                                                   :name            "Load-Search"}
                       :de.explorama.search.events/load-search2    {:user-attributes {:attributes {:role     ["dummy" "admin" "data-scientist" "domain-expert"]
                                                                                                   :username ["admin" "*"]}
                                                                                      :required   [:role]
                                                                                      :optional   [:username]}
                                                                    :data-attributes {}
                                                                    :id              :de.explorama.search.events/load-search2
                                                                    :name            "Load-Search"}
                       :de.explorama.search.events/execute-search {:user-attributes {:attributes {:role     ["dummy" "admin" "data-scientist" "domain-expert"]
                                                                                                  :username ["admin" "*"]}
                                                                                     :required   []
                                                                                     :optional   [:username :role]}
                                                                   :data-attributes {}
                                                                   :id              :de.explorama.search.events/execute-search
                                                                   :name            "Load-Search"}} true)
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search"
                                                   :user    {:name     "Captain Arrhab"
                                                             :role     "captain"
                                                             :username "admin"}})))))
  (testing (is (= true (:valid? (pdp/call-request {:feature "de.explorama.search.events/execute-search"
                                                   :user    {:name     "Captain Arrhab"
                                                             :role     "dummy"
                                                             :username "cahrrhab"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search2"
                                                    :user    {:name     "Admin"
                                                              :role     "captain"
                                                              :username "dummy"}})))))
  (testing (is (= false (:valid? (pdp/call-request {:feature "de.explorama.search.events/load-search2"
                                                    :user    {:name     "Admin"
                                                              :role     "captain"
                                                              :username "admin"}}))))))

(deftest update-role-attibute
  (pr/create-policy :config.data/read-config-a {:user-attributes {:role ["admin"]}
                                                :data-attributes {:id ["a"]}})
  (pr/update-policy :config.data/read-config-a "test" false true)
  (testing (is (= ["admin" "test"]
                  (get-in (pr/fetch-policy :config.data/read-config-a)
                          [:user-attributes :role]))))
  (pr/update-policy :config.data/read-config-a "test" false false)
  (testing (is (= ["admin"]
                  (get-in (pr/fetch-policy :config.data/read-config-a)
                          [:user-attributes :role])))))

(deftest update-optional-with-role
  (pr/create-policy :config.data/read-config-a {:user-attributes {:attributes {:username ["admin"]}
                                                                  :required [:username]}
                                                :data-attributes {:id ["a"]}})
  (pr/update-policy :config.data/read-config-a "test" false true)
  (testing (is (and (= ["test"]
                       (get-in (pr/fetch-policy :config.data/read-config-a)
                               [:user-attributes :attributes :role]))
                    (= [:role]
                       (get-in (pr/fetch-policy :config.data/read-config-a)
                               [:user-attributes :optional])))))
  (pr/update-policy :config.data/read-config-a "test" false false)
  (testing (is (and (= nil
                       (get-in (pr/fetch-policy :config.data/read-config-a)
                               [:user-attributes :attributes :role]))
                    (= nil
                       (get-in (pr/fetch-policy :config.data/read-config-a)
                               [:user-attributes :optional]))))))