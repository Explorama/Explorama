(ns de.explorama.frontend.mosaic.data-instances
  (:require [de.explorama.frontend.mosaic.path :as gp]
            [re-frame.core :as re-frame]))

(defn data-instance [db frame-id-or-path]
  (get-in db (gp/data-instance frame-id-or-path)))

(re-frame/reg-sub
 ::data-instance
 (fn [db [_ path]]
   (data-instance db path)))
