(ns de.explorama.frontend.reporting.screenshot.png
  (:require [clojure.string :refer [join]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]
            [reagent.dom.server :refer [render-to-string]]
            [de.explorama.frontend.reporting.screenshot.util :refer [calc-frames-datasources
                                                                     create-export-date make-screenshot-]]))

(defn export-png-footer
  ([width frame-ids dynamic-font-size?]
   (let [width (or width) ;(when dynamic-font-size? (:width))) ;@(re-frame/subscribe [:de.explorama.frontend.reporting.standalone.page/available-size]))))
         font-size (when (and dynamic-font-size? (number? width))
                     (max 100 (* 100 (/ width 1400))))
         show-datasources? @(fi/call-api [:config :get-config-sub] :export-settings :export-show-datasources?)
         show-date? @(fi/call-api [:config :get-config-sub] :export-settings :export-show-date?)
         show-user? @(fi/call-api [:config :get-config-sub] :export-settings :export-show-user?)
         description (-> @(fi/call-api [:config :get-config-sub] :export-settings :export-custom-description)
                         (get @(re-frame/subscribe [::i18n/current-language])))
         datasources (calc-frames-datasources frame-ids)
         datasource-mapping @(fi/call-api [:config :get-config-sub] :export-settings :export-datasource-mapping)
         {created-by    :pdf-footer-created
          export-displayed-datasources :export-displayed-datasources}
         @(re-frame/subscribe [::i18n/translate-multi :pdf-footer-created :export-displayed-datasources])
         {user :name} @(fi/call-api :user-info-sub)]
     (if-not (or show-datasources? show-date? show-user?)
       [:<>]
       [:div.export-footer {:style {:width (cond
                                             width width
                                             dynamic-font-size? "100%"
                                             :else "780px")
                                    :font-size (if font-size
                                                 (str font-size "%")
                                                 "100%")}}
        [:div.text-justify
         (cond-> ""
           (seq description)
           (str description)
           (and show-datasources? (seq datasources))
           (cond-> (seq description)
             (str "; ")
             :always
             (str export-displayed-datasources " " (join ", " (map #(get datasource-mapping % %) datasources)))))]
        (when (or show-date? show-user?)
          [:div.inline.justify-between.text-gray.text-italic
           (when show-date?
             [:span {:style {:margin-right 20}}
              (create-export-date)])
           (when show-user?
             [:span (str created-by user)])])])))
  ([frame-ids dynamic-font-size?]
   (export-png-footer nil dynamic-font-size?)))

(defn make-screenshot-with-details [{:keys [callback-fn type frame-id frame-ids] :as params}]
  (make-screenshot-
   (assoc (dissoc params :file-name)
          :type :canvas
          :callback-fn
          (fn [canvas]
            (let [parent (js/document.createElement "div")
                  footer-wrapper (js/document.createElement "div")]
              (aset parent "style" "position: absolute; left: 0px; top: 0px; z-index: -1000;")
              (.appendChild parent canvas)
              (aset footer-wrapper "innerHTML" (render-to-string
                                                (export-png-footer
                                                 (aget canvas "width")
                                                 (or frame-ids #{frame-id})
                                                 (= type :dashboard))))

              (.appendChild parent footer-wrapper)
              (.appendChild js/document.body parent)
              (make-screenshot- (assoc params
                                       :node parent
                                       :callback-fn
                                       (fn [res params]
                                         (.removeChild js/document.body parent)
                                         (when (fn? callback-fn)
                                           (callback-fn res params))))))))))