(ns de.explorama.frontend.knowledge-editor.store
  (:require [clojure.string :as str]
            [reagent.core :as reagent]))

(def dummy-data [{}
                 {"e1498277-98f8-4f23-b90a-9f7f9b952ad9" {:properties [["title" "string" "New Title"]], :contexts [["type" "entry"]], :notes "Some text", :id "e1498277-98f8-4f23-b90a-9f7f9b952ad9", :date "2023-04-12T16:18:51"},
                  "262f131b-c8a4-4a0f-a74f-c923c8e176e9" {:properties [["title" "string" "New Title"]], :contexts [["type" "entry"]], :notes "Some text", :id "262f131b-c8a4-4a0f-a74f-c923c8e176e9", :date "2023-04-12T16:18:56"}}
                 {"742b33cd-261c-4625-8a17-493d71801e2f" {:id "742b33cd-261c-4625-8a17-493d71801e2f",
                                                          :title "test1",
                                                          :date "2023-04-12T16:19:13",
                                                          :data {"9d44eebc-9677-43aa-9c48-900f38d4b2d9" {:id "9d44eebc-9677-43aa-9c48-900f38d4b2d9", :type :event, :state-id "8f122e33-ab9b-4249-a5fb-92e2eb35e1ea", :event-id "262f131b-c8a4-4a0f-a74f-c923c8e176e9" :pos [500 1000] :color "#BBBBBB"},
                                                                 "2ae152f1-c2f8-46e6-b761-8aff1b4fb71f" {:id "2ae152f1-c2f8-46e6-b761-8aff1b4fb71f", :type :event, :state-id nil, :event-id "e1498277-98f8-4f23-b90a-9f7f9b952ad9", :pos [1500 750]  :color "#BBBBBB"},
                                                                 "b010a35c-2fd3-471f-b15f-2e77f084c1a7" {:id "b010a35c-2fd3-471f-b15f-2e77f084c1a7", :state-id "e1d949ed-ba72-4f09-80f5-5dc38e415438", :type :connection, :from "2ae152f1-c2f8-46e6-b761-8aff1b4fb71f", :to "9d44eebc-9677-43aa-9c48-900f38d4b2d9" :color "#000000" :label "Test"}}}}
                 [["else" "title"]
                  ["else" "arch-type"]]])

(def color-type "#528b8b")
(def color-comp "#886688")
(def color-lib "#4682B4")
(def color-interface "#668888")

(def arch-data [{"e1498277-98f8-4f23-b90a-9f7f9b952ad9" {:properties [["title" "string" "New Title"]]
                                                         :contexts   [["type" "entry"]]
                                                         :notes      "Some text"
                                                         :datasource "test"
                                                         :id         "e1498277-98f8-4f23-b90a-9f7f9b952ad9"
                                                         :date       "2023-04-12T16:18:51"},
                 "262f131b-c8a4-4a0f-a74f-c923c8e176e9" {:properties [["title" "string" "New Title"]]
                                                         :contexts   [["type" "entry"]]
                                                         :notes      "Some text"
                                                         :datasource "test"
                                                         :id         "262f131b-c8a4-4a0f-a74f-c923c8e176e9"
                                                         :date       "2023-04-12T16:18:56"}}
                {"context-type-type"                             {:properties [["name" "string" "type"]]
                                                                  :contexts   []
                                                                  :notes      "type type"
                                                                  :id         "context-type-type"
                                                                  :date       "2023-04-13T16:38:35"
                                                                  :datasource "Architecture"}
                 "context-type-interface"                        {:properties [["name" "string" "interface"]]
                                                                  :contexts   [["type" "type"]]
                                                                  :notes      "Interface type"
                                                                  :id         "context-type-interface"
                                                                  :date       "2023-04-13T16:38:35"
                                                                  :datasource "Architecture"}
                 "context-type-component"                        {:properties [["name" "string" "component"]]
                                                                  :contexts   [["type" "type"]]
                                                                  :notes      "Component type"
                                                                  :id         "context-type-component"
                                                                  :date       "2023-04-13T16:38:35"
                                                                  :datasource "Architecture"}
                 "context-type-component-type"                   {:properties [["name" "string" "component-type"]]
                                                                  :contexts   [["type" "type"]]
                                                                  :notes      "Component-type type"
                                                                  :id         "context-type-component-type"
                                                                  :date       "2023-04-13T16:38:35"
                                                                  :datasource "Architecture"}
                 "context-component-type-physical"               {:properties [["name" "string" "physical"]]
                                                                  :contexts   [["type" "component-type"]]
                                                                  :notes      "Component-type physical"
                                                                  :id         "context-component-type-physical"
                                                                  :date       "2023-04-13T16:38:35"
                                                                  :datasource "Architecture"}
                 "context-type-server"                           {:properties [["name" "string" "server"]]
                                                                  :contexts   [["type" "type"]]
                                                                  :notes      "Server type"
                                                                  :id         "context-type-server"
                                                                  :date       "2023-04-13T16:38:35"
                                                                  :datasource "Architecture"}
                 "context-interface-ac-api"                      {:properties [["name" "string" "component"]]
                                                                  :contexts   [["type" "interface"]]
                                                                  :notes      "AC API"
                                                                  :id         "context-interface-ac-api"
                                                                  :date       "2023-04-13T16:38:35"
                                                                  :datasource "Architecture"}
                 "context-type-library"                          {:properties [["name" "string" "library"]]
                                                                  :contexts   [["type" "type"]]
                                                                  :notes      "AC API"
                                                                  :id         "context-type-library"
                                                                  :date       "2023-04-13T16:38:35"
                                                                  :datasource "Architecture"}
                 "context-component-mosaic"                       {:properties [["name" "string" "mosaic"]]
                                                                   :contexts   [["type" "component"]]
                                                                   :notes      "mosaic Component"
                                                                   :id         "context-component-mosaic"
                                                                   :date       "2023-04-13T16:11:18"
                                                                   :datasource "Architecture"}
                 "context-server-mosaic"                          {:properties [["name" "string" "mosaic"]]
                                                                   :contexts   [["type" "server"]
                                                                                ["component-type" "physical"]]
                                                                   :notes      "mosaic Server"
                                                                   :id         "context-server-mosaic"
                                                                   :date       "2023-04-13T16:12:56"
                                                                   :datasource "Architecture"}
                 "context-library-pneumatic-tubes"               {:properties [["name" "string" "Pneumatic tubes"] ["url" "string" "https://github.com/drapanjanas/pneumatic-tubes"]]
                                                                  :contexts   [["type" "Library"]]
                                                                  :notes      "Library for http-kit that allows re-frame like behavior for websocket communication."
                                                                  :id         "context-library-pneumatic-tubes"
                                                                  :date       "2023-04-13T16:18:15"
                                                                  :datasource "Architecture"}
                 "context-interface-client-server-communication" {:properties [["name" "string" "Client-Server-Communication"]]
                                                                  :contexts   [["type" "interface"]]
                                                                  :notes      "Client <-> Server Communication"
                                                                  :id         "context-interface-client-server-communication"
                                                                  :date       "2023-04-13T16:14:21"
                                                                  :datasource "Architecture"}
                 "context-technologie-websocket"                 {:properties [["name" "string" "Websocket"]]
                                                                  :contexts   [["type" "technologie"]]
                                                                  :notes      "Websockets are a technologie to allow bidrectional client server communication"
                                                                  :id         "context-technologie-websocket"
                                                                  :date       "2023-04-13T16:16:06"
                                                                  :datasource "Architecture"}
                 "context-component-search"                       {:properties [["name" "string" "search"]]
                                                                   :contexts   [["type" "component"] ["component-type" "physical"]]
                                                                   :notes      "search Server"
                                                                   :id         "context-component-search"
                                                                   :date       "2023-04-13T16:24:28"
                                                                   :datasource "Architecture"}
                 "context-component-ac-service"                   {:properties [["name" "string" "ac-service"]]
                                                                   :contexts   [["type" "component"] ["component-type" "physical"]]
                                                                   :notes      "AC services parts of expdb"
                                                                   :id         "context-component-ac-service"
                                                                   :date       "2023-04-13T16:20:14"
                                                                   :datasource "Architecture"}
                 "context-component-expdb"                    {:properties [["name" "string" "expdb"]]
                                                               :contexts   [["type" "component"] ["component-type" "physical"]]
                                                               :notes      "expdb"
                                                               :id         "context-component-expdb"
                                                               :date       "2023-04-13T16:20:34"
                                                               :datasource "Architecture"}
                 "context-client-mosaic"                          {:properties [["name" "string" "mosaic"]]
                                                                   :contexts   [["type" "client"] ["component-type" "physical"]]
                                                                   :notes      "Mosaic Client"
                                                                   :id         "context-client-mosaic"
                                                                   :date       "2023-04-13T16:12:15"
                                                                   :datasource "Architecture"}
                 "context-interface-data-tile-api"               {:properties [["name" "string" "Data-Tile-API"]]
                                                                  :contexts   [["type" "interface"]]
                                                                  :notes      "Data-Tile API"
                                                                  :id         "context-interface-data-tile-api"
                                                                  :date       "2023-04-13T16:25:15"
                                                                  :datasource "Architecture"}}
                {"1f79ee4c-29bd-4c29-91f5-51df173fefe0" {:id    "1f79ee4c-29bd-4c29-91f5-51df173fefe0"
                                                         :title "Component View"
                                                         :date  "2023-04-14T10:43:26"
                                                         :data  {"f65a8842-6114-4dd0-894c-627162de5496" {:id         "f65a8842-6114-4dd0-894c-627162de5496"
                                                                                                         :type       :context
                                                                                                         :state-id   "053356a5-217f-4e85-98b2-6b53a43f7de6"
                                                                                                         :element-id "context-interface-data-tile-api"
                                                                                                         :color      color-interface
                                                                                                         :pos        [-317.9219615439624 -288.62750068260135]}
                                                                 "e5c087fa-5f52-4249-b745-8ebc678de139" {:id       "e5c087fa-5f52-4249-b745-8ebc678de139"
                                                                                                         :state-id "a79b5ee6-2cce-4160-bd14-34155e6a020b"
                                                                                                         :type     :connection
                                                                                                         :color    "#3B3838"
                                                                                                         :from     "be3acd50-efe7-4c09-8444-3d0f13b4cc34"
                                                                                                         :to       "e261ee02-46a7-43cd-844b-990b48aafc56"}
                                                                 "be3acd50-efe7-4c09-8444-3d0f13b4cc34" {:id         "be3acd50-efe7-4c09-8444-3d0f13b4cc34"
                                                                                                         :type       :context
                                                                                                         :state-id   "26ee1a81-0537-4433-963e-8d39b6ffcf87"
                                                                                                         :element-id "context-component-ac-service"
                                                                                                         :color      color-comp
                                                                                                         :pos        [-278.92863924904657 1590.13217756619]}
                                                                 "270a1644-a52d-465a-9a07-0be45b9ca50e" {:id       "270a1644-a52d-465a-9a07-0be45b9ca50e"
                                                                                                         :state-id "2bb7142e-a548-4b26-afde-3f5505935ae6"
                                                                                                         :type     :connection
                                                                                                         :color    "#3B3838"
                                                                                                         :from     "e261ee02-46a7-43cd-844b-990b48aafc56"
                                                                                                         :to       "edada92a-ff70-4057-be29-bb3d340024dc"}
                                                                 "6a718417-c59a-468b-8da5-3342edd228b3" {:id         "6a718417-c59a-468b-8da5-3342edd228b3"
                                                                                                         :type       :context
                                                                                                         :state-id   "e269bd6c-168a-4a68-bf73-bed6b00c00ea"
                                                                                                         :element-id "context-component-expdb"
                                                                                                         :color      color-comp
                                                                                                         :pos        [-282.2494186817192 2286.388895345159]}
                                                                 "5c6889e5-f689-4e24-bd12-1ec998685d5b" {:id         "5c6889e5-f689-4e24-bd12-1ec998685d5b"
                                                                                                         :type       :context
                                                                                                         :state-id   "c18b7d06-f843-40d2-beef-4b25f4091d2a"
                                                                                                         :element-id "context-component-mosaic"
                                                                                                         :color      color-comp
                                                                                                         :pos        [-1612.2135562138174 33.37058885787165]}
                                                                 "477a811b-4ebf-4ec3-8b94-ab59fede95d9" {:id       "477a811b-4ebf-4ec3-8b94-ab59fede95d9"
                                                                                                         :state-id "c5b36d80-f3a6-4837-8e27-3e80dbb7b059"
                                                                                                         :type     :connection
                                                                                                         :color    "#3B3838"
                                                                                                         :from     "e261ee02-46a7-43cd-844b-990b48aafc56"
                                                                                                         :to       "5c6889e5-f689-4e24-bd12-1ec998685d5b"}
                                                                 "edada92a-ff70-4057-be29-bb3d340024dc" {:id         "edada92a-ff70-4057-be29-bb3d340024dc"
                                                                                                         :type       :context
                                                                                                         :state-id   "91bb6e64-2ccd-4ffb-aecf-225d862a9ece"
                                                                                                         :element-id "context-component-search"
                                                                                                         :color      color-comp
                                                                                                         :pos        [1345.4479538824771 129.86932605749112]}
                                                                 "574ce827-723b-4763-95e0-36bb5ec09c9e" {:id       "574ce827-723b-4763-95e0-36bb5ec09c9e"
                                                                                                         :state-id "884c0cea-6e5f-4616-a8e6-b2326ce583d6"
                                                                                                         :type     :connection
                                                                                                         :color    "#3B3838"
                                                                                                         :from     "6a718417-c59a-468b-8da5-3342edd228b3"
                                                                                                         :to       "be3acd50-efe7-4c09-8444-3d0f13b4cc34"}
                                                                 "92888492-915f-4333-bc2a-4ab35903f3a3" {:id       "92888492-915f-4333-bc2a-4ab35903f3a3"
                                                                                                         :state-id "6d4c74cc-5805-42dc-a2cc-d54feaed0d29"
                                                                                                         :type     :connection
                                                                                                         :color    "#3B3838"
                                                                                                         :from     "edada92a-ff70-4057-be29-bb3d340024dc"
                                                                                                         :to       "f65a8842-6114-4dd0-894c-627162de5496"}
                                                                 "129b35f8-d665-47ea-aea5-8d7ef337703d" {:id       "129b35f8-d665-47ea-aea5-8d7ef337703d"
                                                                                                         :state-id "caecca6e-d9ef-4f65-be87-c2bd37cbbf66"
                                                                                                         :type     :connection
                                                                                                         :color    "#3B3838"
                                                                                                         :from     "f65a8842-6114-4dd0-894c-627162de5496"
                                                                                                         :to       "5c6889e5-f689-4e24-bd12-1ec998685d5b"}
                                                                 "e261ee02-46a7-43cd-844b-990b48aafc56" {:id         "e261ee02-46a7-43cd-844b-990b48aafc56"
                                                                                                         :type       :context
                                                                                                         :state-id   "b10a5378-d1b7-4b83-978d-f8c4e6126a6a"
                                                                                                         :element-id "context-interface-ac-api"
                                                                                                         :color      color-interface
                                                                                                         :pos        [-289.9023299884979 818.0534873545669]}}}
                 "d0ebc8ff-009d-4c9f-ab4c-60e16a5b3ff1" {:id    "d0ebc8ff-009d-4c9f-ab4c-60e16a5b3ff1"
                                                         :title "mosaic Component View"
                                                         :date  "2023-04-18T16:23:14"
                                                         :data  {"bc7b0b4f-1d33-4f99-b1ab-3e7c0305f39d" {:id         "bc7b0b4f-1d33-4f99-b1ab-3e7c0305f39d"
                                                                                                         :type       :context
                                                                                                         :state-id   "694a083d-475a-4a97-81df-217c1ee0ed82"
                                                                                                         :element-id "context-server-mosaic"
                                                                                                         :color      color-comp
                                                                                                         :pos        [-849.4694757871331 1905.3051677966962]}
                                                                 "1463ef06-da0c-44a4-8dac-3af26b706e1a" {:id         "1463ef06-da0c-44a4-8dac-3af26b706e1a"
                                                                                                         :type       :context
                                                                                                         :state-id   "143c545d-bdc4-4599-9776-9c4b82b1d084"
                                                                                                         :element-id "context-technologie-websocket"
                                                                                                         :color      color-lib
                                                                                                         :pos        [-2144.9387659486993 607.1996796793547]}
                                                                 "f83c0442-60c3-411a-ab33-cf7de8a0db37" {:id       "f83c0442-60c3-411a-ab33-cf7de8a0db37"
                                                                                                         :state-id "026883bb-42e8-4385-8c76-f08a146342c0"
                                                                                                         :type     :connection
                                                                                                         :color    "#3B3838"
                                                                                                         :from     "1463ef06-da0c-44a4-8dac-3af26b706e1a"
                                                                                                         :to       "bfa2582c-5355-4887-b333-565c96c61c2a"}
                                                                 "bfa2582c-5355-4887-b333-565c96c61c2a" {:id         "bfa2582c-5355-4887-b333-565c96c61c2a"
                                                                                                         :type       :context
                                                                                                         :state-id   "578768aa-da3c-4009-824f-526a33883f02"
                                                                                                         :element-id "context-interface-client-server-communication"
                                                                                                         :color      color-interface
                                                                                                         :pos        [-857.4552072006338 984.6636091888231]}
                                                                 "35aacb47-8b8c-4062-ac12-c7afc333cac3" {:id       "35aacb47-8b8c-4062-ac12-c7afc333cac3"
                                                                                                         :state-id "6e05c49c-1000-4201-8081-ee75b65de0e8"
                                                                                                         :type     :connection
                                                                                                         :color    "#3B3838"
                                                                                                         :from     "bfa2582c-5355-4887-b333-565c96c61c2a"
                                                                                                         :to       "549ef3fa-e3b9-4ea3-b741-69b87b28b8c0"}
                                                                 "98273468-f979-4596-a82d-80ca7abb02e2" {:id       "98273468-f979-4596-a82d-80ca7abb02e2"
                                                                                                         :state-id "210715fa-9406-4733-949c-c36659c4a816"
                                                                                                         :type     :connection
                                                                                                         :color    "#3B3838"
                                                                                                         :from     "bfa2582c-5355-4887-b333-565c96c61c2a"
                                                                                                         :to       "bc7b0b4f-1d33-4f99-b1ab-3e7c0305f39d"}
                                                                 "96fedafd-01a8-409e-b8b3-43908ad05954" {:id       "96fedafd-01a8-409e-b8b3-43908ad05954"
                                                                                                         :state-id "51e3021f-e09d-4efd-a632-342f35faa182"
                                                                                                         :type     :connection
                                                                                                         :color    "#3B3838"
                                                                                                         :from     "8f9ae439-dc27-4b48-85d2-c1642ea5d077"
                                                                                                         :to       "bfa2582c-5355-4887-b333-565c96c61c2a"}
                                                                 "549ef3fa-e3b9-4ea3-b741-69b87b28b8c0" {:id         "549ef3fa-e3b9-4ea3-b741-69b87b28b8c0"
                                                                                                         :type       :context
                                                                                                         :state-id   "5908a4e7-93e6-4fd0-8f8e-b352bd356ea7"
                                                                                                         :element-id "context-client-mosaic"
                                                                                                         :color      color-comp
                                                                                                         :pos        [-860.2504063977805 49.49514061583017]}
                                                                 "8f9ae439-dc27-4b48-85d2-c1642ea5d077" {:id         "8f9ae439-dc27-4b48-85d2-c1642ea5d077"
                                                                                                         :type       :context
                                                                                                         :state-id   "7de7f7e5-be75-4f8d-93ba-41781690d03d"
                                                                                                         :element-id "context-library-pneumatic-tubes"
                                                                                                         :color      color-lib
                                                                                                         :pos        [-2134.9044013602743 1363.5637227165212]}}}}
                {:event   [["else" "title"]
                           ["else" "type"]]
                 :context [["else" "name"]
                           ["else" "type"]]}])

(defn- create-context [type value])
(defn- create-prop [type value])

{"e1498277-98f8-4f23-b90a-9f7f9b952ad9" {:properties [["title" "string" "New Title"]]
                                         :contexts   [["type" "entry"]]
                                         :notes      "Some text"
                                         :datasource "test"
                                         :id         "e1498277-98f8-4f23-b90a-9f7f9b952ad9"
                                         :date       "2023-04-12T16:18:51"}}

{"context-type-type"                             {:properties [["name" "string" "type"]]
                                                  :contexts   []
                                                  :notes      "type type"
                                                  :id         "context-type-type"
                                                  :date       "2023-04-13T16:38:35"
                                                  :datasource "Architecture"}}

(defn- normalize-id [val]
  (-> val
      (str/lower-case)
      (str/replace #"\ " "-")
      (str/replace #"[^a-z0-9-]" "")))

(def data-trans
  (let [events (reduce (fn [acc event]
                         (assoc acc (get event "id")
                                {:id (get event "id")
                                 :datasource (get event "datasource")
                                 :date (get event "date")
                                 :notes (get event "notes")
                                 :properties [["fact 1" "integer" (get event "fact 1")]]
                                 :contexts (into [["country" "Country 1"]
                                                  ["category 1" (get event "category 1")]
                                                  ["location" (get event "location")]]
                                                 (mapv (fn [org]
                                                         ["org" org])
                                                       (if (vector? (get event "org"))
                                                         (get event "org")
                                                         [(get event "org")])))}))
                       {}
                       [])
        contexts (reduce (fn [acc event]
                           (reduce (fn [acc [type name]]
                                     (let [id (normalize-id (str "context-" type "-" name))]
                                       (assoc acc id {:properties [["name" "string" name]]
                                                      :contexts   [["type" type]]
                                                      :notes      ""
                                                      :id         id
                                                      :date       "2023-04-13T16:38:35"
                                                      :datasource "Countries"})))
                                   acc
                                   (into [["category 1" (get event "category 1")]
                                          ["location" [[6.35 2.4333]]]]
                                         (mapv (fn [org]
                                                 ["org" org])
                                               (if (vector? (get event "org"))
                                                 (get event "org")
                                                 [(get event "org")])))))
                         {"context-country-Country 1" {:properties [["name" "string" "Country 1"]]
                                                       :contexts   [["type" "country"]]
                                                       :notes      "Country 1 country"
                                                       :id         "context-country-Country 1"
                                                       :date       "2023-04-13T16:38:35"
                                                       :datasource "Countries"}
                          "context-type-country" {:properties [["name" "string" "country"]]
                                                  :contexts   [["type" "type"]]
                                                  :notes      "country type"
                                                  :id         "context-type-country"
                                                  :date       "2023-04-13T16:38:35"
                                                  :datasource "Countries"}
                          "context-type-org" {:properties [["name" "string" "org"]]
                                                       :contexts   [["type" "type"]]
                                                       :notes      "org type"
                                                       :id         "context-type-org"
                                                       :date       "2023-04-13T16:38:35"
                                                       :datasource "Countries"}
                          "context-type-category 1" {:properties [["name" "string" "category 1"]]
                                                     :contexts   [["type" "type"]]
                                                     :notes      "category 1 type"
                                                     :id         "context-type-category 1"
                                                     :date       "2023-04-13T16:38:35"
                                                     :datasource "Countries"}
                          "context-type-location" {:properties [["name" "string" "location"]]
                                                   :contexts   [["type" "type"]]
                                                   :notes      "location type"
                                                   :id         "context-type-location"
                                                   :date       "2023-04-13T16:38:35"
                                                   :datasource "Countries"}
                          "context-type-type" {:properties [["name" "string" "type"]]
                                               :contexts   []
                                               :notes      "type type"
                                               :id         "context-type-country"
                                               :date       "2023-04-13T16:38:35"
                                               :datasource "Countries"}}
                         [])]
    [events
     contexts
     {}
     {:event   [["else" "title"]
                ["else" "type"]]
      :context [["else" "name"]
                ["else" "type"]]}]))

(def empty-data [{} {} {} []])

(def init-data arch-data #_data-trans)

(def dummy-events (reagent/atom (first init-data)))
(def dummy-contexts (reagent/atom (second init-data)))
(def dummy-figures (reagent/atom (get init-data 2)))

(def filtered-events (reagent/atom nil))
(def filtered-contexts (reagent/atom nil))
(def filtered-figures (reagent/atom nil))

(defn lookup-event [event-id]
  (get @dummy-events event-id))

(defn lookup-context [event-id]
  (get @dummy-contexts event-id))

(defn lookup-figure [event-id])