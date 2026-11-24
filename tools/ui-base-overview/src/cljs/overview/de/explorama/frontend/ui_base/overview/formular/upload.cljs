(ns de.explorama.frontend.ui-base.overview.formular.upload
  (:require [de.explorama.frontend.ui-base.components.formular.upload :refer [upload default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]
            [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
            [re-frame.core :as re-frame]))

(defcomponent
  {:name "Upload"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [upload]"
   :desc [:<> "Component for uploading files into the client. Uses the standard "
          [:a {:href "https://developer.mozilla.org/en-US/docs/Web/API/FileReader/result"
               :target "_blank"}
           "FileReader API"]]
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [upload {:on-file-loaded (fn [result file-infos] (js/console.log "File loaded " (:name file-infos) (:size file-infos) result))
           :on-complete (fn [infos] (js/console.log "Done :) Infos:" infos))
           :multi-files? true
           :local-read-as :string}]
  {:title "Text File/s upload (local)"})

(re-frame/reg-event-db
 :uploaded-file
 (fn [db [_ new-value]]
   (assoc db :uploaded-file new-value)))

(re-frame/reg-sub
 :uploaded-file
 (fn [db]
   (get db :uploaded-file)))

(defn loaded-comp []
  (when-let [loaded-file @(re-frame/subscribe [:uploaded-file])]
    [:div
     "loaded: "
     (:name loaded-file)
     (str " ("
          (:size loaded-file)
          " bytes"
          ")   ")
     [button {:label "X"
              :on-click #(re-frame/dispatch [:uploaded-file nil])}]]))

(defn upload-comp []
  (let [loaded-file @(re-frame/subscribe [:uploaded-file])]
    (when-not loaded-file
      [upload {:upload-button-params {:label "Upload Mapping"
                                      :variant :secondary}
               :on-file-loaded (fn [result metas]
                                 (js/console.log "loaded - result:" result metas)
                                 (re-frame/dispatch [:uploaded-file metas]))
               :variant :button
               :file-type [".edn"]}])))

(defexample
  [:<>
   [loaded-comp]
   [upload-comp]]

  {:title "Upload Button (local)"
   :code-before "
(re-frame/reg-event-db
 :uploaded-file
 (fn [db [_ new-value]]
   (assoc db :uploaded-file new-value)))

(re-frame/reg-sub
 :uploaded-file
 (fn [db]
   (get db :uploaded-file)))

(defn loaded-comp []
 (when-let [loaded-file @(re-frame/subscribe [:uploaded-file])]
    [:div
     \"loaded: \"
     (:name loaded-file)
     (str \" (\"
          (:size loaded-file)
          \" bytes\"
          \")   \")
     [button {:label \"X\"
              :on-click #(re-frame/dispatch [:uploaded-file nil])}]]))

(defn upload-comp []
 (let [loaded-file @(re-frame/subscribe [:uploaded-file])]
  (when-not loaded-file
    [upload {:upload-button-params {:label \"Upload Mapping\"
                                    :variant :secondary}
             :on-file-loaded (fn [result metas]
                               (js/console.log \"loaded - result:\" result metas)
                               (re-frame/dispatch [:uploaded-file metas]))
             :variant :button
             :file-type [\".edn\"]
             :local-read-as :clj}])))"
   :desc "Variant as it works in data-provisioning for uploading an mapping file (edn)"})

(defexample
  [upload {:on-file-loaded #(do)
           :on-max-size-error (fn [error-infos]
                                (js/console.log "error-infos" error-infos)
                                (js/alert "Upload aborted!"))
           :max-file-size {"csv" (* 200 1024 1024)
                           "json" (* 10 1024 1024)
                           "xlsx" (* 10 1024 1024)}
           :file-type [".csv" ".json" ".xlsx"]}]

  {:title "Upload area with size limits"
   :desc "Variant as it works in data-provisioning for uploading an csv, json or xlsx with size limits (without progress)"})

(defexample
  [upload {:variant :button
           :on-file-loaded #(do)
           :on-max-size-error (fn [error-infos]
                                (js/console.log "error-infos" error-infos)
                                (js/alert "Upload aborted!"))
           :max-file-size {"csv" (* 200 1024 1024)
                           "json" (* 10 1024 1024)
                           "xlsx" (* 10 1024 1024)}
           :file-type [".csv" ".json" ".xlsx"]}]

  {:title "Upload button with size limits"
   :desc "Same like \"Upload area with size limits\" example with a button"})
