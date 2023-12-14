(ns de.explorama.frontend.ui-base.overview.utils.data-exchange
  (:require [de.explorama.frontend.ui-base.utils.data-exchange :as data-exchange]
            [de.explorama.frontend.ui-base.overview.page :refer [defutils defdocu]]))

(defutils
  {:name "Data-Exchange"
   :require-statement "[de.explorama.frontend.ui-base.utils.data-exchange :as data-exchange]"
   :desc "Functions for downloading and uploading data"})

(defdocu
  {:title "content-types"
   :is-function? false
   :desc [:<> "The following content-types are available under "
          [:b "data-exchange/content-type-name"]
          ":"
          [:br] [:br]
          [:code.parameter {:style {:padding "0px"}}
           " text: edn-content-type, text-content-type, xml-content-type, html-content-type, csv-content-type, json-content-type\n"
           " images: png-content-type, jpg-content-type, svg-content-type, bmp-content-type\n"
           " others: pdf-content-type, zip-content-type, tar-content-type, bin-content-type"]]})

(defdocu
  {:fn-metas (meta #'data-exchange/download-content)
   :signatures ["[filename content options]"
                "[filename content]"]})

(defdocu
  {:fn-metas (meta #'data-exchange/download-from-remote)
   :signatures ["[filename url options]"
                "[filename url]"]})

(defdocu
  {:fn-metas (meta #'data-exchange/file-extention)
   :returns [:string :map-entry]})
