(ns de.explorama.backend.indicator.persistence.util-test
  (:require [clojure.java.io :as io]
            [de.explorama.backend.indicator.persistence.backend.redis :as redisb]
            [de.explorama.backend.mocks.redis :as redis-mock]))

(def PAdmin {:username "PAdmin"
             :role "admin"})
(def mmeier {:username "MMeier"
             :role "data-expert"})

(def indicator-1-id "1uia")
(def indicator-2-id "1uiaua23")
(def indicator-1 {:id indicator-1-id
                  :creator "PAdmin"
                  :name "Indicator 1"
                  :dis {"di-1" "foo"}
                  :calculation-desc []
                  :group-attributes ["country" "year"]})
(def indicator-2 {:id indicator-2-id
                  :creator "PAdmin"
                  :name "Indicator 1"
                  :dis {"di-1" "foo"}
                  :calculation-desc []
                  :group-attributes ["country" "year"]})
(def indicator-3-id "2hfd")
(def indicator-3 {:id indicator-3-id
                  :creator "MMeier"
                  :name "Indicator 2"
                  :dis {"di-1" "foo"}
                  :calculation-desc []
                  :group-attributes ["country" "year"]})

(defn indicator-path [creator id]
  (str "indicator-test/"
       creator
       java.io.File/separator
       id ".edn"))

(defn indicator-file-exist [{:keys [creator id]}]
  (.exists (io/file (indicator-path creator id))))

(defn indicator-redis-exist [{:keys [creator id]}]
  (get (redis-mock/get-state) (@#'redisb/indicator-path creator id)))
