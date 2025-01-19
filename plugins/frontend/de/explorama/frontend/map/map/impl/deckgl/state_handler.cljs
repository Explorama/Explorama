(ns de.explorama.frontend.map.map.impl.deckgl.state-handler
  (:require [de.explorama.frontend.map.map.protocol.state-handler :as proto]))

(defn- no-op [& _])

(deftype DeckGLStateHandler [frame-id
                                 obj-manager-instance
                                 extra-fns]
  proto/mapStateHandler
  (render-map [_]
    (no-op frame-id extra-fns obj-manager-instance))

  (one-time-render-done-listener [_ listener-fn]
    (no-op obj-manager-instance listener-fn))

  (move-to-data [_]
    (no-op obj-manager-instance
                  @((:move-data-max-zoom extra-fns))))
  (move-to-marker [_ marker-id]
    (no-op obj-manager-instance extra-fns frame-id marker-id))

  (set-marker-data [_ marker-data]
    (no-op frame-id marker-data))

  (get-marker-data [_]
    (no-op frame-id))

  (display-marker-cluster [_]
    (no-op obj-manager-instance extra-fns frame-id))
  (display-markers [_]
    (no-op obj-manager-instance extra-fns frame-id))
  (update-marker-styles [_ to-update-ids]
    (no-op obj-manager-instance extra-fns frame-id to-update-ids))
  (marker-higlighted? [_ marker-id]
    (no-op frame-id marker-id))
  (list-highlighted-marker [_]
    (no-op frame-id))
  (highlight-marker [_ marker-id]
    (no-op obj-manager-instance
                      extra-fns
                      frame-id
                      marker-id))
  (de-highlight-marker [_ marker-id]
    (no-op obj-manager-instance
                         extra-fns
                         frame-id
                         marker-id))

  (temp-hide-marker-layer [_]
    (no-op obj-manager-instance))

  (restore-temp-hidden-marker-layer [_]
    (no-op obj-manager-instance))

  (hide-markers-with-id [_ marker-ids]
    (no-op obj-manager-instance marker-ids))
  (display-all-markers [_]
    (no-op obj-manager-instance))

  (cache-event-data [_ event-id event-data]
    (no-op frame-id event-id event-data))
  (cached-event-data [_ event-id]
    (no-op frame-id event-id))

  (set-feature-data [_ feature-data]
    (no-op frame-id feature-data))
  (get-feature-data [_ feature-layer-id]
    (no-op frame-id feature-layer-id))
  (set-filtered-feature-data [_ feature-data]
    (no-op frame-id feature-data))
  (get-filtered-feature-data [_ feature-layer-id]
    (no-op frame-id feature-layer-id))

  (display-feature-layer [_ feature-layer-id]
    (no-op frame-id obj-manager-instance feature-layer-id))
  (hide-feature-layer [_ feature-layer-id]
    (no-op frame-id obj-manager-instance feature-layer-id extra-fns))
  (remove-feature-layer [_ feature-layer-id]
    (no-op frame-id obj-manager-instance feature-layer-id extra-fns))
  (clear-feature-layers [_]
    (no-op frame-id obj-manager-instance))
  (list-active-feature-layers [_]
    (no-op frame-id))

  (display-overlayer [_ overlayer-id]
    (no-op obj-manager-instance frame-id overlayer-id))
  (list-active-overlayers [_]
    (no-op frame-id))
  (hide-overlayer [_ overlayer-id]
    (no-op obj-manager-instance frame-id overlayer-id))

  (switch-base-layer [_ base-layer-id]
    (no-op obj-manager-instance frame-id base-layer-id))

  (resize-map [_]
    (no-op obj-manager-instance))
  (move-to [_ zoom position]
    (no-op obj-manager-instance zoom position))
  (view-position [_]
    (no-op obj-manager-instance))

  (select-cluster-with-marker [_ marker-id]
    (no-op obj-manager-instance marker-id))

  (display-popup [_ position content-desc]
    (no-op frame-id
                   extra-fns
                   obj-manager-instance
                   position
                   content-desc))
  (hide-popup [_]
    (no-op frame-id obj-manager-instance))

  (destroy-instance [_]
    (no-op frame-id)))

(defn create-instance [frame-id obj-manager-instance {:keys [workspace-scale]
                                                      :as extra-fns}]
  (->DeckGLStateHandler frame-id obj-manager-instance extra-fns))