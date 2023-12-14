(ns de.explorama.shared.reporting.description-types
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
            #?(:clj [taoensso.timbre :refer [error]]
               :cljs [taoensso.timbre :refer-macros [error]])))

(s/def :tile/type keyword?)
(s/def :tile/position (s/coll-of number? :algorithmsnd vector? :count 2))
(s/def :tile/size (s/coll-of number? :algorithmsnd vector? :count 2))
(s/def :tile/legend-position #{:right :bottom})

(s/def :template/tile (s/keys :req-un [:tile/position :tile/size]
                              :opt-un [:tile/type :tile/legend-position]))
(s/def :template/id string?)
(s/def :template/name string?)
(s/def :template/desc string?)
(s/def :template/timestamp int?)
(s/def :template/grid (s/coll-of number? :algorithmsnd vector? :count 2 :into []))
(s/def :template/tiles (s/* :template/tile))

(s/def :module/legend? boolean?)
(s/def :module/options-title coll?)
(s/def :module/options string?)

(s/def :module/context-menu (s/keys :req-un [:module/legend? :module/options-title :module/options]))

(s/def :module/di map?)
(s/def :module/state map?)
(s/def :module/preview string?)
(s/def :module/title string?)
(s/def :module/vertical string?)
(s/def :module/tool string?)
(s/def :module/desc string?)

(s/def :desc/vertical-module (s/keys :req-un [:module/di :module/state :module/title :module/vertical :module/tool]
                                     :opt-un [:module/desc :module/preview :module/context-menu]))
(s/def :desc/text-module (s/keys :req-un [:module/state :module/tool]))

(s/def :desc/id string?)
(s/def :desc/name string?)
(s/def :desc/subtitle string?)
(s/def :desc/template-id :template/id)
(s/def :desc/modules (s/* (s/alt :desc/vertical-module :desc/text-module)))
(s/def :desc/desc string?)
(s/def :desc/timestamp int?)

(defmulti desc-type ::desc-type)
(defmethod desc-type :template [_]
  (s/keys
   :req-un [:template/id
            :template/name
            :template/timestamp
            :template/grid
            :template/tiles]
   :opt-un [:template/tiles]))

(defmethod desc-type :dashboard [_]
  (s/keys
   :req-un [:desc/id
            :desc/name
            :desc/template-id
            :desc/modules
            :desc/timestamp]
   :opt-un [:desc/desc
            :desc/subtitle]))

(defmethod desc-type :report [_]
  (s/keys
   :req-un [:desc/id
            :desc/name
            :desc/template-id
            :desc/modules
            :desc/timestamp]
   :opt-un [:desc/desc
            :desc/subtitle]))

(s/def ::desc (s/multi-spec desc-type ::desc-type))

(defn valid-desc? [desc-type desc]
  (let [desc (assoc desc ::desc-type desc-type)]
    (if (s/valid? ::desc desc)
      true
      (error "desc not conform with spec"
             {:desc desc
              :explain (s/explain-str ::desc desc)}))))
