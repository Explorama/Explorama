(ns de.explorama.backend.mosaic.client.redo
  (:require [de.explorama.shared.mosaic.common-paths :as gcp]
            [clojure.set :as set]))

(defn validate-operations-desc [ds-acs dim-info {grp-by-key gcp/grp-by-key

                                                 _ gcp/couple-key

                                                 sub-grp-by-key gcp/sub-grp-by-key

                                                 render-mode-key gcp/render-mode-key

                                                 {sort-attr :by}
                                                 gcp/sort-key

                                                 {grp-sort-by :by
                                                  grp-sort-attr :attr
                                                  :as sort-grp}
                                                 gcp/sort-grp-key

                                                 {sub-grp-sort-by :by
                                                  sub-grp-sort-attr :attr
                                                  :as sort-sub-grp}
                                                 gcp/sort-sub-grp-key

                                                 scatter-x gcp/scatter-x
                                                 scatter-y gcp/scatter-y
                                                 :as op-desc}]
  (let [datasource-attributes (->> (select-keys ds-acs (:datasources dim-info))
                                   vals
                                   (map #(map (fn [{:keys [key]}] key) %))
                                   (map set)
                                   (apply set/union))
        datasource-attributes (conj datasource-attributes "layout")]
    (case render-mode-key gcp/render-mode-key-scatter
          (let [scatter-x-valid? (or (datasource-attributes scatter-x)
                                     (nil? scatter-x))
                scatter-y-valid? (or (datasource-attributes scatter-y)
                                     (nil? scatter-y))]
            [(cond-> {}
               (not scatter-x-valid?)
               (assoc gcp/scatter-x scatter-x)

               (not scatter-y-valid?)
               (assoc gcp/scatter-y scatter-y))
             (cond-> op-desc
               (not scatter-x-valid?)
               (dissoc gcp/scatter-x)

               (not scatter-y-valid?)
               (dissoc gcp/scatter-y))])
          gcp/render-mode-key-treemap
          (let [valid-group-by? (or (datasource-attributes grp-by-key)
                                    (nil? grp-by-key))
                valid-sub-group-by? (or (datasource-attributes sub-grp-by-key)
                                        (nil? sub-grp-by-key))]
            [(cond-> {}

               (not valid-group-by?)
               (assoc gcp/grp-by-key grp-by-key)

               (not valid-sub-group-by?)
               (assoc gcp/sub-grp-by-key sub-grp-by-key))
             (cond-> op-desc

               (not valid-group-by?)
               (dissoc gcp/grp-by-key
                       gcp/sub-grp-by-key
                       gcp/sort-grp-key
                       gcp/sort-sub-grp-key)

               (not valid-sub-group-by?)
               (dissoc gcp/sub-grp-by-key gcp/sort-sub-grp-key))])
          (let [valid-sort? (datasource-attributes sort-attr)
                valid-group-by? (or (datasource-attributes grp-by-key)
                                    (nil? grp-by-key))
                valid-sub-group-by? (or (datasource-attributes sub-grp-by-key)
                                        (nil? sub-grp-by-key))
                valid-sort-groups? (or (= :name grp-sort-by)
                                       (= "layout" grp-sort-by)
                                       (and (= :aggregate grp-sort-by)
                                            (datasource-attributes grp-sort-attr))
                                       (nil? sort-grp))
                valid-sort-sub-groups? (or (= :name sub-grp-sort-by)
                                           (= "layout" sub-grp-sort-by)
                                           (and (= :aggregate sub-grp-sort-by)
                                                (datasource-attributes sub-grp-sort-attr))
                                           (nil? sort-sub-grp))]
            [(cond-> {}
               (not valid-sort?)
               (assoc gcp/sort-key sort-attr)

               (not valid-group-by?)
               (assoc gcp/grp-by-key grp-by-key)

               (not valid-sub-group-by?)
               (assoc gcp/sub-grp-by-key sub-grp-by-key)

               (not valid-sort-groups?)
               (assoc gcp/sort-grp-key gcp/start-desc-sort-groups)

               (not valid-sort-sub-groups?)
               (assoc gcp/sort-sub-grp-key gcp/start-desc-sort-groups))
             (cond-> op-desc
               (not valid-sort?)
               (assoc gcp/sort-key gcp/start-desc-sort)

               (not valid-group-by?)
               (dissoc gcp/grp-by-key
                       gcp/sub-grp-by-key
                       gcp/sort-grp-key
                       gcp/sort-sub-grp-key)

               (not valid-sub-group-by?)
               (dissoc gcp/sub-grp-by-key gcp/sort-sub-grp-key)

               (not valid-sort-groups?)
               (assoc gcp/sort-grp-key gcp/start-desc-sort-groups)

               (not valid-sort-sub-groups?)
               (assoc gcp/sort-sub-grp-key gcp/start-desc-sort-groups))]))))