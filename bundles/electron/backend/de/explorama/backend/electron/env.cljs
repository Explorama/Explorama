(ns de.explorama.backend.electron.env
  (:require [process]))

(defn get-env [k]
  (aget process "env" k))