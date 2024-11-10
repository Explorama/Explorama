(require '[clojure.data.csv :as csv]
         '[clojure.edn :as edn]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(def ^:private lorem
  (str/split "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
             #"\s"))

(def ^:private avg-length-lorem (count lorem))

(def ^:private dicts (atom {}))

(defn- exclusive-0 []
  (some (fn [_] (let [v (rand)]
                  (when (< 0 v) v)))
        (range 100)))

(defn- normal-distribution [min max skew]
  (loop [min min
         max max
         skew skew]
    (let [u (exclusive-0)
          v (exclusive-0)
          num (* (Math/sqrt (* -2.0 (Math/log u))) (Math/cos (* 2.0 Math/PI v)))
          num (+ 0.55 (/ num 10.0))]
      (if (or (< 1 num) (< num 0))
        (recur min max skew)
        (let [num (Math/pow num skew)
              num (* num (- max min))]
          (+ num min))))))

(defn- round-to-precision [precision x]
  (let [y (+ x (if (nil? precision) 0.5 (/ precision 2)))]
    (int
     (- y (mod y (if (nil? precision) 1 precision))))))

(defn ndist-value [values-num]
  (normal-distribution 0 values-num 1))

(defn ndist-value-int [values-num]
  (->>
   (normal-distribution 0 values-num 1)
   (round-to-precision 1)))

(defn ndist-value-seq [values]
  (->>
   (ndist-value-int (count values))
   (get values)))

#_(defn generate-values [n min max step]
    (loop [data (mapv (fn [_] 0) (range (- max min)))
           i 0]
      (if (< i n)
        (let [randNum (normal-distribution min max 1)
              rounded (round-to-precision randNum step)]
          (recur (update data rounded inc)
                 (+ step i)))
        data)))

#_(spit "distribution.edn"
        (let [data (generate-values 100000 0 1000 1)
              sum (reduce + data)]
          (mapv (fn [x] (double (/ x sum))) data)))

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
                                (conj dict (apply str prefix (repeatedly (+ min (ndist-value-int (inc (- max min))))
                                                                         #(ndist-value-seq alphabet)))))
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
      (ndist-value-seq numbers))
    (and min
         max
         (zero? min))
    (fn []
      (ndist-value-int (inc max)))
    (and min
         max)
    (fn []
      (int (+ (ndist-value (inc (- (double max)
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
      (ndist-value-seq numbers))
    (and min
         max
         (zero? min))
    (fn []
      (ndist-value (inc max)))
    (and min
         max)
    (fn []
      (+ (ndist-value (inc (- (double max)
                              (double min))))
         min))))

(defn- string-value-fn [{:keys [min max]
                         :or {min 1 max 10}}]
  (assert (<= min max) "min must be less than max")
  (assert (< 0 min) "min must be greater than 0")
  (fn []
    (str/join " " (repeatedly (int (/ (+ min (ndist-value-int (- max min)))
                                      avg-length-lorem))
                              #(ndist-value-seq lorem)))))

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
          (ndist-value-seq values))
        dict
        (let [dict (vec (generate-rand-dict dict))]
          (fn []
            (ndist-value-seq dict)))))

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
    (let [year (+ min (ndist-value-int (inc (- max min))))
          leap-year (mod year 4)
          month (inc (ndist-value-int 11))
          day (inc (ndist-value-int (case month
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
    (println "Generating " number-of-rows " rows for " file-name)
    (generate-csv header-desc header-descs number-of-rows file-name)))
