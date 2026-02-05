#!/usr/bin/env bb

(require '[babashka.fs :as fs]
         '[babashka.process :refer [shell]])

(defn windows? []
  (-> (System/getProperty "os.name")
      (.toLowerCase)
      (.contains "win")))

(defn copy-assets [source target]
  (println (str "Copying " source " -> " target))
  (when (fs/exists? target)
    (fs/delete-tree target))
  (fs/copy-tree source target {:replace-existing true}))

(defn main [& args]
  (when (empty? args)
    (println "Usage: gather-assets.bb.clj <mode>")
    (System/exit 1))

  (let [mode (first args)
        pwd (str (fs/cwd))
        res-path (fs/path pwd "../../assets")
        target-path (fs/path pwd "resources/public")
        assets-css-path (fs/path res-path "css")
        assets-fonts-path (fs/path res-path "fonts")
        assets-img-path (fs/path res-path "img")
        target-css-path (fs/path target-path "css")
        target-fonts-path (fs/path target-path "fonts")
        target-img-path (fs/path target-path "img")]

    (println "Update style assets")
    (println "")

    ;; Remove old folders from assets directory
    (doseq [path [assets-css-path assets-fonts-path assets-img-path]]
      (when (fs/exists? path)
        (fs/delete-tree path)))
    (println "Remove old folders done.")
    (println "")

    ;; Build styles
    (println "Building styles...")
    (shell {:dir "../../styles"} (str "bash build.sh " mode))
    (println "")

    ;; Create target directories
    (fs/create-dirs target-path)

    ;; Always copy assets (figwheel doesn't follow symlinks)
    (println "Gathering assets (copy mode - figwheel doesn't follow symlinks)...")
    (copy-assets assets-css-path target-css-path)
    (copy-assets assets-fonts-path target-fonts-path)
    (copy-assets assets-img-path target-img-path)

    (println "")
    (println "Gather assets done.")))

(apply main *command-line-args*)
