(ns de.explorama.backend.projects.abac
  (:require [de.explorama.backend.abac.pep :as pep]
            [de.explorama.backend.abac.policy-repository :as pr]))

(defn policy-id
  [project-id suffix]
  (keyword (str project-id "-" suffix)))

(defn fetch-policy [policy-id]
  (update
   (pr/fetch-policy policy-id)
   :shared-to
   (fn [shared] ;Make sure shared-to keys are string
     (into {}
           (for [[k v] shared]
             [(name k) v])))))

(defn create-policy-map
  [project-id project-name creator suffix]
  {:user-attributes {:attributes {:username (if (= suffix "write")
                                              [creator]
                                              [])
                                  :role []}
                     :optional [:username :role]}
   :data-attributes {:id [project-id]}
   :id (policy-id project-id suffix)
   :name (str project-name " " suffix)
   :creator creator
   :shared-to {}
   :group-conf :projects
   :group-id project-id
   :group-name project-name})

(defn has-access? [user project-id prefix]
  (pep/checked-function-call (policy-id project-id prefix)
                             user
                             {:id project-id}
                             (fn []
                               true)
                             (fn [failreason]
                               false)))

(def create-policy pr/create-policy)
(def delete-policy pr/delete-policy)
(def edit-policy pr/edit-policy)