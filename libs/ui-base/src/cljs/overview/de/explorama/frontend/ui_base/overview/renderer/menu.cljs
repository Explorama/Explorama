(ns de.explorama.frontend.ui-base.overview.renderer.menu
  (:require [de.explorama.frontend.ui-base.overview.renderer.config :as c]
            [de.explorama.frontend.ui-base.overview.renderer.db :as db]
            [re-frame.core :as rf]
            [de.explorama.frontend.ui-base.version :as v-infos]))

(defn title-comp []
  [:div.basic-infos
   [:div.explorama-logo]
   [:center
    [:a.lib-name {:href c/repo-link
                  :target "_blank"}
     c/project-name]
    [:br]
    [:a.version (str "v" v-infos/version)]]])

(defn first-level-menu-item [title]
  [:button.menu-item {:style {:padding-left "24px"
                              :cursor :default}
                      :type :button}
   [:span.menu-title.h
    title]])

(defn second-level-menu-item [title]
  [:button.menu-item {:style {:padding-left "40px"
                              :cursor :default}
                      :type :button}
   [:span.menu-title.h.sec-lvl
    title]])

(defn update-url [section category component]
  (js/history.replaceState js/history.state
                           ""
                           (str "?section=" section "&category=" category "&component=" component)))

(defn third-level-menu-item [section category component]
  (let [[s cat comp] @(rf/subscribe [::db/current-component])]
    [:a.menu-item.menu {:style {:padding-left "56px"}
                        :href "#component-overview"
                        :on-click #(do
                                     (update-url section category component)
                                     (rf/dispatch [::db/select-component section category component]))}
     [:span.menu-title.third-lvl {:class (when (and (= s section)
                                                    (= cat category)
                                                    (= comp component))
                                           "curr-view")}
      component]]))

(defn components-menu [section category]
  (let [components @(rf/subscribe [::db/components section category])]
    (reduce (fn [p comp]
              (conj p
                   ; [table-of-contents examples]
                    [:li
                     [third-level-menu-item section category comp]]))
            [:ul.menu-list-root]
            components)))

(defn section-menu [section]
  (let [categories @(rf/subscribe [::db/categories section])]
    (reduce (fn [p category]
              (conj p
                    [:li
                     [second-level-menu-item
                      category]]
                    [:li
                     [components-menu section category]]))
            [:ul.menu-list-root]
            categories)))

(defn menu []
  (let [sections @(rf/subscribe [::db/sections])]
    [:div.menu-parent
     [:div
      [title-comp]]
     (reduce (fn [p section]
               (conj p
                     [:li.menu-list-item-root
                      [first-level-menu-item
                       section]
                      [section-menu section]]))
             [:ul.menu-list-root]
             sections)]))
