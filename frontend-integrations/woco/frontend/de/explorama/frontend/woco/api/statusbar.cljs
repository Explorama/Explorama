(ns de.explorama.frontend.woco.api.statusbar
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [reagent.ratom :as ratom]))

(spec/def ::subscription-vector
  (spec/cat :subscription-key keyword?
            :args (spec/* (constantly true))))

(spec/def ::status-name-sub ::subscription-vector)

(spec/def ::status-message-sub ::subscription-vector)

(spec/def ::status
  (spec/keys :req-un [::status-name-sub ::status-message-sub]))

(re-frame/reg-event-fx
 ::reg-status
 (fn [{:keys [db]} [_ id status]]
   (if (spec/valid? ::status status)
     {:db (assoc-in db [::status id] status)}
     {:log/error (str "Invalid statusbar message.  ID: "
                      id
                      ", status: "
                      status)})))

(re-frame/reg-event-db
 ::del-status
 (fn [db [_ id]]
   (update db ::status dissoc id)))

(defn get-status-infos [db]
  (let [status-infos (get db ::status)]
    (if (seq status-infos)
      status-infos
      {:unsaved {:status-name-sub [:de.explorama.frontend.common.i18n/translate :unsaved-workspace]}})))

(re-frame/reg-sub
 ::status-info
 (fn [db _]
   (get-status-infos db)))

(re-frame/reg-sub
 ::status-display
 (fn [_]
   (ratom/make-reaction
    (fn []
      (let [status-info @(re-frame/subscribe [::status-info])]
        (mapv (fn [[_ {name-sub :status-name-sub
                       message-sub :status-message-sub
                       :as info}]]
                [(when name-sub @(re-frame/subscribe name-sub))
                 (when message-sub @(re-frame/subscribe message-sub))])
              status-info)))))
 (fn [parts _]
   (string/join ", "
                (map (fn [info]
                       (string/join ": "
                                    (filter identity info)))
                     parts))))
