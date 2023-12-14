(ns de.explorama.cli.data-transformer.parser.instances
  (:require [de.explorama.cli.data-transformer.parser.csv :as csv]))

(defmulti parser-instance (fn [desc] (get-in desc [:meta-data :file-format])))

(defmethod parser-instance :csv [_]
  (csv/new-instance))
