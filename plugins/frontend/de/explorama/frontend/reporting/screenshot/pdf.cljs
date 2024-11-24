(ns de.explorama.frontend.reporting.screenshot.pdf
  (:require ["jspdf"]
            [clojure.string :refer [join]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.reporting.util.dom :refer [fit-size get-node-size]]
            [de.explorama.frontend.reporting.screenshot.util :refer [base64-valid?
                                                                     calc-frames-datasources
                                                                     export-css-defs
                                                                     create-export-date ignore-export-fn type-to-fn]]))


;;; --------------- Generell PDF settings -------------------

;; PDF config: See http://raw.githack.com/MrRio/jsPDF/master/docs/jsPDF.html
;The format of the first page.
(def format "a4")
;size for format in mm
(def format-size-mm {:w 210 :h 297 :pm 3 :im 5}) ;a4 (pm = print/cut margin ; im = inner margin for content)
;Compress the generated PDF.
(def compress? true)

(def footer-fontsize 8)
(def footer-lineheight-factor 1.3)

(def footer? true)
(def default-footer-height 15)

(defn applied-format-size [orientation]
  (let [{:keys [w h]} format-size-mm]
    (if (= orientation "landscape")
      [h w]
      [w h])))

;; PDF config: See http://raw.githack.com/MrRio/jsPDF/master/docs/jsPDF.html
;Orientation of the first page

(defn- calc-footer-metas [pdf-obj frame-ids page-num max-pages orientation]
  (let [{page-label    :pdf-footer-page
         created-by    :pdf-footer-created
         export-displayed-datasources :export-displayed-datasources}
        @(re-frame/subscribe [::i18n/translate-multi :pdf-footer-exported :pdf-footer-created :pdf-footer-date :pdf-footer-page :export-displayed-datasources])
        description (-> @(fi/call-api [:config :get-config-sub]
                                      :export-settings :export-custom-description)
                        (get @(re-frame/subscribe [::i18n/current-language])))
        [w h] (applied-format-size orientation)
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
     :orientation orientation
     :footer-text footer-text
     :footer-height (cond-> (-> (count desc)
                                (/ (if (= orientation "landscape")
                                     170
                                     110))
                                (js/Math.ceil)
                                (max 1)
                                (- 1))
                      :always (* line-height footer-lineheight-factor))
                      ;; :always (int))
     :footer-width footer-desc-width}))

(defn- make-pdf-footer [pdf-obj {:keys [curr-page orientation max-page footer-text footer-meta-text footer-meta-height footer-width footer-height page-str]}]
  (let [[w h] (applied-format-size orientation)
        {:keys [pm im]} format-size-mm
        content-margin (+ im pm)
        left-content-margin (+ content-margin im)
        text-y (- h content-margin footer-height footer-meta-height)
        line-y (- text-y footer-meta-height im)
        page-str (if (and curr-page max-page)
                   (str @(re-frame/subscribe [::i18n/translate :pdf-footer-page])
                        curr-page "/" max-page)
                   page-str)]

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

;;; --------------- Dashboard PDF -------------------

;Orientation of the first page
(def dashboard-orientation "landscape")

(defn make-screenshot-dashboard [{:keys [dom-id callback-fn frame-ids image-type screenshot-params file-name] :as params}]
  (let [node (js/document.getElementById dom-id)
        make-fn (type-to-fn image-type)
        {:keys [height width]} (get-node-size node)]
    (when (and node make-fn (or file-name callback-fn))
      (.then (make-fn node (clj->js (-> (or screenshot-params {})
                                        (assoc :filter ignore-export-fn
                                               :extraStyleContent export-css-defs))))
             (fn [res]
               (if-not (base64-valid? res)
                 (make-screenshot-dashboard params)
                 (let [{:keys [pm im]} format-size-mm
                       [w h] (applied-format-size dashboard-orientation)
                       margin-mm (+ pm im)
                       width-mm (- w (* 2 margin-mm))
                       pdf-obj (-> (new js/jspdf.jsPDF #js{:orientation dashboard-orientation
                                                           :format format
                                                           :compress compress?})
                                   (.setFontSize footer-fontsize))
                       {:keys [footer-height footer-meta-height] :as footer-metas} (calc-footer-metas pdf-obj frame-ids 1 1 dashboard-orientation)
                       height-mm (- h
                                    (* 2 margin-mm)
                                    (if footer?
                                      (-> (max footer-height default-footer-height)
                                          (+ footer-meta-height im))
                                      0)
                                    im)
                       [width-mm height-mm] (fit-size width-mm height-mm width height)]
                   (cond-> (new js/jspdf.jsPDF #js{:orientation dashboard-orientation
                                                   :format format
                                                   :compress compress?})

                     footer? (make-pdf-footer footer-metas)
                     :always (.addImage res
                                        (name image-type)
                                        margin-mm margin-mm
                                        width-mm
                                        height-mm)
                     :always (.output "save" file-name))))
               (when (fn? callback-fn)
                 (callback-fn res params)))))))

;;; --------------- Report PDF -------------------

;Orientation of the first page
(def report-orientation "portrait")

(defn- add-pdf-images [sizes images content-margin current-pdf page-elements]
  (let [first-element (first page-elements)]
    (reduce (fn [acc-page-height element]
              (let [idx (if (= element "header")
                          0
                          (inc element))
                    is-first? (= element first-element)
                    {:keys [width height]} (get sizes idx)
                    image (get images idx)]
                (if (and width height image)
                  (do
                    (.addImage current-pdf
                               image
                               content-margin
                               (if is-first? content-margin acc-page-height)
                               width
                               height)
                    (+ acc-page-height height))
                  acc-page-height)))
            content-margin
            page-elements)

    current-pdf))

(defn- max-page-size [{:keys [footer-height footer-meta-height]}]
  (let [{:keys [pm im]} format-size-mm
        [^number w ^number h] (applied-format-size report-orientation)
        ^number content-margin (+ pm im)
        module-height->width-ratio 0.7 ;;to ensure that 2 images are on a page when footer is growing
        ^number max-page-height (- h
                                   (* 2 content-margin)
                                   (if (and footer? footer-height footer-meta-height)
                                     (-> (max footer-height default-footer-height)
                                         (+ footer-meta-height content-margin))
                                     0)
                                   im)
        ^number max-page-width (min (- w (* 2 content-margin))
                                    (* max-page-height module-height->width-ratio))]
    {:max-page-width max-page-width
     :max-page-height max-page-height
     :content-margin content-margin}))

(defn- fill-pdf [{:keys [file-name frame-ids]}
                 {:keys [sizes acc-page-height
                         pages page-mapping page-frame-id-mapping footer-meta-text]}
                 images]
  (let [pdf-obj (-> (new js/jspdf.jsPDF #js{:orientation report-orientation
                                            :format format
                                            :compress compress?})
                    (.setFontSize footer-fontsize))
        {:keys [content-margin]} (max-page-size nil)
        footer-metas (calc-footer-metas pdf-obj
                                        (get page-frame-id-mapping 1)
                                        1 pages report-orientation)
        base-pdf (cond-> pdf-obj
                   footer? (make-pdf-footer (assoc footer-metas :footer-meta-text footer-meta-text)))
        add-image-fn (partial add-pdf-images sizes images content-margin)]
    (loop [current-pdf base-pdf
           [current-page page-elements] (first page-mapping)
           page-mapping (rest page-mapping)]
      (if (not current-page)
        (.output current-pdf "save" file-name)
        (let [footer-metas (when (not= current-page 1)
                             (calc-footer-metas current-pdf
                                                (get page-frame-id-mapping current-page)
                                                current-page pages report-orientation))]
          (cond-> current-pdf
            (not= current-page 1)
            (.addPage format report-orientation)
            (not= current-page 1)
            (make-pdf-footer (assoc footer-metas :footer-meta-text footer-meta-text))
            :always
            (add-image-fn page-elements)
            :always
            (recur (first page-mapping)
                   (rest page-mapping))))))))

(defn- make-report-images [result nodes make-fn {:keys [screenshot-params] :as params} callback-fn]
  (if (empty? nodes)
    (callback-fn result)
    (.then (make-fn (first nodes) (clj->js (-> (or screenshot-params {})
                                               (assoc :filter ignore-export-fn
                                                      :extraStyleContent export-css-defs))))
           (fn [img]
             (if-not (base64-valid? img)
               (make-report-images result nodes make-fn params callback-fn)
               (make-report-images (conj result img)
                                   (rest nodes)
                                   make-fn
                                   screenshot-params
                                   callback-fn))))))

(defn- check-fits-current-page [^number page-height ^number acc-page-height ^number element-height]
  (let [^number new-acc (+ acc-page-height
                           element-height)
        ^boolean fits? (>= page-height new-acc)]
    {:next-page? (not fits?)
     :new-height-acc (if fits?
                       new-acc
                       element-height)}))

(defn make-screenshot-report [{:keys [rows dom-id image-type frame-ids module-mapping] :as params}]
  (let [temp-pdf-obj (-> (new js/jspdf.jsPDF #js{:orientation report-orientation
                                                 :format format
                                                 :compress compress?})
                         (.setFontSize footer-fontsize))
        {:keys [footer-meta-text] :as footer-metas} (calc-footer-metas temp-pdf-obj frame-ids 1 1 report-orientation)
        {:keys [max-page-width max-page-height]} (max-page-size footer-metas)
        {:keys [nodes] :as nodes-with-size}
        (reduce (fn [{:keys [pages acc-page-height] :as acc} postfix]
                  (let [frame-ids (when (number? postfix)
                                    (get module-mapping postfix))
                        node (js/document.getElementById (str dom-id postfix))
                        {:keys [width height]} (when node (get-node-size node))
                        [scaled-width scaled-height] (when (and width height)
                                                       (fit-size max-page-width max-page-height
                                                                 width height))
                        {:keys [next-page? new-height-acc]} (check-fits-current-page max-page-height acc-page-height scaled-height)
                        pages (cond-> pages
                                next-page? inc)]
                    (cond-> acc
                      node (update :nodes conj node)
                      next-page? (assoc :pages pages)
                      frame-ids (update-in [:page-frame-id-mapping pages] #(apply conj
                                                                                  (or % #{})
                                                                                  frame-ids))
                      new-height-acc (assoc :acc-page-height new-height-acc)
                      new-height-acc (update-in [:page-mapping pages] #(-> (or % [])
                                                                           (conj postfix)))
                      (and scaled-width scaled-height)
                      (update :sizes conj {:width scaled-width
                                           :height scaled-height}))))
                {:acc-page-height 0
                 :pages 1
                 :page-mapping {}
                 :page-frame-id-mapping {}
                 :nodes []
                 :sizes []
                 :footer-meta-text footer-meta-text}
                (conj (range rows) "header"))
        make-fn (type-to-fn image-type)]
    (make-report-images [] nodes make-fn params (partial fill-pdf params nodes-with-size))))

;;; --------------- Export PDF -------------------

(defn make-screenshot-pdf [{:keys [type] :as params}]
  (if (= type :report)
    (make-screenshot-report params)
    (make-screenshot-dashboard params)))