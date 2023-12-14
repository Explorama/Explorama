(ns de.explorama.frontend.map.map.protocol.state-handler)

(defprotocol mapStateHandler
  (render-map [instance])
  (one-time-render-done-listener [instance listener-fn])
  (move-to-data [instance])
  (move-to-marker [instance marker-id])

  (set-marker-data [instance marker-data])
  (get-marker-data [instance])
  (display-marker-cluster [instance])
  (display-markers [instance]) 
  (update-marker-styles [instance to-update-ids])
  (marker-higlighted? [instance marker-id])
  (list-highlighted-marker [instance])
  (highlight-marker [instance marker-id])
  (de-highlight-marker [instance marker-id])

  (temp-hide-marker-layer [instance])
  (restore-temp-hidden-marker-layer [instance])
  
  (hide-markers-with-id [instance marker-ids])
  (display-all-markers [instance])

  (cache-event-data [instance event-id event-data])
  (cached-event-data [instance event-id])

  (set-feature-data [instance feature-data])
  (get-feature-data [instance feature-layer-id])
  (set-filtered-feature-data [instance feature-data])
  (get-filtered-feature-data [instance feature-layer-id])
  (display-feature-layer [instance feature-layer-id])
  (hide-feature-layer [instance feature-layer-id])
  (remove-feature-layer [instance feature-layer-id])
  (clear-feature-layers [instance])
  (list-active-feature-layers [instance]) 

  (display-overlayer [instance overlayer-id])
  (list-active-overlayers [instance])
  (hide-overlayer [instance overlayer-id])

  (switch-base-layer [instance base-layer-id])

  (resize-map [instance])
  (move-to [instance zoom position])
  (view-position [instance])

  (select-cluster-with-marker [instance marker-id])

  (display-popup [instance position content-desc])
  (hide-popup [instance])

  (destroy-instance [instance]))