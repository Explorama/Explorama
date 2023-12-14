(ns de.explorama.backend.indicator.policies)

(def standard-rights {:de.explorama.backend.indicator.events/load-indicator {:user-attributes {:role ["admin" "data-scientist" "domain-expert"]}
                                                        :data-attributes {},
                                                        :id :de.explorama.backend.indicator.events/load-indicator,
                                                        :name "indicator-name"}
                      :rights-and-roles {:user-attributes {:role ["admin"]}
                                         :data-attributes {}
                                         :id :rights-and-roles
                                         :name "indicator-rights-and-roles"}})
