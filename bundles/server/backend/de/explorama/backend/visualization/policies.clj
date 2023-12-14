(ns de.explorama.backend.visualization.policies)

(def standard-rights {:visualization.core/load-visualization {:user-attributes {:role ["admin" "domain-expert"]}
                                                              :data-attributes {}
                                                              :id :visualization.core/load-visualization
                                                              :name "Visualization"}
                      :rights-and-roles {:user-attributes {:role ["admin"]}
                                         :data-attributes {}
                                         :id :rights-and-roles
                                         :name "visualization-rights-and-roles"}})
