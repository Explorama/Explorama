(ns de.explorama.backend.configuration.policies)

(def standard-rights {:configuration.core/load-data-management
                      {:user-attributes {:role ["admin"]}
                       :data-attributes {}
                       :id              :configuration.core/load-data-management
                       :name            "data-management-label"}
                      :rights-and-roles        {:user-attributes {:role ["admin"]}
                                                :data-attributes {}
                                                :id              :rights-and-roles
                                                :name            "data-management-rights-and-roles"}})
