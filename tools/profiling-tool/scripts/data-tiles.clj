(require '[babashka.fs :as fs]
         '[cheshire.core :as json]
         '[clojure.string :as str])

(defn parse-file [file]
  (-> (fs/read-all-lines file)
      first
      (json/parse-string true)))

(def target-path "../gen_resources/data_tiles/")
(def source-path "../gen_resources/exp-import/")

(fs/create-dirs target-path)

(defn transform-file [fpath]
  (let [data (parse-file fpath)
        country-map (into {}
                          (comp (map (fn [{:keys [global-id type name]}]
                                       (when (= type "country")
                                         [global-id name])))
                                (filter identity))
                          (:contexts data))
        ds-name (get-in data [:datasource :name])
        data-tiles (reduce (fn [acc item]
                             (reduce (fn [acc {:keys [dates context-refs]}]
                                       (conj acc {"year" (-> dates first :value (subs 0 4))
                                                  "country" (some #(get country-map (:global-id %)) context-refs)
                                                  "datasource" ds-name
                                                  "identifier" "search"
                                                  "bucket" "default"}))
                                     acc
                                     (:features item)))
                           #{}
                           (:items data))]
    (spit (str target-path
               "dt-"
               (str/replace (fs/file-name fpath) #"\.json" ".edn"))
          data-tiles
          :encoding "UTF-8")))

(doseq [file (fs/match source-path (str "regex:.*.json") {:recursive true})]
  (transform-file file))
