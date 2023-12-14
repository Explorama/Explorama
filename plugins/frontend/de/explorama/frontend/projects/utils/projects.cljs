(ns de.explorama.frontend.projects.utils.projects
  (:require [de.explorama.frontend.projects.path :as path]))

(defn group-logs-by-origin-indexed [logs]
  (let [indexed (map-indexed vector logs)
        grouped (group-by (fn [[_ [_ [origin]]]]
                            origin)
                          indexed)
        woco-events (get grouped "woco")
        vertical-events (dissoc grouped "woco")]
    (assoc (into {}
                 (map (fn [[origin events]]
                        [origin (vec (sort-by first (into events
                                                          woco-events)))])
                      vertical-events))
           "woco" woco-events)))

(defn group-logs-by-origin [logs]
  (let [indexed-grouped (group-logs-by-origin-indexed logs)]
    (into {}
          (map (fn [[origin events]]
                 [origin (mapv #(get-in % [1 1])
                               events)])
               indexed-grouped))))

(defn all-projects [db]
  (reduce (fn [acc [_ ps]]
            (merge acc
                   ps))
          {}
          (get-in db path/projects)))