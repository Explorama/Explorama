(ns de.explorama.frontend.ui-base.utils.data-exchange
  (:require [clojure.string :as clj-str]
            [taoensso.timbre :refer [error]]))

;; Content-types from https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types

(def edn-content-type "text/edn") ;; not registered at http://www.iana.org/assignments/media-types/media-types.xhtml 
(def text-content-type "text/plain")
(def xml-content-type "text/xml")
(def html-content-type "text/html")
(def csv-content-type "text/csv")
(def json-content-type "application/json")
(def pdf-content-type "application/pdf")
(def png-content-type "image/png")
(def jpg-content-type "image/jpeg")
(def svg-content-type "image/svg+xml")
(def bmp-content-type "image/bmp")
(def zip-content-type "application/zip")
(def tar-content-type "application/x-tar")
(def bin-content-type "application/octet-stream")

(defn ^:export download-content
  "Downloads a given content (string, data-structure etc.)
   
| Parameter    | description  |
| ------------- | ------------- |
| `filename`  | download-filename  |
| `content`  | The download content (string, data-structure, etc.). When using `:send-as-blob?` an js/Uint8Array  |
| `optional params`  | Map, for supported Keys see table below  |


  Supported optional params:
| Parameter | description |
| -------------- | ------------- |
| `content-type`| The content-type of download content. Default:  `text/plain` or `de.explorama.frontend.ui-base.utils.data-exchange/text-content-type` |
| `send-as-blob?` | If true, the content will be handled as blob, e.g. for binary data |
| `charset` | Default: `UTF-8` |
   
  Example:
  ```clojure
    => (download-content \"my-file.edn\" {:a 0 :b 1} {:content-type edn-content-type})
    => nil
  ```
  "
  ([filename content {:keys [content-type charset send-as-blob?]
                      :or {content-type text-content-type
                           send-as-blob? false
                           charset "UTF-8"}}]
   (try
     (let [temp (js/document.createElement "a")
           url (if send-as-blob?
                 (js/URL.createObjectURL (js/Blob. (clj->js [content])
                                                   #js{:type (cond-> (str content-type)
                                                               charset (str ";charset=;" charset))}))
                 (cond-> (str "data:" content-type)
                   charset (str " charset=" charset)
                   :always (str "," (js/encodeURIComponent content))))]
       (.setAttribute temp "href" url)
       (.setAttribute temp "download" filename)
       (aset temp "style" "display" "none")
       (.appendChild js/document.body temp);
       (.click temp)
       (.removeChild js/document.body temp))
     (catch :default e
       (error "Failed to download file" filename e))))
  ([filename content]
   (download-content filename content {})))

(defn ^:export download-from-remote
  "Download content from remote
  
| Parameter | description |
| -------------- | ------------- |
| `filename` | download-filename |
| `url`   | remote url which delivers the download content` |
| `optional params` | Map, for supported Keys see table below |


  Supported optional params:
| Parameter           | description |
| -------------- | ------------- |
| `content-type` | The content-type of download content. Default:  `text/plain` or `de.explorama.frontend.ui-base.utils.data-exchange/text-content-type` |
| `request-type` | e.g. `GET` or `POST`. Default: `GET` |
| `charset` | Default: `UTF-8` |
| `timeout` | Timeout of request |
| `on-timeout` | fn which will be triggered when request timed out |
| `on-loadstart` | fn which will be triggered when load is starting |
| `on-loadend` | fn which fires when a request has completed (successfully or not) |
| `on-response` | fn which will be triggered when there is a response from server after the transaction completes successfully |
| `on-progress` |fn which fires periodically when a request receives more data |
| `on-error` | fn which fires when the request encountered an error |
  
  Example:
  ```clojure
    => (download-from-remote \"datasources.json\" \"http://localhost:3453/datasources?bucket=temp\")})
    => nil
  ```
   "
  ([filename url {:keys [content-type request-type charset
                         timeout on-timeout
                         on-response on-progress on-error
                         on-loadstart on-loadend]
                  :or {content-type text-content-type
                       request-type "GET"
                       charset "UTF-8"}}]
   (when (and filename url)
     (try
       (let [req (js/XMLHttpRequest.)]
         (.open req request-type url true)
         (.setRequestHeader req "content-type" (str "application/x-www-form-urlencoded; charset=" charset))
         (when timeout
           (aset req "timeout" timeout))
         (aset req "responseType" "blob")
         (aset req "onload"
               (fn [ev]
                 (let [status (aget ev "srcElement" "status")]
                   (if (not= status 200)
                     (do (error "Download request failed with status:" status (aget ev "srcElement"))
                         (when on-error
                           (on-error status (aget ev "srcElement" "response"))))
                     (let [temp (js/document.createElement "a")]
                       (.setAttribute temp
                                      "href"
                                      (js/window.URL.createObjectURL
                                       (js/Blob. (clj->js [(aget req "response")])
                                                 #js{:type content-type})))
                       (.setAttribute temp "download" filename)
                       (aset temp "style" "display" "none")
                       (.appendChild js/document.body temp);
                       (.click temp)
                       (.removeChild js/document.body temp)
                       (when on-response
                         (on-response (aget ev "srcElement" "response"))))))))
         (when on-timeout
           (aset req "ontimeout" on-timeout))
         (when on-progress
           (aset req "onprogress" on-progress))
         (when on-loadstart
           (aset req "onloadstart" on-loadstart))
         (when on-loadend
           (aset req "onloadend" on-loadend))
         (when on-error
           (aset req "onerror" on-error))
         (.send req))
       (catch :default e
         (error "Failed to download file" filename e)))))
  ([filename url]
   (download-from-remote filename url {})))

(defn ^:export file-extention
  "Calculates the extention of a filename-string
   
| Parameter | description |
| -------------- | ------------- |
| `filename` | The filename as string |
| `optional look-up` | A look-up table to map an extention to another, For example: jpeg -> jpg |

  Example:
  ```clojure
    => (file-extention \"my-file.json\")
    => \"json\"
   
    => (file-extention \"my-file.jpeg\" {\"jpeg\" :jpg})
    => :jpg
  ```
   "
  ([filename look-up]
   (try
     (let [dot-idx (clj-str/last-index-of (or filename "")
                                          ".")
           dot-idx (when dot-idx (inc dot-idx))
           ext (clj-str/trim (subs filename dot-idx (count filename)))]
       (if-not dot-idx
         ""
         (get (or look-up {})
              ext ext)))
     (catch :default e
       (error "Failed to calculate file-extention from" filename "Error:" e)
       "")))
  ([filename]
   (file-extention filename {})))
