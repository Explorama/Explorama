(ns de.explorama.frontend.configuration.path
  (:require [de.explorama.frontend.configuration.config :as config]
            [taoensso.timbre :refer [error]]))

(defn dissoc-in [db path]
  (if (vector? path)
    (update-in db (pop path)
               dissoc
               (peek path))
    (do
      (error (str "can't dissoc-in. " path " is not a vector"))
      db)))

(def root-key config/default-namespace)
(def root [root-key])


(def configs-key :configs)
(def configs (conj root configs-key))

(defn config-type [conf-type]
  (conj configs conf-type))

(defn config-entry [conf-type config-id]
  (conj (config-type conf-type)
        config-id))

(def i18n-key :i18n)
(def i18n (conj root i18n-key))

(defn i18n-entry [type]
  (conj i18n type))

(def temporary-user-settings-key :temporary-user-settings)
(def temporary-user-settings (conj root temporary-user-settings-key))

(def language-key :language)
(def temporary-user-settings-language (conj temporary-user-settings language-key))

(def theme-key :theme)
(def temporary-user-settings-theme (conj temporary-user-settings theme-key))

(def ac-root
  (conj root :acs))

(def characteristics-root
  (conj ac-root :characteristics))

(defn characteristics [attribute]
  (conj characteristics-root attribute))

(def characteristics-usage-root
  (conj ac-root :characteristics-usage))

(defn characteristics-usage [identifier]
  (conj characteristics-usage-root identifier))

(defn characteristics-requesting? [identifier]
  (conj (characteristics-usage identifier)
        :requesting?))

(def attr-types
  (conj ac-root :attribute-types))

(def layout-sidebar-title
  (conj root :layout-sidebar-title))

(defn layout-error-status [id]
  (conj root :layout-error-status id))

(def burger-menu-infos
  (conj root :burger-menu))

(def dialogs-root (conj root :dialogs))

(defn dialog [dialog-key]
  (conj dialogs-root :dialog dialog-key))

(defn dialog-is-active? [dialog-key]
  (conj (dialog dialog-key)
        :is-active?))

(defn dialog-tag [dialog-key]
  (conj (dialog dialog-key)
        :tag))

(defn dialog-data [dialog-key]
  (conj (dialog dialog-key)
        :data))

(def project-post-checks-root-key :project-post-checks)

(def project-post-checks-root
  (conj root project-post-checks-root-key))

(defn project-outdated-config-type [config-type]
  (conj project-post-checks-root config-type))

(defn project-outdated-configs [config-type]
  (conj (project-outdated-config-type config-type)
        :outdated))

(def project-post-handles-callback
  (conj project-post-checks-root :project-post-handles-callback))

(def project-post-handles-count
  (conj project-post-checks-root :project-post-handles-count))

(def project-post-handles-done
  (conj project-post-checks-root :project-post-handles-done))

(def post-process-dialog-callbacks-key :dialog-callbacks)
(def post-process-dialog-callbacks
  (conj project-post-checks-root post-process-dialog-callbacks-key))

(def data-from-verticals-key
  (conj root :data-from-verticals))

(defn data-from-verticals
  ([path]
   (conj data-from-verticals-key path))
  ([v-name path]
   (into (conj data-from-verticals-key v-name) path)))

(def temp-vertical-changes-key
  :temp-vertical-changes)

(def temp-vertical-changes-path
  (conj root temp-vertical-changes-key))

(defn temp-vertical-changes [vertical]
  (conj temp-vertical-changes-path vertical))

(def active-role-tab
  (conj root :active-role-tab))

(def data-management
  (conj root :data-management))

(def geographic-attribute-value
  (conj data-management :geographic-attribute-value))

(def topic-texts
  (conj data-management :topic-texts))

(def datasource->topics
  (conj data-management :datasource->topics))

(def available-datasources
  (conj data-management :available-datasources))

(def current-geographic-attributes
  (conj data-management :current-geographic-attributes))

(def loaded-geographic-attributes
  (conj data-management :loaded-geographic-attributes))

(def loaded-topic
  (conj data-management :loaded-topic))

(def current-datasource
  (conj data-management :current-datasource))

(def datasource-value
  (conj data-management :datasource-value))

(def topic-id
  (conj data-management :topic-id))

(def mapped-datasources
  (conj data-management :mapped-datasources))


(def mapped-topics-options
  (conj data-management :mapped-topics-options))

(def initially-mapped-topics
  (conj data-management :initially-mapped-topics))

(def rows-path
  (conj data-management :rows-path))

(def active-dm-tab
  (conj data-management :active-dm-tab))

(def add-geo-attr-frame?
  (conj data-management :add-geo-attr-frame?))

(def new-role-frame-key
  :new-role-frame?)
(def new-role-frame?
  (conj root new-role-frame-key))

(def active-tab
  (conj root :active-tab))

(def path-template-key
  :path-template)
(def path-template
  (conj root path-template-key))

(def role-tabs-key
  :role-tabs)
(def role-tabs
  (conj root role-tabs-key))

(def delete-warning-frame-key
  :delete-warning-frame?)
(def delete-warning-frame?
  (conj root delete-warning-frame-key))

(def role-title-key
  :role-title)
(def role-title
  (conj root role-title-key))

(def mouse-layout-error-status
  (conj root :mouse-layout-error-status))

(def mouse-layout-temporary
  (conj root :mouse-layout-temporary))

(def mouse-layout-last-applied
  (conj root :mouse-layout-last-applied))

(def woco-temporary
  (conj root :woco-temporary))

(def woco-last-applied
  (conj root :woco-last-applied))

(def delete-datasource-dialog
  (conj root :delete-datasource-dialog))

(def delete-datasource-loadscreen
  (conj root :delete-datasource-loadscreen))
