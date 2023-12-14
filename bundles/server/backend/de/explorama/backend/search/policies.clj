(ns de.explorama.backend.search.policies)

(def standard-rights {:de.explorama.backend.search.core/load-search           {:user-attributes {:role ["admin" "data-scientist" "domain-expert"]}
                                                                       :data-attributes {}
                                                                       :id              :de.explorama.backend.search.core/load-search
                                                                       :name            "search-name"}
                      :de.explorama.backend.search.direct-search/unified      {:user-attributes {:role ["admin" "data-scientist" "domain-expert"]}
                                                                       :data-attributes {}
                                                                       :id              :de.explorama.backend.search.direct-search/unified
                                                                       :name            "direct-search-unified"}
                      :rights-and-roles                 {:user-attributes {:role ["admin"]}
                                                         :data-attributes {}
                                                         :id              :rights-and-roles
                                                         :name            "search-rights-and-roles"}})
