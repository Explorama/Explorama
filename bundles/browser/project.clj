(require '[clojure.string :as st]
         '[clojure.pprint :refer [pprint]])

(def app-version "0.0.0")
(def ml-regression-simple-linear-version "2.0.4")

(def dev-envs {"goog.DEBUG" true
               "de.explorama.shared.woco.config.DEFAULT_LOG_LEVEL" "debug"
               "de.explorama.shared.woco.config.RUNTIME_MODE" "dev"})

(def prod-envs {"goog.DEBUG" false
                "de.explorama.shared.woco.config.DEFAULT_LOG_LEVEL" "info"
                "de.explorama.shared.woco.config.RUNTIME_MODE" "prod"})

(def file-sep (java.io.File/separator))
(defn- add-to-path [path & add]
  (st/join file-sep
           (into [path] add)))

(defn- shell-add-to-path [path & add]
  ;;Dont know why, but path with java file sep and shell dont works
  (st/join "/"
           (into [path] add)))

(def shell-exec (if (st/includes?
                     (st/lower-case (System/getProperty "os.name"))
                     "windows")
                  "sh"
                  "bash"))

(def shell-build-dist-folder (shell-add-to-path ".." ".." "dist" "browser"))
(def shell-build-profiling-dist-folder (shell-add-to-path ".." ".." "dist" "browser-profiling"))

(def build-dist-folder (add-to-path ".." ".." "dist" "browser"))
(def build-profiling-dist-folder (add-to-path ".." ".." "dist" "browser-profiling"))
(def plugins-paths (add-to-path ".." ".." "plugins"))
(def libs-paths (add-to-path ".." ".." "libs"))
(def assets-paths (add-to-path ".." ".." "assets"))
(def frontend-integrations-paths (add-to-path ".." ".." "frontend-integrations"))
(def tools-paths (add-to-path ".." ".." "tools"))

(def fixed-frontend-folders #{(add-to-path "frontend")
                              (add-to-path assets-paths)
                              (add-to-path plugins-paths "shared")
                              (add-to-path plugins-paths "frontend")
                              (add-to-path libs-paths "ui-base" "src" "cljs" "lib")
                              (add-to-path libs-paths "data-format-lib" "src" "cljc")
                              (add-to-path frontend-integrations-paths "woco" "frontend")
                              (add-to-path frontend-integrations-paths "woco" "shared")
                              (add-to-path "frontend-integrations" "woco" "frontend")})

(def fixed-frontend-test-folders #{(add-to-path "test" "frontend")
                                   (add-to-path plugins-paths "frontend_test")
                                   (add-to-path plugins-paths "shared_test")})

(def fixed-backend-folders #{(add-to-path "backend")
                             (add-to-path assets-paths)
                             (add-to-path plugins-paths "shared")
                             (add-to-path plugins-paths "backend")
                             (add-to-path libs-paths "cache" "src" "cljc")
                             (add-to-path libs-paths "data-format-lib" "src" "cljc")
                             (add-to-path libs-paths "data-transformer" "src")
                             (add-to-path "frontend-integrations" "woco" "backend")
                             (add-to-path frontend-integrations-paths "woco" "shared")
                             (add-to-path frontend-integrations-paths "woco" "backend")})

(def fixed-backend-test-folders #{(add-to-path "test" "backend")
                                  (add-to-path plugins-paths "backend_test")
                                  (add-to-path plugins-paths "shared_test")})

(def fixed-profiling-folders #{(add-to-path tools-paths "profiling-tool" "src" "shared")
                               (add-to-path tools-paths "profiling-tool" "src" "browser")
                               (add-to-path tools-paths "profiling-tool" "gen_src" "browser")})

(def source-folders {:frontend-paths (vec (sort fixed-frontend-folders))
                     :frontend-test-paths (vec (sort (set (concat fixed-frontend-folders
                                                                  fixed-frontend-test-folders
                                                                  fixed-backend-folders)))) ;This is required because of the way the browser version is built
                     :backend-paths (vec (sort fixed-backend-folders))
                     :backend-test-paths (vec (sort (set (concat fixed-frontend-folders
                                                                 fixed-backend-test-folders
                                                                 fixed-backend-folders))))
                     :profiling-paths (vec (sort fixed-profiling-folders))})
(println "---------------------------")
(println "Using the following sources (woco):")
(pprint source-folders)
(println "")

(defproject explorama app-version
  :description "Browser version of explorama"
  :url "https://github.com/Explorama/Explorama"
  :license {:name "Eclipse Public License - v 1.0"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]
                 [org.clojure/core.async "1.6.681"]


                 [mount "0.1.17"]
                 [cljsjs/quill "1.3.7-0"]

                 ;; Common
                 [com.taoensso/timbre "5.1.2"]
                 [com.taoensso/tufte "2.2.0"]

                 [expound/expound "0.9.0"] ;TODO r1/deps remove this once we do not use spec any more
                 [io.github.cljsjs/papaparse "5.4.1-0"]

                 ;; Client-side
                 [cljsjs/moment "2.29.4-0"]
                 [io.github.cljsjs/bezier-easing "2.1.0-0"]
                 [metosin/malli "0.12.0"]
                 [cljsjs/blueprintjs-core "4.6.0-0"]
                 [cljsjs/blueprintjs-datetime "4.3.2-0"]
                 [cljsjs/react-beautiful-dnd "12.2.0-2"]
                 [cljsjs/rc-slider "9.7.1-1"]
                 [cljsjs/resumable.js "1.1.0-0"]
                 [cljsjs/date-fns "2.30.0-0"]
                 [cljsjs/react-tooltip-lite "1.11.2-0" :exclusions [cljsjs/react-dom]]
                 [cljsjs/react-virtualized "9.21.1-0"]
                 [cljsjs/react-window "1.8.7-0"]
                 [cljsjs/react-number-format "4.4.4-0"]

                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [clj-time/clj-time "0.15.2"]
                 [org.clojure/math.combinatorics "0.2.0"]

                 ;;1.11.11-1 is a forked version with fixing scrollbars which contains changes from https://github.com/2knu/html-to-image/tree/with_scrollbar_fixes
                 [io.github.cljsjs/html-to-image-mod "1.11.11-1"]
                 [cljsjs/jspdf "2.5.1-0"]
                 [cljsjs/pixi-legacy "7.2.4-0"]
                 [cljsjs/react-toastify "9.1.3-0" :exclusions [cljsjs/react-dom]]
                 [cljsjs/re-resizable "6.9.9-0" :exclusions [cljsjs/react-dom]]
                 [cljsjs/react-autosuggest "10.0.2-0" :exclusions [cljsjs/react-dom]]
                 [day8.re-frame/http-fx "0.2.4" :exclusions [cljsjs/react-dom]]
                 [re-frame-utils/re-frame-utils "0.1.0" :exclusions [cljsjs/react-dom]]
                 [funcool/cuerdas "2022.06.16-403"]
                 [cljsjs/crypto-js "4.1.1-0"]

                 [cljsjs/chartjs "3.9.1-0"]
                 [io.github.cljsjs/chartjs-adapter-date-fns "2.0.0-0" :exclusions [cljsjs/chartjs]]
                 [cljsjs/moment "2.29.4-0"]
                 [cljsjs/vis "4.21.0-1"]
                 [io.github.cljsjs/react-d3-cloud "1.0.5-0"]
                 [cljsjs/seedrandom "3.0.5-0"]
                 [cljsjs/openlayers "6.15.1-0"]
                 [io.github.cljsjs/openlayers-ol-ext "3.2.30-0"]

                 [prismatic/dommy "1.1.0"]

                 [reagent/reagent "1.0.0"]
                 [cljsjs/react "17.0.2-0"]
                 [cljsjs/react-dom "17.0.2-0"]
                 [cljsjs/react-dom-server "17.0.2-0"]
                 [re-frame/re-frame "1.2.0"]]


  :plugins [[lein-cljsbuild "1.1.8" :exclusions [org.apache.commons/commons-compress]]
            [lein-licenses "0.2.2"]
            [lein-shell "0.5.0"]
            [lein-doo "0.1.11" :exclusions [org.clojure/clojure]]]
  :min-lein-version "2.5.3"
  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.7"]
                                  [nrepl "1.0.0"]
                                  [figwheel-sidecar "0.5.20"]]
                   :plugins [[lein-figwheel "0.5.20"]]
                   :figwheel {:server-port 4000}}
             :dev-benchmark {:dependencies [[binaryage/devtools "1.0.7"]
                                            [nrepl "1.0.0"]
                                            [figwheel-sidecar "0.5.20"]]
                             :plugins [[lein-figwheel "0.5.20"]]
                             :figwheel {:server-port 4000}}
             :test {:dependencies [[doo "0.1.11"]]}
             :build {}}
  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ~(into (:frontend-paths source-folders)
                          (:backend-paths source-folders))
     :figwheel     {:on-jsload "de.explorama.frontend.woco.app.core/reload"
                    :websocket-host :js-client-host}
     :compiler     {:main                 de.explorama.frontend.woco.app.core
                    :output-to            "resources/public/js/woco.js"
                    :output-dir           "resources/public/js/compiled"
                    :asset-path           "js/compiled"
                    :closure-defines      ~dev-envs
                    :parallel-build true
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    :language-in     :ecmascript-next
                    :npm-deps {:ml-regression-simple-linear ~ml-regression-simple-linear-version}}}
    {:id           "dev-benchmark"
     :source-paths ~(vec (concat (:frontend-paths source-folders)
                                 (:backend-paths source-folders)
                                 (:profiling-paths source-folders)))
     :figwheel     {:on-jsload "de.explorama.frontend.woco.app.core/reload"
                    :websocket-host :js-client-host}
     :compiler     {:main                 de.explorama.profiling-tool.core
                    :output-to            "resources/public/js/woco_benchmark.js"
                    :output-dir           "resources/public/js/compiled_benchmark"
                    :asset-path           "js/compiled_benchmark"
                    :closure-defines      ~dev-envs
                    :parallel-build true
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    :language-in     :ecmascript-next
                    :npm-deps {:ml-regression-simple-linear ~ml-regression-simple-linear-version}}}

    {:id           "min"
     :source-paths ~(into (:frontend-paths source-folders)
                          (:backend-paths source-folders))
     :compiler     {:main                 de.explorama.frontend.woco.app.core
                    :output-to           ~(add-to-path build-dist-folder "js" "woco.js")
                    :output-dir          ~(add-to-path build-dist-folder "js" "woco-sources")
                    :closure-defines ~prod-envs
                    :parallel-build true
                    :optimizations   :simple
                    :infer-externs   true
                    :language-in     :ecmascript-next
                    :npm-deps {:ml-regression-simple-linear ~ml-regression-simple-linear-version}}}
                    ;;debugging flags
                    ;; :pseudo-names    true
                    ;; :pretty-print    true
                    ;; :verbose true

    {:id           "min-benchmark"
     :source-paths ~(vec (concat (:frontend-paths source-folders)
                                 (:backend-paths source-folders)
                                 (:profiling-paths source-folders)))
     :compiler     {:main                 de.explorama.profiling-tool.core
                    :output-to           ~(add-to-path build-profiling-dist-folder "js" "woco.js")
                    :output-dir          ~(add-to-path build-profiling-dist-folder "js" "woco-sources")
                    :closure-defines ~prod-envs
                    :parallel-build true
                    :optimizations   :simple
                    :infer-externs   true
                    :language-in     :ecmascript-next
                    :npm-deps {:ml-regression-simple-linear ~ml-regression-simple-linear-version}}}

    {:id           "test-frontend"
     :source-paths ~(:frontend-test-paths source-folders)
     :compiler     {:main          de.explorama.frontend.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :asset-path    "base/resources/public/js/compiled/test/out"
                    :closure-defines ~dev-envs
                    :parallel-build true
                    :optimizations :none
                    :language-in     :ecmascript-next}}
    {:id           "test-backend"
     :source-paths ~(:backend-test-paths source-folders)
     :compiler     {:main          de.explorama.backend.runner
                    :output-to     "resources/public/js/compiled/test-backend.js"
                    :output-dir    "resources/public/js/compiled/test/out-backend"
                    :asset-path    "base/resources/public/js/compiled/test/out"
                    :closure-defines ~dev-envs
                    :parallel-build true
                    :optimizations :none
                    :language-in     :ecmascript-next
                    :npm-deps {:ml-regression-simple-linear ~ml-regression-simple-linear-version}}}]}

  :doo {:alias {:default [:chrome-headless]}
        :coverage {:packages ["de.explorama.*"]}
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
  :aliases {"test" ["do"
                    ["with-profile" "test" "doo" "chrome-headless" "test-frontend" "once"]
                    ["with-profile" "test" "doo" "chrome-headless" "test-backend" "once"]]
            "test-frontend" ["do"
                             ["shell" "make" "prepare-test"]
                             ["with-profile" "test" "doo" "chrome-headless" "test-frontend" "once"]]
            "test-backend" ["do"
                            ["shell" "make" "prepare-test"]]
            ;;                 ["with-profile" "test" "doo" "chrome-headless" "test-backend" "once"]]
            ;; "build" ["do"
            ;;          "clean"
            ;;          ["shell" "rm" "-rf" ~(shell-add-to-path shell-build-dist-folder)]
            ;;          ["shell" ~shell-exec "gather-assets.sh" "prod"]
            ;;          ["with-profile" "build" "cljsbuild" "once" "min"]
            ;;          ["shell" "rm" "-rf" ~(shell-add-to-path shell-build-dist-folder "js" "woco-sources")]]
            "build-benchmark" ["do"
                               ["shell" "rm" "-rf" ~(shell-add-to-path shell-build-profiling-dist-folder)]
                               ["shell" ~shell-exec "gather-assets.sh" "benchmark"]
                               ["with-profile" "build" "cljsbuild" "once" "min-benchmark"]
                               ["shell" "rm" "-rf" ~(shell-add-to-path shell-build-profiling-dist-folder "js" "woco-sources")]]})
