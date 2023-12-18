(ns de.explorama.backend.indicator.persistence.store.core
  "This will be the main point to access the backend where the indicator descriptions will be saved.
   There is no rights checking if a user is allowed to access/change/delete a de.explorama.backend.indicator.
   This should be done before calling the persistance functions."
  (:require [de.explorama.backend.indicator.persistence.store.adapter :as adapter]
            [de.explorama.backend.indicator.persistence.store.expdb :as expdb-backend]
            [taoensso.timbre :refer [error]]))

(defonce instance (atom nil))

(defn write-indicator
  "Returns a indicator with the following keys:
   [:id :creator :name :description :shared-by]."
  [indicator]
  (adapter/write-indicator @instance indicator))

(defn list-indicators
  "Returns all indicators with the following keys:
   [:id :creator :name :description :shared-by]"
  []
  (adapter/list-indicators @instance))

(defn user-for-indicator-id [id]
  (adapter/user-for-indicator-id @instance id))

(defn read-indicator
  "Returns the complete indicator description."
  ([id]
   (let [creator (user-for-indicator-id id)]
     (if (nil? creator)
       (error "No creator found for indicator id" id)
       (read-indicator creator id))))
  ([_user id]
   (adapter/read-indicator @instance id)))

(defn short-indicator-desc
  "Returns only the following keys:
   [:id :creator :name :description :shared-by]"
  [_user id]
  (adapter/short-indicator-desc @instance id))

(defn list-all-user-indicators
  "Returns all indicators for a specific user with the following keys: 
   [:id :creator :name :description :shared-by]"
  [user]
  (adapter/list-all-user-indicators @instance user))

(defn delete-indicator
  "Deletes the given indicator version.
   On successfull deletion returns a vector:
     [creator id]
   On failure a error message is printed and nil will be returned."
  [_user id]
  (adapter/delete-indicator @instance id))

(defn new-instance []
  (reset! instance (expdb-backend/new-instance)))