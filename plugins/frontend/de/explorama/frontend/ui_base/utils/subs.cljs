(ns de.explorama.frontend.ui-base.utils.subs
  (:require [taoensso.timbre :refer [error]]))

;;; handle refs

(defn ^:export is-derefable?
  "Checks if x implements IDeref"
  [x]
  (implements? IDeref x))

(defn ^:export val-or-deref
  "Derefs any that implements IDeref, otherwise returns x.
     
   Example:
   ```clojure
    => (val-or-deref (atom 10))
    => 10 
    => (val-or-deref 10)
    => 10 
  ```
   "
  [x]
  (if (is-derefable? x)
    (deref x)
    x))

;;; translations

(defn default-translation
  "Throw an error by default, each frontend should set a new function."
  [word-key]
  (error "You need to set a translate function to the UI-Base to get translated ARIA labels!")
  (atom (name word-key)))

(defonce ^:private translation-fn (atom default-translation))

(defn ^:export set-translation-fn
  "Set a translations functions. The default function will write an error to the console and return the keyword as string."
  [new-fn]
  (reset! translation-fn new-fn))

(defn ^:export translate-label
  "Translate any keyword with the ficen translation function. 
   Strings or derefables are returned as value."
  [word-key]
  (if (keyword? word-key)
    @(@translation-fn word-key)
    (val-or-deref word-key)))
