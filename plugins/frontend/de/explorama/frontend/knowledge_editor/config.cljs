(ns de.explorama.frontend.knowledge-editor.config)

(def tool-name "knowledge-editor")
(def default-namespace :knowledge-editor)
(def default-vertical-str (name default-namespace))

(def prune-char \u2026) ;".." \u2025
(def header-height 36)

(def width 1600)
(def height (- 1000 (* 2 header-height)))

(def default-color-events "#528b8b")
(def default-color-contexts "#886688" #_"#668888")
(def default-color-contexts-2 "#4682B4")
(def default-color-connection "#3B3838")