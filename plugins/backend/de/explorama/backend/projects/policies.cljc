(ns de.explorama.backend.projects.policies)

(def standard-rights {:de.explorama.backend.projects.direct-search/project {:user-attributes {:role ["admin" "data-scientist" "domain-expert"]}
                                                                    :data-attributes {}
                                                                    :id :de.explorama.backend.projects.direct-search/project
                                                                    :name "Directsearch: Projects"}
                      :rights-and-roles {:user-attributes {:role ["admin"]}
                                         :data-attributes {}
                                         :id :rights-and-roles
                                         :name "projects-rights-and-roles"}})