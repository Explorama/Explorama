(ns de.explorama.frontend.projects.re-events
  "This namespace collects all the re-frame event handlers.

  Currently, a single namespace is enough, as this only collects a certain group
  of event handlers: At the moment I'm writing this (which differs from the
  moment you're reading this, of course), the overall mechanics are as follows:
  - de.explorama.frontend.projects.core requires a number of namespaces, which are loaded before the
    code in de.explorama.frontend.projects.core is loaded. Deep down the dependency tree, the tubes
    namespace creates a WebSocket channel.
  - Creating a new WebSocket channel (or re-opening one after a prior one was
    closed) triggers some response to coordinate state.
  - This response is coming back even before the rest of de.explorama.frontend.projects.core was loaded,
    which - again, before I wrote this - was also registering the re-frame
    handlers for those WebSocket messages.

  So, this namespace extracts the re-freame events which might occur prior to
  the de.explorama.frontend.projects.core namespace being loaded.

  In order to minimize risk, I did not change the namespace of the re-frame
  event types, that's why they still are keywords in the `de.explorama.frontend.projects.core` namespace.

  A cleaner target architecture would split this namespace into a number of
  namespaces based on concern for better code locality.

  Also, the event types would reflect their namespaces.

  And, last but not least, no namespace should trigger events top-level. This
  couples timing to the dependency tree (which is unnecessary and oftentimes
  misleading), and it makes reloading a namespace in the REPL a nightmare.

  Yet, I'm leaving this as a message, because I'm only undertaking the first
  step of that journey here, but I want you to share my ultimate goal, so we
  are moving in the same direction. Welcome traveller :)

  A, and one last thing. Have a look at the de.explorama.frontend.projects.subs namespace also. We leave
  no one behind. Hooyah. (Too much?)"
  (:require [de.explorama.frontend.projects.path :as pp]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ws-api/locks-client
 (fn [db [_ locks]]
   (pp/locks db locks)))

(re-frame/reg-event-fx
 ws-api/notify-client
 (fn [{db :db} [_ notifications]]
   {:db (assoc-in db [:projects :notifications] notifications)
    :dispatch [ws-api/request-projects-route]}))
