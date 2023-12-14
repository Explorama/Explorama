(defproject de.explorama.tools/cli-data-transformer "0.0.0"    
    :description "Cli tool for data transformation of explorama"
  :url "https://github.com/Explorama/Explorama"
  :license {:name "Eclipse Public License - v 1.0"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.babashka/sci "0.3.32"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.206"]
                 [clj-time "0.15.2"]
                 [yogthos/config "1.2.0"]
                 [org.clojure/data.csv "1.0.1"]

                 [metosin/jsonista "0.3.7"]
                 [metosin/malli "0.11.0"]

                 [org.clojure/tools.logging "1.2.4"]
                 [com.taoensso/timbre "5.2.1"]
                 [com.taoensso/tufte "2.2.0"]

                 [com.fzakaria/slf4j-timbre "0.3.21" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/log4j-over-slf4j "1.7.36"]
                 [org.slf4j/jul-to-slf4j "1.7.36"]
                 [org.slf4j/jcl-over-slf4j "1.7.36"]]

  :managed-dependencies [[org.clojure/tools.reader "1.3.6"]
                         [metosin/malli "0.8.9"]]

  :plugins [[lein-licenses "0.2.2"]
            [lein-shell "0.5.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/" "../../libs/data-transformer/src/"]
  :test-paths ["test/"]

  :clean-targets ^{:protect false} ["target"]

  :repl-options {:timeout 200000}

  :profiles
  {:uberjar {:prep-tasks ["compile"]
             :aot :all}
   :dev
   {:dependencies [[nrepl "0.9.0"]]}}

  :main de.explorama.cli.data-transformer.core

  :uberjar-name "builder.jar"
  :zip ["builder.jar" "exploramaxml.sh" "project-info/" "countries.edn"]

  :jvm-opts ["-XX:+UnlockExperimentalVMOptions"
             "-XX:+UseShenandoahGC"
             "-XX:ShenandoahGCHeuristics=compact"]
  :release-tasks [#_"Do not use lein release locally, but let the CI system build your releases for you."])
