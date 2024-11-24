(ns de.explorama.backend.woco.user-preferences
  (:require [de.explorama.backend.expdb.middleware.db :as mdb]))

(def ^:private bucket "user-preferences")

(defn get-user-preferences [{:keys [client-callback]} [_user-info]]
  (client-callback (mdb/get+ bucket)))

(defn save-user-preference [{:keys [client-callback]} [_user-info pref-key pref-val]]
  (mdb/set bucket pref-key pref-val)
  (client-callback pref-key (mdb/get bucket pref-key)))