(ns de.explorama.frontend.ui-base.utils.client
  (:require [clojure.string :refer [includes?]]))

(defn ^:export client-os
  "Return the current operationsystem of user client."
  []
  (when-let [app-version (aget js/window "navigator" "appVersion")]
    (cond
      (includes? app-version "Win")
      :win
      (includes? app-version "Mac")
      :mac
      (includes? app-version "Linux")
      :linux
      (includes? app-version "X11")
      :unix
      :else
      :unknown)))

(defn ^:export is-win-client?
  "Checks if users operationsystem is windows"
  []
  (= :win (client-os)))

(def console-threshold 160) ; min-width of browser console

;; Based on https://github.com/sindresorhus/devtools-detect
(defn ^:export console-open?
  "Returns true if browser console is open"
  []
  (let [width-threshold (> (- (aget js/window "outerWidth")
                              (aget js/window "innerWidth"))
                           console-threshold)
        heigh-threshold (> (- (aget js/window "outerHeight")
                              (aget js/window "innerHeight"))
                           console-threshold)]

    (boolean (and (not (and width-threshold heigh-threshold))
                  (or (and (aget js/window "Firebug")
                           (aget js/window "Firebug" "chrome")
                           (aget js/window "Firebug" "chrome" "isInitialized"))
                      width-threshold
                      heigh-threshold)))))