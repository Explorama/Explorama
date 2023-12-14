(ns de.explorama.frontend.woco.notifications-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [de.explorama.frontend.woco.api.notifications :as notifications]))

(deftest show-notification-test
  (testing "tests show-condition: all"
    (is (notifications/show-notification?
         "*"
         {:category {:projects :blub}}))
    (is (notifications/show-notification?
         true
         {:category {:projects :blub}}))
    (is (not (notifications/show-notification?
              nil
              {:category {:projects :blub}})))
    (is (not (notifications/show-notification?
              false
              {:category {:projects :blub}}))))

  (testing "tests show-condition: all/none from specific category"
    (is (notifications/show-notification?
         {:projects "*"
          :network "*"
          :operations "*"}
         {:category {:projects :blub}}))
    (is (notifications/show-notification?
         {:projects true
          :network true
          :operations true}
         {:category {:projects :blub}}))
    (is (not (notifications/show-notification?
              {:projects nil
               :network true
               :operations true}
              {:category {:projects :blub}})))
    (is (not (notifications/show-notification?
              {:projects false
               :network true
               :operations true}
              {:category {:projects :blub}}))))

  (testing "tests show-condition: specific entries from category"
    (is (notifications/show-notification?
         {:network [:ws :i18n :config]}
         {:category {:network :i18n}}))
    (is (notifications/show-notification?
         {:network [:i18n]}
         {:category {:network :i18n}}))
    (is (not (notifications/show-notification?
              {:network [:ws :config]}
              {:category {:network :i18n}})))
    (is (not (notifications/show-notification?
              {:network []}
              {:category {:network :i18n}})))
    (is (not (notifications/show-notification?
              {:projects "*"}
              {:category {:network :i18n}})))))