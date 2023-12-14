(ns de.explorama.frontend.mosaic.data-structure.nested-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [de.explorama.frontend.mosaic.render.parameter :refer [calc-leaf->root]]))

(deftest leaf->root
  (testing "leaf->root plain"
    (is (= {[] {:element-type :leaf
                :event-count 1
                :event-count-max 1
                :group-count 1
                :group-count-max 1}}
           (calc-leaf->root (clj->js [["#5c5b5b" "1" "default"]])))))
  (testing "leaf->root plain"
    (is (= {[] {:element-type :leaf
                :event-count 2
                :event-count-max 2
                :group-count 1
                :group-count-max 1}}
           (calc-leaf->root (clj->js [["#5c5b5b" "1" "default"] ["#5c5b5b" "2" "default"]])))))
  (testing "leaf->root plain"
    (is (= {[] {:element-type :leaf
                :event-count 3
                :event-count-max 3
                :group-count 1
                :group-count-max 1}}
           (calc-leaf->root (clj->js [["#5c5b5b" "1" "default"] ["#5c5b5b" "2" "default"] ["#5c5b5b" "3" "default"]])))))
  (testing "leaf->root group-by empty"
    (is (= {[] {:element-type :group
                :event-count 0
                :event-count-children-max 0
                :group-count 1
                :group-count-max 1}}
           (calc-leaf->root (clj->js [{:key :root
                                       :group-key? true}
                                      []])))))
  (testing "leaf->root group-by"
    (is (= {[] {:element-type :group
                :event-count 18
                :event-count-children-max 7
                :group-count 3
                :group-count-max 3}
            [0] {:element-type :leaf
                 :event-count 5
                 :event-count-max 5
                 :group-count 1
                 :group-count-max 1}
            [1] {:element-type :leaf
                 :event-count 6
                 :event-count-max 6
                 :group-count 1
                 :group-count-max 1}
            [2] {:element-type :leaf
                 :event-count 7
                 :event-count-max 7
                 :group-count 1
                 :group-count-max 1}}
           (calc-leaf->root (clj->js [{:key :root
                                       :group-key? true}
                                      [[{:key 1
                                         :group-key? true} (mapv (fn [a] ["#5c5b5b" (str a) "default"]) (range 5))]
                                       [{:key 2
                                         :group-key? true} (mapv (fn [a] ["#5c5b5b" (str a) "default"]) (range 6))]
                                       [{:key 3
                                         :group-key? true} (mapv (fn [a] ["#5c5b5b" (str a) "default"]) (range 7))]]])))))
  (testing "leaf->root sub-group-by"
    (is (= {[] {:element-type :group
                :event-count 34
                :event-count-children-max 18
                :group-count 3
                :group-count-max 3}

            [0] {:element-type :group
                 :event-count 11
                 :event-count-children-max 6
                 :group-count 2
                 :group-count-max 2}
            [1] {:element-type :group
                 :event-count 18
                 :event-count-children-max 7
                 :group-count 3
                 :group-count-max 3}
            [2] {:element-type :group
                 :event-count 5
                 :event-count-children-max 5
                 :group-count 1
                 :group-count-max 1}

            [0 0] {:element-type :leaf
                   :event-count 5
                   :event-count-max 5
                   :group-count 1
                   :group-count-max 1}
            [0 1] {:element-type :leaf
                   :event-count 6
                   :event-count-max 6
                   :group-count 1
                   :group-count-max 1}
            [1 0] {:element-type :leaf
                   :event-count 5
                   :event-count-max 5
                   :group-count 1
                   :group-count-max 1}
            [1 1] {:element-type :leaf
                   :event-count 6
                   :event-count-max 6
                   :group-count 1
                   :group-count-max 1}
            [1 2] {:element-type :leaf
                   :event-count 7
                   :event-count-max 7
                   :group-count 1
                   :group-count-max 1}
            [2 0] {:element-type :leaf
                   :event-count 5
                   :event-count-max 5
                   :group-count 1
                   :group-count-max 1}}
           (calc-leaf->root (clj->js [{:key :root
                                       :group-key? true}
                                      [[{:key 1
                                         :group-key? true} [[{:key 1
                                                              :group-key? true} (mapv (fn [a] ["#5c5b5b" (str a) "default"]) (range 5))]
                                                            [{:key 2
                                                              :group-key? true} (mapv (fn [a] ["#5c5b5b" (str a) "default"]) (range 6))]]]
                                       [{:key 2
                                         :group-key? true} [[{:key 1
                                                              :group-key? true} (mapv (fn [a] ["#5c5b5b" (str a) "default"]) (range 5))]
                                                            [{:key 2
                                                              :group-key? true} (mapv (fn [a] ["#5c5b5b" (str a) "default"]) (range 6))]
                                                            [{:key 3
                                                              :group-key? true} (mapv (fn [a] ["#5c5b5b" (str a) "default"]) (range 7))]]]
                                       [{:key 3
                                         :group-key? true} [[{:key 1
                                                              :group-key? true} (mapv (fn [a] ["#5c5b5b" (str a) "default"]) (range 5))]]]]]))))))
