#!/usr/bin/env bb
(require '[clojure.string :as st]
         '[clojure.java.io :as io])

(def file-sep (java.io.File/separator))
(defn- add-to-path [path & add]
  (st/join file-sep
           (into [path] add)))

(def root-path "../..")

(defn namespace-filter [namespace & [ignore-namespaces]]
  (and
   (st/starts-with? namespace "de.explorama.")
   (or (not ignore-namespaces)
       (not (ignore-namespaces namespace)))))

(def namespace-block-regex #"(\(ns\s[^\)]+)*\)")
(def ns-regex {:pattern #"\(ns\s+(\S+)\b"
               :symbol-fn #(when (<= 2 (count %))
                             (st/trim (str (second %))))})
(def event-effect-regex {:pattern #"\(\S*/?(reg-event-fx|reg-event-db)\s+(\S+)\b"
                         :symbol-fn #(when (<= 3 (count %))
                                       (st/trim (str (get % 2))))})

(def event-sub-regex {:pattern #"\(\S*/?reg-sub\s+(\S+)\b"
                      :symbol-fn #(when (<= 2 (count %))
                                    (st/trim (str (second %))))})

(def fn-regex {:pattern #"^(?!\;)\(defn\-?\s+(\^\S+\s+)?(\S+).*"
               :symbol-fn #(when (<= 3 (count %))
                             (st/trim (str (get % 2))))})
(def var-regex {:pattern #"^(?!\;)\(def\s+(\^\S+\s+)?(\S+).*"
                :symbol-fn #(when (<= 3 (count %))
                              (st/trim (str (get % 2))))})

(defn- check-match [acc s
                    {:keys [pattern symbol-fn]}
                    & [{:keys [result-type line]}]]
  (if-let [matches (re-find pattern s)]
    (if-let [symbol (symbol-fn matches)]
      (cond (map? (get acc result-type))
            (update acc result-type
                    assoc
                    symbol
                    {:line line})
            result-type
            (assoc acc result-type symbol)
            :else symbol)
      (do
        (println "SYMBOL EMPTY" (vec matches))
        acc))
    acc))

(defn find-alias-usage [s sym]
  (let [pattern (re-pattern (str "\\" sym "/(\\S+)"))
        matches (re-seq pattern s)]
    (reduce (fn [acc [_ use-sym]]
              (cond-> acc
                use-sym (conj (st/replace (st/trim (str use-sym))
                                          ")" ""))))
            #{}
            matches)))

(defn- extract-requires [meta-block]
  (when meta-block
    (try
      (when-let [idx (or (st/index-of meta-block "(:require\n")
                         (st/index-of meta-block "(:require "))]
        (-> (str "["
                 (subs meta-block (+ idx (count "(:require "))))
            (st/replace "#?(" "")
            (st/replace ":cljs" "")
            (st/replace ":clj" "")
            (st/replace ")" "")
            (str "]")
            (read-string)))
      (catch Throwable e
        (println "   Failed extract-requires for" meta-block)
        (println e)))))

(defn- solve-require-statement [require-statement]
  (let [is-vec? (vector? require-statement)
        require-namespace (if is-vec?
                            (first require-statement)
                            require-statement)
        check-refers? (and is-vec?
                           (>= 1 (count require-statement)))
        get-fn (fn [search-for]
                 (when-let [idx (when check-refers?
                                  (.indexOf require-statement search-for))]
                   (when (<= 0 idx)
                     (get require-statement (inc idx)))))
        as-symbol (get-fn :as)
        refer-symbols (get-fn :refer)
        refer-macros (get-fn :refer-macros)]
    (cond-> {:require-ns (str require-namespace)}
      refer-symbols
      (assoc :in-use (set (mapv #(st/trim (str %))
                                refer-symbols)))
      refer-macros
      (update :in-use (fn [o] (apply conj (or o #{})
                                     (mapv #(st/trim (str %))
                                           refer-macros))))
      as-symbol
      (assoc :as (st/trim (str as-symbol))))))

(defn gather-metas [s]
  (when-let [meta-block (first (re-find namespace-block-regex s))]
    (let [namespace (check-match {} meta-block ns-regex)
          require-statements  (extract-requires meta-block)]
      (reduce (fn [acc require-statement]
                (try
                  (let [{:keys [require-ns in-use as] :as m}
                        (solve-require-statement require-statement)
                        used-via-alias (if (and as (seq as))
                                         (find-alias-usage s as)
                                         #{})
                        in-use (apply conj
                                      (or in-use #{})
                                      used-via-alias)]
                    (cond-> acc
                      (and m (namespace-filter require-ns))
                      (update-in
                       [:requires require-ns]
                       (fn [o]
                         (cond-> (or o {})
                           (seq in-use)
                           (update :in-use #(apply conj (or % #{}) in-use)))))))
                  (catch Throwable e
                    (println "   Failed analyse require for"
                             namespace
                             require-statement)
                    (println e)
                    acc)))

              {:namespace namespace
               :requires {}}
              require-statements))))

(defn remove-self-usage-symbols [state s]
  (let [state (reduce (fn [state [sym]]
                        (if (or (st/includes? s (str "(" sym ")"))
                                (st/includes? s (str "(" sym " "))
                                (st/includes? s (str "(" sym "\n"))
                                (st/includes? s (str " " sym " "))
                                (st/includes? s (str " " sym "\n"))
                                (st/includes? s (str "\n" sym "\n")))
                          (update state :fns dissoc sym)
                          state))
                      state
                      (:fns state))
        state (reduce (fn [state [sym]]
                        (if (or (st/includes? s (str " " sym " "))
                                (st/includes? s (str " " sym "\n"))
                                (st/includes? s (str "\n" sym "\n")))
                          (update state :vars dissoc sym)
                          state))
                      state
                      (:vars state))]
    state))

(defn gather-symbols [s]
  (->
   (reduce (fn [acc [line s]]
             (-> acc
                 (check-match s fn-regex {:line line :result-type :fns})
                 (check-match s var-regex {:line line :result-type :vars})))
           {:fns {}
            :vars {}}
           (map-indexed #(vector %1 %2)
                        (st/split s #"\n")))
   (remove-self-usage-symbols s)))

(defn remove-ns-requires [state org-namespace requires]
  (let [namespaces (keys requires)]
    (reduce (fn [state namespace]
              (if (get-in state [:analysed-files namespace])
                (let [required-symbols (get-in requires [namespace :in-use] #{})]
                  (-> state
                      (update-in [:analysed-files namespace :vars] #(apply dissoc % required-symbols))
                      (update-in [:analysed-files namespace :fns] #(apply dissoc % required-symbols))
                      (update-in [:analysed-files org-namespace :requires] dissoc namespace)))
                state))
            state
            namespaces)))

(defn remove-requires [state]
  (reduce (fn [state [namespace {:keys [requires]}]]
            (remove-ns-requires state namespace requires))
          state
          (:analysed-files state)))

(defn analyse-file [state path]
  (let [t (slurp path)
        {:keys [requires namespace] :as analyse-result}
        (merge
         (gather-metas t)
         (gather-symbols t))]
    (-> state
        (assoc-in [:analysed-files namespace] analyse-result)
        (update :used-namespaces #(apply conj (or % #{})
                                         (filter namespace-filter
                                                 (keys requires))))
        (remove-ns-requires namespace requires))))

(defn print-summary [{:keys [used-namespaces analysed-files]} ignore-namespaces]
  (let [sym-formatter (fn [[sym {:keys [line]}]]
                        (str sym " (line: " line ")"))]
    (doseq [[_ {:keys [namespace fns vars]}] (sort-by #(str (get-in % [1 :namespace]))
                                                      (vec analysed-files))]
      (when (namespace-filter namespace ignore-namespaces)
        (if (not (used-namespaces namespace))
          (println " !!! " namespace "is not in use")
          (when (or (seq fns)
                    (seq vars))
            (println " --- " namespace)
            (when (seq fns)
              (println "      >>> Unused functions: ")
              (doseq [fn-str (sort (map sym-formatter fns))]
                (println "         " fn-str)))
            (when (seq vars)
              (println "      >>> Unused vars: ")
              (doseq [var-str (sort (map sym-formatter vars))]
                (println "         " var-str)))
            (println)))))))

(defn analyse-files [paths ignore-namespaces]
  (-> (reduce analyse-file {} paths)
      (remove-requires)
      (print-summary ignore-namespaces)))

(def line-endings #{".cljs" ".clj" ".cljc"})

(defn analyse-bundle [bundle-name folder-paths & [ignore-namespaces]]
  (let [f-filter (fn [f]
                   (let [file-name (.getName f)]
                     (and
                      (.isFile f)
                      (some #(st/ends-with? file-name %)
                            line-endings))))
        files (reduce (fn [acc path]
                        (apply conj
                               acc
                               (mapv #(.getAbsolutePath %)
                                     (filter f-filter
                                             (file-seq (io/file path))))))
                      #{}
                      folder-paths)]
    (println "Analyse bundle" bundle-name)
    (println "    files"  (count files))
    (analyse-files (sort files) ignore-namespaces)

    (println "---------------------------------------------------")))

;; (clojure.pprint/pprint
;;  (analyse-file
;;   {}
;;   (add-to-path root-path "bundles" "browser" "frontend-integrations"
;;                "woco" "frontend" "de" "explorama" "frontend" "woco" "app" "core.cljs")))

;; Which are entry points and not required by others
(def root-files #{;;App
                  "de.explorama.main.core"
                  "de.explorama.backend.core"
                  "de.explorama.frontend.woco.app.core"
                  "de.explorama.backend.woco.app.dev-core"
                  ;;Tests
                  "de.explorama.backend.runner"
                  "de.explorama.frontend.runner"
                  ;;libs"
                  "de.explorama.frontend.ui-base.overview.core"
                  ;;Tools
                  "de.explorama.cli.data-transformer.core"
                  "de.explorama.profiling-tool.core"})


(analyse-bundle "All"
                #{(add-to-path root-path "libs")
                  (add-to-path root-path "plugins")
                  (add-to-path root-path "frontend-integrations")
                  (add-to-path root-path "bundles" "browser" "frontend")
                  (add-to-path root-path "bundles" "browser" "backend")
                  (add-to-path root-path "bundles" "browser" "frontend-integrations")
                  (add-to-path root-path "bundles" "browser" "test")
                  (add-to-path root-path "bundles" "electron" "main")
                  (add-to-path root-path "bundles" "electron" "frontend")
                  (add-to-path root-path "bundles" "electron" "backend")
                  (add-to-path root-path "bundles" "electron" "frontend-integrations")
                  (add-to-path root-path "bundles" "electron" "test")
                  ;; (add-to-path root-path "bundles" "server" "frontend")
                  ;; (add-to-path root-path "bundles" "server" "backend")
                  ;; (add-to-path root-path "bundles" "server" "frontend-integrations")
                  ;; (add-to-path root-path "bundles" "server" "test")
                  (add-to-path root-path "tools" "cli-data-transformer" "src")
                  (add-to-path root-path "tools" "profiling-tool" "src")}
                root-files)