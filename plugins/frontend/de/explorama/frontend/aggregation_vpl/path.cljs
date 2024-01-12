(ns de.explorama.frontend.aggregation-vpl.path)

(defn frame-desc [frame-id]
  [:vpl frame-id])

(defn dim-info [frame-id]
  (conj (frame-desc frame-id) :dim-info))

(defn data-instances-temp [frame-id]
  (conj (frame-desc frame-id) :data-instances-temp))

(defn dim-info-temp [frame-id]
  (conj (frame-desc frame-id) :dim-info-temp))

(defn data-acs-temp [frame-id]
  (conj (frame-desc frame-id) :data-acs-temp))
