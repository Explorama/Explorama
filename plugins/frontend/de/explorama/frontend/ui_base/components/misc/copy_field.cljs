(ns de.explorama.frontend.ui-base.components.misc.copy-field
  (:require [de.explorama.frontend.ui-base.components.common.error-boundary :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.formular.input-group :refer [input-group]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [reagent.core :as r]
            [taoensso.timbre :refer-macros [debug]]))

(def parameter-definition
  {:copy-value {:type [:string :derefable]
                :required true
                :desc "The value that should be copied to the clipboard."}
   :input-field-label {:type [:string :derefable]
                       :desc "Label shown before the input-field."}
   :input-field-extra-props {:type :map
                             :desc "Extra Properties that should be set for the input-field. The props :id, :label, :parent-container, :on-change, :value will be overriden."}
   :copy-aria-label {:type [:string :derefable :keyword]
                     :desc "A aria label for the copy button"}
   :clear-aria-label {:type [:string :derefable :keyword]
                      :desc "The aria-label for the clear button"}
   :aria-label {:type [:string :derefable :keyword]
                :required true
                :desc "The aria-label for the clear button"}
   :success-icon {:type :keyword
                  :desc "The button icon that will be visible when the copy action was a success."}
   :failed-icon {:type :keyword
                 :desc "The button icon that will be visible when the copy action failed."}
   :normal-icon {:type :keyword
                 :desc "The button icon that will be visible when nothing is done or after copy was done."}
   :success-icon-params {:type :map
                         :desc "Additional icon-params that will be used for the success-icon."}
   :failed-icon-params {:type :map
                        :desc "Additional icon-params that will be used for the failed-icon."}
   :normal-icon-params {:type :map
                        :desc "Additional icon-params that will be used for the normal-icon."}
   :back-to-normal-delay {:type :integer
                          :desc "Delay defined in ms after which the icon should change back to normal."}
   :on-success {:type :function
                :desc "Function that will be called when the copy was successfull."}
   :on-failed {:type :function
               :desc "Function that will be called with the exception as param why the copy failed."}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {;:copied-message "Copied"
                         ;:failed-message "Not copied"
                         ;:show-tooltip? false
                         :success-icon :check
                         :copy-aria-label :aria-copy
                         :clear-aria-label :aria-clear
                         :success-icon-params {:color :green
                                               :brightness 2}
                         :failed-icon :cross
                         :failed-icon-params {:color :red}
                         :normal-icon :w-multiple
                         :back-to-normal-delay 6000})

(defn- copy-to-clipboard [input-id success-callback failed-callback]
  (when-let [input-elem (js/document.getElementById input-id)]
    (try
      (when input-elem
        (.select input-elem)
        (.setSelectionRange input-elem 0 99999); /*For mobile devices*/
        (.execCommand js/document "copy")
        (success-callback))
      (catch
       :default e
        (debug "copy-to-clipboard failed - Exception: " e)
        (failed-callback e)))))

(defn ^:export copy-field [{:keys [on-success
                                   on-failure
                                   back-to-normal-delay]
                            :or {back-to-normal-delay (:back-to-normal-delay default-parameters)}
                            :as params}]
  (let [input-id (str (random-uuid))
        delay-to-normal (r/atom nil)
        copied-success? (r/atom false)
        copied-failed? (r/atom false)
        delay-to-normal-fn (fn []
                             (when @delay-to-normal
                               (js/clearTimeout @delay-to-normal))
                             (reset! delay-to-normal
                                     (js/setTimeout (fn []
                                                      (reset! delay-to-normal nil)
                                                      (reset! copied-success? false)
                                                      (reset! copied-failed? false))
                                                    back-to-normal-delay)))
        success-callback (fn []
                           (when (fn? on-success)
                             (on-success))
                           (reset! copied-success? true)
                           (reset! copied-failed? false)
                           (delay-to-normal-fn))
        failed-callback (fn []
                          (when (fn? on-failure)
                            (on-failure))
                          (reset! copied-success? false)
                          (reset! copied-failed? true))]
    (fn [params]
      (let [params (merge default-parameters params)]
        [error-boundary {:validate-fn #(validate "copy-field" specification params)}
         (let [{:keys [copy-value aria-label
                       input-field-extra-props
                       clear-aria-label
                       copy-aria-label copied-message
                       failed-message show-tooltip?
                       success-icon failed-icon normal-icon
                       success-icon-params failed-icon-params normal-icon-params]} (merge default-parameters params)
               copy-value (val-or-deref copy-value)
               #_#_copied-message (val-or-deref copied-message)
               #_#_failed-message (val-or-deref failed-message)
               #_#_show-tooltip? (val-or-deref show-tooltip?)]
           [input-group
            {:items [{:type :input
                      :id "1"
                      :component-props (assoc input-field-extra-props
                                              :id input-id
                                              :aria-label aria-label
                                              :clear-aria-label clear-aria-label
                                              :on-change #(str copy-value)
                                              :value copy-value)}
                     {:type :button
                      :id "2"
                      :component-props {:variant :secondary
                                        :start-icon (cond
                                                      @copied-success? success-icon
                                                      @copied-failed? failed-icon
                                                      :else normal-icon)
                                        :icon-params (or (cond
                                                           @copied-success? success-icon-params
                                                           @copied-failed? failed-icon-params
                                                           :else normal-icon-params)
                                                         {})
                                        :on-click #(copy-to-clipboard input-id
                                                                      success-callback
                                                                      failed-callback)
                                        :aria-label copy-aria-label}}]}])]))))