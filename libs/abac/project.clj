(defproject de.explorama/abac "0.0.0"
  :description "Library for de.explorama.backend.abac."
  :url "https://github.com/Explorama/Explorama"
  :license {:name "Eclipse Public License - v 1.0"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[metosin/jsonista "0.3.5"]
                 [compojure/compojure "1.6.2"]
                 [cljs-ajax/cljs-ajax "0.8.4" :exclusions [cheshire]]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/core.async "1.5.648"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [yogthos/config "1.1.9"]
                 ;; Runtime dependencies to use timbre logging all over the place, even for java libraries
                 ;; --- make sure to also have the proper exclusions
                 [com.taoensso/timbre "5.1.2"]
                 [com.taoensso/carmine "3.1.0"]
                 [com.fzakaria/slf4j-timbre "0.3.21" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/log4j-over-slf4j "1.7.33"]
                 [org.slf4j/jul-to-slf4j "1.7.33"]
                 [org.slf4j/jcl-over-slf4j "1.7.33"]

                 ;; JWT Related deps
                 [buddy/buddy-core "1.10.1"]
                 [buddy/buddy-sign "3.4.1"]
                 [clj-time/clj-time "0.15.2"]

                 ; Synced deps
                 [gorillalabs/hato "2.0.1"]]

  :managed-dependencies [[org.clojure/tools.reader "1.3.6"]]

  :plugins [[lein-licenses "0.2.2"]
            [lein-shell "0.5.0"]
            [lein-parent "0.3.8"]]

  :source-paths ["src/clj"
                 "src_sync/clj"
                 "src/cljs"]

  :profiles
  {:dev  {:prep-tasks ["clean"
                       ["shell" "bash" "replace-v.sh"]]}
   :test {:source-paths ["test"]}}

  :cljsbuild {:builds [{:id           "lib"
                        :source-paths ["src/cljs"]}]}

  :jvm-opts ["-XX:+UnlockExperimentalVMOptions"
             "-XX:+UseShenandoahGC"
             "-XX:ShenandoahGCHeuristics=compact"]
  :release-tasks [#_"Do not use lein release locally, but let the CI system build your releases for you."])
