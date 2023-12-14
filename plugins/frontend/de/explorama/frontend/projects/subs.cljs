(ns de.explorama.frontend.projects.subs
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.projects.path :as pp]))

(defn project-loading? [db]
  (get-in db pp/project-loading))

(re-frame/reg-sub
 ::project-loading?
 (fn [db]
   (project-loading? db)))

(defn loaded-project-id [db]
  (get-in db (pp/project-id)))

(re-frame/reg-sub
 :de.explorama.frontend.projects.core/loaded-project-id
 (fn [db _]
   (loaded-project-id db)))

(defn project-by-id [db id]
  (or (get-in db [:projects :projects :created-projects id])
      (get-in db [:projects :projects :allowed-projects id])
      (get-in db [:projects :projects :read-only-projects id])
      (get-in db [:projects :projects :public-read-only-projects id])))

(defn project-loaded-infos [db]
  (let [project-id (loaded-project-id db)]
    (project-by-id db project-id)))

(re-frame/reg-sub
 :de.explorama.frontend.projects.core/project-loaded-infos
 (fn [db _]
   (project-loaded-infos db)))

