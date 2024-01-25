(ns de.explorama.backend.handler
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [de.explorama.backend.abac.jwt :as jwt]
            [de.explorama.backend.abac.util :refer [user-info-role-fix]]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.shared.common.configs.provider :as configp]
            [hiccup.page :refer [html5]]
            [jsonista.core :as json]
            [muuntaja.core :as m]
            [pneumatic-tubes.httpkit :refer [websocket-handler]]
            [reitit.coercion.spec :as rcs]
            [reitit.core]
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [taoensso.timbre :as log]))

(def response-mappers
  {"text/plain" :message
   "application/json" identity
   "application/xml" identity})

(defmacro gen-error [fnc]
  `(try
     {:status 200
      :body ~fnc}
     (catch Throwable e#
       (probe/rate-exception e#)
       (error e#)
       {:status 400
        :body {:msg (.getMessage e#)}})))

#_
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
                      config/bucket-config)]
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

(defn- files-only [files]
  (filter #(.isFile %) files))

(defn- local-assets [path path-to]
  (let [replacement-path (-> (str/replace path "\\" "/")
                             (str/replace "//" "/"))]
    (into []
          (comp (map #(str/replace % "\\" "/"))
                (map #(str/replace-first %
                                         replacement-path
                                         ""))
                (map #(str path-to %))
                (filter #(str/ends-with? % ".css"))
                (map (fn [path]
                       [:link {:rel "stylesheet"
                               :href path
                               :type "text/css"}])))
          (->> (io/file path)
               file-seq
               files-only))))

(defn prepend [s prefix-line]
  (str prefix-line "\n" s))

(def routes
  [["/" {:get (fn [_]
                {:status 200
                 :body
                 (html5
                  (into
                   [:head
                    [:meta {:charset "utf-8"}]
                    [:meta {:http-equiv "X-UA-Compatible"
                            :content "IE=edge,chrome=1"}]
                    [:title "Explorama"]
                    [:link {:rel "shortcut icon"
                            :href "images/favicon.ico"}]]
                   (sort-by (fn [[_ {href :href}]] href)
                            (local-assets "resources/public" "asset")))
                  [:body.initial.login
                   [:div#app]
                   [:script {:src "js/woco.js"}]
                   [:script "de.explorama.frontend.woco.app.core.init();"]])})}]
   ["/js/woco.js"
    {:get (fn [_]
            {:status 200
             :content-type "application/javascript;charset=UTF-8"
             :body
             (prepend (slurp "resources/public/js/woco.js" :encoding "UTF-8")
                      (str "const EXPLORAMA_CLIENT_CONFIG = '"
                           (json/write-value-as-string @configp/client-configs)
                           "';"))})}]
   ["/img/*" (ring/create-resource-handler {:root "public/assets/img/"})]
   ["/asset/assets/img/*" (ring/create-resource-handler {:root "public/assets/img/"})]
   ["/asset/assets/fonts/*" (ring/create-resource-handler {:root "public/assets/fonts/"})]
   ["/asset/assets/css/*" (ring/create-resource-handler {:root "public/assets/css/"})]
   ["/js/compiled/*" (ring/create-resource-handler {:root "public/js/compiled/"})]
   ["/ws" {:get {:parameters {:query {:username string? :token string? :client-id string?}}
                 :handler
                 (fn [{{req :query} :parameters}]
                   (println "user-info" req)
                   (try
                     (let [user-info (user-info-role-fix req)]
                       (println "user-info" user-info)
                       (if (jwt/user-valid? user-info)
                         {:status 200
                          :body (websocket-handler (frontend-api/routes->tubes) user-info)}
                         {:status 403}))
                     (catch Throwable e
                       (log/error "Error creating websocket connection" e)
                       {:status 403})))}}]]
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
    
(def routes-opts
  {:exception pretty/exception
   :data {:coercion   rcs/coercion
          :muuntaja   (m/create
                       (assoc-in
                        m/default-options
                        [:default-format]
                        "application/edn"))
          :middleware [parameters/parameters-middleware
                       muuntaja/format-negotiate-middleware
                       muuntaja/format-response-middleware
                       (exception/create-exception-middleware
                        {::exception/default (partial exception/wrap-log-to-console exception/default-handler)})
                       muuntaja/format-request-middleware
                       coercion/coerce-response-middleware
                       coercion/coerce-request-middleware
                       multipart/multipart-middleware]}})
