(ns de.explorama.profiling-tool.resource.cljsc-opts)

(def content
["{:output-dir \"resources/public/js/compiled-woco\", :closure-defines {\"goog.DEBUG\" true}, :optimizations :none, :preloads [devtools.preload figwheel.connect], :output-to \"resources/public/js/woco.js\", :source-map-timestamp true, :asset-path \"js/compiled-woco\", :external-config {:devtools/config {:features-to-install :all}, :figwheel/config {:on-jsload \"de.explorama.frontend.woco.app.core/reload\", :build-id \"dev-woco\", :websocket-url \"ws://[[client-hostname]]:4000/figwheel-ws\"}}, :main de.explorama.frontend.woco.app.core}"])