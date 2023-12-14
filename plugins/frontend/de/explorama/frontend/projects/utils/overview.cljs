(ns de.explorama.frontend.projects.utils.overview
  (:require [clojure.string :as str]))

(defn filter-projects [overview-filter projects]
  (case overview-filter
    :created (:created-projects projects)
    :shared (merge
             (:allowed-projects projects)
             (:read-only-projects projects))
    :public (:public-read-only-projects projects)
    (merge (:created-projects projects)
           (:allowed-projects projects)
           (:read-only-projects projects)
           (:public-read-only-projects projects))))

(defn sort-projects [overview-sorting projects]
  (let [sorting-fn
        #(case overview-sorting
           :title-a-z (sort-by (fn [p] (str/lower-case (get p :title))) < %)
           :title-z-a (sort-by (fn [p] (str/lower-case (get p :title))) > %)
           (sort-by :last-modified > %))]
    (->> projects
         vals
         sorting-fn
         vec)))