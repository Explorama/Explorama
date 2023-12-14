(ns de.explorama.frontend.ui-base.utils.interop
  (:require [goog.object :as goog-obj]
            [goog.string :as goog-string]
            [goog.string.format]))

(defn ^:export safe-aget
  "Get a value from an object multiple levels deep. This is useful for pulling values from deeply nested objects, such as JSON responses.
   
   Example:
   ```clojure
     => (safe-aget #js{\"a\" #js{\"b\" \"c\"}} \"a\")
     => #js{\"b\" \"c\"}
     => (safe-aget #js{\"a\" #js{\"b\" \"c\"}} \"a\" \"b\")
     => \"c\"
   
      Difference to aget:
      => (safe-aget #js{\"a\" #js{\"b\" \"c\"}} \"a\" \"test\" \"c\")
      => undefined
   
      => (aget #js{\"a\" #js{\"b\" \"c\"}} \"a\" \"test\" \"c\")
      => Uncaught TypeError: Cannot read property 'c' of undefined
         at interop.cljs?rel=1612430380223:22
   ``` "
  [obj & prop-path]
  (apply goog-obj/getValueByKeys obj prop-path))

(defn ^:export format
  "Formats a string with the given args.
   Example:
   ```clojure
   => (format \"I have %d apples and %d peaches.\" 4 10)
   => \"I have 4 apples and 10 peaches.\"
   ```"
  [fmt & args]
  (apply goog-string/format fmt args))

(defn ^:export safe-number?
  "Ensures that
   * Number is not null
   * Number is not infinity/-infinity (number? is true for infinity)
   * Number is not NaN
   * Number is a number
  
   See https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Global_Objects/Number/isFinite
   
   Example:
   ```clojure
   => (save-number? nil)
   => false
   => (save-number? 1)
   => true
   => (save-number? (/ 1 0))
   => false
   => (save-number? \"test\")
   => false
   ```"
  [number]
  (js/Number.isFinite number))