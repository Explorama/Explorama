(ns de.explorama.frontend.reporting.data.templates
  (:require [re-frame.core :refer [dispatch reg-sub reg-event-db reg-event-fx]]
            [taoensso.timbre :refer [debug]]
            [de.explorama.frontend.reporting.paths.templates :as templates-path]
            [de.explorama.shared.reporting.ws-api :as ws-api]))

(def templates
  {"3_1" {:id "3_1"
          :name "Template 1"
          :desc "Zeile 1: 3 Spalten, Zeile 2: 1 Spalte"
          :timestamp 1627662367930
          :grid [3 2]
          :tiles [{:position [0 0]
                   :size [1 1]
                   :legend-position :bottom}
                  {:position [1 0]
                   :size [1 1]
                   :legend-position :bottom}
                  {:position [2 0]
                   :size [1 1]
                   :legend-position :bottom}
                  {:position [0 1]
                   :size [3 1]
                   :legend-position :right}]}
   "2_2_2" {:id "2_2_2"
            :name "Template 2"
            :desc "2 Zeilen; Tiles in 2er kästchen aufgeteilt"
            :timestamp 1627662367930
            :grid [3 2]
            :tiles [{:position [0 0]
                     :size [2 1]
                     :legend-position :right}
                    {:position [0 1]
                     :size [2 1]
                     :legend-position :right}
                    {:position [2 0]
                     :size [1 2]
                     :legend-position :bottom}]}
   "1_2_2_1" {:id "1_2_2_1"
              :name "Template 3"
              :desc "1 Zeile; 2 Spalten"
              :timestamp 1627662367930
              :grid [3 2]
              :tiles [{:position [0 0]
                       :size [1 1]
                       :legend-position :bottom}
                      {:position [1 0]
                       :size [2 1]
                       :legend-position :right}
                      {:position [0 1]
                       :size [2 1]
                       :legend-position :right}
                      {:position [2 1]
                       :size [1 1]
                       :legend-position :bottom}]}
   "1_2" {:id "1_2"
          :name "Template 4"
          :desc "1 Zeile; 2 Spalten"
          :timestamp 1627662367930
          :grid [3 2]
          :tiles [{:position [0 0]
                   :size [2 2]
                   :legend-position :right}
                  {:position [2 0]
                   :size [1 2]
                   :legend-position :bottom}]}
   "1_1" {:id "1_1"
          :name "Template 5"
          :desc "2 Zeilen; 2er höhe"
          :timestamp 1627662367930
          :grid [3 4]
          :tiles [{:position [0 0]
                   :size [3 2]
                   :legend-position :right}
                  {:position [0 2]
                   :size [3 2]
                   :legend-position :right}]}
   "1" {:id "1"
        :name "Template 6"
        :desc "1 Zeile; 4er höhe"
        :timestamp 1627662367930
        :grid [3 4]
        :tiles [{:position [0 0]
                 :size [3 4]
                 :legend-position :right}]}})

(defn all-templates [db]
  templates)

(reg-sub
 ::all-templates
 all-templates)

(defn template [db t-id]
  (get templates t-id))

(reg-sub
 ::template
 (fn [db [_ t-id]]
   (template db t-id)))
   


