(defproject data-format-lib "0.0.0"
  description "Library for data-format-lib of explorama"
  :url "https://github.com/Explorama/Explorama"
  :license {:name "Eclipse Public License - v 1.0"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.597"]
                 [metosin/malli "0.8.2"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [clj-time/clj-time "0.15.2"]
                 [org.clojure/math.combinatorics "0.1.6"]
                 [com.taoensso/timbre "5.1.2"]
                 [com.taoensso/tufte "2.2.0"]
                 [funcool/cuerdas "2.2.1"]]

  :managed-dependencies [[org.clojure/tools.reader "1.3.6"]

                         ;Inconsistency inside cljs itself...
                         [com.google.code.findbugs/jsr305 "3.0.2"]
                         [com.google.errorprone/error_prone_annotations "2.0.18"]]

  :profiles {:dev
             {:prep-tasks ["clean"
                           ["shell" "bash" "replace-v.sh"]]
              :dependencies [[cider/piggieback "0.4.2"]
                             [criterium "0.4.5"]
                             [com.clojure-goes-fast/clj-memory-meter "0.1.2"]
                             [com.bhauman/figwheel-main "0.2.3"]]}
             :test {:dependencies [[doo "0.1.11"]
                                   [metosin/jsonista "0.3.3"]]}}

  :cljfmt {;; Allow two or more empty lines in a row
           :remove-consecutive-blank-lines? false}

  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
  :plugins [[lein-parent "0.3.8"]
            [lein-shell "0.5.0"]
            [org.clojure/clojure "1.10.3"]
            [org.clojure/clojurescript "1.10.597" :exclusions [org.clojure/clojure]]
            [com.google.errorprone/error_prone_annotations "2.3.4"]
            [com.google.guava/guava "28.1-jre"]
            [com.google.code.gson/gson "2.8.5"]
            [lein-licenses "0.2.2"]
            [lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.11" :exclusions [org.clojure/clojure]]]

  :source-paths ["src/cljc" "src_sync/cljc"]
  :test-paths ["test/cljc" "test/clj"]

  :doo {:build "test"
        :alias {:default [:chrome-headless]}
        :coverage {:packages [data-format-lib]}
        :karma
        {:browsers [chrome-headless]
         :launchers {:chrome-no-security {:plugin "karma-chrome-launcher"
                                          :name "Chrome_no_security"}}

         :config {"customLaunchers" {"Chrome_no_security" {"base" "Chrome"}}
                  "reporters" ["progress"  "coverage"]
                  "coverageReporter" {"dir" "target/coverage/"
                                      "reporters" [{"type" "text"
                                                    "subdir" "."
                                                    "file" "coverage.txt"}
                                                   {"type" "text-summary"
                                                    "subdir" "."
                                                    "file" "text-summary.txt"}
                                                   {"type" "json-summary"
                                                    "subdir" "."
                                                    "file" "json-summary.json"}
                                                   {"type" "html"
                                                    "subdir" "report-html"}
                                                   {"type" "cobertura"
                                                    "subdir" "."
                                                    "file" "cobertura.xml"}]}}}}

  :cljsbuild
  {:builds
   [{:id           "test"
     :source-paths ["src/cljc" "test/cljc" "test/cljs" "src_sync/cljc"]
     :compiler     {:main          data-format-lib.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :asset-path    "base/resources/public/js/compiled/test/out"
                    :optimizations :none}}]}


  :jvm-opts ["-XX:+UnlockExperimentalVMOptions"
             "-XX:+UseShenandoahGC"
             "-XX:ShenandoahGCHeuristics=compact"]

  :release-tasks [#_"Do not use lein release locally, but let the CI system build your releases for you."])
