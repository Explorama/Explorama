(ns de.explorama.frontend.ui-base.utils.virtual-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [de.explorama.frontend.ui-base.utils.virtual :as virtual]))

(deftest row-style-fixed-height
  (testing "returns the absolute-positioning style a consumer applies directly to its row element"
    (is (= {:position "absolute"
            :top 100
            :left 0
            :width "100%"
            :height 20}
           (virtual/row-style 100 20 false)))))

(deftest row-style-measured
  (testing "omits height so the element can size to its content"
    (let [style (virtual/row-style 100 20 true)]
      (is (not (contains? style :height)))
      (is (= 100 (:top style)))
      (is (= "100%" (:width style)))))
  (testing "a zero offset is still emitted"
    (is (= 0 (:top (virtual/row-style 0 20 true))))))

(deftest cell-style-positions-both-axes
  (is (= {:position "absolute"
          :top 60
          :left 240
          :width 120
          :height 30}
         (virtual/cell-style 240 120 60 30))))

(deftest sizer-style-variants
  (testing "vertical only"
    (is (= {:position "relative" :height 500}
           (virtual/sizer-style nil 500))))
  (testing "horizontal only"
    (is (= {:position "relative" :width 1200}
           (virtual/sizer-style 1200 nil))))
  (testing "both axes"
    (is (= {:position "relative" :width 1200 :height 500}
           (virtual/sizer-style 1200 500))))
  (testing "a zero extent is emitted, not dropped"
    (is (= {:position "relative" :width 0 :height 0}
           (virtual/sizer-style 0 0)))))
