(ns de.explorama.shared.common.logging
  (:require #?(:cljs [taoensso.timbre :as timbre :refer-macros [info]]
               :clj [taoensso.timbre :as timbre :refer [info]])
            [clojure.string :refer [join upper-case]]
            [taoensso.encore :as enc :refer [have have?]]))

(defn to-str-output [f vargs err]
  #?(:clj (f (cond-> (join " " (map str vargs))
               err (str "\nException:\n" err)))
     :cljs (f (cond-> (join " " (map #(str (js->clj %)) vargs))
                err (str "\nException:\n" err)))))

(defn to-obj-output [f vargs err]
  #?(:clj (f (cond-> (join " " (map str vargs))
               err (str "\nException:\n" err)))
     :cljs (.apply f js/console (to-array (cond-> vargs
                                            err (-> (vec)
                                                    (conj err)))))))

(def devtools-level-to-fn
  {:fatal   #?(:cljs js/console.error :clj println)
   :error   #?(:cljs js/console.error :clj println)
   :warn    #?(:cljs js/console.warn  :clj println)
   :info    #?(:cljs js/console.info  :clj println)
   :debug   #?(:cljs js/console.debug :clj println)
   :trace   #?(:cljs js/console.trace :clj println)
   :default #?(:cljs js/console.log   :clj println)})

;; Based on timbre/timbre.cljc
(defn- get-timestamp [timestamp-opts instant]
  #?(:clj
     (let [{:keys [pattern locale timezone]} timestamp-opts]
       ;; iso8601 example: 2020-09-14T08:31:17.040Z (UTC)
       (.format ^java.text.SimpleDateFormat (enc/simple-date-format* pattern locale timezone)
                instant))

     :cljs
     (let [{:keys [pattern]} timestamp-opts]
       (if (enc/kw-identical? pattern :iso8601)
         (.toISOString (js/Date. instant)) ; e.g. 2020-09-14T08:29:49.711Z (UTC)
         ;; Pattern can also be be `goog.i18n.DateTimeFormat.Format`, etc.
         (.format
          (goog.i18n.DateTimeFormat. pattern)
          instant)))))

(defn error-fn [{:keys [?err] :as data}]
  (let [err (have ?err)
        nl enc/system-newline]
    (str
     (.-stack err) ; Includes `ex-message`
     (when-let [d (ex-data err)]
       (str nl "ex-data:" nl "    " (pr-str d)))

     (when-let [c (ex-cause err)]
       (str nl nl "Caused by:" nl
            (error-fn
             (assoc data :?err c)))))))

(defonce force-output-fn (atom nil))

(defn appender-template
  "Template for unified logging output
   - f (data): function which will get the data and return a fn (output) which will be triggered"
  [f output-fn]
  {:enabled?   true
   :async?     false
   :min-level  nil
   :rate-limit nil
   :output-fn  nil
   :fn
   (fn [data]
     (let [{:keys [level ?ns-str ?line vargs_ instant ?err]
            {:keys [timestamp-opts]} :config}
           data
           err (when ?err (error-fn data))
           timestamp (get-timestamp timestamp-opts instant)
           vargs (list* (str timestamp " "
                             (upper-case (name level))
                             " [" ?ns-str ":" ?line "] -")
                        (force vargs_))
           output-fn (or @force-output-fn output-fn)]
       (output-fn (f data) vargs err)))})

(def devtools-appender
  "Simple js/console appender which avoids pr-str and uses cljs-devtools
  to format output"
  (appender-template
   (fn [{:keys [level]}]
     (devtools-level-to-fn level (devtools-level-to-fn :default)))
   to-obj-output))

(def println-appender
  (appender-template
   (fn [_] println)
   to-str-output))

(defn ^:export set-log-level [level & {:keys [appenders-conf force-str-output?]
                                       :or {appenders-conf {:console devtools-appender}}}]
  (info "Set log level" level)
  (if force-str-output?
    (reset! force-output-fn to-str-output)
    (reset! force-output-fn nil))
  (timbre/merge-config!
   (cond-> {:min-level (cond-> level
                         (string? level) (keyword))
            :timestamp-opts {:pattern "yyyy-MM-dd HH:mm:ss.S"}}
     (map? appenders-conf)
     (assoc :appenders appenders-conf))))