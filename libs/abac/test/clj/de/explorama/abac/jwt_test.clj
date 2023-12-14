(ns de.explorama.abac.jwt-test
  (:require [de.explorama.backend.abac.jwt :as jwt]
            [clj-time.core :as t]
            [clojure.test :refer [deftest testing is use-fixtures]])
  (:import [org.joda.time DateTime]))

(def date1 (DateTime. 1645611027408)) ; 2022-02-23T11:13
(def date2 (DateTime. 1645871400000)) ; 2022-02-26T11:13
(def date3 (DateTime. 1645698600000)) ; 2022-02-24T11:13

(def PAdmin-user {:username "PAdmin" :role "admin"})
(def cahrrhab-user {:username "CAhrrhab" :role "captain"})

(defn date1-token [user-infos]
  (with-redefs [t/now (fn [] date1)]
    (jwt/user-token user-infos)))

(defn date2-token [user-infos]
  (with-redefs [t/now (fn [] date2)]
    (jwt/user-token user-infos)))

(defn date3-token [user-infos]
  (with-redefs [t/now (fn [] date3)]
    (jwt/user-token user-infos)))

(deftest token-valid
  (testing "token-valid?"
    (let [token1 (date1-token PAdmin-user)
          token2 (date2-token PAdmin-user)
          token3 (date3-token PAdmin-user)]
      (with-redefs [t/now (fn [] date2)]
        (is (not (:valid? (jwt/token-valid? token1)))) ; expired (after date2)
        (is (:valid? (jwt/token-valid? token2))) ; valid (equal date2)
        (is (:valid? (jwt/token-valid? token3))))))) ;valid (before date2)

(deftest user-valid
  (testing "valid-users"
    (let [token1 (date2-token PAdmin-user)
          token2 (date2-token cahrrhab-user)]
      (with-redefs [t/now (fn [] date2)]
        (is (jwt/user-valid? PAdmin-user token1))
        (is (jwt/user-valid? cahrrhab-user token2))
        (is (not (jwt/user-valid? PAdmin-user token2)))
        (is (not (jwt/user-valid? cahrrhab-user token1)))))))

(deftest new-user-token
  (testing "generating new token"
    (let [token1 (date1-token PAdmin-user)
          token2 (date2-token PAdmin-user)]
      (with-redefs [t/now (fn [] date2)]
        (is (nil? (jwt/new-user-token PAdmin-user token1)))
        (is (not= token2
                  (jwt/new-user-token PAdmin-user token2)))))))