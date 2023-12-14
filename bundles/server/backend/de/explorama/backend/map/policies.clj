(ns de.explorama.backend.map.policies)

(def standard-rights {:de.explorama.backend.map.core/load-map {:user-attributes {:role ["admin" "domain-expert"]},
                                                       :data-attributes {},
                                                       :id              :de.explorama.backend.map.core/load-map,
                                                       :name            "map"}
                      :rights-and-roles        {:user-attributes {:role ["admin"]}
                                                :data-attributes {}
                                                :id              :rights-and-roles
                                                :name            "map-rights-and-roles"}})
