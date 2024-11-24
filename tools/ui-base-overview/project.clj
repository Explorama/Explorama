(defproject de.explorama/ui-base "0.0.0"
  :description "Collection of basic ui components for explorama"
  :url "https://github.com/Explorama/Explorama"
  :license {:name "Eclipse Public License - v 1.0"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.9.1"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.597"]
                 [metosin/malli "0.8.2"]
                 [reagent/reagent "1.0.0"] ;"0.10.0"
                 [cljsjs/blueprintjs-core "3.34.0-0"]
                 [cljsjs/blueprintjs-datetime "3.19.3-0"]
                 [cljsjs/rc-slider "9.7.1-1"]
                 [cljsjs/resumable.js "1.1.0-0"]
                 [cljsjs/date-fns "2.20.2-1"]
                 [cljsjs/react-tooltip-lite "1.11.2-0" :exclusions [cljsjs/react-dom]]
                 [cljsjs/react-virtualized "9.21.1-0"]
                 [cljsjs/react-number-format "4.4.4-0"]

                 [com.taoensso/timbre "5.1.2"]
                 [com.taoensso/tufte "2.2.0"]]

  :managed-dependencies [[org.clojure/tools.reader "1.3.6"]

                         ;Inconsistency inside cljs itself...
                         [com.google.code.findbugs/jsr305 "3.0.2"]
                         [com.google.errorprone/error_prone_annotations "2.0.18"]]

  :plugins [[lein-explorama-sync "0.13.0" :exclusions [org.clojure/tools.cli]] ;???
            [lein-parent "0.3.8"]
            [lein-shell "0.5.0"]
            [lein-cljsbuild "1.1.8" :exclusions [org.apache.commons/commons-compress]]
            [lein-exec "0.3.7"]
            [lein-licenses "0.2.2"]
            [lein-doo "0.1.11" :exclusions [org.clojure/google-closure-library-third-party
                                            org.clojure/google-closure-library
                                            org.clojure/clojure
                                            org.clojure/clojurescript]]]


  :source-paths ["src/cljs/lib"]
  :test-paths ["test/clj/" "test/cljs"]
  :jar-exclusions [#"public"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/public/js/overview-compiled"
                                    "target"
                                    "test/js"]
  :repl-options {:timeout 1200000}
                ; :init-ns de.explorama.frontend.ui-base.core}

  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.6"]
                                  [nrepl "1.0.0"]
                                  [figwheel-sidecar "0.5.20"]
                                  [markdown-clj "1.11.3"]
                                  [cljsjs/highlight "11.5.1-0"]
                                  [fipp "0.6.26"]
                                  [re-frame/re-frame "1.2.0"]] ;"0.12.0"
                   :figwheel {:css-dirs ["resources/public/css"]
                              :server-port 13600}
                   :source-paths ["src/cljs/lib" "src/cljs/overview"]
                   :prep-tasks ["clean"
                                ["shell" "bash" "replace-v.sh"]
                                ["shell" "bash" "dl_style_assets.sh"]
                                ["shell" "cp" "-rf" "css" "resources/public/"]
                                ["shell" "cp" "-rf" "fonts" "resources/public/"]
                                ["shell" "cp" "-rf" "img" "resources/public/"]]
                   :plugins [[lein-figwheel "0.5.20"]]}
             :test {:source-paths ["src/clj/" "test/clj/"]
                    :prep-tasks [["shell" "bash" "replace-v.sh"]]}
             :overview-build {:dependencies [[cljsjs/highlight "11.5.1-0"]
                                             [markdown-clj "1.11.3"]
                                             [fipp "0.6.26"]
                                             [re-frame/re-frame "1.2.0"]
                                             [cljsjs/react "17.0.2-0-prod"]
                                             [cljsjs/react-dom "17.0.2-0-prod"]
                                             [cljsjs/react-dom-server "17.0.2-0-prod"]]
                              :prep-tasks ["clean"
                                           ["shell" "bash" "replace-v.sh"]]
                              :source-paths ["src/cljs/lib" "src/cljs/overview"]}}

  :aliases {"dev" ["do"
                   "clean"
                   ["with-profile" "+dev" "cljsbuild" "once" "dev"]
                   ["figwheel" "dev"]]
            ;overwrite figwheel to ensure prep-task of dev-profile is executed
            "figwheel" ["do"
                        "clean"
                        ["with-profile" "+dev" "cljsbuild" "once" "dev"]
                        ["figwheel" "dev"]]
            "overview-build" ["with-profile" "overview-build" "cljsbuild" "once" "standalone-overview"]}

  :doo {:build "test"
        :alias {:default [:chrome-headless]}
        :coverage {:packages [de.explorama.ui-base]}
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
   [{:id           "dev"
     :source-paths ["src/cljs/lib" "src/cljs/overview"]
     :figwheel     {:before-jsload "de.explorama.frontend.ui-base.overview.core/before-load"
                    :on-jsload "de.explorama.frontend.ui-base.overview.core/mount-root"
                    :websocket-host :js-client-host}
     :compiler     {:main                 de.explorama.frontend.ui-base.overview.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}
    {:id           "standalone-overview"
     :source-paths ["src/cljs/lib" "src/cljs/overview"]
     :compiler     {:main                 de.explorama.frontend.ui-base.overview.core
                    :output-to            "target/overview/overview.js"
                    :output-dir           "target/overview-compiled/js-files"
                    :closure-defines {"goog.DEBUG" false
                                      cljs.core/*global* "window"} ;; needed for advanced
                    :optimizations   :advanced
                    :infer-externs   true
                    ;;debugging flags
                    ;; :pseudo-names    true
                    ;; :pretty-print    true
                    ;; :verbose true
                    :source-map-path "target/overview/overview.js.map"
                    :source-map      "target/overview/overview.js.map"
                    :asset-path           "js-files"}}

    {:id           "test"
     :source-paths ["src/cljs/lib" "test/cljs"]
     :compiler     {:main          lib.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :asset-path    "base/resources/public/js/compiled/test/out"
                    :optimizations :none}}]})
