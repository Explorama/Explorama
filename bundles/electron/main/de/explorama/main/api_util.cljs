(ns de.explorama.main.api-util
  (:require [electron :refer [ipcMain]]
            [de.explorama.main.wrapper.window :refer [send-to-window]]
            [cljs.reader :refer [read-string]]
            [taoensso.timbre :refer-macros [info debug error trace]]))

(defn on [event-name handler]
  (.on ipcMain event-name handler))

(defn handle [event-name handler]
  (.handle ipcMain event-name handler))

(defn type->handler [type]
  (case type
    (:listen :async) on
    :invoke handle
    (error "API type not supported" (str type))))

(defn- secure-api-call [api-handler send-fn result-fn & params]
  (try
    (apply result-fn params)
    (catch :default e
      (error e "secure guard prevents from crashing" {:api-handler api-handler
                                                      :params (str params)})
      (send-fn [:failed :unknown-error]))))

(defn api-wrap
  ([api-handler api-result result-fn type]
   (when-let [handler (type->handler type)]
     (let [async? (= type :async)]
       (handler api-handler
                (fn [e params-str]
                  (trace "api call"
                         (str {:api api-handler
                               :callback-api api-result
                               :params params-str}))
                  (when (fn? result-fn)
                    (let [[callback-vec :as params] (read-string params-str)
                          send-fn (fn [result]
                                    (when result
                                      (send-to-window e
                                                      api-result
                                                      (str [callback-vec result]))))
                          result-fn (partial secure-api-call api-handler send-fn result-fn)

                          result (if async?
                                   (apply (partial result-fn {:callback-fn send-fn})
                                          (rest params))
                                   (apply result-fn (rest params)))]
                      (when-not async?
                        (send-fn result)))))))))

  ([api-handler api-result result-fn]
   (api-wrap api-handler api-result result-fn :listen)))