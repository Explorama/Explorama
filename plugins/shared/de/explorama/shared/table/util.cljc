(ns de.explorama.shared.table.util)

(def default-namespace :table)

(def default-vertical-table-str (name default-namespace))

(defn is-table? [frame-id]
  (= (:vertical frame-id) default-vertical-table-str))
