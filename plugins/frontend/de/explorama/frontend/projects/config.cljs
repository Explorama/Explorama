(ns de.explorama.frontend.projects.config)

(def default-namespace :projects)
(def default-vertical-str (name default-namespace))

(def default-vertical-protocol-str (str default-vertical-str "-protocol"))

(def disable-project-loadingscreen false)

(def use-default-protocol-func false)

(def default-project-class "project-1")

(def configs (js->clj (aget js/window "EXPLORAMA_PROJECTS_CONFIGS") :keywordize-keys true))

(def min-project-title-length 4)
(def max-project-title-length 50)
(def max-project-desc-length 50)

(def max-protocol-entry-length 100)

(def chat-max-length 10000)
