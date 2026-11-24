;; Our World in Data, CO2 and Greenhouse Gas Emissions (CC BY 4.0).
;; https://github.com/owid/co2-data - 22 countries, 2000-2022.

(ns data.dummy-data-owid-co2)

(def data
 {:contexts [{:global-id "country-Argentina", :type "country", :name "Argentina"}
             {:global-id "country-Australia", :type "country", :name "Australia"}
             {:global-id "country-Brazil", :type "country", :name "Brazil"}
             {:global-id "country-Canada", :type "country", :name "Canada"}
             {:global-id "country-China", :type "country", :name "China"}
             {:global-id "country-Egypt", :type "country", :name "Egypt"}
             {:global-id "country-France", :type "country", :name "France"}
             {:global-id "country-Germany", :type "country", :name "Germany"}
             {:global-id "country-India", :type "country", :name "India"}
             {:global-id "country-Indonesia", :type "country", :name "Indonesia"}
             {:global-id "country-Italy", :type "country", :name "Italy"}
             {:global-id "country-Japan", :type "country", :name "Japan"}
             {:global-id "country-Kenya", :type "country", :name "Kenya"}
             {:global-id "country-Mexico", :type "country", :name "Mexico"}
             {:global-id "country-Nigeria", :type "country", :name "Nigeria"}
             {:global-id "country-Norway", :type "country", :name "Norway"}
             {:global-id "country-Poland", :type "country", :name "Poland"}
             {:global-id "country-South Africa", :type "country", :name "South Africa"}
             {:global-id "country-Spain", :type "country", :name "Spain"}
             {:global-id "country-Sweden", :type "country", :name "Sweden"}
             {:global-id "country-United Kingdom", :type "country", :name "United Kingdom"}
             {:global-id "country-United States", :type "country", :name "United States"}
             {:global-id "continent-Africa", :type "continent", :name "Africa"}
             {:global-id "continent-Asia", :type "continent", :name "Asia"}
             {:global-id "continent-Europe", :type "continent", :name "Europe"}
             {:global-id "continent-North America", :type "continent", :name "North America"}
             {:global-id "continent-Oceania", :type "continent", :name "Oceania"}
             {:global-id "continent-South America", :type "continent", :name "South America"}],
  :datasource {:global-id "source-owid-co2", :name "OWID CO2"},
  :items [{:global-id "owid-co2-ARG-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 143.355, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.8522, :unit "t/person"} {:name "methane", :type "decimal", :value 113.917, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5619, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006166, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18907.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 703.63, :unit "TWh"} {:name "population", :type "integer", :value 37213986, :unit "people"} {:name "gdp", :type "integer", :value 536480965694, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-ARG-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 135.004, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.5882, :unit "t/person"} {:name "methane", :type "decimal", :value 113.092, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5255, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006242, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18268.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 687.34, :unit "TWh"} {:name "population", :type "integer", :value 37624817, :unit "people"} {:name "gdp", :type "integer", :value 514867255248, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-ARG-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 124.93, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.2851, :unit "t/person"} {:name "methane", :type "decimal", :value 116.586, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.4756, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00635, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17306.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 658.15, :unit "TWh"} {:name "population", :type "integer", :value 38029345, :unit "people"} {:name "gdp", :type "integer", :value 460558541367, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-ARG-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 134.421, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.4983, :unit "t/person"} {:name "methane", :type "decimal", :value 123.989, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.4861, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006481, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18341.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 704.76, :unit "TWh"} {:name "population", :type "integer", :value 38424283, :unit "people"} {:name "gdp", :type "integer", :value 503104157615, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-ARG-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 157.235, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.0508, :unit "t/person"} {:name "methane", :type "decimal", :value 125.187, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5496, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006635, :unit "°C"} {:name "energy per capita", :type "decimal", :value 19351.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 751.16, :unit "TWh"} {:name "population", :type "integer", :value 38815915, :unit "people"} {:name "gdp", :type "integer", :value 550306327169, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-ARG-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 161.728, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.1239, :unit "t/person"} {:name "methane", :type "decimal", :value 123.062, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5464, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006794, :unit "°C"} {:name "energy per capita", :type "decimal", :value 20306.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 796.37, :unit "TWh"} {:name "population", :type "integer", :value 39216786, :unit "people"} {:name "gdp", :type "integer", :value 601050418142, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-ARG-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 174.342, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.4001, :unit "t/person"} {:name "methane", :type "decimal", :value 125.455, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5699, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006957, :unit "°C"} {:name "energy per capita", :type "decimal", :value 21100.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 836.06, :unit "TWh"} {:name "population", :type "integer", :value 39622113, :unit "people"} {:name "gdp", :type "integer", :value 652296814311, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-ARG-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 173.764, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.3423, :unit "t/person"} {:name "methane", :type "decimal", :value 122.57, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5516, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007078, :unit "°C"} {:name "energy per capita", :type "decimal", :value 21717.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 869.08, :unit "TWh"} {:name "population", :type "integer", :value 40016767, :unit "people"} {:name "gdp", :type "integer", :value 714725198062, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-ARG-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 187.596, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.6407, :unit "t/person"} {:name "methane", :type "decimal", :value 120.002, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5853, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007187, :unit "°C"} {:name "energy per capita", :type "decimal", :value 21746.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 879.07, :unit "TWh"} {:name "population", :type "integer", :value 40424151, :unit "people"} {:name "gdp", :type "integer", :value 747157417513, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-ARG-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 178.378, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.3662, :unit "t/person"} {:name "methane", :type "decimal", :value 114.282, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.566, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007362, :unit "°C"} {:name "energy per capita", :type "decimal", :value 20858.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 852.15, :unit "TWh"} {:name "population", :type "integer", :value 40854832, :unit "people"} {:name "gdp", :type "integer", :value 705920838368, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-ARG-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 186.121, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.5078, :unit "t/person"} {:name "methane", :type "decimal", :value 105.478, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5586, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007513, :unit "°C"} {:name "energy per capita", :type "decimal", :value 21368.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 882.27, :unit "TWh"} {:name "population", :type "integer", :value 41288694, :unit "people"} {:name "gdp", :type "integer", :value 785785170150, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-ARG-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 189.884, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.5502, :unit "t/person"} {:name "methane", :type "decimal", :value 104.441, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5507, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007664, :unit "°C"} {:name "energy per capita", :type "decimal", :value 21743.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 907.38, :unit "TWh"} {:name "population", :type "integer", :value 41730655, :unit "people"} {:name "gdp", :type "integer", :value 837742673804, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-ARG-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 191.715, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.5471, :unit "t/person"} {:name "methane", :type "decimal", :value 107.532, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5485, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007791, :unit "°C"} {:name "energy per capita", :type "decimal", :value 22393.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 944.13, :unit "TWh"} {:name "population", :type "integer", :value 42161718, :unit "people"} {:name "gdp", :type "integer", :value 829147409948, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-ARG-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 189.525, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.4508, :unit "t/person"} {:name "methane", :type "decimal", :value 109.297, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5373, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007905, :unit "°C"} {:name "energy per capita", :type "decimal", :value 23705.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1009.45, :unit "TWh"} {:name "population", :type "integer", :value 42582453, :unit "people"} {:name "gdp", :type "integer", :value 849088398588, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-ARG-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 188.539, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.3822, :unit "t/person"} {:name "methane", :type "decimal", :value 110.422, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5316, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008039, :unit "°C"} {:name "energy per capita", :type "decimal", :value 23080.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 993.01, :unit "TWh"} {:name "population", :type "integer", :value 43024071, :unit "people"} {:name "gdp", :type "integer", :value 827750786191, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-ARG-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 191.809, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.4117, :unit "t/person"} {:name "methane", :type "decimal", :value 109.966, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5418, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008157, :unit "°C"} {:name "energy per capita", :type "decimal", :value 23230.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1009.99, :unit "TWh"} {:name "population", :type "integer", :value 43477011, :unit "people"} {:name "gdp", :type "integer", :value 850356641236, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-ARG-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 190.02, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.3285, :unit "t/person"} {:name "methane", :type "decimal", :value 111.82, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5369, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008274, :unit "°C"} {:name "energy per capita", :type "decimal", :value 22909.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1005.72, :unit "TWh"} {:name "population", :type "integer", :value 43900312, :unit "people"} {:name "gdp", :type "integer", :value 832669278245, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-ARG-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 187.068, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.2238, :unit "t/person"} {:name "methane", :type "decimal", :value 115.89, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.52, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00838, :unit "°C"} {:name "energy per capita", :type "decimal", :value 22511.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 996.99, :unit "TWh"} {:name "population", :type "integer", :value 44288898, :unit "people"} {:name "gdp", :type "integer", :value 856142153676, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-ARG-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 182.483, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.0865, :unit "t/person"} {:name "methane", :type "decimal", :value 115.387, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.4968, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008491, :unit "°C"} {:name "energy per capita", :type "decimal", :value 22081.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 986.05, :unit "TWh"} {:name "population", :type "integer", :value 44654876, :unit "people"} {:name "gdp", :type "integer", :value 833736983405, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-ARG-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 180.428, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.0119, :unit "t/person"} {:name "methane", :type "decimal", :value 115.653, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.4865, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008612, :unit "°C"} {:name "energy per capita", :type "decimal", :value 20582.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 925.66, :unit "TWh"} {:name "population", :type "integer", :value 44973469, :unit "people"} {:name "gdp", :type "integer", :value 817053926667, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-ARG-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 164.613, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.6425, :unit "t/person"} {:name "methane", :type "decimal", :value 115.126, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.4682, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008718, :unit "°C"} {:name "energy per capita", :type "decimal", :value 19117.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 863.97, :unit "TWh"} {:name "population", :type "integer", :value 45191960, :unit "people"} {:name "gdp", :type "integer", :value 735814250460, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-ARG-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 190.197, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.1975, :unit "t/person"} {:name "methane", :type "decimal", :value 112.747, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5159, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008842, :unit "°C"} {:name "energy per capita", :type "decimal", :value 20925.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 948.19, :unit "TWh"} {:name "population", :type "integer", :value 45312282, :unit "people"} {:name "gdp", :type "integer", :value 812324184006, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-ARG-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 183.768, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.0471, :unit "t/person"} {:name "methane", :type "decimal", :value 115.333, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.4897, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008962, :unit "°C"} {:name "energy per capita", :type "decimal", :value 22049.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1001.24, :unit "TWh"} {:name "population", :type "integer", :value 45407904, :unit "people"} {:name "gdp", :type "integer", :value 854914363923, :unit "2011 International$"}],
              :locations [{:lat -38.42, :lon -63.62}],
              :context-refs [{:global-id "country-Argentina"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-AUS-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 349.898, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 18.2882, :unit "t/person"} {:name "methane", :type "decimal", :value 185.127, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3715, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.009708, :unit "°C"} {:name "energy per capita", :type "decimal", :value 68222.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1305.26, :unit "TWh"} {:name "population", :type "integer", :value 19132472, :unit "people"} {:name "gdp", :type "integer", :value 692059312020, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-AUS-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 357.668, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 18.4694, :unit "t/person"} {:name "methane", :type "decimal", :value 197.299, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3921, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.009904, :unit "°C"} {:name "energy per capita", :type "decimal", :value 67924.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1315.39, :unit "TWh"} {:name "population", :type "integer", :value 19365438, :unit "people"} {:name "gdp", :type "integer", :value 713890072308, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-AUS-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 362.425, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 18.4955, :unit "t/person"} {:name "methane", :type "decimal", :value 190.631, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3799, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010089, :unit "°C"} {:name "energy per capita", :type "decimal", :value 68472.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1341.75, :unit "TWh"} {:name "population", :type "integer", :value 19595356, :unit "people"} {:name "gdp", :type "integer", :value 747065694937, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-AUS-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 370.784, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 18.7081, :unit "t/person"} {:name "methane", :type "decimal", :value 147.337, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3409, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010326, :unit "°C"} {:name "energy per capita", :type "decimal", :value 68663.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1360.86, :unit "TWh"} {:name "population", :type "integer", :value 19819449, :unit "people"} {:name "gdp", :type "integer", :value 774450991263, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-AUS-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 382.005, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 19.0558, :unit "t/person"} {:name "methane", :type "decimal", :value 168.446, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3352, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010557, :unit "°C"} {:name "energy per capita", :type "decimal", :value 70058.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1404.44, :unit "TWh"} {:name "population", :type "integer", :value 20046679, :unit "people"} {:name "gdp", :type "integer", :value 809792445090, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-AUS-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 385.082, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 18.9747, :unit "t/person"} {:name "methane", :type "decimal", :value 148.877, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.301, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010749, :unit "°C"} {:name "energy per capita", :type "decimal", :value 69494.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1410.35, :unit "TWh"} {:name "population", :type "integer", :value 20294490, :unit "people"} {:name "gdp", :type "integer", :value 840092380696, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-AUS-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 391.373, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 19.0074, :unit "t/person"} {:name "methane", :type "decimal", :value 167.974, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2792, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010936, :unit "°C"} {:name "energy per capita", :type "decimal", :value 71432.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1470.84, :unit "TWh"} {:name "population", :type "integer", :value 20590566, :unit "people"} {:name "gdp", :type "integer", :value 866678665246, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-AUS-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 398.896, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 19.0363, :unit "t/person"} {:name "methane", :type "decimal", :value 165.577, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2664, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011142, :unit "°C"} {:name "energy per capita", :type "decimal", :value 71877.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1506.17, :unit "TWh"} {:name "population", :type "integer", :value 20954545, :unit "people"} {:name "gdp", :type "integer", :value 911250727265, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-AUS-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 404.279, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 18.9175, :unit "t/person"} {:name "methane", :type "decimal", :value 147.579, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2614, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011407, :unit "°C"} {:name "energy per capita", :type "decimal", :value 71675.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1531.75, :unit "TWh"} {:name "population", :type "integer", :value 21370595, :unit "people"} {:name "gdp", :type "integer", :value 937889628737, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-AUS-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 406.977, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 18.6832, :unit "t/person"} {:name "methane", :type "decimal", :value 151.71, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2915, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011629, :unit "°C"} {:name "energy per capita", :type "decimal", :value 69299.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1509.54, :unit "TWh"} {:name "population", :type "integer", :value 21783014, :unit "people"} {:name "gdp", :type "integer", :value 963129933389, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-AUS-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 405.026, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 18.2925, :unit "t/person"} {:name "methane", :type "decimal", :value 141.697, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2156, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.01195, :unit "°C"} {:name "energy per capita", :type "decimal", :value 68145.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1508.84, :unit "TWh"} {:name "population", :type "integer", :value 22141583, :unit "people"} {:name "gdp", :type "integer", :value 993853968687, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-AUS-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 403.874, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.9661, :unit "t/person"} {:name "methane", :type "decimal", :value 193.829, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1713, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012208, :unit "°C"} {:name "energy per capita", :type "decimal", :value 69333.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1558.6, :unit "TWh"} {:name "population", :type "integer", :value 22479779, :unit "people"} {:name "gdp", :type "integer", :value 1024000293343, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-AUS-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 405.005, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.7225, :unit "t/person"} {:name "methane", :type "decimal", :value 194.517, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1587, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012433, :unit "°C"} {:name "energy per capita", :type "decimal", :value 67218.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1536.11, :unit "TWh"} {:name "population", :type "integer", :value 22852648, :unit "people"} {:name "gdp", :type "integer", :value 1062662906305, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-AUS-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 397.85, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.1219, :unit "t/person"} {:name "methane", :type "decimal", :value 153.629, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1278, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012647, :unit "°C"} {:name "energy per capita", :type "decimal", :value 66946.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1555.59, :unit "TWh"} {:name "population", :type "integer", :value 23236271, :unit "people"} {:name "gdp", :type "integer", :value 1086068056518, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-AUS-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 392.61, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.6391, :unit "t/person"} {:name "methane", :type "decimal", :value 164.133, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.107, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012842, :unit "°C"} {:name "energy per capita", :type "decimal", :value 66387.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1566.46, :unit "TWh"} {:name "population", :type "integer", :value 23595599, :unit "people"} {:name "gdp", :type "integer", :value 1113888062929, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-AUS-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 400.589, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.7275, :unit "t/person"} {:name "methane", :type "decimal", :value 161.088, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1315, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013024, :unit "°C"} {:name "energy per capita", :type "decimal", :value 66879.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1601.63, :unit "TWh"} {:name "population", :type "integer", :value 23947997, :unit "people"} {:name "gdp", :type "integer", :value 1139303344945, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-AUS-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 409.666, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.8379, :unit "t/person"} {:name "methane", :type "decimal", :value 142.951, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1575, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013238, :unit "°C"} {:name "energy per capita", :type "decimal", :value 66045.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1606.89, :unit "TWh"} {:name "population", :type "integer", :value 24329963, :unit "people"} {:name "gdp", :type "integer", :value 1169967279612, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-AUS-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 413.408, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.7162, :unit "t/person"} {:name "methane", :type "decimal", :value 166.517, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1492, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.01345, :unit "°C"} {:name "energy per capita", :type "decimal", :value 64778.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1602.03, :unit "TWh"} {:name "population", :type "integer", :value 24731044, :unit "people"} {:name "gdp", :type "integer", :value 1197832839959, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-AUS-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 414.781, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.5122, :unit "t/person"} {:name "methane", :type "decimal", :value 168.041, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1291, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013646, :unit "°C"} {:name "energy per capita", :type "decimal", :value 64726.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1625.91, :unit "TWh"} {:name "population", :type "integer", :value 25119700, :unit "people"} {:name "gdp", :type "integer", :value 1231576952488, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-AUS-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 414.925, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.2826, :unit "t/person"} {:name "methane", :type "decimal", :value 154.257, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1188, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013838, :unit "°C"} {:name "energy per capita", :type "decimal", :value 65938.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1680.29, :unit "TWh"} {:name "population", :type "integer", :value 25482714, :unit "people"} {:name "gdp", :type "integer", :value 1255505542015, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-AUS-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 398.547, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 15.4813, :unit "t/person"} {:name "methane", :type "decimal", :value 135.224, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1336, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.01404, :unit "°C"} {:name "energy per capita", :type "decimal", :value 61584.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1585.41, :unit "TWh"} {:name "population", :type "integer", :value 25743787, :unit "people"} {:name "gdp", :type "integer", :value 1232259790388, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-AUS-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 388.472, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 14.9663, :unit "t/person"} {:name "methane", :type "decimal", :value 140.75, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0537, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.014229, :unit "°C"} {:name "energy per capita", :type "decimal", :value 61230.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1589.32, :unit "TWh"} {:name "population", :type "integer", :value 25956417, :unit "people"} {:name "gdp", :type "integer", :value 1296705194119, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-AUS-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 384.078, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 14.6589, :unit "t/person"} {:name "methane", :type "decimal", :value 139.423, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0235, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.014411, :unit "°C"} {:name "energy per capita", :type "decimal", :value 63386.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1660.79, :unit "TWh"} {:name "population", :type "integer", :value 26200987, :unit "people"} {:name "gdp", :type "integer", :value 1344251444393, :unit "2011 International$"}],
              :locations [{:lat -25.27, :lon 133.78}],
              :context-refs [{:global-id "country-Australia"} {:global-id "continent-Oceania"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-BRA-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 340.183, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9549, :unit "t/person"} {:name "methane", :type "decimal", :value 417.31, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3334, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.039206, :unit "°C"} {:name "energy per capita", :type "decimal", :value 13586.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2364.26, :unit "TWh"} {:name "population", :type "integer", :value 174018278, :unit "people"} {:name "gdp", :type "integer", :value 1739728033832, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-BRA-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 346.166, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9635, :unit "t/person"} {:name "methane", :type "decimal", :value 429.615, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3473, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.040209, :unit "°C"} {:name "energy per capita", :type "decimal", :value 13068.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2304.05, :unit "TWh"} {:name "population", :type "integer", :value 176301201, :unit "people"} {:name "gdp", :type "integer", :value 1785513006345, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-BRA-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 347.765, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9482, :unit "t/person"} {:name "methane", :type "decimal", :value 450.588, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.324, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.041289, :unit "°C"} {:name "energy per capita", :type "decimal", :value 13173.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2351.54, :unit "TWh"} {:name "population", :type "integer", :value 178503485, :unit "people"} {:name "gdp", :type "integer", :value 1861742323845, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-BRA-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 344.645, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9081, :unit "t/person"} {:name "methane", :type "decimal", :value 471.538, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2463, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.042747, :unit "°C"} {:name "energy per capita", :type "decimal", :value 13211.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2386.33, :unit "TWh"} {:name "population", :type "integer", :value 180622688, :unit "people"} {:name "gdp", :type "integer", :value 1904932706436, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-BRA-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 361.434, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9786, :unit "t/person"} {:name "methane", :type "decimal", :value 494.991, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2633, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.043951, :unit "°C"} {:name "energy per capita", :type "decimal", :value 13704.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2503.37, :unit "TWh"} {:name "population", :type "integer", :value 182675144, :unit "people"} {:name "gdp", :type "integer", :value 2038447931287, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-BRA-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 364.371, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9729, :unit "t/person"} {:name "methane", :type "decimal", :value 497.686, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.231, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.045038, :unit "°C"} {:name "energy per capita", :type "decimal", :value 13910.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2569.06, :unit "TWh"} {:name "population", :type "integer", :value 184688101, :unit "people"} {:name "gdp", :type "integer", :value 2129214530430, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-BRA-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 368.871, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9762, :unit "t/person"} {:name "methane", :type "decimal", :value 493.284, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2057, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.046021, :unit "°C"} {:name "energy per capita", :type "decimal", :value 14152.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2641.58, :unit "TWh"} {:name "population", :type "integer", :value 186653099, :unit "people"} {:name "gdp", :type "integer", :value 2240805319046, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-BRA-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 390.573, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.0714, :unit "t/person"} {:name "methane", :type "decimal", :value 491.52, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2399, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.046966, :unit "°C"} {:name "energy per capita", :type "decimal", :value 14905.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2810.54, :unit "TWh"} {:name "population", :type "integer", :value 188552311, :unit "people"} {:name "gdp", :type "integer", :value 2405626901513, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-BRA-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 412.638, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.1676, :unit "t/person"} {:name "methane", :type "decimal", :value 488.155, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2875, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.047857, :unit "°C"} {:name "energy per capita", :type "decimal", :value 15424.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2936.35, :unit "TWh"} {:name "population", :type "integer", :value 190367297, :unit "people"} {:name "gdp", :type "integer", :value 2559842659987, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-BRA-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 389.775, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.0292, :unit "t/person"} {:name "methane", :type "decimal", :value 486.858, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2369, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.048668, :unit "°C"} {:name "energy per capita", :type "decimal", :value 15230.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2925.52, :unit "TWh"} {:name "population", :type "integer", :value 192079958, :unit "people"} {:name "gdp", :type "integer", :value 2589568436235, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-BRA-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 440.269, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.2729, :unit "t/person"} {:name "methane", :type "decimal", :value 507.871, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3214, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.049457, :unit "°C"} {:name "energy per capita", :type "decimal", :value 16462.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3188.73, :unit "TWh"} {:name "population", :type "integer", :value 193701931, :unit "people"} {:name "gdp", :type "integer", :value 2820492538862, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-BRA-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 461.911, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.3653, :unit "t/person"} {:name "methane", :type "decimal", :value 505.267, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3397, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.050229, :unit "°C"} {:name "energy per capita", :type "decimal", :value 16982.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3316.32, :unit "TWh"} {:name "population", :type "integer", :value 195284736, :unit "people"} {:name "gdp", :type "integer", :value 2968455007203, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-BRA-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 497.468, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.5268, :unit "t/person"} {:name "methane", :type "decimal", :value 506.852, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4232, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.051055, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17312.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3408.33, :unit "TWh"} {:name "population", :type "integer", :value 196876113, :unit "people"} {:name "gdp", :type "integer", :value 3025484250011, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-BRA-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 531.569, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.6782, :unit "t/person"} {:name "methane", :type "decimal", :value 506.459, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5069, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.051911, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17639.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3500.96, :unit "TWh"} {:name "population", :type "integer", :value 198478290, :unit "people"} {:name "gdp", :type "integer", :value 3116394665745, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-BRA-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 556.526, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.7814, :unit "t/person"} {:name "methane", :type "decimal", :value 512.64, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5692, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.052789, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17870.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3575.64, :unit "TWh"} {:name "population", :type "integer", :value 200085126, :unit "people"} {:name "gdp", :type "integer", :value 3132099937531, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-BRA-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 528.174, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.6189, :unit "t/person"} {:name "methane", :type "decimal", :value 521.174, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4919, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.053658, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17530.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3535.44, :unit "TWh"} {:name "population", :type "integer", :value 201675534, :unit "people"} {:name "gdp", :type "integer", :value 3021043246476, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-BRA-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 491.745, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.4198, :unit "t/person"} {:name "methane", :type "decimal", :value 524.133, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3894, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.054479, :unit "°C"} {:name "energy per capita", :type "decimal", :value 16984.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3451.45, :unit "TWh"} {:name "population", :type "integer", :value 203218108, :unit "people"} {:name "gdp", :type "integer", :value 2922076150345, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-BRA-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 497.255, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.4291, :unit "t/person"} {:name "methane", :type "decimal", :value 522.645, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3822, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.055261, :unit "°C"} {:name "energy per capita", :type "decimal", :value 16988.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3477.63, :unit "TWh"} {:name "population", :type "integer", :value 204703440, :unit "people"} {:name "gdp", :type "integer", :value 2960731613785, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-BRA-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 476.601, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.3124, :unit "t/person"} {:name "methane", :type "decimal", :value 522.116, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2974, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.056041, :unit "°C"} {:name "energy per capita", :type "decimal", :value 16915.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3486.38, :unit "TWh"} {:name "population", :type "integer", :value 206107265, :unit "people"} {:name "gdp", :type "integer", :value 3013541086239, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-BRA-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 476.724, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.298, :unit "t/person"} {:name "methane", :type "decimal", :value 528.946, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2854, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.056836, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17194.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3567.02, :unit "TWh"} {:name "population", :type "integer", :value 207455459, :unit "people"} {:name "gdp", :type "integer", :value 3050327159562, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-BRA-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 447.999, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.147, :unit "t/person"} {:name "methane", :type "decimal", :value 535.772, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2742, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.05767, :unit "°C"} {:name "energy per capita", :type "decimal", :value 16401.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3422.43, :unit "TWh"} {:name "population", :type "integer", :value 208660845, :unit "people"} {:name "gdp", :type "integer", :value 2950377451264, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-BRA-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 496.562, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.3697, :unit "t/person"} {:name "methane", :type "decimal", :value 545.361, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3469, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.058588, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17089.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3581.2, :unit "TWh"} {:name "population", :type "integer", :value 209550291, :unit "people"} {:name "gdp", :type "integer", :value 3097565215045, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-BRA-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 480.058, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.2827, :unit "t/person"} {:name "methane", :type "decimal", :value 566.221, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2792, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.05954, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17744.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3731.72, :unit "TWh"} {:name "population", :type "integer", :value 210306411, :unit "people"} {:name "gdp", :type "integer", :value 3187412907763, :unit "2011 International$"}],
              :locations [{:lat -14.24, :lon -51.93}],
              :context-refs [{:global-id "country-Brazil"} {:global-id "continent-South America"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-CAN-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 566.68, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 18.344, :unit "t/person"} {:name "methane", :type "decimal", :value 121.653, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.2213, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.023338, :unit "°C"} {:name "energy per capita", :type "decimal", :value 118073.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3647.49, :unit "TWh"} {:name "population", :type "integer", :value 30891800, :unit "people"} {:name "gdp", :type "integer", :value 1137681278760, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-CAN-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 558.981, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.9083, :unit "t/person"} {:name "methane", :type "decimal", :value 121.985, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.1756, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.023693, :unit "°C"} {:name "energy per capita", :type "decimal", :value 114440.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3572.1, :unit "TWh"} {:name "population", :type "integer", :value 31213578, :unit "people"} {:name "gdp", :type "integer", :value 1161459919261, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-CAN-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 564.256, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.8927, :unit "t/person"} {:name "methane", :type "decimal", :value 124.416, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.1483, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.024041, :unit "°C"} {:name "energy per capita", :type "decimal", :value 117118.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3693.39, :unit "TWh"} {:name "population", :type "integer", :value 31535579, :unit "people"} {:name "gdp", :type "integer", :value 1199865635264, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-CAN-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 581.19, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 18.2555, :unit "t/person"} {:name "methane", :type "decimal", :value 123.516, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.1017, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.024395, :unit "°C"} {:name "energy per capita", :type "decimal", :value 116565.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3711.02, :unit "TWh"} {:name "population", :type "integer", :value 31836483, :unit "people"} {:name "gdp", :type "integer", :value 1224575760991, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-CAN-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 576.286, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.9346, :unit "t/person"} {:name "methane", :type "decimal", :value 127.205, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.0143, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.024748, :unit "°C"} {:name "energy per capita", :type "decimal", :value 117265.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3768.07, :unit "TWh"} {:name "population", :type "integer", :value 32132680, :unit "people"} {:name "gdp", :type "integer", :value 1265917327101, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-CAN-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 570.396, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.583, :unit "t/person"} {:name "methane", :type "decimal", :value 126.354, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.9271, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.025095, :unit "°C"} {:name "energy per capita", :type "decimal", :value 117037.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3796.7, :unit "TWh"} {:name "population", :type "integer", :value 32440172, :unit "people"} {:name "gdp", :type "integer", :value 1310077836124, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-CAN-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 566.315, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.2872, :unit "t/person"} {:name "methane", :type "decimal", :value 129.923, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.8511, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.02543, :unit "°C"} {:name "energy per capita", :type "decimal", :value 112017.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3669.61, :unit "TWh"} {:name "population", :type "integer", :value 32759174, :unit "people"} {:name "gdp", :type "integer", :value 1348293394113, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-CAN-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 589.956, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.8277, :unit "t/person"} {:name "methane", :type "decimal", :value 128.17, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.8729, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.025762, :unit "°C"} {:name "energy per capita", :type "decimal", :value 118416.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3918.65, :unit "TWh"} {:name "population", :type "integer", :value 33092171, :unit "people"} {:name "gdp", :type "integer", :value 1379925831954, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-CAN-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 574.214, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.1668, :unit "t/person"} {:name "methane", :type "decimal", :value 124.892, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.7916, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.026087, :unit "°C"} {:name "energy per capita", :type "decimal", :value 115754.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3871.87, :unit "TWh"} {:name "population", :type "integer", :value 33449088, :unit "people"} {:name "gdp", :type "integer", :value 1397938727409, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-CAN-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 541.13, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 15.9973, :unit "t/person"} {:name "methane", :type "decimal", :value 118.125, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.7172, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.026376, :unit "°C"} {:name "energy per capita", :type "decimal", :value 109988.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3720.5, :unit "TWh"} {:name "population", :type "integer", :value 33826370, :unit "people"} {:name "gdp", :type "integer", :value 1360538049277, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-CAN-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 554.307, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.2093, :unit "t/person"} {:name "methane", :type "decimal", :value 118.916, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.6637, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.026664, :unit "°C"} {:name "energy per capita", :type "decimal", :value 109967.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3760.55, :unit "TWh"} {:name "population", :type "integer", :value 34196900, :unit "people"} {:name "gdp", :type "integer", :value 1406355361431, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-CAN-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 562.832, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.2878, :unit "t/person"} {:name "methane", :type "decimal", :value 119.642, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.6323, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.026974, :unit "°C"} {:name "energy per capita", :type "decimal", :value 112865.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3900.12, :unit "TWh"} {:name "population", :type "integer", :value 34555449, :unit "people"} {:name "gdp", :type "integer", :value 1454221365109, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-CAN-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 561.77, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.0862, :unit "t/person"} {:name "methane", :type "decimal", :value 122.582, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.6071, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.027286, :unit "°C"} {:name "energy per capita", :type "decimal", :value 111724.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3901.69, :unit "TWh"} {:name "population", :type "integer", :value 34922513, :unit "people"} {:name "gdp", :type "integer", :value 1479829523672, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-CAN-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 568.59, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.1104, :unit "t/person"} {:name "methane", :type "decimal", :value 123.239, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.6118, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.027601, :unit "°C"} {:name "energy per capita", :type "decimal", :value 113458.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4004.35, :unit "TWh"} {:name "population", :type "integer", :value 35293421, :unit "people"} {:name "gdp", :type "integer", :value 1514296485427, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-CAN-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 564.938, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 15.8538, :unit "t/person"} {:name "methane", :type "decimal", :value 128.456, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5929, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.02792, :unit "°C"} {:name "energy per capita", :type "decimal", :value 112508.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4009.16, :unit "TWh"} {:name "population", :type "integer", :value 35634265, :unit "people"} {:name "gdp", :type "integer", :value 1557757464897, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-CAN-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 563.003, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 15.6554, :unit "t/person"} {:name "methane", :type "decimal", :value 127.164, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5902, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.02824, :unit "°C"} {:name "energy per capita", :type "decimal", :value 111824.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4021.45, :unit "TWh"} {:name "population", :type "integer", :value 35962236, :unit "people"} {:name "gdp", :type "integer", :value 1568025782105, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-CAN-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 553.707, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 15.2313, :unit "t/person"} {:name "methane", :type "decimal", :value 122.513, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5645, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.028548, :unit "°C"} {:name "energy per capita", :type "decimal", :value 109289.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3973.04, :unit "TWh"} {:name "population", :type "integer", :value 36353345, :unit "people"} {:name "gdp", :type "integer", :value 1583727974022, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-CAN-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 566.66, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 15.3948, :unit "t/person"} {:name "methane", :type "decimal", :value 124.508, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5752, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.028868, :unit "°C"} {:name "energy per capita", :type "decimal", :value 109394.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4026.63, :unit "TWh"} {:name "population", :type "integer", :value 36808498, :unit "people"} {:name "gdp", :type "integer", :value 1631871440185, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-CAN-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 575.091, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 15.4201, :unit "t/person"} {:name "methane", :type "decimal", :value 125.972, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5656, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.029185, :unit "°C"} {:name "energy per capita", :type "decimal", :value 109464.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4082.49, :unit "TWh"} {:name "population", :type "integer", :value 37294996, :unit "people"} {:name "gdp", :type "integer", :value 1677189073139, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-CAN-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 579.007, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 15.3246, :unit "t/person"} {:name "methane", :type "decimal", :value 125.164, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5612, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.029498, :unit "°C"} {:name "energy per capita", :type "decimal", :value 107455.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4059.99, :unit "TWh"} {:name "population", :type "integer", :value 37782934, :unit "people"} {:name "gdp", :type "integer", :value 1708858221312, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-CAN-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 524.208, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 13.7328, :unit "t/person"} {:name "methane", :type "decimal", :value 110.822, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.491, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.029786, :unit "°C"} {:name "energy per capita", :type "decimal", :value 100465.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3834.94, :unit "TWh"} {:name "population", :type "integer", :value 38171903, :unit "people"} {:name "gdp", :type "integer", :value 1622144486274, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-CAN-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 537.479, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 13.9772, :unit "t/person"} {:name "methane", :type "decimal", :value 114.91, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4579, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.030085, :unit "°C"} {:name "energy per capita", :type "decimal", :value 100431.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3861.98, :unit "TWh"} {:name "population", :type "integer", :value 38454058, :unit "people"} {:name "gdp", :type "integer", :value 1703443269914, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-CAN-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 547.658, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 14.1072, :unit "t/person"} {:name "methane", :type "decimal", :value 117.055, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4593, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.03038, :unit "°C"} {:name "energy per capita", :type "decimal", :value 102122.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3964.52, :unit "TWh"} {:name "population", :type "integer", :value 38821259, :unit "people"} {:name "gdp", :type "integer", :value 1761295624423, :unit "2011 International$"}],
              :locations [{:lat 56.13, :lon -106.35}],
              :context-refs [{:global-id "country-Canada"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-CHN-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 3643.81, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.8701, :unit "t/person"} {:name "methane", :type "decimal", :value 950.032, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 14.283, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.061084, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9294.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 11800.28, :unit "TWh"} {:name "population", :type "integer", :value 1269581174, :unit "people"} {:name "gdp", :type "integer", :value 5952682232028, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-CHN-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 3724.114, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.9124, :unit "t/person"} {:name "methane", :type "decimal", :value 957.323, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 14.4947, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.062864, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9752.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 12470.06, :unit "TWh"} {:name "population", :type "integer", :value 1278725061, :unit "people"} {:name "gdp", :type "integer", :value 6329737256820, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-CHN-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 4098.181, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.1846, :unit "t/person"} {:name "methane", :type "decimal", :value 961.78, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 15.603, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.064778, :unit "°C"} {:name "energy per capita", :type "decimal", :value 10553.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 13580.49, :unit "TWh"} {:name "population", :type "integer", :value 1286866835, :unit "people"} {:name "gdp", :type "integer", :value 6814493895929, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-CHN-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 4835.251, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.7352, :unit "t/person"} {:name "methane", :type "decimal", :value 1007.435, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 17.4856, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.067015, :unit "°C"} {:name "energy per capita", :type "decimal", :value 12219.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 15818.76, :unit "TWh"} {:name "population", :type "integer", :value 1294517323, :unit "people"} {:name "gdp", :type "integer", :value 7248623080792, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-CHN-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 5210.961, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.002, :unit "t/person"} {:name "methane", :type "decimal", :value 1069.441, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 18.2138, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.069425, :unit "°C"} {:name "energy per capita", :type "decimal", :value 14214.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 18509.14, :unit "TWh"} {:name "population", :type "integer", :value 1302100316, :unit "people"} {:name "gdp", :type "integer", :value 7830953177799, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-CHN-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 5881.991, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.49, :unit "t/person"} {:name "methane", :type "decimal", :value 1119.984, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 19.8723, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.072125, :unit "°C"} {:name "energy per capita", :type "decimal", :value 16050.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 21027.16, :unit "TWh"} {:name "population", :type "integer", :value 1310027135, :unit "people"} {:name "gdp", :type "integer", :value 8602939667333, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-CHN-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 6486.185, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.921, :unit "t/person"} {:name "methane", :type "decimal", :value 1162.008, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 21.2007, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.0751, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17486.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 23048.33, :unit "TWh"} {:name "population", :type "integer", :value 1318054030, :unit "people"} {:name "gdp", :type "integer", :value 9489543138489, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-CHN-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 6974.663, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.2607, :unit "t/person"} {:name "methane", :type "decimal", :value 1176.147, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 22.1423, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.078283, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18893.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25049.58, :unit "TWh"} {:name "population", :type "integer", :value 1325813576, :unit "people"} {:name "gdp", :type "integer", :value 10358206113742, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-CHN-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 7492.404, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.6172, :unit "t/person"} {:name "methane", :type "decimal", :value 1183.054, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 23.3775, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.081705, :unit "°C"} {:name "energy per capita", :type "decimal", :value 19508.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 26020.46, :unit "TWh"} {:name "population", :type "integer", :value 1333821011, :unit "people"} {:name "gdp", :type "integer", :value 10799148337884, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-CHN-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 7881.491, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.8707, :unit "t/person"} {:name "methane", :type "decimal", :value 1197.731, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 25.0103, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.08531, :unit "°C"} {:name "energy per capita", :type "decimal", :value 20229.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 27158.9, :unit "TWh"} {:name "population", :type "integer", :value 1342522717, :unit "people"} {:name "gdp", :type "integer", :value 11572544299171, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-CHN-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 8610.048, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.3704, :unit "t/person"} {:name "methane", :type "decimal", :value 1241.241, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 25.8423, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.089192, :unit "°C"} {:name "energy per capita", :type "decimal", :value 21497.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 29055.61, :unit "TWh"} {:name "population", :type "integer", :value 1351561505, :unit "people"} {:name "gdp", :type "integer", :value 12858808500983, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-CHN-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 9520.153, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.9988, :unit "t/person"} {:name "methane", :type "decimal", :value 1323.284, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 27.6107, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.093504, :unit "°C"} {:name "energy per capita", :type "decimal", :value 23037.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 31336.54, :unit "TWh"} {:name "population", :type "integer", :value 1360250658, :unit "people"} {:name "gdp", :type "integer", :value 13691526496125, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-CHN-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 9767.311, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.1319, :unit "t/person"} {:name "methane", :type "decimal", :value 1319.353, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 27.9427, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.097893, :unit "°C"} {:name "energy per capita", :type "decimal", :value 23821.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 32624.18, :unit "TWh"} {:name "population", :type "integer", :value 1369520866, :unit "people"} {:name "gdp", :type "integer", :value 14773157494771, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-CHN-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 9942.371, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.2098, :unit "t/person"} {:name "methane", :type "decimal", :value 1330.891, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 28.1847, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.102351, :unit "°C"} {:name "energy per capita", :type "decimal", :value 24548.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 33852.57, :unit "TWh"} {:name "population", :type "integer", :value 1379008040, :unit "people"} {:name "gdp", :type "integer", :value 15925464549520, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-CHN-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 9976.027, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.1876, :unit "t/person"} {:name "methane", :type "decimal", :value 1340.685, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 28.1287, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.106802, :unit "°C"} {:name "energy per capita", :type "decimal", :value 25021.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 34729.07, :unit "TWh"} {:name "population", :type "integer", :value 1387951970, :unit "people"} {:name "gdp", :type "integer", :value 17103948867278, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-CHN-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 9858.04, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.061, :unit "t/person"} {:name "methane", :type "decimal", :value 1341.393, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 27.8448, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.111231, :unit "°C"} {:name "energy per capita", :type "decimal", :value 25166.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 35135.99, :unit "TWh"} {:name "population", :type "integer", :value 1396134171, :unit "people"} {:name "gdp", :type "integer", :value 18301224436466, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-CHN-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 9748.175, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.9429, :unit "t/person"} {:name "methane", :type "decimal", :value 1299.595, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 27.5428, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.115562, :unit "°C"} {:name "energy per capita", :type "decimal", :value 25125.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 35277.71, :unit "TWh"} {:name "population", :type "integer", :value 1404052628, :unit "people"} {:name "gdp", :type "integer", :value 19545709173693, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-CHN-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 10000.014, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.0804, :unit "t/person"} {:name "methane", :type "decimal", :value 1319.021, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 27.7974, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.119993, :unit "°C"} {:name "energy per capita", :type "decimal", :value 25949.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 36649.38, :unit "TWh"} {:name "population", :type "integer", :value 1412354730, :unit "people"} {:name "gdp", :type "integer", :value 20894362082435, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-CHN-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 10346.79, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.2916, :unit "t/person"} {:name "methane", :type "decimal", :value 1354.933, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 28.1668, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.124574, :unit "°C"} {:name "energy per capita", :type "decimal", :value 27068.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 38409.82, :unit "TWh"} {:name "population", :type "integer", :value 1419008954, :unit "people"} {:name "gdp", :type "integer", :value 22294284747546, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-CHN-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 10713.515, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.5261, :unit "t/person"} {:name "methane", :type "decimal", :value 1373.722, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 28.8879, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.1293, :unit "°C"} {:name "energy per capita", :type "decimal", :value 28231.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 40188.72, :unit "TWh"} {:name "population", :type "integer", :value 1423520357, :unit "people"} {:name "gdp", :type "integer", :value 23631941251482, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-CHN-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 10896.521, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.6407, :unit "t/person"} {:name "methane", :type "decimal", :value 1398.962, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 30.9928, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.134105, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29095.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 41493.89, :unit "TWh"} {:name "population", :type "integer", :value 1426106093, :unit "people"} {:name "gdp", :type "integer", :value 24151843898871, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-CHN-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 11284.401, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.9109, :unit "t/person"} {:name "methane", :type "decimal", :value 1450.486, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 30.6085, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.139078, :unit "°C"} {:name "energy per capita", :type "decimal", :value 30738.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 43847.18, :unit "TWh"} {:name "population", :type "integer", :value 1426437269, :unit "people"} {:name "gdp", :type "integer", :value 26180600167672, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-CHN-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 11711.808, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.2178, :unit "t/person"} {:name "methane", :type "decimal", :value 1513.475, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 31.2084, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.144243, :unit "°C"} {:name "energy per capita", :type "decimal", :value 31235.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 44516.49, :unit "TWh"} {:name "population", :type "integer", :value 1425179562, :unit "people"} {:name "gdp", :type "integer", :value 26966016991166, :unit "2011 International$"}],
              :locations [{:lat 35.86, :lon 104.2}],
              :context-refs [{:global-id "country-China"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-EGY-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 142.837, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9544, :unit "t/person"} {:name "methane", :type "decimal", :value 48.798, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5599, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001157, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7779.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 568.53, :unit "TWh"} {:name "population", :type "integer", :value 73083287, :unit "people"} {:name "gdp", :type "integer", :value 444250275939, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-EGY-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 126.372, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.6928, :unit "t/person"} {:name "methane", :type "decimal", :value 48.443, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.4919, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001215, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7947.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 593.31, :unit "TWh"} {:name "population", :type "integer", :value 74652035, :unit "people"} {:name "gdp", :type "integer", :value 475679003242, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-EGY-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 128.362, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.6837, :unit "t/person"} {:name "methane", :type "decimal", :value 50.058, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.4887, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001273, :unit "°C"} {:name "energy per capita", :type "decimal", :value 8004.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 610.24, :unit "TWh"} {:name "population", :type "integer", :value 76239140, :unit "people"} {:name "gdp", :type "integer", :value 501912285229, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-EGY-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 149.954, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9261, :unit "t/person"} {:name "methane", :type "decimal", :value 50.997, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5423, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001341, :unit "°C"} {:name "energy per capita", :type "decimal", :value 8297.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 645.99, :unit "TWh"} {:name "population", :type "integer", :value 77853548, :unit "people"} {:name "gdp", :type "integer", :value 528750466385, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-EGY-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 151.917, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9114, :unit "t/person"} {:name "methane", :type "decimal", :value 51.405, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.531, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001411, :unit "°C"} {:name "energy per capita", :type "decimal", :value 8581.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 682.05, :unit "TWh"} {:name "population", :type "integer", :value 79477441, :unit "people"} {:name "gdp", :type "integer", :value 561597926809, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-EGY-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 166.934, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.0583, :unit "t/person"} {:name "methane", :type "decimal", :value 51.591, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.564, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001486, :unit "°C"} {:name "energy per capita", :type "decimal", :value 8624.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 699.42, :unit "TWh"} {:name "population", :type "integer", :value 81101012, :unit "people"} {:name "gdp", :type "integer", :value 600297394092, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-EGY-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 178.389, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.1571, :unit "t/person"} {:name "methane", :type "decimal", :value 52.802, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.5831, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001567, :unit "°C"} {:name "energy per capita", :type "decimal", :value 8907.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 736.64, :unit "TWh"} {:name "population", :type "integer", :value 82700404, :unit "people"} {:name "gdp", :type "integer", :value 650403845291, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-EGY-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 189.201, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.245, :unit "t/person"} {:name "methane", :type "decimal", :value 52.371, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6007, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001653, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9309.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 784.58, :unit "TWh"} {:name "population", :type "integer", :value 84276224, :unit "people"} {:name "gdp", :type "integer", :value 713462332878, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-EGY-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 195.86, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.281, :unit "t/person"} {:name "methane", :type "decimal", :value 53.566, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6111, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001742, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9687.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 831.84, :unit "TWh"} {:name "population", :type "integer", :value 85864791, :unit "people"} {:name "gdp", :type "integer", :value 775303694329, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-EGY-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 205.243, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.3456, :unit "t/person"} {:name "methane", :type "decimal", :value 53.06, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6513, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001835, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9847.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 861.67, :unit "TWh"} {:name "population", :type "integer", :value 87501635, :unit "people"} {:name "gdp", :type "integer", :value 831415021492, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-EGY-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 202.704, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.2726, :unit "t/person"} {:name "methane", :type "decimal", :value 50.266, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6084, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001926, :unit "°C"} {:name "energy per capita", :type "decimal", :value 10035.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 895.11, :unit "TWh"} {:name "population", :type "integer", :value 89196073, :unit "people"} {:name "gdp", :type "integer", :value 898751418203, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-EGY-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 221.675, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.4335, :unit "t/person"} {:name "methane", :type "decimal", :value 51.806, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6429, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002027, :unit "°C"} {:name "energy per capita", :type "decimal", :value 10336.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 941.57, :unit "TWh"} {:name "population", :type "integer", :value 91093066, :unit "people"} {:name "gdp", :type "integer", :value 923052843843, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-EGY-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 212.524, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.2813, :unit "t/person"} {:name "methane", :type "decimal", :value 52.923, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.608, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002123, :unit "°C"} {:name "energy per capita", :type "decimal", :value 10518.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 979.9, :unit "TWh"} {:name "population", :type "integer", :value 93161000, :unit "people"} {:name "gdp", :type "integer", :value 950287307199, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-EGY-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 219.087, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.2981, :unit "t/person"} {:name "methane", :type "decimal", :value 51.534, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6211, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002222, :unit "°C"} {:name "energy per capita", :type "decimal", :value 10209.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 973.27, :unit "TWh"} {:name "population", :type "integer", :value 95333551, :unit "people"} {:name "gdp", :type "integer", :value 984556585202, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-EGY-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 227.254, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.3301, :unit "t/person"} {:name "methane", :type "decimal", :value 51.438, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6408, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002325, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9863.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 961.97, :unit "TWh"} {:name "population", :type "integer", :value 97528655, :unit "people"} {:name "gdp", :type "integer", :value 1035721838706, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-EGY-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 226.654, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.2757, :unit "t/person"} {:name "methane", :type "decimal", :value 50.312, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6402, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002427, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9800.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 976.11, :unit "TWh"} {:name "population", :type "integer", :value 99597334, :unit "people"} {:name "gdp", :type "integer", :value 1079611566322, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-EGY-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 245.164, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.412, :unit "t/person"} {:name "methane", :type "decimal", :value 50.653, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6927, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002538, :unit "°C"} {:name "energy per capita", :type "decimal", :value 10135.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1030.26, :unit "TWh"} {:name "population", :type "integer", :value 101644583, :unit "people"} {:name "gdp", :type "integer", :value 1121578599865, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-EGY-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 253.364, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.4433, :unit "t/person"} {:name "methane", :type "decimal", :value 51.054, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.7043, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002653, :unit "°C"} {:name "energy per capita", :type "decimal", :value 10325.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1070.75, :unit "TWh"} {:name "population", :type "integer", :value 103696055, :unit "people"} {:name "gdp", :type "integer", :value 1158060486104, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-EGY-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 241.903, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.289, :unit "t/person"} {:name "methane", :type "decimal", :value 50.307, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6585, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002762, :unit "°C"} {:name "energy per capita", :type "decimal", :value 10137.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1071.35, :unit "TWh"} {:name "population", :type "integer", :value 105682092, :unit "people"} {:name "gdp", :type "integer", :value 1220661740193, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-EGY-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 234.06, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.1762, :unit "t/person"} {:name "methane", :type "decimal", :value 46.912, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6311, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002868, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9749.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1048.58, :unit "TWh"} {:name "population", :type "integer", :value 107553159, :unit "people"} {:name "gdp", :type "integer", :value 1289253465646, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-EGY-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 246.323, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.2533, :unit "t/person"} {:name "methane", :type "decimal", :value 46.511, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.7006, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00298, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9028.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 986.9, :unit "TWh"} {:name "population", :type "integer", :value 109315118, :unit "people"} {:name "gdp", :type "integer", :value 1307778142529, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-EGY-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 266.839, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.4049, :unit "t/person"} {:name "methane", :type "decimal", :value 47.178, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.7238, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003101, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9564.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1061.29, :unit "TWh"} {:name "population", :type "integer", :value 110957005, :unit "people"} {:name "gdp", :type "integer", :value 1400985648780, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-EGY-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 249.899, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.219, :unit "t/person"} {:name "methane", :type "decimal", :value 49.239, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6659, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003214, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9857.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1110.14, :unit "TWh"} {:name "population", :type "integer", :value 112618252, :unit "people"} {:name "gdp", :type "integer", :value 1460427350416, :unit "2011 International$"}],
              :locations [{:lat 26.82, :lon 30.8}],
              :context-refs [{:global-id "country-Egypt"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-FRA-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 407.445, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.8497, :unit "t/person"} {:name "methane", :type "decimal", :value 86.397, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5971, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013639, :unit "°C"} {:name "energy per capita", :type "decimal", :value 52416.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3117.91, :unit "TWh"} {:name "population", :type "integer", :value 59483716, :unit "people"} {:name "gdp", :type "integer", :value 2031676031476, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-FRA-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 411.592, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.8707, :unit "t/person"} {:name "methane", :type "decimal", :value 86.257, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.602, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013796, :unit "°C"} {:name "energy per capita", :type "decimal", :value 52467.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3143.05, :unit "TWh"} {:name "population", :type "integer", :value 59905131, :unit "people"} {:name "gdp", :type "integer", :value 2077786711965, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-FRA-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 407.027, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.747, :unit "t/person"} {:name "methane", :type "decimal", :value 84.895, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5497, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.01395, :unit "°C"} {:name "energy per capita", :type "decimal", :value 51700.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3118.92, :unit "TWh"} {:name "population", :type "integer", :value 60327247, :unit "people"} {:name "gdp", :type "integer", :value 2107316572579, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-FRA-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 412.989, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.7992, :unit "t/person"} {:name "methane", :type "decimal", :value 83.604, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4935, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.014108, :unit "°C"} {:name "energy per capita", :type "decimal", :value 51909.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3153.04, :unit "TWh"} {:name "population", :type "integer", :value 60741119, :unit "people"} {:name "gdp", :type "integer", :value 2130940712046, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-FRA-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 414.073, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.7686, :unit "t/person"} {:name "methane", :type "decimal", :value 82.771, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4473, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.014266, :unit "°C"} {:name "energy per capita", :type "decimal", :value 51983.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3180.13, :unit "TWh"} {:name "population", :type "integer", :value 61175244, :unit "people"} {:name "gdp", :type "integer", :value 2196883540402, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-FRA-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 416.343, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.7561, :unit "t/person"} {:name "methane", :type "decimal", :value 81.853, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4066, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.014426, :unit "°C"} {:name "energy per capita", :type "decimal", :value 51181.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3154.08, :unit "TWh"} {:name "population", :type "integer", :value 61625031, :unit "people"} {:name "gdp", :type "integer", :value 2238790479440, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-FRA-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 406.669, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.5539, :unit "t/person"} {:name "methane", :type "decimal", :value 80.954, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3292, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.014581, :unit "°C"} {:name "energy per capita", :type "decimal", :value 50426.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3128.93, :unit "TWh"} {:name "population", :type "integer", :value 62049831, :unit "people"} {:name "gdp", :type "integer", :value 2297018080700, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-FRA-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 396.461, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.3502, :unit "t/person"} {:name "methane", :type "decimal", :value 80.865, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2586, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.014733, :unit "°C"} {:name "energy per capita", :type "decimal", :value 49265.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3075.79, :unit "TWh"} {:name "population", :type "integer", :value 62432438, :unit "people"} {:name "gdp", :type "integer", :value 2354672057875, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-FRA-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 389.794, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.2089, :unit "t/person"} {:name "methane", :type "decimal", :value 81.134, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2162, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.014882, :unit "°C"} {:name "energy per capita", :type "decimal", :value 49137.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3084.84, :unit "TWh"} {:name "population", :type "integer", :value 62780187, :unit "people"} {:name "gdp", :type "integer", :value 2362451821701, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-FRA-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 371.404, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.8854, :unit "t/person"} {:name "methane", :type "decimal", :value 79.836, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1786, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.015024, :unit "°C"} {:name "energy per capita", :type "decimal", :value 46095.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2908.91, :unit "TWh"} {:name "population", :type "integer", :value 63106460, :unit "people"} {:name "gdp", :type "integer", :value 2295352990111, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-FRA-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 377.079, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.946, :unit "t/person"} {:name "methane", :type "decimal", :value 79.801, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1318, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.01517, :unit "°C"} {:name "energy per capita", :type "decimal", :value 47325.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3001.23, :unit "TWh"} {:name "population", :type "integer", :value 63417366, :unit "people"} {:name "gdp", :type "integer", :value 2342490492199, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-FRA-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 355.127, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.572, :unit "t/person"} {:name "methane", :type "decimal", :value 77.433, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.03, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.015307, :unit "°C"} {:name "energy per capita", :type "decimal", :value 45465.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2897.71, :unit "TWh"} {:name "population", :type "integer", :value 63733776, :unit "people"} {:name "gdp", :type "integer", :value 2393401943613, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-FRA-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 358.1, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.5902, :unit "t/person"} {:name "methane", :type "decimal", :value 76.667, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0245, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.015447, :unit "°C"} {:name "energy per capita", :type "decimal", :value 45190.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2894.86, :unit "TWh"} {:name "population", :type "integer", :value 64058465, :unit "people"} {:name "gdp", :type "integer", :value 2400896391731, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-FRA-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 360.013, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.5913, :unit "t/person"} {:name "methane", :type "decimal", :value 76.53, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0206, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.01559, :unit "°C"} {:name "energy per capita", :type "decimal", :value 45428.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2925.05, :unit "TWh"} {:name "population", :type "integer", :value 64387736, :unit "people"} {:name "gdp", :type "integer", :value 2414733629218, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-FRA-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 327.809, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.0672, :unit "t/person"} {:name "methane", :type "decimal", :value 75.539, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9243, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.015719, :unit "°C"} {:name "energy per capita", :type "decimal", :value 43247.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2797.78, :unit "TWh"} {:name "population", :type "integer", :value 64692495, :unit "people"} {:name "gdp", :type "integer", :value 2437822780219, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-FRA-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 332.131, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.1163, :unit "t/person"} {:name "methane", :type "decimal", :value 74.128, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9381, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.015852, :unit "°C"} {:name "energy per capita", :type "decimal", :value 43493.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2823.43, :unit "TWh"} {:name "population", :type "integer", :value 64916337, :unit "people"} {:name "gdp", :type "integer", :value 2464953810985, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-FRA-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 335.496, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.1546, :unit "t/person"} {:name "methane", :type "decimal", :value 74.133, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9479, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.015987, :unit "°C"} {:name "energy per capita", :type "decimal", :value 42506.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2766.6, :unit "TWh"} {:name "population", :type "integer", :value 65086853, :unit "people"} {:name "gdp", :type "integer", :value 2491956183265, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-FRA-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 338.368, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.183, :unit "t/person"} {:name "methane", :type "decimal", :value 73.88, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9406, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.016128, :unit "°C"} {:name "energy per capita", :type "decimal", :value 42218.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2756.23, :unit "TWh"} {:name "population", :type "integer", :value 65284775, :unit "people"} {:name "gdp", :type "integer", :value 2549057506683, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-FRA-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 323.102, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.9314, :unit "t/person"} {:name "methane", :type "decimal", :value 73.924, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.8796, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.016258, :unit "°C"} {:name "energy per capita", :type "decimal", :value 42589.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2790.43, :unit "TWh"} {:name "population", :type "integer", :value 65519537, :unit "people"} {:name "gdp", :type "integer", :value 2596599107835, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-FRA-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 316.321, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.8125, :unit "t/person"} {:name "methane", :type "decimal", :value 71.514, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.8529, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.016395, :unit "°C"} {:name "energy per capita", :type "decimal", :value 41554.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2731.35, :unit "TWh"} {:name "population", :type "integer", :value 65729460, :unit "people"} {:name "gdp", :type "integer", :value 2644453795826, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-FRA-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 281.515, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.2715, :unit "t/person"} {:name "methane", :type "decimal", :value 68.697, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.8007, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.016511, :unit "°C"} {:name "energy per capita", :type "decimal", :value 37087.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2444.27, :unit "TWh"} {:name "population", :type "integer", :value 65905273, :unit "people"} {:name "gdp", :type "integer", :value 2438594133224, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-FRA-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 307.272, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.6497, :unit "t/person"} {:name "methane", :type "decimal", :value 67.554, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.8335, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.016639, :unit "°C"} {:name "energy per capita", :type "decimal", :value 39307.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2597.56, :unit "TWh"} {:name "population", :type "integer", :value 66083547, :unit "people"} {:name "gdp", :type "integer", :value 2604822841790, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-FRA-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 295.304, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.4556, :unit "t/person"} {:name "methane", :type "decimal", :value 65.721, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.7869, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.016761, :unit "°C"} {:name "energy per capita", :type "decimal", :value 34796.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2306.19, :unit "TWh"} {:name "population", :type "integer", :value 66277412, :unit "people"} {:name "gdp", :type "integer", :value 2671568767031, :unit "2011 International$"}],
              :locations [{:lat 46.23, :lon 2.21}],
              :context-refs [{:global-id "country-France"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-DEU-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 898.976, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.9903, :unit "t/person"} {:name "methane", :type "decimal", :value 100.75, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.5238, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.034632, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48756.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3988.18, :unit "TWh"} {:name "population", :type "integer", :value 81797255, :unit "people"} {:name "gdp", :type "integer", :value 2718560455818, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-DEU-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 915.255, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 11.1707, :unit "t/person"} {:name "methane", :type "decimal", :value 96.953, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.5623, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.035041, :unit "°C"} {:name "energy per capita", :type "decimal", :value 49586.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4062.8, :unit "TWh"} {:name "population", :type "integer", :value 81933882, :unit "people"} {:name "gdp", :type "integer", :value 2793373055251, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-DEU-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 898.834, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.9523, :unit "t/person"} {:name "methane", :type "decimal", :value 93.47, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.4221, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.035444, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48882.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4011.72, :unit "TWh"} {:name "population", :type "integer", :value 82068328, :unit "people"} {:name "gdp", :type "integer", :value 2822441773260, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-DEU-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 894.501, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.8937, :unit "t/person"} {:name "methane", :type "decimal", :value 90.241, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.2348, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.035845, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48841.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4010.47, :unit "TWh"} {:name "population", :type "integer", :value 82111619, :unit "people"} {:name "gdp", :type "integer", :value 2831676106520, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-DEU-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 876.57, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.6778, :unit "t/person"} {:name "methane", :type "decimal", :value 87.161, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.0639, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.03624, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48845.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4009.89, :unit "TWh"} {:name "population", :type "integer", :value 82092706, :unit "people"} {:name "gdp", :type "integer", :value 2894624877966, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-DEU-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 867.88, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.5779, :unit "t/person"} {:name "methane", :type "decimal", :value 84.118, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.9321, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.03663, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48297.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3962.64, :unit "TWh"} {:name "population", :type "integer", :value 82046733, :unit "people"} {:name "gdp", :type "integer", :value 2945460930734, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-DEU-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 886.434, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.8496, :unit "t/person"} {:name "methane", :type "decimal", :value 81.356, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.8974, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.037029, :unit "°C"} {:name "energy per capita", :type "decimal", :value 49722.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4062.39, :unit "TWh"} {:name "population", :type "integer", :value 81701971, :unit "people"} {:name "gdp", :type "integer", :value 3086359147617, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-DEU-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 846.153, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.408, :unit "t/person"} {:name "methane", :type "decimal", :value 81.571, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.6863, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.037411, :unit "°C"} {:name "energy per capita", :type "decimal", :value 47890.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3893.42, :unit "TWh"} {:name "population", :type "integer", :value 81298546, :unit "people"} {:name "gdp", :type "integer", :value 3220275669650, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-DEU-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 856.643, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.5614, :unit "t/person"} {:name "methane", :type "decimal", :value 79.662, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.6729, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.037797, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48509.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3934.67, :unit "TWh"} {:name "population", :type "integer", :value 81110780, :unit "people"} {:name "gdp", :type "integer", :value 3289020399474, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-DEU-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 789.984, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.7607, :unit "t/person"} {:name "methane", :type "decimal", :value 77.142, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.5069, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.038152, :unit "°C"} {:name "energy per capita", :type "decimal", :value 45618.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3692.18, :unit "TWh"} {:name "population", :type "integer", :value 80935276, :unit "people"} {:name "gdp", :type "integer", :value 3136502162046, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-DEU-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 826.705, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.2276, :unit "t/person"} {:name "methane", :type "decimal", :value 76.001, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.4813, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.038525, :unit "°C"} {:name "energy per capita", :type "decimal", :value 47595.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3847.15, :unit "TWh"} {:name "population", :type "integer", :value 80830916, :unit "people"} {:name "gdp", :type "integer", :value 3301123203586, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-DEU-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 804.542, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.9501, :unit "t/person"} {:name "methane", :type "decimal", :value 74.456, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.3334, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.038886, :unit "°C"} {:name "energy per capita", :type "decimal", :value 45890.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3710.58, :unit "TWh"} {:name "population", :type "integer", :value 80857877, :unit "people"} {:name "gdp", :type "integer", :value 3467712967640, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-DEU-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 814.093, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.0609, :unit "t/person"} {:name "methane", :type "decimal", :value 73.017, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.329, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.039253, :unit "°C"} {:name "energy per capita", :type "decimal", :value 46491.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3761.92, :unit "TWh"} {:name "population", :type "integer", :value 80916391, :unit "people"} {:name "gdp", :type "integer", :value 3482225441398, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-DEU-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 831.746, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.2632, :unit "t/person"} {:name "methane", :type "decimal", :value 71.534, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.3578, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.039629, :unit "°C"} {:name "energy per capita", :type "decimal", :value 47735.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3868.52, :unit "TWh"} {:name "population", :type "integer", :value 81041581, :unit "people"} {:name "gdp", :type "integer", :value 3497463140441, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-DEU-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 792.584, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.7398, :unit "t/person"} {:name "methane", :type "decimal", :value 70.317, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.2348, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.039986, :unit "°C"} {:name "energy per capita", :type "decimal", :value 45596.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3710.46, :unit "TWh"} {:name "population", :type "integer", :value 81376078, :unit "people"} {:name "gdp", :type "integer", :value 3574741315261, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-DEU-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 800.823, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.7569, :unit "t/person"} {:name "methane", :type "decimal", :value 69.267, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.262, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.040347, :unit "°C"} {:name "energy per capita", :type "decimal", :value 46064.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3780.86, :unit "TWh"} {:name "population", :type "integer", :value 82077546, :unit "people"} {:name "gdp", :type "integer", :value 3628073946157, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-DEU-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 797.988, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.6422, :unit "t/person"} {:name "methane", :type "decimal", :value 67.725, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.2547, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.040707, :unit "°C"} {:name "energy per capita", :type "decimal", :value 46416.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3841.45, :unit "TWh"} {:name "population", :type "integer", :value 82760102, :unit "people"} {:name "gdp", :type "integer", :value 3708979940863, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-DEU-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 784.595, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.4411, :unit "t/person"} {:name "methane", :type "decimal", :value 67.433, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.181, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.041061, :unit "°C"} {:name "energy per capita", :type "decimal", :value 47525.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3949.56, :unit "TWh"} {:name "population", :type "integer", :value 83104010, :unit "people"} {:name "gdp", :type "integer", :value 3808389445530, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-DEU-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 758.771, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.1014, :unit "t/person"} {:name "methane", :type "decimal", :value 65.036, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.0656, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.041402, :unit "°C"} {:name "energy per capita", :type "decimal", :value 46125.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3845.41, :unit "TWh"} {:name "population", :type "integer", :value 83368507, :unit "people"} {:name "gdp", :type "integer", :value 3845758522294, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-DEU-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 708.649, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.4808, :unit "t/person"} {:name "methane", :type "decimal", :value 63.043, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.9108, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.04172, :unit "°C"} {:name "energy per capita", :type "decimal", :value 44922.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3753.7, :unit "TWh"} {:name "population", :type "integer", :value 83559184, :unit "people"} {:name "gdp", :type "integer", :value 3886392581475, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-DEU-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 647.177, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.7387, :unit "t/person"} {:name "methane", :type "decimal", :value 61.045, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.8408, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.042011, :unit "°C"} {:name "energy per capita", :type "decimal", :value 41838.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3498.92, :unit "TWh"} {:name "population", :type "integer", :value 83628711, :unit "people"} {:name "gdp", :type "integer", :value 3742721113842, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-DEU-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 677.998, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.1006, :unit "t/person"} {:name "methane", :type "decimal", :value 60.245, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.839, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.042317, :unit "°C"} {:name "energy per capita", :type "decimal", :value 42994.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3598.49, :unit "TWh"} {:name "population", :type "integer", :value 83697080, :unit "people"} {:name "gdp", :type "integer", :value 3841041747482, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-DEU-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 667.843, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.9424, :unit "t/person"} {:name "methane", :type "decimal", :value 58.913, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.7796, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.042617, :unit "°C"} {:name "energy per capita", :type "decimal", :value 41094.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3455.45, :unit "TWh"} {:name "population", :type "integer", :value 84086227, :unit "people"} {:name "gdp", :type "integer", :value 3909612753939, :unit "2011 International$"}],
              :locations [{:lat 51.17, :lon 10.45}],
              :context-refs [{:global-id "country-Germany"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-IND-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 987.065, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.933, :unit "t/person"} {:name "methane", :type "decimal", :value 685.316, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.8691, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.019743, :unit "°C"} {:name "energy per capita", :type "decimal", :value 3521.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3725.4, :unit "TWh"} {:name "population", :type "integer", :value 1057922731, :unit "people"} {:name "gdp", :type "integer", :value 2804478499774, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-IND-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 1001.198, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.9288, :unit "t/person"} {:name "methane", :type "decimal", :value 689.962, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.8968, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.020249, :unit "°C"} {:name "energy per capita", :type "decimal", :value 3470.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3740.87, :unit "TWh"} {:name "population", :type "integer", :value 1077898579, :unit "people"} {:name "gdp", :type "integer", :value 2940745466088, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-IND-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 1032.75, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.9409, :unit "t/person"} {:name "methane", :type "decimal", :value 683.408, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.932, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.020758, :unit "°C"} {:name "energy per capita", :type "decimal", :value 3526.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 3870.81, :unit "TWh"} {:name "population", :type "integer", :value 1097600380, :unit "people"} {:name "gdp", :type "integer", :value 3024984520809, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-IND-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 1068.398, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.9567, :unit "t/person"} {:name "methane", :type "decimal", :value 695.315, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.8636, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.021289, :unit "°C"} {:name "energy per capita", :type "decimal", :value 3589.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4008.71, :unit "TWh"} {:name "population", :type "integer", :value 1116803000, :unit "people"} {:name "gdp", :type "integer", :value 3251114494190, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-IND-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 1134.611, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.9988, :unit "t/person"} {:name "methane", :type "decimal", :value 705.392, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.9658, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.021843, :unit "°C"} {:name "energy per capita", :type "decimal", :value 3831.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4352.65, :unit "TWh"} {:name "population", :type "integer", :value 1135991509, :unit "people"} {:name "gdp", :type "integer", :value 3495022816540, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-IND-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 1195.393, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.0353, :unit "t/person"} {:name "methane", :type "decimal", :value 721.894, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.0386, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.02242, :unit "°C"} {:name "energy per capita", :type "decimal", :value 3991.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4608.42, :unit "TWh"} {:name "population", :type "integer", :value 1154676321, :unit "people"} {:name "gdp", :type "integer", :value 3752627653422, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-IND-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 1293.321, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.1027, :unit "t/person"} {:name "methane", :type "decimal", :value 733.283, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.2273, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.023041, :unit "°C"} {:name "energy per capita", :type "decimal", :value 4139.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4854.87, :unit "TWh"} {:name "population", :type "integer", :value 1172878888, :unit "people"} {:name "gdp", :type "integer", :value 4069856052453, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-IND-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 1393.485, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.1703, :unit "t/person"} {:name "methane", :type "decimal", :value 749.827, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.4239, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.023723, :unit "°C"} {:name "energy per capita", :type "decimal", :value 4425.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5268.96, :unit "TWh"} {:name "population", :type "integer", :value 1190676028, :unit "people"} {:name "gdp", :type "integer", :value 4403215224414, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-IND-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 1490.366, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.2338, :unit "t/person"} {:name "methane", :type "decimal", :value 764.115, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.6502, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.024466, :unit "°C"} {:name "energy per capita", :type "decimal", :value 4616.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5576.53, :unit "TWh"} {:name "population", :type "integer", :value 1207930960, :unit "people"} {:name "gdp", :type "integer", :value 4649548164533, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-IND-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 1613.328, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.3164, :unit "t/person"} {:name "methane", :type "decimal", :value 762.322, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 5.1196, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.02525, :unit "°C"} {:name "energy per capita", :type "decimal", :value 4886.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5988.39, :unit "TWh"} {:name "population", :type "integer", :value 1225524764, :unit "people"} {:name "gdp", :type "integer", :value 4993478894185, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-IND-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 1678.53, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.3499, :unit "t/person"} {:name "methane", :type "decimal", :value 769.342, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 5.038, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.026077, :unit "°C"} {:name "energy per capita", :type "decimal", :value 5024.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6247.89, :unit "TWh"} {:name "population", :type "integer", :value 1243481565, :unit "people"} {:name "gdp", :type "integer", :value 5361371354007, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-IND-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 1765.695, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.4, :unit "t/person"} {:name "methane", :type "decimal", :value 781.037, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 5.1209, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.026941, :unit "°C"} {:name "energy per capita", :type "decimal", :value 5222.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6587.01, :unit "TWh"} {:name "population", :type "integer", :value 1261224956, :unit "people"} {:name "gdp", :type "integer", :value 5720692888000, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-IND-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 1926.986, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.507, :unit "t/person"} {:name "methane", :type "decimal", :value 783.911, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 5.5128, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.027898, :unit "°C"} {:name "energy per capita", :type "decimal", :value 5425.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6937.77, :unit "TWh"} {:name "population", :type "integer", :value 1278674501, :unit "people"} {:name "gdp", :type "integer", :value 6052427493607, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-IND-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 1995.337, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.5398, :unit "t/person"} {:name "methane", :type "decimal", :value 758.221, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 5.6564, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.028849, :unit "°C"} {:name "energy per capita", :type "decimal", :value 5527.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 7163.06, :unit "TWh"} {:name "population", :type "integer", :value 1295829507, :unit "people"} {:name "gdp", :type "integer", :value 6424470752490, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-IND-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 2148.052, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.6369, :unit "t/person"} {:name "methane", :type "decimal", :value 765.858, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 6.0567, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.029835, :unit "°C"} {:name "energy per capita", :type "decimal", :value 5831.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 7652.73, :unit "TWh"} {:name "population", :type "integer", :value 1312277184, :unit "people"} {:name "gdp", :type "integer", :value 6875186158349, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-IND-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 2231.817, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.6806, :unit "t/person"} {:name "methane", :type "decimal", :value 773.451, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 6.3039, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.03085, :unit "°C"} {:name "energy per capita", :type "decimal", :value 5943.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 7892.49, :unit "TWh"} {:name "population", :type "integer", :value 1328024494, :unit "people"} {:name "gdp", :type "integer", :value 7391521519812, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-IND-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 2352.54, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.7505, :unit "t/person"} {:name "methane", :type "decimal", :value 781.626, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 6.6469, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.031896, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6148.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 8262.86, :unit "TWh"} {:name "population", :type "integer", :value 1343944296, :unit "people"} {:name "gdp", :type "integer", :value 8057465593010, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-IND-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 2425.722, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.7841, :unit "t/person"} {:name "methane", :type "decimal", :value 786.601, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 6.7429, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.032959, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6304.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 8571.67, :unit "TWh"} {:name "population", :type "integer", :value 1359657398, :unit "people"} {:name "gdp", :type "integer", :value 8550029645172, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-IND-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 2595.227, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.8879, :unit "t/person"} {:name "methane", :type "decimal", :value 800.125, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 7.0649, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.034104, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6593.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 9063.36, :unit "TWh"} {:name "population", :type "integer", :value 1374659067, :unit "people"} {:name "gdp", :type "integer", :value 9173201683791, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-IND-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 2611.175, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.8799, :unit "t/person"} {:name "methane", :type "decimal", :value 800.179, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 7.0408, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.035243, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6682.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 9281.59, :unit "TWh"} {:name "population", :type "integer", :value 1389030307, :unit "people"} {:name "gdp", :type "integer", :value 9595463242427, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-IND-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 2422.732, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.7273, :unit "t/person"} {:name "methane", :type "decimal", :value 807.835, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 6.8909, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.036296, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6268.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 8792.22, :unit "TWh"} {:name "population", :type "integer", :value 1402617694, :unit "people"} {:name "gdp", :type "integer", :value 8945313249462, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-IND-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 2675.778, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.8921, :unit "t/person"} {:name "methane", :type "decimal", :value 821.053, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 7.2579, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.037461, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6749.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 9544.97, :unit "TWh"} {:name "population", :type "integer", :value 1414203896, :unit "people"} {:name "gdp", :type "integer", :value 9801621018003, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-IND-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 2831.132, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9862, :unit "t/person"} {:name "methane", :type "decimal", :value 835.674, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 7.5441, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.038693, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7057.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 10059.48, :unit "TWh"} {:name "population", :type "integer", :value 1425423209, :unit "people"} {:name "gdp", :type "integer", :value 10476248645535, :unit "2011 International$"}],
              :locations [{:lat 20.59, :lon 78.96}],
              :context-refs [{:global-id "country-India"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-IDN-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 281.054, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.3007, :unit "t/person"} {:name "methane", :type "decimal", :value 199.062, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1017, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.026531, :unit "°C"} {:name "energy per capita", :type "decimal", :value 5387.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1164.2, :unit "TWh"} {:name "population", :type "integer", :value 216077789, :unit "people"} {:name "gdp", :type "integer", :value 1138300071768, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-IDN-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 316.759, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.4457, :unit "t/person"} {:name "methane", :type "decimal", :value 201.118, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2329, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.026946, :unit "°C"} {:name "energy per capita", :type "decimal", :value 5690.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1246.69, :unit "TWh"} {:name "population", :type "integer", :value 219097905, :unit "people"} {:name "gdp", :type "integer", :value 1185220908414, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-IDN-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 308.201, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.3877, :unit "t/person"} {:name "methane", :type "decimal", :value 199.735, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1734, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.027502, :unit "°C"} {:name "energy per capita", :type "decimal", :value 5768.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1281.02, :unit "TWh"} {:name "population", :type "integer", :value 222088499, :unit "people"} {:name "gdp", :type "integer", :value 1244249942443, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-IDN-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 339.024, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.5065, :unit "t/person"} {:name "methane", :type "decimal", :value 201.292, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.226, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.027913, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6183.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1391.68, :unit "TWh"} {:name "population", :type "integer", :value 225048005, :unit "people"} {:name "gdp", :type "integer", :value 1309811268640, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-IDN-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 342.709, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.5036, :unit "t/person"} {:name "methane", :type "decimal", :value 205.566, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1979, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.028377, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6040.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1376.72, :unit "TWh"} {:name "population", :type "integer", :value 227926649, :unit "people"} {:name "gdp", :type "integer", :value 1387446383159, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-IDN-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 347.361, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.5046, :unit "t/person"} {:name "methane", :type "decimal", :value 209.288, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1736, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.028809, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6137.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1416.89, :unit "TWh"} {:name "population", :type "integer", :value 230871648, :unit "people"} {:name "gdp", :type "integer", :value 1474875329634, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-IDN-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 346.366, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.4805, :unit "t/person"} {:name "methane", :type "decimal", :value 221.061, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1321, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.029522, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6159.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1441.13, :unit "TWh"} {:name "population", :type "integer", :value 233951650, :unit "people"} {:name "gdp", :type "integer", :value 1565012923832, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-IDN-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 387.577, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.6349, :unit "t/person"} {:name "methane", :type "decimal", :value 224.719, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2304, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.030009, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6524.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1546.68, :unit "TWh"} {:name "population", :type "integer", :value 237062338, :unit "people"} {:name "gdp", :type "integer", :value 1674227359084, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-IDN-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 365.384, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.5214, :unit "t/person"} {:name "methane", :type "decimal", :value 229.503, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1401, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.030549, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6514.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1564.58, :unit "TWh"} {:name "population", :type "integer", :value 240157902, :unit "people"} {:name "gdp", :type "integer", :value 1809491142510, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-IDN-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 398.624, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.6389, :unit "t/person"} {:name "methane", :type "decimal", :value 242.077, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.265, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.031249, :unit "°C"} {:name "energy per capita", :type "decimal", :value 6608.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1607.27, :unit "TWh"} {:name "population", :type "integer", :value 243220024, :unit "people"} {:name "gdp", :type "integer", :value 1905717479305, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-IDN-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 445.37, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.8082, :unit "t/person"} {:name "methane", :type "decimal", :value 247.332, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3367, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.031855, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7062.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1739.49, :unit "TWh"} {:name "population", :type "integer", :value 246305327, :unit "people"} {:name "gdp", :type "integer", :value 2039334560589, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-IDN-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 500.458, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.0061, :unit "t/person"} {:name "methane", :type "decimal", :value 288.165, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4514, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.032523, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7366.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1837.59, :unit "TWh"} {:name "population", :type "integer", :value 249470029, :unit "people"} {:name "gdp", :type "integer", :value 2178184530109, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-IDN-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 515.77, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.041, :unit "t/person"} {:name "methane", :type "decimal", :value 300.722, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4755, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.033227, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7665.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1937.15, :unit "TWh"} {:name "population", :type "integer", :value 252698525, :unit "people"} {:name "gdp", :type "integer", :value 2309530044109, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-IDN-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 493.435, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9286, :unit "t/person"} {:name "methane", :type "decimal", :value 299.584, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3988, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.033956, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7131.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1824.51, :unit "TWh"} {:name "population", :type "integer", :value 255852464, :unit "people"} {:name "gdp", :type "integer", :value 2437876929100, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-IDN-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 498.029, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 1.9238, :unit "t/person"} {:name "methane", :type "decimal", :value 308.06, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4043, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.034755, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7171.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1856.63, :unit "TWh"} {:name "population", :type "integer", :value 258877395, :unit "people"} {:name "gdp", :type "integer", :value 2559933272409, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-IDN-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 551.16, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.1053, :unit "t/person"} {:name "methane", :type "decimal", :value 298.367, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5568, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.035772, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7244.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1896.46, :unit "TWh"} {:name "population", :type "integer", :value 261799249, :unit "people"} {:name "gdp", :type "integer", :value 2684763880628, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-IDN-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 547.969, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.0707, :unit "t/person"} {:name "methane", :type "decimal", :value 300.969, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5482, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.036398, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7120.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1884.23, :unit "TWh"} {:name "population", :type "integer", :value 264627429, :unit "people"} {:name "gdp", :type "integer", :value 2819889903036, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-IDN-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 570.928, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.1355, :unit "t/person"} {:name "methane", :type "decimal", :value 320.719, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.587, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.037, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7569.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2023.71, :unit "TWh"} {:name "population", :type "integer", :value 267346654, :unit "people"} {:name "gdp", :type "integer", :value 2962852274581, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-IDN-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 607.378, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.25, :unit "t/person"} {:name "methane", :type "decimal", :value 352.425, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.6534, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.037625, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7896.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2131.56, :unit "TWh"} {:name "population", :type "integer", :value 269951847, :unit "people"} {:name "gdp", :type "integer", :value 3116158974437, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-IDN-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 665.049, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.4406, :unit "t/person"} {:name "methane", :type "decimal", :value 365.884, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.7932, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.038381, :unit "°C"} {:name "energy per capita", :type "decimal", :value 8271.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2253.94, :unit "TWh"} {:name "population", :type "integer", :value 272489381, :unit "people"} {:name "gdp", :type "integer", :value 3272568062078, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-IDN-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 623.304, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.2681, :unit "t/person"} {:name "methane", :type "decimal", :value 351.989, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.7729, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.038967, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7700.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2116.31, :unit "TWh"} {:name "population", :type "integer", :value 274814861, :unit "people"} {:name "gdp", :type "integer", :value 3204972631957, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-IDN-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 632.946, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.287, :unit "t/person"} {:name "methane", :type "decimal", :value 351.41, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.7168, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.039571, :unit "°C"} {:name "energy per capita", :type "decimal", :value 7937.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2196.68, :unit "TWh"} {:name "population", :type "integer", :value 276758056, :unit "people"} {:name "gdp", :type "integer", :value 3323654666991, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-IDN-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 758.021, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 2.7186, :unit "t/person"} {:name "methane", :type "decimal", :value 386.071, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.0199, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.040181, :unit "°C"} {:name "energy per capita", :type "decimal", :value 9859.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2749.24, :unit "TWh"} {:name "population", :type "integer", :value 278830527, :unit "people"} {:name "gdp", :type "integer", :value 3500093638879, :unit "2011 International$"}],
              :locations [{:lat -0.79, :lon 113.92}],
              :context-refs [{:global-id "country-Indonesia"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-ITA-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 469.598, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.1994, :unit "t/person"} {:name "methane", :type "decimal", :value 61.303, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.8407, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007664, :unit "°C"} {:name "energy per capita", :type "decimal", :value 36668.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2100.08, :unit "TWh"} {:name "population", :type "integer", :value 57272200, :unit "people"} {:name "gdp", :type "integer", :value 1863751547560, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-ITA-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 469.679, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.196, :unit "t/person"} {:name "methane", :type "decimal", :value 58.016, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.828, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007865, :unit "°C"} {:name "energy per capita", :type "decimal", :value 36587.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2096.66, :unit "TWh"} {:name "population", :type "integer", :value 57305760, :unit "people"} {:name "gdp", :type "integer", :value 1910272015654, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-ITA-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 477.136, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.3095, :unit "t/person"} {:name "methane", :type "decimal", :value 59.063, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.8166, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008068, :unit "°C"} {:name "energy per capita", :type "decimal", :value 36598.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2101.49, :unit "TWh"} {:name "population", :type "integer", :value 57420636, :unit "people"} {:name "gdp", :type "integer", :value 1929301903714, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-ITA-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 495.094, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.5746, :unit "t/person"} {:name "methane", :type "decimal", :value 57.755, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.7904, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008281, :unit "°C"} {:name "energy per capita", :type "decimal", :value 37583.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2170.05, :unit "TWh"} {:name "population", :type "integer", :value 57739697, :unit "people"} {:name "gdp", :type "integer", :value 1947667703883, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-ITA-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 500.687, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.6063, :unit "t/person"} {:name "methane", :type "decimal", :value 55.592, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.75, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008495, :unit "°C"} {:name "energy per capita", :type "decimal", :value 38239.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2224.66, :unit "TWh"} {:name "population", :type "integer", :value 58176736, :unit "people"} {:name "gdp", :type "integer", :value 1994293720592, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-ITA-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 501.365, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.5675, :unit "t/person"} {:name "methane", :type "decimal", :value 55.428, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.6939, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008708, :unit "°C"} {:name "energy per capita", :type "decimal", :value 37986.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2222.93, :unit "TWh"} {:name "population", :type "integer", :value 58519696, :unit "people"} {:name "gdp", :type "integer", :value 2029243417217, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-ITA-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 496.075, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.4432, :unit "t/person"} {:name "methane", :type "decimal", :value 54.581, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.6215, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008919, :unit "°C"} {:name "energy per capita", :type "decimal", :value 37816.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2221.86, :unit "TWh"} {:name "population", :type "integer", :value 58754434, :unit "people"} {:name "gdp", :type "integer", :value 2086553648584, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-ITA-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 489.794, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.2863, :unit "t/person"} {:name "methane", :type "decimal", :value 54.576, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5549, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.009133, :unit "°C"} {:name "energy per capita", :type "decimal", :value 36801.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2175.28, :unit "TWh"} {:name "population", :type "integer", :value 59108723, :unit "people"} {:name "gdp", :type "integer", :value 2134371774096, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-ITA-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 478.132, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.0275, :unit "t/person"} {:name "methane", :type "decimal", :value 53.961, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4918, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.009339, :unit "°C"} {:name "energy per capita", :type "decimal", :value 36098.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2150.09, :unit "TWh"} {:name "population", :type "integer", :value 59561687, :unit "people"} {:name "gdp", :type "integer", :value 2129113318512, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-ITA-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 424.032, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.0785, :unit "t/person"} {:name "methane", :type "decimal", :value 53.466, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3456, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.009524, :unit "°C"} {:name "energy per capita", :type "decimal", :value 33300.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1994.81, :unit "TWh"} {:name "population", :type "integer", :value 59904016, :unit "people"} {:name "gdp", :type "integer", :value 2029023036275, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-ITA-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 435.672, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.2414, :unit "t/person"} {:name "methane", :type "decimal", :value 53.357, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3076, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.009711, :unit "°C"} {:name "energy per capita", :type "decimal", :value 34277.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2062.27, :unit "TWh"} {:name "population", :type "integer", :value 60164212, :unit "people"} {:name "gdp", :type "integer", :value 2080545936781, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-ITA-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 423.897, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.0219, :unit "t/person"} {:name "methane", :type "decimal", :value 50.008, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2294, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.009895, :unit "°C"} {:name "energy per capita", :type "decimal", :value 33468.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2020.39, :unit "TWh"} {:name "population", :type "integer", :value 60367882, :unit "people"} {:name "gdp", :type "integer", :value 2110883320816, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-ITA-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 403.444, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.665, :unit "t/person"} {:name "methane", :type "decimal", :value 50.658, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1542, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010065, :unit "°C"} {:name "energy per capita", :type "decimal", :value 32379.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1959.97, :unit "TWh"} {:name "population", :type "integer", :value 60531522, :unit "people"} {:name "gdp", :type "integer", :value 2047959835066, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-ITA-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 369.461, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.0913, :unit "t/person"} {:name "methane", :type "decimal", :value 49.341, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0473, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010221, :unit "°C"} {:name "energy per capita", :type "decimal", :value 30716.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1863.06, :unit "TWh"} {:name "population", :type "integer", :value 60653864, :unit "people"} {:name "gdp", :type "integer", :value 2010255414011, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-ITA-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 349.389, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.7594, :unit "t/person"} {:name "methane", :type "decimal", :value 47.452, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9851, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010368, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29940.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1816.32, :unit "TWh"} {:name "population", :type "integer", :value 60664169, :unit "people"} {:name "gdp", :type "integer", :value 2010164047423, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-ITA-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 361.244, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.9635, :unit "t/person"} {:name "methane", :type "decimal", :value 46.66, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0204, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010522, :unit "°C"} {:name "energy per capita", :type "decimal", :value 30350.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1838.47, :unit "TWh"} {:name "population", :type "integer", :value 60575314, :unit "people"} {:name "gdp", :type "integer", :value 2025809415322, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-ITA-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 358.133, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.9217, :unit "t/person"} {:name "methane", :type "decimal", :value 45.691, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0119, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010675, :unit "°C"} {:name "energy per capita", :type "decimal", :value 30333.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1834.54, :unit "TWh"} {:name "population", :type "integer", :value 60478103, :unit "people"} {:name "gdp", :type "integer", :value 2052012434345, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-ITA-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 352.666, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.8398, :unit "t/person"} {:name "methane", :type "decimal", :value 46.875, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9803, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010825, :unit "°C"} {:name "energy per capita", :type "decimal", :value 30594.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1847.63, :unit "TWh"} {:name "population", :type "integer", :value 60390317, :unit "people"} {:name "gdp", :type "integer", :value 2086237040647, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-ITA-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 349.048, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.7907, :unit "t/person"} {:name "methane", :type "decimal", :value 46.39, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9502, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010981, :unit "°C"} {:name "energy per capita", :type "decimal", :value 30853.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1859.78, :unit "TWh"} {:name "population", :type "integer", :value 60277496, :unit "people"} {:name "gdp", :type "integer", :value 2105551772855, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-ITA-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 339.638, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.6484, :unit "t/person"} {:name "methane", :type "decimal", :value 45.695, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9158, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.01113, :unit "°C"} {:name "energy per capita", :type "decimal", :value 30599.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1839.95, :unit "TWh"} {:name "population", :type "integer", :value 60130136, :unit "people"} {:name "gdp", :type "integer", :value 2115725732767, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-ITA-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 302.6, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.0507, :unit "t/person"} {:name "methane", :type "decimal", :value 44.653, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.8607, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011261, :unit "°C"} {:name "energy per capita", :type "decimal", :value 27764.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1663.42, :unit "TWh"} {:name "population", :type "integer", :value 59912763, :unit "people"} {:name "gdp", :type "integer", :value 1925751927111, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-ITA-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 335.93, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.6242, :unit "t/person"} {:name "methane", :type "decimal", :value 44.086, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9112, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011414, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29887.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1785.14, :unit "TWh"} {:name "population", :type "integer", :value 59729353, :unit "people"} {:name "gdp", :type "integer", :value 2060299944789, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-ITA-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 340.115, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.7048, :unit "t/person"} {:name "methane", :type "decimal", :value 42.624, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9063, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011567, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29444.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1755.46, :unit "TWh"} {:name "population", :type "integer", :value 59619106, :unit "people"} {:name "gdp", :type "integer", :value 2136009300257, :unit "2011 International$"}],
              :locations [{:lat 41.87, :lon 12.57}],
              :context-refs [{:global-id "country-Italy"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-JPN-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 1260.203, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.9207, :unit "t/person"} {:name "methane", :type "decimal", :value 42.953, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.9397, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.020475, :unit "°C"} {:name "energy per capita", :type "decimal", :value 49228.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6253.32, :unit "TWh"} {:name "population", :type "integer", :value 127027794, :unit "people"} {:name "gdp", :type "integer", :value 4209651870326, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-JPN-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 1245.935, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.7905, :unit "t/person"} {:name "methane", :type "decimal", :value 41.539, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.8493, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.021028, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48606.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6185.7, :unit "TWh"} {:name "population", :type "integer", :value 127260144, :unit "people"} {:name "gdp", :type "integer", :value 4224163277272, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-JPN-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 1275.42, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.0045, :unit "t/person"} {:name "methane", :type "decimal", :value 40.553, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.8559, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.021592, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48365.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6165.9, :unit "TWh"} {:name "population", :type "integer", :value 127484599, :unit "people"} {:name "gdp", :type "integer", :value 4226576076119, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-JPN-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 1283.845, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.0559, :unit "t/person"} {:name "methane", :type "decimal", :value 39.714, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.6427, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.02216, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48271.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6162.91, :unit "TWh"} {:name "population", :type "integer", :value 127671254, :unit "people"} {:name "gdp", :type "integer", :value 4288493482445, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-JPN-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 1279.122, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.0075, :unit "t/person"} {:name "methane", :type "decimal", :value 39.444, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.4709, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.022725, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48875.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6247.11, :unit "TWh"} {:name "population", :type "integer", :value 127815769, :unit "people"} {:name "gdp", :type "integer", :value 4380398118363, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-JPN-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 1286.412, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.0569, :unit "t/person"} {:name "methane", :type "decimal", :value 38.946, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.3461, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.023294, :unit "°C"} {:name "energy per capita", :type "decimal", :value 49673.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6353.94, :unit "TWh"} {:name "population", :type "integer", :value 127913331, :unit "people"} {:name "gdp", :type "integer", :value 4450600710060, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-JPN-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 1263.44, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.8708, :unit "t/person"} {:name "methane", :type "decimal", :value 38.588, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.1297, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.023853, :unit "°C"} {:name "energy per capita", :type "decimal", :value 49501.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6336.1, :unit "TWh"} {:name "population", :type "integer", :value 127997247, :unit "people"} {:name "gdp", :type "integer", :value 4514225078439, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-JPN-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 1298.974, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.1413, :unit "t/person"} {:name "methane", :type "decimal", :value 38.629, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 4.1238, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.024429, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48993.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6275.42, :unit "TWh"} {:name "population", :type "integer", :value 128087683, :unit "people"} {:name "gdp", :type "integer", :value 4590814009043, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-JPN-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 1228.687, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.5874, :unit "t/person"} {:name "methane", :type "decimal", :value 36.215, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.8337, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.024974, :unit "°C"} {:name "energy per capita", :type "decimal", :value 48051.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 6158.08, :unit "TWh"} {:name "population", :type "integer", :value 128155992, :unit "people"} {:name "gdp", :type "integer", :value 4542408020234, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-JPN-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 1160.253, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.0509, :unit "t/person"} {:name "methane", :type "decimal", :value 35.8, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.6818, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.025486, :unit "°C"} {:name "energy per capita", :type "decimal", :value 43858.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5622.33, :unit "TWh"} {:name "population", :type "integer", :value 128192471, :unit "people"} {:name "gdp", :type "integer", :value 4298103109270, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-JPN-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 1211.09, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.448, :unit "t/person"} {:name "methane", :type "decimal", :value 35.999, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.635, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.026021, :unit "°C"} {:name "energy per capita", :type "decimal", :value 46675.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5983.17, :unit "TWh"} {:name "population", :type "integer", :value 128185273, :unit "people"} {:name "gdp", :type "integer", :value 4480295644398, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-JPN-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 1261.181, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.8456, :unit "t/person"} {:name "methane", :type "decimal", :value 34.923, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.6577, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.026583, :unit "°C"} {:name "energy per capita", :type "decimal", :value 44260.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5669.59, :unit "TWh"} {:name "population", :type "integer", :value 128096430, :unit "people"} {:name "gdp", :type "integer", :value 4468738319171, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-JPN-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 1302.113, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.1785, :unit "t/person"} {:name "methane", :type "decimal", :value 34.064, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.7251, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.027164, :unit "°C"} {:name "energy per capita", :type "decimal", :value 43894.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5615.28, :unit "TWh"} {:name "population", :type "integer", :value 127928283, :unit "people"} {:name "gdp", :type "integer", :value 4530172048567, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-JPN-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 1311.876, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 10.2712, :unit "t/person"} {:name "methane", :type "decimal", :value 33.943, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.7189, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.027748, :unit "°C"} {:name "energy per capita", :type "decimal", :value 43658.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5576.22, :unit "TWh"} {:name "population", :type "integer", :value 127723642, :unit "people"} {:name "gdp", :type "integer", :value 4621006950566, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-JPN-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 1260.272, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.8855, :unit "t/person"} {:name "methane", :type "decimal", :value 32.739, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.5535, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.028309, :unit "°C"} {:name "energy per capita", :type "decimal", :value 42606.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5431.79, :unit "TWh"} {:name "population", :type "integer", :value 127486601, :unit "people"} {:name "gdp", :type "integer", :value 4634693421103, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-JPN-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 1219.982, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.5853, :unit "t/person"} {:name "methane", :type "decimal", :value 31.983, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.4459, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.028853, :unit "°C"} {:name "energy per capita", :type "decimal", :value 42138.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5363.26, :unit "TWh"} {:name "population", :type "integer", :value 127275868, :unit "people"} {:name "gdp", :type "integer", :value 4707025000790, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-JPN-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 1199.933, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.4364, :unit "t/person"} {:name "methane", :type "decimal", :value 31.677, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.3903, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.029388, :unit "°C"} {:name "energy per capita", :type "decimal", :value 41646.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5295.82, :unit "TWh"} {:name "population", :type "integer", :value 127159950, :unit "people"} {:name "gdp", :type "integer", :value 4742508721138, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-JPN-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 1184.392, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.3212, :unit "t/person"} {:name "methane", :type "decimal", :value 31.599, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.2923, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.029918, :unit "°C"} {:name "energy per capita", :type "decimal", :value 42137.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5354.2, :unit "TWh"} {:name "population", :type "integer", :value 127064686, :unit "people"} {:name "gdp", :type "integer", :value 4821960242931, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-JPN-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 1138.55, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.9711, :unit "t/person"} {:name "methane", :type "decimal", :value 30.936, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 3.0994, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.030429, :unit "°C"} {:name "energy per capita", :type "decimal", :value 41900.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5317.67, :unit "TWh"} {:name "population", :type "integer", :value 126913432, :unit "people"} {:name "gdp", :type "integer", :value 4852985093686, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-JPN-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 1102.063, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.6982, :unit "t/person"} {:name "methane", :type "decimal", :value 30.323, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.9716, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.030921, :unit "°C"} {:name "energy per capita", :type "decimal", :value 41086.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5205.6, :unit "TWh"} {:name "population", :type "integer", :value 126699421, :unit "people"} {:name "gdp", :type "integer", :value 4833467877373, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-JPN-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 1037.285, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.2126, :unit "t/person"} {:name "methane", :type "decimal", :value 29.722, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.9503, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.031386, :unit "°C"} {:name "energy per capita", :type "decimal", :value 38203.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4825.24, :unit "TWh"} {:name "population", :type "integer", :value 126304533, :unit "people"} {:name "gdp", :type "integer", :value 4626661481209, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-JPN-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 1058.502, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.4222, :unit "t/person"} {:name "methane", :type "decimal", :value 29.54, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.8711, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.031861, :unit "°C"} {:name "energy per capita", :type "decimal", :value 40020.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 5029.71, :unit "TWh"} {:name "population", :type "integer", :value 125679342, :unit "people"} {:name "gdp", :type "integer", :value 4725883595048, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-JPN-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 1029.645, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.2373, :unit "t/person"} {:name "methane", :type "decimal", :value 29.664, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.7437, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.032325, :unit "°C"} {:name "energy per capita", :type "decimal", :value 39974.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 4996.77, :unit "TWh"} {:name "population", :type "integer", :value 124997585, :unit "people"} {:name "gdp", :type "integer", :value 4774494846664, :unit "2011 International$"}],
              :locations [{:lat 36.2, :lon 138.25}],
              :context-refs [{:global-id "country-Japan"} {:global-id "continent-Asia"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-KEN-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 10.385, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.3389, :unit "t/person"} {:name "methane", :type "decimal", :value 24.248, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0407, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000806, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1350.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 41.39, :unit "TWh"} {:name "population", :type "integer", :value 30642893, :unit "people"} {:name "gdp", :type "integer", :value 59408907766, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-KEN-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 9.224, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.2917, :unit "t/person"} {:name "methane", :type "decimal", :value 24.417, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0359, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000816, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1260.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 39.86, :unit "TWh"} {:name "population", :type "integer", :value 31619170, :unit "people"} {:name "gdp", :type "integer", :value 62956470966, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-KEN-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 7.819, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.2396, :unit "t/person"} {:name "methane", :type "decimal", :value 25.418, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0298, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000825, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1247.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 40.69, :unit "TWh"} {:name "population", :type "integer", :value 32629809, :unit "people"} {:name "gdp", :type "integer", :value 64478127960, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-KEN-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 6.716, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.1996, :unit "t/person"} {:name "methane", :type "decimal", :value 26.822, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0243, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000835, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1111.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 37.41, :unit "TWh"} {:name "population", :type "integer", :value 33652233, :unit "people"} {:name "gdp", :type "integer", :value 67617075406, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-KEN-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 7.621, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.2196, :unit "t/person"} {:name "methane", :type "decimal", :value 28.377, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0266, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000844, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1188.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 41.26, :unit "TWh"} {:name "population", :type "integer", :value 34713453, :unit "people"} {:name "gdp", :type "integer", :value 72070033430, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-KEN-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 8.454, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.2362, :unit "t/person"} {:name "methane", :type "decimal", :value 27.952, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0286, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000855, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1259.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 45.09, :unit "TWh"} {:name "population", :type "integer", :value 35796485, :unit "people"} {:name "gdp", :type "integer", :value 77598831261, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-KEN-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 9.567, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.2592, :unit "t/person"} {:name "methane", :type "decimal", :value 27.789, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0313, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000868, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1350.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 49.83, :unit "TWh"} {:name "population", :type "integer", :value 36904010, :unit "people"} {:name "gdp", :type "integer", :value 83653248054, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-KEN-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 9.734, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.2559, :unit "t/person"} {:name "methane", :type "decimal", :value 39.619, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0309, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000881, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1309.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 49.79, :unit "TWh"} {:name "population", :type "integer", :value 38036354, :unit "people"} {:name "gdp", :type "integer", :value 91060343688, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-KEN-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 10.099, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.2576, :unit "t/person"} {:name "methane", :type "decimal", :value 40.322, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0315, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000896, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1288.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 50.52, :unit "TWh"} {:name "population", :type "integer", :value 39206349, :unit "people"} {:name "gdp", :type "integer", :value 92971180385, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-KEN-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 12.185, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.3015, :unit "t/person"} {:name "methane", :type "decimal", :value 39.141, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0387, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000911, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1398.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 56.5, :unit "TWh"} {:name "population", :type "integer", :value 40408887, :unit "people"} {:name "gdp", :type "integer", :value 97792492598, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-KEN-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 11.857, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.285, :unit "t/person"} {:name "methane", :type "decimal", :value 39.565, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0356, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000932, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1447.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 60.2, :unit "TWh"} {:name "population", :type "integer", :value 41598570, :unit "people"} {:name "gdp", :type "integer", :value 108082167772, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-KEN-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 12.218, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.2857, :unit "t/person"} {:name "methane", :type "decimal", :value 40.61, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0354, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000952, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1465.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 62.65, :unit "TWh"} {:name "population", :type "integer", :value 42758467, :unit "people"} {:name "gdp", :type "integer", :value 117152107312, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-KEN-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 11.246, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.2562, :unit "t/person"} {:name "methane", :type "decimal", :value 40.598, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0322, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00097, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1354.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 59.44, :unit "TWh"} {:name "population", :type "integer", :value 43888306, :unit "people"} {:name "gdp", :type "integer", :value 122504792826, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-KEN-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 12.163, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.2704, :unit "t/person"} {:name "methane", :type "decimal", :value 40.338, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0345, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000984, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1423.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 64.05, :unit "TWh"} {:name "population", :type "integer", :value 44986794, :unit "people"} {:name "gdp", :type "integer", :value 127157517259, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-KEN-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 13.214, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.2869, :unit "t/person"} {:name "methane", :type "decimal", :value 41.061, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0373, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.000997, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1470.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 67.7, :unit "TWh"} {:name "population", :type "integer", :value 46051441, :unit "people"} {:name "gdp", :type "integer", :value 133540839466, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-KEN-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 15.212, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.3231, :unit "t/person"} {:name "methane", :type "decimal", :value 41.821, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.043, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001012, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1671.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 78.72, :unit "TWh"} {:name "population", :type "integer", :value 47088528, :unit "people"} {:name "gdp", :type "integer", :value 140175144054, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-KEN-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 16.289, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.3384, :unit "t/person"} {:name "methane", :type "decimal", :value 44.76, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.046, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001023, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1743.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 83.92, :unit "TWh"} {:name "population", :type "integer", :value 48139683, :unit "people"} {:name "gdp", :type "integer", :value 146082118752, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-KEN-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 16.181, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.3289, :unit "t/person"} {:name "methane", :type "decimal", :value 42.684, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.045, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001041, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1727.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 84.99, :unit "TWh"} {:name "population", :type "integer", :value 49197757, :unit "people"} {:name "gdp", :type "integer", :value 151656608610, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-KEN-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 17.003, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.3387, :unit "t/person"} {:name "methane", :type "decimal", :value 44.186, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0463, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001056, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1644.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 82.57, :unit "TWh"} {:name "population", :type "integer", :value 50207109, :unit "people"} {:name "gdp", :type "integer", :value 160257055688, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-KEN-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 18.159, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.3546, :unit "t/person"} {:name "methane", :type "decimal", :value 49.255, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.049, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001069, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1678.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 85.95, :unit "TWh"} {:name "population", :type "integer", :value 51202829, :unit "people"} {:name "gdp", :type "integer", :value 168452589868, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-KEN-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 19.99, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.3828, :unit "t/person"} {:name "methane", :type "decimal", :value 54.435, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0569, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001082, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1565.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 81.73, :unit "TWh"} {:name "population", :type "integer", :value 52217327, :unit "people"} {:name "gdp", :type "integer", :value 168031463891, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-KEN-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 20.59, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.3869, :unit "t/person"} {:name "methane", :type "decimal", :value 50.344, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0558, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001093, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1634.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 87.01, :unit "TWh"} {:name "population", :type "integer", :value 53219167, :unit "people"} {:name "gdp", :type "integer", :value 180662393951, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-KEN-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 20.544, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.3787, :unit "t/person"} {:name "methane", :type "decimal", :value 51.79, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0547, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001105, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1609.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 87.34, :unit "TWh"} {:name "population", :type "integer", :value 54252458, :unit "people"} {:name "gdp", :type "integer", :value 190362153076, :unit "2011 International$"}],
              :locations [{:lat -0.02, :lon 37.91}],
              :context-refs [{:global-id "country-Kenya"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-MEX-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 391.435, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.9689, :unit "t/person"} {:name "methane", :type "decimal", :value 126.288, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5343, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00937, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17198.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1696.18, :unit "TWh"} {:name "population", :type "integer", :value 98625553, :unit "people"} {:name "gdp", :type "integer", :value 1241194674661, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-MEX-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 407.485, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.0708, :unit "t/person"} {:name "methane", :type "decimal", :value 124.883, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.586, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00959, :unit "°C"} {:name "energy per capita", :type "decimal", :value 16786.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1680.27, :unit "TWh"} {:name "population", :type "integer", :value 100099101, :unit "people"} {:name "gdp", :type "integer", :value 1251502726420, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-MEX-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 410.301, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.0404, :unit "t/person"} {:name "methane", :type "decimal", :value 125.145, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5621, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00981, :unit "°C"} {:name "energy per capita", :type "decimal", :value 16948.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1721.1, :unit "TWh"} {:name "population", :type "integer", :value 101548627, :unit "people"} {:name "gdp", :type "integer", :value 1275458026881, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-MEX-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 435.969, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.2336, :unit "t/person"} {:name "methane", :type "decimal", :value 128.239, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5766, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010039, :unit "°C"} {:name "energy per capita", :type "decimal", :value 16915.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1741.97, :unit "TWh"} {:name "population", :type "integer", :value 102978517, :unit "people"} {:name "gdp", :type "integer", :value 1306677417369, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-MEX-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 438.588, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.2013, :unit "t/person"} {:name "methane", :type "decimal", :value 127.414, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.533, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010267, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17662.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1843.9, :unit "TWh"} {:name "population", :type "integer", :value 104394131, :unit "people"} {:name "gdp", :type "integer", :value 1373060298988, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-MEX-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 463.436, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.3798, :unit "t/person"} {:name "methane", :type "decimal", :value 129.146, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5657, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010503, :unit "°C"} {:name "energy per capita", :type "decimal", :value 19092.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2020.16, :unit "TWh"} {:name "population", :type "integer", :value 105811504, :unit "people"} {:name "gdp", :type "integer", :value 1427140428046, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-MEX-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 474.523, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.4243, :unit "t/person"} {:name "methane", :type "decimal", :value 132.226, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.551, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010746, :unit "°C"} {:name "energy per capita", :type "decimal", :value 19108.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2049.44, :unit "TWh"} {:name "population", :type "integer", :value 107253666, :unit "people"} {:name "gdp", :type "integer", :value 1510732401129, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-MEX-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 473.595, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.3539, :unit "t/person"} {:name "methane", :type "decimal", :value 134.533, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5035, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010982, :unit "°C"} {:name "energy per capita", :type "decimal", :value 19017.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2068.58, :unit "TWh"} {:name "population", :type "integer", :value 108774356, :unit "people"} {:name "gdp", :type "integer", :value 1572479287017, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-MEX-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 472.981, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.2852, :unit "t/person"} {:name "methane", :type "decimal", :value 138.096, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4758, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011215, :unit "°C"} {:name "energy per capita", :type "decimal", :value 19512.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2153.63, :unit "TWh"} {:name "population", :type "integer", :value 110374286, :unit "people"} {:name "gdp", :type "integer", :value 1607479004533, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-MEX-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 460.29, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.1097, :unit "t/person"} {:name "methane", :type "decimal", :value 136.687, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4606, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.01144, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18904.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2117.24, :unit "TWh"} {:name "population", :type "integer", :value 111999720, :unit "people"} {:name "gdp", :type "integer", :value 1543820957956, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-MEX-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 456.409, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.0168, :unit "t/person"} {:name "methane", :type "decimal", :value 136.679, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3699, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011663, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18706.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2125.45, :unit "TWh"} {:name "population", :type "integer", :value 113623898, :unit "people"} {:name "gdp", :type "integer", :value 1638271507559, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-MEX-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 483.302, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.1937, :unit "t/person"} {:name "methane", :type "decimal", :value 139.773, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4017, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011896, :unit "°C"} {:name "energy per capita", :type "decimal", :value 19279.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2221.89, :unit "TWh"} {:name "population", :type "integer", :value 115243503, :unit "people"} {:name "gdp", :type "integer", :value 1716088331953, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-MEX-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 501.492, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.2929, :unit "t/person"} {:name "methane", :type "decimal", :value 137.234, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4347, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012136, :unit "°C"} {:name "energy per capita", :type "decimal", :value 19191.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2241.89, :unit "TWh"} {:name "population", :type "integer", :value 116818208, :unit "people"} {:name "gdp", :type "integer", :value 1774999946047, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-MEX-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 495.412, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.1862, :unit "t/person"} {:name "methane", :type "decimal", :value 138.822, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4044, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012373, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18952.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2242.86, :unit "TWh"} {:name "population", :type "integer", :value 118343572, :unit "people"} {:name "gdp", :type "integer", :value 1803059285337, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-MEX-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 484.045, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.041, :unit "t/person"} {:name "methane", :type "decimal", :value 139.134, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3648, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012606, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18621.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2230.57, :unit "TWh"} {:name "population", :type "integer", :value 119784265, :unit "people"} {:name "gdp", :type "integer", :value 1854551091374, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-MEX-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 479.434, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.9599, :unit "t/person"} {:name "methane", :type "decimal", :value 140.263, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3542, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012833, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18215.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2205.42, :unit "TWh"} {:name "population", :type "integer", :value 121072310, :unit "people"} {:name "gdp", :type "integer", :value 1916008739051, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-MEX-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 478.828, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.9167, :unit "t/person"} {:name "methane", :type "decimal", :value 141.776, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3529, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013044, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18618.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2276.18, :unit "TWh"} {:name "population", :type "integer", :value 122251344, :unit "people"} {:name "gdp", :type "integer", :value 1961906742138, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-MEX-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 464.407, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.7634, :unit "t/person"} {:name "methane", :type "decimal", :value 142.684, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2909, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013251, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18879.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2329.71, :unit "TWh"} {:name "population", :type "integer", :value 123400052, :unit "people"} {:name "gdp", :type "integer", :value 2007719438156, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-MEX-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 468.421, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.7602, :unit "t/person"} {:name "methane", :type "decimal", :value 143.368, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2752, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013455, :unit "°C"} {:name "energy per capita", :type "decimal", :value 18337.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2284.34, :unit "TWh"} {:name "population", :type "integer", :value 124573713, :unit "people"} {:name "gdp", :type "integer", :value 2051539127317, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-MEX-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 468.236, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.7232, :unit "t/person"} {:name "methane", :type "decimal", :value 144.505, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2625, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013686, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17971.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2260.17, :unit "TWh"} {:name "population", :type "integer", :value 125762978, :unit "people"} {:name "gdp", :type "integer", :value 2047701179708, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-MEX-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 428.937, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.3828, :unit "t/person"} {:name "methane", :type "decimal", :value 146.363, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.22, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013887, :unit "°C"} {:name "energy per capita", :type "decimal", :value 16329.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2070.56, :unit "TWh"} {:name "population", :type "integer", :value 126799053, :unit "people"} {:name "gdp", :type "integer", :value 1880176557054, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-MEX-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 446.838, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.5005, :unit "t/person"} {:name "methane", :type "decimal", :value 146.594, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.212, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.014087, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17223.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2198.55, :unit "TWh"} {:name "population", :type "integer", :value 127648151, :unit "people"} {:name "gdp", :type "integer", :value 1973141022977, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-MEX-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 453.799, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.5284, :unit "t/person"} {:name "methane", :type "decimal", :value 148.504, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2092, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.014286, :unit "°C"} {:name "energy per capita", :type "decimal", :value 17858.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2296.86, :unit "TWh"} {:name "population", :type "integer", :value 128613116, :unit "people"} {:name "gdp", :type "integer", :value 2033437602787, :unit "2011 International$"}],
              :locations [{:lat 23.63, :lon -102.55}],
              :context-refs [{:global-id "country-Mexico"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-NGA-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 96.831, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.7662, :unit "t/person"} {:name "methane", :type "decimal", :value 286.267, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3796, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.004923, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1704.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 215.37, :unit "TWh"} {:name "population", :type "integer", :value 126382490, :unit "people"} {:name "gdp", :type "integer", :value 268133056679, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-NGA-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 100.218, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.7717, :unit "t/person"} {:name "methane", :type "decimal", :value 285.049, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3901, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005023, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1880.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 244.26, :unit "TWh"} {:name "population", :type "integer", :value 129862594, :unit "people"} {:name "gdp", :type "integer", :value 293371700731, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-NGA-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 89.906, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.6736, :unit "t/person"} {:name "methane", :type "decimal", :value 242.577, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3423, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005109, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1880.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 250.95, :unit "TWh"} {:name "population", :type "integer", :value 133471995, :unit "people"} {:name "gdp", :type "integer", :value 344836483316, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-NGA-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 100.03, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.7291, :unit "t/person"} {:name "methane", :type "decimal", :value 267.581, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3617, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005198, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1947.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 267.13, :unit "TWh"} {:name "population", :type "integer", :value 137202646, :unit "people"} {:name "gdp", :type "integer", :value 387284228400, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-NGA-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 94.962, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.6732, :unit "t/person"} {:name "methane", :type "decimal", :value 261.971, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3319, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005284, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1961.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 276.61, :unit "TWh"} {:name "population", :type "integer", :value 141057045, :unit "people"} {:name "gdp", :type "integer", :value 438814816180, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-NGA-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 101.666, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.7011, :unit "t/person"} {:name "methane", :type "decimal", :value 255.255, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3435, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005365, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2102.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 304.85, :unit "TWh"} {:name "population", :type "integer", :value 145017256, :unit "people"} {:name "gdp", :type "integer", :value 481797619872, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-NGA-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 90.097, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.6044, :unit "t/person"} {:name "methane", :type "decimal", :value 238.028, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.2945, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00544, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1838.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 274.02, :unit "TWh"} {:name "population", :type "integer", :value 149077335, :unit "people"} {:name "gdp", :type "integer", :value 527631752471, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-NGA-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 82.17, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.5361, :unit "t/person"} {:name "methane", :type "decimal", :value 224.094, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.2609, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005508, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1643.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 251.89, :unit "TWh"} {:name "population", :type "integer", :value 153267254, :unit "people"} {:name "gdp", :type "integer", :value 581001445796, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-NGA-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 86.489, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.5488, :unit "t/person"} {:name "methane", :type "decimal", :value 219.135, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.2699, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005591, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1969.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 310.31, :unit "TWh"} {:name "population", :type "integer", :value 157595014, :unit "people"} {:name "gdp", :type "integer", :value 638908028616, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-NGA-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 77.031, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.4754, :unit "t/person"} {:name "methane", :type "decimal", :value 212.871, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.2444, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005634, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1211.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 196.34, :unit "TWh"} {:name "population", :type "integer", :value 162049466, :unit "people"} {:name "gdp", :type "integer", :value 709931188516, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-NGA-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 112.05, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.6724, :unit "t/person"} {:name "methane", :type "decimal", :value 222.123, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3363, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005686, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1445.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 240.85, :unit "TWh"} {:name "population", :type "integer", :value 166642888, :unit "people"} {:name "gdp", :type "integer", :value 809999411809, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-NGA-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 126.115, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.7359, :unit "t/person"} {:name "methane", :type "decimal", :value 233.312, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3658, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005737, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2022.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 346.69, :unit "TWh"} {:name "population", :type "integer", :value 171379602, :unit "people"} {:name "gdp", :type "integer", :value 871103799000, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-NGA-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 110.125, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.625, :unit "t/person"} {:name "methane", :type "decimal", :value 238.341, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.315, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005777, :unit "°C"} {:name "energy per capita", :type "decimal", :value 1987.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 350.17, :unit "TWh"} {:name "population", :type "integer", :value 176200627, :unit "people"} {:name "gdp", :type "integer", :value 908380661120, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-NGA-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 117.401, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.6484, :unit "t/person"} {:name "methane", :type "decimal", :value 228.363, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3328, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00582, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2503.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 453.33, :unit "TWh"} {:name "population", :type "integer", :value 181049440, :unit "people"} {:name "gdp", :type "integer", :value 957382666012, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-NGA-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 123.971, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.6669, :unit "t/person"} {:name "methane", :type "decimal", :value 231.686, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3496, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005867, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2658.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 494.19, :unit "TWh"} {:name "population", :type "integer", :value 185896917, :unit "people"} {:name "gdp", :type "integer", :value 1017790765613, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-NGA-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 110.525, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.5797, :unit "t/person"} {:name "methane", :type "decimal", :value 230.338, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3122, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005911, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2458.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 468.84, :unit "TWh"} {:name "population", :type "integer", :value 190671883, :unit "people"} {:name "gdp", :type "integer", :value 1044789628210, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-NGA-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 116.396, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.5955, :unit "t/person"} {:name "methane", :type "decimal", :value 221.453, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3289, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005958, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2421.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 473.3, :unit "TWh"} {:name "population", :type "integer", :value 195443698, :unit "people"} {:name "gdp", :type "integer", :value 1027896680223, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-NGA-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 112.54, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.562, :unit "t/person"} {:name "methane", :type "decimal", :value 225.834, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3128, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006009, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2284.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 457.56, :unit "TWh"} {:name "population", :type "integer", :value 200254580, :unit "people"} {:name "gdp", :type "integer", :value 1036180414988, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-NGA-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 106.786, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.5211, :unit "t/person"} {:name "methane", :type "decimal", :value 229.374, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.2907, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00606, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2383.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 488.56, :unit "TWh"} {:name "population", :type "integer", :value 204938752, :unit "people"} {:name "gdp", :type "integer", :value 1056103594567, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-NGA-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 127.859, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.6103, :unit "t/person"} {:name "methane", :type "decimal", :value 237.142, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3448, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00612, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2446.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 512.45, :unit "TWh"} {:name "population", :type "integer", :value 209485637, :unit "people"} {:name "gdp", :type "integer", :value 1079426893318, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-NGA-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 124.899, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.5837, :unit "t/person"} {:name "methane", :type "decimal", :value 216.406, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3552, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006183, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2256.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 482.8, :unit "TWh"} {:name "population", :type "integer", :value 213996185, :unit "people"} {:name "gdp", :type "integer", :value 1060059201469, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-NGA-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 147.777, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.6762, :unit "t/person"} {:name "methane", :type "decimal", :value 210.478, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.4008, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006252, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2388.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 521.99, :unit "TWh"} {:name "population", :type "integer", :value 218529286, :unit "people"} {:name "gdp", :type "integer", :value 1098721596994, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-NGA-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 131.441, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 0.589, :unit "t/person"} {:name "methane", :type "decimal", :value 202.906, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.3502, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006315, :unit "°C"} {:name "energy per capita", :type "decimal", :value 2363.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 527.35, :unit "TWh"} {:name "population", :type "integer", :value 223150906, :unit "people"} {:name "gdp", :type "integer", :value 1134448580250, :unit "2011 International$"}],
              :locations [{:lat 9.08, :lon 8.68}],
              :context-refs [{:global-id "country-Nigeria"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-NOR-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 42.131, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.3815, :unit "t/person"} {:name "methane", :type "decimal", :value 8.508, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1651, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00108, :unit "°C"} {:name "energy per capita", :type "decimal", :value 129858.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 583.18, :unit "TWh"} {:name "population", :type "integer", :value 4490864, :unit "people"} {:name "gdp", :type "integer", :value 242751277829, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-NOR-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 43.519, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.6416, :unit "t/person"} {:name "methane", :type "decimal", :value 9.249, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1694, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.0011, :unit "°C"} {:name "energy per capita", :type "decimal", :value 115019.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 519.16, :unit "TWh"} {:name "population", :type "integer", :value 4513650, :unit "people"} {:name "gdp", :type "integer", :value 255455331302, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-NOR-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 42.571, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.381, :unit "t/person"} {:name "methane", :type "decimal", :value 9.62, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1621, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00112, :unit "°C"} {:name "energy per capita", :type "decimal", :value 119752.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 543.44, :unit "TWh"} {:name "population", :type "integer", :value 4538016, :unit "people"} {:name "gdp", :type "integer", :value 267081351259, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-NOR-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 43.919, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.6215, :unit "t/person"} {:name "methane", :type "decimal", :value 10.366, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1588, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00114, :unit "°C"} {:name "energy per capita", :type "decimal", :value 105140.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 479.93, :unit "TWh"} {:name "population", :type "integer", :value 4564666, :unit "people"} {:name "gdp", :type "integer", :value 277856282106, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-NOR-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 44.257, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.6384, :unit "t/person"} {:name "methane", :type "decimal", :value 10.451, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1547, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001161, :unit "°C"} {:name "energy per capita", :type "decimal", :value 105951.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 486.5, :unit "TWh"} {:name "population", :type "integer", :value 4591739, :unit "people"} {:name "gdp", :type "integer", :value 297743119876, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-NOR-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 43.279, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.3614, :unit "t/person"} {:name "methane", :type "decimal", :value 9.661, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1462, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001181, :unit "°C"} {:name "energy per capita", :type "decimal", :value 120792.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 558.44, :unit "TWh"} {:name "population", :type "integer", :value 4623120, :unit "people"} {:name "gdp", :type "integer", :value 314940774381, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-NOR-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 43.859, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.4108, :unit "t/person"} {:name "methane", :type "decimal", :value 10.241, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1434, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001202, :unit "°C"} {:name "energy per capita", :type "decimal", :value 109687.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 511.2, :unit "TWh"} {:name "population", :type "integer", :value 4660487, :unit "people"} {:name "gdp", :type "integer", :value 332457531096, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-NOR-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 45.605, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.6846, :unit "t/person"} {:name "methane", :type "decimal", :value 11.076, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1448, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001223, :unit "°C"} {:name "energy per capita", :type "decimal", :value 117374.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 552.72, :unit "TWh"} {:name "population", :type "integer", :value 4709017, :unit "people"} {:name "gdp", :type "integer", :value 352692911227, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-NOR-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 44.692, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.3731, :unit "t/person"} {:name "methane", :type "decimal", :value 10.868, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1394, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001245, :unit "°C"} {:name "energy per capita", :type "decimal", :value 117427.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 559.91, :unit "TWh"} {:name "population", :type "integer", :value 4768097, :unit "people"} {:name "gdp", :type "integer", :value 364943654963, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-NOR-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 43.098, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.9255, :unit "t/person"} {:name "methane", :type "decimal", :value 10.372, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1368, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001265, :unit "°C"} {:name "energy per capita", :type "decimal", :value 106436.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 513.94, :unit "TWh"} {:name "population", :type "integer", :value 4828625, :unit "people"} {:name "gdp", :type "integer", :value 370113292846, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-NOR-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 45.659, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.3388, :unit "t/person"} {:name "methane", :type "decimal", :value 10.092, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.137, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001287, :unit "°C"} {:name "energy per capita", :type "decimal", :value 101844.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 497.93, :unit "TWh"} {:name "population", :type "integer", :value 4889160, :unit "people"} {:name "gdp", :type "integer", :value 383760934305, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-NOR-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 44.775, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.04, :unit "t/person"} {:name "methane", :type "decimal", :value 9.638, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1299, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001309, :unit "°C"} {:name "energy per capita", :type "decimal", :value 101764.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 504.04, :unit "TWh"} {:name "population", :type "integer", :value 4952969, :unit "people"} {:name "gdp", :type "integer", :value 399420041611, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-NOR-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 44.258, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.8191, :unit "t/person"} {:name "methane", :type "decimal", :value 8.336, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1266, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00133, :unit "°C"} {:name "energy per capita", :type "decimal", :value 111775.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 560.94, :unit "TWh"} {:name "population", :type "integer", :value 5018453, :unit "people"} {:name "gdp", :type "integer", :value 410276121127, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-NOR-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 44.539, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.7673, :unit "t/person"} {:name "methane", :type "decimal", :value 8.41, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1263, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001352, :unit "°C"} {:name "energy per capita", :type "decimal", :value 102996.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 523.24, :unit "TWh"} {:name "population", :type "integer", :value 5080126, :unit "people"} {:name "gdp", :type "integer", :value 414445317738, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-NOR-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 44.975, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.7544, :unit "t/person"} {:name "methane", :type "decimal", :value 8.3, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1268, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001374, :unit "°C"} {:name "energy per capita", :type "decimal", :value 105489.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 541.94, :unit "TWh"} {:name "population", :type "integer", :value 5137397, :unit "people"} {:name "gdp", :type "integer", :value 422933730082, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-NOR-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 45.523, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.7717, :unit "t/person"} {:name "methane", :type "decimal", :value 8.408, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1286, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001396, :unit "°C"} {:name "energy per capita", :type "decimal", :value 105836.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 549.27, :unit "TWh"} {:name "population", :type "integer", :value 5189777, :unit "people"} {:name "gdp", :type "integer", :value 430788988142, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-NOR-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 44.708, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.5386, :unit "t/person"} {:name "methane", :type "decimal", :value 8.323, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1263, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001418, :unit "°C"} {:name "energy per capita", :type "decimal", :value 106500.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 557.63, :unit "TWh"} {:name "population", :type "integer", :value 5235974, :unit "people"} {:name "gdp", :type "integer", :value 435806553454, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-NOR-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 44.197, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.3757, :unit "t/person"} {:name "methane", :type "decimal", :value 8.233, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1229, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001441, :unit "°C"} {:name "energy per capita", :type "decimal", :value 106154.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 560.16, :unit "TWh"} {:name "population", :type "integer", :value 5276807, :unit "people"} {:name "gdp", :type "integer", :value 446543702505, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-NOR-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 44.472, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.3724, :unit "t/person"} {:name "methane", :type "decimal", :value 8.349, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1211, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001463, :unit "°C"} {:name "energy per capita", :type "decimal", :value 103850.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 551.63, :unit "TWh"} {:name "population", :type "integer", :value 5311751, :unit "people"} {:name "gdp", :type "integer", :value 450245217440, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-NOR-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 42.866, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.0157, :unit "t/person"} {:name "methane", :type "decimal", :value 7.916, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1156, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001485, :unit "°C"} {:name "energy per capita", :type "decimal", :value 96479.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 515.94, :unit "TWh"} {:name "population", :type "integer", :value 5347733, :unit "people"} {:name "gdp", :type "integer", :value 455304831150, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-NOR-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 41.275, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.673, :unit "t/person"} {:name "methane", :type "decimal", :value 8.042, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1174, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001506, :unit "°C"} {:name "energy per capita", :type "decimal", :value 103430.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 556.38, :unit "TWh"} {:name "population", :type "integer", :value 5379275, :unit "people"} {:name "gdp", :type "integer", :value 449485260525, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-NOR-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 41.058, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.592, :unit "t/person"} {:name "methane", :type "decimal", :value 8.205, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1114, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001527, :unit "°C"} {:name "energy per capita", :type "decimal", :value 105249.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 569.2, :unit "TWh"} {:name "population", :type "integer", :value 5408082, :unit "people"} {:name "gdp", :type "integer", :value 467007972731, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-NOR-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 40.833, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.483, :unit "t/person"} {:name "methane", :type "decimal", :value 8.234, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1088, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.001548, :unit "°C"} {:name "energy per capita", :type "decimal", :value 97780.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 533.57, :unit "TWh"} {:name "population", :type "integer", :value 5456796, :unit "people"} {:name "gdp", :type "integer", :value 482328451592, :unit "2011 International$"}],
              :locations [{:lat 60.47, :lon 8.47}],
              :context-refs [{:global-id "country-Norway"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-POL-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 317.306, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.2938, :unit "t/person"} {:name "methane", :type "decimal", :value 74.009, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2438, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010725, :unit "°C"} {:name "energy per capita", :type "decimal", :value 26596.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1017.52, :unit "TWh"} {:name "population", :type "integer", :value 38258077, :unit "people"} {:name "gdp", :type "integer", :value 488207682001, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-POL-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 313.267, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.1906, :unit "t/person"} {:name "methane", :type "decimal", :value 71.262, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2193, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010859, :unit "°C"} {:name "energy per capita", :type "decimal", :value 26419.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1010.46, :unit "TWh"} {:name "population", :type "integer", :value 38247359, :unit "people"} {:name "gdp", :type "integer", :value 499086644170, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-POL-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 305.852, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.0004, :unit "t/person"} {:name "methane", :type "decimal", :value 69.222, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1645, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010987, :unit "°C"} {:name "energy per capita", :type "decimal", :value 26146.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 999.58, :unit "TWh"} {:name "population", :type "integer", :value 38229407, :unit "people"} {:name "gdp", :type "integer", :value 514089670231, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-POL-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 318.733, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.343, :unit "t/person"} {:name "methane", :type "decimal", :value 65.67, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1526, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011124, :unit "°C"} {:name "energy per capita", :type "decimal", :value 27071.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1034.21, :unit "TWh"} {:name "population", :type "integer", :value 38203527, :unit "people"} {:name "gdp", :type "integer", :value 537330337680, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-POL-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 323.621, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.4789, :unit "t/person"} {:name "methane", :type "decimal", :value 63.702, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1311, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011259, :unit "°C"} {:name "energy per capita", :type "decimal", :value 27280.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1041.22, :unit "TWh"} {:name "population", :type "integer", :value 38167568, :unit "people"} {:name "gdp", :type "integer", :value 570430122148, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-POL-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 322.577, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.4612, :unit "t/person"} {:name "methane", :type "decimal", :value 63.355, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0898, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011398, :unit "°C"} {:name "energy per capita", :type "decimal", :value 27854.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1061.94, :unit "TWh"} {:name "population", :type "integer", :value 38124377, :unit "people"} {:name "gdp", :type "integer", :value 595958527688, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-POL-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 336.387, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.8357, :unit "t/person"} {:name "methane", :type "decimal", :value 63.082, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0995, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011538, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29165.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1110.36, :unit "TWh"} {:name "population", :type "integer", :value 38071169, :unit "people"} {:name "gdp", :type "integer", :value 638713864539, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-POL-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 335.735, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.8304, :unit "t/person"} {:name "methane", :type "decimal", :value 60.359, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0658, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011683, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29054.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1104.64, :unit "TWh"} {:name "population", :type "integer", :value 38020420, :unit "people"} {:name "gdp", :type "integer", :value 690249985288, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-POL-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 329.428, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.6701, :unit "t/person"} {:name "methane", :type "decimal", :value 58.02, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0279, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011823, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29593.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1124.43, :unit "TWh"} {:name "population", :type "integer", :value 37995902, :unit "people"} {:name "gdp", :type "integer", :value 726319216200, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-POL-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 315.886, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.3123, :unit "t/person"} {:name "methane", :type "decimal", :value 54.028, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0024, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011951, :unit "°C"} {:name "energy per capita", :type "decimal", :value 28556.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1085.19, :unit "TWh"} {:name "population", :type "integer", :value 38002153, :unit "people"} {:name "gdp", :type "integer", :value 760579624624, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-POL-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 334.028, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.7682, :unit "t/person"} {:name "methane", :type "decimal", :value 54.204, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0026, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012095, :unit "°C"} {:name "energy per capita", :type "decimal", :value 30377.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1157.25, :unit "TWh"} {:name "population", :type "integer", :value 38095356, :unit "people"} {:name "gdp", :type "integer", :value 795619859853, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-POL-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 333.406, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.7259, :unit "t/person"} {:name "methane", :type "decimal", :value 53.162, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.967, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012234, :unit "°C"} {:name "energy per capita", :type "decimal", :value 30429.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1162.67, :unit "TWh"} {:name "population", :type "integer", :value 38208644, :unit "people"} {:name "gdp", :type "integer", :value 843236863910, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-POL-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 325.778, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.5175, :unit "t/person"} {:name "methane", :type "decimal", :value 54.782, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.932, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012369, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29489.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1127.93, :unit "TWh"} {:name "population", :type "integer", :value 38247867, :unit "people"} {:name "gdp", :type "integer", :value 856266755052, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-POL-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 321.757, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.4099, :unit "t/person"} {:name "methane", :type "decimal", :value 53.301, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9121, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012504, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29569.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1131.32, :unit "TWh"} {:name "population", :type "integer", :value 38259267, :unit "people"} {:name "gdp", :type "integer", :value 863601156195, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-POL-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 309.415, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.0861, :unit "t/person"} {:name "methane", :type "decimal", :value 49.907, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.8724, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012633, :unit "°C"} {:name "energy per capita", :type "decimal", :value 28468.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1089.35, :unit "TWh"} {:name "population", :type "integer", :value 38265113, :unit "people"} {:name "gdp", :type "integer", :value 896737189458, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-POL-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 312.549, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.1677, :unit "t/person"} {:name "methane", :type "decimal", :value 49.081, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.8828, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012762, :unit "°C"} {:name "energy per capita", :type "decimal", :value 28824.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1103.01, :unit "TWh"} {:name "population", :type "integer", :value 38266314, :unit "people"} {:name "gdp", :type "integer", :value 936043804062, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-POL-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 323.3, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.4494, :unit "t/person"} {:name "methane", :type "decimal", :value 48.563, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9135, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.012898, :unit "°C"} {:name "energy per capita", :type "decimal", :value 30061.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1150.25, :unit "TWh"} {:name "population", :type "integer", :value 38263260, :unit "people"} {:name "gdp", :type "integer", :value 963689941627, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-POL-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 336.505, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.7964, :unit "t/person"} {:name "methane", :type "decimal", :value 48.124, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9354, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.01304, :unit "°C"} {:name "energy per capita", :type "decimal", :value 31585.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1208.29, :unit "TWh"} {:name "population", :type "integer", :value 38254964, :unit "people"} {:name "gdp", :type "integer", :value 1013223736654, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-POL-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 335.719, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.779, :unit "t/person"} {:name "methane", :type "decimal", :value 48.997, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9139, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013186, :unit "°C"} {:name "energy per capita", :type "decimal", :value 31957.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1222.08, :unit "TWh"} {:name "population", :type "integer", :value 38241070, :unit "people"} {:name "gdp", :type "integer", :value 1073462064151, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-POL-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 317.444, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.3044, :unit "t/person"} {:name "methane", :type "decimal", :value 48.285, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.856, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013327, :unit "°C"} {:name "energy per capita", :type "decimal", :value 31059.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1187.27, :unit "TWh"} {:name "population", :type "integer", :value 38225884, :unit "people"} {:name "gdp", :type "integer", :value 1121231497071, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-POL-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 302.102, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.9144, :unit "t/person"} {:name "methane", :type "decimal", :value 46.439, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.8593, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013457, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29845.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1139.23, :unit "TWh"} {:name "population", :type "integer", :value 38171005, :unit "people"} {:name "gdp", :type "integer", :value 1098581305284, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-POL-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 330.694, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.6932, :unit "t/person"} {:name "methane", :type "decimal", :value 47.204, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.897, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.0136, :unit "°C"} {:name "energy per capita", :type "decimal", :value 32196.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1224.76, :unit "TWh"} {:name "population", :type "integer", :value 38040303, :unit "people"} {:name "gdp", :type "integer", :value 1173809548862, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-POL-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 314.892, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.2034, :unit "t/person"} {:name "methane", :type "decimal", :value 46.473, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.8391, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.013736, :unit "°C"} {:name "energy per capita", :type "decimal", :value 30919.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1186.87, :unit "TWh"} {:name "population", :type "integer", :value 38385734, :unit "people"} {:name "gdp", :type "integer", :value 1231021338216, :unit "2011 International$"}],
              :locations [{:lat 51.92, :lon 19.15}],
              :context-refs [{:global-id "country-Poland"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 378.333, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.0224, :unit "t/person"} {:name "methane", :type "decimal", :value 77.98, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.483, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007151, :unit "°C"} {:name "energy per capita", :type "decimal", :value 25135.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1185.38, :unit "TWh"} {:name "population", :type "integer", :value 47159715, :unit "people"} {:name "gdp", :type "integer", :value 356072104365, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 371.602, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.8122, :unit "t/person"} {:name "methane", :type "decimal", :value 77.52, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4463, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007331, :unit "°C"} {:name "energy per capita", :type "decimal", :value 25049.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1191.54, :unit "TWh"} {:name "population", :type "integer", :value 47566805, :unit "people"} {:name "gdp", :type "integer", :value 374889022813, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 356.501, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.4241, :unit "t/person"} {:name "methane", :type "decimal", :value 77.387, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3573, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007501, :unit "°C"} {:name "energy per capita", :type "decimal", :value 24180.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1161.12, :unit "TWh"} {:name "population", :type "integer", :value 48019414, :unit "people"} {:name "gdp", :type "integer", :value 396137571619, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 404.406, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.3382, :unit "t/person"} {:name "methane", :type "decimal", :value 79.97, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4624, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007691, :unit "°C"} {:name "energy per capita", :type "decimal", :value 25937.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1257.96, :unit "TWh"} {:name "population", :type "integer", :value 48500351, :unit "people"} {:name "gdp", :type "integer", :value 413710071358, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 449.322, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.1714, :unit "t/person"} {:name "methane", :type "decimal", :value 80.587, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5705, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.0079, :unit "°C"} {:name "energy per capita", :type "decimal", :value 28020.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1372.77, :unit "TWh"} {:name "population", :type "integer", :value 48991420, :unit "people"} {:name "gdp", :type "integer", :value 439265862379, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 416.205, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.4099, :unit "t/person"} {:name "methane", :type "decimal", :value 81.855, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4061, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008092, :unit "°C"} {:name "energy per capita", :type "decimal", :value 26254.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1299.32, :unit "TWh"} {:name "population", :type "integer", :value 49490041, :unit "people"} {:name "gdp", :type "integer", :value 470188845104, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 446.744, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.9352, :unit "t/person"} {:name "methane", :type "decimal", :value 81.391, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4602, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008299, :unit "°C"} {:name "energy per capita", :type "decimal", :value 26522.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1326.06, :unit "TWh"} {:name "population", :type "integer", :value 49998271, :unit "people"} {:name "gdp", :type "integer", :value 505574497707, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 465.034, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.2034, :unit "t/person"} {:name "methane", :type "decimal", :value 82.957, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4763, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008518, :unit "°C"} {:name "energy per capita", :type "decimal", :value 26958.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1362.19, :unit "TWh"} {:name "population", :type "integer", :value 50528575, :unit "people"} {:name "gdp", :type "integer", :value 542976204451, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 494.673, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.6777, :unit "t/person"} {:name "methane", :type "decimal", :value 85.087, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5435, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008746, :unit "°C"} {:name "energy per capita", :type "decimal", :value 28557.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1459.7, :unit "TWh"} {:name "population", :type "integer", :value 51114595, :unit "people"} {:name "gdp", :type "integer", :value 571660802378, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 474.512, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.1731, :unit "t/person"} {:name "methane", :type "decimal", :value 84.236, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5058, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.008964, :unit "°C"} {:name "energy per capita", :type "decimal", :value 28093.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1453.21, :unit "TWh"} {:name "population", :type "integer", :value 51728518, :unit "people"} {:name "gdp", :type "integer", :value 574768021657, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 467.353, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.9285, :unit "t/person"} {:name "methane", :type "decimal", :value 84.606, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.4027, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.009175, :unit "°C"} {:name "energy per capita", :type "decimal", :value 27968.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1463.99, :unit "TWh"} {:name "population", :type "integer", :value 52344047, :unit "people"} {:name "gdp", :type "integer", :value 607601612432, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 474.273, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.9494, :unit "t/person"} {:name "methane", :type "decimal", :value 84.478, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3755, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00939, :unit "°C"} {:name "energy per capita", :type "decimal", :value 27289.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1446.21, :unit "TWh"} {:name "population", :type "integer", :value 52995212, :unit "people"} {:name "gdp", :type "integer", :value 645388107070, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 461.438, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.5797, :unit "t/person"} {:name "methane", :type "decimal", :value 87.855, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3201, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.009598, :unit "°C"} {:name "energy per capita", :type "decimal", :value 26478.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1424.1, :unit "TWh"} {:name "population", :type "integer", :value 53782570, :unit "people"} {:name "gdp", :type "integer", :value 660853021773, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 456.188, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.3431, :unit "t/person"} {:name "methane", :type "decimal", :value 87.75, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2932, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.009804, :unit "°C"} {:name "energy per capita", :type "decimal", :value 26163.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1430.57, :unit "TWh"} {:name "population", :type "integer", :value 54678789, :unit "people"} {:name "gdp", :type "integer", :value 677278303675, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 481.638, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.6634, :unit "t/person"} {:name "methane", :type "decimal", :value 88.512, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.358, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010021, :unit "°C"} {:name "energy per capita", :type "decimal", :value 26046.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1448.05, :unit "TWh"} {:name "population", :type "integer", :value 55594840, :unit "people"} {:name "gdp", :type "integer", :value 686853872664, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 457.473, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.065, :unit "t/person"} {:name "methane", :type "decimal", :value 87.344, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2922, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010227, :unit "°C"} {:name "energy per capita", :type "decimal", :value 24952.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1415.42, :unit "TWh"} {:name "population", :type "integer", :value 56723536, :unit "people"} {:name "gdp", :type "integer", :value 695933197143, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 457.05, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.9821, :unit "t/person"} {:name "methane", :type "decimal", :value 85.799, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2914, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010433, :unit "°C"} {:name "energy per capita", :type "decimal", :value 25848.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1480.06, :unit "TWh"} {:name "population", :type "integer", :value 57259550, :unit "people"} {:name "gdp", :type "integer", :value 700558003750, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 440.01, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.6344, :unit "t/person"} {:name "methane", :type "decimal", :value 87.121, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2231, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010631, :unit "°C"} {:name "energy per capita", :type "decimal", :value 25594.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1475.14, :unit "TWh"} {:name "population", :type "integer", :value 57635158, :unit "people"} {:name "gdp", :type "integer", :value 708670169977, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 453.766, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.7417, :unit "t/person"} {:name "methane", :type "decimal", :value 86.077, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2353, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.010836, :unit "°C"} {:name "energy per capita", :type "decimal", :value 24053.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1409.86, :unit "TWh"} {:name "population", :type "integer", :value 58613001, :unit "people"} {:name "gdp", :type "integer", :value 719458334697, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 470.727, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.8997, :unit "t/person"} {:name "methane", :type "decimal", :value 85.415, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2693, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011048, :unit "°C"} {:name "energy per capita", :type "decimal", :value 24698.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1471.71, :unit "TWh"} {:name "population", :type "integer", :value 59587880, :unit "people"} {:name "gdp", :type "integer", :value 721641639539, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 435.298, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.1876, :unit "t/person"} {:name "methane", :type "decimal", :value 83.513, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2381, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011246, :unit "°C"} {:name "energy per capita", :type "decimal", :value 23110.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1399.6, :unit "TWh"} {:name "population", :type "integer", :value 60562374, :unit "people"} {:name "gdp", :type "integer", :value 675871755463, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 439.464, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.1455, :unit "t/person"} {:name "methane", :type "decimal", :value 81.623, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.192, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.011445, :unit "°C"} {:name "energy per capita", :type "decimal", :value 22855.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1405.65, :unit "TWh"} {:name "population", :type "integer", :value 61502602, :unit "people"} {:name "gdp", :type "integer", :value 709077881289, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-ZAF-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 428.782, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.8739, :unit "t/person"} {:name "methane", :type "decimal", :value 81.235, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1426, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.01164, :unit "°C"} {:name "energy per capita", :type "decimal", :value 21580.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1346.17, :unit "TWh"} {:name "population", :type "integer", :value 62378417, :unit "people"} {:name "gdp", :type "integer", :value 723559323969, :unit "2011 International$"}],
              :locations [{:lat -30.56, :lon 22.94}],
              :context-refs [{:global-id "country-South Africa"} {:global-id "continent-Africa"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-ESP-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 309.26, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.5393, :unit "t/person"} {:name "methane", :type "decimal", :value 45.728, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2122, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.004611, :unit "°C"} {:name "energy per capita", :type "decimal", :value 37185.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1525.35, :unit "TWh"} {:name "population", :type "integer", :value 41019772, :unit "people"} {:name "gdp", :type "integer", :value 1094998702299, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-ESP-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 311.031, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.541, :unit "t/person"} {:name "methane", :type "decimal", :value 46.678, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2106, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.004744, :unit "°C"} {:name "energy per capita", :type "decimal", :value 38478.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1587.08, :unit "TWh"} {:name "population", :type "integer", :value 41245602, :unit "people"} {:name "gdp", :type "integer", :value 1147959133197, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-ESP-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 331.028, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.9277, :unit "t/person"} {:name "methane", :type "decimal", :value 47.206, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2603, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.004889, :unit "°C"} {:name "energy per capita", :type "decimal", :value 38668.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1614.63, :unit "TWh"} {:name "population", :type "integer", :value 41755703, :unit "people"} {:name "gdp", :type "integer", :value 1191323484900, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-ESP-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 335.539, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.8938, :unit "t/person"} {:name "methane", :type "decimal", :value 47.092, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2134, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005038, :unit "°C"} {:name "energy per capita", :type "decimal", :value 39871.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1694.78, :unit "TWh"} {:name "population", :type "integer", :value 42506540, :unit "people"} {:name "gdp", :type "integer", :value 1239651963472, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-ESP-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 352.139, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.1449, :unit "t/person"} {:name "methane", :type "decimal", :value 47.019, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2308, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005191, :unit "°C"} {:name "energy per capita", :type "decimal", :value 40348.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1744.46, :unit "TWh"} {:name "population", :type "integer", :value 43234562, :unit "people"} {:name "gdp", :type "integer", :value 1289831715789, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-ESP-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 367.447, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.3589, :unit "t/person"} {:name "methane", :type "decimal", :value 46.895, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2414, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00535, :unit "°C"} {:name "energy per capita", :type "decimal", :value 40272.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1770.33, :unit "TWh"} {:name "population", :type "integer", :value 43958883, :unit "people"} {:name "gdp", :type "integer", :value 1348839731490, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-ESP-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 358.869, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.0292, :unit "t/person"} {:name "methane", :type "decimal", :value 46.748, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.173, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005504, :unit "°C"} {:name "energy per capita", :type "decimal", :value 39893.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1783.07, :unit "TWh"} {:name "population", :type "integer", :value 44695446, :unit "people"} {:name "gdp", :type "integer", :value 1415738502534, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-ESP-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 366.469, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.0513, :unit "t/person"} {:name "methane", :type "decimal", :value 47.691, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1634, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005662, :unit "°C"} {:name "energy per capita", :type "decimal", :value 40170.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1828.41, :unit "TWh"} {:name "population", :type "integer", :value 45516501, :unit "people"} {:name "gdp", :type "integer", :value 1481143208580, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-ESP-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 335.284, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.2517, :unit "t/person"} {:name "methane", :type "decimal", :value 46.432, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0461, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005805, :unit "°C"} {:name "energy per capita", :type "decimal", :value 38382.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1774.59, :unit "TWh"} {:name "population", :type "integer", :value 46235055, :unit "people"} {:name "gdp", :type "integer", :value 1510615290401, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-ESP-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 296.159, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.3506, :unit "t/person"} {:name "methane", :type "decimal", :value 49.206, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9398, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.005931, :unit "°C"} {:name "energy per capita", :type "decimal", :value 35426.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1652.1, :unit "TWh"} {:name "population", :type "integer", :value 46635182, :unit "people"} {:name "gdp", :type "integer", :value 1468765814163, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-ESP-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 282.585, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.0329, :unit "t/person"} {:name "methane", :type "decimal", :value 47.536, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.8482, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006049, :unit "°C"} {:name "energy per capita", :type "decimal", :value 36130.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1692.35, :unit "TWh"} {:name "population", :type "integer", :value 46840469, :unit "people"} {:name "gdp", :type "integer", :value 1480376411582, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-ESP-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 283.275, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.0274, :unit "t/person"} {:name "methane", :type "decimal", :value 46.791, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.8216, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006178, :unit "°C"} {:name "energy per capita", :type "decimal", :value 35349.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1661.36, :unit "TWh"} {:name "population", :type "integer", :value 46998040, :unit "people"} {:name "gdp", :type "integer", :value 1477187548437, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-ESP-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 277.432, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.9005, :unit "t/person"} {:name "methane", :type "decimal", :value 45.211, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.7937, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006296, :unit "°C"} {:name "energy per capita", :type "decimal", :value 35039.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1647.51, :unit "TWh"} {:name "population", :type "integer", :value 47018323, :unit "people"} {:name "gdp", :type "integer", :value 1433478645419, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-ESP-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 252.109, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.38, :unit "t/person"} {:name "methane", :type "decimal", :value 43.305, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.7147, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006404, :unit "°C"} {:name "energy per capita", :type "decimal", :value 33685.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1578.53, :unit "TWh"} {:name "population", :type "integer", :value 46860394, :unit "people"} {:name "gdp", :type "integer", :value 1413362053623, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-ESP-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 253.658, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.4293, :unit "t/person"} {:name "methane", :type "decimal", :value 44.21, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.7152, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006512, :unit "°C"} {:name "energy per capita", :type "decimal", :value 33266.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1554.22, :unit "TWh"} {:name "population", :type "integer", :value 46720187, :unit "people"} {:name "gdp", :type "integer", :value 1433089409797, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-ESP-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 270.248, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.7889, :unit "t/person"} {:name "methane", :type "decimal", :value 45.507, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.7633, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006625, :unit "°C"} {:name "energy per capita", :type "decimal", :value 33697.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1573.14, :unit "TWh"} {:name "population", :type "integer", :value 46683683, :unit "people"} {:name "gdp", :type "integer", :value 1488098863146, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-ESP-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 259.555, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.554, :unit "t/person"} {:name "methane", :type "decimal", :value 45.157, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.7334, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006735, :unit "°C"} {:name "energy per capita", :type "decimal", :value 33871.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1582.91, :unit "TWh"} {:name "population", :type "integer", :value 46732768, :unit "people"} {:name "gdp", :type "integer", :value 1533303930046, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-ESP-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 273.484, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.8351, :unit "t/person"} {:name "methane", :type "decimal", :value 45.153, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.7602, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006849, :unit "°C"} {:name "energy per capita", :type "decimal", :value 34181.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1602.04, :unit "TWh"} {:name "population", :type "integer", :value 46868594, :unit "people"} {:name "gdp", :type "integer", :value 1578931492849, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-ESP-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 268.521, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.7019, :unit "t/person"} {:name "methane", :type "decimal", :value 46.051, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.731, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.006963, :unit "°C"} {:name "energy per capita", :type "decimal", :value 34584.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1628.67, :unit "TWh"} {:name "population", :type "integer", :value 47092817, :unit "people"} {:name "gdp", :type "integer", :value 1615001473949, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-ESP-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 250.674, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.2846, :unit "t/person"} {:name "methane", :type "decimal", :value 45.547, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6759, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007068, :unit "°C"} {:name "energy per capita", :type "decimal", :value 33390.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1583.87, :unit "TWh"} {:name "population", :type "integer", :value 47435121, :unit "people"} {:name "gdp", :type "integer", :value 1647042667976, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-ESP-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 211.765, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.4414, :unit "t/person"} {:name "methane", :type "decimal", :value 46.022, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6023, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007156, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29844.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1422.96, :unit "TWh"} {:name "population", :type "integer", :value 47679482, :unit "people"} {:name "gdp", :type "integer", :value 1460507935923, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-ESP-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 228.806, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.7932, :unit "t/person"} {:name "methane", :type "decimal", :value 45.944, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6206, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.007258, :unit "°C"} {:name "energy per capita", :type "decimal", :value 32091.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1531.9, :unit "TWh"} {:name "population", :type "integer", :value 47735670, :unit "people"} {:name "gdp", :type "integer", :value 1541121952313, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-ESP-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 234.223, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.8972, :unit "t/person"} {:name "methane", :type "decimal", :value 44.986, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.6241, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00736, :unit "°C"} {:name "energy per capita", :type "decimal", :value 33289.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1592.17, :unit "TWh"} {:name "population", :type "integer", :value 47828386, :unit "people"} {:name "gdp", :type "integer", :value 1625139888517, :unit "2011 International$"}],
              :locations [{:lat 40.46, :lon -3.75}],
              :context-refs [{:global-id "country-Spain"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-SWE-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 54.892, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.1871, :unit "t/person"} {:name "methane", :type "decimal", :value 10.953, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.2152, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002873, :unit "°C"} {:name "energy per capita", :type "decimal", :value 73464.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 651.78, :unit "TWh"} {:name "population", :type "integer", :value 8872099, :unit "people"} {:name "gdp", :type "integer", :value 303452362920, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-SWE-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 55.782, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.2705, :unit "t/person"} {:name "methane", :type "decimal", :value 10.946, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.2171, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002898, :unit "°C"} {:name "energy per capita", :type "decimal", :value 78209.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 695.76, :unit "TWh"} {:name "population", :type "integer", :value 8896018, :unit "people"} {:name "gdp", :type "integer", :value 308398096624, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-SWE-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 56.634, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.3455, :unit "t/person"} {:name "methane", :type "decimal", :value 11.161, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.2156, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002923, :unit "°C"} {:name "energy per capita", :type "decimal", :value 72769.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 649.47, :unit "TWh"} {:name "population", :type "integer", :value 8925045, :unit "people"} {:name "gdp", :type "integer", :value 317463771012, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-SWE-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 57.221, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.3874, :unit "t/person"} {:name "methane", :type "decimal", :value 11.217, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.2069, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002949, :unit "°C"} {:name "energy per capita", :type "decimal", :value 68007.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 609.24, :unit "TWh"} {:name "population", :type "integer", :value 8958434, :unit "people"} {:name "gdp", :type "integer", :value 326402407102, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-SWE-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 56.428, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.274, :unit "t/person"} {:name "methane", :type "decimal", :value 11.538, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1972, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.002976, :unit "°C"} {:name "energy per capita", :type "decimal", :value 73894.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 664.6, :unit "TWh"} {:name "population", :type "integer", :value 8993805, :unit "people"} {:name "gdp", :type "integer", :value 341901244849, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-SWE-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 53.787, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.9567, :unit "t/person"} {:name "methane", :type "decimal", :value 9.793, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1817, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003004, :unit "°C"} {:name "energy per capita", :type "decimal", :value 74532.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 673.01, :unit "TWh"} {:name "population", :type "integer", :value 9029771, :unit "people"} {:name "gdp", :type "integer", :value 354496929423, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-SWE-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 53.651, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.9083, :unit "t/person"} {:name "methane", :type "decimal", :value 9.902, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1754, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00303, :unit "°C"} {:name "energy per capita", :type "decimal", :value 69394.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 630.14, :unit "TWh"} {:name "population", :type "integer", :value 9080628, :unit "people"} {:name "gdp", :type "integer", :value 372234670461, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-SWE-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 52.879, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.7802, :unit "t/person"} {:name "methane", :type "decimal", :value 9.603, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1679, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003057, :unit "°C"} {:name "energy per capita", :type "decimal", :value 70079.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 641.11, :unit "TWh"} {:name "population", :type "integer", :value 9148280, :unit "people"} {:name "gdp", :type "integer", :value 387881844813, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-SWE-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 50.765, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.5061, :unit "t/person"} {:name "methane", :type "decimal", :value 9.334, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1584, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003082, :unit "°C"} {:name "energy per capita", :type "decimal", :value 69259.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 638.57, :unit "TWh"} {:name "population", :type "integer", :value 9219903, :unit "people"} {:name "gdp", :type "integer", :value 388980516356, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-SWE-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 47.143, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.0699, :unit "t/person"} {:name "methane", :type "decimal", :value 9.263, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1496, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003106, :unit "°C"} {:name "energy per capita", :type "decimal", :value 62353.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 579.81, :unit "TWh"} {:name "population", :type "integer", :value 9298738, :unit "people"} {:name "gdp", :type "integer", :value 373026350439, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-SWE-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 53.029, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.6544, :unit "t/person"} {:name "methane", :type "decimal", :value 8.888, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1592, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003134, :unit "°C"} {:name "energy per capita", :type "decimal", :value 66295.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 621.74, :unit "TWh"} {:name "population", :type "integer", :value 9378234, :unit "people"} {:name "gdp", :type "integer", :value 399838964466, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-SWE-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 49.114, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.1976, :unit "t/person"} {:name "methane", :type "decimal", :value 8.887, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1424, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003158, :unit "°C"} {:name "energy per capita", :type "decimal", :value 64768.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 612.02, :unit "TWh"} {:name "population", :type "integer", :value 9449263, :unit "people"} {:name "gdp", :type "integer", :value 397617949429, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-SWE-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 46.607, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.896, :unit "t/person"} {:name "methane", :type "decimal", :value 8.814, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1333, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003182, :unit "°C"} {:name "energy per capita", :type "decimal", :value 67781.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 645.24, :unit "TWh"} {:name "population", :type "integer", :value 9519477, :unit "people"} {:name "gdp", :type "integer", :value 395278763366, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-SWE-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 45.106, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.6982, :unit "t/person"} {:name "methane", :type "decimal", :value 8.523, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1279, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003204, :unit "°C"} {:name "energy per capita", :type "decimal", :value 62865.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 603.54, :unit "TWh"} {:name "population", :type "integer", :value 9600620, :unit "people"} {:name "gdp", :type "integer", :value 399973749048, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-SWE-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 43.385, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.4743, :unit "t/person"} {:name "methane", :type "decimal", :value 8.22, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1223, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003226, :unit "°C"} {:name "energy per capita", :type "decimal", :value 61958.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 600.78, :unit "TWh"} {:name "population", :type "integer", :value 9696438, :unit "people"} {:name "gdp", :type "integer", :value 410604225974, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-SWE-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 43.31, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.4196, :unit "t/person"} {:name "methane", :type "decimal", :value 8.035, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1223, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003247, :unit "°C"} {:name "energy per capita", :type "decimal", :value 63479.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 622.06, :unit "TWh"} {:name "population", :type "integer", :value 9799483, :unit "people"} {:name "gdp", :type "integer", :value 429037422635, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-SWE-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 43.308, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.3643, :unit "t/person"} {:name "methane", :type "decimal", :value 8.028, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1224, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.00327, :unit "°C"} {:name "energy per capita", :type "decimal", :value 61411.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 609.4, :unit "TWh"} {:name "population", :type "integer", :value 9923281, :unit "people"} {:name "gdp", :type "integer", :value 437921057919, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-SWE-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 42.355, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.2112, :unit "t/person"} {:name "methane", :type "decimal", :value 8.096, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1177, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003292, :unit "°C"} {:name "energy per capita", :type "decimal", :value 62771.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 631.34, :unit "TWh"} {:name "population", :type "integer", :value 10057864, :unit "people"} {:name "gdp", :type "integer", :value 449166531542, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-SWE-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 41.856, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.1135, :unit "t/person"} {:name "methane", :type "decimal", :value 8.039, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1139, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003313, :unit "°C"} {:name "energy per capita", :type "decimal", :value 59869.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 609.19, :unit "TWh"} {:name "population", :type "integer", :value 10175400, :unit "people"} {:name "gdp", :type "integer", :value 457925387676, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-SWE-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 40.772, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.9665, :unit "t/person"} {:name "methane", :type "decimal", :value 7.938, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1099, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003335, :unit "°C"} {:name "energy per capita", :type "decimal", :value 60882.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 625.82, :unit "TWh"} {:name "population", :type "integer", :value 10279120, :unit "people"} {:name "gdp", :type "integer", :value 467020665386, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-SWE-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 36.622, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.5371, :unit "t/person"} {:name "methane", :type "decimal", :value 7.964, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1042, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003354, :unit "°C"} {:name "energy per capita", :type "decimal", :value 57977.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 600.28, :unit "TWh"} {:name "population", :type "integer", :value 10353682, :unit "people"} {:name "gdp", :type "integer", :value 456885314424, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-SWE-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 38.568, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.7027, :unit "t/person"} {:name "methane", :type "decimal", :value 7.942, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.1046, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003374, :unit "°C"} {:name "energy per capita", :type "decimal", :value 60836.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 633.68, :unit "TWh"} {:name "population", :type "integer", :value 10416134, :unit "people"} {:name "gdp", :type "integer", :value 481492421258, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-SWE-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 36.342, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 3.4653, :unit "t/person"} {:name "methane", :type "decimal", :value 7.94, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.0968, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.003394, :unit "°C"} {:name "energy per capita", :type "decimal", :value 59279.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 621.68, :unit "TWh"} {:name "population", :type "integer", :value 10487335, :unit "people"} {:name "gdp", :type "integer", :value 494214521158, :unit "2011 International$"}],
              :locations [{:lat 60.13, :lon 18.64}],
              :context-refs [{:global-id "country-Sweden"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-GBR-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 569.034, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.6353, :unit "t/person"} {:name "methane", :type "decimal", :value 100.304, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.2305, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.030661, :unit "°C"} {:name "energy per capita", :type "decimal", :value 45110.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2664.12, :unit "TWh"} {:name "population", :type "integer", :value 59057333, :unit "people"} {:name "gdp", :type "integer", :value 1889565292321, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-GBR-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 577.971, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.7485, :unit "t/person"} {:name "methane", :type "decimal", :value 97.011, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.2495, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.030924, :unit "°C"} {:name "energy per capita", :type "decimal", :value 45402.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2691.81, :unit "TWh"} {:name "population", :type "integer", :value 59288095, :unit "people"} {:name "gdp", :type "integer", :value 1939268312671, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-GBR-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 560.273, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.4099, :unit "t/person"} {:name "methane", :type "decimal", :value 92.26, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.1331, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.031179, :unit "°C"} {:name "energy per capita", :type "decimal", :value 44185.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2630.84, :unit "TWh"} {:name "population", :type "integer", :value 59540764, :unit "people"} {:name "gdp", :type "integer", :value 1983994308619, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-GBR-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 571.619, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.5556, :unit "t/person"} {:name "methane", :type "decimal", :value 87.62, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.0671, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.031441, :unit "°C"} {:name "energy per capita", :type "decimal", :value 44612.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2668.73, :unit "TWh"} {:name "population", :type "integer", :value 59820385, :unit "people"} {:name "gdp", :type "integer", :value 2050625402735, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-GBR-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 573.43, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.5302, :unit "t/person"} {:name "methane", :type "decimal", :value 83.165, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 2.0043, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.031704, :unit "°C"} {:name "energy per capita", :type "decimal", :value 44717.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2690.65, :unit "TWh"} {:name "population", :type "integer", :value 60169964, :unit "people"} {:name "gdp", :type "integer", :value 2099526641365, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-GBR-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 570.338, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.4121, :unit "t/person"} {:name "methane", :type "decimal", :value 76.798, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.9269, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.031977, :unit "°C"} {:name "energy per capita", :type "decimal", :value 44766.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2712.68, :unit "TWh"} {:name "population", :type "integer", :value 60596107, :unit "people"} {:name "gdp", :type "integer", :value 2161729510824, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-GBR-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 567.846, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.3012, :unit "t/person"} {:name "methane", :type "decimal", :value 71.09, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.8561, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.032245, :unit "°C"} {:name "energy per capita", :type "decimal", :value 43870.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2678.34, :unit "TWh"} {:name "population", :type "integer", :value 61051093, :unit "people"} {:name "gdp", :type "integer", :value 2212618867772, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-GBR-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 559.566, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 9.0933, :unit "t/person"} {:name "methane", :type "decimal", :value 66.371, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.7764, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.032506, :unit "°C"} {:name "energy per capita", :type "decimal", :value 42319.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2604.19, :unit "TWh"} {:name "population", :type "integer", :value 61535859, :unit "people"} {:name "gdp", :type "integer", :value 2267631534502, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-GBR-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 544.932, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.7854, :unit "t/person"} {:name "methane", :type "decimal", :value 61.309, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.7003, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.032758, :unit "°C"} {:name "energy per capita", :type "decimal", :value 41253.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2558.82, :unit "TWh"} {:name "population", :type "integer", :value 62027303, :unit "people"} {:name "gdp", :type "integer", :value 2252034759561, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-GBR-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 494.108, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.9049, :unit "t/person"} {:name "methane", :type "decimal", :value 58.192, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5679, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.032987, :unit "°C"} {:name "energy per capita", :type "decimal", :value 38761.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2422.81, :unit "TWh"} {:name "population", :type "integer", :value 62506222, :unit "people"} {:name "gdp", :type "integer", :value 2151479208521, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-GBR-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 511.905, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 8.1246, :unit "t/person"} {:name "methane", :type "decimal", :value 53.283, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.5364, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.033224, :unit "°C"} {:name "energy per capita", :type "decimal", :value 39284.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2475.15, :unit "TWh"} {:name "population", :type "integer", :value 63006482, :unit "people"} {:name "gdp", :type "integer", :value 2190919919855, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-GBR-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 469.713, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.396, :unit "t/person"} {:name "methane", :type "decimal", :value 51.071, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3623, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.033442, :unit "°C"} {:name "energy per capita", :type "decimal", :value 36722.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2332.19, :unit "TWh"} {:name "population", :type "integer", :value 63509285, :unit "people"} {:name "gdp", :type "integer", :value 2223011392593, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-GBR-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 487.477, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.6215, :unit "t/person"} {:name "methane", :type "decimal", :value 49.958, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3946, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.033669, :unit "°C"} {:name "energy per capita", :type "decimal", :value 37134.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2375.15, :unit "TWh"} {:name "population", :type "integer", :value 63960488, :unit "people"} {:name "gdp", :type "integer", :value 2255210551431, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-GBR-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 477.611, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 7.4175, :unit "t/person"} {:name "methane", :type "decimal", :value 47.803, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.3539, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.033892, :unit "°C"} {:name "energy per capita", :type "decimal", :value 37094.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2388.47, :unit "TWh"} {:name "population", :type "integer", :value 64389386, :unit "people"} {:name "gdp", :type "integer", :value 2296252599482, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-GBR-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 438.807, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.7648, :unit "t/person"} {:name "methane", :type "decimal", :value 46.74, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.2373, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.034096, :unit "°C"} {:name "energy per capita", :type "decimal", :value 34774.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2255.71, :unit "TWh"} {:name "population", :type "integer", :value 64865914, :unit "people"} {:name "gdp", :type "integer", :value 2369725553442, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-GBR-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 422.461, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.4614, :unit "t/person"} {:name "methane", :type "decimal", :value 46.304, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1933, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.034295, :unit "°C"} {:name "energy per capita", :type "decimal", :value 34863.8, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2279.47, :unit "TWh"} {:name "population", :type "integer", :value 65382107, :unit "people"} {:name "gdp", :type "integer", :value 2426435869267, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-GBR-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 399.43, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 6.0618, :unit "t/person"} {:name "methane", :type "decimal", :value 45.942, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.1286, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.034483, :unit "°C"} {:name "energy per capita", :type "decimal", :value 34099.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2246.92, :unit "TWh"} {:name "population", :type "integer", :value 65893359, :unit "people"} {:name "gdp", :type "integer", :value 2478973016876, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-GBR-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 387.367, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.8378, :unit "t/person"} {:name "methane", :type "decimal", :value 46.574, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0768, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.034664, :unit "°C"} {:name "energy per capita", :type "decimal", :value 33924.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2251.04, :unit "TWh"} {:name "population", :type "integer", :value 66354473, :unit "people"} {:name "gdp", :type "integer", :value 2539548543933, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-GBR-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 379.73, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.6886, :unit "t/person"} {:name "methane", :type "decimal", :value 46.395, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 1.0337, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.034844, :unit "°C"} {:name "energy per capita", :type "decimal", :value 33653.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2246.42, :unit "TWh"} {:name "population", :type "integer", :value 66752473, :unit "people"} {:name "gdp", :type "integer", :value 2582848171416, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-GBR-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 364.753, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.4351, :unit "t/person"} {:name "methane", :type "decimal", :value 45.686, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9835, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.035014, :unit "°C"} {:name "energy per capita", :type "decimal", :value 32677.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2192.98, :unit "TWh"} {:name "population", :type "integer", :value 67110958, :unit "people"} {:name "gdp", :type "integer", :value 2624285076706, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-GBR-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 326.263, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.8442, :unit "t/person"} {:name "methane", :type "decimal", :value 44.528, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.928, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.035166, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29440.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1982.84, :unit "TWh"} {:name "population", :type "integer", :value 67351860, :unit "people"} {:name "gdp", :type "integer", :value 2334804059780, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-GBR-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 342.366, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 5.0594, :unit "t/person"} {:name "methane", :type "decimal", :value 44.922, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.9287, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.035324, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29422.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 1990.97, :unit "TWh"} {:name "population", :type "integer", :value 67668788, :unit "people"} {:name "gdp", :type "integer", :value 2512190098029, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-GBR-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 311.118, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 4.5632, :unit "t/person"} {:name "methane", :type "decimal", :value 44.674, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 0.829, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.035468, :unit "°C"} {:name "energy per capita", :type "decimal", :value 29480.3, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 2009.94, :unit "TWh"} {:name "population", :type "integer", :value 68179315, :unit "people"} {:name "gdp", :type "integer", :value 2615230634906, :unit "2011 International$"}],
              :locations [{:lat 55.38, :lon -3.44}],
              :context-refs [{:global-id "country-United Kingdom"} {:global-id "continent-Europe"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}
          {:global-id "owid-co2-USA-2000",
  :features [{:facts [{:name "co2", :type "decimal", :value 6023.158, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 21.3979, :unit "t/person"} {:name "methane", :type "decimal", :value 799.68, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 23.6096, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.186483, :unit "°C"} {:name "energy per capita", :type "decimal", :value 94308.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 26546.24, :unit "TWh"} {:name "population", :type "integer", :value 281484126, :unit "people"} {:name "gdp", :type "integer", :value 12947417795437, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2000-01-01"}]}]}
          {:global-id "owid-co2-USA-2001",
  :features [{:facts [{:name "co2", :type "decimal", :value 5903.562, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 20.7667, :unit "t/person"} {:name "methane", :type "decimal", :value 776.109, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 22.9773, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.189158, :unit "°C"} {:name "energy per capita", :type "decimal", :value 91172.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25918.36, :unit "TWh"} {:name "population", :type "integer", :value 284279634, :unit "people"} {:name "gdp", :type "integer", :value 13073810008320, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2001-01-01"}]}]}
          {:global-id "owid-co2-USA-2002",
  :features [{:facts [{:name "co2", :type "decimal", :value 5947.819, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 20.718, :unit "t/person"} {:name "methane", :type "decimal", :value 757.572, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 22.6451, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.19188, :unit "°C"} {:name "energy per capita", :type "decimal", :value 91601.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 26297.27, :unit "TWh"} {:name "population", :type "integer", :value 287084335, :unit "people"} {:name "gdp", :type "integer", :value 13307343638671, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2002-01-01"}]}]}
          {:global-id "owid-co2-USA-2003",
  :features [{:facts [{:name "co2", :type "decimal", :value 6006.44, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 20.7184, :unit "t/person"} {:name "methane", :type "decimal", :value 764.525, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 21.721, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.194679, :unit "°C"} {:name "energy per capita", :type "decimal", :value 91005.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 26383.08, :unit "TWh"} {:name "population", :type "integer", :value 289908091, :unit "people"} {:name "gdp", :type "integer", :value 13680911930765, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2003-01-01"}]}]}
          {:global-id "owid-co2-USA-2004",
  :features [{:facts [{:name "co2", :type "decimal", :value 6111.545, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 20.8737, :unit "t/person"} {:name "methane", :type "decimal", :value 765.332, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 21.3616, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.19751, :unit "°C"} {:name "energy per capita", :type "decimal", :value 91872.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 26899.12, :unit "TWh"} {:name "population", :type "integer", :value 292786248, :unit "people"} {:name "gdp", :type "integer", :value 14198910513593, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2004-01-01"}]}]}
          {:global-id "owid-co2-USA-2005",
  :features [{:facts [{:name "co2", :type "decimal", :value 6126.903, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 20.7188, :unit "t/person"} {:name "methane", :type "decimal", :value 772.655, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 20.6997, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.20039, :unit "°C"} {:name "energy per capita", :type "decimal", :value 91007.9, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 26912.56, :unit "TWh"} {:name "population", :type "integer", :value 295716666, :unit "people"} {:name "gdp", :type "integer", :value 14673826218839, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2005-01-01"}]}]}
          {:global-id "owid-co2-USA-2006",
  :features [{:facts [{:name "co2", :type "decimal", :value 6045.326, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 20.2367, :unit "t/person"} {:name "methane", :type "decimal", :value 785.023, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 19.7597, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.203098, :unit "°C"} {:name "energy per capita", :type "decimal", :value 89378.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 26699.96, :unit "TWh"} {:name "population", :type "integer", :value 298730208, :unit "people"} {:name "gdp", :type "integer", :value 15065164240312, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2006-01-01"}]}]}
          {:global-id "owid-co2-USA-2007",
  :features [{:facts [{:name "co2", :type "decimal", :value 6121.542, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 20.2805, :unit "t/person"} {:name "methane", :type "decimal", :value 793.939, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 19.4339, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.205854, :unit "°C"} {:name "energy per capita", :type "decimal", :value 89671.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 27066.71, :unit "TWh"} {:name "population", :type "integer", :value 301844223, :unit "people"} {:name "gdp", :type "integer", :value 15333183877566, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2007-01-01"}]}]}
          {:global-id "owid-co2-USA-2008",
  :features [{:facts [{:name "co2", :type "decimal", :value 5919.221, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 19.4092, :unit "t/person"} {:name "methane", :type "decimal", :value 810.64, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 18.4689, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.20856, :unit "°C"} {:name "energy per capita", :type "decimal", :value 86589.6, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 26407.25, :unit "TWh"} {:name "population", :type "integer", :value 304970325, :unit "people"} {:name "gdp", :type "integer", :value 15288552732632, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2008-01-01"}]}]}
          {:global-id "owid-co2-USA-2009",
  :features [{:facts [{:name "co2", :type "decimal", :value 5486.104, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.8107, :unit "t/person"} {:name "methane", :type "decimal", :value 772.483, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 17.409, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.211062, :unit "°C"} {:name "energy per capita", :type "decimal", :value 81521.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25110.53, :unit "TWh"} {:name "population", :type "integer", :value 308023452, :unit "people"} {:name "gdp", :type "integer", :value 14864003344421, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2009-01-01"}]}]}
          {:global-id "owid-co2-USA-2010",
  :features [{:facts [{:name "co2", :type "decimal", :value 5669.25, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 18.2254, :unit "t/person"} {:name "methane", :type "decimal", :value 756.187, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 17.0157, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.213679, :unit "°C"} {:name "energy per capita", :type "decimal", :value 83435.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25953.55, :unit "TWh"} {:name "population", :type "integer", :value 311062785, :unit "people"} {:name "gdp", :type "integer", :value 15239586726210, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2010-01-01"}]}]}
          {:global-id "owid-co2-USA-2011",
  :features [{:facts [{:name "co2", :type "decimal", :value 5538.973, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.6341, :unit "t/person"} {:name "methane", :type "decimal", :value 764.383, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 16.0643, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.216157, :unit "°C"} {:name "energy per capita", :type "decimal", :value 81912.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25729.09, :unit "TWh"} {:name "population", :type "integer", :value 314105075, :unit "people"} {:name "gdp", :type "integer", :value 15477885525000, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2011-01-01"}]}]}
          {:global-id "owid-co2-USA-2012",
  :features [{:facts [{:name "co2", :type "decimal", :value 5331.467, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.8124, :unit "t/person"} {:name "methane", :type "decimal", :value 749.641, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 15.2525, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.218584, :unit "°C"} {:name "energy per capita", :type "decimal", :value 78985.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25047.37, :unit "TWh"} {:name "population", :type "integer", :value 317115349, :unit "people"} {:name "gdp", :type "integer", :value 15830886903539, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2012-01-01"}]}]}
          {:global-id "owid-co2-USA-2013",
  :features [{:facts [{:name "co2", :type "decimal", :value 5473.436, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.0986, :unit "t/person"} {:name "methane", :type "decimal", :value 748.067, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 15.5161, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.221055, :unit "°C"} {:name "energy per capita", :type "decimal", :value 80374.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25728.63, :unit "TWh"} {:name "population", :type "integer", :value 320110752, :unit "people"} {:name "gdp", :type "integer", :value 16122472460312, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2013-01-01"}]}]}
          {:global-id "owid-co2-USA-2014",
  :features [{:facts [{:name "co2", :type "decimal", :value 5531.385, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 17.1189, :unit "t/person"} {:name "methane", :type "decimal", :value 763.215, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 15.5964, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.223574, :unit "°C"} {:name "energy per capita", :type "decimal", :value 80454.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25995.91, :unit "TWh"} {:name "population", :type "integer", :value 323115377, :unit "people"} {:name "gdp", :type "integer", :value 16491319003554, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2014-01-01"}]}]}
          {:global-id "owid-co2-USA-2015",
  :features [{:facts [{:name "co2", :type "decimal", :value 5368.497, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.4614, :unit "t/person"} {:name "methane", :type "decimal", :value 754.889, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 15.1637, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.226008, :unit "°C"} {:name "energy per capita", :type "decimal", :value 78949.2, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25747.43, :unit "TWh"} {:name "population", :type "integer", :value 326126496, :unit "people"} {:name "gdp", :type "integer", :value 16937633974582, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2015-01-01"}]}]}
          {:global-id "owid-co2-USA-2016",
  :features [{:facts [{:name "co2", :type "decimal", :value 5245.362, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 15.9347, :unit "t/person"} {:name "methane", :type "decimal", :value 733.371, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 14.8204, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.228405, :unit "°C"} {:name "energy per capita", :type "decimal", :value 78155.1, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25727.05, :unit "TWh"} {:name "population", :type "integer", :value 329179423, :unit "people"} {:name "gdp", :type "integer", :value 17220065720000, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2016-01-01"}]}]}
          {:global-id "owid-co2-USA-2017",
  :features [{:facts [{:name "co2", :type "decimal", :value 5195.417, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 15.6392, :unit "t/person"} {:name "methane", :type "decimal", :value 749.256, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 14.4419, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.230803, :unit "°C"} {:name "energy per capita", :type "decimal", :value 77729.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25822.16, :unit "TWh"} {:name "population", :type "integer", :value 332204657, :unit "people"} {:name "gdp", :type "integer", :value 17606124654726, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2017-01-01"}]}]}
          {:global-id "owid-co2-USA-2018",
  :features [{:facts [{:name "co2", :type "decimal", :value 5361.236, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 16.001, :unit "t/person"} {:name "methane", :type "decimal", :value 777.6, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 14.5948, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.233252, :unit "°C"} {:name "energy per capita", :type "decimal", :value 79894.0, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 26768.99, :unit "TWh"} {:name "population", :type "integer", :value 335056492, :unit "people"} {:name "gdp", :type "integer", :value 18124692769570, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2018-01-01"}]}]}
          {:global-id "owid-co2-USA-2019",
  :features [{:facts [{:name "co2", :type "decimal", :value 5235.912, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 15.5005, :unit "t/person"} {:name "methane", :type "decimal", :value 799.062, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 14.1181, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.235647, :unit "°C"} {:name "energy per capita", :type "decimal", :value 78683.5, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 26578.49, :unit "TWh"} {:name "population", :type "integer", :value 337790069, :unit "people"} {:name "gdp", :type "integer", :value 18540552700117, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2019-01-01"}]}]}
          {:global-id "owid-co2-USA-2020",
  :features [{:facts [{:name "co2", :type "decimal", :value 4689.954, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 13.8169, :unit "t/person"} {:name "methane", :type "decimal", :value 761.174, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 13.3396, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.237789, :unit "°C"} {:name "energy per capita", :type "decimal", :value 72562.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 24630.39, :unit "TWh"} {:name "population", :type "integer", :value 339436156, :unit "people"} {:name "gdp", :type "integer", :value 18027359681343, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2020-01-01"}]}]}
          {:global-id "owid-co2-USA-2021",
  :features [{:facts [{:name "co2", :type "decimal", :value 5020.111, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 14.758, :unit "t/person"} {:name "methane", :type "decimal", :value 789.44, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 13.6169, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.240084, :unit "°C"} {:name "energy per capita", :type "decimal", :value 76307.4, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 25956.83, :unit "TWh"} {:name "population", :type "integer", :value 340161438, :unit "people"} {:name "gdp", :type "integer", :value 19099378164000, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2021-01-01"}]}]}
          {:global-id "owid-co2-USA-2022",
  :features [{:facts [{:name "co2", :type "decimal", :value 5055.403, :unit "Mt"} {:name "co2 per capita", :type "decimal", :value 14.802, :unit "t/person"} {:name "methane", :type "decimal", :value 801.907, :unit "Mt"} {:name "share of global co2", :type "decimal", :value 13.4711, :unit "%"} {:name "temperature change from co2", :type "decimal", :value 0.242407, :unit "°C"} {:name "energy per capita", :type "decimal", :value 77627.7, :unit "kWh"} {:name "primary energy consumption", :type "decimal", :value 26512.51, :unit "TWh"} {:name "population", :type "integer", :value 341534041, :unit "people"} {:name "gdp", :type "integer", :value 19493170182843, :unit "2011 International$"}],
              :locations [{:lat 37.09, :lon -95.71}],
              :context-refs [{:global-id "country-United States"} {:global-id "continent-North America"}],
              :dates [{:type "occured-at", :value "2022-01-01"}]}]}]})
