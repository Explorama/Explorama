(ns de.explorama.frontend.woco.log
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as timbre
             :refer-macros [info warn error]]))

(defn effect [level message]
  (case level
    ::error (error message)
    ::warn (warn message)
    ::info (info message)))

(defn do-nothing [level message])

(re-frame/reg-fx ::error #(effect ::error %))
(re-frame/reg-fx ::warn #(effect ::warn %))
(re-frame/reg-fx ::info #(effect ::info %))

(defn do-effect [_ [level message]]
  {level message})

(re-frame/reg-event-fx ::error do-effect)
(re-frame/reg-event-fx ::warn do-effect)
(re-frame/reg-event-fx ::info do-effect)
