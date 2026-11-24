(ns de.explorama.backend.electron.env)

;; js/process is a global in the Node/electron runtime; requiring it as a
;; namespace fails under figwheel's :target :bundle compile (builtins are
;; not in node_modules) — see file.cljs for the same pattern.
(defn get-env [k]
  (aget js/process "env" k))
