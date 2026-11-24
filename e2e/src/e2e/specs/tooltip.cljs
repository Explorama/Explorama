(ns e2e.specs.tooltip
  (:require [e2e.registry :refer [defspec]]
            [e2e.pages.workspace :as ws]
            [promesa.core :as p]))

(defn- boxes-overlap? [a b]
  (and (< (.-x a) (+ (.-x b) (.-width b)))
       (< (.-x b) (+ (.-x a) (.-width a)))
       (< (.-y a) (+ (.-y b) (.-height b)))
       (< (.-y b) (+ (.-y a) (.-height a)))))

(defspec "hovering a tool palette entry shows its tooltip"
  (fn [page expect]
    (p/let [_ (ws/open-workspace page)
            trigger (.locator page "#tool-search")
            label (.getAttribute trigger "aria-label")
            _ (.hover trigger)
            popup (.locator page ".tooltip-popup")]
      (p/do
        (-> (expect popup) (.toBeVisible))
        (-> (expect popup) (.toHaveText label))))))

(defspec "the tooltip popup is portaled out to the document body"
  (fn [page expect]
    (p/do
      (ws/open-workspace page)
      (.hover (.locator page "#tool-search"))
      (-> (expect (.locator page "body > .tooltip-popup")) (.toBeVisible)))))

(defspec "the tooltip is offset clear of its trigger"
  (fn [page expect]
    (p/let [_ (ws/open-workspace page)
            trigger (.locator page "#tool-search")
            _ (.hover trigger)
            popup (.locator page ".tooltip-popup")
            _ (-> (expect popup) (.toBeVisible))
            trigger-box (.boundingBox trigger)
            popup-box (.boundingBox popup)]
      (when (boxes-overlap? trigger-box popup-box)
        (throw (js/Error. (str "tooltip overlaps its trigger: popup "
                               (js/JSON.stringify popup-box)
                               " trigger " (js/JSON.stringify trigger-box))))))))
