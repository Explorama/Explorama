(ns de.explorama.frontend.map.map.protocol.object-manager)

(defprotocol mapObjectManager
  (create-map-instance [instance headless?])
  (map-instance [instance])

  (create-marker-layer [instance])
  (remove-marker-layer [instance])
  (marker-layer-created? [instance])
  (get-cluster-layer [instance])
  (get-marker-layer [instance])

  (create-markers [instance markers-data])
  (remove-markers [instance marker-ids])
  (clear-markers [instance])
  (marker-created? [instance marker-id])
  (get-marker-objs [instance marker-ids])
  (marker-ids [instance])

  (create-feature-layer [instance feature-layer])
  (remove-feature-layer [instance feature-layer-id])
  (feature-layer-created? [instance feature-layer-id])
  (get-feature-layer-obj [instance feature-layer-id])
  (all-feature-layers [instance])
  
  ;;Feature Layer Movement specific fns:
  (create-arrow-features [instance feature-layer-id arrow-descs])
  (clear-arrow-features [instance feature-layer-id])
  (get-arrow-features [instance feature-layer-id arrow-ids])
  (arrow-feature-ids [instance feature-layer-id])

  ;;Feature Layer Heatmap specific fns:
  (clear-heatmap-features [instance feature-layer-id])
  (create-heatmap-features [instance feature-layer-id heatmap-data])

  (create-area-features [_ feature-layer-id descs])
  (remove-area-features [_ feature-layer-id descs])

  (create-overlayer [instance overlayer])
  (remove-overlayer [instance overlayer-id])
  (overlayer-created? [instance overlayer-id])
  (get-overlayer-obj [instance overlayer-id])

  (create-base-layer [instance base-layer])
  (remove-base-layer [instance base-layer-id])
  (base-layer-created? [instance base-layer-id])
  (get-base-layer-obj [instance base-layer-id])

  (get-popup [instance])

  (destroy-instance [instace]))