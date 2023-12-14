(ns de.explorama.backend.configuration.user-configs
  (:require [de.explorama.backend.configuration.config :as config]))

(defn direct-search [] {:group-1
                        {:name :direct-search-selection
                         :drop-downs
                         {:list-type
                          {:name :direct-search-filter-type
                           :options [{:display-name "direct-search-whitelist-type"
                                      :name "Whitelist"}
                                     {:display-name "direct-search-blacklist-type"
                                      :name "Blacklist"}]
                           :selected "Whitelist"
                           :multi false}
                          :attributes
                          {:name :direct-search-attributes
                           :options []
                           :selected config/explorama-direct-search-attributes
                           :multi true}}}})

(defn initialize [{:keys [client-callback]} _]
  (client-callback [:Directsearch (direct-search)]))
