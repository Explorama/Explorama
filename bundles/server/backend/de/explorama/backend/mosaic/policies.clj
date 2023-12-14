(ns de.explorama.backend.mosaic.policies)

(def standard-rights {:de.explorama.backend.mosaic.core/load-mosaic {:user-attributes {:role ["admin" "data-scientist"]},
                                              :data-attributes {},
                                              :id :de.explorama.backend.mosaic.core/load-mosaic,
                                              :name "mosaic"}
                      :rights-and-roles {:user-attributes {:role ["admin"]}
                                         :data-attributes {},
                                         :id :rights-and-roles
                                         :name "mosaic-rights-and-roles"}})
