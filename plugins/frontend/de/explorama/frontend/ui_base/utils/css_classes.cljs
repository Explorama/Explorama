(ns de.explorama.frontend.ui-base.utils.css-classes)

(def input-parent-class "input")
(def input-text-class "text-input")

;; used from woco to handle visibility of toolbars for portal components
(def ^:export toolbar-ignore-class "ignore-window-focus-leave")
;; every node which has this class will not be visible on exports (png/pdf)
(def ^:export export-ignore-class "ignore-for-export")

(def input-container-class "explorama__input__container")
(def form-message-class "form__message")
(def form-input-info-class "explorama__form--info")
(def form-input-error-class "explorama__form--error")
(def form-hint-class "form__field__info")

(defn ^:export add-class
  "Handles adding classes to existing classes depending on type of old-classes
   Example:
   ```clojure
   => (add-class [\"my-class1\"] \"new-class\")
   => [\"my-class1\" \"new-class\"]

   => (add-class \"my-class1\" \"new-class\")
   => \"my-class1 new-class\"

   => (add-class nil \"new-class\")
   => [\"new-class\"]
   ```"
  [old-classes add-class]
  (cond
    (vector? old-classes) (conj old-classes add-class)
    (string? old-classes) (str old-classes " " add-class)
    :else [add-class]))