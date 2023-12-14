(ns de.explorama.backend.rights-roles.ldap.ldap-util-test
  (:require [de.explorama.backend.rights-roles.ldap.ldap-util :as ldap-util]
            [clojure.test :refer [deftest testing is]]))

(def user-config {:displayname [:sn :givenName]
                  :loginname :uid
                  :mail :mail})
(def group-config {:id-key :gidNumber
                   :displayname :cn})
(def user-group-config {:found :user
                        :key :gidNumber
                        :ref-key :gidNumber})
(def user-group-config-b {:found :group
                          :key :members
                          :ref-key :uid})

(def user-a {:cn "PAdmin"
             :dn "uid=PAdmin,ou=Users,dc=explorama,dc=de"
             :gidNumber "1"
             :givenName "Sandra"
             :sn "Admin"
             :uid "PAdmin"
             :uidNumber "2"})
(def user-b {:cn "PAdmin"
             :dn "uid=PAdmin,ou=Users,dc=explorama,dc=de"
             :gidNumber ["1" "2"]
             :givenName "Sandra"
             :sn "Admin"
             :uid "PAdmin"
             :uidNumber "1"})
(def user-c {:cn "CAhrab"
             :dn "uid=PAdmin,ou=Users,dc=explorama,dc=de"
             :gidNumber ["2"]
             :givenName "Ahrhab"
             :sn "Captain"
             :uid "CAhrab"
             :uidNumber "3"})

(def group-1 {:gidNumber "1"
              :cn "Admin"
              :members ["PAdmin"]})
(def group-2 {:gidNumber "2"
              :cn "User"
              :members ["PAdmin" "CAhrab"]})

(def groups {"1" group-1
             "2" group-2})

(deftest user-role
  (testing "found-in-user"
    (let [group-displayname (get group-config :displayname)]
      (is (= (get group-1 group-displayname)
             (#'ldap-util/user-role user-a groups user-group-config group-config)))
      (is (nil? (#'ldap-util/user-role user-a {"2" group-2} user-group-config group-config)))
      (is (= [(get group-1 group-displayname)
              (get group-2 group-displayname)]
             (#'ldap-util/user-role user-b groups user-group-config group-config)))))
  (testing "found-in-group"
    (let [group-displayname (get group-config :displayname)]
      (is (= [(get group-2 group-displayname)]
             (#'ldap-util/user-role user-c groups user-group-config-b group-config))))))
