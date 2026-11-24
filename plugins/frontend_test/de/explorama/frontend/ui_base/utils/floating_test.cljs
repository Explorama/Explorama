(ns de.explorama.frontend.ui-base.utils.floating-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [de.explorama.frontend.ui-base.utils.floating :as floating]))

(deftest direction->placement-test
  (testing "the four tooltip directions map onto floating-ui placements"
    (is (= "top" (floating/direction->placement :up)))
    (is (= "bottom" (floating/direction->placement :down)))
    (is (= "left" (floating/direction->placement :left)))
    (is (= "right" (floating/direction->placement :right))))
  (testing "an unknown direction falls back to the :up default"
    (is (= "top" (floating/direction->placement :sideways)))
    (is (= "top" (floating/direction->placement nil)))))

(deftest placement->side-test
  (testing "a bare placement yields its side"
    (is (= :top (floating/placement->side "top")))
    (is (= :left (floating/placement->side "left"))))
  (testing "an aligned placement yields the side, not the alignment"
    (is (= :bottom (floating/placement->side "bottom-start")))
    (is (= :right (floating/placement->side "right-end"))))
  (testing "an unusable placement falls back to :top"
    (is (= :top (floating/placement->side nil)))
    (is (= :top (floating/placement->side "")))))
