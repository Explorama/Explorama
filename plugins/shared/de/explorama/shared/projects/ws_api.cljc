(ns de.explorama.shared.projects.ws-api)

(def update-user-info ::update-user-info)
(def failed-handler ::failed-handler)

(def log-event-route ::log-event-route)
(def log-event-result ::log-event-result)

(def request-projects-route ::request-projects-route)
(def request-projects-result ::request-projects-result)
(def update-project-detail-route ::update-project-detail-route)

(def project-sync-event-route ::project-sync-event-route)
(def project-sync-event-result ::project-sync-event-result)

(def re-connect-route ::re-connect-route)

(def search-projects-route ::search-projects-route)
(def search-projects-result ::search-projects-result)

(def create-project-route ::create-project-route)
(def create-project-result ::create-project-result)

(def copy-project-route ::copy-project-route)
(def copy-project-result ::copy-project-result)

(def create-snapshot-route ::create-snapshot-route)
(def create-snapshot-result ::create-snapshot-result)

(def delete-snapshot-route ::delete-snapshot-route)
(def delete-snapshot-result ::delete-snapshot-result)

(def load-project-infos-route ::load-project-infos-route)
(def load-project-infos-result ::load-project-infos-result)

(def load-project-after-create-route ::load-project-after-create-route)
(def load-project-after-create-result ::load-project-after-create-result)

(def server-loaded-project-route ::server-loaded-project-route)
(def server-loaded-project-result ::server-loaded-project-result)

(def unloaded-project-route ::unloaded-project-route)
(def unloaded-project-result ::unloaded-project-result)

(def load-head-route ::load-head-route)
(def load-head-result ::load-head-result)

(def based-events-route ::based-events-route)
(def based-events-result ::based-events-result)

(def update-snapshot-desc-route ::update-snapshot-desc-route)
(def update-snapshot-desc-result ::update-snapshot-desc-result)

(def delete-project-route ::close-project-route)
(def delete-project-result ::close-project-result)

(def exportable-route ::exportable-route)
(def exportable-result ::exportable-result)

(def share-project-route ::share-project-route)
(def share-project-rescreate-screenshotult ::share-project-result)

(def show-project-in-overview-route ::show-project-in-overview-route)
(def show-project-in-overview-result ::show-project-in-overview-result)

(def create-session-route ::create-session-route)
(def create-session-result ::create-session-result)

(def save-as-file-route ::save-as-file-route)
(def save-as-file-result ::save-as-file-result)

(def search-route ::search-route)
(def search-result ::search-result)

(def reload-projects-route ::reload-projects-route)
(def reload-projects-result ::reload-projects-result)

(def mouse-position-update-route ::mouse-position-update-route)
(def mouse-position-update-result ::mouse-position-update-result)

(def automated-tests-results-route ::automated-tests-results-route)
(def automated-tests-results-result ::automated-tests-results-result)

(def receive-mouse-position-update ::receive-mouse-position-update)
(def remove-mouse-pos ::remove-mouse-pos)

(def notify-client ::notify-client)
(def locks-client ::locks-client)