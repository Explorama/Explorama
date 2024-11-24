(ns lib.utils.data-exchange-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [de.explorama.frontend.ui-base.utils.data-exchange :refer [file-extention]]))

(def example-look-up {"jpeg" :jpg
                      "jpg" :jpg
                      "PNG" :png})

(deftest file-extention-test
  (testing "testing file-extention"
    (is (= (file-extention "test-file.jpeg")
           (file-extention ".jpeg")
           (file-extention "test-file.jpeg" nil)
           "jpeg"))
    (is (= (file-extention "test-file.jpg")
           (file-extention "test-file.jpg" nil)
           "jpg"))
    (is (= (file-extention "test-file.png")
           (file-extention "test-file.png" nil)
           "png"))
    (is (= (file-extention "test-file.jpeg" example-look-up)
           (file-extention "test-file.jpg" example-look-up)
           :jpg))
    (is (= (file-extention "test-file.PNG" example-look-up)
           :png))
    (is (= (file-extention nil)
           (file-extention nil nil)
           (file-extention "")
           (file-extention "test-file")
           (file-extention "test-file.")
           (file-extention "jpeg")
           ""))))