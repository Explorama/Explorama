(ns de.explorama.backend.algorithms.policies)

(def standard-rights {:de.explorama.backend.algorithms.core/load-ki {:user-attributes {:role ["admin" "data-scientist" "domain-expert"]}
                                        :data-attributes {}
                                        :id :de.explorama.backend.algorithms.core/load-ki
                                        :name "algorithms"}
                      :rights-and-roles {:user-attributes {:role ["admin"]}
                                         :data-attributes {}
                                         :id :rights-and-roles
                                         :name "ki-rights-and-roles"}})