(ns de.explorama.backend.projects.projects-test
  (:require [de.explorama.backend.abac.policy-repository :as pr]
            [de.explorama.backend.abac.repository-adapter.dummy-policy-repository :as dummy]
            [clojure.core.async :refer [chan]]
            [clojure.test :refer [compose-fixtures deftest is testing
                                  use-fixtures]]
            [de.explorama.backend.mocks.redis :as redis-mock]
            [de.explorama.backend.storage.agent.core :as pcore]
            [de.explorama.backend.projects.core :as core]
            [de.explorama.backend.projects.persistence.core :as repo]
            [de.explorama.backend.projects.projects.core :as proj]
            [de.explorama.backend.projects.projects.path :as ppath]
            [de.explorama.backend.projects.projects.store :as store]
            [taoensso.timbre :refer [error]]))

(def user-info {:username "test-user1"
                :role     "test-role"})

(def user-info2 {:username "test-user2"
                 :role     "test-role"})

(def user-info3 {:username "test-user3"
                 :role     "test-role2"})

(defn frame-id [project-id frame-id]
  {:workspace-id project-id
   :frame-id frame-id})

(defn event [workspace-id id c o]
  {:frame-id (frame-id workspace-id id)
   :event-name (str "test" c)
   :origin "test"
   :description {:description "test desc"}
   :user-info user-info})

(defn filter-events [events workspace-id]
  (filterv (fn [{{id :workspace-id} :frame-id}]
             (= id workspace-id))
           events))

(defn events-f1 [workspace-id] (map #(event workspace-id 1 % "Mosaic") (range 16)))
(defn events-f2 [workspace-id] (map #(event workspace-id 2 % "Suche") (range 11)))
(defn events-f3 [workspace-id] (map #(event workspace-id 3 % "Map") (range 17)))
(defn events-id [workspace-id id] (map #(event workspace-id id % "Mosaic") (range 16)))

(defn test-proj
  ([project-id proj-name]
   {:project-id project-id
    :title proj-name
    :description {:test "desc"}})
  ([project-id proj-name {allowed-user :allowed-user read-only-user :read-only-user}]
   {:project-id project-id
    :title proj-name
    :description {:test "desc"}}))

(defn identity-mapping [_ value]
  value)

(defn equal-test
  ([seq1 seq2 value-mapping]
   (loop [lines seq1
          events seq2
          result true]
     (cond
       (and (empty? lines) (not-empty events)) false
       (and (not-empty lines) (empty? events)) false
       (and (empty? lines) (empty? events)) result
       :else (let [[c [lorigin lframe-id levent-name ldesc]] (first lines)
                   {:keys [frame-id event-name origin description]} (first events)]
               (recur
                (rest lines)
                (rest events)
                (and (= lorigin (value-mapping :origin origin))
                     (= lframe-id (value-mapping :frame-id frame-id))
                     (= levent-name (value-mapping :event-name event-name))
                     (= ldesc (value-mapping :description description))
                     result))))))
  ([seq1 seq2 value-mapping1 value-mapping2]
   (loop [lines seq1
          events seq2
          result true]
     (cond
       (and (empty? lines) (not-empty events)) false
       (and (not-empty lines) (empty? events)) false
       (and (empty? lines) (empty? events)) result
       :else (let [[_ [lorigin lframe-id levent-name ldesc]] (first lines)
                   {:keys [frame-id event-name origin description]} (first events)]
               (recur
                (rest lines)
                (rest events)
                (or
                 (and (= lorigin (value-mapping2 :origin origin))
                      (= lframe-id (value-mapping2 :frame-id frame-id))
                      (= levent-name (value-mapping2 :event-name event-name))
                      (= ldesc (value-mapping2 :description description))
                      result)
                 (and (= lorigin (value-mapping1 :origin origin))
                      (= lframe-id (value-mapping1 :frame-id frame-id))
                      (= levent-name (value-mapping1 :event-name event-name))
                      (= ldesc (value-mapping1 :description description))
                      result))))))))

(defn write-eventlog! [events nthreads time-to-write]
  (core/main-thread nthreads)
  (doseq [event events]
    (core/eventlog event))
  (Thread/sleep time-to-write)
  (swap! core/main-thread-state assoc :shutdown-channel (chan)))

(defn randomize-order [seqs]
  (loop [seqs seqs
         seq-e (vec (range (count seqs)))
         result []]
    (if (empty? seq-e)
      result
      (let [rand-num (rand-int (count seq-e))
            list-num (nth seq-e rand-num)
            curr (first (nth seqs list-num))
            curr-list (rest (nth seqs list-num))]
        (recur
         (assoc seqs list-num curr-list)
         (if (empty? curr-list)
           (vec (concat (subvec seq-e 0 rand-num) (subvec seq-e (inc rand-num))))
           seq-e)
         (conj result curr))))))

(defn test-setup [test-fn]
  (with-redefs
   [de.explorama.backend.abac.policy-repository/backend (atom (dummy/dummy-adapter nil))
    ppath/project-prefix "test-plogs/projects/"
    ppath/workspaces-prefix "test-plogs/workspaces/"
    store/projects-store (pcore/create {:impl :test
                                        :path @#'store/path
                                        :init {}})]
    (store/start!)
    (reset! core/counter 0)
    (test-fn)))

(use-fixtures :each (compose-fixtures redis-mock/fixture test-setup))

(deftest test-eventlog-simple
  (try (write-eventlog! (events-f1 1) 5 5000)
       (let [path (ppath/lookup-plogs-path (frame-id 1 1) user-info)
             instance (repo/new-instance path)
             lines (repo/read-lines instance)
             result (equal-test lines (events-f1 1) identity-mapping)]
         (try (testing "the eventlog can be read as written"
                (is (true? result)))
              (catch Exception e (error e))))
       (catch Exception e (error e))))

(deftest test-eventlog-shuffle
  (try (let [events (randomize-order [(events-f1 1) (events-f2 1) (events-f3 1)])
             _ (write-eventlog! events 5 10000)
             path1 (ppath/lookup-plogs-path (frame-id 1 1) user-info)
             path2 (ppath/lookup-plogs-path (frame-id 1 2) user-info)
             path3 (ppath/lookup-plogs-path (frame-id 1 3) user-info)
             instance (repo/new-instance path1)]
         (try (let [lines (repo/read-lines instance)
                    result (equal-test lines events identity-mapping)]
                (testing "a shuffled eventlog comes out in the right order"
                  (is (= path1 path2 path3)))
                (testing "a shuffled eventlog comes out in the right order"
                  (is (true? result))))
              (catch Exception e (error e))))
       (catch Exception e (error e))))

(deftest test-get-head
  (write-eventlog! (events-f1 1) 5 10000)
  (try (let [path (ppath/lookup-plogs-path (frame-id 1 1) user-info)
             head (proj/get-head path)]
         (testing "head is counted up"
           (is (= (mod (count (events-f1 1))
                       1000000)
                  (:c head)))))
       (catch Exception e (error e))))

(deftest test-create-project
  (try (let [events (randomize-order [(events-f1 1) (events-f2 2) (events-f3 1)])
             _ (write-eventlog! events 5 10000)
             _ (proj/create! (test-proj 1 "test-project") user-info)
             proj-path1 (ppath/project-path user-info 1)
             proj-instance1 (repo/new-instance proj-path1)
             proj-log1 (repo/read-lines-force proj-instance1)
             result1 (equal-test proj-log1
                                 (filter-events events 1)
                                 identity-mapping)
             _ (proj/create! (test-proj 2 "test-project") user-info)
             proj-path2 (ppath/project-path user-info 2)
             proj-instance2 (repo/new-instance proj-path2)
             proj-log2 (repo/read-lines-force proj-instance2)
             result2 (equal-test proj-log2
                                 (filter-events events 2)
                                 identity-mapping)]
         (try (testing "project event log can be read and contains all events for workspace 1"
                (is (true? result1)))
              (testing "project event log can be read and contains all events for workspace 2"
                (is (true? result2)))
              (catch Exception e (error e))))
       (catch Exception e (error e))))

(deftest test-load-project
  (try (let [events (randomize-order [(events-f1 1) (events-f2 2) (events-f3 1)])
             _ (write-eventlog! events 5 10000)
             cur-project (test-proj 1 "test-project")
             {:keys [project-id]} (proj/create! cur-project user-info)
             proj-path (ppath/lookup-plogs-path {:project-id project-id} user-info)
             proj-instance (repo/new-instance proj-path)
             {:keys [logs]} (proj/load-project {:project-id project-id} user-info)
             result (equal-test logs
                                (filter-events events 1)
                                identity-mapping)]
         (try (testing "loading project event logs"
                (is (= true result)))
              (catch Exception e (error e))))
       (catch Exception e (error e))))

(deftest test-list-projects
  (try (let [proj1 (assoc (test-proj 1
                                     "test-project1")
                          :allowed-user [(:username user-info)]
                          :read-only-user [])
             proj2 (assoc (test-proj 2
                                     "test-project2")
                          :allowed-user [(:username user-info2)]
                          :read-only-user [])
             proj3 (assoc (test-proj 3
                                     "test-project3")
                          :allowed-user [(:username user-info3)]
                          :read-only-user [])
             proj4 (assoc (test-proj 4
                                     "test-project4")
                          :allowed-user [(:username user-info)]
                          :read-only-user [])
             _ (proj/create! proj1 user-info)
             _ (proj/create! proj2 user-info2)
             _ (proj/create! proj3 user-info3)
             _ (proj/create! proj4 user-info)
             projects1 (proj/list-projects user-info)
             projects2 (proj/list-projects user-info2)
             projects3 (proj/list-projects user-info3)
             no-public-projects (empty? (proj/all-public-read-only-projects))
             _ (proj/share-project {:project-id 1
                                    :shared-by (:username user-info)
                                    :public-read-only? true})
             public-projects (contains? (proj/all-public-read-only-projects) 1)]
         (testing "getting out the projects from the saved structure for user 1"
           (is (= {:created-projects [proj1 proj4]
                   :allowed-projects []
                   :read-only-projects []}
                  {:created-projects (mapv (fn [[_ v]]
                                             (select-keys v
                                                          [:project-id
                                                           :title
                                                           :description
                                                           :allowed-user
                                                           :read-only-user]))
                                           (:created-projects projects1))
                   :allowed-projects (mapv (fn [[_ v]]
                                             (select-keys v
                                                          [:project-id
                                                           :title
                                                           :description
                                                           :allowed-user
                                                           :read-only-user]))
                                           (:allowed-projects projects1))
                   :read-only-projects (mapv (fn [[_ v]]
                                               (select-keys v
                                                            [:project-id
                                                             :title
                                                             :description
                                                             :allowed-user
                                                             :read-only-user]))
                                             (:read-only-projects projects1))})))
         (testing "getting out the projects from the saved structure for user 2"
           (is (= {:created-projects [proj2]
                   :allowed-projects []
                   :read-only-projects []}
                  {:created-projects (mapv (fn [[_ v]]
                                             (select-keys v
                                                          [:project-id
                                                           :title
                                                           :description
                                                           :allowed-user
                                                           :read-only-user]))
                                           (:created-projects projects2))
                   :allowed-projects (mapv (fn [[_ v]]
                                             (select-keys v
                                                          [:project-id
                                                           :title
                                                           :description
                                                           :allowed-user
                                                           :read-only-user]))
                                           (:allowed-projects projects2))
                   :read-only-projects (mapv (fn [[_ v]]
                                               (select-keys v
                                                            [:project-id
                                                             :title
                                                             :description
                                                             :allowed-user
                                                             :read-only-user]))
                                             (:read-only-projects projects2))})))
         (testing "getting out the projects from the saved structure for user 3"
           (is (= {:created-projects [proj3]
                   :allowed-projects []
                   :read-only-projects []}
                  {:created-projects (mapv (fn [[_ v]]
                                             (select-keys v
                                                          [:project-id
                                                           :title
                                                           :description
                                                           :allowed-user
                                                           :read-only-user]))
                                           (:created-projects projects3))
                   :allowed-projects (mapv (fn [[_ v]]
                                             (select-keys v
                                                          [:project-id
                                                           :title
                                                           :description
                                                           :allowed-user
                                                           :read-only-user]))
                                           (:allowed-projects projects3))
                   :read-only-projects (mapv (fn [[_ v]]
                                               (select-keys v
                                                            [:project-id
                                                             :title
                                                             :description
                                                             :allowed-user
                                                             :read-only-user]))
                                             (:read-only-projects projects3))})))
         (testing "listing public projects"
           (is (true? no-public-projects))
           (is (true? public-projects))))
       (catch Exception e (error e))))

(deftest test-load-snapshot
  (try (let [events (randomize-order [(events-f3 1)])
             _ (write-eventlog! events 5 10000)
             {:keys [project-id]} (proj/create! (test-proj 1 "test-project") user-info)
             events-2 (events-id project-id 2)
             _ (write-eventlog! events-2 5 20000)
             {events-logs-2 :logs} (proj/load-project {:project-id 1} user-info)
             {:keys [snapshot-id]} (proj/create-snapshot! {:project-id project-id}
                                                          user-info)
             events-3 (events-id project-id 3)
             _ (write-eventlog! events-3 5 20000)
             snapshot (proj/load-project-with-head {:plogs-id {:project-id project-id}
                                                    :snapshot-id snapshot-id}
                                                   user-info)
             result (= (:logs snapshot)
                       events-logs-2)]
         (testing "loading snapshot"
           (is (= true result))))
       (catch Exception e (error e))))

(deftest test-share-project
  (try
    (let [proj1 (assoc (test-proj 1 "test-project1")
                       :allowed-user [(:username user-info)
                                      (:username user-info2)]
                       :read-only-user [])
          _ (proj/create! proj1 user-info)
          _ (proj/share-project {:project-id 1
                                 :shared-by (:username user-info)
                                 :added-allowed-user [(:username user-info2)]
                                 :added-read-only-user [(:username user-info3)]})
          shared-with-user2? (boolean (get-in (proj/list-projects user-info2) [:allowed-projects 1]))
          shared-with-user3? (boolean (get-in (proj/list-projects user-info3) [:read-only-projects 1]))
          _ (proj/share-project {:project-id 1
                                 :shared-by (:username user-info)
                                 :deleted-allowed-user [(:username user-info2)]
                                 :deleted-read-only-user [(:username user-info3)]})
          unshared-with-user2? (boolean (get-in (proj/list-projects user-info2) [:allowed-projects 1]))
          unshared-with-user3? (boolean (get-in (proj/list-projects user-info3) [:read-only-projects 1]))
          _ (proj/share-project {:project-id 1
                                 :shared-by (:username user-info)
                                 :added-allowed-groups [(:role user-info3)]})
          shared-with-role? (boolean (get-in (proj/list-projects user-info3) [:allowed-projects 1]))
          not-shared-with-diffrent-role? (boolean (get-in (proj/list-projects user-info2) [:allowed-projects 1]))
          _ (proj/share-project {:project-id 1
                                 :shared-by (:username user-info)
                                 :deleted-allowed-groups [(:role user-info3)]})
          unshared-with-role? (boolean (get-in (proj/list-projects user-info3) [:allowed-projects 1]))
          _ (proj/share-project {:project-id 1
                                 :shared-by (:username user-info)
                                 :added-read-only-groups [(:role user-info)]})
          shared-with-role-read-only? (boolean (get-in (proj/list-projects user-info2) [:read-only-projects 1]))
          not-shared-with-user? (boolean (get-in (proj/list-projects user-info) [:read-only-projects 1]))
          _ (proj/share-project {:project-id 1
                                 :deleted-read-only-groups [(:role user-info)]
                                 :shared-by (:username user-info)
                                 :public-read-only? true})
          public-shared-with-user3? (get-in (proj/list-projects user-info3) [:public-read-only-projects 1 :public-read-only?])
          public-shared-with-user1? (get-in (proj/list-projects user-info) [:created-projects 1 :public-read-only?])
          public-shared? (and public-shared-with-user3? public-shared-with-user1?)
          _ (proj/share-project {:project-id 1
                                 :shared-by (:username user-info)
                                 :public-read-only? false})
          public-unshared? (boolean (get-in (proj/list-projects user-info3) [:public-read-only-projects 1]))
          _ (proj/share-project {:project-id 1
                                 :shared-by (:username user-info)
                                 :added-allowed-user [(:username user-info2)]
                                 :public-read-only? true})
          allowed-shared-user2? (boolean (get-in (proj/list-projects user-info2) [:allowed-projects 1]))
          not-public-shared? (get-in (proj/list-projects user-info2) [:public-read-only-projects 1 :public-read-only?])
          public-shared-with? (get-in (proj/list-projects user-info3) [:public-read-only-projects 1 :public-read-only?])
          _ (proj/share-project {:project-id 1
                                 :shared-by (:username user-info)
                                 :public-read-only? false})
          still-allowed-shared? (boolean (get-in (proj/list-projects user-info2) [:allowed-projects 1]))
          unshared-public? (get-in (proj/list-projects user-info3) [:public-read-only-projects 1 :public-read-only?])]
      (testing "share/unshare with user"
        (is (true? shared-with-user2?))
        (is (false? unshared-with-user2?))
        (is (true? shared-with-user3?))
        (is (false? unshared-with-user3?)))
      (testing "share/unshare with role"
        (is (true? shared-with-role?))
        (is (false? not-shared-with-diffrent-role?))
        (is (false? unshared-with-role?))
        (is (true? shared-with-role-read-only?))
        (is (false? not-shared-with-user?)))
      (testing "public share/unshare"
        (is (true? public-shared?))
        (is (false? public-unshared?)))
      (testing "public share/unshare and with user"
        (is (true? allowed-shared-user2?))
        (is (nil? not-public-shared?))
        (is (true? public-shared-with?))
        (is (true? still-allowed-shared?))
        (is (nil? unshared-public?))))
    (catch Exception e (error e))))

(deftest test-show-hide-project
  (try
    (let [proj1 (assoc (test-proj 1 "test-project1")
                       :allowed-user [(:username user-info)]
                       :read-only-user [])
          _ (proj/create! proj1 user-info)
          _ (proj/share-project {:project-id 1
                                 :shared-by (:username user-info)
                                 :public-read-only? true})
          show-user-proj1? (get-in (proj/list-projects user-info) [:created-projects 1 :show-in-overview?])
          not-show-public-proj1? (get-in (proj/list-projects user-info2) [:public-read-only-projects 1 :show-in-overview?])
          _ (proj/show-project-in-overview? (get-in (proj/list-projects user-info2) [:public-read-only-projects 1])
                                            user-info2
                                            true)
          public-pin-for-user2? (get-in (proj/list-projects user-info2) [:public-read-only-projects 1 :show-in-overview?])
          not-public-pin-for-user3? (get-in (proj/list-projects user-info3) [:public-read-only-projects 1 :show-in-overview?])
          _ (proj/show-project-in-overview? (get-in (proj/list-projects user-info2) [:public-read-only-projects 1])
                                            user-info2
                                            false)
          unpined-for-user2? (get-in (proj/list-projects user-info2) [:public-read-only-projects 1 :show-in-overview?])
          _ (proj/show-project-in-overview? (get-in (proj/list-projects user-info) [:created-projects 1])
                                            user-info
                                            false)
          user-proj1-hide? (get-in (proj/list-projects user-info) [:created-projects 1 :show-in-overview?])]
      (testing "created projects correct visible"
        (is (true? show-user-proj1?))
        (is (false? not-show-public-proj1?)))
      (testing "public project pin"
        (is (true? public-pin-for-user2?))
        (is (false? not-public-pin-for-user3?)))
      (testing "unpin/hide project"
        (is (false? unpined-for-user2?))
        (is (false? user-proj1-hide?))))
    (catch Exception e (error e))))