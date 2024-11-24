(ns de.explorama.frontend.woco.screenshot.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [error]]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.screenshot.util :refer [make-screenshot- export-pixel-ratio]]
            [de.explorama.frontend.woco.screenshot.pdf :refer [make-screenshot-pdf]]
            [de.explorama.frontend.woco.screenshot.png :refer [make-screenshot-with-details]]))

(defn make-frame-screenshot
  ([params]
   (make-frame-screenshot params make-screenshot-))
  ([{:keys [frame-id callback-fn]
     :as params}
    follow-up-fn]
   (try
     (follow-up-fn (assoc (or params {})
                          :dom-id (config/frame-dom-id frame-id)
                          :screenshot-params {:style {:transform ""
                                                      :left 0
                                                      :top 0
                                                      :zIndex -1000}
                                              :quality 1.0
                                              :cacheBust true
                                              :pixelRatio export-pixel-ratio}
                          :callback-fn (fn [r]
                                         (when (fn? callback-fn)
                                           (callback-fn r params)))))
     (catch :default e
       (error "Failed to make screenshot from frame" frame-id e)))))

(defn make-screenshot [{:keys [type dom-id frame-id add-export-details?] :as params}]
  (cond
    (and (= type :pdf)
         dom-id)
    (make-screenshot-pdf params)
    (and (= type :pdf)
         frame-id)
    (make-frame-screenshot params make-screenshot-pdf)
    (and add-export-details? frame-id)
    (make-frame-screenshot params make-screenshot-with-details)
    (and add-export-details? dom-id)
    (make-screenshot-with-details params)
    dom-id
    (make-screenshot- params)
    frame-id
    (make-frame-screenshot params make-screenshot-)
    :else (make-screenshot- params)))

(re-frame/reg-event-fx
 ::export-tab
 (fn [{db :db} [_ event]]
   (let [png-label (i18n/translate db :png-export)]
     {:dispatch (fi/call-api :context-menu-event-vec
                             event
                             {:event [:de.explorama.frontend.woco.page/screenshot-later]
                              :fix-top 2
                              :options [{:label png-label
                                         :type :png
                                         :icon :image}
                                        {:label "PDF"
                                         :type :pdf
                                         :icon :file-empty}]}
                             nil)})))
