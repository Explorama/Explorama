(ns de.explorama.frontend.woco.frame.view.product-tour
  (:require [cuerdas.core :as str]
            [de.explorama.shared.common.configs.mouse :as shortcuts-mouse]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.misc.core :as comp-misc]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.navigation.control :as nav-control]))

(re-frame/reg-sub
 ::step-position
 (fn [db [_ {:keys [frame-id left top right bottom]}]]
   (let [{[width] :full-size
          coords :coords} (evts/frame-desc db frame-id)
         {zoom :z} (nav-control/position db)
         [left-abs top-abs] (nav-control/workspace->page db coords)]
     (cond-> {:top top-abs
              :left left-abs}
       left (assoc :offset-left
                   (+ (* width zoom) left))
       right (assoc :offset-right right)
       top (assoc :offset-top top)
       bottom (assoc :offset bottom)))))

(defn preference-panning-desc [join-label-key]
  (let [{:keys [product-tour-mouse-pref-left
                product-tour-mouse-pref-middle
                product-tour-mouse-pref-right]
         join-label join-label-key}
        @(re-frame/subscribe [::i18n/translate-multi
                              :product-tour-mouse-pref-left
                              :product-tour-mouse-pref-middle
                              :product-tour-mouse-pref-right
                              join-label-key])]
    (->> (val-or-deref (fi/call-api [:user-preferences :preference-sub]
                                    shortcuts-mouse/pref-key
                                    shortcuts-mouse/mouse-default))
         (filter #(= :panning (:action %)))
         (map (fn [{:keys [button]}]
                (case button
                  1 product-tour-mouse-pref-left
                  2 product-tour-mouse-pref-middle
                  3 product-tour-mouse-pref-right)))
         (str/join (str " " join-label " ")))))

(defn product-tour-step [frame-id]
  (let [step-pos @(re-frame/subscribe [::step-position {:frame-id frame-id
                                                        :left 10}])
        params @(re-frame/subscribe [:de.explorama.frontend.woco.frame.plugin-api/product-tour frame-id])
        current-step (re-frame/subscribe [:de.explorama.frontend.woco.api.product-tour/current-step])
        current-language (re-frame/subscribe [::i18n/current-language])
        steps-label (re-frame/subscribe [::i18n/translate :product-tour-steps-label])
        next-button-label (re-frame/subscribe [::i18n/translate :product-tour-next-button-label])
        back-button-label (re-frame/subscribe [::i18n/translate :product-tour-back-button-label])
        max-steps (re-frame/subscribe [:de.explorama.frontend.woco.api.product-tour/max-steps])]
    [comp-misc/product-tour-step (assoc (merge params step-pos)
                                        :current-step current-step
                                        :language current-language
                                        :steps-label steps-label
                                        :next-button-label next-button-label
                                        :back-button-label back-button-label
                                        :val-sub-fn (fn [keyword-vector]
                                                      (if
                                                       (and (vector? keyword-vector)
                                                            (= (first keyword-vector) :preference-panning)) (preference-panning-desc (second keyword-vector))
                                                       @(re-frame/subscribe (if (vector? keyword-vector)
                                                                              keyword-vector
                                                                              [::i18n/translate keyword-vector]))))
                                        :prev-fn (fn [_]
                                                   (re-frame/dispatch [:de.explorama.frontend.woco.api.product-tour/previous-step]))
                                        :next-fn (fn [{:keys [component additional-info]}]
                                                   (re-frame/dispatch [:de.explorama.frontend.woco.api.product-tour/next-step component additional-info]))
                                        :cancel-fn (fn [_]
                                                     (re-frame/dispatch [:de.explorama.frontend.woco.api.product-tour/cancel-product-tour]))
                                        :max-steps max-steps)]))