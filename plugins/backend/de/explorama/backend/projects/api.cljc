(ns de.explorama.backend.projects.api
  (:require [de.explorama.backend.common.environment.probe :as probe]
            [de.explorama.backend.projects.core :as projects]
            [de.explorama.backend.projects.direct-search :as direct-search]
            [de.explorama.backend.projects.event-log :as event-log]
            [de.explorama.backend.projects.locks :as locks]
            [de.explorama.backend.projects.notifications :as notifications]
            [de.explorama.backend.projects.session :as session]
            [de.explorama.backend.projects.util.core :as util]
            [de.explorama.shared.common.config :as config-shared]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [taoensso.timbre :refer [debug warn] :as log]))

(defn- user-valid? [tube user-info]
  true)

(defn- update-user-info [tube user-info failed-callback]
  (merge tube user-info))

(defn- load-and-lock-project [client-callback user-info
                              project-id read-only? client-id project-infos head-desc]
  (let [user-info (select-keys user-info [:name :username :mail])
        already-locked? (locks/locked? project-id)
        successful-locked? (when (and (not already-locked?)
                                      (or (and config-shared/explorama-project-sync?
                                               (not read-only?))
                                          (not config-shared/explorama-project-sync?)))
                             (try
                               (locks/lock project-id
                                           client-id
                                           user-info)
                               (catch #?(:clj Throwable :cljs :default) e
                                 (probe/rate-exception e)
                                 (warn e
                                       "Error locking project"
                                       {:client-id     client-id
                                        :user-info     user-info
                                        :project-infos project-infos}))))]
    (debug ::load-project-infos user-info project-id)

    ;; (when config-shared/explorama-project-sync?)
    ;;   (session/dispatch-user-watching client-id project-id user-info)
    ;;   (session/dispatch-to tube
    ;;                        [:de.explorama.backend.projects.core/set-watching-users
    ;;                         (session/users-by-project-id client-id project-id)]))

    (if (or (and (not already-locked?)
                 successful-locked?)
            #_(and config-shared/explorama-project-sync?)
            read-only?)
      (client-callback (if (get head-desc :head)
                         (projects/load-project-with-head head-desc user-info)
                         (projects/load-project project-infos user-info)))
      (throw (ex-info "Project is already locked" {:project-id project-id
                                                   :client-id  client-id
                                                   :user-info  user-info})))))

(defn create-project [{:keys [client-callback failed-callback user-validation] :as metas}
                      [project-infos user-info]]
  (when (user-valid? metas user-info)
    (let [created (projects/create! project-infos user-info)]

      (client-callback (merge project-infos created)))))

(defn load-project-after-create [{:keys [client-callback failed-callback user-validation user-info client-id]
                                  :as metas}
                                 [project-id]]

  (let [user-info (select-keys user-info [:name :username :mail])]
    (try
      (locks/lock project-id client-id user-info)
      (catch #?(:clj Throwable :cljs :default) e
        (probe/rate-exception e)
        (warn e
              "Error locking project"
              {:client-id  client-id
               :user-info  user-info
               :project-id project-id})))))

(defn load-project-infos [{:keys [client-callback failed-callback user-validation user-info client-id]
                           :as metas}
                          [{:keys [project-id]
                            :as project-infos}
                           {:keys [head-desc]}
                           read-only?]]
  (load-and-lock-project client-callback user-info project-id read-only? client-id project-infos head-desc))

(defn log-event [{:keys [client-callback failed-callback user-info client-id]
                  :as metas}
                 [event-desc a]]
  (let [user-info (select-keys user-info [:username :role :token])]
    ;TODO r1/warning investigate
    (event-log/eventlog metas
                        (assoc event-desc
                               :user-info user-info
                               :client-id client-id))))

(defn request-projects [{:keys [client-callback failed-callback]
                         :as metas}
                        [user-info]]
  (when (user-valid? metas user-info)
    (client-callback (projects/list-projects user-info))))

(defn update-project-detail [{:keys [client-callback failed-callback]
                              :as metas}
                             [project-id update-type new-value user-info]]
  (when (user-valid? metas user-info)
    (projects/update-project-detail project-id update-type new-value)
    (client-callback (projects/list-projects user-info))))
    ;; (notifications/broadcast)))

(defn copy-project [{:keys [client-callback failed-callback]
                     :as metas}
                    [src-project to-project-infos user-info]]
  (when (user-valid? metas user-info)
    (let [copied (projects/copy! src-project
                                 to-project-infos
                                 user-info)]
      (client-callback copied))))

(defn search-projects [tube [_ user-info query]]
  (when (user-valid? tube user-info)
    (direct-search/overview-search tube user-info query)))

(defn create-snapshot [tube [_ snapshot-infos user-info]]
  (when (user-valid? tube user-info)
    (projects/create-snapshot! snapshot-infos user-info)
    (session/dispatch-to tube [:de.explorama.backend.projects.views.overview/update-projects (projects/list-projects user-info)])))

(defn delete-snapshot [tube [_ snapshot-infos user-info]]
  (when (user-valid? tube user-info)
    (projects/delete-snapshot! snapshot-infos user-info)
    (session/dispatch-to tube [:de.explorama.backend.projects.views.overview/update-projects (projects/list-projects user-info)])


  ;; :de.explorama.backend.projects.core/server-loaded-project
  ;;    ;; signals finalization of loading a project into the client
  ;; (fn [{client-id :client-id tube-project-id :project-id :as tube} [_ user-info project-id]]
  ;;   (go
  ;;     (when (user-valid? tube user-info)
  ;;       (when (not= tube-project-id project-id)
  ;;         (warn "Problem with project loading: Finalization of loading a project
  ;;                           is signalled by the client, but the project-id on the WebSocket channel differ."
  ;;               {:project-id-on-tube                tube-project-id
  ;;                :project-id-from-websocket-message project-id
  ;;                :tube                              tube
  ;;                :client-id                         client-id}))
  ;;                                 ;; TODO: This should raise an exception. However, I'm not sure how good our
  ;;                                 ;; exception-handling is. So, I'll leave it with this warning for now, as this
  ;;                                 ;; should not create an issue with the stability of our system itself. It should
  ;;                                 ;; be local to that client.
  ;;       (when project-id
  ;;         (notifications/remove-notification [(:username user-info) ""]
  ;;                                            project-id)
  ;;         (notifications/remove-notification [(:username user-info) (:role user-info)]
  ;;                                            project-id)
  ;;         (notifications/broadcast))))
    tube))

(defn unloaded-project [{:keys [client-id project-read-only? username]
                         :as tube}
                        [_ _ project-id]]
  (session/broadcast-event client-id
                           (:project-id tube)
                           [:remove-mouse-pos username]
                           true)

  (when config-shared/explorama-project-sync?
    (session/dispatch-user-left client-id project-id {:username username}))

  (when (or (and project-read-only? (not config-shared/explorama-project-sync?))
            (not project-read-only?))
    (locks/unlock project-id client-id))
  (dissoc tube :project-id))

(defn load-head [{:keys [client-callback failed-callback]
                  :as metas}
                 [head-infos user-info]]
  (when (user-valid? metas user-info)
    (client-callback (projects/load-project-with-head head-infos user-info))))

(defn based-events [{:keys [client-callback failed-callback]
                     :as metas}
                    [plogs-id user-info]]
  (when (user-valid? metas user-info)
    (client-callback (:logs (projects/load-project plogs-id user-info)))))

(defn delete-project [{:keys [client-callback failed-callback]
                       :as metas}
                      [project-infos user-info]]
  (if (and (projects/user-allowed? project-infos user-info)
           (user-valid? metas user-info))
    (let [right-users-mapping (util/request-user-roles)]
      (projects/delete-project! project-infos)
      (notifications/delete-notifications! project-infos right-users-mapping)
      (notifications/broadcast)
      (client-callback {:success? true}))
    (client-callback {:success? false
                      :reason :not-allowed})))

(defn update-snapshot-desc [tube [_ project-id snapshot-id new-title new-description]]
  (projects/update-snapshot! project-id snapshot-id new-title new-description)
  (session/broadcast [:de.explorama.backend.projects.views.overview/reload-projects]))

(defn share-project [tube
                     [_ {:keys [added-allowed-user added-allowed-groups
                                added-read-only-user added-read-only-groups
                                deleted-allowed-user deleted-allowed-groups
                                deleted-read-only-user deleted-read-only-groups
                                project-id project-creator]
                         :as   share-info}]]
  (let [right-users-mapping (util/request-user-roles)
        content {:type       :new-project
                 :project-id project-id}
        plogs (projects/read-log-file {:project-id project-id} nil)
        share-with {:user  (concat added-allowed-user added-read-only-user)
                    :group (concat added-allowed-groups added-read-only-groups)}]
    (notifications/remove-notifications-for-users (into deleted-read-only-user
                                                        deleted-allowed-user)
                                                  project-id)
    (notifications/remove-group-notifications (into deleted-allowed-groups
                                                    deleted-read-only-groups)
                                              project-id
                                              right-users-mapping)
    (projects/share-project share-info)
    (session/dispatch-to tube [:de.explorama.backend.projects.views.share-project/share-settings (mapv second plogs) share-with])
    (notifications/add-notifications-for-users (into added-read-only-user
                                                     added-allowed-user)
                                               project-id
                                               content)
    (notifications/add-group-notifications (into added-allowed-groups
                                                 added-read-only-groups)
                                           project-creator
                                           project-id
                                           right-users-mapping
                                           content)
    (notifications/broadcast)))

(defn show-project-in-overview [tube [_ project-desc user-info show? query]]
  (when (user-valid? tube user-info)
    (let [projects (if query
                     (direct-search/project-search query user-info)
                     (projects/list-projects user-info))]
      (when-not show?
        (notifications/remove-notification [(:username user-info) ""] (:project-id project-desc))
        (notifications/remove-notification [(:username user-info) (:role user-info)] (:project-id project-desc))
        (notifications/broadcast))
      (session/dispatch-to tube [:de.explorama.backend.projects.views.overview/update-projects projects]))))

(defn create-session [tube [_ user-info]]
  (when (user-valid? tube user-info)
    (session/dispatch-to tube [ws-api/notify-client (notifications/get-notifications user-info)])
    (session/dispatch-to tube [ws-api/locks-client (locks/locks)])))


(defn direct-search [{:keys [client-callback failed-callback user-info client-id]
                      :as metas}
                     [query]]
   (when (user-valid? metas user-info)
     (client-callback (direct-search/project-search query user-info))))

(defn reload-projects [tube [_ user-info projects-id]]
  (when (user-valid? tube user-info)
    (direct-search/reload-projects tube user-info projects-id)))

