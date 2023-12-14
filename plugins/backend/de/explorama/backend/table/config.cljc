(ns de.explorama.backend.table.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def explorama-table-max-data-amount
  (defconfig
    {:env :explorama-table-max-data-amount
     :default 2000000
     :type :integer
     :doc "How much data can be visualized in the Client in one window.
         If the number of Events is reached, it will show a Message to the user and no data will be visualized."}))


(def explorama-table-stop-filterview-amount
  (defconfig
    {:env :explorama-table-stop-filterview-amount
     :default 200000
     :type :integer
     :doc "This defines how much data can be filtered with the local-filter in each frame.
         If the number of events is larger than this, only a Message is shown when the user tries to use the filtering."}))


(def explorama-table-warn-filterview-amount
  (defconfig
    {:env :explorama-table-warn-filterview-amount
     :default 50000
     :type :integer
     :doc "This defines at what point a warning message should be shown to the user when he tries to use the local-filter."}))
