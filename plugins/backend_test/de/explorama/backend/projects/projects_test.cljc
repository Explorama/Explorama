(ns de.explorama.backend.projects.projects-test
  (:require [clojure.test :refer [compose-fixtures deftest is testing
                                  use-fixtures]]
            [de.explorama.backend.expdb.middleware.simple-db-test :refer [test-setup]]
            [de.explorama.backend.projects.core :as proj]
            [de.explorama.backend.projects.event-log :as core]
            [de.explorama.backend.projects.persistence.event-log :as repo]
            [taoensso.timbre :refer [error]]))
(def ^:private user-info {:username "test-user1"
                          :role     "test-role"})

(def ^:private user-info2 {:username "test-user2"
                           :role     "test-role"})

(def ^:private user-info3 {:username "test-user3"
                           :role     "test-role2"})

(defn- frame-id [project-id frame-id]
  {:workspace-id project-id
   :frame-id frame-id})

(defn- event [workspace-id id c o]
  {:frame-id (frame-id workspace-id id)
   :event-name (str "test" c)
   :origin o
   :description {:description "test desc"}
   :user-info user-info})

(defn- filter-events [events workspace-id]
  (filterv (fn [{{id :workspace-id} :frame-id}]
             (= id workspace-id))
           events))

(defn- events-f1 [workspace-id] (map #(event workspace-id 1 % "Mosaic") (range 16)))
(defn- events-f2 [workspace-id] (map #(event workspace-id 2 % "Suche") (range 11)))
(defn- events-f3 [workspace-id] (map #(event workspace-id 3 % "Map") (range 17)))
(defn- events-id [workspace-id id] (map #(event workspace-id id % "Mosaic") (range 16)))

(defn- test-proj
  ([project-id proj-name]
   {:project-id project-id
    :title proj-name
    :description {:test "desc"}})
  ([project-id proj-name _]
   {:project-id project-id
    :title proj-name
    :description {:test "desc"}}))

(defn- identity-mapping [_ value]
  value)

(use-fixtures :each (compose-fixtures test-setup (fn [test-fn]
                                                   (reset! @#'core/counter 0)
                                                   (test-fn))))

(defn- equal-test [seq1 seq2 value-mapping]
  (loop [lines seq1
         events seq2
         result true]
    (cond
      (and (empty? lines) (not-empty events)) false
      (and (not-empty lines) (empty? events)) false
      (and (empty? lines) (empty? events)) result
      :else (let [[_counter [lorigin lframe-id levent-name ldesc]] (first lines)
                  {:keys [frame-id event-name origin description]} (first events)]
              (recur
               (rest lines)
               (rest events)
               (and (= lorigin (value-mapping :origin origin))
                    (= lframe-id (value-mapping :frame-id frame-id))
                    (= levent-name (value-mapping :event-name event-name))
                    (= ldesc (value-mapping :description description))
                    result))))))

(defn- write-eventlog! [user events]
  (doseq [event events]
    (core/eventlog {:user-info user} event)))

(defn- randomize-order [seqs]
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

(deftest test-eventlog-simple
  (write-eventlog! user-info (events-f1 1))
  (let [path 1
        instance (repo/new-instance path)
        lines (repo/read-lines instance)
        result (equal-test lines (events-f1 1) identity-mapping)]
    (testing "the eventlog can be read as written"
      (is (true? result)))))

(deftest test-eventlog-shuffle
  (let [events (randomize-order [(events-f1 1) (events-f2 1) (events-f3 1)])
        _ (write-eventlog! user-info events)
        path1 1
        instance (repo/new-instance path1)
        lines (repo/read-lines instance)
        result (equal-test lines events identity-mapping)]
    (testing "a shuffled eventlog comes out in the right order"
      (is (true? result)))))

(deftest test-get-head
  (write-eventlog! user-info (events-f1 1))
  (let [path 1
        head (proj/get-head path)]
    (testing "head is counted up"
      (is (= (mod (count (events-f1 1))
                  1000000)
             (:c head))))))

(deftest test-create-project
  (let [events (randomize-order [(events-f1 1) (events-f2 2) (events-f3 1)])
        _ (write-eventlog! user-info events)
        _ (proj/create! (test-proj 1 "test-project") user-info)
        proj-path1 1
        proj-instance1 (repo/new-instance proj-path1)
        proj-log1 (repo/read-lines proj-instance1)
        result1 (equal-test proj-log1
                            (filter-events events 1)
                            identity-mapping)
        _ (proj/create! (test-proj 2 "test-project") user-info)
        proj-path2 2
        proj-instance2 (repo/new-instance proj-path2)
        proj-log2 (repo/read-lines proj-instance2)
        result2 (equal-test proj-log2
                            (filter-events events 2)
                            identity-mapping)]
    (testing "project event log can be read and contains all events for workspace 1"
      (is (true? result1)))
    (testing "project event log can be read and contains all events for workspace 2"
      (is (true? result2)))))

(deftest test-load-project
  (let [events (randomize-order [(events-f1 1) (events-f2 2) (events-f3 1)])
        _ (write-eventlog! user-info events)
        cur-project (test-proj 1 "test-project")
        {:keys [project-id]} (proj/create! cur-project user-info)
        {:keys [logs]} (proj/load-project {:project-id project-id} user-info)
        result (equal-test logs
                           (filter-events events 1)
                           identity-mapping)]
    (testing "loading project event logs"
      (is (= true result)))))


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
             _no-public-projects (empty? (proj/all-public-read-only-projects))
             _ (proj/share-project {:project-id 1
                                    :shared-by (:username user-info)
                                    :public-read-only? true})
             public-projects (-> (proj/all-public-read-only-projects) keys set)]
         (testing "Getting out all projects for all users there is currently no ownership or access rights"
           (is (= (-> projects1 :created-projects keys set)
                  #{1 2 3 4}))
           (is (= (-> projects2 :created-projects keys set)
                  #{1 2 3 4}))
           (is (= (-> projects3 :created-projects keys set)
                  #{1 2 3 4}))
           (is (= public-projects
                  #{1 2 3 4})))
         #_#_#_#_; TODO r1/projects fix this when as soon as rights work again
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
       (catch #?(:cljs :default :clj Throwable) e (error e))))

(deftest test-load-snapshot
  (let [events (randomize-order [(events-f3 1)])
        _ (write-eventlog! user-info events)
        {:keys [project-id]} (proj/create! (test-proj 1 "test-project") user-info)
        events-2 (events-id project-id 2)
        _ (write-eventlog! user-info events-2)
        {events-logs-2 :logs} (proj/load-project {:project-id 1} user-info)
        {:keys [snapshot-id]} (proj/create-snapshot! {:project-id project-id}
                                                     user-info)
        events-3 (events-id project-id 3)
        _ (write-eventlog! user-info events-3)
        snapshot (proj/load-project-with-head {:plogs-id {:project-id project-id}
                                               :snapshot-id snapshot-id}
                                              user-info)
        result (= (:logs snapshot)
                  events-logs-2)]
    (testing "loading snapshot"
      (is (= true result)))))

#_; TODO r1/projects fix this when as soon as rights work again
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
      (catch #?(:cljs :default :clj Throwable) e (error e))))
