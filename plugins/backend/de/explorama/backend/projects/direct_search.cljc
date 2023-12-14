(ns de.explorama.backend.projects.direct-search
  (:require [clojure.string :as string]
            [de.explorama.backend.projects.core :as projects]
            [de.explorama.backend.projects.locks :as locks]
            [de.explorama.backend.projects.session :as session :refer [dispatch-to]]
            [de.explorama.shared.projects.config :as config-projects]))

(defn merge-projects [{:keys [created-projects allowed-projects read-only-projects public-read-only-projects]}]
  (merge created-projects
         allowed-projects
         read-only-projects
         public-read-only-projects))

(defn project-search [query user]
  (let [lower-case-query (string/lower-case query)]
    (->> user
         projects/list-projects
         (reduce (fn [acc [k projects]]
                   (assoc acc
                          k
                          (->> projects
                               (filter (fn [[_ {:keys [description title]}]]
                                         (or (= lower-case-query "projects")
                                             (string/includes? (string/lower-case title)
                                                               lower-case-query)
                                             (string/includes? (string/lower-case (or description ""))
                                                               lower-case-query))))
                               (map (fn [[pid p-desc]]
                                      [pid (assoc p-desc
                                                  :show-in-overview? true)]))
                               (into {}))))
                 {}))))

(defn search [tube {:keys [query limit user result-event]}]
  (let [found-projetcs (project-search query user)
        search-result (->> found-projetcs
                           merge-projects
                           (map (fn [[_ p]]
                                  (let [is-locked? (locks/locked? (:project-id p))
                                        label (if is-locked?
                                                (str (:title p) " (Locked)")
                                                (:title p))]
                                    {:label label
                                     :other-text (or (:description p) "")
                                     :event (str (cond
                                                   is-locked? [:de.explorama.backend.projects.direct-search/project-locked p]
                                                   config-projects/explorama-direct-search-open [:de.explorama.backend.projects.direct-search/add-and-open-project p]
                                                   :else [:de.explorama.backend.projects.direct-search/only-add-project p]))
                                     :project-desc (str p)})))
                           (sort-by (fn [{:keys [label]}]
                                      (string/lower-case label)))
                           vec)]
    (dispatch-to tube [result-event {:title "Projects"
                                     :event (str [:de.explorama.backend.projects.direct-search/show-all found-projetcs])
                                     :ui-results (take limit search-result)
                                     :results search-result
                                     :result-count (count search-result)}])))

(defn overview-search [tube user query]
  (let [search-result (project-search query user)]
    (dispatch-to tube [:de.explorama.backend.projects.views.overview/set-result-projects query search-result])))

(defn reload-projects [tube user-info projects-ids]
  (let [all-projects (-> user-info
                         projects/list-projects
                         merge-projects)]
    (dispatch-to tube
                 [:de.explorama.backend.projects.views.result-overview/update-projects
                  (into [] (-> all-projects
                               (select-keys projects-ids)
                               vals))])))
