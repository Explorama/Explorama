(ns de.explorama.backend.algorithms.data.aggregations)

(defn most-frequent [data]
  (->>
   (frequencies data)
   (sort-by (fn [[_ num]] num))
   reverse
   first
   key))

(defn average [data]
  (double (/ (reduce + data)
             (count data))))

(defn median [data]
  (let [num (int (/ (count data) 2))
        num-int (int num)
        data (vec (sort data))]
    (if (even? num-int)
      (/ (+ (get data (int (Math/floor num)))
            (get data (int (Math/ceil num)))
            2))
      (get data (int (Math/ceil num))))))

(defn aggregation-map [func attr data]
  (func
   (->> (map #(get % attr)
             data)
        (filter identity))))

(defn aggregate [rest-features transformed-data]
  (let [replacement-aggregate
        (reduce (fn [acc {v :value {:keys [method replacement value] :as missing-value} :missing-value}]
                  (let [replacement
                        (cond (and (= method :replace)
                                   (= replacement :number)
                                   value)
                              value
                              (and (= method :replace)
                                   (= replacement :average))
                              (aggregation-map average v transformed-data)
                              (and (= method :replace)
                                   (= replacement :median))
                              (aggregation-map median v transformed-data)
                              (and (= method :replace)
                                   (= replacement :most-frequent))
                              (aggregation-map most-frequent v transformed-data)
                              :else nil)]
                    (cond (and replacement acc)
                          (assoc acc v replacement)
                          (and acc (not missing-value) (not replacement))
                          acc
                          :else
                          acc)))
                {}
                rest-features)]
    replacement-aggregate))