(ns de.explorama.frontend.woco.preferences.client
  (:require [taoensso.timbre :refer [error]]
            [de.explorama.frontend.woco.path :as path]
            [re-frame.core :refer [reg-event-fx reg-sub]]))

(def ^:private local-storage js/window.localStorage)

(defn- local-pref-key [pref-key]
  (str "explorama" pref-key))

(defn- set-item [key val]
  (.setItem local-storage key val))

(defn set-preference
  "Saves the given preference.
   The result should be either a fx-compatible map or nil."
  [username pref-key pref-val]
  (let [pref-key (local-pref-key pref-key)]
    (set-item pref-key pref-val))
  nil)

(defn get-preference
  [username pref-key default-val]
  (let [pref-key (local-pref-key pref-key)
        val (.getItem local-storage pref-key)]
    (if (and (nil? val) default-val)
      (do
        (set-item pref-key default-val)
        default-val)
      val)))

(defonce ^:private storage-watcher (atom {}))

(defn add-storage-watcher [key-to-watch changed-fn]
  (swap! storage-watcher assoc (local-pref-key key-to-watch) changed-fn))

(defn remove-storage-watcher [key-to-watch]
  (swap! storage-watcher dissoc (local-pref-key key-to-watch)))

(reg-event-fx
 ::set-preference
 (fn [{db :db}
      [_ pref-key pref-val]]
   (let [{:keys [username]} (get-in db path/user-info)]
     (set-preference username pref-key pref-val))))

(defn get-preferences-db [db pref-key default-val]
  (let [{:keys [username]} (get-in db path/user-info)]
    (get-preference username pref-key default-val)))

(reg-sub
 ::get-preference
 (fn [db [_ pref-key default-val]]
   (get-preferences-db db pref-key default-val)))

(reg-event-fx
 ::get-preference
 (fn [{db :db} [_ pref-key default-val callback-vec]]
   (if (vector? callback-vec)
     (let [{:keys [username]} (get-in db path/user-info)
           pref-val (get-preference username pref-key default-val)]
       {:dispatch (conj callback-vec pref-val)})
     (do
       (error "get-preference callback-vec is not valid" {:pref-key pref-key
                                                          :callback-vec callback-vec})
       nil))))

(.addEventListener js/window "storage"
                   (fn [e]
                     (let [changed-key (aget e "key")
                           old-val (aget e "oldValue")
                           new-val (aget e "newValue")
                           watcher-fn (get @storage-watcher changed-key)]
                       (when watcher-fn
                         (watcher-fn changed-key old-val new-val)))))

