(ns de.explorama.frontend.search.direct-search
  (:require [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
 ::show-all
 (fn [_ [_ row-name results]]
   {:dispatch [:de.explorama.frontend.search.api.core/open-with-row row-name results false]}))