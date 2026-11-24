(ns de.explorama.frontend.charts.charts.chartjs-test
  (:require ["chart.js/auto" :refer [Chart]]
            ["chartjs-adapter-date-fns"]
            [cljs.test :refer-macros [deftest testing is]]
            [de.explorama.frontend.charts.charts.utils :as cutils]
            [de.explorama.frontend.charts.config :as config]))

(def ^:private x-axis-time
  {:unit "month"
   :displayFormats {:year "yyyy"
                    :month "yyyy-MM"
                    :quarter "yyyy-MM"
                    :day "yyyy-MM-dd"}})

(defn- render [scale-x]
  (let [canvas (doto (js/document.createElement "canvas")
                 (aset "width" 600)
                 (aset "height" 400))]
    (.appendChild js/document.body canvas)
    (Chart. (.getContext canvas "2d")
            (clj->js {:type "line"
                      :data {:datasets [{:label "events"
                                         :data [{:x "2021-07-01" :y 1}
                                                {:x "2021-08-01" :y 2}
                                                {:x "2021-09-01" :y 3}]}]}
                      :options {:responsive false
                                :animation false
                                :scales {:x scale-x}}}))))

(deftest chartjs-interop
  (testing "the chart.js module exposes a constructible Chart with its
            controllers registered"
    (let [chart (render (cutils/x-axis nil ["a" "b" "c"] "month" :light))]
      (is (= "category" (.. chart -scales -x -type)))
      (is (pos? (count (.. chart -scales -x -ticks))))
      (.destroy chart)))

  (testing "a time axis formats its ticks, so the date-fns adapter reached the
            same chart.js instance the app renders with"
    (let [chart (render (cutils/x-axis x-axis-time [] "month" :light))
          labels (mapv #(.-label %) (.. chart -scales -x -ticks))]
      (is (= "time" (.. chart -scales -x -type)))
      (is (seq labels))
      (is (every? #(re-matches #"\d{4}-\d{2}" %) labels))
      (.destroy chart)))

  (testing "the axis line colour is read from the v4 border option"
    (let [chart (render (cutils/x-axis nil [] "month" :dark))]
      (is (= config/dark-mode-grid-color
             (.. chart -options -scales -x -border -color)))
      (.destroy chart))))
