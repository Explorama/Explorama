(ns de.explorama.frontend.mosaic.render.engine)

(defprotocol Engine

  (rect
    [_ stage x y h w c]
    [_ stage x y h w c args])
  (circle
    [_ stage x y r c]
    [_ stage x y r c args])
  (polygon
    [_ stage points c]
    [_ stage points c args])
  (point [_ x y])
  (text
    [_ stage text-str x y w h]
    [_ stage text-str x y w h args])
  (img
    [_ stage id x y w h]
    [_ stage id x y w h args])
  (interaction-primitive
    [_ stage on func path stage-num]
    [_ stage on func path stage-num opts])

  (merge-state! [_ values])
  (merge-in-state! [_ path values])
  (set-state! [_ new-state])
  (assoc-state! [_ values])
  (assoc-in-state! [_ path value])
  (dissoc-state! [_ values])
  (state [_])
  (label-dict [_])
  (frame-id [_])

  (set-args! [_ args])
  (args [_])

  (app [_])
  (set-app! [_ app])

  (render-funcs [_])

  (update-zoom [_ stage-key])
  (rerender [_ stage-key])
  (update-theme [_ color])
  (update-annotations [_ params])
  (update-highlights [_ params])
  (focus-event [_ event-id])
  (reset [_ stage-key])
  (render [_])
  (destroy [_])

  (resize [_ new-width new-height])
  (click [_ event-type modifer coods])
  (move-to!
    [_ stage-key x y z]
    [_ stage-key x y]
    [_ stage-key index]
    [_ stage-key x y z zoom])
  (dirty? [_])
  (dirty! [_ value]))
