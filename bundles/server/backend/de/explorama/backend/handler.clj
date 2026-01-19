(ns de.explorama.backend.handler
  (:require [clojure.string :as str]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [de.explorama.backend.common.environment.probe :as probe]
            [de.explorama.backend.frontend-api :as frontend-api]
            [pneumatic-tubes.httpkit :refer [websocket-handler]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.util.response :refer [not-found]]
            [taoensso.timbre :as log]))

(defmacro gen-error [fnc]
  `(try
     {:status 200
      :body ~fnc}
     (catch Throwable e#
       (probe/rate-exception e#)
       (error e#)
       {:status 400
        :body {:msg (.getMessage e#)}})))

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

(defn prepend [s prefix-line]
  (str prefix-line "\n" s))

(defroutes routes
  (GET "/ws" _req
    (if true ; Ignoring token validation for now
      (websocket-handler (frontend-api/routes->tubes))
      {:status 403}))
  (not-found "")
  #_(fn [{{req :query} :parameters :as req-raw}]
      (println "user-info" req req-raw)
      (try
        (let [user-info (user-info-role-fix req)]
          (println "user-info" user-info)
          (if true #_(jwt/user-valid? user-info) ;TODO r1/rights change this for multiuser setups
              (websocket-handler (frontend-api/routes->tubes))
              {:status 403}))
        (catch Throwable e
          (log/error e "Error creating websocket connection")
          {:status 403})))
  #_{:get {:parameters {:query {:username string? :token string? :client-id string? :role string?}}
           :content-type "application/javascript;charset=UTF-8"
           :handler
           (fn [{{req :query} :parameters}]
             (println "user-info" req)
             (try
               (let [user-info (user-info-role-fix req)]
                 (println "user-info" user-info)
                 (if true #_(jwt/user-valid? user-info) ;TODO r1/rights change this for multiuser setups
                     (websocket-handler (frontend-api/routes->tubes))
                     {:status 403}))
               (catch Throwable e
                 (log/error e "Error creating websocket connection")
                 {:status 403})))}}
  #_["/datasources" {:post (fn [req]
                             (gen-error
                              (let [body (get req :body-params)
                                    {:keys [bucket default-bucket] :as options} (default-params
                                                                                 (get req :body-params {}))
                                    bucket (or bucket default-bucket)
                                    {:keys [success data]}
                                    (import/transform->import body options bucket)]
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
                                             (seq (select-keys (:dims (get (configp/get :explorama-bucket-config "de.explorama.backend.handler") (keyword bucket)))
                                                               qp)))
                                        (interc/delete (buckets/new-instance bucket)
                                                       {:body-params (select-keys (:dims (get (configp/get :explorama-bucket-config "de.explorama.backend.handler") (keyword bucket)))
                                                                                  qp)
                                                        :bucket bucket})))))}]
  #_["/api" ["/check" {:get (fn handler [_] {:status 200, :body "ok"})
                       :name ::ping}]]
  #_["/search" [["/attributes" {:get (fn [context]
                                       (let [body (get context :body-params)]
                                         (gen-error
                                          (search-api/attributes body))))}]
                ["/attributes-types" {:get (fn [context]
                                             (let [body (get context :body-params)]
                                               (gen-error
                                                (search-api/attribute-types body))))}]
                ["/attributes-values" {:get (fn [context]
                                              (log/debug "/attributes-values" context)
                                              (let [body (get context :body-params)]
                                                (log/debug "/attributes-values" body)
                                                (gen-error
                                                 (search-api/attributes-values body))))}]
                ["/data-tiles" {:get (fn [context]
                                       (let [{:keys [formdata]} (get context :body-params)]
                                         (gen-error
                                          (data-tile/get-data-tiles formdata))))}]
                ["neighborhood" {:get (fn [context]
                                        (let [body (get context :body-params)]
                                          (gen-error
                                           (search-api/neighborhood body))))}]]]
  #_["/probe"
     [["/liveness" {:get (fn [req]
                           (if (probe/liveness)
                             {:status 200}
                             {:status 500}))}]
      ["/readiness" {:get (fn [req]
                            (if (probe/readiness)
                              {:status 200}
                              {:status 500}))}]]])

(defn wrap-exception [handler]
  (fn [request]
    (try (handler request)
         (catch Exception e
           (probe/rate-exception e)
           (log/error e "uncaught exception in request")
           {:status 400}))))

(def handler (-> #'routes
                 wrap-exception
                 site
                 (wrap-cors :access-control-allow-origin [#".*"])))
