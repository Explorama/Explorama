(ns de.explorama.frontend.woco.configs.core
  (:require [de.explorama.frontend.ui-base.utils.select :as select-util]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.configs.paths :as cpath]
            [de.explorama.shared.woco.ws-api :as ws-api]))

(re-frame/reg-event-db
 ws-api/roles-and-users-result
 (fn [db [_ {:keys [roles users]}]]
   (-> db
       (assoc-in cpath/possible-users (mapv (fn [{:keys [name username mail]}]
                                              {:label name
                                               :value username
                                               :mail mail})
                                            users))
       (assoc-in cpath/possible-roles (select-util/vals->options roles)))))