(ns de.explorama.shared.configuration.defaults)

(defn- prepare-config-format
  ([coll color-map]
   (into {}
         (map #(vector (:id %)
                       (if color-map
                         (update-in % [:color-scheme]
                                    (fn [a] (if (map? a)
                                              a
                                              (get color-map a))))
                         %))
              coll)))
  ([coll]
   (prepare-config-format coll nil)))

(declare default-colors)
(declare default-overlayers)

(defn load-defaults [_user-info config-types]
  (select-keys {:color-scales default-colors
                :default-overlayers default-overlayers}
               config-types))

(def default-colors
  (prepare-config-format
   [{:name "Neutral-1-5"
     :id "colorscale1"
     :color-scale-numbers 5
     :colors {:0 "#0093dd"
              :1 "#005ca1"
              :2 "#28166f"
              :3 "#801d77"
              :4 "#dd137b"}}
    {:name "Scale-1-5"
     :id "colorscale2"
     :color-scale-numbers 5
     :colors {:0 "#4fb34f"
              :1 "#0292b5"
              :2 "#033579"
              :3 "#fb8d02"
              :4 "#e33b3b"}}
    {:name "Scale-2-5"
     :id "colorscale3"
     :color-scale-numbers 5
     :colors {:0 "#babab9"
              :1 "#5c5b5b"
              :2 "#50678a"
              :3 "#fb8d02"
              :4 "#e33b3b"}}
   ;; Accessibility oriented color scales
    {:name "Qualitative-5"
     :id "access-colorscale1"
     :color-scale-numbers 5
     :colors {:0 "#4477aa"
              :1 "#ee6677"
              :2 "#228833"
              :3 "#ccbb44"
              :4 "#66ccee"}}
    {:name "Diverging-5"
     :id "access-colorscale2"
     :color-scale-numbers 5
     :colors {:0 "#364b9a"
              :1 "#6ea6cd"
              :2 "#eaeccc"
              :3 "#f67e4b"
              :4 "#a50026"}}
    {:name "Sequential-5"
     :id "access-colorscale3"
     :color-scale-numbers 5
     :colors {:0 "#fff7bc"
              :1 "#fec44f"
              :2 "#ec7014"
              :3 "#cc4c02"
              :4 "#662506"}}]))
