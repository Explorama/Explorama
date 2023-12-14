(ns de.explorama.frontend.mosaic.render.common
  (:require [de.explorama.frontend.common.frontend-interface :as fi]))

(defn min-zoom [card-width card-height card-margin width height]
  (* (min (/ height (+ card-margin card-margin card-height))
          (/ width (+ card-margin card-margin card-width)))
     1.3))

(defn max-zoom [card-width card-height card-margin cards-per-line cards-count width height]
  (let [card-space-x (+ card-width card-margin card-margin)
        card-space-y (+ card-height card-margin card-margin)
        all-cards-space-x (* card-space-x cards-per-line)
        all-cards-space-y (* card-space-y
                             (Math/ceil (/ cards-count
                                           cards-per-line)))
        new-zoom-x (/ width all-cards-space-x)
        new-zoom-y (/ height all-cards-space-y)]
    (min new-zoom-y new-zoom-x)))

(defn row-length
  "Gegeben ein Container mit einem inneren Seitenverhältnis (/ Höhe Breite)
container-ratio, der eine Anzahl g von Karten aufnehmen soll, deren
Platzbedarfes Seitenverhältnis card-ratio ist, wieviele Karten müssen mindestens
in eine Zeile passen, damit alle Karten in den Container passen?"
  [g container-ratio card-ratio]
  (let [n (->> (* g card-ratio (/ container-ratio))
               (.sqrt js/Math)
               (.ceil js/Math))]
    (if (> g (* n (.floor js/Math (* n container-ratio (/ card-ratio)))))
      (inc n)
      n)))

(defn calculate-cards-per-line
  ([cards]
   (+ 1 (calculate-cards-per-line cards {:width 1.5
                                         :height 1})))
  ([cards {:keys [width height]}]
   (row-length cards
               (/ height width)
               1.3))
  ([cards {:keys [width height]} {:keys [cwidth cheight]}]
   (row-length cards
               (/ height width)
               (/ cheight cwidth)))
  ([_cards {:keys [width]} {:keys [cwidth cmargin]} zoom]
   ;Etwas andere Berechnung, wenn nur die Breite aufgefüllt werden soll (rearrange)
   (let [card-space-x (+ cwidth cmargin cmargin)
         n (.floor js/Math
                   (/ width
                      (* zoom card-space-x)))]
     n)))

(defn data-interaction? [frame-id]
  @(fi/call-api :flags-sub frame-id :data-interaction?))
