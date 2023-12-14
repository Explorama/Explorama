(ns de.explorama.backend.common.mocks.redis
  (:require [taoensso.carmine :as car]
            [taoensso.carmine
             (protocol    :as car-protocol)
             (connections :as car-conns)]
            [taoensso.encore :as enc]))

(def ^:private state (atom {}))

(defn- mock-get [k]
  (get @state k))

(defn- mock-set [k v]
  (swap! state assoc k v))

(defn- mock-del [key]
  (swap! state dissoc key)
  ;; Should return number of keys deleted, hard coding to 1 here
  1)

(defn- mock-rename [from to]
  (swap! state (fn [s]
                 (-> (assoc s to (get s from))
                     (dissoc s from)))))

(defn- mock-scan [_ _ pattern]
  ["0" (keys @state)])

(defn- pooled-conn-mock [& _])
(defn- release-conn-mock [& _])
(defn- execute-requests-mock [& _])
(defn- run!-mock [& _])
(defn- -with-replies-mock [body-fn _]
  (body-fn))

(defn get-state []
  @state)

(defn fixture [test-fn]
  (with-redefs [car/get mock-get
                car/set mock-set
                car/del mock-del
                car/rename mock-rename
                car/scan mock-scan
                car-conns/pooled-conn pooled-conn-mock
                car-conns/release-conn release-conn-mock
                car-protocol/execute-requests execute-requests-mock
                car-protocol/-with-replies -with-replies-mock
                enc/run! run!-mock]
    (test-fn)
    (reset! state {})))
