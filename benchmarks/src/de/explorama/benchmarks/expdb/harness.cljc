(ns de.explorama.benchmarks.expdb.harness
  (:require [clojure.string :as str]
            [de.explorama.benchmarks.expdb.data :as data]
            #?(:clj [clojure.java.io :as io])
            #?(:clj [clojure.java.shell :as shell])))

#?(:cljs (def ^:private node-fs (js/require "fs")))
#?(:cljs (def ^:private node-cp (js/require "child_process")))

(defn now-ms []
  #?(:clj (/ (System/nanoTime) 1000000.0)
     :cljs (.now js/performance)))

(defn timestamp []
  #?(:clj (str (java.time.Instant/now))
     :cljs (.toISOString (js/Date.))))

(defn git-sha []
  (str/trim
   #?(:clj (:out (shell/sh "git" "rev-parse" "--short" "HEAD"))
      :cljs (.toString (.execSync node-cp "git rev-parse --short HEAD")))))

(defn check-failure! [scenario-key result]
  (if (and (map? result) (false? (:success result)))
    (throw (ex-info (str "storage operation failed in " scenario-key)
                    {:scenario scenario-key :result result}))
    result))

(defn- stats [samples]
  (let [sorted (vec (sort samples))
        n (count sorted)
        mid (quot n 2)]
    {:min (first sorted)
     :max (peek sorted)
     :median (if (odd? n)
               (nth sorted mid)
               (/ (+ (nth sorted (dec mid)) (nth sorted mid)) 2.0))
     :mean (/ (reduce + sorted) (double n))}))

(defn run-scenario [{:keys [key setup! run! teardown!]}
                    {:keys [warmup iterations] :or {warmup 1 iterations 5}}]
  (let [sample! (fn []
                  (when setup! (setup!))
                  (let [start (now-ms)
                        result (run!)
                        elapsed (- (now-ms) start)]
                    (check-failure! key result)
                    (when teardown! (teardown!))
                    elapsed))]
    (dotimes [_ warmup] (sample!))
    {:iterations iterations
     :ms (stats (vec (repeatedly iterations sample!)))}))

(defn assert-deterministic! []
  (when-not (and (= (data/kv-pairs 42 5 200) (data/kv-pairs 42 5 200))
                 (= (data/import-payload 42 2 3 4) (data/import-payload 42 2 3 4)))
    (throw (ex-info "benchmark data generators are not deterministic" {}))))

(defn- pad [s n]
  (let [s (str s)]
    (str s (apply str (repeat (max 0 (- n (count s))) " ")))))

(defn- fmt-ms [x]
  (str (/ (Math/round (* 100.0 x)) 100.0)))

(defn print-report [{:keys [bundle backend git-sha scenarios]}]
  (println (str "expdb benchmark - bundle " (name bundle)
                ", backend " (name backend)
                ", sha " git-sha))
  (println (str (pad "scenario" 22) (pad "median" 10) (pad "mean" 10)
                (pad "min" 10) (pad "max" 10) "(ms)"))
  (doseq [[k {:keys [ms]}] (sort-by (comp name key) scenarios)]
    (println (str (pad (name k) 22)
                  (pad (fmt-ms (:median ms)) 10)
                  (pad (fmt-ms (:mean ms)) 10)
                  (pad (fmt-ms (:min ms)) 10)
                  (pad (fmt-ms (:max ms)) 10)))))

(defn write-results! [dir {:keys [bundle backend] :as results}]
  (let [file (str dir "/"
                  (str/replace (:timestamp results) #"[:.]" "-")
                  "-" (name bundle) "-" (name backend) ".edn")]
    #?(:clj (do (.mkdirs (io/file dir))
                (spit file (pr-str results)))
       :cljs (do (.mkdirSync node-fs dir #js{:recursive true})
                 (.writeFileSync node-fs file (pr-str results))))
    file))
