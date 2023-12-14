(ns de.explorama.frontend.search.views.validation
  (:require [clojure.string :as st]
            [re-frame.core :refer [reg-sub
                                   subscribe]]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.search.path :as spath]
            [de.explorama.frontend.search.data.topics :refer [is-topic-attr-desc? get-topics]]
            [de.explorama.frontend.search.config :as config]
            [de.explorama.frontend.search.data.acs :as acs]))

(defn match-parameter-conf?
  ([selected-attrs row]
   (match-parameter-conf? selected-attrs row false))
  ([selected-attrs [[attr]] as-map?]
   (if-let [attr-conf (config/conf-for-attr attr)]
     (let [{:keys [required-attributes required-attributes-num]
            :or {required-attributes []
                 required-attributes-num 0}} attr-conf
           all-required (vector? required-attributes)
           check-req-attrs (if all-required
                             (every? selected-attrs required-attributes)
                             (boolean (some selected-attrs required-attributes)))
           check-req-attrs-num (> (count selected-attrs)
                                  required-attributes-num)]
       (if as-map?
         {:req-attrs-valid? check-req-attrs
          :req-attrs-infotext (if (= 1 (count required-attributes))
                                (st/replace @(subscribe [::i18n/translate :required-attributes-infotext-one])
                                            "<p>"
                                            (i18n/attribute-label (first required-attributes)))
                                (st/replace @(subscribe [::i18n/translate (if all-required
                                                                            :required-attributes-infotext-multi
                                                                            :required-attributes-infotext-one-needed)])
                                            "<p>"
                                            (st/join ", "
                                                     (map #(i18n/attribute-label %)
                                                          required-attributes))))
          :req-attrs-num-valid? check-req-attrs-num
          :req-attrs-num-infotext (if (= 1 required-attributes-num)
                                    (st/replace @(subscribe [::i18n/translate :required-attributes-num-infotext-one])
                                                "<p>"
                                                required-attributes-num)
                                    (st/replace @(subscribe [::i18n/translate :required-attributes-num-infotext-multi])
                                                "<p>"
                                                required-attributes-num))}
         (and check-req-attrs check-req-attrs-num)))
     (if as-map?
       {}
       true))))

(defn values-valid? [[attr] options values]
  (let [check-fn (if (= attr "month")
                   (fn [o v]
                     (try
                       (= (js/parseInt v)
                          (-> (st/split o #"-")  ;calculate month from options (yyyy-mm)
                              (second)
                              (js/parseInt)))
                       (catch :default _
                         false)))
                   (fn [o v] (= (str o) v)))]
    (and (seq values)
         (or (<= 5000
                 (count options))
             (every? (fn [v]
                       (let [v (str (get v :value v))]
                         (some #(check-fn % v)
                               options)))
                     values)))))

(defn value-valid? [options value]
  (let [v (get value :value value)
        v-str (str v)]
    (and (not (nil? value))
         (or (and (string? v)
                  (not-empty v))
             (not (string? v)))
         (or (<= 5000
                 (count options))
             (some #(= (str %) v-str)
                   options)))))

(defn is-range-valid? [options from to]
  (let [from (get from :value from)
        to (get to :value to)
        not-number-values? (and (not (number? from))
                                (not (number? to)))
        number-values (and (number? from)
                           (number? to))
        from-value-valid? (when (and options not-number-values?)
                            (value-valid? options from))
        to-value-valid? (when (and options not-number-values?)
                          (value-valid? options to))]
    (and (boolean from)
         (boolean to)
         (or
          not-number-values?
          (and number-values
               (<= from to)))
         (or number-values
             (and not-number-values?
                  (or (not options)
                      (empty? options)
                      (and from-value-valid?
                           to-value-valid?)))))))

(defn in-closed-range [[begin end] x]
  (if (some nil? [begin end x])
    false
    (try (and (not (pos? (compare begin x)))
              (not (pos? (compare x end))))
         (catch :default e
           false))))

(defmulti validate-row (fn [_ attr-type attr-desc _]
                         (if (is-topic-attr-desc? attr-desc)
                           :topic
                           attr-type)))

(defmethod validate-row :day [_ _attr-type attr-desc {:keys [options advanced
                                                             start-date end-date
                                                             selected-date last-x]
                                                      condi :cond}]
  (cond
    (or (not advanced)
        (and advanced
             (#{"in range" "not in range"} (:value condi))))
    (let [interval [(apply min options) (apply max options)]
          valid-start? (in-closed-range interval start-date)
          valid-end? (in-closed-range interval end-date)]
      {:details {:valid-start? valid-start?
                 :valid-end? valid-end?}
       :valid? (boolean (and valid-start? valid-end?))})
    (and advanced (= "current" (:value condi))) {:valid? true}
    (and advanced (= "last-x" (:value condi))) {:details {:valid-number? (and last-x (number? last-x) (> last-x 0))}
                                                :valid? (boolean (and last-x (number? last-x) (> last-x 0)))}
    :else (let [valid-selected? (boolean (some #{selected-date} options))]
            {:details {:valid-selected? valid-selected?}
             :valid? valid-selected?})))

(defn- basic-validation [attr-type attr-desc {:keys [options advanced
                                                     values value from to  all-values? empty-values?] :as row-data}]
  {:valid? (boolean (or (and (not (nil? value))
                             (number? (get value :value value))
                             (or
                              (= :integer attr-type)
                              (= :decimal attr-type)
                              (= :double attr-type)))              ;Evtl. eleganter zu lösen
                        (and advanced (or all-values? empty-values?))
                        (and (not (nil? value))
                             (= attr-type :notes)
                             (> (count value) 0))
                        (and (not (nil? value))
                             (= attr-type :external-ref)
                             (> (count value) 0))
                        (is-range-valid? options from to)
                        (value-valid? options value)
                        (values-valid? attr-desc options values)))
   :empty? (and (nil? from) (nil? to) (nil? value) (empty? values))})

(defmethod validate-row :default [_ attr-type attr-desc row-data]
  (basic-validation attr-type attr-desc row-data))

(defmethod validate-row :topic [db attr-type attr-desc {:keys [options topic-selection? values]}]
  (let [available-datasources (set options)
        check-options (if topic-selection?
                        (set (map :value (get-topics db available-datasources)))
                        available-datasources)]
    {:valid? (boolean (and (seq values)
                           (every? check-options values)))
     :empty? (empty? values)}))

(defmethod validate-row :month [_ attr-type attr-desc {:keys [last-x advanced]
                                                       condi :cond
                                                       :as row-data}]
  (cond
    (and advanced (= "current" (:value condi))) {:valid? true}
    (and advanced (= "last-x" (:value condi))) {:details {:valid-number? (and last-x (number? last-x) (> last-x 0))}
                                                :valid? (boolean (and last-x (number? last-x) (> last-x 0)))}
    :else (basic-validation attr-type attr-desc row-data)))

(defmethod validate-row :year [_ attr-type attr-desc {:keys [last-x advanced]
                                                      condi :cond
                                                      :as row-data}]
  (cond
    (and advanced (= "current" (:value condi))) {:valid? true}
    (and advanced (= "last-x" (:value condi))) {:details {:valid-number? (and last-x (number? last-x) (> last-x 0))}
                                                :valid? (boolean (and last-x (number? last-x) (> last-x 0)))}
    :else (basic-validation attr-type attr-desc row-data)))

(defmethod validate-row :location [_ attr-type attr-desc {values :values :as row-data}]
  (let [valid? (and (seq values)
                    (= 4 (count values))
                    (every? number? values))]
    {:valid? valid?
     :empty? (not valid?)}))

(defn is-row-valid? [db frame-id [attr-desc row-data]]
  (let [attr-type (acs/attr-type db attr-desc)]
    (boolean (:valid? (validate-row db attr-type attr-desc row-data)))))

(defn row-valid-infos [db frame-id path attr-desc]
  (let [formdata (get-in db (spath/frame-search-rows frame-id) {})
        selected-attrs (into #{} (map first) (keys formdata))]
    (-> (or (match-parameter-conf? selected-attrs [attr-desc] true) {})
        (assoc :validation-result (validate-row db
                                                (acs/attr-type db attr-desc)
                                                attr-desc
                                                (get-in db path))))))

(defn formdata-submit-valid?
  ([db frame-id formdata additional-formdata]
   (let [selected-attrs (into #{} (map first) (concat (keys formdata)
                                                      (keys additional-formdata)))]
     (loop [row (first formdata)
            formdata (rest formdata)]
       (cond
         (nil? row) true
         (and
          (is-row-valid? db frame-id row)
          (match-parameter-conf? selected-attrs row))
         (recur (first formdata)
                (rest formdata))
         :else false))))
  ([db frame-id formdata]
   (formdata-submit-valid? db frame-id formdata nil)))

(defn search-formdata-valid? [db frame-id]
  (let [search-rows (get-in db (conj config/search-pre-path frame-id))]
    (and (not-empty search-rows)
         (formdata-submit-valid? db frame-id search-rows))))

(reg-sub
 ::search-formdata-valid?
 (fn [db [_ frame-id]]
   (search-formdata-valid? db frame-id)))

(reg-sub
 ::product-tour-search-formdata-valid?
 (fn [db _]
   (let [frame-id (ffirst (get-in db config/search-pre-path))]
     (when (seq frame-id)
       (search-formdata-valid? db frame-id)))))

(reg-sub
 ::row-valid-infos
 (fn [db [_ frame-id path attr-desc]]
   (row-valid-infos db frame-id path attr-desc)))