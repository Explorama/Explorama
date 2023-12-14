(ns de.explorama.frontend.woco.details-view-test
  (:require [de.explorama.frontend.woco.details-view :as sut]
            [de.explorama.frontend.common.i18n :as i18n]
            [cljs.test :refer-macros [deftest is]]
            [de.explorama.frontend.woco.config :as config]))

;;TODO r1/test need to fix the re-frame/subscribe deref in prepare-content
#_(deftest prepare-content-test
  (with-redefs [i18n/default-language "en-GB"]
    (is (= "" (sut/prepare-content nil nil)))
    (is (= ":a" (sut/prepare-content nil :a)))
    (is (= "12" (sut/prepare-content nil 12)))
    (is (= "" (sut/prepare-content nil "")))
    (is (= "ize" (sut/prepare-content nil "ize")))
    (is (= ["eleje "
            [:a
             {:href "https://github.com/clojure/spec.alpha", :target "_blank"}
             "https://github.com/clojure/spec.alpha"]
            " kozepe "
            [:a
             {:href "https://github.com/clojure/spec.alpha", :target "_blank"}
             "https://github.com/clojure/spec.alpha"]
            " vege"]
           (sut/prepare-content nil "eleje https://github.com/clojure/spec.alpha kozepe https://github.com/clojure/spec.alpha vege")))
    (is (= "ize,bigyo" (sut/prepare-content nil "ize,bigyo")))
    (is (= "ize,bigyo" (sut/prepare-content nil ["ize,bigyo"])))
    (is (= "(0 1 2)" (sut/prepare-content nil (range 3))))
    (is (= "0, 1, 2" (sut/prepare-content nil (vec (range 3)))))
    (is (= "13.2, alma, korte" (sut/prepare-content nil [13.2 "alma" "korte"])))
    (is (= "Test1"
           (:content (sut/prepare-content "annotation" {"content" "Test1"
                                                        "author" "PAdmin"}))))
    (is (= "Test2"
           (:content (sut/prepare-content "annotation" {"content" "Test2"
                                                        "author" "PAdmin"
                                                        "editor" "mmeier"}))))))
