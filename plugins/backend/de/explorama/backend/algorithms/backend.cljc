(ns de.explorama.backend.algorithms.backend
  (:require [de.explorama.backend.algorithms.algorithm :as alg]
            [de.explorama.backend.algorithms.prediction-registry.core :as pregistry]
            [de.explorama.backend.algorithms.predictions :as kp]
            [de.explorama.backend.algorithms.registry :as registry]
            [de.explorama.backend.algorithms.regression.lr :as lr]
            [de.explorama.backend.algorithms.tasks :as tasks]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.shared.algorithms.ws-api :as ws-api]
            [taoensso.timbre :refer [warn]]))

(defn- default-fn [& _]
  (warn "Not yet implemented"))

(defn init []
  (pregistry/new-instance)
  (frontend-api/register-routes {ws-api/user-info-update-route (partial default-fn ws-api/user-info-update-route)
                                 ws-api/data-options           tasks/data-options
                                 ws-api/training-data          tasks/training-data
                                 ws-api/predict                tasks/predict
                                 ws-api/load-predictions       tasks/load-predictions
                                 ws-api/load-prediction        tasks/load-prediction
                                 ws-api/save-prediction        tasks/save-prediction
                                 ws-api/delete-prediction      tasks/delete-prediction
                                 ws-api/init                   tasks/init})
  (let [lr-instance (lr/create-instance)]
    (registry/register-algorithm (alg/algorithm-key lr-instance) lr-instance))
  (kp/init! tasks/miss-fn)
  (kp/train-init! tasks/training-data-lookup))
