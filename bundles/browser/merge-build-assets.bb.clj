#!/usr/bin/env bb

(require '[clojure.java.io :as io]
         '[clojure.string :as str])
(:import java.util.Base64)
(:import java.nio.file.Files)
(:import java.nio.file.Paths)

(def separator (java.io.File/separator))
(def directory (str (or (first *command-line-args*) "dist")))
(def html-template "index.html.template")
(def html-file "index.html")
(def source-file (str directory separator html-template))
(def target-file (str directory separator html-file))

(defn delete-directory-recursive
  "Recursively delete a directory."
  [^java.io.File file]
  (when (.isDirectory file)
    (run! delete-directory-recursive (.listFiles file)))
  (io/delete-file file))

(println "--- Merge assets into index.html for production build ---")
(def index-file (atom (slurp source-file)))

(def css-includes (re-seq (re-pattern "<link rel=\"stylesheet\" href=\".*\" type=\"text/css\">")
                          @index-file))

(doseq [css-include css-includes]
;;   (when-not (str/includes? css-include "woco.css")
  (let [css-file (-> (re-find (re-pattern "href=\".*\" type") css-include)
                     (str/replace "href=\"" "")
                     (str/replace "\" type" "")
                     (str/replace "\\" separator)
                     (str/replace "/" separator))
        f (io/file (str directory separator css-file))]
    (when (.isFile f)
      (let [content (-> (slurp f :encoding "UTF-8")
                        (str/replace "url(\"../" "url(\"")
                        (str/replace "url(../" "url("))
            _ (println "   > Replace css-file link with inline resource" {:length (count content)
                                                                          :replace-include css-include})
            font-includes (re-seq #"fonts/\S+.woff2" content)
            content (reduce (fn [content font-include]
                              (let [font-file (-> (str directory separator font-include)
                                                  (str/replace "\\" separator)
                                                  (str/replace "/" separator))
                                    font-base64 (->> (java.nio.file.Paths/get (.toURI (io/file font-file)))
                                                     (java.nio.file.Files/readAllBytes)
                                                     (.encodeToString (java.util.Base64/getEncoder)))]
                                (println "     > Replace font with base64 variant" {:replace-font font-include})
                                (str/replace content
                                             font-include
                                             (str "data:application/x-font-woff;charset=utf-8;base64," font-base64))))
                            content
                            font-includes)]
        (reset! index-file
                (str/replace @index-file
                             css-include
                             (str "<style type=\"text/css\">\n"
                                  content
                                  "\n    </style>")))))))

(println "   > Write out " target-file)

(spit target-file @index-file :encoding "UTF-8")

(println "   > cleanup folders:")
(println "     > Delete fonts/")
(delete-directory-recursive (io/file (str directory separator "fonts")))

(println "--- Done ---")
              