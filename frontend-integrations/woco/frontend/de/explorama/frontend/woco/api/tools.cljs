(ns de.explorama.frontend.woco.api.tools
  "API used to reg-/de-register tools.
   Tools can be shown in the toolbar (left)
   or in the header (top right)
   and one can be a project tool."
  (:require [de.explorama.frontend.woco.tools :as tools-impl]
            [re-frame.core :as re-frame]
            [clojure.spec.alpha :as spec]))

;Desc
#_{:id ""
   :icon icon-id
   :class <reagent-css-classes>
   :tooltip-text <sub-vec> ;optional => no tooltip
   :tool-group :header|:bar|:project
   :bar-group :top|:middle|:bottom
   :header-group :left|:right|:middle
   :sort-order <num>
   :action <event-vec>
   :action-key <keyword>
   :enabled-sub <sub-vec> ;optional => always active
   :notification-sub <sub-vec>} ;optional => no noftifications


(spec/def :tool/id string?)
(spec/def :tool/class string?) ;eg: "project__save"
(spec/def :tool/icon (spec/or :keyword keyword? :string string?)) ;eg: "search"
(spec/def :tool/tooltip-text (spec/or :vector vector? :string string?))
(spec/def :tool/tool-group #{:header :bar :reporting :project :sidebar :sync-project :hidden})
(spec/def :tool/bar-group #{:top :middle :bottom}) ;only for tool-group = bar;
(spec/def :tool/header-group #{:left :middle :right}) ;only for tool-group = header
(spec/def :tool/active-sub vector?)
(spec/def :tool/visible-sub vector?)
(spec/def :tool/sort-order number?)
(spec/def :tool/action vector?) ; empty vector => no action
(spec/def :tool/action-key keyword?)
(spec/def :tool/enabled-sub vector?) ;optional => always active
(spec/def :tool/notification-sub vector?)
(spec/def :tool/vertical string?)
(spec/def :tool/component keyword?)
(spec/def :tool/close-event vector?)


(defmulti tool-group :tool-group)
(defmethod tool-group :hidden [_]
  (spec/keys
   :req-un [:tool/id
            :tool/tool-group
            :tool/action]
   :opt-un [:tool/vertical
            :tool/enabled-sub
            :tool/notification-sub
            :tool/tooltip-text
            :tool/component]))

(defmethod tool-group :header [_]
  (spec/keys
   :req-un [:tool/id
            :tool/tool-group
            :tool/action
            :tool/icon
            :tool/header-group]
   :opt-un [:tool/vertical
            :tool/enabled-sub
            :tool/notification-sub
            :tool/tooltip-text
            :tool/component
            :tool/action-key]))

(defmethod tool-group :bar [_]
  (spec/keys
   :req-un [:tool/id
            :tool/tool-group
            :tool/action
            :tool/icon
            :tool/bar-group]
   :opt-un [:tool/vertical
            :tool/sort-order
            :tool/enabled-sub
            :tool/notification-sub
            :tool/tooltip-text
            :tool/component]))

(defmethod tool-group :project [_]
  (spec/keys
   :req-un [:tool/id
            :tool/tool-group
            :tool/action]
   :opt-un [:tool/vertical
            :tool/sort-order
            :tool/class
            :tool/enabled-sub
            :tool/tooltip-text
            :tool/action-key]))

(defmethod tool-group :sync-project [_]
  (spec/keys
   :req-un [:tool/id
            :tool/tool-group
            :tool/icon
            :tool/action]
   :opt-un [:tool/vertical
            :tool/sort-order
            :tool/active-sub
            :tool/visible-sub
            :tool/class
            :tool/enabled-sub
            :tool/notification-sub
            :tool/tooltip-text
            :tool/action-key]))

(defmethod tool-group :reporting [_]
  (spec/keys
   :req-un [:tool/id
            :tool/tool-group
            :tool/action]
   :opt-un [:tool/vertical
            :tool/sort-order
            :tool/class
            :tool/enabled-sub
            :tool/tooltip-text
            :tool/action-key]))

(defmethod tool-group :sidebar [_]
  (spec/keys
   :req-un [:tool/id
            :tool/tool-group
            :tool/icon
            :tool/action]
   :opt-un [:tool/vertical
            :tool/close-event
            :tool/sort-order
            :tool/class
            :tool/enabled-sub
            :tool/tooltip-text]))

(spec/def :tool/desc (spec/multi-spec tool-group :tool-group))

(re-frame/reg-event-db
 ::register
 (fn [db [_ tool-desc]]
   (tools-impl/register db tool-desc)))

(re-frame/reg-event-db
 ::deregister
 (fn [db [_ tool-id]]
   (tools-impl/deregister db tool-id)))
