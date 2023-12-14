(ns de.explorama.backend.expdb.legacy.compatibility)

(defn create-formdata-for-ds [datasource]
  [[["datasource" "Datasource"] {:values [datasource]}]])