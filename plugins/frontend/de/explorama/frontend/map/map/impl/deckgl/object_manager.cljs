(ns de.explorama.frontend.map.map.impl.deckgl.object-manager
  (:require [de.explorama.frontend.map.map.protocol.object-manager :as proto]))

(defn no-op [& _])

(deftype DeckGLObjectManager [frame-id extra-fns]
  proto/mapObjectManager
  (create-map-instance [_ headless?]
    (no-op frame-id headless? extra-fns))
  (map-instance [_]
    (no-op frame-id))

  (create-marker-layer [_]
    (no-op frame-id extra-fns))
  (remove-marker-layer [_]
    (no-op frame-id extra-fns))
  (marker-layer-created? [_]
    (no-op frame-id))
  (get-cluster-layer [_]
    (no-op frame-id))
  (get-marker-layer [_]
    (no-op frame-id))

  (create-markers [_ markers-data]
    (no-op frame-id extra-fns markers-data))
  (remove-markers [_ marker-ids]
    (no-op frame-id marker-ids extra-fns))
  (clear-markers [_]
    (no-op frame-id extra-fns))
  (marker-created? [_ marker-id]
    (no-op frame-id marker-id))
  (marker-ids [_]
    (no-op frame-id))
  (get-marker-objs [_ marker-ids]
    (no-op frame-id marker-ids))

  (create-feature-layer [_ feature-layer]
    (no-op frame-id extra-fns feature-layer))
  (remove-feature-layer [_ feature-layer-id]
    (no-op frame-id extra-fns feature-layer-id))
  (feature-layer-created? [_ feature-layer-id]
    (no-op frame-id feature-layer-id))
  (get-feature-layer-obj [_ feature-layer-id]
    (no-op frame-id feature-layer-id))
  (all-feature-layers [_]
    (no-op frame-id))

  (create-arrow-features [_ feature-layer-id arrow-descs]
    (no-op frame-id feature-layer-id arrow-descs))
  (clear-arrow-features [_ feature-layer-id]
    (no-op frame-id feature-layer-id))

  (create-heatmap-features [_ feature-layer-id heatmap-data]
    (no-op frame-id feature-layer-id heatmap-data))
  (clear-heatmap-features [_ feature-layer-id]
    (no-op frame-id feature-layer-id))

  (create-overlayer [_ overlayer]
    (no-op frame-id extra-fns overlayer))
  (remove-overlayer [instance overlayer-id])
  (overlayer-created? [instance overlayer-id])
  (get-overlayer-obj [_ overlayer-id]
    (no-op frame-id overlayer-id))

  (create-base-layer [_ base-layer]
    (no-op frame-id base-layer))
  (remove-base-layer [instance base-layer-id])
  (base-layer-created? [instance base-layer-id])
  (get-base-layer-obj [_ base-layer-id]
    (no-op frame-id base-layer-id))

  (get-popup [_]
    (no-op frame-id))

  (destroy-instance [_]
    (no-op frame-id extra-fns)))

(defn create-instance [frame-id extra-fns]
  (->DeckGLObjectManager frame-id extra-fns))