(ns de.explorama.frontend.expdb.path)

(def root :expdb)
(def imports :imports)

(def buckets [root :buckets])
(def uploading? [root :uploading?])

(def current-mapping [root imports :current-mapping])

(def current-data [root imports :current-data])

(def meta-data [root imports :meta-data])

(def current-header [root imports :current-header])

(def raw-mapping [root imports :raw-mapping])

(def uploaded-mapping [root imports :uploaded-mapping])

(def datasource [root imports :datasource])

(def show-dialog [root imports :show-dialog])

(def options [root imports :options])

(def datasource-name [root imports :datasource-name])

(def load-screen [root imports :load-screen])

(def done-screen [root imports :done-screen])

(def raw-meta-data [root imports :raw-meta-data])

(def show-view? [root :show?])

(def import-summary [root imports :import-summary])