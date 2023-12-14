[[{:values ["id" "fact1" "fact2" "fact3" "country" "context1" "context2" "text" "date"]}
  [[:id] [:int] [:double] [:string]
   [:category {:dict {:num 200 :prefix "country-" :id :country-a}}]
   [:category {:dict {:num 10000 :alphabet :alpha-mixed :id :context-1a}}]
   [:category {:dict {:num 200 :prefix "context2-" :id :context-2}}]
   [:text] [:date]]
  1000
  "../gen_raw_data/data-a-1k.csv"]
 [{:values ["id" "fact1" "fact2" "fact3" "country" "context1" "context2" "text" "date"]}
  [[:id] [:int] [:double] [:string]
   [:category {:dict {:num 200 :prefix "country-" :id :country-a}}]
   [:category {:dict {:num 10000 :alphabet :alpha-mixed :id :context-1a}}]
   [:category {:dict {:num 200 :prefix "context2-" :id :context-2}}]
   [:text] [:date]]
  10000
  "../gen_raw_data/data-a-10k.csv"]
 [{:values ["id" "fact1" "fact2" "fact3" "country" "context1" "context2" "text" "date"]}
  [[:id] [:int] [:double] [:string]
   [:category {:dict {:num 200 :prefix "country-" :id :country-a}}]
   [:category {:dict {:num 10000 :alphabet :alpha-mixed :id :context-1a}}]
   [:category {:dict {:num 200 :prefix "context2-" :id :context-2}}]
   [:text] [:date]]
  100000
  "../gen_raw_data/data-a-100k.csv"]
 [{:values ["id" "fact1" "fact2" "fact3" "country" "context1" "context2" "text" "date"]}
  [[:id] [:int] [:double] [:string]
   [:category {:dict {:num 200 :prefix "country-" :id :country-a}}]
   [:category {:dict {:num 10000 :alphabet :alpha-mixed :id :context-1a}}]
   [:category {:dict {:num 200 :prefix "context2-" :id :context-2}}]
   [:text] [:date]]
  1000000
  "../gen_raw_data/data-a-1m.csv"]

 [{:values ["id" "fact1" "fact2" "fact3" "country" "context1" "context2" "text" "date"]}
  [[:id] [:int] [:double] [:string]
   [:category {:dict {:num 200 :prefix "country-" :id :country-a}}]
   [:category {:dict {:num 10000 :alphabet :alpha-mixed :id :context-1b}}]
   [:category {:dict {:num 200 :prefix "context2-" :id :context-2}}]
   [:text] [:date]]
  1000
  "../gen_raw_data/data-b-1k.csv"]
 [{:values ["id" "fact1" "fact2" "fact3" "country" "context1" "context2" "text" "date"]}
  [[:id] [:int] [:double] [:string]
   [:category {:dict {:num 200 :prefix "country-" :id :country-a}}]
   [:category {:dict {:num 10000 :alphabet :alpha-mixed :id :context-1b}}]
   [:category {:dict {:num 200 :prefix "context2-" :id :context-2}}]
   [:text] [:date]]
  10000
  "../gen_raw_data/data-b-10k.csv"]
 [{:values ["id" "fact1" "fact2" "fact3" "country" "context1" "context2" "text" "date"]}
  [[:id] [:int] [:double] [:string]
   [:category {:dict {:num 200 :prefix "country-" :id :country-a}}]
   [:category {:dict {:num 10000 :alphabet :alpha-mixed :id :context-1b}}]
   [:category {:dict {:num 200 :prefix "context2-" :id :context-2}}]
   [:text] [:date]]
  100000
  "../gen_raw_data/data-b-100k.csv"]
 [{:values ["id" "fact1" "fact2" "fact3" "country" "context1" "context2" "text" "date"]}
  [[:id] [:int] [:double] [:string]
   [:category {:dict {:num 200 :prefix "country-" :id :country-a}}]
   [:category {:dict {:num 10000 :alphabet :alpha-mixed :id :context-1b}}]
   [:category {:dict {:num 200 :prefix "context2-" :id :context-2}}]
   [:text] [:date]]
  1000000
  "../gen_raw_data/data-b-1m.csv"]]