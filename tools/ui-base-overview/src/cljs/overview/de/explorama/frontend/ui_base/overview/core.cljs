(ns de.explorama.frontend.ui-base.overview.core
  (:require [reagent.dom :as reagent]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.ui-base.core]
            [de.explorama.frontend.ui-base.overview.page]
            [de.explorama.frontend.ui-base.overview.renderer.page :as page]
            [de.explorama.frontend.ui-base.overview.formular.core]
            [de.explorama.frontend.ui-base.overview.frames.core]
            [de.explorama.frontend.ui-base.overview.misc.core]
            [de.explorama.frontend.ui-base.overview.common.core]
            [de.explorama.frontend.ui-base.overview.utils.core]
            [de.explorama.frontend.ui-base.utils.subs :refer [set-translation-fn]]
            [de.explorama.frontend.ui-base.utils.specification :refer [enable-validation]])
  (:require-macros [de.explorama.frontend.ui-base.overview.page]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   {}))

(defn dev-setup []
  (enable-console-print!)
  (println "dev mode"))

(defn ^:export before-load [changed-files]
  (page/reset-components changed-files))

(defn ^:export mount-root []
  (re-frame/clear-subscription-cache!)
  (enable-validation)
  (set-translation-fn #(atom (name %)))
  (reagent/render [page/overview]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::initialize-db])
  (dev-setup)
  (mount-root))