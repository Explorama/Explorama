(ns de.explorama.frontend.woco.path
  (:require [taoensso.timbre :refer [error]]))

(def root-key :woco)
(def root [root-key])

(def workspace-rect (conj root :workspace-rect))
(def global-loadingscreen (conj root :global-loadingscreen))

(def scale-info (conj root :scale-info))

(def plugins-init-done-key :plugins-init-done)
(def plugins-init-done (conj root plugins-init-done-key))

(def url-info-key :url-info)
(def url-info (conj root url-info-key))

(def data-instances-key :data-instances)

(def direct-search-key :direct-search)
(def direct-search (conj root direct-search-key))

(def direct-search-suggestions (conj direct-search :suggestions))
(def direct-search-result-list-open (conj direct-search :result-list-open))
(def direct-search-query (conj direct-search :query))

(def replay-progress-key :replay-progress)
(def replay-progress [root-key replay-progress-key])

(def curr-max-zindex-key :curr-max-zindex)
(def curr-max-zindex [root-key curr-max-zindex-key])

(def drop-zone-frame-key :drop-zone-frame)
(def drop-zone-frame (conj root drop-zone-frame-key))

(def sidebar-key :sidebar)
(def sidebar (conj root sidebar-key))

(def sidebar-width
  (conj root :sidebar-width))

(def frames (conj root :frames))
(defn frame-desc [frame-id] (conj frames frame-id))
(defn- frame-property [frame-id prop]
  (conj (frame-desc frame-id) prop))

(defn frame-open-legend [frame-id]
  (frame-property frame-id :frame-open-legend))

(defn frame-coords [frame-id]
  (frame-property frame-id :coords))

(defn frame-size [frame-id]
  (frame-property frame-id :size))

(defn frame-full-size [frame-id]
  (frame-property frame-id :full-size))

(defn frame-title [frame-id]
  (frame-property frame-id :title))

(defn frame-z-index [frame-id]
  (frame-property frame-id :z-index))

(defn frame-is-maximized [frame-id]
  (frame-property frame-id :is-maximized?))

(defn frame-is-minimized [frame-id]
  (frame-property frame-id :is-minimized?))

(defn frame-resized-infos [frame-id]
  (frame-property frame-id :resized-infos))

(defn frame-on-drop [frame-id]
  (frame-property frame-id :on-drop))

(defn frame-data-consumer [frame-id]
  (frame-property frame-id :data-consumer))

(defn frame-optional-class [frame-id]
  (frame-property frame-id :optional-class))

(defn frame-vertical-number [frame-id]
  (frame-property frame-id :vertical-number))

(defn frame-size-delta [frame-id]
  (frame-property frame-id :size-delta))

(defn frame-no-event-logging [frame-id]
  (frame-property frame-id :no-event-logging?))

(defn frame-custom-title [frame-id]
  (frame-property frame-id :custom-title))

(defn frame-filter [frame-id]
  (conj root :filter frame-id))

(defn frame-last-applied-filters [frame-id]
  (frame-property frame-id :last-applied-filters))

(defn frame-filter-selected-ui-row [frame-id attr constraint-key]
  (conj (frame-filter frame-id) :selected-ui attr constraint-key))

(defn frame-not-supported-redo-ops [frame-id]
  (frame-property frame-id :not-supported-redo-ops))

(defn frame-undo-connection-update-event [frame-id]
  (frame-property frame-id :undo-connection-update-event))

(defn frame-show-info [frame-id]
  (frame-property frame-id :frame-show-info))

(defn frame-warn-screen-done? [frame-id]
  (frame-property frame-id :warn-screen-done?))

(defn frame-last-action-triggered [frame-id]
  (frame-property frame-id :last-action-triggered))

(def couple-infos-key :couple-infos)

(defn frame-couple-infos [frame-id]
  (conj (frame-desc frame-id)
        couple-infos-key))

(defn frame-couple-handler [frame-id]
  (conj (frame-couple-infos frame-id)
        :handler))

(defn frame-couple-with [frame-id]
  (conj (frame-couple-infos frame-id)
        :with))

(defn connection-negotiation [connection-negotiation-id]
  (conj root :connection-negotiation connection-negotiation-id))

(defn frame-event [frame-id] (frame-property frame-id :event))
(defn frame-type [frame-id] (frame-property frame-id :type))

(def all-frames-title [:de.explorama.frontend.woco.frame/all-frames-title])
(defn all-frame-title [frame-id] (conj all-frames-title
                                       frame-id
                                       :woco.frame/title))

(defn workspace-id [] [root-key :workspace-id])

(def client-id [root-key :client-id])

(def current-group-key :current-group)
(def current-group [root-key current-group-key])

(def delete-di-queue (conj root :delete-di-queue))

(def interaction-mode [root-key :interaction-mode])
(def pending-interaction-mode [root-key :pending-interaction-mode])

(def registry [root-key :registry])

(def menu-items [root-key :menu-items])

(defn dissoc-in [db path]
  (if (vector? path)
    (update-in db (pop path)
               dissoc
               (peek path))
    (do
      (error (str "can't dissoc-in. " path " is not a vector"))
      db)))

(def login-form-key :login-form)
(def login-form (conj root login-form-key))
(defn login-value [k]
  (conj login-form k))

(def rights-and-roles-key :rights-and-roles)
(def login-root [rights-and-roles-key])

(def logged-in (conj login-root :logged-in?))
(def user-info (conj login-root :user-info))
(def login-message (conj login-root :login-message))
(def ldap-available (conj login-root :ldap-available))

(def welcome-active? (conj root :welcome-active?))
(def welcome-loading? (conj root :welcome-loading?))
(def welcome-callback (conj root :welcome-callback))

(def init-events [:init-events])

(def context-menu (conj root :context-menu))

(defn operation
  ([connection-negotiation-id]
   (conj root :operation connection-negotiation-id))
  ([connection-negotiation-id source-or-target]
   (conj (operation connection-negotiation-id)
         source-or-target)))
(def details-view-api-key :details-view-events)
;;API event table
(def details-view-events [details-view-api-key])
(defn event-details [event-id]
  (conj details-view-events event-id))

(def details-view-key :details-view)

(def details-view (conj root details-view-key))
(def events-key :events)
(def details-view-frame-id (conj details-view :frame-id))


(def show-details-view (conj details-view :show?))
(def details-view-newest (conj details-view :newest-element))
(def details-view-coords (conj details-view :coords))
(def details-view-compare-events (conj details-view :compare))

(def tool-descs
  (conj root
        :tools))

(defn tool-desc [tool-id]
  (conj tool-descs
        tool-id))

(def page-bounding-box (conj root :page-bounding-box))

(def page-dropped? (conj root :page-dropped?))

(def overlayer-active-key :overlayer-active?)
(def overlayer-active? (conj root overlayer-active-key))

(def product-tour-key :product-tour)
(def product-tour-steps-key :current-steps)
(def product-tour-all-steps-key :all-steps)
(def product-tour-popup-key :popup-open?)

(def product-tour (conj root product-tour-key))

(def product-tour-popup (conj product-tour product-tour-popup-key))

(def product-tour-steps
  (conj product-tour product-tour-steps-key))

(def product-tour-all-steps
  (conj product-tour product-tour-all-steps-key))

(def product-tour-current-step
  (conj product-tour-steps 0))

(def product-tour-active-comp
  (conj product-tour-current-step :component))

(def product-tour-active-additional-infos
  (conj product-tour-current-step :additional-info))
(def maximized-frame-key :maximized-frame)
(def maximized-frame (conj root maximized-frame-key))

(def navigation-key :navigation)
(def navigation (conj root navigation-key))
(def navigation-position (conj navigation :position))
(def navigation-bar-offset (conj navigation :offset))
(def navigation-position-x (conj navigation-position :x))
(def navigation-position-y (conj navigation-position :y))
(def navigation-position-z (conj navigation-position :z))

(def last-logged-position (conj navigation :last-logged-position))

(def show-minimap? (conj navigation :show-minimap?))
(def show-framelist? (conj navigation :show-framelist?))
(def show-frame-screenshots? (conj navigation :show-frame-screenshots?))
(def fullscreen? (conj navigation :fullscreen?))

(def id-counter-root
  (conj root :id-counter))

(defn id-counter [vertical]
  (conj id-counter-root vertical))

(def vertical-plugin-apis
  (conj root :plugin-api))

(defn vertical-plugin-api
  ([frame-id]
   (conj vertical-plugin-apis (:vertical frame-id)))
  ([frame-id endpoint]
   (conj vertical-plugin-apis (:vertical frame-id) endpoint))
  ([frame-id endpoint aspect]
   (conj vertical-plugin-apis (:vertical frame-id) endpoint aspect)))

(def overlays
  (conj root :overlays))
(defn overlay [id]
  (conj root :overlays id))

(def generic-dialog-desc
  [root-key :generic-dialog])

(def presentation-mode-key
  :presentation-mode)

(def presentation-mode
  (conj root presentation-mode-key))

(def slides
  (conj presentation-mode :slides))

(def current-slide
  (conj presentation-mode :current-slide))

(def edit-presentation-mode
  (conj presentation-mode :edit-mode))

(def presentation-current-mode
  (conj presentation-mode :mode))

(def presentation-last-mode
  (conj presentation-mode :last-mode))

(def presentation-sidebar-open
  (conj presentation-mode :sidebar-open))

(def presentation-key-handler
  (conj presentation-mode :key-handler))

(def presentation-animation-activated
  (conj presentation-mode :animation-activated))

(def presentation-show-dialog?
  (conj presentation-mode :show-dialog?))

(def presentation-dialog-data
  (conj presentation-mode :dialog-data))

(def datalink
  (conj root :datalink))

(def ignored-frames
  (conj root :ignored-frames))

(def current-workspace-grid
  (conj root :current-workspace-grid))

(def frame-header-colors
  (conj root :header-color))

(defn frame-header-color [frame-id]
  (conj frame-header-colors frame-id))

(defn frame-published-by [frame-id]
  (conj (frame-desc frame-id) :published-by-frame))

(defn position-handling-source [frame-id]
  (conj (frame-desc frame-id) :position-handling-source))

(defn frame-publishing? [frame-id]
  (conj (frame-desc frame-id) :publishing?))

(defn selections [frame-id]
  (conj root :selections frame-id))

(def show-connecting-edges? [root-key :show-connections?])