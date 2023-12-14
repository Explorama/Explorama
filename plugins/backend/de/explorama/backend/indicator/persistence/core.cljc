(ns de.explorama.backend.indicator.persistence.core
  (:require [de.explorama.backend.indicator.config :as config]
            [de.explorama.backend.common.middleware.cache-invalidate :as cache-invalidate]
            [de.explorama.backend.indicator.data.core :as data]
            [de.explorama.backend.indicator.persistence.store.core :as persistence]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]
            [malli.core :as m]
            [malli.error :as me]
            [taoensso.timbre :refer [error warn]]))

(def ^{:doc "Used to validated that the description given to the api is correct.
             Mainly if all needed and some optional keys are given and from the right type."}
  indicator-desc-spec
  [:map
   [:id [:string {:min 1 :max 255}]]
   [:name [:string {:min 1 :max 255}]]
   [:creator [:string {:min 1}]]
   [:shared-by {:optional true} [:string {:min 1}]]
   [:description {:optional true} [:string {:min 1 :max 255}]]
   [:dis map?]
   [:calculation-desc vector?]])

(defn- validate-indicator-desc
  "If the indicator is valid returns nil.
   Otherwise returning a map containing a status,msg and reason"
  [indicator]
  (when (and config/explorama-indicator-desc-validation
             (not (m/validate indicator-desc-spec
                              indicator)))
    (error "indicator description not valid " (m/explain indicator-desc-spec
                                                         indicator))
    {:status :failed
     :msg :indicator-desc-not-valid
     :data {:indicator indicator
            :reason (me/humanize
                     (m/explain indicator-desc-spec
                                indicator))}}))

(defn- expand-indicator-description [{:keys [username] :as user}
                                     {:keys [id]}]
  (assoc (persistence/read-indicator user id)
         :write-access? true))

(defn- check-access
  "Used to check if the given user is really the creator from the de.explorama.backend.indicator."
  [{:keys [username] :as user} indicator-id]
  (= username
     (:creator
      (persistence/short-indicator-desc user indicator-id))))

(defn all-user-indicators [user]
  (let [all-user-indicators (persistence/list-all-user-indicators user)]
    (mapv #(expand-indicator-description user %)
          all-user-indicators)))

(defn create-new-indicator [{creator :username
                             :as user}
                            {:keys [id name di dis]
                             :as indicator}]
  (let [indicator-validation-result (validate-indicator-desc indicator)]
    (if (nil? indicator-validation-result)
      {:status :success
       :data (persistence/write-indicator indicator)}
      indicator-validation-result)))

(defn share-with-user [current-user share-with-user {:keys [id]
                                                     :as indicator}]
  (let [indicator-validation-result (validate-indicator-desc indicator)
        correct-user? (check-access current-user id)]
    (cond
      (and (nil? indicator-validation-result)
           correct-user?) (let [original-indicator (expand-indicator-description current-user indicator)
                                indicator (assoc original-indicator
                                                 :id (cljc-uuid)
                                                 :creator (:username share-with-user)
                                                 :shared-by (:username current-user))]
                            {:status :success
                             :data (persistence/write-indicator indicator)})
      (and (nil? indicator-validation-result)
           (not correct-user?)) {:status :failed
                                 :msg :sharing-failed-with-user
                                 :data {:user current-user
                                        :id id}}
      :else indicator-validation-result)))

(defn- inform-caches [dirty-tile]
  (cache-invalidate/send-invalidate #{"transparent-data"}
                                    {"identifier" #{"indicator"}
                                     "description" #{(get dirty-tile "description")}}))

(defn update-indicator [user {:keys [id version
                                     name description
                                     creator]
                              :as indicator}]
  (let [indicator-validation-result (validate-indicator-desc indicator)]
    (if (nil? indicator-validation-result)
      (let [original-indicator (persistence/read-indicator user id)
            write? (check-access user id)]
        (cond
          (not write?)
          (do
            (error "User has no rights to update de.explorama.backend.indicator." {:id id
                                                              :user user})
            {:status :failed
             :msg :no-rights-update-infos
             :data {:id id
                    :user user}})
          (not= original-indicator indicator)
          (let [indicator-desc (persistence/read-indicator id)
                remove-data-tile-query (data/data-tile-desc indicator-desc)]
            (inform-caches remove-data-tile-query)
            {:status :success
             :data (persistence/write-indicator indicator)})
          :else
          (do
            (warn "Nothing changed to be updated (name/description/data/calculation).")
            {:status :info
             :msg :nothing-changed
             :data {:id id}})))
      indicator-validation-result)))

(defn delete-indicator [user {:keys [id]}]
  (if (check-access user id)
    {:status :success
     :data (persistence/delete-indicator user id)}
    (do
      (error "The user has no write-access to delete the de.explorama.backend.indicator." {:id id
                                                                      :user user})
      {:status :failed
       :msg :no-rights-to-delete
       :data {:id id
              :user user}})))

(defn read-indicator [indicator-id]
  (persistence/read-indicator indicator-id))
