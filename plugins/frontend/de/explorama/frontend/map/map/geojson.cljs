(ns de.explorama.frontend.map.map.geojson)

(defonce ^:private store (atom {}))

(defn store-geojson [path geojson]
  (let [geojson (if (string? geojson)
                  (.parse js/JSON geojson)
                  geojson)]
    (swap! store assoc path geojson)))

(defn get-geojson [path]
  (get @store path))