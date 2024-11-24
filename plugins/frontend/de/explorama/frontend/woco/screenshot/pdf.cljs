(ns de.explorama.frontend.woco.screenshot.pdf
  (:require ["jspdf" :refer [jsPDF]]
            [clojure.string :refer [join]]
            [cuerdas.core :as curedas]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.screenshot.util :refer [calc-frames-datasources ignore-export-fn
                                                                create-export-date type-to-fn export-css-defs]]
            [de.explorama.frontend.woco.util.dom :refer [fit-size get-node-size]]))

(def orientation "landscape") ;can also be "portrait"
;The format of the first page.
(def format "a4")
;size for format in mm
(def format-size-mm {:w 210 :h 297 :pm 3 :im 5}) ;a4 (pm = print/cut margin ; im = inner margin for content)
;Compress the generated PDF.
(def compress? true)

(def footer-fontsize 8)
(def footer-lineheight-factor 1.3)

(def title-height 5)

(def footer? true)
(def default-footer-height 15)

(def applied-format-size
  (let [{:keys [w h]} format-size-mm]
    (if (= orientation "landscape")
      [h w]
      [w h])))

;; PDF config: See http://raw.githack.com/MrRio/jsPDF/master/docs/jsPDF.html
;Orientation of the first page

(defn- calc-footer-metas [pdf-obj frame-ids page-num max-pages]
  (let [{page-label    :pdf-footer-page
         created-by    :pdf-footer-created
         export-displayed-datasources :export-displayed-datasources}
        @(re-frame/subscribe [::i18n/translate-multi :pdf-footer-exported :pdf-footer-created :pdf-footer-date :pdf-footer-page :export-displayed-datasources])
        description (-> @(fi/call-api [:config :get-config-sub]
                                      :export-settings :export-custom-description)
                        (get @(re-frame/subscribe [::i18n/current-language])))
        [w h] applied-format-size
        {:keys [pm im]} format-size-mm
        content-margin (+ im pm)
        left-content-margin (+ content-margin im)
        show-date? @(fi/call-api [:config :get-config-sub] :export-settings :export-show-date?)
        show-user? @(fi/call-api [:config :get-config-sub] :export-settings :export-show-user?)
        show-datasources? @(fi/call-api [:config :get-config-sub] :export-settings :export-show-datasources?)
        datasources (calc-frames-datasources frame-ids)
        datasource-mapping @(fi/call-api [:config :get-config-sub] :export-settings :export-datasource-mapping)
        page-str (str page-label page-num "/" max-pages)
        {line-height "h"
         page-width "w"}
        (js->clj (.getTextDimensions pdf-obj page-str))
        desc (cond-> ""
               (seq description)
               (str description)
               (and show-datasources? (seq datasources))
               (cond-> (seq description)
                 (str "; ")
                 :always
                 (str export-displayed-datasources " " (join ", " (map #(get datasource-mapping % %) datasources)))))
        footer-desc-width (- w left-content-margin page-width content-margin 10)
        {user-name :name} @(fi/call-api :user-info-sub)
        export-by (str created-by user-name)
        export-date-string (create-export-date)
        footer-text (when (seq desc)
                      desc)
        footer-meta-text (when (or show-user? show-date?)
                           (cond-> ""
                             show-date?
                             (str export-date-string "    ")
                             show-user?
                             (str export-by)))]
    {:page-str page-str
     :footer-meta-text footer-meta-text
     :footer-meta-height (if (seq footer-meta-text)
                           (* line-height footer-lineheight-factor)
                           0)
     :footer-text footer-text
     :footer-height (cond-> (-> (count desc)
                                (/ 170)
                                (js/Math.ceil)
                                (max 1)
                                (- 1))
                      :always (* line-height footer-lineheight-factor))
                      ;; :always (int))
     :footer-width footer-desc-width}))

(defn- make-pdf-footer [pdf-obj {:keys [footer-text footer-meta-text footer-meta-height footer-width footer-height page-str]}]
  (let [[w h] applied-format-size
        {:keys [pm im]} format-size-mm
        content-margin (+ im pm)
        left-content-margin (+ content-margin im)
        text-y (- h content-margin footer-height footer-meta-height)
        line-y (- text-y footer-meta-height im)]
    (cond-> (-> pdf-obj
        ;separator
                (.setDrawColor 227)
                (.setLineWidth 0.1)
                (.line pm line-y (- w pm) line-y)
                (.setFontSize footer-fontsize)
                (.setTextColor 50 50 50))
      (seq footer-text)
      (.text footer-text
             left-content-margin
             text-y
             #js{:align "justify"
                 :lineHeightFactor footer-lineheight-factor
                 :maxWidth footer-width})
      (seq footer-meta-text)
      (-> (.setFont js/undefined "italic")
          (.setTextColor 134 142 150)
          (.text footer-meta-text
                 left-content-margin
                 (- h content-margin)
                 #js{:lineHeightFactor footer-lineheight-factor
                     :maxWidth footer-width})
          (.setFont js/undefined "normal")
          (.setTextColor 50 50 50))
      :always
      (.text page-str
             (+ (- w left-content-margin (.getTextWidth pdf-obj page-str)))
             (- h content-margin)))))

(defn finalize-pdf [node res
                    {:keys [type optional-title-sub-vec file-name frame-ids callback-fn] :as params}
                    & [sidebar-res sidebar-node]]
  (let [optional-title (when optional-title-sub-vec @(re-frame/subscribe optional-title-sub-vec))
        {:keys [pm im]} format-size-mm
        [w h] applied-format-size
        margin-mm (+ pm im)
        width-mm-org (- w (* 2 margin-mm))
        title-height (if optional-title (+ title-height margin-mm) 0)
        pdf-obj (-> (new jsPDF #js{:orientation orientation
                                   :format format
                                   :compress compress?})
                    (.setFontSize footer-fontsize))
        {:keys [footer-height footer-meta-height] :as footer-metas} (calc-footer-metas pdf-obj frame-ids 1 1)
        height-mm-org (- h
                         (* 2 margin-mm)
                         title-height
                         (if footer?
                           (-> (max footer-height default-footer-height)
                               (+ footer-meta-height im))
                           0)
                         im)
        {:keys [height width]} (get-node-size node)
        [width-mm height-mm] (fit-size width-mm-org height-mm-org width height)
        [width-scrn-mm height-scrn-mm]
        (when sidebar-node (let [{height-scrn :height width-scrn :width}
                                 (get-node-size sidebar-node)]
                             (fit-size width-mm-org height-mm-org width-scrn height-scrn)))]
    (cond-> pdf-obj
      footer? (make-pdf-footer footer-metas)
      optional-title (->
                      (.setTextColor 70)
                      (.setFontSize 20)
                      (.setFont js/undefined "bold")
                      (.text margin-mm
                             title-height
                             optional-title))
      :always (.addImage res
                         (name type)
                         margin-mm
                         (+ margin-mm title-height)
                         width-mm
                         height-mm)
      sidebar-res (.addImage sidebar-res
                             (name type)
                             (+ margin-mm (- width-mm width-scrn-mm))
                             (+ margin-mm title-height)
                             width-scrn-mm
                             height-scrn-mm)
      :always (.output "save" file-name))
    (when (fn? callback-fn)
      (callback-fn res params))))

(defn make-screenshot-pdf [{:keys [dom-id callback-fn type screenshot-params file-name sidebar]
                            :as params}]
  (let [node (js/document.getElementById dom-id)
        make-fn (type-to-fn type)]
    (when (and node make-fn (or file-name callback-fn))
      (.then (make-fn node (clj->js (-> (or screenshot-params {})
                                        (assoc :filter ignore-export-fn
                                               :extraStyleContent export-css-defs))))
             (fn [res]
               (if-not sidebar
                 (finalize-pdf node res params)
                 (let [make-fn (type-to-fn type)
                       sidebar-node (js/document.getElementById sidebar)]
                   (.then (make-fn sidebar-node (clj->js {:style {:right :inherit
                                                                  :left 0
                                                                  :top 0
                                                                  :z-index -1000}
                                                          :quality 1.0
                                                          :cacheBust true
                                                          :pixelRatio 2
                                                          :filter ignore-export-fn
                                                          :extraStyleContent export-css-defs}))
                          (fn [sidebar-res]
                            (finalize-pdf node res params sidebar-res sidebar-node))))))))))
