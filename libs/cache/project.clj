(defproject de.explorama/cache "0.0.0"
  :description "Library for de.explorama.shared.cache."
  :url "https://github.com/Explorama/Explorama"
  :license {:name "Eclipse Public License - v 1.0"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.taoensso/timbre "5.1.2"]
                 [com.taoensso/tufte "2.2.0"]]

  :profiles {:dev
             {:prep-tasks ["clean"
                           ["shell" "bash" "replace-v.sh"]]
              :dependencies [[cider/piggieback "0.4.2"]
                             [criterium "0.4.5"]
                             [com.clojure-goes-fast/clj-memory-meter "0.1.2"]]}
             :test {:dependencies [[metosin/jsonista "0.3.3"]]}}

  :cljfmt {;; Allow two or more empty lines in a row
           :remove-consecutive-blank-lines? false}

  :plugins [[lein-parent "0.3.8"]
            [lein-shell "0.5.0"]
            [lein-licenses "0.2.2"]]
  :source-paths ["src/clj" "src/cljc" "src_sync/cljc"]
  :test-paths ["test/cljc"]

  :jvm-opts ["-XX:+UnlockExperimentalVMOptions"
             "-XX:+UseShenandoahGC"
             "-XX:ShenandoahGCHeuristics=compact"]

  :release-tasks [#_"Do not use lein release locally, but let the CI system build your releases for you."])

