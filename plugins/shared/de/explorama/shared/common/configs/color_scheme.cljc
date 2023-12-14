(ns de.explorama.shared.common.configs.color-scheme
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :refer [starts-with?]]))

;; Example
;; (def color-scheme-1
;;   {:name "Neutral-1-5"
;;    :id "colorscale1"
;;    :color-scale-numbers 5
;;    :colors {:0 "#0093dd"
;;             :1 "#005ca1"
;;             :2 "#28166f"
;;             :3 "#801d77"
;;             :4 "#dd137b"}})

(spec/def :colors/key keyword?)
(spec/def :colors/val (spec/and string?
                                (fn [s]
                                  (and (starts-with? s "#")
                                       ;;hex val like #0093dd
                                       (= 7 (count s))))))

(spec/def :color-scheme/id (spec/and string?
                                     #(seq %)))
(spec/def :color-scheme/name (spec/and string?
                                       #(seq %)))
(spec/def :color-scheme/color-scale-numbers number?)
(spec/def :color-scheme/colors (spec/and map?
                                         (spec/every-kv :colors/key :colors/val)
                                         #(seq %)))

(spec/def :color-scheme/desc
  (spec/keys
   :req-un [:color-scheme/colors]
   ;;optional because self-created color-selections will not have this
   :opt-un [:color-scheme/id
            :color-scheme/name
            :color-scheme/color-scale-numbers]))