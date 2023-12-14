(ns de.explorama.backend.configuration.config
  (:require [de.explorama.shared.common.configs.provider :as configp :refer [defconfig]]))

(def plugin-key :configuration)
(def plugin-string (name plugin-key))

(def explorama-direct-search-attributes
  (defconfig
    {:env :explorama-direct-search-attributes
     :type :edn-string
     :default ["country" "year" "organisation" "datasource" "event-type"]
     :doc "Defines what attributes are available for direct-search."}))

(def explorama-lang-files
  (defconfig
    {:env :explorama-lang-files
     :type :string
     :default "resources/langfiles"
     :doc "Folder for holding the resources."}))

(def explorama-month-names
  (defconfig
    {:env :explorama-month-names
     :default  {1 {:de-DE "Januar", :en-GB "January"},
                2 {:de-DE "Februar", :en-GB "February"},
                3 {:de-DE "März", :en-GB "March"},
                4 {:de-DE "April", :en-GB "April"},
                5 {:de-DE "Mai", :en-GB "May"},
                6 {:de-DE "Juni", :en-GB "June"},
                7 {:de-DE "Juli", :en-GB "July"},
                8 {:de-DE "August", :en-GB "August"},
                9 {:de-DE "September", :en-GB "September"},
                10 {:de-DE "Oktober", :en-GB "October"},
                11 {:de-DE "November", :en-GB "November"},
                12 {:de-DE "Dezember", :en-GB "December"}}
     :type :edn-string
     :doc "Localized texts for the months"}))
  