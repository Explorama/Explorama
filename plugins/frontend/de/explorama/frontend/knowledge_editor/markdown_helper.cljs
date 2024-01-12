(ns de.explorama.frontend.knowledge-editor.markdown-helper
  (:require [markdown.core :as m]
            [hickory.core :as h]
            [clojure.string :as str]
            [clojure.walk :as walk]))

(def link-pattern #"//[a-zA-Z\-0-9]+/[a-zA-Z\-\ 0-9]+;")

(defn split-pattern [pattern]
  (-> (subs pattern 2)
      (str/replace ";"
                   "")
      (str/split "/")))

(defn- custom [text]
  (let [matches
        (loop [matches (re-seq link-pattern text)
               result {}]
          (if-let [match (first matches)]
            (let [[type name]
                  (split-pattern match)]
              (recur (rest matches)
                     (assoc result
                            match [:a {:style {:font-weight :bold}
                                       :on-click (fn [])}
                                   (if (= "title" type)
                                     name
                                     (str "" name " (context/" type ")"))])))
            result))]
    (if (seq matches)
      (loop [current text
             result [:<>]
             matches (seq matches)]
        (if (empty? matches)
          (if (seq current)
            (conj result current)
            result)
          (let [[old new] (first matches)
                ; this will break if you use the same context twice in line
                [covered remaining] (let [splitted (str/split current old)]
                                      (if (= 2 (count splitted))
                                        splitted
                                        [(first splitted) ""]))]
            (recur remaining
                   (conj result covered new)
                   (rest matches)))))
      text)))

(defn markdown->hiccup [markdown-text]
  (let [[_ _ & r] (-> (m/md->html markdown-text)
                      h/parse
                      h/as-hiccup
                      first
                      (get 3))
        pw (walk/postwalk (fn [n]
                            (if (string? n)
                              (custom n)
                              n))
                          r)]
    (into [:<>] pw)))
