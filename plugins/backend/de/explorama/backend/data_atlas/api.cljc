(ns de.explorama.backend.data-atlas.api
  (:require [de.explorama.backend.common.data.descriptions :as desc]
            [de.explorama.backend.data-atlas.attribute-characteristics :as acs]
            [de.explorama.backend.data-atlas.match :as match]
            [taoensso.timbre :refer [debug error]]))

(defn get-data-elements [{:keys [client-callback]}
                         [user-info frame-id selection query language]]
  (let [_ (debug "ALL" frame-id selection query language)
        query (match/normalize-query query)
        {attribute-types :attribute-types :as data-elements} (acs/get-current-values user-info selection query)
        data-descriptions (desc/describe-selection selection data-elements attribute-types language)]
    (client-callback data-elements data-descriptions)))
