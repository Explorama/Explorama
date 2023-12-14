(ns de.explorama.frontend.data-atlas.path)

(def root :data-atlas)
(def replay [root :replay])
(def session [root :session])

(def frame-container [root :frame])

(defn frame [frame-id]
  (conj frame-container frame-id))

(defn replay=? [db frame-id]
  (get-in db (conj replay
                   frame-id)))
(defn replay= [db frame-id val]
  (assoc-in db
            (conj replay
                  frame-id)
            val))

(defn data [frame-id]
  (conj (frame frame-id) :data))

(defn data-lists [frame-id]
  (conj (data frame-id) :lists))
(defn data-lists-data-sources [frame-id]
  (conj (data-lists frame-id) :data-sources))
(defn data-lists-temp-data-sources [frame-id]
  (conj (data-lists frame-id) :temp-data-sources))
(defn data-lists-attributes [frame-id]
  (conj (data-lists frame-id) :attributes))
(defn data-lists-attributes-ranges [frame-id]
  (conj (data-lists frame-id) :ranges))
(defn data-lists-characteristics [frame-id]
  (conj (data-lists frame-id) :characteristics))
(defn data-lists-matched? [frame-id]
  (conj (data-lists frame-id) :matched?))

(defn data-selection [frame-id]
  (conj (data frame-id) :selection))
(defn data-selection-data-source [frame-id]
  (conj (data-selection frame-id) :data-source))
(defn data-selection-temp-data-source [frame-id]
  (conj (data-selection frame-id) :temp-data-source))
(defn data-selection-attribute [frame-id]
  (conj (data-selection frame-id) :attribute))
(defn data-selection-characteristic [frame-id]
  (conj (data-selection frame-id) :characteristic))

(defn data-search [frame-id]
  (conj (data frame-id) :search))

(defn data-descriptions [frame-id]
  (conj (data frame-id) :description))

(defn list-component-atoms [frame-id]
  (conj (frame frame-id) :list-component-atoms))

(defn loading? [frame-id]
  (conj (frame frame-id) :loading?))