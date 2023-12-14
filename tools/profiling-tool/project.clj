(defproject de.explorama/profiling-tool "0.0.0"
  :description "Profiling tool for explorama"
  :url "https://github.com/Explorama/Explorama"
  :license {:name "Eclipse Public License - v 1.0"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.214"]
                 [clj-time "0.15.2"]
                 [yogthos/config "1.2.0"]

                 [gorillalabs/hato "2.0.1"]
                 [http.async.client "1.4.0"]
                 [http-kit/http-kit "2.6.0"]

                 [org.clojure/data.csv "1.0.1"]

                 [org.clojure/tools.logging "1.2.4"]
                 [com.taoensso/timbre "6.0.4"]
                 [com.taoensso/tufte "2.4.5"]

                 [com.fzakaria/slf4j-timbre "0.3.21" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/log4j-over-slf4j "2.0.6"]
                 [org.slf4j/jul-to-slf4j "2.0.6"]
                 [org.slf4j/jcl-over-slf4j "2.0.6"]
                 [org.slf4j/slf4j-simple "2.0.6"]

                 [etaoin "1.0.39"]
                 [com.cognitect/transit-clj "1.0.329"]]

  :managed-dependencies [[org.clojure/tools.reader "1.3.6"]
                         [metosin/malli "0.10.1"]]

  :plugins [[lein-licenses "0.2.2"]
            [lein-shell "0.5.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/"]
  :test-paths ["test/"]

  :clean-targets ^{:protect false} ["target"]

  :repl-options {:timeout 200000}

  :prep-tasks ["clean"]

  :profiles
  {:uberjar {:prep-tasks ["compile"]
             :aot :all}
   :dev
   {:dependencies [[nrepl "1.0.0"]]}}

  :main de.explorama.profiling-tool.core

  :uberjar-name "profiling-tool.jar"
  :zip ["profiling-tool.jar" "builder.sh" "project-info/"]

  :jvm-opts ["-XX:+UnlockExperimentalVMOptions"
             "-XX:+UseShenandoahGC"
             "-XX:ShenandoahGCHeuristics=compact"]
  :release-tasks [#_"Do not use lein release locally, but let the CI system build your releases for you."])
