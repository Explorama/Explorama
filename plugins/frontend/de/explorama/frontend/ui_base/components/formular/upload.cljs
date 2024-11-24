(ns de.explorama.frontend.ui-base.components.formular.upload
  (:require
   [clojure.string :as clj-str]
   [reagent.core :as reagent]
   [cljsjs.resumable.js]
   [cljs.reader :as reader]
   [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
   [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
   [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
   [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
   [de.explorama.frontend.ui-base.utils.data-exchange :refer [file-extention]]))

(def parameter-definition
  {:variant {:type :keyword
             :characteristics [:area :button]
             :desc "Upload variant. Area provides an drop-area"}
   :target {:type [:keyword :string]
            :desc "The target of upload. Can be :local to upload it directly in client or an URL for upload to server. For :local all parameters with :local and for url all parameters with :remote are relevant. Other parameters take into account on both"}
   :remote-test-target {:type [:string :function]
                        :desc "The target URL for the request to the server for each chunk to see if it already exists. This can be a string or a function that allows you to construct and return a value, based on supplied params"}
   :multi-files? {:type :boolean
                  :desc "If true, user can select and upload more than one file"}
   :local-read-as {:type :keyword
                   :characters [:clj :string :data-url :bin :bin-str]
                   :desc [:<> "Defines how to read the files. :clj will call (read-string <result>) on file-string-content. For further explanations about the other types see: " [:a {:href "https://developer.mozilla.org/en-US/docs/Web/API/FileReader/result" :target "_blank"} "FileReader API"]]}
   :charset {:type :string
             :desc "The charset for reading text on local upload"}
   :remote-upload-method {:type :string
                          :characteristics ["POST" "PUT" "PATCH"]
                          :desc "HTTP method to use when sending chunks to the server"}

   :remote-max-chunk-retries {:type :number
                              :desc "The maximum number of retries for a chunk before the upload is failed. Valid values are any positive integer and undefined for no limit."}
   :remote-chunk-retry-timeout {:type :number
                                :desc "The number of milliseconds to wait before retrying a chunk on a non-permanent error. Valid values are any positive integer and undefined for immediate retry."}
   :remote-chunksize {:type :number
                      :desc "The size in bytes of each uploaded chunk of data. The last uploaded chunk will be at least this size and up to two the size"}
   :remote-test-chunks? {:type :boolean
                         :desc "Make a request (method :remote-test-chunks-method) to the server for each chunks to see if it already exists. If implemented on the server-side, this will allow for upload resumes even after a browser crash or even a computer restart"}
   :remote-test-chunks-method {:type :string
                               :characteristics ["GET" "POST"]
                               :desc "Method for chunk test request"}
   :remote-query {:type [:map :function]
                  :desc "Extra parameters to include in the multipart request with data. This can be an map or a function. If a function, it will be passed a ResumableFile and a ResumableChunk object"}
   :remote-headers {:type [:map :function]
                    :desc "Extra headers to include in the upload with data. This can be an object or a function that allows you to construct and return a value, based on supplied file"}
   :remote-simultaneous-uploads {:type :number
                                 :desc "Number of simultaneous uploads"}
   :min-file-size {:type [:map :number]
                   :desc "The minimum allowed file size. You can define a look-up map (E.g. {\"json\" 10 \"csv\" 100}) or a number which represents the size in bytes"}
   :max-file-size {:type [:map :number]
                   :desc "The maximum allowed file size. You can define a look-up map (E.g. {\"json\" 10 \"csv\" 100}) or a number which represents the size in bytes"}
   :file-type {:type :vector
               :desc "The file types allowed to upload. If it's empty any file type is allowed"}
   :on-file-added {:type :function
                   :default-fn-str "(fn [file-infos])"
                   :desc "Triggered when for every file the user selects. When returning false, upload will be canceled/aborted"}
   :on-file-loaded {:type :function
                    :required true
                    :default-fn-str "(fn [result file-infos])"
                    :desc "Triggered for every file when it's upload is complete. Required when :target is :local."}
   :on-complete {:type :function
                 :default-fn-str "(fn [infos])"
                 :desc "Triggered when upload is completed (all files and chunks uploaded). infos is a map with additional infos like the total-size and filenames"}
   :on-file-progress {:type :function
                      :default-fn-str "(fn [progress-infos])"
                      :desc "Returns a map with :progress key which is a float between 0 and 1 indicating the current upload progress of the given file. Information about the file is also part of these map"}
   :on-progress {:type :function
                 :default-fn-str "(fn [progress-infos])"
                 :desc "Returns a map with :progress key which is a float between 0 and 1 indicating the current upload progress of the whole upload-process. :relative? is added when :target is an url. If relative? is true, the value is returned relative to all files in the Resumable.js instance"}
   :on-error {:type :function
              :default-fn-str "(fn [error-infos])"
              :desc "Triggered when an error occurs"}
   :on-max-size-error {:type :function
                       :default-fn-str "(fn [error-infos])"
                       :desc "Triggered when a file size is bigger then defined :max-file-size. Will abort the upload"}
   :on-min-size-error {:type :function
                       :default-fn-str "(fn [error-infos])"
                       :desc "Triggered when a file size is smaller then defined :min-file-size. Will abort the upload"}
   :upload-area-hint {:type [:string :derefable]
                      :desc "Is visible as hint in upload-area"}
   :upload-button-params {:type :map
                          :desc "Properties for Button. See at button component for API"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:variant :area
                         :target :local
                         :remote-test-chunks? false
                         :remote-chunksize (* 1024 1024)
                         :remote-simultaneous-uploads 3
                         :remote-upload-method "POST"
                         :remote-test-chunks-method "GET"
                         :local-read-as :clj
                         :multi-files? false
                         :upload-area-hint "Click and select or drag some file/s here"
                         :upload-button-params {:label "Upload"}
                         :charset "UTF-8"})

(defn- file-size-valid? [filename filesize {:keys [min-file-size max-file-size on-max-size-error on-min-size-error]}]
  (if (and filename filesize (or min-file-size max-file-size))
    (let [ext (file-extention filename)
          min-file-size (cond-> min-file-size
                          (map? min-file-size) (get ext))
          max-file-size (cond-> max-file-size
                          (map? max-file-size) (get ext))
          min-check? (or
                      (nil? min-file-size)
                      (and min-file-size (<= min-file-size filesize)))
          max-check? (or
                      (nil? max-file-size)
                      (and max-file-size (<= filesize max-file-size)))]
      (when (and on-min-size-error min-file-size (not min-check?))
        (on-min-size-error {:name filename
                            :size filesize
                            :extention ext
                            :min-size min-file-size}))
      (when (and on-max-size-error max-file-size (not max-check?))
        (on-max-size-error {:name filename
                            :size filesize
                            :extention ext
                            :max-size max-file-size}))
      (and min-check? max-check?))

    true))

(defn- remote-upload [{:keys [target remote-test-target remote-query multi-files? file-type
                              remote-test-chunks? remote-chunksize remote-simultaneous-uploads
                              remote-upload-method remote-max-chunk-retries remote-chunk-retry-timeout
                              remote-test-chunks-method remote-headers
                              on-file-added on-file-loaded on-complete
                              on-progress on-file-progress on-error]
                       :as params}
                      & upload-refs]
  (let [resumable (js/Resumable. (clj->js (cond-> {:target target
                                                   :testChunks remote-test-chunks?
                                                   :generateUniqueIdentifier true}
                                            remote-max-chunk-retries (assoc :maxChunkRetries remote-max-chunk-retries)
                                            remote-chunk-retry-timeout (assoc :chunkRetryInterval remote-chunk-retry-timeout)
                                            remote-upload-method (assoc :uploadMethod remote-upload-method)
                                            remote-test-chunks-method (assoc :testMethod remote-test-chunks-method)
                                            remote-chunksize (assoc :chunkSize remote-chunksize)
                                            remote-simultaneous-uploads (assoc :simultaneousUploads remote-simultaneous-uploads)
                                            file-type (assoc :fileType (mapv #(clj-str/replace % #"\." "") file-type))
                                            remote-headers (assoc :headers remote-headers)
                                            remote-test-target (assoc :testTarget remote-test-target)
                                            remote-query (assoc :query remote-query)
                                            (not multi-files?) (assoc :maxFiles 1))))]
    (.on resumable "fileAdded" (fn [file]
                                 (let [fn-res (when on-file-added (on-file-added  {:name (aget file "fileName")
                                                                                   :mime-type (aget file "file" "type")
                                                                                   :extention (file-extention (aget file "fileName"))
                                                                                   :last-modified (js/Date. (aget file "file" "lastModified"))
                                                                                   :unique-identifier (aget file "uniqueIdentifier")
                                                                                   :size (aget file "size")
                                                                                   :chunks (aget file "chunks" "length")
                                                                                   :file file}))]

                                   (if (and (file-size-valid? (aget file "fileName")
                                                              (aget file "size")
                                                              params)
                                            (not (false? fn-res)))
                                     (.upload resumable)
                                     (do
                                       (.abort file)
                                       (.cancel resumable))))))
    (.on resumable "fileSuccess" (fn [file message]
                                   (when on-file-loaded
                                     (on-file-loaded {:name (aget file "fileName")
                                                      :mime-type (aget file "file" "type")
                                                      :extention (file-extention (aget file "fileName"))
                                                      :last-modified (js/Date. (aget file "file" "lastModified"))
                                                      :unique-identifier (aget file "uniqueIdentifier")
                                                      :size (aget file "size")
                                                      :chunks (aget file "chunks" "length")
                                                      :file file
                                                      :message message}))))
    (.on resumable "fileProgress" (fn [file message]
                                    (when on-file-progress
                                      (on-file-progress {:name (aget file "fileName")
                                                         :progress (.progress resumable)
                                                         :mime-type (aget file "file" "type")
                                                         :extention (file-extention (aget file "fileName"))
                                                         :last-modified (js/Date. (aget file "file" "lastModified"))
                                                         :unique-identifier (aget file "uniqueIdentifier")
                                                         :size (aget file "size")
                                                         :chunks (aget file "chunks" "length")
                                                         :file file
                                                         :message message}))))
    (.on resumable "progress" (fn [relative]
                                (when on-progress
                                  (on-progress {:progress (.progress resumable)
                                                :relative? relative}))))
    (.on resumable "complete" (fn []
                                (when on-complete
                                  (let [files (aget resumable "files")]
                                    (on-complete {:total-size (apply + (map #(aget % "size") files))
                                                  :files (mapv #(aget % "fileName") files)})))))
    (.on resumable "error" (fn [message file]
                             (when on-error
                               (on-error  {:name (aget file "fileName")
                                           :mime-type (aget file "file" "type")
                                           :extention (file-extention (aget file "fileName"))
                                           :last-modified (js/Date. (aget file "file" "lastModified"))
                                           :unique-identifier (aget file "uniqueIdentifier")
                                           :size (aget file "size")
                                           :message message}))
                             (.abort file)
                             (.cancel resumable)))
    (doseq [ref upload-refs]
      (when-let [node (val-or-deref ref)]
        (.assignBrowse resumable node)
        (.assignDrop resumable node)))))

(defn- local-read-file [file num-files total-size loaded-sizes
                        {:keys [on-file-loaded charset local-read-as
                                on-file-progress on-progress
                                on-error on-complete]}]
  (let [file-reader (js/FileReader.)]
    (when (and file file-reader on-file-loaded)
      (case local-read-as
        :data-url (.readAsDataURL file-reader file)
        :bin (.readAsArrayBuffer file-reader file)
        :bin-str (.readAsBinaryString file-reader file)
        (.readAsText file-reader file charset))
      (aset file-reader
            "on-error"
            (fn [ev]
              (when on-error
                (on-error {:event ev}))))

      (aset file-reader
            "onprogress"
            (fn [ev]
              (let [filename (aget file "name")
                    file-progress (/ (aget ev "loaded")
                                     (aget ev "total"))
                    _ (swap! loaded-sizes assoc filename file-progress)
                    overall-progress (/ (apply + (vals @loaded-sizes))
                                        num-files)]
                (when on-file-progress
                  (on-file-progress {:name filename
                                     :progress file-progress
                                     :mime-type (aget file "type")
                                     :extention (file-extention filename)
                                     :last-modified (js/Date. (aget file "lastModified"))
                                     :file file
                                     :size (aget file "size")}))
                (when (and on-complete (= 1 overall-progress))
                  (on-complete {:total-size total-size
                                :files (vec (keys @loaded-sizes))}))
                (when on-progress
                  (on-progress {:progress overall-progress})))))

      (aset file-reader
            "onload"
            (fn [ev]
              (let [res (aget ev "target" "result")]
                (on-file-loaded (if (= :clj local-read-as)
                                  (reader/read-string res)
                                  res)
                                {:name (aget file "name")
                                 :mime-type (aget file "type")
                                 :extention (file-extention (aget file "name"))
                                 :last-modified (js/Date. (aget file "lastModified"))
                                 :file file
                                 :size (aget file "size")})))))))

(defn- local-upload-result-fn [files {:keys [multi-files? on-error on-file-added min-file-size max-file-size] :as params}]
  (try
    (let [total-size (apply + (map #(aget % "size") files))
          num-files (count files)
          loaded-sizes (atom {})
          added-abort? (when on-file-added
                         (some #(false? (on-file-added {:name (aget % "name")
                                                        :mime-type (aget % "type")
                                                        :extention (file-extention (aget % "name"))
                                                        :last-modified (js/Date. (aget % "lastModified"))
                                                        :file %
                                                        :size (aget % "size")}))
                               files))
          size-abort? (when (or min-file-size max-file-size)
                        (some #(false? (file-size-valid? (aget % "name")
                                                         (aget % "size")
                                                         params))
                              files))]
      (when-not (or added-abort? size-abort?)
        (if multi-files?
          (doseq [file files]
            (local-read-file file
                             num-files
                             total-size
                             loaded-sizes
                             params))
          (local-read-file (first files)
                           num-files
                           total-size
                           loaded-sizes
                           params))))
    (catch :default e
      (on-error {:exception e}))))

(defn- local-upload [ref {:keys [multi-files? file-type] :as params}]
  [:input (cond-> {:ref #(reset! ref %)
                   :type :file
                   :style {:display :none}
                   :on-change
                   (fn [ev]
                     (local-upload-result-fn (array-seq (aget ev "target" "files"))
                                             params)
                     (aset @ref "value" nil))} ;Otherwise on-change not fired when loading a file where filename is same as actual}
            multi-files? (assoc :multiple true)
            (vector? file-type) (assoc :accept (clj-str/join "," file-type)))])

(defn- upload-area [{:keys [target] :as params}]
  (let [area-ref (reagent/atom nil)
        upload-ref (reagent/atom nil)]
    (reagent/create-class
     {:reagent-render
      (fn [{:keys [target multi-files? upload-area-hint] :as params}]
        (let [local-target? (= target :local)
              upload-area-hint (val-or-deref upload-area-hint)]
          [:div.explorama__form__file-upload {:ref #(reset! area-ref %)
                                              :on-drag-enter #(.preventDefault %)
                                              :on-drag-over #(.preventDefault %)
                                              :on-drop (fn [e]
                                                         (.preventDefault e)
                                                         (local-upload-result-fn (if multi-files?
                                                                                   (array-seq (aget e "dataTransfer" "files"))
                                                                                   [(aget e "dataTransfer" "files" 0)])
                                                                                 params))
                                              :on-click #(when (and local-target? @upload-ref)
                                                           (.click @upload-ref))}
           [:span  upload-area-hint]
           (when local-target?
             [local-upload upload-ref params])]))
      :component-did-mount
      (fn [_]
        (when (string? target)
          (remote-upload params area-ref)))})))

(defn- upload-button [{:keys [target upload-button-params] :as params}]
  (let [local-target? (= target :local)
        button-id (str ::up-but (random-uuid))
        upload-ref (reagent/atom nil)]
    (reagent/create-class
     {:reagent-render
      (fn [_]
        [:div.explorama__form__input
         [button (merge (or upload-button-params {})
                        {:id button-id
                         :on-click #(do
                                      (when (and local-target? @upload-ref)
                                        (.click @upload-ref))
                                      (when (and upload-button-params (get upload-button-params :on-click))
                                        ((get upload-button-params :on-click) %)))})]
         (when local-target?
           [local-upload upload-ref params])])
      :component-did-mount
      (fn [_]
        (when (string? target)
          (remote-upload params (js/document.getElementById button-id))))})))

(defn ^:export upload [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "upload" specification params)}
     (let [{:keys [variant]} params]
       (case variant
         :area [upload-area params]
         :button [upload-button params]
         [:<>]))]))