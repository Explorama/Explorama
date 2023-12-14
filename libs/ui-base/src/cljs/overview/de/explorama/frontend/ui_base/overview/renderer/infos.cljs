(ns de.explorama.frontend.ui-base.overview.renderer.infos
  (:require [clojure.string :refer [join] :as string]
            [reagent.core :as reagent]
            [reagent.dom :as dom]
            [cljsjs.highlight]
            [cljsjs.highlight.langs.clojure]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli-str]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [section]]))

(defn sort-parameters [parameters]
  (let [{tv true fv false nv nil :as groups} (group-by #(get-in % [1 :required])
                                                       parameters)]
    (-> groups
        (dissoc true false nil) ;keep only groups
        (vals)
        (as-> g (apply concat g))
        (into tv) ;list: add required to front
        (vec)
        (into nv) ;vec: add nils then false to end
        (into fv))))

(defn- detailed-infos [{:keys [parameters]} component-default]
  (when-let [parameters (sort-parameters parameters)]
    [:table.code
     {:tabIndex 0}
     [:thead
      [:tr
       [:th "Parameter"]
       [:th.normal "Type"]
       [:th.medium  "Default"]
       [:th.normal "Description"]]]
     (reduce (fn [par [param {:keys [type characteristics required require-cond desc default-fn-str]}]]
               (conj par
                     [:tr
                      [:td
                       [:span.parameter (name param)]]
                      [:td (str type)]
                      (let [dval (get component-default param "")]
                        [:td (when (or default-fn-str (fn? dval))
                               {:class "func-def"})
                         (let [dval (get component-default param "")]
                           (if (or default-fn-str (fn? dval))
                             [:code default-fn-str]
                             (str dval)))])
                      (let [[ckey cvalue] require-cond
                            cond-str (cond
                                       (= :* cvalue) (str ckey)
                                       (vector? cvalue) (str ckey " value in " (set cvalue))
                                       :else (str ckey " = " cvalue))
                            cond-block (when require-cond
                                         [:<> ", when "
                                          [:b {:style {:color "#226f06"}}
                                           cond-str]])]
                        [:td
                         (cond
                           (keyword required)
                           [:<>
                            "One of the "
                            [:b {:style {:color "blue"}}
                             required]
                            " options is required"
                            cond-block
                            [:br]]
                           required
                           [:<>
                            "This option is "
                            [:b {:style {:color "#db3333"}}
                             "required"]
                            cond-block
                            [:br]]
                           :else
                           [:<>])
                         desc
                         (when characteristics
                           [:<>
                            [:br]
                            "Can be: "
                            [:br]
                            [:code (join ", " characteristics)]])])]))

             [:tbody]
             parameters)]))

(defn- component-infos [{:keys [name desc is-sub?]}]
  [:div.component-infos
   (if is-sub?
     [:h3 {:style {:text-transform :capitalize}}
      name]
     [:h2 {:style {:text-transform :capitalize}}
      name])
   [:div desc]])

(defn- repo []
  [:a {:href "https://gitlab.com/explorama/utility/ui-base"
       :target "_blank"}
   "Ui-Base Gitlab Repository"])

(defn md-code [code]
  (let [r (reagent/atom nil)]
    (reagent/create-class
     {:component-did-mount (fn []
                             (when @r
                               (.highlightElement js/hljs (dom/dom-node @r))))
      :component-did-update (fn []
                              (when @r
                                (.highlightElement js/hljs (dom/dom-node @r))))
      :reagent-render
      (fn [code]
        [:pre {:class "clojure"}
         [:code {:ref #(reset! r %)
                 :tabIndex 0}
          code]])})))

(defn rd-infos
  ([{:keys [show-gitlab-link? details-label name require-statement namespace is-sub? sub
            parameters sub-parameters]
     :or {show-gitlab-link? true
          details-label "Parameters"}
     :as infos-desc}
    component-default]
   [:<>
  ; (when show-gitlab-link?
   ; [repo])
    [component-infos infos-desc]
    [:div {:style {:padding "16px"
                   :margin-bottom "85px"}}
     [:h3 {:id (cond-> (str namespace "api")
                 is-sub? (str "-" name))}
      "API"]
     (when require-statement
       (with-meta
         [md-code (str "(:require " require-statement ")")]
         {:key (str namespace "_" name "_ api")}))
     (when parameters
       [:<>
        [section {:default-open? true
                  :label [:b {:style {:font-size "16px"
                                      :margin-left "5px"}}
                          details-label]}
         [detailed-infos infos-desc component-default]]
        [section {:default-open? false
                  :label [:b {:style {:font-size "16px"
                                      :margin-left "5px"}}
                          "Specification"]}
         [md-code (parameters->malli-str parameters sub-parameters)]]])]
    (when sub
      (reduce (fn [res {:keys [default-parameters] :as item}] (conj res [rd-infos item default-parameters])) [:<>] sub))])
  ([infos-desc]
   (rd-infos infos-desc {})))
