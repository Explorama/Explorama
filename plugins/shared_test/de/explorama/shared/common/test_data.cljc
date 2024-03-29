(ns de.explorama.shared.common.test-data)

(def fact-1 "fact-1")
(def fact-2 "fact-2")
(def fact-3 "fact-3")
(def fact-4 "fact-4")
(def fact-5 "fact-5")
(def fact-6 "fact-6")
(def org "org")
(def category-1 "category-1")
(def datasource "datasource")
(def country "country")


(def country-a "Country A")
(def country-b "Country B")
(def country-c "Country C")
(def country-d "Country D")
(def country-e "Country E")
(def country-f "Country F")
(def country-g "Country G")
(def country-h "Country H")
(def country-i "Country I")
(def country-j "Country J")
(def country-k "Country K")
(def country-l "Country L")
(def country-m "Country M")
(def country-n "Country N")
(def country-o "Country O")

(def datasource-a "Datasource A")
(def datasource-b "Datasource B")
(def datasource-c "Datasource C")
(def datasource-d "Datasource D")
(def datasource-e "Datasource E")


(defn org-val [n]
  (str "org" n))

(defn category-val [n m]
  (str "Category " n " " m))

(defn id-val [c n]
  (str "Country " c "-" n))

(def country-a-datasource-a-dt-1
  {datasource datasource-a
   "year"       "1997"
   "identifier" "search"
   country    country-a
   "bucket"     "default"})

(def country-a-datasource-a-dt-2
  {datasource datasource-a
   "year"       "1998"
   "identifier" "search"
   country    country-a
   "bucket"     "default"})

(def country-a-datasource-a-dt-3
  {datasource datasource-a
   "year"       "1999"
   "identifier" "search"
   country    country-a
   "bucket"     "default"})

(def country-a-datasource-a-dt-4
  {datasource datasource-a
   "year"       "2000"
   "identifier" "search"
   country    country-a
   "bucket"     "default"})

(def country-a-datasource-a-data
  {country-a-datasource-a-dt-1
   [{country    country-a,
     category-1 (category-val 1 "B"),
     "id"         (id-val "A" 1),
     datasource datasource-a
     org        (org-val 2)
     "location"   [[15 15]]
     "annotation" ""
     "date"       "1997-12-02"
     "notes"      "Lorem ipsum dolor sit amet"
     fact-1     0}
    {country    country-a
     category-1 (category-val 1 "B")
     "id"         (id-val "A" 2)
     datasource datasource-a
     org        (org-val 4)
     "location"   [[15 15]]
     "annotation" ""
     "date"       "1997-10-10"
     "notes"      "consectetur adipiscing elit"
     fact-1     0}]
   country-a-datasource-a-dt-2
   [{country    country-a
     category-1 (category-val 1 "A")
     "id"         (id-val "A" 3)
     datasource datasource-a
     org        [(org-val 2) (org-val 3)]
     "location"   [[15 15]]
     "annotation" ""
     "date"       "1998-08-01"
     "notes"      "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
     fact-1     1}
    {country    country-a
     category-1 (category-val 1 "B")
     "id"         (id-val "A" 4)
     datasource datasource-a
     org        (org-val 1)
     "location"   [[15 15]]
     "annotation" ""
     "date"       "1998-06-21"
     "notes"      "Ut enim ad minim veniam"
     fact-1     0}
    {country    country-a
     category-1 (category-val 1 "B")
     "id"         (id-val "A" 5)
     datasource datasource-a
     org        (org-val 1)
     "location"   [[15 15]]
     "annotation" ""
     "date"       "1998-02-19"
     "notes"      "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat"
     fact-1     0}
    {country    country-a
     category-1 (category-val 1 "B")
     "id"         (id-val "A" 6)
     datasource datasource-a
     org        (org-val 1)
     "location"   [[15 15]]
     "annotation" ""
     "date"       "1998-08-07"
     "notes"      "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur"
     fact-1     0}]
   country-a-datasource-a-dt-3
   [{country    country-a
     category-1 (category-val 1 "B")
     "id"         (id-val "A" 7)
     datasource datasource-a
     org        (org-val 1)
     "location"   [[15 15]]
     "annotation" ""
     "date"       "1999-06-07"
     "notes"      "Excepteur sint occaecat cupidatat non proident"
     fact-1     0}
    {country    country-a
     category-1 (category-val 1 "A")
     "id"         (id-val "A" 8)
     datasource datasource-a
     org        [(org-val 3) (org-val 7)]
     "location"   [[15 15]]
     "annotation" ""
     "date"       "1999-06-06"
     "notes"      "sunt in culpa qui officia deserunt mollit anim id est laborum"
     fact-1     0}]
   country-a-datasource-a-dt-4
   [{country    country-a
     category-1 (category-val 1 "B")
     "id"         (id-val "A" 9)
     datasource datasource-a
     org        [(org-val 5) (org-val 3)]
     "location"   [[15 15]]
     "annotation" ""
     "date"       "2000-05-05"
     "notes"      "Lorem ipsum dolor sit amet"
     fact-1     1}
    {country    [country-a country-b]
     category-1 (category-val 1 "B")
     "id"         (id-val "B" 5)
     datasource datasource-a
     org        [(org-val 1) (org-val 3)]
     "location"   [[15 15]]
     "annotation" ""
     "date"       "2000-11-02"
     "notes"      "consectetur adipiscing elit"
     fact-1     0}]})

(def country-b-datasource-a-dt-1
  {datasource datasource-a
   "year"       "1997"
   "identifier" "search"
   country    country-b
   "bucket"     "default"})

(def country-b-datasource-a-dt-2
  {datasource datasource-a
   "year"       "1998"
   "identifier" "search"
   country    country-b
   "bucket"     "default"})

(def country-b-datasource-a-dt-3
  {datasource datasource-a
   "year"       "2000"
   "identifier" "search"
   country    country-b
   "bucket"     "default"})

(def country-b-datasource-a-data
  {country-b-datasource-a-dt-1
   [{country    country-b
     category-1 (category-val 1 "B")
     "id"         (id-val "B" 1)
     datasource datasource-a
     org        (org-val 1)
     "location"   [[15 15]]
     "annotation" ""
     "date"       "1997-10-30"
     "notes"      "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
     fact-1     0}]
   country-b-datasource-a-dt-2
   [{country    country-b
     category-1 (category-val 1 "C")
     "id"         (id-val "B" 2)
     datasource datasource-a
     org        [(org-val 4) (org-val 6)]
     "location"   [[15 15]]
     "annotation" ""
     "date"       "1998-11-21"
     "notes"      "Ut enim ad minim veniam"
     fact-1     0}
    {country    country-b
     category-1 (category-val 1 "B")
     "id"         (id-val "B" 3)
     datasource datasource-a
     org        [(org-val 5)]
     "location"   [[15 15]]
     "annotation" ""
     "date"       "1998-04-11"
     "notes"      "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat"
     fact-1     0}]
   country-b-datasource-a-dt-3
   [{country    country-b
     category-1 (category-val 1 "B")
     "id"         (id-val "B" 4)
     datasource datasource-a
     org        (org-val 1)
     "location"   [[15 15]]
     "annotation" ""
     "date"       "2000-11-02"
     "notes"      "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur"
     fact-1     0}
    {country    [country-a country-b]
     category-1 (category-val 1 "B")
     "id"         (id-val "B" 5)
     datasource datasource-a
     org        [(org-val 1) (org-val 3)]
     "location"   [[15 15]]
     "annotation" ""
     "date"       "2000-11-02"
     "notes"      "consectetur adipiscing elit"
     fact-1     0}]})

(defn get-data [ds num merge-elements]
  (->
   (reduce-kv (fn [acc _ tile]
                (->> tile
                     (map #(vector (get % "id") %))
                     (into acc)))
              {}
              (if (= ds "A")
                country-a-datasource-a-data
                country-b-datasource-a-data))
   (get (id-val ds num))
   (merge merge-elements)))

(def all-events (set (concat (flatten (vals country-b-datasource-a-data))
                             (flatten (vals country-a-datasource-a-data)))))

(defn prepare-data [data]
  (vec (flatten (vals data))))

(def error-case-data
  [{country country-a, category-1 (category-val "A" 2), "id" "1"
    "datasource" datasource-a, org (org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1997-12-02", "notes" "Text", fact-1 0}
   {country country-a, category-1 (category-val "A" 2), "id" "2"
    "datasource" datasource-a, org (org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1997-10-10", "notes" "Text", fact-1 0}
   {country country-a, category-1 (category-val "A" 1), "id" "3"
    "datasource" datasource-a, org [(org-val 4) (org-val 2)], "location" [[15 15]]
    "annotation" "", "date" "1998-08-01", "notes" "Text", fact-1 1}
   {country country-a, category-1 (category-val "A" 2), "id" "4","datasource" datasource-a, org (org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1998-06-21", "notes" "Text", fact-1 0}
   {country country-a, category-1 (category-val "A" 2), "id" "5"
    "datasource" datasource-a, org (org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1998-02-19", "notes" "Text", fact-1 0}
   {country country-a, category-1 (category-val "A" 2), "id" "6", "datasource" datasource-a, org (org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1998-08-07", "notes" "Text", fact-1 0}])