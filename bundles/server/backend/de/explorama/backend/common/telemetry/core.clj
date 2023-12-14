
(ns de.explorama.backend.common.telemetry.core
  (:import [io.grpc ManagedChannelBuilder]
           [io.opentelemetry.exporter.otlp.trace OtlpGrpcSpanExporter]
           [io.opentelemetry.exporter.logging LoggingSpanExporter]
           [io.opentelemetry.context Context]
           [io.opentelemetry.context.propagation ContextPropagators]
           [io.opentelemetry.api.trace.propagation W3CTraceContextPropagator]
           [io.opentelemetry.api.common Attributes]
           [io.opentelemetry.sdk OpenTelemetrySdk]
           [io.opentelemetry.sdk.trace SdkTracerProvider]
           [io.opentelemetry.sdk.trace.export SimpleSpanProcessor BatchSpanProcessor]))

(defn build-exporter-logging
  []
  (LoggingSpanExporter/create))

(defn build-exporter-otlp
  [ip port]
  (let [port (Integer. port)
        channel (-> (ManagedChannelBuilder/forAddress ip port)
                    (.usePlaintext)
                    (.build))
        exporter (-> (OtlpGrpcSpanExporter/builder)
                     (.setChannel channel)
                     (.build))]
    exporter))

(defn build-otel
  [exporter processor-type]
  (let [span-processor  (case processor-type
                          :batch (.build (BatchSpanProcessor/builder exporter))
                          :simple (SimpleSpanProcessor/create exporter)
                          (.build (BatchSpanProcessor/builder exporter)))
        tracer-provider (-> (SdkTracerProvider/builder)
                            (.addSpanProcessor span-processor)
                            (.build))
        open-telemetry  (-> (OpenTelemetrySdk/builder)
                            (.setTracerProvider tracer-provider)
                            (.setPropagators (ContextPropagators/create (W3CTraceContextPropagator/getInstance)))
                            (.build))]
    open-telemetry))

(defn build-tracer
  [otel label]
  (.getTracer otel label))

(defn create-span
  ([tracer label]
   (create-span tracer label nil))
  ([tracer label parent]
   (when tracer
     (let [span (.spanBuilder tracer label)
           span (if parent
                  (.setParent span (.with (Context/current) parent))
                  span)]
       (.startSpan span)))))

(defn end-span
  [span]
  (when span
    (.end span)))

(defn- event-attributes
  [attributes]
  (let [add-attributes (fn [builder] (doseq [[k v] attributes]
                                       (.put builder (name k) (str v)))
                         builder)]
    (-> (Attributes/builder)
        (add-attributes)
        (.build))))

(defn add-event
  ([span label]
   (add-event span label nil))
  ([span label attributes]
   (when span
     (if (empty? attributes)
       (.addEvent span label)
       (.addEvent span label (event-attributes attributes))))))

(defn add-attributes
  [span attributes]
  (when span
    (for [[k v] attributes]
      (.setAttribute span (name k) (str v)))))

(def ^:dynamic *span* nil)
(def ^:dynamic *tracer* nil)

(defmacro with-new-span [[label tracer] & body]
  `(binding [*tracer* (or ~tracer *tracer*)
             *span* (create-span (or ~tracer *tracer*) ~label *span*)]
     (try ~@body (finally (end-span *span*)))))

(defn reg-event
  ([label] (reg-event label nil))
  ([label attributes]
   (add-event *span* label attributes)))

(defn reg-attributes
  [attributes]
  (add-attributes *span* attributes))