(ns de.explorama.frontend.ui-base.overview.renderer.page
  (:require [clojure.string :refer [join split includes? index-of replace trim blank?]]
            [re-frame.core :as rf]
            [de.explorama.frontend.ui-base.overview.renderer.db :as db]
            [de.explorama.frontend.ui-base.overview.renderer.menu :as m]
            [de.explorama.frontend.ui-base.overview.renderer.infos :as infos :refer [rd-infos md-code]]
            [markdown.core :refer [md->html]]
            [fipp.clojure :as fipp]
            [cljs.reader :refer [read-string]])
  (:require-macros [de.explorama.frontend.ui-base.overview.page]))

(def relevant-path "ui_base\\overview")
(def relevant-path-lib "ui_base\\components")
(def relevant-path-functions-lib "ui_base\\utils")
(def relevant-ns "ui-base.overview")
(def relevant-ns-lib "ui-base.components")
(def full-path "src\\cljs\\overview")

(defn extract-namespace [filename path replace-underscore?]
  (let [idx (index-of filename path)]
    (cond-> (-> (subs filename
                      (+ 1
                         idx
                         (count path)))
                (replace #".cljs" "")
                (replace #"\\" "."))
      replace-underscore? (replace #"_" "-"))))

(defn reset-components [changed-files]
  (doseq [{cfile :file} changed-files]
    (let [[category component] (split
                                (extract-namespace cfile (cond
                                                           (includes? cfile "components")
                                                           relevant-path-lib
                                                           (includes? cfile "utils")
                                                           relevant-path-functions-lib
                                                           :else
                                                           relevant-path)
                                                   true)
                                #"\.")]
      (rf/dispatch [::db/reset-component category component]))))

(defn- pprint [code]
  (with-out-str (fipp/pprint (read-string code)
                             {:width 100})))

(defn add-doc [{:keys [signatures name title fn-metas is-function? section] :as infos}]
  (let [[category component] (split
                              (extract-namespace (:namespace infos) relevant-ns true)
                              #"\.")
        title (or name title (str (:name fn-metas)))
        signatures (if (vector? signatures)
                     signatures
                     (vec (:arglists fn-metas)))]
    (rf/dispatch [::db/add-component
                  category
                  component
                  (assoc infos
                         :is-function? (not (false? is-function?))
                         :signatures signatures
                         :section (or section db/functions-key)
                         :name (or name title)
                         :title title)])))

(defn add-doc-infos [{:keys [name section] :as infos}]
  (let [[category component] (split
                              (extract-namespace (:namespace infos) relevant-ns true)
                              #"\.")]
    (rf/dispatch [::db/add-component-infos
                  category
                  component
                  (assoc infos
                         :section (or section db/functions-key)
                         :name (or name component))])))

(defn add-component-infos [{:keys [name] :as infos}]
  (let [[category component] (split
                              (extract-namespace (:namespace infos) relevant-ns true)
                              #"\.")]
    (rf/dispatch [::db/add-component-infos
                  category
                  component
                  (assoc infos
                         :name (or name component))])))

(defn add-example [{:keys [code fn-namespace] :as r} & desc]
  (let [[category component] (clojure.string/split
                              (extract-namespace fn-namespace relevant-ns true)
                              #"\.")
        func (first desc)]
    (rf/dispatch [::db/add-component
                  category component
                  (merge r
                         {:code (pprint code)
                          :id (str fn-namespace "-" (random-uuid))
                          :func func})])))

(defn- remove-double-whitespaces [s]
  (-> (reduce (fn [r s]
                (let [s (trim s)]
                  (if (not= "" s)
                    (str r "\n" s)
                    (str r "\n"))))
              ""
              (split (trim s)
                     #"\n"))))

(defn render-doc [{fn-name :name
                   :keys [desc fn-metas  returns
                          signatures is-function?]}]
  (let [{:keys [doc]} fn-metas]
    [:div {:style {:white-space :pre-wrap
                   :margin-bottom "85px"}
           :id (str "example_" fn-name)}
     [:div.live-example {:style {:padding-top "0px"}}
      [:h4 fn-name
       (when (vector? signatures)
         [:<>
          [:font {:style {:font-size "11px"}}
           (reduce (fn [r s]
                     (conj r [:br]
                           (str s)))
                   [:<>]
                   signatures)]])]
      [:hr]
      [:div {:style {:padding-left "16px"
                     :padding-right "16px"}}
       (when (and (not desc)
                  (not doc))
         [:div {:style {:margin "10px"}}
          "No docstring or description available"])
       (when desc
         [:div {:style {:margin "10px"}}
          desc])
       (when (and doc (string? doc))
         [:div {:style {:margin "10px"}
                :dangerouslySetInnerHTML {:__html
                                          (-> (replace (md->html (remove-double-whitespaces doc))
                                                       "<table>"
                                                       "<table class=code>")
                                              (replace "<code>"
                                                       "<code class=parameter style=padding:0px>"))}}])]
      [:hr]
      (when is-function?
        [:div {:style {:margin-top "10px"}}
         [:b "Returns: "]
         (cond
           (vector? returns) (join ", " (map #(name %) returns))
           returns (name returns)
           :else "nil")])]]))

(defn render-example [{:keys [desc code func
                              show-code?
                              code-before code-after
                              title id
                              bg-color]
                       :or {show-code? true}}]
  [:div {:style {:white-space :pre-wrap
                 :margin-bottom "85px"}
         :id (str "example_" title)}
   [:h4 title]
   [:div {:style {:padding-left "16px"
                  :padding-right "16px"}}
    (when desc
      [:div {:style {:margin "10px"}}
       desc])
    [:div.live-example {:style {:background-color bg-color}}
     func]
    (when show-code?
      [md-code (cond-> ""
                 code-before (str code-before "\n\n")
                 :always (str code)
                 code-after (str "\n\n" code-after))])]])

(defn- render-component-infos [section category component]
  (let [{:keys [default-parameters] :as infos} @(rf/subscribe [::db/component-infos section category component])]
    (when infos
      [rd-infos infos default-parameters])))

(defn render-examples [section category component]
  (let [examples @(rf/subscribe [::db/examples section category component])]
    (reduce (fn [p example]
              (if (= section "functions")
                (conj p [render-doc example])
                (conj p [render-example example])))
            [:div [render-component-infos section category component]]
            examples)))

(defn render-current-component []
  (let [[section category component] @(rf/subscribe [::db/current-component])]
    [:div.page-content {:id "component-overview"}
     (with-meta
       [render-examples section category component]
       {:key (str section category component "___exmpl-comp")})]))

(defn table-of-contents []
  (let [[section category component] @(rf/subscribe [::db/current-component])
        examples @(rf/subscribe [::db/examples section category component])
        {:keys [namespace]} @(rf/subscribe [::db/component-infos section category component])]
    [:div.table-of-content
     [:p.toc-main-title
      "Contents"]
     (reduce (fn [p {:keys [title fn-name id]}]
               (conj p
                     (with-meta
                       [:li.toc-elem
                        [:a.toc-title {:href (str "#example_" title)}
                         [:span
                          (or title fn-name)]]]
                       {:key (str "toc_" (or title fn-name))})))
             [:ul.toc-content
              (when namespace
                [:li.toc-elem
                 [:a.toc-title {:href (str "#" (str namespace "api"))}
                  [:span
                   "API"]]])]
             examples)]))

(defn overview []
  [:div.main
   [m/menu]
   [table-of-contents]
   [render-current-component]])