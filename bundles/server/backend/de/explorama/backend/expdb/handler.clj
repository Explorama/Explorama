(ns de.explorama.backend.expdb.handler
  (:require [clojure.string :as str]
            [de.explorama.backend.environment.probe.core :as probe]
            [de.explorama.backend.expdb.buckets :as buckets]
            [de.explorama.backend.expdb.config :as config-expdb]
            [de.explorama.backend.expdb.import :as imp]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.api :as search-api]
            [de.explorama.backend.expdb.legacy.search.data-tile :as data-tile]
            [de.explorama.backend.expdb.persistence.indexed :as persistence]
            [de.explorama.backend.expdb.persistence.interceptor :as interc]
            [taoensso.timbre :as log :refer [error]]
            [taoensso.tufte :refer [add-basic-println-handler!]]))

(add-basic-println-handler! {})

(defmacro gen-error [fnc]
  `(try
     {:status 200
      :body ~fnc}
     (catch Throwable e#
       (probe/rate-exception e#)
       (error e#)
       {:status 400
        :body {:msg (.getMessage e#)}})))

(defn apply-to-all-buckets [bucket default-bucket f]
  (try
    (cond bucket
          (f (buckets/new-instance bucket))
          default-bucket
          (f (buckets/new-instance default-bucket))
          :else
          (let [result
                (mapv (fn [[bucket-key]]
                        [bucket-key (f (buckets/new-instance bucket-key))])
                      config-expdb/explorama-bucket-config)]
            {:response-data (into {} result)
             :success (every? (fn [[_ {success :success}]]
                                success)
                              result)}))
    (catch Throwable t
      (log/error t)
      {:success false
       :message "Something went wrong."})))

(defmulti parse-param first)

(defmethod parse-param :default [[_ _]] nil)

(defmethod parse-param "skip-contexts" [[_ value]]
  [:skip-contexts?
   ({"false" false "true" true} value)])

(defmethod parse-param "skip-textcards" [[_ value]]
  [:skip-textcards?
   ({"false" false "true" true} value)])

(defmethod parse-param "graph-consistency-check" [[_ value]]
  [:graph-consistency-check?
   ({"false" false "true" true} value)])

(defmethod parse-param "bucket" [[_ value]]
  [:bucket (if (clojure.string/blank? value)
             :default
             (keyword (clojure.string/trim value)))])

(defmethod parse-param "silent" [[_ value]]
  [:silent
   ({"false" false "true" true} value)])

(defmethod parse-param "force-update" [[_ value]]
  [:force-update
   ({"false" false "true" true} value)])

(defmethod parse-param "field" [[_ value]]
  (if (< 50 (count value))
    (throw (ex-info "too big field name" {:length (count value)}))
    [:field (clojure.string/trim value)]))

(defmethod parse-param "analyse" [[_ value]]
  (try
    [:analyse
     (let [values (str/split value #"\,")]
       {:flat (set (map keyword (filter #(not (str/starts-with? %1 "print(")) values)))
        :print (map (fn [prints]
                      (let [print-struct (subs prints (count "print(") (dec (count prints)))]
                        (str/split print-struct #"\;")))
                    (filter #(str/starts-with? %1 "print(") values))})]
    (catch Throwable e
      (log/error e))))

(defn- default-params [params]
  (merge {:default-bucket :default}
         params))

(def routes
  [["/datasources" {:post (fn [req]
                            (gen-error
                             (let [body (get req :body-params)
                                   {:keys [bucket default-bucket] :as options} (default-params
                                                                                (get req :body-params {}))
                                   bucket (or bucket default-bucket)
                                   {:keys [success data]}
                                   (imp/transform->import body options bucket)]
                               {:status (if success 200 400)
                                :body {:result data}})))
                    :get (fn [context]
                           (gen-error
                            (let [{:keys [bucket] :as opts} (default-params
                                                             (get context :body-params {}))]
                              (apply-to-all-buckets
                               bucket
                               nil ; this will do datasources for all buckets if no bucket is provided
                               (fn [instance]
                                 (persistence/data-sources instance opts))))))
                    :delete (fn [context]
                              (gen-error
                               (let [{{query-params :query} :parameters} context
                                     {:keys [bucket default-bucket all?] :as qp} (default-params query-params)]
                                 (cond all?
                                       (apply-to-all-buckets
                                        bucket
                                        default-bucket
                                        (fn [instance]
                                          (interc/delete-all instance)))
                                       (and bucket
                                            (seq (select-keys (:dims (get config-expdb/explorama-bucket-config (keyword bucket)))
                                                              qp)))
                                       (interc/delete (buckets/new-instance bucket)
                                                      {:body-params (select-keys (:dims (get config-expdb/explorama-bucket-config (keyword bucket)))
                                                                                 qp)
                                                       :bucket bucket})))))}]
   ["/db" ["/data-tiles" {:get (fn [context]
                                 (let [{:keys [data-tiles]} (get context :body-params)]
                                   (gen-error
                                    (reduce (fn [acc [bucket tiles]]
                                              (into acc
                                                    (persistence/data-tiles (buckets/new-instance bucket) tiles)))
                                            []
                                            (group-by #(get % "bucket") data-tiles)))))}]]
   ["/api" ["/check" {:get (fn handler [_] {:status 200, :body "ok"})
                      :name ::ping}]]
   ["/search" [["/attributes" {:get (fn [context]
                                      (let [body (get context :body-params)]
                                        (gen-error
                                         (search-api/attributes body))))}]
               ["/attribute-types" {:get (fn [context]
                                            (let [body (get context :body-params)]
                                              (gen-error
                                               (search-api/attribute-types body))))}]
               ["/attribute-values" {:get (fn [context]
                                             (log/debug "/attribute-values" context)
                                             (let [body (get context :body-params)]
                                               (log/debug "/attribute-values" body)
                                               (gen-error
                                                (search-api/attribute-values body))))}]
               ["/data-tiles" {:get (fn [context]
                                      (let [{:keys [formdata]} (get context :body-params)]
                                        (gen-error
                                         (data-tile/get-data-tiles formdata))))}]
               ["/neighborhood" {:get (fn [context]
                                        (let [body (get context :body-params)]
                                          (gen-error
                                           (search-api/neighborhood body))))}]]]
   ["/probe"
    [["/liveness" {:get (fn [req]
                          (if (probe/liveness)
                            {:status 200}
                            {:status 500}))}]
     ["/readiness" {:get (fn [req]
                           (if (probe/readiness)
                             {:status 200}
                             {:status 500}))}]]]])
#_#_:patch {:responses {} ;datasources
            :handler
            (fn [context]
              (log/debug context)
              (telemetry/with-new-span ["patch-datasource" config/tracer]
                (try
                  (let [body (get-in context [:request :body])
                        {:keys [bucket default-bucket field] :as options} (get-options context)
                        bucket (or bucket default-bucket)
                        data (json/read-value (slurp body))
                        {:keys [success response-data]}
                        (apply-to-all-buckets
                         bucket
                         default-bucket
                         (fn [instance]
                           (persistence/patch instance bucket field data)))]
                    {:status (if success 200 400)
                     :response-data response-data})
                  (catch Throwable e
                    (log/error e)
                    {:status 500
                     :response-data (.getMessage e)}))))}

#_#_"/datasource" ;s/:id
  {:delete {:responses {}
            :handler
            (fn [context]
              (log/debug context)
              (telemetry/with-new-span ["delete-datasource" config/tracer]
                (let [datasource-global-id ""; TODO
                      {:keys [bucket silent default-bucket] :as options} (get-options context)
                      {:keys [success name] :as response-data}
                      (apply-to-all-buckets
                       bucket
                       default-bucket
                       (fn [instance]
                         (persistence/delete instance datasource-global-id options)))]
                  (delete-return bucket default-bucket success silent response-data name))))}}
