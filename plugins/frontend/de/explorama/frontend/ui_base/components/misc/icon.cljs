(ns de.explorama.frontend.ui-base.components.misc.icon
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]))

(def default-parameters {})

(def sizes {:xs     "icon-xs"
            :small  "icon-sm"
            :medium "icon-md"
            :large  "icon-lg"
            :xl     "icon-xl"
            :xxl    "icon-xxl"
            :3xl    "icon-3xl"
            :adapt-to-font "icon-font-size"})

(def colors {:white "icon-white"
             :black "icon-black"
             :gray {:default "icon-gray"
                    :0 "icon-black"
                    :1 "icon-gray-900"
                    :2 "icon-gray-800"
                    :3 "icon-gray-700"
                    :4 "icon-gray-600"
                    :5 "icon-gray-500"
                    :6 "icon-gray-400"
                    :7 "icon-gray-300"
                    :8 "icon-gray-200"
                    :9 "icon-gray-100"
                    :10 "icon-white"}
             :orange {:default "icon-orange"
                      :0 "icon-black"
                      :1 "icon-orange-900"
                      :2 "icon-orange-800"
                      :3 "icon-orange-700"
                      :4 "icon-orange-600"
                      :5 "icon-orange-500"
                      :6 "icon-orange-400"
                      :7 "icon-orange-300"
                      :8 "icon-orange-200"
                      :9 "icon-orange-100"
                      :10 "icon-white"}
             :teal {:default "icon-teal"
                    :0 "icon-black"
                    :1 "icon-teal-900"
                    :2 "icon-teal-800"
                    :3 "icon-teal-700"
                    :4 "icon-teal-600"
                    :5 "icon-teal-500"
                    :6 "icon-teal-400"
                    :7 "icon-teal-300"
                    :8 "icon-teal-200"
                    :9 "icon-teal-100"
                    :10 "icon-white"}
             :yellow {:default "icon-yellow"
                      :0 "icon-black"
                      :1 "icon-yellow-900"
                      :2 "icon-yellow-800"
                      :3 "icon-yellow-700"
                      :4 "icon-yellow-600"
                      :5 "icon-yellow-500"
                      :6 "icon-yellow-400"
                      :7 "icon-yellow-300"
                      :8 "icon-yellow-200"
                      :9 "icon-yellow-100"
                      :10 "icon-white"}
             :green {:default "icon-green"
                     :0 "icon-black"
                     :1 "icon-green-900"
                     :2 "icon-green-800"
                     :3 "icon-green-700"
                     :4 "icon-green-600"
                     :5 "icon-green-500"
                     :6 "icon-green-400"
                     :7 "icon-green-300"
                     :8 "icon-green-200"
                     :9 "icon-green-100"
                     :10 "icon-white"}
             :blue {:default "icon-blue"
                    :0 "icon-black"
                    :1 "icon-blue-900"
                    :2 "icon-blue-800"
                    :3 "icon-blue-700"
                    :4 "icon-blue-600"
                    :5 "icon-blue-500"
                    :6 "icon-blue-400"
                    :7 "icon-blue-300"
                    :8 "icon-blue-200"
                    :9 "icon-blue-100"
                    :10 "icon-white"}
             :red {:default "icon-red"
                   :0 "icon-black"
                   :1 "icon-red-900"
                   :2 "icon-red-800"
                   :3 "icon-red-700"
                   :4 "icon-red-600"
                   :5 "icon-red-500"
                   :6 "icon-red-400"
                   :7 "icon-red-300"
                   :8 "icon-red-200"
                   :9 "icon-red-100"
                   :10 "icon-white"}
             :explorama__window__group-1 "explorama__window__group-1"
             :explorama__window__group-2 "explorama__window__group-2"
             :explorama__window__group-3 "explorama__window__group-3"
             :explorama__window__group-4 "explorama__window__group-4"
             :explorama__window__group-5 "explorama__window__group-5"
             :explorama__window__group-6 "explorama__window__group-6"
             :explorama__window__group-7 "explorama__window__group-7"
             :explorama__window__group-8 "explorama__window__group-8"
             :explorama__window__group-9 "explorama__window__group-9"
             :explorama__window__group-10 "explorama__window__group-10"
             :explorama__window__group-11 "explorama__window__group-11"
             :explorama__window__group-12 "explorama__window__group-12"
             :explorama__window__group-13 "explorama__window__group-13"
             :explorama__window__group-14 "explorama__window__group-14"
             :explorama__window__group-15 "explorama__window__group-15"})

;;HINT: Use resources/public/css/read-icons.sh Script to extract icons from woco.css
(def icon-collection  {:union {:group "Operations" :class "icon-union"}
                       :intersect {:group "Operations" :class "icon-intersect"}
                       :difference {:group "Operations" :class "icon-difference"}
                       :symdiff {:group "Operations" :class "icon-symdiff"}
                       :filter {:group "Operations" :class "icon-filter"}
                       :reorder {:group "Operations" :class "icon-reorder"}
                       :sort-asc {:group "Operations" :class "icon-sort-asc"}
                       :sort-desc {:group "Operations" :class "icon-sort-desc"}
                       :sort_asc {:group "Operations" :class "icon-sort_asc"}
                       :timeline {:group "Operations" :class "icon-timeline"}
                       :sort-by {:group "Operations" :class "icon-sort"}
                       :group-by {:group "Operations" :class "icon-group"}
                       :related-by {:group "Operations" :class "icon-related"}
                       :grid {:group "Operations" :class "icon-grid"}
                       :scatter-plot {:group "Operations" :class "icon-scatter-plot"}
                       :scatter {:group "Operations" :class "icon-scatter"}
                       :copy {:group "Operations" :class "icon-copy"}
                       :replace {:group "Operations" :class "icon-replace"}
                       :arrangeh {:group "Operations" :class "icon-arrangeh"}
                       :arrangev {:group "Operations" :class "icon-arrangev"}
                       :group {:group "Operations", :class "icon-group"}
                       :subgroup {:group "Operations", :class "icon-subgroup"}
                       :ungroup {:group "Operations", :class "icon-ungroup"}
                       :unsubgroup {:group "Operations", :class "icon-unsubgroup"}
                       :sort {:group "Operations", :class "icon-sort"}
                       :sort-group {:group "Operations", :class "icon-sort-group"}
                       :sort-subgroup {:group "Operations", :class "icon-sort-subgroup"}
                       :couple {:group "Operations", :class "icon-couple"}
                       :uncouple {:group "Operations", :class "icon-uncouple"}
                       :move-window {:group "Operations", :class "icon-move-window"}

                       :maximize {:group "Frame" :class "icon-maximize"}
                       :minimize {:group "Frame" :class "icon-minimize"}
                       :win-maximize {:group "Frame" :class "icon-window-max"}
                       :win-minimize {:group "Frame" :class "icon-window-min"}
                       :win-multiple {:group "Frame" :class "icon-window-multiple"}
                       :close {:group "Frame" :class "icon-close"}
                       :close2 {:group "Frame" :class "icon-close2"}
                       :cog {:group "Frame" :class "icon-cog"}
                       :cog1 {:group "Frame" :class "icon-cog1"}
                       :cog2 {:group "Frame" :class "icon-cog2"}
                       :compress {:group "Frame" :class "icon-compress"}
                       :compress1 {:group "Frame" :class "icon-compress1"}
                       :expand {:group "Frame" :class "icon-expand"}
                       :expand1 {:group "Frame" :class "icon-expand1"}
                       :maximizewindow {:group "Frame" :class "icon-maximizewindow"}
                       :window-max {:group "Frame", :class "icon-window-max"}
                       :minimize1 {:group "Frame", :class "icon-minimize1"}
                       :maximize1 {:group "Frame", :class "icon-maximize1"}
                       :w-multiple {:group "Frame", :class "icon-w-multiple"}
                       :window-multiple {:group "Frame", :class "icon-window-multiple"}
                       :wmax {:group "Frame", :class "icon-wmax"}
                       :window-min {:group "Frame", :class "icon-window-min"}
                       :w-min {:group "Frame", :class "icon-w-min"}
                       :w-max {:group "Frame", :class "icon-w-max"}
                       :window-link {:group "Frame" :class "icon-window-link"}

                       :anchor {:group "Verticals" :class "icon-anchor"}
                       :charts {:group "Verticals" :class "icon-charts"}
                       :mosaic {:group "Verticals" :class "icon-mosaic"}
                       :mosaic2 {:group "Verticals" :class "icon-mosaic2"}
                       :head-cogs {:group "Verticals" :class "icon-head-cogs"}
                       :map {:group "Verticals" :class "icon-map"}
                       :cogs {:group "Verticals" :class "icon-cogs"}
                       :search {:group "Verticals" :class "icon-search"}
                       :woco {:group "Verticals" :class "icon-woco"}
                       :table {:group "Verticals" :class "icon-table"}
                       :sherlock {:group "Verticals" :class "icon-sherlock"}
                       :atlas {:group "Verticals" :class "icon-atlas"}
                       :tempimport {:group "Verticals", :class "icon-tempimport"}
                       :explorama {:group "Verticals", :class "icon-explorama"}
                       :prediction {:group "Verticals", :class "icon-prediction"}
                       :indicator {:group "Verticals", :class "icon-indicator"}
                       :note {:group "Verticals", :class "icon-note"}

                       :card1 {:group "Objectcards" :class "icon-card1"}
                       :card2 {:group "Objectcards" :class "icon-card2"}
                       :card3 {:group "Objectcards" :class "icon-card3"}
                       :nolayout_1 {:group "Objectcards" :class "icon-nolayout_1"}
                       :nolayout_2 {:group "Objectcards" :class "icon-nolayout_2"}
                       :nolayout_3 {:group "Objectcards" :class "icon-nolayout_3"}
                       :objectcard {:group "Objectcards" :class "icon-objectcard"}
                       :nolayout_info {:group "Objectcards", :class "icon-nolayout_info"}

                       :mosaic-calendar {:group "mosaic", :class "icon-mosaic-calendar"}
                       :mosaic-charts {:group "mosaic", :class "icon-mosaic-charts"}
                       :mosaic-circle {:group "mosaic", :class "icon-mosaic-circle"}
                       :mosaic-city {:group "mosaic", :class "icon-mosaic-city"}
                       :mosaic-clock {:group "mosaic", :class "icon-mosaic-clock"}
                       :mosaic-coin {:group "mosaic", :class "icon-mosaic-coin"}
                       :mosaic-drop {:group "mosaic", :class "icon-mosaic-drop"}
                       :mosaic-euro {:group "mosaic", :class "icon-mosaic-euro"}
                       :mosaic-flame {:group "mosaic", :class "icon-mosaic-flame"}
                       :mosaic-globe {:group "mosaic", :class "icon-mosaic-globe"}
                       :mosaic-globe2 {:group "mosaic", :class "icon-mosaic-globe2"}
                       :mosaic-group {:group "mosaic", :class "icon-mosaic-group"}
                       :mosaic-health {:group "mosaic", :class "icon-mosaic-health"}
                       :mosaic-heart {:group "mosaic", :class "icon-mosaic-heart"}
                       :mosaic-info {:group "mosaic", :class "icon-mosaic-info"}
                       :mosaic-leaf {:group "mosaic", :class "icon-mosaic-leaf"}
                       :mosaic-map {:group "mosaic", :class "icon-mosaic-map"}
                       :mosaic-note {:group "mosaic", :class "icon-mosaic-note"}
                       :mosaic-percentage {:group "mosaic", :class "icon-mosaic-percentage"}
                       :mosaic-pin {:group "mosaic", :class "icon-mosaic-pin"}
                       :mosaic-rain {:group "mosaic", :class "icon-mosaic-rain"}
                       :mosaic-search {:group "mosaic", :class "icon-mosaic-search"}
                       :mosaic-star {:group "mosaic", :class "icon-mosaic-star"}
                       :mosaic-sun {:group "mosaic", :class "icon-mosaic-sun"}
                       :mosaic-transfer {:group "mosaic", :class "icon-mosaic-transfer"}

                       :add-project {:group "Projects" :class "icon-add-project"}

                       :charts-bar {:group "Charts" :class "icon-charts-bar"}
                       :charts-bubble {:group "Charts" :class "icon-charts-bubble"}
                       :charts-line {:group "Charts" :class "icon-charts-line"}
                       :charts-pie {:group "Charts" :class "icon-charts-pie"}
                       :charts-scatter {:group "Charts" :class "icon-charts-scatter"}
                       :charts-wordcloud {:group "Charts" :class "icon-charts-wordcloud"}
                       :charts-wordcloud2 {:group "Charts" :class "icon-charts-wordcloud2"}
                       :charts-wordcloud3 {:group "Charts" :class "icon-charts-wordcloud3"}

                       :type-string {:group "Types" :class "icon-type-string"}
                       :type-number {:group "Types" :class "icon-type-number"}
                       :type-date {:group "Types" :class "icon-type-date"}

                       :error {:group "Status", :class "icon-error"}
                       :warning {:group "Status" :class "icon-warning"}
                       :info {:group "Status" :class "icon-info"}
                       :info-circle {:group "Status" :class "icon-info-circle"}
                       :question-circle {:group "Status" :class "icon-question-circle"}
                       :info-square {:group "Status" :class "icon-info-square"}
                       :check {:group "Status" :class "icon-check"}
                       :check-square {:group "Status" :class "icon-check-square"}
                       :notallowed {:group "Status" :class "icon-notallowed"}

                       :back {:group "Arrows" :class "icon-back"}
                       :trend-down {:group "Arrows" :class "icon-trend-down"}
                       :trend-none {:group "Arrows" :class "icon-trend-none"}
                       :trend-up {:group "Arrows" :class "icon-trend-up"}
                       :previous {:group "Arrows", :class "icon-previous"}
                       :snapshot-arrow {:group "Arrows", :class "icon-snapshot-arrow"}
                       :snapshot-arrow-n {:group "Arrows", :class "icon-snapshot-arrow-n"}
                       :snapshot-arrow-pfeil {:group "Arrows", :class "icon-snapshot-arrow-pfeil"}
                       :arrow-down {:group "Arrows" :class "icon-arrow-down"}
                       :arrow-up {:group "Arrows" :class "icon-arrow-up"}
                       :next {:group "Arrows" :class "icon-next"}
                       :prev {:group "Arrows" :class "icon-prev"}
                       :play {:group "Arrows" :class "icon-play"}
                       :chevron-up {:group "Arrows" :class "icon-chevron-up"}
                       :chevron-down {:group "Arrows" :class "icon-chevron-down"}
                       :chevron-left {:group "Arrows" :class "icon-chevron-left"}
                       :chevron-right {:group "Arrows" :class "icon-chevron-right"}
                       :collapse-closed {:group "Arrows" :class "icon-collapse-closed"}
                       :collapse-open {:group "Arrows" :class "icon-collapse-open"}
                       :collapse {:group "Arrows" :class "icon-collapse"}
                       :collapse2 {:group "Arrows" :class "icon-collapse2"}
                       :mosaic-scale-content {:group "Arrows" :class "icon-mosaic-scale-content"}
                       :mosaic-scale-window {:group "Arrows" :class "icon-mosaic-scale-window"}
                       :mosaicscalecontent {:group "Arrows" :class "icon-mosaicscalecontent"}
                       :shuffle {:group "Arrows" :class "icon-shuffle"}
                       :indicator-arrow {:group "Arrows" :class "icon-indicator_arrow"}
                       :drag-indicator {:group "Arrows" :class "icon-drag-indicator"}

                       :note-color {:group "Notes" :class "icon-note-color"}
                       :color-circle {:group "Notes" :class "color-circle"}
                       :text-color {:group "Notes" :class "icon-text-color" :sub-icon :color-bar}
                       :custom-font-size {:group "Notes" :class "icon-text-color"} ;;TODO r1/icons do we need a custom icon for this
                       :font-size {:group "Notes" :class "icon-font-size"}
                       :highlight-color {:group "Notes" :class "icon-highlight-color" :sub-icon :color-bar}
                       :color-bar {:group "Notes" :class "icon-color-bar"}
                       :bold {:group "Notes" :class "icon-bold"}
                       :italics {:group "Notes" :class "icon-italics"}
                       :underlined {:group "Notes" :class "icon-underlined"}
                       :strikethrough {:group "Notes" :class "icon-strikethrough"}
                       :remove-formatting {:group "Notes" :class "icon-remove-formatting"}
                       :align-left {:group "Notes" :class "icon-align-left"}
                       :align-center {:group "Notes" :class "icon-align-center"}
                       :align-right {:group "Notes" :class "icon-align-right"}
                       :align-justified {:group "Notes" :class "icon-align-justified"}
                       :list-bulleted {:group "Notes" :class "icon-list-bulleted"}
                       :list-numbered {:group "Notes" :class "icon-list-numbered"}

                       :save-search {:group "Uncategorized" :class "icon-save-search"}
                       :menu {:group "Uncategorized" :class "icon-menu"}
                       :bars {:group "Uncategorized" :class "icon-bars"}
                       :bars2 {:group "Uncategorized" :class "icon-bars2"}
                       :pin {:group "Uncategorized" :class "icon-pin"}
                       :unpin {:group "Uncategorized" :class "icon-unpin"}
                       :bell {:group "Uncategorized" :class "icon-bell"}
                       :book {:group "Uncategorized" :class "icon-book"}
                       :broom {:group "Uncategorized" :class "icon-broom"}
                       :brush {:group "Uncategorized" :class "icon-brush"}
                       :burgermenu {:group "Uncategorized" :class "icon-burgermenu"}
                       :camera {:group "Uncategorized" :class "icon-camera"}
                       :count-green {:group "Uncategorized" :class "icon-count-green"}
                       :count-grey {:group "Uncategorized" :class "icon-count-grey"}
                       :count-red {:group "Uncategorized" :class "icon-count-red"}
                       :count-yellow {:group "Uncategorized" :class "icon-count-yellow"}
                       :database {:group "Uncategorized" :class "icon-database"}
                       :envelope {:group "Uncategorized" :class "icon-envelope"}
                       :eye-plus {:group "Uncategorized" :class "icon-eye-plus"}
                       :eye-slash {:group "Uncategorized" :class "icon-eye-slash"}
                       :eye {:group "Uncategorized" :class "icon-eye"}
                       :file-text {:group "Uncategorized" :class "icon-file-text"}
                       :download {:group "Uncategorized" :class "icon-download"}
                       :history {:group "Uncategorized" :class "icon-history"}
                       :focus {:group "Uncategorized" :class "icon-focus"}
                       :folder-open {:group "Uncategorized" :class "icon-folder-open"}
                       :home {:group "Uncategorized" :class "icon-home"}
                       :language {:group "Uncategorized" :class "icon-language"}
                       :magic {:group "Uncategorized" :class "icon-magic"}
                       :plus {:group "Uncategorized" :class "icon-plus"}
                       :minus {:group "Uncategorized" :class "icon-minus"}
                       :model {:group "Uncategorized" :class "icon-model"}
                       :reset {:group "Uncategorized" :class "icon-reset"}
                       :save {:group "Uncategorized" :class "icon-save"}
                       :select {:group "Uncategorized" :class "icon-select"}
                       :share {:group "Uncategorized" :class "icon-share"}
                       :sign-out {:group "Uncategorized" :class "icon-sign-out"}
                       :start {:group "Uncategorized" :class "icon-start"}
                       :trash {:group "Uncategorized" :class "icon-trash"}
                       :user {:group "Uncategorized" :class "icon-user"}
                       :users {:group "Uncategorized" :class "icon-users"}
                       :user-edit {:group "Uncategorized" :class "icon-user-edit"}
                       :edit {:group "Uncategorized" :class "icon-edit"}
                       :drop {:group "Uncategorized" :class "icon-drop"}
                       :infosettings {:group "Uncategorized" :class "icon-infosettings"}
                       :infosettings2 {:group "Uncategorized" :class "icon-infosettings2"}
                       :infosettings3 {:group "Uncategorized" :class "icon-infosettings3"}
                       :path {:group "Uncategorized" :class "icon-Path"}
                       :star-o {:group "Uncategorized" :class "icon-star-o"}
                       :star {:group "Uncategorized" :class "icon-star"}
                       :expand-list {:group "Uncategorized" :class "icon-expand-list"}
                       :searchlist {:group "Uncategorized" :class "icon-searchlist"}
                       :palette {:group "Uncategorized", :class "icon-palette"}
                       :file {:group "Uncategorized", :class "icon-file"}
                       :related {:group "Uncategorized", :class "icon-related"}
                       :save2 {:group "Uncategorized", :class "icon-save2"}
                       :file-empty {:group "Uncategorized", :class "icon-file-empty"}
                       :protokoll {:group "Uncategorized", :class "icon-protokoll"}
                       :layer {:group "Uncategorized", :class "icon-layer"}
                       :file-download {:group "Uncategorized", :class "icon-file-download"}
                       :graph {:group "Uncategorized", :class "icon-graph"}
                       :iva-right {:group "Uncategorized", :class "icon-iva-right"}
                       :upload {:group "Uncategorized", :class "icon-upload"}
                       :lock {:group "Uncategorized", :class "icon-lock"}
                       :open-new-tab {:group "Uncategorized" :class "icon-open-new-tab"}
                       :open-new-tab2 {:group "Uncategorized" :class "icon-open-new-tab2"}
                       :image {:group "Uncategorized" :class "icon-image"}
                       :report {:group "Uncategorized" :class "icon-report"}
                       :marker {:group "Uncategorized" :class "icon-marker"}
                       :comment-empty {:group "Uncategorized" :class "icon-speech-bubble"}
                       :comment-text {:group "Uncategorized" :class "icon-speech-bubble-text"}
                       :calender {:group "Uncategorized" :class "icon-calendar"}
                       :tour {:group "Uncategorized" :class "icon-tour"}
                       :compass {:group "Uncategorized" :class "icon-compass"}
                       :search-guided {:group "Uncategorized" :class "icon-search-guided"}
                       :search-free {:group "Uncategorized" :class "icon-search-free"}
                       :user-cursor {:group "Uncategorized" :class "icon-user-cursor"}
                       :details-view {:group "Uncategorized" :class "icon-details-view"}

                       :magnet {:group "Uncategorized" :class "icon-magnet"}
                       :window-list {:group "Uncategorized" :class "icon-window-list"}
                       :fit-width {:group "Uncategorized" :class "icon-fit-width"}
                       :fit-height {:group "Uncategorized" :class "icon-fit-height"}
                       :minimap {:group "Uncategorized" :class "icon-minimap"}
                       :window {:group "Uncategorized" :class "icon-window"}
                       :grid-lines {:group "Uncategorized" :class "icon-grid-lines"}

                       :pause {:group "Uncategorized" :class "icon-pause"}
                       :drag-5 {:group "Uncategorized" :class "icon-drag-5"}

                       :window-plus {:group "Uncategorized" :class "icon-window-plus"}
                       :mouse {:group "Uncategorized" :class "icon-mouse"}

                       :treemap-settings {:group "Uncategorized" :class "icon-treemap-settings"}
                       :treemap {:group "Uncategorized" :class "icon-treemap"}})

(def parameter-definition
  {:icon {:type [:string :keyword]
          :required true
          :desc "Its recommanded to use a keyword. If its a string it has to be an css class, if its a keyword the css-class will get from icon-collection. See icon-collection to which are provided"}
   :size {:type [:number :keyword]
          :characteristics (vec (sort (keys sizes)))
          :desc "Size of the icon."}
   :color {:type [:string :keyword]
           :characteristics (vec (sort (keys colors)))
           :desc "Color of the icon. Strings will be changed to keywords automatically"}
   :custom-color {:type :string
                  :desc "Color which will be set as background-color style. If :color and :custom-color is set, than the :color will win"}
   :color-important? {:type :boolean
                      :desc "Overwrite the default color with the color attribute."}
   :brightness {:type [:number]
                :desc "Brightness of the icon. Range 0 to 10 "}
   :tooltip {:type [:derefable :string]
             :desc "String which will be visibile if you hover over the icon"}
   :tooltip-extra-params {:type :map
                          :desc "Parameters for tooltip-component see tooltip for more information."}
   :extra-class {:type [:string :vector]
                 :desc "Extra class or classes which will added to icon element"}})
(def specification (parameters->malli parameter-definition nil))

(defn- icon- [params]
  (let [{:keys [size color custom-color brightness extra-class color-important?] ico :icon}
        (merge default-parameters params)
        icon-class (if (string? ico)
                     ico
                     (get-in icon-collection [ico :class]))
        sub-icon (when icon-class
                   (get-in icon-collection [ico :sub-icon]))
        sub-icon-class (when sub-icon
                         (get-in icon-collection [sub-icon :class]))
        color (cond-> color
                (string? color) (keyword))
        color-swatch (get colors color)
        color (or (get color-swatch (keyword (str brightness)))
                  (get color-swatch :default)
                  color-swatch)]
    (if-not icon-class
      [:<>]
      [:<>
       [:span {:class (cond-> [icon-class]
                        (and color-important?
                             (not sub-icon-class))
                        (conj (str color "-important"))
                        (and (not color-important?)
                             (not sub-icon-class))
                        (conj color)
                        (and extra-class (string? extra-class))
                        (conj extra-class)
                        (and extra-class (vector? extra-class))
                        (concat extra-class)
                        (keyword? size)
                        (conj (get sizes size))
                        :always vec)
               :style (cond-> {}
                        (number? size)
                        (assoc :width size
                               :height size)
                        (and (not color)
                             (string? custom-color))
                        (assoc :background-color custom-color))}]

       (when sub-icon-class
         [:span {:class (cond-> [sub-icon-class "absolute" "top-8"]
                          color-important? (conj (str color "-important"))
                          (not color-important?) (conj color))}])])))

(defn- with-tooltip [tooltip-params params]
  [tooltip tooltip-params
   [icon- params]])

(defn ^:export icon [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "icon" specification params)}
     (let [{:keys [tooltip-extra-params] ico :icon
            tooltip-text :tooltip}
           params
           icon-class (if (string? ico)
                        ico
                        (get-in icon-collection [ico :class]))
           contains-tooltip? (contains? params :tooltip)
           tooltip-params (merge tooltip-extra-params
                                 {:text tooltip-text})]
       (cond
         (not icon-class) [:<>]
         contains-tooltip? [with-tooltip tooltip-params params]
         :else [icon- params]))]))
