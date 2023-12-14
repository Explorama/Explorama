(ns de.explorama.cli.data-transformer.transformer-helper
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [de.explorama.cli.data-transformer.parser.instances :as parsers]
            [de.explorama.cli.data-transformer.sandbox :as sandbox]
            [de.explorama.shared.data-transformer.generator :as generator]
            [de.explorama.shared.data-transformer.generator.edn-json :as edn-json-gen]
            [de.explorama.shared.data-transformer.generator.operations :as operatons-gen]
            [de.explorama.shared.data-transformer.mapping :as mapping]
            [de.explorama.shared.data-transformer.parser :as parser]
            [de.explorama.shared.data-transformer.schema :as schema]
            [de.explorama.shared.data-transformer.spec :as data-spec]
            [de.explorama.shared.data-transformer.suggestions :as suggestions]
            [malli.error :as me]
            [taoensso.timbre :refer [error]]))

#_(defn check-and-create-report [normal-result]
    (let [checking-state (chk-ctx/get-checking-state)
          error-count (:error-count checking-state)
          warn-count (:warning-count checking-state)
          report-file (str "report-" (System/currentTimeMillis) ".edn")]
      (cond
        (and error-count (pos? error-count))
        (do
          (error (format "Some error happend. See %s for more infos."
                         report-file))
          (spit report-file (with-out-str (pprint/pprint checking-state)))
          nil)
        (and warn-count (pos? warn-count))
        (do
          (warn (format "Some warning occured. See %s for more infos."
                        report-file))
          (spit report-file (with-out-str (pprint/pprint checking-state)))
          normal-result)
        :else
        normal-result)))

(defn check [fname]
  (let [^java.io.File file (io/file fname)
        content (slurp file :encoding "UTF-8")
        content (if (= "edn" (.extension file))
                  (edn/read-string content)
                  (schema/decode content))]
    (if (schema/validate content) ;TODO r1/data super dirty
      true
      (schema/explain content))))

#_(defn demo [source mapping limit extra-files]
    (let [^java.io.File mapping-f (io/file mapping)
          ^java.io.File source-f (io/file source)
          extra-files (mapv #(io/file %) extra-files)
          one-not-exist? (some (fn [^java.io.File f]
                                 (not (.exists f)))
                               extra-files)]
      (if (or (not (.exists mapping-f))
              (not (.exists source-f))
              one-not-exist?)
        (error "mapping/source/extra-file file not found." {:mapping (.getAbsolutePath mapping-f)
                                                            :source (.getAbsolutePath source-f)
                                                            :extra-files (mapv (fn [^java.io.File f]
                                                                                 (.getAbsolutePath f))
                                                                               extra-files)})
        (let [desc (assoc (sandbox/eval-mapping mapping-f extra-files
                                                :source-file source
                                                :timestamp (System/currentTimeMillis))
                          :file-name (.getAbsolutePath source-f)
                          :limit limit
                          :country-mapping (loc/prepare-alternatives config/countries))
              result (transformer/demo desc)]
          (check-and-create-report
           result)
          (spit "demo.edn" (with-out-str (pprint/pprint result)))))))

(defn operation [source mapping limit extra-files]
  (let [^java.io.File mapping-f (io/file mapping)
        ^java.io.File source-f (io/file source)
        extra-files (mapv #(io/file %) extra-files)
        one-not-exist? (some (fn [^java.io.File f]
                               (not (.exists f)))
                             extra-files)]
    (if (or (not (.exists mapping-f))
            (not (.exists source-f))
            one-not-exist?)
      (error "mapping/source/extra-file file not found." {:mapping (.getAbsolutePath mapping-f)
                                                          :source (.getAbsolutePath source-f)
                                                          :extra-files (mapv (fn [^java.io.File f]
                                                                               (.getAbsolutePath f))
                                                                             extra-files)})
      (let [desc (sandbox/eval-mapping mapping-f
                                       extra-files
                                       :source-file source
                                       :timestamp (System/currentTimeMillis))
            _ (when-not (schema/validate desc)
                (throw (ex-info "Invalid mapping" {:malli-humanize (schema/explain desc)})))
            content (parser/parse (parsers/parser-instance desc)
                                  desc
                                  (.getAbsolutePath source-f))
            instance (operatons-gen/new-instance)
            data (mapping/mapping instance
                                  desc
                                  content)
            result (generator/finalize instance
                                       data
                                       :json)]

        (if (data-spec/validate result)
          result
          (let [explain (data-spec/explain result)]
            (throw (ex-info "Generated data is not valid."
                            {:malli-explain explain
                             :malli-humanize (me/humanize explain)}))))))))

#_(defn- check-gen-files [desc out]
    (let [checking-state (chk-ctx/get-checking-state)
          error-count (:error-count checking-state)
          warn-count (:warning-count checking-state)]
      (when (and (not error-count)
                 (not warn-count))
        (info "Check generated xml-file(s).")
        (if (:chunksize desc)
          (let [split-out (str/split out #"/")
                file-prefix (peek split-out)
                folder-path (str/join "/" (pop split-out))
                all-files (if (= (count split-out) 1)
                            (.listFiles (io/file "."))
                            (.listFiles (io/file folder-path)))
                generated-files (filterv
                                 (fn [^java.io.File f]
                                   (str/starts-with? (.getName f)
                                                     file-prefix))
                                 all-files)]
            (doseq [generated-file generated-files]
              (check generated-file)))
          (check out)))))

(defn gen [source mapping target extra-files]
  (let [^java.io.File mapping-f (io/file mapping)
        ^java.io.File source-f (io/file source)
        extra-files (mapv #(io/file %) extra-files)
        one-not-exist? (some (fn [^java.io.File f]
                               (not (.exists f)))
                             extra-files)]
    (if (or (not (.exists mapping-f))
            (not (.exists source-f))
            one-not-exist?)
      (error "mapping/source/extra-file file not found." {:mapping (.getAbsolutePath mapping-f)
                                                          :source (.getAbsolutePath source-f)
                                                          :extra-files (mapv (fn [^java.io.File f]
                                                                               (.getAbsolutePath f))
                                                                             extra-files)})
      (let [desc (sandbox/eval-mapping mapping-f
                                       extra-files
                                       :source-file source
                                       :timestamp (System/currentTimeMillis))
            _ (when-not (schema/validate desc)
                (throw (ex-info "Invalid mapping" {:malli-humanize (schema/explain desc)})))
            content (parser/parse (parsers/parser-instance desc)
                                  desc
                                  (.getAbsolutePath source-f))
            instance (edn-json-gen/new-instance)
            data (mapping/mapping instance
                                  desc
                                  content)
            result (generator/finalize instance
                                       data
                                       :edn)]
        (spit target
              result
              :encoding "UTF-8")
        #_;TODO r1/mapping - validate resilts
          (if (data-spec/validate decoded)
            (spit target
                  (generator/finalize instance
                                      data
                                      :json)
                  :encoding "UTF-8")
            (let [explain (data-spec/explain decoded)]
              (throw (ex-info "Generated data is not valid."
                              {:malli-explain explain
                               :malli-humanize (me/humanize explain)}))))))))

(defn gen-mapping [source mapping target extra-files]
  (let [^java.io.File mapping-f (io/file mapping)
        ^java.io.File source-f (io/file source)
        extra-files (mapv #(io/file %) extra-files)
        one-not-exist? (some (fn [^java.io.File f]
                               (not (.exists f)))
                             extra-files)]
    (if (or (not (.exists mapping-f))
            (not (.exists source-f))
            one-not-exist?)
      (error "mapping/source/extra-file file not found." {:mapping (.getAbsolutePath mapping-f)
                                                          :source (.getAbsolutePath source-f)
                                                          :extra-files (mapv (fn [^java.io.File f]
                                                                               (.getAbsolutePath f))
                                                                             extra-files)})
      (let [desc (sandbox/eval-mapping mapping-f
                                       extra-files
                                       :source-file source
                                       :timestamp (System/currentTimeMillis))
            content (parser/parse (parsers/parser-instance desc)
                                  desc
                                  (.getAbsolutePath source-f))
            result (suggestions/create desc content)]
        (spit target
              (with-out-str (pp/pprint `(def ~'desc
                                          ~result)))
              :encoding "UTF-8")
        (spit target
              "\n\ndesc\n"
              :encoding "UTF-8"
              :append true)
        #_;TODO r1/mapping - validate resilts
          (if (data-spec/validate decoded)
            (spit target
                  (generator/finalize instance
                                      data
                                      :json)
                  :encoding "UTF-8")
            (let [explain (data-spec/explain decoded)]
              (throw (ex-info "Generated data is not valid."
                              {:malli-explain explain
                               :malli-humanize (me/humanize explain)}))))))))