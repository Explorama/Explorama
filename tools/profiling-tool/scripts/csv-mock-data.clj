(require '[clojure.data.csv :as csv]
         '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(def ^:private lorem
  (str/split "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
             #"\s"))

(def ^:private avg-length-lorem (count lorem))

(def ^:private dicts (atom {}))

(defn- generate-rand-dict [{:keys [num min max alphabet id prefix]
                            :or {min 3
                                 max 25
                                 alphabet :all
                                 num 128
                                 prefix ""}}]
  (cond (and id (get @dicts id))
        (get @dicts id)
        (and (not= prefix "")
             (not (str/blank? prefix)))
        (set (mapv #(str prefix %) (range num)))
        :else
        (do
          (assert (and (<= min max)
                       (< 0 min)) "min must be less than max")
          (assert (or (keyword? alphabet)
                      (string? alphabet))
                  "Alphabet must be a keyword or string")
          (let [alphabet (if (string? alphabet)
                           alphabet
                           (case alphabet
                             :alpha-lowercase "abcdefghijklmnopqrstuvwxyz"
                             :alpha-uppercase "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                             :alpha-mixed "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                             :alphanum-mixed "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                             :alphanum-lowercase "abcdefghijklmnopqrstuvwxyz0123456789"
                             :alphanum-uppercase "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                             :all "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{};':\",./<>?\\|`~öäüÖÄÜß"))
                dict (loop [i 0
                            dict #{}]
                       (if (< i num)
                         (recur (inc i)
                                (conj dict (apply str prefix (repeatedly (+ min (rand-int (inc (- max min))))
                                                                         #(rand-nth alphabet)))))
                         dict))]
            (when id
              (swap! dicts assoc id dict))
            dict))))

(defn- int-value-fn [{:keys [min max numbers]
                      :or {min Integer/MIN_VALUE max Integer/MAX_VALUE}}]
  (assert (<= min max) "min must be less than max")
  (assert (or (seq numbers)
              (nil? numbers))
          "Numbers must be a list of nil")
  (cond
    numbers
    (fn []
      (rand-nth numbers))
    (and min
         max
         (zero? min))
    (fn []
      (rand-int (inc max)))
    (and min
         max)
    (fn []
      (int (+ (rand (inc (- (double max)
                            (double min))))
              min)))))

(defn- double-value-fn [{:keys [min max numbers]
                         :or {min Float/MIN_VALUE max Float/MAX_VALUE}}]
  (assert (<= min max) "min must be less than max")
  (assert (or (seq numbers)
              (nil? numbers))
          "Numbers must be a list of nil")
  (cond
    numbers
    (fn []
      (rand-nth numbers))
    (and min
         max
         (zero? min))
    (fn []
      (rand (inc max)))
    (and min
         max)
    (fn []
      (+ (rand (inc (- (double max)
                       (double min))))
         min))))

(defn- string-value-fn [{:keys [min max]
                         :or {min 1 max 10}}]
  (assert (<= min max) "min must be less than max")
  (assert (< 0 min) "min must be greater than 0")
  (fn []
    (str/join " " (repeatedly (int (/ (+ min (rand-int (- max min)))
                                      avg-length-lorem))
                              #(rand-nth lorem)))))

(defn- category-value-fn [{:keys [values dict]}]
  (assert (or (seq values)
              (nil? values))
          "Values must be a list or nil")
  (assert (or (map? dict)
              (nil? dict))
          "Num must be an integer or nil")
  (assert (or dict values)
          "dict or values must be provided")
  (cond values
        (fn []
          (rand-nth values))
        dict
        (let [dict (vec (generate-rand-dict dict))]
          (fn []
            (rand-nth dict)))))

(defn- text-value-fn [{:keys [min max]
                       :or {min 128 max 2048}}]
  (string-value-fn {:min min
                    :max max}))

(defn- lat-value-fn [{:keys [lat-min lat-max]
                      :or {lat-min -90 lat-max 90}}]
  (assert (and (<= lat-min lat-max)
               (<= -90 lat-min)
               (<= 90 lat-max)) "Lat must be within bounds")
  (double-value-fn {:min lat-min
                    :max lat-max}))

(defn- lon-value-fn [{:keys [lon-min lon-max]
                      :or {lon-min -180 lon-max 180}}]
  (assert (and (<= lon-min lon-max)
               (<= -180 lon-min)
               (<= 180 lon-max)) "Lon must be within bounds")
  (double-value-fn {:min lon-min
                    :max lon-max}))

(defn- pos-value-fn [{:keys [lon-min lon-max lat-min lat-max]
                      :or {lon-min -180 lon-max 180
                           lat-min -90 lat-max 90}}]
  (let [lon (lon-value-fn {:lon-min lon-min
                           :lon-max lon-max})
        lat (lat-value-fn {:lat-min lat-min
                           :lat-max lat-max})]
    (fn []
      [(lat) (lon)])))

(defn- id-value-fn [{:keys [type]
                     :or {type :uuid}}]
  (case type
    :uuid
    (fn []
      (str (java.util.UUID/randomUUID)))
    :inc
    (let [c (atom -1)]
      (fn []
        (swap! c inc)))))

(defn- date-value-fn [{:keys [min max separator]
                       :or {min 2000
                            max 2023
                            separator "-"}}]
  (assert (<= min max) "min must be less than max")
  (fn []
    (let [year (+ min (rand-int (inc (- max min))))
          leap-year (mod year 4)
          month (inc (rand-int 12))
          day (inc (rand-int (case month
                               1 31
                               2 (if (zero? leap-year)
                                   29
                                   28)
                               3 31
                               4 30
                               5 31
                               6 30
                               7 31
                               8 31
                               9 30
                               10 31
                               11 30
                               12 31)))]
      (str year separator
           (if (< month 10)
             (str "0" month)
             month)
           separator
           (if (< day 10)
             (str "0" day)
             day)))))

(defn- header [{:keys [values dict]} header-descs]
  (assert (or (and values (seq values))
              (nil? values))
          "Values must be a list or nil")
  (vec (or values
           (generate-rand-dict (or dict
                                   {:num (count header-descs)})))))

(defn generate-csv [header-desc header-descs number-of-rows file-name]
  (let [functions (mapv (fn [[header & [params]]]
                          (case header
                            :id (id-value-fn params)
                            :int (int-value-fn params)
                            :double (double-value-fn params)
                            :string (string-value-fn params)
                            :category (category-value-fn params)
                            :text (text-value-fn params)
                            :pos (pos-value-fn params)
                            :lat (lat-value-fn params)
                            :lon (lon-value-fn params)
                            :date (date-value-fn params)))
                        header-descs)]
    (loop [rows [(header header-desc header-descs)]
           i 0]
      (if (< i number-of-rows)
        (recur
         (conj rows
               (mapv (fn [fun]
                       (fun))
                     functions))
         (inc i))
        (with-open [writer (io/writer file-name)]
          (csv/write-csv writer
                         rows))))))

(defn get-params [raw-params]
  (cond (= "-f" (nth raw-params 0))
        (edn/read-string (slurp (nth raw-params 1)))
        (= "-p" (nth raw-params 0))
        [(map edn/read-string (rest raw-params))]
        :else
        (throw (ex-info "Invalid params"
                        {:params raw-params}))))

(let [params (get-params *command-line-args*)]
  (doseq [param-set params
          :let [header-desc (nth param-set 0)
                header-descs (nth param-set 1)
                number-of-rows (nth param-set 2)
                file-name (nth param-set 3)]]
    (generate-csv header-desc header-descs number-of-rows file-name)))
