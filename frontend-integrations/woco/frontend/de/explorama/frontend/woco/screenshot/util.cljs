(ns de.explorama.frontend.woco.screenshot.util
  (:require [clojure.string :refer [blank? lower-case split trim]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [export-ignore-class]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [cljsjs.html-to-image-mod]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.util.date :refer [timestamp->date-str timestamp->time-str]]))

(def export-pixel-ratio 2)

(def export-exclude-classes #{export-ignore-class})

;; Hide all scrollbars
(def export-css-defs "*::-webkit-scrollbar {width: 0px; height: 0px;}")

(defn ignore-export-fn [dom-node]
  (if-let [class-list (aget dom-node "classList")]
    (not (boolean
          (some export-exclude-classes
                (array-seq class-list))))
    true))

(defn type-to-fn [type]
  (case type
    (:jpg :jpeg) js/HtmlToImage.toJpeg
    :svg js/HtmlToImage.toSvg
    :blob js/HtmlToImage.toBlob
    :canvas js/HtmlToImage.toCanvas
    :pixel js/HtmlToImage.toPixelData
    js/HtmlToImage.toPng))

(defn base64-valid? [base64-st]
  (and (string? base64-st)
       (not (blank? base64-st))
       (not= base64-st "data:,")))

(defn download-base64-img [base64img file-name]
  (when (string? file-name)
    (let [link (js/document.createElement "a")]
      (aset link "download" file-name)
      (aset link "href" base64img)
      (.click link))))

(defn make-screenshot- [{:keys [dom-id node callback-fn type screenshot-params file-name]
                         :as params}]
  (let [node (or node (js/document.getElementById dom-id))
        make-fn (type-to-fn type)]
    (when (and node make-fn (or file-name callback-fn))
      (.then (make-fn node (clj->js (-> (or screenshot-params {})
                                        (assoc :filter ignore-export-fn
                                               :extraStyleContent export-css-defs))))
             (fn [res]
               (download-base64-img res file-name)
               (when (fn? callback-fn)
                 (callback-fn res params))
               res)))))

(defn create-export-date []
  (let [{date-label    :pdf-footer-date}
        @(re-frame/subscribe [::i18n/translate-multi :pdf-footer-date])
        show-time? @(fi/call-api [:config :get-config-sub] :export-settings :export-show-time?)]
    (cond-> (str date-label
                 (timestamp->date-str))
      show-time?
      (str " "
           (timestamp->time-str)))))

(defn calc-frame-datasources [frame-id]
  (when frame-id
    @(fi/call-api :frame-info-api-value-sub frame-id :datasources)))

(defn calc-frames-datasources [frame-ids]
  (sort-by #(lower-case (trim %))
           (set (filter identity
                        (mapcat calc-frame-datasources
                                (set frame-ids))))))