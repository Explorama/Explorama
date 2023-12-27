(ns de.explorama.backend.projects.core
  (:require [clojure.string :as str]
            [de.explorama.backend.projects.abac
             :refer [create-policy create-policy-map delete-policy edit-policy
                     fetch-policy fetch-policy has-access? policy-id]]
            [de.explorama.backend.projects.persistence.event-log :as event-backend]
            [de.explorama.backend.projects.persistence.project :as project-backend]
            [de.explorama.backend.projects.util.core :as util]
            [de.explorama.shared.common.configs.platform-specific :as platfom-config]
            [de.explorama.shared.common.unification.time :as cljc-time]
            [taoensso.timbre :refer [debug]]))

(defn update-policy
  [policy {:keys [public? users roles shared-to]}]
  (cond-> policy
    users (assoc-in [:user-attributes :attributes :username] users)
    roles (assoc-in [:user-attributes :attributes :role] roles)
    shared-to (assoc :shared-to shared-to)
    (not (nil? public?)) (update-in [:user-attributes :attributes :username]
                                    (fn [old-usernames]
                                      (if public?
                                        (conj old-usernames "*")
                                        (filterv #(not= % "*")
                                                 old-usernames))))))

(defn user-allowed? [{:keys [project-id]} u-info]
  (if platfom-config/explorama-multi-user
    true
    (or (nil? project-id)
        (has-access? u-info project-id "write"))))

(defn- project-public-read-only? [project-id]
  (has-access? {:username "*"} project-id "read"))

(defn get-head [project-id]
  (let [instance (event-backend/new-instance project-id)
        content (vec (event-backend/read-lines instance))]
    (if (empty? content)
      {:c 0
       :t 0}
      (first (peek content)))))

(defonce date-formatter (cljc-time/formatter "yyyy-MM-dd HH:mm:ss"))
(defn format-date [t]
  (cljc-time/unparse date-formatter t))

(defn- write-mapping-table-for-tests [_id _title])
  ;;TODO r1/testing activate this and change it to something that is not csv
  ;; (when (and
  ;;         ;; Otherwise the other vars are unbound, which causes an Exception 
  ;;        config/automate-tests-enabled?
  ;;        config/automate-tests-create
  ;;        config/automate-tests-mapping-path)
  ;;   (let [file-name config/automate-tests-mapping-path
  ;;         file (io/file file-name)]
  ;;     (if (not (.exists file))
  ;;       (do
  ;;         (io/make-parents file-name)
  ;;         (write-csv file-name [["project-id" "project-name" "test-type"]
  ;;                               [id title "project"]]))
  ;;       (write-csv file-name [[id title "project"]])))))

(defn create! [{id :project-id
                title :title
                desc :description}
               user-info
               & _]
  (let [creator (util/username user-info)
        t (cljc-time/now)
        {write-pol-id :id
         :as write-pol} (create-policy-map id title creator "write")
        {read-pol-id :id
         :as read-pol} (create-policy-map id title creator "read")
        instance (project-backend/new-instance id)]
    (create-policy write-pol-id write-pol)
    (create-policy read-pol-id read-pol)
    (project-backend/update instance
                            {:project-id id
                             :title title
                             :description desc
                             :date (format-date t)
                             :last-modified (cljc-time/current-ms)
                             :snapshots []}))
  (write-mapping-table-for-tests id title)
  {:project-id id})

(defn copy! [{src-id :project-id}
             {to-id :project-id
              :as to-project}
             user-info]
  (let [_ (create! to-project user-info true)
        to-proj (project-backend/new-instance to-id)
        {src-snapshots :snapshots} (project-backend/read src-id)
        src-instance (event-backend/new-instance src-id)
        src-logs (event-backend/read-lines src-instance)
        to-logs (mapv #(-> %
                           (assoc-in [1 1 :workspace-id] to-id)
                           (update-in [1 3] (fn [event-params]
                                              (str/replace event-params
                                                           src-id
                                                           to-id))))
                      src-logs)
        to-instance (event-backend/new-instance to-id)]
    (event-backend/append-lines to-instance to-logs)
    (project-backend/update to-proj
                            (assoc to-project
                                   :last-modified (cljc-time/current-ms)
                                   :snapshots src-snapshots))
    {:project-id to-id}))

(defn all-projects []
  (reduce (fn [res id]
            (let [instance (project-backend/new-instance id)
                  {:keys [title date] :as pdesc} (project-backend/read instance)]
              ;TODO r1/projects check access rights
              (cond-> res 
                (and title date) 
                (assoc id pdesc))))
          {}
          (project-backend/list-all)))

(defn all-public-read-only-projects []
  (reduce (fn [res id]
            (let [instance (project-backend/new-instance id)]
              ;TODO r1/projects check access rights
              (assoc res id (project-backend/read instance))))
          {}
          (project-backend/list-all)))

(defn user-public-access?
  "It only checks if the user has direct read access based on username or role.
   If not it is assumed that the user has access because public read.
   Use this only if (has-access? project-id user-info \"read\") is true."
  [project-id {:keys [username role]}]
  (let [read-pol (fetch-policy (policy-id project-id "read"))
        username-access? ((set (get-in read-pol [:user-attributes :attributes :username]))
                          username)
        role-access? ((set (get-in read-pol [:user-attributes :attributes :role]))
                      role)
        public-access? (and (not username-access?)
                            (not role-access?))]
    public-access?))

(defn list-projects [user-info]
  (reduce (fn [acc [p-id p-desc]]
            (if-not platfom-config/explorama-multi-user
              (assoc-in acc [:created-projects p-id] p-desc)
              ;TODO r1/projects implement for multi user
              (let [_public-read-only? (project-public-read-only? p-id)
                    public-access? (user-public-access? p-id user-info)
                    has-access? (partial has-access? user-info p-id)]
                (cond
                  (has-access? "write")
                  (assoc-in acc
                            [:allowed-projects p-id]
                            p-desc)
                  (has-access? "read")
                  (if public-access?
                    (assoc-in acc
                              [:public-read-only-projects p-id]
                              p-desc)
                    (assoc-in acc
                              [:read-only-projects p-id]
                              p-desc))
                  :else acc))))
          {:created-projects {}
           :allowed-projects {}
           :read-only-projects {}
           :public-read-only-projects {}}
          (all-projects)))

(defn project-links [username base-url]
  (let [projects (if (nil? username)
                   (all-projects)
                   (->> username
                        list-projects
                        vals
                        (apply merge)))]
    (mapv (fn [[id val]]
            {:title (:title val)
             :url (str base-url "/?project-id=" id)})
          projects)))

(defn read-log-file [{:keys [project-id] :as _plogs-id} _user-info]
  (let [instance (event-backend/new-instance project-id)]
    (event-backend/read-lines instance)))

(defn load-project [{:keys [project-id] :as plogs-id} user-info]
  (let [instance (project-backend/new-instance project-id)
        project-desc (project-backend/read instance)
        logs (read-log-file plogs-id user-info)]
    {:description (dissoc project-desc :date-obj)
     :logs logs}))

(defn delete-project! [{project-id :project-id}]
  (delete-policy (policy-id project-id "read"))
  (delete-policy (policy-id project-id "write"))
  (let [instance-project (project-backend/new-instance project-id)
        instance-event (event-backend/new-instance project-id)]
    (event-backend/delete instance-event)
    (project-backend/delete instance-project)))

(defn create-snapshot! [{project-id :project-id
                         snapshot-head :head
                         snapshot-id :id
                         snapshot-title :title
                         snapshot-description :description}
                        _user-info]
  (let [instance (project-backend/new-instance project-id)
        project (project-backend/read instance)
        head (or snapshot-head
                 (get-head project-id))
        snapshot-id (or snapshot-id
                        (util/gen-project-id))
        head-already-exist? (some #(when (= head
                                            %)
                                     %)
                                  (get project :snapshots))]
    (if head-already-exist?
      (do
        (debug "A snapshot with the same head already-exist return as result"
               head-already-exist?)
        head-already-exist?)
      (do
        (project-backend/update instance
                                (update project
                                        :snapshots
                                        #(conj (or % [])
                                               {:snapshot-id snapshot-id
                                                :head head
                                                :title snapshot-title
                                                :description snapshot-description})))
        {:snapshot-id snapshot-id}))))

(defn update-snapshot! [project-id snapshot-id new-title new-description]
  (let [instance (project-backend/new-instance project-id)
        project (project-backend/read instance)]
    (project-backend/update instance
                            (update project
                                    :snapshots
                                    (fn [snapshots]
                                      (mapv (fn [{snap-id :snapshot-id
                                                  :as snapshot}]
                                              (if (= snap-id snapshot-id)
                                                (assoc snapshot
                                                       :title new-title
                                                       :description new-description)
                                                snapshot))
                                            snapshots))))))

(defn delete-snapshot! [{project-id :project-id
                         head :head}
                        _user-info]
  (let [instance (project-backend/new-instance project-id)
        project (project-backend/read instance)]
    (project-backend/update instance
                            (update project
                                    :snapshots
                                    #(filterv (fn [{snapshot-head :head}]
                                                (not (= snapshot-head
                                                        head)))
                                              %)))))

(defn compare-snapshots
  [{{snap1-t :t
     snap1-c :c} :head}
   {{snap2-t :t
     snap2-c :c} :head}]
  (cond
    (and (= snap1-t
            snap2-t)
         (= snap1-c
            snap2-c)) 0
    (or (> snap1-t snap2-t)
        (and (= snap1-t snap2-t)
             (> snap1-c snap2-c))) 1
    :else -1))

(defn remove-newer-heads [{:keys [project-id snapshots]} instance result head]
  (let [new-instance (event-backend/new-instance project-id)]
    (event-backend/append-lines new-instance result)
    (let [project (project-backend/read instance)]
      (project-backend/update instance
                              (assoc project
                                     :snapshots
                                     (filterv #(>= (compare-snapshots {:head head} %)
                                                   0)
                                              snapshots))))))

(defn load-project-with-head [{:keys [snapshot-id head read-only?]
                               {:keys [project-id] :as plogs-id} :plogs-id
                               :as head-desc}
                              user-info]
  (let [_public-access? (when (has-access? user-info project-id "read")
                         (user-public-access? project-id user-info))
        _public-read-only? (project-public-read-only? project-id)
        instance (project-backend/new-instance project-id)
        project-desc (project-backend/read instance)
        instance (event-backend/new-instance project-id)
        head (or head
                 (some #(when (= (:snapshot-id %) snapshot-id)
                          (:head %))
                       (get project-desc :snapshots)))
        content (vec (event-backend/read-lines instance))
        log-count (count content)
        result (loop [result []
                      i 0]
                 (if (< i log-count)
                   (let [line (get content i)]
                     (if (= head (first line))
                       (conj result line)
                       (recur (conj result line)
                              (inc i))))
                   result))]
    (when-not read-only?
      (remove-newer-heads plogs-id instance result head))
    (if result
      {:description (dissoc project-desc :date-obj)
       :snapshot head-desc
       :plogs-id plogs-id
       :logs result}
      {:description {:project-id project-id
                     :snapshot-head head}
       :plogs-id plogs-id
       :error "empty result"})))

(defn- policy-update-map [public-read-only?
                          old-shared-to new-shared-to
                          old-users old-groups
                          new-users new-groups
                          deleted-users deleted-groups]
  (let [deleted-users (set deleted-users)
        deleted-groups (set deleted-groups)]
    (cond-> {}
      (not (nil? public-read-only?)) (assoc :public? public-read-only?)
      (not= old-shared-to new-shared-to) (assoc :shared-to new-shared-to)
      (not-empty deleted-users) (assoc :users (filterv #(not (deleted-users %)) old-users))
      (not-empty deleted-groups) (assoc :roles (filterv #(not (deleted-groups %)) old-groups))
      (not-empty new-groups) (update :roles (fn [c-groups]
                                              (vec (concat (or c-groups old-groups)
                                                           new-groups))))
      (not-empty new-users) (update :users (fn [c-users]
                                             (vec (concat (or c-users old-users)
                                                          new-users)))))))

(defn- merge-share [cur-share new-shares shared-by]
  (reduce (fn [share n-share]
            (if n-share
              (assoc share
                     (util/username n-share) shared-by)
              share))
          cur-share
          new-shares))

(defn- update-shared-to [shared-by old-shared-to new-users new-groups deleted-users deleted-groups public-read-only?]
  (cond-> old-shared-to
    (not-empty deleted-users) (util/apply-dissoc deleted-users)
    (not-empty deleted-groups) (util/apply-dissoc deleted-groups)
    (not-empty new-groups) (merge-share new-groups shared-by)
    (not-empty new-users) (merge-share new-users shared-by)
    (and (not (contains? old-shared-to "*"))
         public-read-only?) (assoc "*" shared-by)
    (and (not (nil? public-read-only?))
         (not public-read-only?)) (dissoc "*")))

(defn share-project
  [{:keys [project-id public-read-only? shared-by
           added-allowed-user added-allowed-groups
           added-read-only-user added-read-only-groups
           deleted-allowed-user deleted-allowed-groups
           deleted-read-only-user deleted-read-only-groups]}]
  (let [read-pol-id (policy-id project-id "read")
        {old-read-pol-shared :shared-to
         {{old-read-users :username
           old-read-groups :role} :attributes} :user-attributes
         :as read-pol} (fetch-policy read-pol-id)
        new-read-pol-shared (update-shared-to shared-by old-read-pol-shared
                                              added-read-only-user added-read-only-groups
                                              deleted-read-only-user deleted-read-only-groups
                                              public-read-only?)
        updated-read-pol (update-policy read-pol (policy-update-map public-read-only?
                                                                    old-read-pol-shared new-read-pol-shared
                                                                    old-read-users old-read-groups
                                                                    added-read-only-user added-read-only-groups
                                                                    deleted-read-only-user deleted-read-only-groups))
        write-pol-id (policy-id project-id "write")
        {old-write-pol-shared :shared-to
         {{old-write-users :username
           old-write-groups :role} :attributes} :user-attributes
         :as write-pol} (fetch-policy write-pol-id)
        new-write-pol-shared (update-shared-to shared-by old-write-pol-shared
                                               added-allowed-user added-allowed-groups
                                               deleted-allowed-user deleted-allowed-groups nil)
        updated-write-pol (update-policy write-pol (policy-update-map nil
                                                                      old-write-pol-shared new-write-pol-shared
                                                                      old-write-users old-write-groups
                                                                      added-allowed-user added-allowed-groups
                                                                      deleted-allowed-user deleted-allowed-groups))]
    (when-not (= read-pol updated-read-pol)
      (edit-policy read-pol-id updated-read-pol))
    (when-not (= write-pol updated-write-pol)
      (edit-policy write-pol-id updated-write-pol))
    {:read updated-read-pol
     :write updated-write-pol}))

(defn update-project-detail [project-id update-type new-value]
  (let [instance (project-backend/new-instance project-id)
        desc (project-backend/read instance)]
    (project-backend/update instance
                            (assoc desc update-type new-value))))