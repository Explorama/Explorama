(ns de.explorama.frontend.reporting.screenshot.core
  (:require [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [de.explorama.frontend.reporting.screenshot.util :refer [make-screenshot-]]
            [de.explorama.frontend.reporting.screenshot.pdf :refer [make-screenshot-pdf]]
            [de.explorama.frontend.reporting.screenshot.png :refer [make-screenshot-with-details]]
            [de.explorama.frontend.reporting.config :as config]
            ["moment" :as momentModule]))

(defn- make-screenshot [{:keys [dom-id export-type add-export-details?] :as params}]
  (cond
    (and (= export-type :pdf)
         dom-id)
    (make-screenshot-pdf params)
    (and add-export-details? dom-id)
    (make-screenshot-with-details params)
    dom-id
    (make-screenshot- params)
    :else (make-screenshot- params)))

(defn get-addtional-params [db reporting-type reporting-id]
  (let [{:keys [modules selected-template]} (get-in db (dr-path/visible-dr reporting-id))]
    (cond-> {:frame-ids (reduce (fn [acc {:keys [frame-id]}]
                                  (cond-> acc
                                    frame-id (conj frame-id)))
                                #{}
                                modules)}
      (= reporting-type :report)
      (assoc :module-mapping
             (reduce (fn [acc [idx {[_x y] :position}]]
                       (let [frame-id (get-in modules [idx :frame-id])]
                         (cond-> acc
                           (and frame-id y)
                           (update y #(-> (or % #{})
                                          (conj frame-id))))))
                     {}
                     (map-indexed vector (:tiles selected-template)))
             :rows (get-in selected-template [:grid 1])))))

(defn screenshot-api [db
                      {:keys [context-id label] :as _tab-details}
                      {:keys [type add-export-details? callback-fn] :as _screenshot-options}]
  (let [{reporting-type :type reporting-id :id} context-id]
    (make-screenshot
     (-> {:dom-id (config/export-dom-id context-id)
          :export-type type
          :image-type type
          :file-name (str label "-" (.format momentModule "YYYY-MM-DDTHH-mm-ss"))
          :type reporting-type
          :add-export-details? add-export-details?
          :callback-fn callback-fn}
         (merge
          (get-addtional-params db reporting-type reporting-id))))))

