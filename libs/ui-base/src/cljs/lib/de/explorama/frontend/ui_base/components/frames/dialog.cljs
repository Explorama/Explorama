(ns de.explorama.frontend.ui-base.components.frames.dialog
  (:require [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
            [de.explorama.frontend.ui-base.components.formular.checkbox :refer [checkbox]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:show? {:type [:boolean :derefable]
           :required true
           :desc "If true the dialog will be shown; Can be a boolean or derefable like an atom or re-frame subscription"}
   :type {:type :keyword
          :characteristics [:message :prompt :warning :container]
          :desc "The type changes the appearance of the dialog. :container will ignore all buttons and is mostly used for other UI-base elements."}
   :compact? {:type :boolean
              :desc "Reduce the padding; mostly used for small :container dialogs"}
   :extra-class {:type [:vector :string]
                 :desc "Additional classes for the parent container."}
   :full-size? {:type :boolean
                :desc "If true will the dialog will cover most of the window. false by default."}
   :hide-fn {:type :function
             :required true
             :desc "The function will be called after clicking each of the buttons. It should result with the show? property being set to false"}
   :title {:type [:string :derefable]
           :desc "The title of the dialog. Will be put in the dialog header. Can be a string or derefable like an atom or re-frame subscription"}
   :message {:type [:string :derefable :component]
             :desc "The dialog's message. Can be a string or derefable like an atom or re-frame subscription"}
   :details {:type [:string :derefable :component]
             :desc "The second line of the dialog's message. Can be a string or derefable like an atom or re-frame subscription"}
   :ok {:type [:map :boolean]
        :desc "Configuration for the :ok button. If true or {}, defaults will be used. If no other buttons are defined this one will be used."}
   :yes {:type [:map :boolean]
         :desc "Configuration for the :yes button. If true or {}, defaults will be used"}
   :no {:type [:map :boolean]
        :desc "Configuration for the :no button. If true or {}, defaults will be used"}
   :cancel {:type [:map :boolean]
            :desc "Configuration for the :cancel button. If true or {}, defaults will be used"}
   :checkbox {:type [:map :boolean]
              :desc "Configuration for a checkbox shown behind all buttons. If true or {}, defaults will be used"}
   :id {:type [:string :derefable]
        :desc "Set the id for the dialog frame."}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:type :message
                         :message ""})
(def button-defaults {:ok {:label "OK"}
                      :yes {:label "Yes"}
                      :no {:label "No"}
                      :cancel {:label "Cancel"}
                      :checkbox {:label "Remember decision?"}})
(def formular-element-order [:ok :yes :no :cancel :checkbox])
(def default-buttons [:ok])

(def overlay-class "overlay")
(def dialog-base-class "dialog")
(def dialog-compact-class "dialog-compact")
(def dialog-header-class "dialog-header")
(def dialog-body-class "dialog-body")
(def dialog-footer-class "dialog-footer")
(def dialog-message-class "dialog-message")
(def dialog-prompt-class "dialog-prompt")
(def dialog-warning-class "dialog-warning")
(def dialog-full-width-class "dialog-full-width")
(def dialog-full-height-class "dialog-full-height")

(defn- dialog-comp [params]
  (let [{:keys [show? hide-fn title full-size?
                type message details compact?
                id extra-class]}
        params
        show? (val-or-deref show?)
        title (val-or-deref title)
        message (val-or-deref message)
        details (val-or-deref details)
        id (val-or-deref id)]
    (if-not show?
      [:<>]
      [:div {:class overlay-class}
       [:div {:id id
              :class (cond-> [dialog-base-class]
                       title
                       (conj (case type
                               :message dialog-message-class
                               :prompt dialog-prompt-class
                               :warning dialog-warning-class
                               ""))
                       compact?
                       (conj dialog-compact-class)
                       full-size?
                       (conj dialog-full-height-class
                             dialog-full-width-class)
                       (vector? extra-class)
                       (concat extra-class)
                       (string? extra-class)
                       (conj extra-class))}
        (when title
          [:div {:class dialog-header-class}
           title])
        [:div {:class dialog-body-class :tabIndex 0}
         (if (string? message)
           [:p message
            [:br]
            details]
           message)]
        (when (not= type :container)
          (reduce (fn [footer button-key]
                    (let [button-params (-> (button-defaults button-key)
                                            (merge (if (map? (params button-key))
                                                     (params button-key)
                                                     {})))]
                      (if (= :checkbox button-key)
                        (conj footer [checkbox button-params])
                        (conj footer [button (update button-params :on-click #(if %
                                                                                (juxt hide-fn %)
                                                                                hide-fn))]))))
                  [:div {:class dialog-footer-class}]
                  (or (seq (filter params formular-element-order))
                      default-buttons)))]])))

(defn ^:export dialog [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "dialog" specification params)}
     [dialog-comp params]]))
