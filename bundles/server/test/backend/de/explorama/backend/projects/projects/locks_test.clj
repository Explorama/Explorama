(ns de.explorama.backend.projects.projects.locks-test
  "Tests the external API of de.explorama.backend.projects.projects.locks.
  For tests of the private functions see de.explorama.backend.projects.projects.locks-test-internal."
  (:require [clojure.test :refer :all])
  (:require [de.explorama.backend.projects.projects.locks :refer :all]
            [expectations.clojure.test :as expectations :refer [expect in side-effects]]
            [taoensso.timbre :as log]))

(defn empty-lock-state [work]
  ;; perform test setup
  (with-redefs [de.explorama.backend.projects.projects.locks/state (atom {})]
    (work)))

(use-fixtures :each empty-lock-state)

(deftest simple-positive-test
  ;; disable side-effects
  (with-redefs [de.explorama.backend.projects.session/broadcast (fn [e])]
    ;; Start with expecting locks to be empty
    (expect empty?
            (locks))

    ;; If a new project is locked, expect lock to return nil.
    (expect nil
            (lock 'project-1 'client-1 'user-1))

    ;; ... then, expect to see the project being locked
    (expect {'project-1 {:lock-user 'user-1
                         :client-id 'client-1}}
            (in (locks)))

    ;; ... and finally, if you unlock the project-id, ...
    (expect {:lock-user 'user-1
             :client-id 'client-1}
            (unlock 'project-1 'client-1))

    ;;  expect to not see the project in the locks anymore
    (expect empty?
            (locks))))

(deftest expect-lock-to-be-closed
  (lock 'project-1 'client-1 'user-1)

  (testing "Locking multiple times for the same client-id/user-id is ok."
    (lock 'project-1 'client-1 'user-1))

  (expect (more-> clojure.lang.ExceptionInfo type
                  :de.explorama.backend.storage.transient/busy-already (-> ex-data :reason))
          (lock 'project-1 'client-1 'user-2))

  (expect (more-> clojure.lang.ExceptionInfo type
                  :de.explorama.backend.storage.transient/busy-already (-> ex-data :reason))
          (lock 'project-1 'client-2 'user-1))

  (expect (more-> clojure.lang.ExceptionInfo type
                  :de.explorama.backend.storage.transient/busy-already (-> ex-data :reason))
          (lock 'project-1 'client-2 'user-2)))

#_(deftest expect-unlock-to-be-tied-to-client-id
    (lock 'project-1 'client-1 'user-1)
    (is (thrown? Exception (unlock 'project-1 'another-client-2))))

(deftest broadcasting
  ;; Start with expecting locks to be empty
  (expect empty?
          (locks))

  ;; If a new project is locked, expect broadcasting that lock.
  (expect [[[:de.explorama.backend.projects.core/locks {'project-1 {:lock-user 'user-1 :client-id 'client-1}}]]]
          (side-effects [de.explorama.backend.projects.session/broadcast]
                        (lock 'project-1 'client-1 'user-1)))

  ;; ... and, if you try to add another lock, expect to see no broadcasting
  ;;     even if you catch the exception.
  (expect []
          (side-effects [de.explorama.backend.projects.session/broadcast]
                        (try
                          (lock 'project-1 'client-1 'user-2)
                          (catch Exception _))))

  ;; ... and finally, if you unlock the project-id, expect to broadcast no locks
  (expect [[[:de.explorama.backend.projects.core/locks {}]]]
          (side-effects [de.explorama.backend.projects.session/broadcast]
                        (unlock 'project-1 'client-1))))

(deftest grace-period

  ;; - once a connection is lost, the lock is set for a
  ;;   certain ttl: After a grace period, the lock is
  ;;   opened automatically.
  ;; - if the same client-id re-connects (with the project still open),
  ;;   the lock is re-issued and kept.

  (let [grace-period 1000]
    (with-redefs [de.explorama.backend.projects.session/broadcast (fn [e])]

      (with-redefs [de.explorama.backend.storage.transient/now (constantly 42)]
        (lock 'project-1 'client-1 'user-1)
        (expect {'project-1 {:lock-user 'user-1
                             :client-id 'client-1}}
                (in (locks))))

      ;; later in time, we unlock that thing and query the locks in the grace period.
      (with-redefs [de.explorama.backend.storage.transient/now (constantly 42000)]
        (unlock 'project-1 'client-1 :grace-period-ms grace-period)
        (expect {'project-1 {:lock-user 'user-1
                             :client-id 'client-1}}
                (in (locks))))

      ;; and finally, after grace period, the lock has disappered.
      (with-redefs [de.explorama.backend.storage.transient/now (constantly (+ 42000 (* 2 grace-period)))]
        (expect empty?
                (locks))))))