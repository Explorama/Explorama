(ns de.explorama.shared.common.data.locations
  (:require [cuerdas.core :as cstr]))

;; Information gatherd from https://www.geonames.org/
;; TODO r1/geo make this configurable

(def ^:private countries
  [{:iso-3361-2 nil, :name "Asia", :lat 51.2086975, :lng 89.2343748, :iso-3361-3 "ABB"}
   {:iso-3361-2 "AD", :name "Andorra", :lat 42.5075314, :lng 1.521815599999968, :iso-3361-3 "AND"}
   {:iso-3361-2 "AE", :name "United Arab Emirates", :lat 23.424076, :lng 53.84781799999996, :iso-3361-3 "ARE"}
   {:iso-3361-2 "AF", :name "Afghanistan", :lat 33.93911, :lng 67.70995300000004, :iso-3361-3 "AFG"}
   {:iso-3361-2 "AG", :name "Antigua and Barbuda", :lat 17.060816, :lng -61.79642799999999, :iso-3361-3 "ATG"}
   {:iso-3361-2 "AL", :name "Albania", :lat 41.153332, :lng 20.168330999999966, :iso-3361-3 "ALB"}
   {:iso-3361-2 "AW", :name "Aruba", :lat 12.521110, :lng -69.968338, :iso-3361-3 "ABW"}
   {:iso-3361-2 "AM", :name "Armenia", :lat 40.069099, :lng 45.03818899999999, :iso-3361-3 "ARM"}
   {:iso-3361-2 "AN", :name "Netherlands Antilles", :lat 12.226079, :lng -69.0600891, :iso-3361-3 "ANT"}
   {:iso-3361-2 "AO", :name "Angola", :lat -11.202692, :lng 17.873886999999968, :iso-3361-3 "AGO"}
   {:iso-3361-2 "AR", :name "Argentina", :lat -38.416097, :lng -63.616671999999994, :iso-3361-3 "ARG"}
   {:iso-3361-2 "AT", :name "Austria", :lat 47.516231, :lng 14.550072, :iso-3361-3 "AUT"}
   {:iso-3361-2 "AU", :name "Australia", :lat -25.274398, :lng 133.77513599999997, :iso-3361-3 "AUS"}
   {:iso-3361-2 "AZ", :name "Azerbaijan", :lat 40.143105, :lng 47.57692700000007, :iso-3361-3 "AZE"}
   {:iso-3361-2 "AI", :name "Anguilla", :lat 18.2232706, :lng -63.0566336, :iso-3361-3 "AIA"}
   {:iso-3361-2 "BA", :name "Bosnia-Herzegovina", :lat 43.915886, :lng 17.67907600000001, :iso-3361-3 "BIH"}
   {:iso-3361-2 "BB", :name "Barbados", :lat 13.193887, :lng -59.54319799999996, :iso-3361-3 "BRB"}
   {:iso-3361-2 "BD", :name "Bangladesh", :lat 23.684994, :lng 90.35633099999995, :iso-3361-3 "BGD"}
   {:iso-3361-2 "BE", :name "Belgium", :lat 50.503887, :lng 4.4699359999999615, :iso-3361-3 "BEL"}
   {:iso-3361-2 "BQ", :name "Bonaire", :lat 12.201890, :lng -68.262383, :iso-3361-3 "BES"}
   {:iso-3361-2 "BF", :name "Burkina Faso", :lat 12.238333, :lng -1.5615930000000162, :iso-3361-3 "BFA"}
   {:iso-3361-2 "BG", :name "Bulgaria", :lat 42.733883, :lng 25.485829999999964, :iso-3361-3 "BGR"}
   {:iso-3361-2 "BH", :name "Bahrain", :lat 25.930414, :lng 50.63777200000004, :iso-3361-3 "BHR"}
   {:iso-3361-2 "BI", :name "Burundi", :lat -3.373056, :lng 29.91888599999993, :iso-3361-3 "BDI"}
   {:iso-3361-2 "BJ", :name "Benin", :lat 9.30769, :lng 2.3158339999999953, :iso-3361-3 "BEN"}
   {:iso-3361-2 "BL", :name "Saint Barthélemy", :lat 17.9, :lng -62.83333300000004, :iso-3361-3 nil}
   {:iso-3361-2 "BN", :name "Brunei Darussalam", :lat 4.535277, :lng 114.72766899999999, :iso-3361-3 "BRN"}
   {:iso-3361-2 "BO", :name "Bolivia", :lat -16.290154, :lng -63.58865300000002, :iso-3361-3 "BOL"}
   {:iso-3361-2 "BR", :name "Brazil", :lat -10.3333333, :lng -53.2, :iso-3361-3 "BRA"}
   {:iso-3361-2 "BS", :name "Bahamas", :lat 25.03428, :lng -77.39627999999999, :iso-3361-3 "BHS"}
   {:iso-3361-2 "BT", :name "Bhutan", :lat 27.514162, :lng 90.43360099999995, :iso-3361-3 "BTN"}
   {:iso-3361-2 "BW", :name "Botswana", :lat -22.328474, :lng 24.684866000000056, :iso-3361-3 "BWA"}
   {:iso-3361-2 "BY", :name "Belarus", :lat 53.709807, :lng 27.953389000000016, :iso-3361-3 "BLR"}
   {:iso-3361-2 "BZ", :name "Belize", :lat 17.497713, :lng -88.18665399999998, :iso-3361-3 "BLZ"}
   {:iso-3361-2 "BM", :name "Bermuda", :lat 32.299507, :lng -64.790337, :iso-3361-3 "BMU"}
   {:iso-3361-2 "CA", :name "Canada", :lat 56.130366, :lng -106.34677099999999, :iso-3361-3 "CAN"}
                ; "Dem. Rep. Congo" (CD) and "Rep. Congo" (CG) are different states!
                ; The plain name "Congo" mostly refers to "Dem. Rep. Congo".
   {:iso-3361-2 "CD", :name "Democratic Republic of Congo", :lat -4.3103947, :lng 21.7500603, :iso-3361-3 "COD"}
   {:iso-3361-2 "CF", :name "Central African Republic", :lat 6.611111, :lng 20.93944399999998, :iso-3361-3 "CAF"}
   {:iso-3361-2 "CG", :name "Republic of Congo", :lat -0.228021, :lng 15.82765900000004, :iso-3361-3 "COG"}
   {:iso-3361-2 "CH", :name "Switzerland", :lat 46.818188, :lng 8.227511999999933, :iso-3361-3 "CHE"}
   {:iso-3361-2 "CI", :name "Cote D'ivoire", :lat 7.539989, :lng -5.547080000000051, :iso-3361-3 "CIV"}
   {:iso-3361-2 "CL", :name "Chile", :lat -35.675147, :lng -71.54296899999997, :iso-3361-3 "CHL"}
   {:iso-3361-2 "CM", :name "Cameroon", :lat 7.369722, :lng 12.354722000000038, :iso-3361-3 "CMR"}
   {:iso-3361-2 "CN", :name "China", :lat 35.86166, :lng 104.19539699999996, :iso-3361-3 "CHN"}
   {:iso-3361-2 "KY", :name "Cayman Islands", :lat 19.292997, :lng -81.366806, :iso-3361-3 "CYM"}
   {:iso-3361-2 "HK", :name "China, Hong Kong SAR", :lat 22.286394, :lng 114.149139, :iso-3361-3 "HKG"}
   {:iso-3361-2 "CO", :name "Colombia", :lat 4.570868, :lng -74.29733299999998, :iso-3361-3 "COL"}
   {:iso-3361-2 "CR", :name "Costa Rica", :lat 9.748917, :lng -83.75342799999999, :iso-3361-3 "CRI"}
   {:iso-3361-2 "CU", :name "Cuba", :lat 21.521757, :lng -77.78116699999998, :iso-3361-3 "CUB"}
   {:iso-3361-2 "CV", :name "Cape Verde", :lat 16.002082, :lng -24.01319699999999, :iso-3361-3 "CPV"}
   {:iso-3361-2 "CY", :name "Cyprus", :lat 35.126413, :lng 33.429858999999965, :iso-3361-3 "CYP"}
   {:iso-3361-2 "CZ", :name "Czech Republic", :lat 49.817492, :lng 15.472962000000052, :iso-3361-3 "CZE"}
   {:iso-3361-2 "CK", :name "Cook Islands", :lat -21.236736, :lng -159.785278, :iso-3361-3 "COK"}
   {:iso-3361-2 "CW", :name "Curaçao", :lat 12.169570, :lng -68.990021, :iso-3361-3 "CUW"}
   {:iso-3361-2 "DE", :name "Germany", :lat 51.165691, :lng 10.451526000000058, :iso-3361-3 "DEU"}
   {:iso-3361-2 "DJ", :name "Djibouti", :lat 11.588, :lng 43.14499999999998, :iso-3361-3 "DJI"}
   {:iso-3361-2 "DK", :name "Denmark", :lat 56.26392, :lng 9.50178500000004, :iso-3361-3 "DNK"}
   {:iso-3361-2 "DM", :name "Dominica", :lat 15.414999, :lng -61.370975999999985, :iso-3361-3 "DMA"}
   {:iso-3361-2 "DO", :name "Dominican Republic", :lat 18.735693, :lng -70.16265099999998, :iso-3361-3 "DOM"}
   {:iso-3361-2 "DZ", :name "Algeria", :lat 36.752887, :lng 3.0420480000000225, :iso-3361-3 "DZA"}
   {:iso-3361-2 "EC", :name "Ecuador", :lat -1.831239, :lng -78.18340599999999, :iso-3361-3 "ECU"}
   {:iso-3361-2 "EE", :name "Estonia", :lat 58.595272, :lng 25.01360699999998, :iso-3361-3 "EST"}
   {:iso-3361-2 "EG", :name "Egypt", :lat 26.820553, :lng 30.802498000000014, :iso-3361-3 "EGY"}
   {:iso-3361-2 "ER", :name "Eritrea", :lat 15.179384, :lng 39.78233399999999, :iso-3361-3 "ERI"}
   {:iso-3361-2 "ES", :name "Spain", :lat 40.463667, :lng -3.7492200000000366, :iso-3361-3 "ESP"}
   {:iso-3361-2 "ET", :name "Ethiopia", :lat 9.145, :lng 40.48967300000004, :iso-3361-3 "ETH"}
   {:iso-3361-2 "EH", :name "Western Sahara", :lat 26.741856, :lng -11.678367, :iso-3361-3 "ESH"}
   {:iso-3361-2 "FI", :name "Finland", :lat 61.92411, :lng 25.748151000000007, :iso-3361-3 "FIN"}
   {:iso-3361-2 "FJ", :name "Fiji", :lat -17.713371, :lng 178.06503199999997, :iso-3361-3 "FJI"}
   {:iso-3361-2 "FM", :name "Federated States of Micronesia", :lat 7.425554, :lng 158.2150717, :iso-3361-3 "FSM"}
   {:iso-3361-2 "FO", :name "Faroe Islands", :lat 62.00, :lng -6.783333, :iso-3361-3 "FRO"}
   {:iso-3361-2 "FO", :name "Faeroe Islands", :lat 62.044872, :lng -7.032297, :iso-3361-3 "FRO"}
   {:iso-3361-2 "FR", :name "France", :lat 46.227638, :lng 2.213749000000007, :iso-3361-3 "FRA"}
   {:iso-3361-2 "FK", :name "Falkland Islands", :lat -51.563412, :lng -59.820557, :iso-3361-3 "FLK"}
   {:iso-3361-2 "GF", :name "French Guiana", :lat 3.9332383, :lng -53.0875742, :iso-3361-3 "GUF"}
   {:iso-3361-2 "GA", :name "Gabon", :lat -0.803689, :lng 11.60944399999994, :iso-3361-3 "GAB"}
   {:iso-3361-2 "GB", :name "United Kingdom", :lat 55.378051, :lng -3.43597299999999, :iso-3361-3 "GBR"}
   {:iso-3361-2 "GD", :name "Grenada", :lat 12.262776, :lng -61.60417100000001, :iso-3361-3 "GRD"}
   {:iso-3361-2 "GE", :name "Georgia", :lat 42.315407, :lng 43.356892000000016, :iso-3361-3 "GEO"}
   {:iso-3361-2 "GG", :name "Guernsey", :lat 49.4481982, :lng -2.589490000000069, :iso-3361-3 nil}
   {:iso-3361-2 "GH", :name "Ghana", :lat 7.946527, :lng -1.0231939999999895, :iso-3361-3 "GHA"}
   {:iso-3361-2 "GM", :name "Gambia", :lat 13.443182, :lng -15.310138999999936, :iso-3361-3 "GMB"}
   {:iso-3361-2 "GN", :name "Guinea", :lat 9.945587, :lng -9.69664499999999, :iso-3361-3 "GIN"}
   {:iso-3361-2 "GQ", :name "Equatorial Guinea", :lat 1.650801, :lng 10.267894999999953, :iso-3361-3 "GNQ"}
   {:iso-3361-2 "GR", :name "Greece", :lat 39.074208, :lng 21.824311999999964, :iso-3361-3 "GRC"}
   {:iso-3361-2 "GT", :name "Guatemala", :lat 14.6133333, :lng -90.53527780000002, :iso-3361-3 "GTM"}
   {:iso-3361-2 "GW", :name "Guinea-Bissau", :lat 11.803749, :lng -15.180413000000044, :iso-3361-3 "GNB"}
   {:iso-3361-2 "GY", :name "Guyana", :lat 4.860416, :lng -58.93018000000001, :iso-3361-3 "GUY"}
   {:iso-3361-2 "GU", :name "Guam", :lat 13.444304, :lng 144.793732, :iso-3361-3 "GUM"}
   {:iso-3361-2 "GI", :name "Gibraltar", :lat 35.946339, :lng -5.655601, :iso-3361-3 "GIB"}
   {:iso-3361-2 "GP", :name "Guadeloupe", :lat 16.1730949, :lng -61.4054001, :iso-3361-3 "GKP"}
   {:iso-3361-2 "GL", :name "Greenland", :lat   71.7069397, :lng -42.6043015, :iso-3361-3 "GRL"}
   {:iso-3361-2 "HN", :name "Honduras", :lat 15.199999, :lng -86.24190499999997, :iso-3361-3 "HND"}
   {:iso-3361-2 "HR", :name "Croatia", :lat 45.1, :lng 15.200000000000045, :iso-3361-3 "HRV"}
   {:iso-3361-2 "HT", :name "Haiti", :lat 18.971187, :lng -72.285215, :iso-3361-3 "HTI"}
   {:iso-3361-2 "HU", :name "Hungary", :lat 47.162494, :lng 19.50330400000007, :iso-3361-3 "HUN"}
   {:iso-3361-2 "ID", :name "Indonesia", :lat -0.789275, :lng 113.92132700000002, :iso-3361-3 "IDN"}
   {:iso-3361-2 "IE", :name "Ireland", :lat 53.41291, :lng -8.243889999999965, :iso-3361-3 "IRL"}
   {:iso-3361-2 "IL", :name "Israel", :lat 31.046051, :lng 34.85161199999993, :iso-3361-3 "ISR"}
   {:iso-3361-2 "IM", :name "Isle of Man", :lat 54.2386803, :lng -4.561995900000056, :iso-3361-3 nil}
   {:iso-3361-2 "IN", :name "India", :lat 20.593684, :lng 78.96288000000004, :iso-3361-3 "IND"}
   {:iso-3361-2 "IQ", :name "Iraq", :lat 33.223191, :lng 43.679291000000035, :iso-3361-3 "IRQ"}
   {:iso-3361-2 "IR", :name "Iran", :lat 32.427908, :lng 53.688045999999986, :iso-3361-3 "IRN"}
   {:iso-3361-2 "IS", :name "Iceland", :lat 64.963051, :lng -19.020835000000034, :iso-3361-3 "ISL"}
   {:iso-3361-2 "IT", :name "Italy", :lat 41.87194, :lng 12.567379999999957, :iso-3361-3 "ITA"}
   {:iso-3361-2 "JE", :name "Jersey", :lat 49.214439, :lng -2.1312500000000227, :iso-3361-3 nil}
   {:iso-3361-2 "JM", :name "Jamaica", :lat 18.109581, :lng -77.297508, :iso-3361-3 "JAM"}
   {:iso-3361-2 "JO", :name "Jordan", :lat 30.585164, :lng 36.238414000000034, :iso-3361-3 "JOR"}
   {:iso-3361-2 "JP", :name "Japan", :lat 36.204824, :lng 138.252924, :iso-3361-3 "JPN"}
   {:iso-3361-2 "KE", :name "Kenya", :lat -0.023559, :lng 37.90619300000003, :iso-3361-3 "KEN"}
   {:iso-3361-2 "KG", :name "Kyrgystan", :lat 41.20438, :lng 74.76609800000006, :iso-3361-3 "KGZ"}
   {:iso-3361-2 "KH", :name "Cambodia", :lat 12.565679, :lng 104.99096299999997, :iso-3361-3 "KHM"}
   {:iso-3361-2 "algorithms", :name "Kiribati", :lat 	1.4167, :lng -157.3626011, :iso-3361-3 "KIR"}
   {:iso-3361-2 "KN", :name "Saint Kitts and Nevis", :lat 17.357822, :lng -62.78299800000002, :iso-3361-3 "KNA"}
   {:iso-3361-2 "KN", :name "Saint Kitts & Nevis", :lat 17.357822, :lng -62.78299800000002, :iso-3361-3 "KNA"}
   {:iso-3361-2 "KM", :name "Comorros", :lat -11.875001, :lng 43.87221899999997, :iso-3361-3 "COM"}
   {:iso-3361-2 "KP", :name "North Korea", :lat 40.339852, :lng 127.51009299999998, :iso-3361-3 "PRK"}
   {:iso-3361-2 "KR", :name "South Korea", :lat 35.907757, :lng 127.76692200000002, :iso-3361-3 "KOR"}
   {:iso-3361-2 "KW", :name "Kuwait", :lat 29.3697222, :lng 47.97833330000003, :iso-3361-3 "KWT"}
   {:iso-3361-2 "KZ", :name "Kazakhstan", :lat 48.019573, :lng 66.92368399999998, :iso-3361-3 "KAZ"}
   {:iso-3361-2 "LA", :name "Laos", :lat 19.85627, :lng 102.495496, :iso-3361-3 "LAO"}
   {:iso-3361-2 "LC", :name "Saint Lucia", :lat   13.909444, :lng -60.978893, :iso-3361-3 "LCA"}
   {:iso-3361-2 "LI", :name "Liechtenstein", :lat 47.166, :lng 9.555373000000031, :iso-3361-3 "LIE"}
   {:iso-3361-2 "LB", :name "Lebanon", :lat 33.854721, :lng 35.86228499999993, :iso-3361-3 "LBN"}
   {:iso-3361-2 "LK", :name "Sri Lanka", :lat 7.873054, :lng 80.77179699999999, :iso-3361-3 "LKA"}
   {:iso-3361-2 "LR", :name "Liberia", :lat 6.428055, :lng -9.429498999999964, :iso-3361-3 "LBR"}
   {:iso-3361-2 "LS", :name "Lesotho", :lat -29.609988, :lng 28.233608000000004, :iso-3361-3 "LSO"}
   {:iso-3361-2 "LT", :name "Lithuania", :lat 55.169438, :lng 23.88127499999996, :iso-3361-3 "LTU"}
   {:iso-3361-2 "LU", :name "Luxembourg", :lat 49.6100036, :lng 6.129595999999992, :iso-3361-3 "LUX"}
   {:iso-3361-2 "LU", :name "Luxemburg", :lat 49.6100036, :lng 6.129595999999992, :iso-3361-3 "LUX"}
   {:iso-3361-2 "LV", :name "Latvia", :lat 56.879635, :lng 24.60318899999993, :iso-3361-3 "LVA"}
   {:iso-3361-2 "LY", :name "Libya", :lat 26.3351, :lng 17.228331000000026, :iso-3361-3 "LBY"}
   {:iso-3361-2 "MA", :name "Morocco", :lat 31.791702, :lng -7.092620000000011, :iso-3361-3 "MAR"}
   {:iso-3361-2 "MD", :name "Moldova", :lat 47.411631, :lng 28.369885000000068, :iso-3361-3 "MDA"}
   {:iso-3361-2 "ME", :name "Montenegro", :lat 42.708678, :lng 19.37438999999995, :iso-3361-3 nil}
   {:iso-3361-2 "MF", :name "Saint Martin (French part)", :lat 18.08255, :lng -63.05225100000001, :iso-3361-3 "MAF"}
   {:iso-3361-2 "SX", :name "Saint Martin (Dutch part)", :lat 18.0237, :lng -63.0458, :iso-3361-3 "SXM"}
   {:iso-3361-2 "MG", :name "Madagascar", :lat -18.766947, :lng 46.869106999999985, :iso-3361-3 "MDG"}
   {:iso-3361-2 "MH", :name "Marshall Islands", :lat 7.131474, :lng 171.184478, :iso-3361-3 "MHL"}
   {:iso-3361-2 "MK", :name "Macedonia", :lat 41.608635, :lng 21.745274999999992, :iso-3361-3 "MKD"}
   {:iso-3361-2 "ML", :name "Mali", :lat 17.570692, :lng -3.9961660000000165, :iso-3361-3 "MLI"}
   {:iso-3361-2 "MM", :name "Myanmar", :lat 21.913965, :lng 95.95622300000002, :iso-3361-3 "MMR"}
   {:iso-3361-2 "MN", :name "Mongolia", :lat 46.862496, :lng 103.84665599999994, :iso-3361-3 "MNG"}
   {:iso-3361-2 "MO", :name "Macao", :lat 22.198745, :lng 113.54387300000008, :iso-3361-3 "MAC"}
   {:iso-3361-2 "MC", :name "Monaco", :lat 43.740070, :lng 7.426644, :iso-3361-3 "MCO"}
   {:iso-3361-2 "MR", :name "Mauritania", :lat 21.00789, :lng -10.940834999999993, :iso-3361-3 "MRT"}
   {:iso-3361-2 "MS", :name "Montserrat", :lat 16.742498, :lng -62.187366, :iso-3361-3 "MSR"}
   {:iso-3361-2 "MT", :name "Malta", :lat 35.9, :lng 14.516667, :iso-3361-3 "MLT"}
   {:iso-3361-2 "MQ", :name "Martinique", :lat 14.469400, :lng -60.865799, :iso-3361-3 "MTQ"}
   {:iso-3361-2 "MU", :name "Mauritius", :lat -20.348404, :lng 57.55215199999998, :iso-3361-3 "MUS"}
   {:iso-3361-2 "MV", :name "Maldives", :lat 3.202778, :lng 73.22068000000002, :iso-3361-3 "MDV"}
   {:iso-3361-2 "MV", :name "Republic Of Maldives", :lat 3.202778, :lng 73.22068000000002, :iso-3361-3 "MDV"}
   {:iso-3361-2 "MW", :name "Malawi", :lat -13.254308, :lng 34.30152499999997, :iso-3361-3 "MWI"}
   {:iso-3361-2 "MX", :name "Mexico", :lat 23.634501, :lng -102.552784, :iso-3361-3 "MEX"}
   {:iso-3361-2 "MY", :name "Malaysia", :lat 4.210484, :lng 113.0933, :iso-3361-3 "MYS"}
   {:iso-3361-2 "MZ", :name "Mozambique", :lat -18.665695, :lng 35.52956199999994, :iso-3361-3 "MOZ"}
   {:iso-3361-2 "YT", :name "Mayotte", :lat -12.809645, :lng 45.130741, :iso-3361-3 "MYT"}
   {:iso-3361-2 "NA", :name "Namibia", :lat -22.95764, :lng 18.490409999999997, :iso-3361-3 "NAM"}
   {:iso-3361-2 "NE", :name "Niger", :lat 17.607789, :lng 8.081666, :iso-3361-3 "NER"}
   {:iso-3361-2 "NG", :name "Nigeria", :lat 9.081999, :lng 8.675277000000051, :iso-3361-3 "NGA"}
   {:iso-3361-2 "NI", :name "Nicaragua", :lat 12.865416, :lng -85.20722899999998, :iso-3361-3 "NIC"}
   {:iso-3361-2 "NL", :name "Netherlands", :lat 52.132633, :lng 5.2912659999999505, :iso-3361-3 "NLD"}
   {:iso-3361-2 "NO", :name "Norway", :lat 60.472024, :lng 8.46894599999996, :iso-3361-3 "NOR"}
   {:iso-3361-2 "NU", :name "Niue", :lat -19.054445, :lng -169.867233, :iso-3361-3 "NIU"}
   {:iso-3361-2 "NP", :name "Nepal", :lat 28.394857, :lng 84.124008, :iso-3361-3 "NPL"}
   {:iso-3361-2 "NR", :name "Nauru", :lat -0.522778, :lng 166.93150300000002, :iso-3361-3 "NRU"}
   {:iso-3361-2 "NZ", :name "New Zealand", :lat -40.900557, :lng 174.88597100000004, :iso-3361-3 "NZL"}
   {:iso-3361-2 "NC", :name "New Caledonia", :lat -21.2107283, :lng 165.8517014, :iso-3361-3 "NCL"}
   {:iso-3361-2 "NF", :name "Norfolk Island", :lat -29.0328267, :lng 167.9543925, :iso-3361-3 "NFK"}
   {:iso-3361-2 "OM", :name "Oman", :lat 21.512583, :lng 55.923254999999926, :iso-3361-3 "OMN"}
   {:iso-3361-2 "PA", :name "Panama", :lat 8.994269, :lng -79.51879200000002, :iso-3361-3 "PAN"}
   {:iso-3361-2 "PE", :name "Peru", :lat -9.189967, :lng -75.015152, :iso-3361-3 "PER"}
   {:iso-3361-2 "PG", :name "Papua New Guinea", :lat -6.314993, :lng 143.95555000000002, :iso-3361-3 "PNG"}
   {:iso-3361-2 "PH", :name "Phillipines", :lat 12.879721, :lng 121.77401699999996, :iso-3361-3 "PHL"}
   {:iso-3361-2 "PK", :name "Pakistan", :lat 30.375321, :lng 69.34511599999996, :iso-3361-3 "PAK"}
   {:iso-3361-2 "PL", :name "Poland", :lat 51.919438, :lng 19.14513599999998, :iso-3361-3 "POL"}
   {:iso-3361-2 "PS", :name "Palestinian Territory, Occupied", :lat 31.92157, :lng 35.20329, :division "country", :iso-3361-3 "PSE"}
   {:iso-3361-2 "PS", :name "Palestine", :lat 31.92157, :lng 35.20329, :division "country", :iso-3361-3 "PSE"}
   {:iso-3361-2 "PT", :name "Portugal", :lat 39.399872, :lng -8.224454000000037, :iso-3361-3 "PRT"}
   {:iso-3361-2 "PW", :name "Palau", :lat 7.51498, :lng 134.58251999999993, :iso-3361-3 "PLW"}
   {:iso-3361-2 "PY", :name "Paraguay", :lat -23.442503, :lng -58.443831999999986, :iso-3361-3 "PRY"}
   {:iso-3361-2 "PF", :name "French Polynesia", :lat -16.499701, :lng -149.406843, :iso-3361-3 "PYF"}
   {:iso-3361-2 "PR", :name "Puerto Rico", :lat 18.200178, :lng -66.664513, :iso-3361-3 "PRI"}
   {:iso-3361-2 "QA", :name "Qatar", :lat 25.354826, :lng 51.183884000000035, :iso-3361-3 "QAT"}
   {:iso-3361-2 "RO", :name "Romania", :lat 45.943161, :lng 24.966760000000022, :iso-3361-3 "ROU"}
   {:iso-3361-2 "RS", :name "Serbia", :lat 44.016521, :lng 21.005858999999987, :iso-3361-3 "SRB"}
   {:iso-3361-2 "RU", :name "Russia", :lat 61.52401, :lng 105.31875600000001, :iso-3361-3 "RUS"}
   {:iso-3361-2 "RW", :name "Rwanda", :lat -1.940278, :lng 29.873887999999965, :iso-3361-3 "RWA"}
   {:iso-3361-2 "RE", :name "Réunion", :lat -20.897305, :lng 55.551167, :iso-3361-3 "REU"}
   {:iso-3361-2 "SA", :name "Saudi Arabia", :lat 23.885942, :lng 45.079162, :iso-3361-3 "SAU"}
   {:iso-3361-2 "SH-HL", :name "Saint Helena", :lat -15.969457, :lng -5.712944, :iso-3361-3 "SH-HL"}
   {:iso-3361-2 "FR-PM", :name "Saint Pierre and Miquelon", :lat 46.956893, :lng -56.393461, :iso-3361-3 "FR-PM"}
   {:iso-3361-2 "AI", :name "St. Kitts-Nevis-Anguilla", :lat 18.21667, :lng -63.05, :iso-3361-3 "AI"}
   {:iso-3361-2 "SB", :name "Solomon Islands", :lat -9.64571, :lng 160.15619400000003, :iso-3361-3 "SLB"}
   {:iso-3361-2 "SC", :name "Seychelles", :lat -4.679574, :lng 55.49197700000002, :iso-3361-3 "SYC"}
   {:iso-3361-2 "SD", :name "Sudan", :lat 12.862807, :lng 30.21763599999997, :iso-3361-3 "SDN"}
   {:iso-3361-2 "SS", :name "South Sudan", :lat 4.859363, :lng 31.571251, :iso-3361-3 "SSD"}
   {:iso-3361-2 "SE", :name "Sweden", :lat 60.128161, :lng 18.643501000000015, :iso-3361-3 "SWE"}
   {:iso-3361-2 "SG", :name "Singapore", :lat 1.2894365, :lng 103.8499802, :iso-3361-3 "SGP"}
   {:iso-3361-2 "SH", :name "Saint Helena, Ascension and Tristan da Cunha", :division "country", :iso-3361-3 "SHN"}
   {:iso-3361-2 "SI", :name "Slovenia", :lat 46.151241, :lng 14.995462999999972, :iso-3361-3 "SVN"}
   {:iso-3361-2 "SK", :name "Slovakia", :lat 48.669026, :lng 19.69902400000001, :iso-3361-3 "SVK"}
   {:iso-3361-2 "SL", :name "Sierra Leone", :lat 8.460555, :lng -11.779889000000026, :iso-3361-3 "SLE"}
   {:iso-3361-2 "SM", :name "San Marino", :lat 43.942402, :lng 12.44543699999997, :iso-3361-3 "SMR"}
   {:iso-3361-2 "SN", :name "Senegal", :lat 14.497401, :lng -14.452361999999994, :iso-3361-3 "SEN"}
   {:iso-3361-2 "PM", :name "Sankt Pierre und Miquelon", :lat 46.9466881, :lng -56.2622848, :iso-3361-3 "SPM"}
   {:iso-3361-2 "SO", :name "Somalia", :lat 5.152149, :lng 46.19961599999999, :iso-3361-3 "SOM"}
   {:iso-3361-2 "SR", :name "Suriname", :lat 3.919305, :lng -56.027783, :iso-3361-3 "SUR"}
   {:iso-3361-2 "ST", :name "Sao Tome and Principe", :lat 0.18636, :lng 6.613080999999966, :iso-3361-3 "STP"}
   {:iso-3361-2 "SV", :name "El Salvador", :lat 13.794185, :lng -88.89652999999998, :iso-3361-3 "SLV"}
   {:iso-3361-2 "SY", :name "Syria", :lat 34.802075, :lng 38.99681499999997, :iso-3361-3 "SYR"}
   {:iso-3361-2 "SZ", :name "Swaziland", :lat -26.522503, :lng 31.465866000000005, :iso-3361-3 "SWZ"}
   {:iso-3361-2 "SJ", :name "Svalbard and Jan Mayen", :lat 79.004959, :lng 17.666016, :iso-3361-3 "SJM"}
   {:iso-3361-2 "TD", :name "Chad", :lat 15.454166, :lng 18.732207000000017, :iso-3361-3 "TCD"}
   {:iso-3361-2 "TG", :name "Togo", :lat 8.619543, :lng 0.8247820000000274, :iso-3361-3 "TGO"}
   {:iso-3361-2 "TC", :name "Turks and Caicos Islands", :lat 21.804132, :lng -72.305832, :iso-3361-3 "TCA"}
   {:iso-3361-2 "TH", :name "Thailand", :lat 15.870032, :lng 100.99254100000007, :iso-3361-3 "THA"}
   {:iso-3361-2 "TJ", :name "Tajikistan", :lat 38.861034, :lng 71.27609299999995, :iso-3361-3 "TJK"}
   {:iso-3361-2 "TK", :name "Tokelau", :lat -8.965767, :lng -171.857372, :iso-3361-3 "TKL"}
   {:iso-3361-2 "TL", :name "Timor Leste", :lat -8.874217, :lng 125.72753899999998, :iso-3361-3 "TLS"}
   {:iso-3361-2 nil, :name "Timor", :lat -9.346017, :lng 124.637077, :iso-3361-3 nil}
   {:iso-3361-2 "TM", :name "Turkmenistan", :lat 38.969719, :lng 59.55627800000002, :iso-3361-3 "TKM"}
   {:iso-3361-2 "TN", :name "Tunisia", :lat 33.886917, :lng 9.537499, :iso-3361-3 "TUN"}
   {:iso-3361-2 "TO", :name "Tonga", :lat -21.178986, :lng -175.198242, :iso-3361-3 "TON"}
   {:iso-3361-2 "TR", :name "Turkey", :lat 38.963745, :lng 35.243322000000035, :iso-3361-3 "TUR"}
   {:iso-3361-2 "TT", :name "Trinidad and Tobago", :lat 10.691803, :lng -61.22250300000002, :iso-3361-3 "TTO"}
   {:iso-3361-2 "TV", :name "Tuvalu", :lat -7.109535, :lng 177.64932999999996, :iso-3361-3 "TUV"}
   {:iso-3361-2 "TW", :name "Taiwan", :lat 23.69781, :lng 120.96051499999999, :iso-3361-3 "TWN"}
   {:iso-3361-2 "TZ", :name "Tanzania", :lat -6.369028, :lng 34.888822000000005, :iso-3361-3 "TZA"}
   {:iso-3361-2 nil, :name "Tibetan", :lat 29.652491, :lng 91.172112, :iso-3361-3 "TIB"}
   {:iso-3361-2 "UA", :name "Ukraine", :lat 48.379433, :lng 31.165579999999977, :iso-3361-3 "UKR"}
   {:iso-3361-2 "UG", :name "Uganda", :lat 1.373333, :lng 32.290275000000065, :iso-3361-3 "UGA"}
   {:iso-3361-2 "UM", :name "United States Minor Outlying Islands", :division "country", :iso-3361-3 "UMI"}
   {:iso-3361-2 "US", :name "United States", :lat 37.09024, :lng -95.71289100000001, :iso-3361-3 "USA"}
   {:iso-3361-2 "VI", :name "US Virgin Islands", :lat 17.738009708772523, :lng -64.76155341409797,, :iso-3361-3 "VIR"}
   {:iso-3361-2 "UY", :name "Uruguay", :lat -32.522779, :lng -55.76583500000004, :iso-3361-3 "URY"}
   {:iso-3361-2 "UZ", :name "Uzbekistan", :lat 41.377491, :lng 64.58526200000006, :iso-3361-3 "UZB"}
   {:iso-3361-2 "VC", :name "Saint Vincent and the Grenadines", :lat 12.984305, :lng -61.28722800000003, :iso-3361-3 "VCT"}
   {:iso-3361-2 "VE", :name "Venezuela", :lat 6.42375, :lng -66.58973000000003, :iso-3361-3 "VEN"}
   {:iso-3361-2 "VN", :name "Viet Nam", :lat 14.058324, :lng 108.277199, :iso-3361-3 "VNM"}
   {:iso-3361-2 "VU", :name "Vanuatu", :lat -15.376706, :lng 166.959158, :iso-3361-3 "VUT"}
   {:iso-3361-2 "VA", :name "Holy See (the)", :lat 41.904755, :lng 12.454628, :iso-3361-3 "VAT"}
   {:iso-3361-2 "VG", :name "British Virgin Islands", :lat 18.4180894, :lng -64.5854311, :iso-3361-3 "VGB"}
   {:iso-3361-2 "VG", :name "Virgin Islands British", :lat 18.4180894, :lng -64.5854311, :iso-3361-3 "VGB"}
   {:iso-3361-2 "WS", :name "Samoa", :lat -13.759029, :lng -172.104629, :iso-3361-3 "WSM"}
   {:iso-3361-2 "WF", :name "Wallis and Futuna Islands", :lat -14.3013291, :lng -178.0908626, :iso-3361-3 "WLF"}
   {:iso-3361-2 "AS", :name "American Samoa", :lat -14.275632, :lng -170.702042, :iso-3361-3 "ASM"}
   {:iso-3361-2 "YE", :name "Yemen", :lat 15.552727, :lng 48.516388000000006, :iso-3361-3 "YEM"}
   {:iso-3361-2 "ZA", :name "South Africa", :lat -30.559482, :lng 22.937505999999985, :iso-3361-3 "ZAF"}
   {:iso-3361-2 "ZM", :name "Zambia", :lat -13.133897, :lng 27.849332000000004, :iso-3361-3 "ZMB"}
   {:iso-3361-2 "ZW", :name "Zimbabwe", :lat -19.015438, :lng 29.154856999999993, :iso-3361-3 "ZWE"}
   {:iso-3361-2 "SZ", :name "Kingdom of Eswatini", :lat -26.5, :lng 31.5, :iso-3361-3 "SWZ"}
   {:iso-3361-2 "XK", :name "Kosovo", :iso-3361-3 "XKX", :lat 42.667542, :lng 21.166191, :unofficial true}
   {:iso-3361-2 "XX", :name "Unknown", :iso-3361-3 "XXX", :unofficial true}
   {:iso-3361-2 "XZ", :name "Stateless", :iso-3361-3 "XXZ", :unofficial true}
   {:iso-3361-2 "XY", :name "Multinational", :iso-3361-3 "XXY", :unofficial true}
   {:iso-3361-2 "XW", :name "International", :iso-3361-3 "XXW", :unofficial true}

                ; Values for continents taken from here: https://de.wikipedia.org/wiki/Liste_geographischer_Mittelpunkte#Rest_der_Welt
   {:iso-3361-2 nil, :name "Europe", :lat 54.9, :lng 25.316667, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Asia", :lat 51.725, :lng 94.443611, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Africa", :lat 2.07035, :lng 17.05291, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "North America", :lat 48.354433, :lng -99.998094, :iso-3361-3 nil}
                ; estimated from http://andywoodruff.com/blog/land-by-latitude-and-longitude-or-a-pile-of-continents/
   {:iso-3361-2 nil, :name "South America", :lat -15.0, :lng -65.0, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Antarctica", :lat -75.0, :lng -0.071389, :iso-3361-3 nil}

                ; Other political or economical associations of countries
   {:iso-3361-2 nil, :name "Staatengem.Asi.", :lat 51.2086975, :lng 89.2343748, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "SPC Secretariate of the Pacific Community", :lat -22.300733, :lng 166.443698, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "CTPT Trinitarian Commission of the Plan Trifinio", :lat 13.707397, :lng -89.244755, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "The Gambia River Basin Development Organization (O.M.V.G.)", :lat 13.443182, :lng -15.310139, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "CAF (Andean Pact)", :lat -16.513937, :lng -68.119854, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Southeastern Europe", :lat 46.297206, :lng 24.771908, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "M.R.C. Mekong River Commission (Asia)", :lat 16.532238, :lng 104.735276, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Fund for the Development of Indigenous Peoples of Latin America and the Caribbean", :lat -16.508265, :lng -68.127753, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "COMIFAC Central African Forests Commission", :lat 3.855836, :lng 11.506989, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "MENA NA The region of North Africa and the Middle East", :lat 26.820553, :lng 30.802498000000014, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "African Union", :lat 9.002769, :lng 38.741149, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "CEPGL Economic Community of the Great Lakes Countries", :lat -1.958570, :lng 30.741149, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "ACB Asean Centre for Biodiversity", :lat 14.163992, :lng 121.230594, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "SICA Central American Integration System", :lat 13.670971, :lng -89.247782, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Latin America", :lat 13.670971, :lng -89.247782, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Caucasus", :lat 43.029644, :lng 44.105239, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "ECOWAS Economic Community of West African States", :lat 9.30769, :lng 2.3158339999999953, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "CARICOM Caribbean Community", :lat 17.692984, :lng -88.793391, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "MOE/SOE Central Eastern European and South Eastern European", :lat 49.082861, :lng 17.118344, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "ABN Niger Basin Authority", :lat 16.461475, :lng 9.970969, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Middle East", :lat 35.689091, :lng 44.882980, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Central American Commission for Environment and Development (CCAD)", :lat 13.670971, :lng -89.247782, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "OTCA Amazon Cooperation Treaty Organization", :lat -15.847357, :lng -47.895831, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Central American Bank for Economic Integration BCIE", :lat 13.670971, :lng -89.247782, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "B.O.A.D West African Development Bank", :lat 6.182517, :lng 1.214651, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Intergovernmental Authority on Development (IGAD)", :lat 12.862807, :lng 30.21763599999997, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "SADC Southern African Development Community", :lat -19.015438, :lng 29.154856999999993, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "CEMAC Economic and Monetary Community of Central Africa", :lat -19.015438, :lng 29.154856999999993, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "OAS Organization of American States", :lat 13.670971, :lng -89.247782, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "East African Community (EAC)", :lat -0.023559, :lng 37.90619300000003, :iso-3361-3 nil}
                ; taken from https://de.wikipedia.org/wiki/Westerngrund --> This is valid before Brexit is executed!
   {:iso-3361-2 nil, :name "European Union", :lat 50.116389, :lng 9.250833, :iso-3361-3 nil}

                ; Other geographical entities
   {:iso-3361-2 "ES-CN", :name "Canary Islands", :lat 28.3430659, :lng -15.776367, :iso-3361-3 "ES-CN"}
   {:iso-3361-2 nil, :name "World", :lat 29.337706, :lng -37.69048, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Panama Canal Zone", :lat 8.953684, :lng -79.537618, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Oceania", :lat -18.3128, :lng 138.5156, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "North America (excl. USA)", :lat 69.235367, :lng -104.846433, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Leeward Islands", :lat 17.65, :lng -63.235, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Kuwaiti Oil Fires", :lat 29.311723, :lng 48.269737, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "International transport", :lat 35.179822, :lng -178.006764, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "French West Africa", :lat 16.004918, :lng -5.700104, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "French Equatorial Africa", :lat 6.884186, :lng 18.689544, :iso-3361-3 nil}
   {:iso-3361-2 "CX", :name "Christmas Island", :lat -10.491241, :lng 105.617352, :iso-3361-3 "CX"}
   {:iso-3361-2 "BQ", :name "Bonaire Sint Eustatius and Saba", :lat 12.178361, :lng -68.262383, :iso-3361-3 "BQ"}
   {:iso-3361-2 nil, :name "Asia (excl. China & India)", :lat 58.475307, :lng 96.810178, :iso-3361-3 nil}
   {:iso-3361-2 nil, :name "Ryukyu Islands", :lat 26.52, :lng 128.053, :iso-3361-3 nil}
   {:name "ignore"}])

(defn loc-id [lat lng]
  (cstr/prune
   (str "loc-" lat "-" lng)
   207))

(defn global-id [type name]
  (cstr/prune
   (str "context-" type "-" name)
   207))

(def countries-with-ids (mapv (fn [{:keys [name lat lng] :as desc}]
                                (->
                                 (assoc desc :global-id (global-id "country" name))
                                 (assoc :loc-id (loc-id lat lng))))
                              countries))

(def iso-3361-3->country (dissoc (reduce (fn [m country] (assoc m (:iso-3361-3 country) country)) {} countries) nil))
(def iso-3361-2->country (dissoc (reduce (fn [m country] (assoc m (:iso-3361-2 country) country)) {} countries) nil))
(def country->desc (dissoc (reduce (fn [m country] (assoc m (:name country) country)) {} countries-with-ids) nil))

(def country-mapping
  {"Palestinian" "Palestinian Territory, Occupied" ;k
   "Czech Rep." "Czech Republic" ;k
   "United Rep. of Tanzania" "Tanzania" ;k
   "Burkina-Faso" "Burkina Faso" ;k
   "Various/Unknown" "Unknown" ;k
   "Central African Rep." "Central African Republic" ;k
   "Cabo Verde" "Cape Verde" ;k
   "Bolivia (Plurinational State of)" "Bolivia" ;k
   "United States of America" "United States" ;k
   "Rep. of Moldova" "Moldova" ;k
   "The former Yugoslav Republic of Macedonia" "Macedonia";k
   "Micronesia (Federated States of)" "Federated States of Micronesia" ;k
   "Sint Maarten (Dutch part)" "Saint Martin (Dutch part)" ;k
   "China, Macao SAR" "Macao" ;k
   "Dem. People's Rep. of Korea" "North Korea" ;k
   "Philippines" "Phillipines" ;k
   "Syrian Arab Rep." "Syria" ;k
   "Timor-Leste" "Timor Leste" ;k
   "Dominican Rep." "Dominican Republic" ;k
   "Russian Federation" "Russia" ;k
   "Lao People's Dem. Rep." "Laos" ;k
   "Kyrgyzstan" "Kyrgystan" ;k
   "Iran (Islamic Rep. of)" "Iran" ;k
   "Comoros" "Comorros" ;k
   "Bosnia and Herzegovina" "Bosnia-Herzegovina" ;k
   "Rep. of Korea" "South Korea" ;k
   "Saint-Pierre-et-Miquelon" "Sankt Pierre und Miquelon" ;k
   "Wallis and Futuna Islands " "Wallis and Futuna Islands" ;k
   "Venezuela (Bolivarian Republic of)" "Venezuela" ;k
   "C├┤te d'Ivoire" "Cote D'ivoire" ;k
   "Serbia and Kosovo (S/RES/1244 (1999))" "Serbia"
   "#N/A" "Stateless" ;k
   "East Germany (GDR)" "Germany" ;approx
   "Brunei" "Brunei Darussalam" ;k
   "East Timor" "Timor Leste" ;k
   "Hong Kong" "China, Hong Kong SAR" ;k
   "New Hebrides" "Vanuatu" ;approx
   "St. Kitts and Nevis" "Saint Kitts and Nevis" ;k
   "Wallis and Futuna" "Wallis and Futuna Islands" ;k
   "Asian" "Asia" ;k
   "Soviet Union" "Russia" ;approx
   "Slovak Republic" "Slovakia" ;k
   "Vietnam" "Viet Nam" ;k
   "St. Martin" "Saint Martin (Dutch part)" ;approx
   "Northern Ireland" "Ireland" ;approx
   "South Vietnam" "Viet Nam" ;approx
   "St. Lucia" "Saint Lucia" ;k
   "West Bank and Gaza Strip" "Palestinian Territory, Occupied" ;approx
   "Rhodesia" "Zimbabwe" ;approx
   "South Yemen" "Yemen" ;approx
   "Ivory Coast" "Cote D'ivoire" ;k
   "Greenland" "Greenland" ;k
   "Sinhalese" "Sri Lanka" ;approx
   "Saba (Netherlands Antilles)" "Netherlands Antilles" ;k
   "North Yemen" "Yemen" ;approx
   "Yugoslavia" "Serbia" ;approx
   "Vatican City" "Holy See (the)" ;k
   "Czechoslovakia" "Slovakia" ;approx
   "Commonwealth of Independent States" "Russia" ;approx
   "West Germany (FRG)" "Germany" ;k
   "Serbia-Montenegro" "Serbia" ;approx
   "Macau" "Macao" ;k
   "Great Britain" "United Kingdom" ;k
   "Corsica" "France" ;approx
   "Man, Isle of" "Isle of Man" ;k
   "Various/unknown" "Unknown" ;k
   "R´┐¢union" "Réunion" ;k
   "USA (INS/DHS)" "United States" ;k
   "State of Palestine" "Palestinian Territory, Occupied" ;k
   "Palestine" "Palestinian Territory, Occupied" ;k
   "The former Yugoslav Rep. of Macedonia" "Macedonia" ;k
   "USA (EOIR)" "United States" ;k
   "C´┐¢te d'Ivoire" "Cote D'ivoire" ;k
   "Serbia and Kosovo: S/RES/1244 (1999)" "Kosovo" ;approx
   "United Kingdom of Great Britain and Northern Ireland" "United Kingdom" ;k
   "Cura´┐¢ao" "Cura├ºao" ;k
   "Kingdom of eSwatini (Swaziland)" "Swaziland" ;k
   "Cambodia (Kampuchea)" "Cambodia" ;k
   "Myanmar (Burma)" "Myanmar" ;k
   "Rumania" "Romania" ;k
   "Serbia (Yugoslavia)" "Serbia" ;k
   "Macedonia, FYR" "Macedonia" ;k
   "North Macedonia" "Macedonia" ;k
   "Russia (Soviet Union)" "Russia" ;k
   "Madagascar (Malagasy)" "Madagascar" ;k
   "Zimbabwe (Rhodesia)" "Zimbabwe" ;k
   "Yemen (North Yemen)" "Yemen" ;k
   "Mosambik" "Mozambique" ;k
   "Rum├ñnien" "Romania" ;k
   "Marokko" "Morocco" ;k
   "Kenia" "Kenya" ;k
   "Russ.F├Âderation" "Russia" ;k
   "Korea, Republik" "South Korea" ;k
   "Weissrussland" "Belarus" ;k
   "Kirgisistan" "Kyrgystan" ;k
   "ZAR" "Central African Republic"
   "Libanon" "Lebanon"
   "Brasilien" "Brazil"
   "Madagaskar" "Madagascar"
   "Mongolei" "Mongolia"
   "Georgien" "Georgia"
   "Papua-Neuguinea" "Papua New Guinea"
   "Usbekistan" "Uzbekistan"
   "Kroatien" "Croatia"
   "Tadschikistan" "Tajikistan"
   "Cote d'Ivoire" "Cote D'ivoire"
   "Sambia" "Gambia"
   "Albanien" "Albania"
   "Bangladesch" "Bangladesh"
   "Armenien" "Armenia"
   "Bulgarien" "Bulgaria"
   "Aserbaidschan" "Azerbaijan"
   "Kasachstan" "Kazakhstan"
   "Irak" "Iraq"
   "Philippinen" "Phillipines"
   "Serbien" "Serbia"
   "Tunesien" "Tunisia"
   "Indonesien" "Indonesia"
   "Jordanien" "Jordan"
   "Moldau" "Moldova"
   "Kambodscha" "Cambodia"
   "├ägypten" "Egypt"
   "S├╝dafrika" "South Africa"
   "Dschibuti" "Djibouti"
   "├äthiopien" "Ethiopia"
   "Ruanda" "Rwanda"
   "Jemen" "Yemen"
   "T├╝rkei" "Turkey"
   "S├╝dsudan" "Sudan"
   "Tschad" "Chad"
   "Mauretanien" "Mauritania"
   "Kolumbien" "Colombia"
   "Simbabwe" "Zimbabwe"
   "Indien" "India"
   "Libyen" "Libya"
   "Tansania" "Tanzania"
   "Domin. Republik" "Dominican Republic"
   "Nordmazedonien" "Macedonia"
   "Bolivien" "Bolivia"
   "Bosnien-Herzeg." "Bosnia-Herzegovina"
   "Syrien" "Syria"
   "Kamerun" "Cameroon"
   "Asien NA" "Asia"
   "CTPT" "CTPT Trinitarian Commission of the Plan Trifinio"
   "O.M.V.G." "The Gambia River Basin Development Organization (O.M.V.G.)"
   "Lateinam. NA" "Latin America"
   "CAF (Andenpakt)" "CAF (Andean Pact)"
   "Fondo Indigena" "Fund for the Development of Indigenous Peoples of Latin America and the Caribbean"
   "COMIFAC" "COMIFAC Central African Forests Commission"
   "S├╝dosteuropa" "Southeastern Europe"
   "Kaukasus NA" "Caucasus"
   "SICA" "SICA Central American Integration System"
   "SPC" "SPC Secretariate of the Pacific Community"
   "M.R.C. (Asien)" "M.R.C. Mekong River Commission (Asia)"
   "MENA NA" "MENA NA The region of North Africa and the Middle East"
   "Afrikan. Union" "African Union"
   "CEPGL" "CEPGL Economic Community of the Great Lakes Countries"
   "ACB" "ACB Asean Centre for Biodiversity"
   "ECOWAS" "ECOWAS Economic Community of West African States"
   "CARICOM" "CARICOM Caribbean Community"
   "Pal├ñst.Gebiete" "Palestinian Territory, Occupied"
   "N/Mittl. Osten" "United States"
   "MOE" "MOE/SOE Central Eastern European and South Eastern European"
   "IGAD" "Intergovernmental Authority on Development (IGAD)"
   "S.A.D.C." "SADC Southern African Development Community"
   "E.A.C." "East African Community (EAC)"
   "BCIE-Zentram.Eb" "Central American Bank for Economic Integration BCIE"
   "MOE/SOE NA" "MOE/SOE Central Eastern European and South Eastern European"
   "Afrika NA" "African Union"
   "ABN" "ABN Niger Basin Authority"
   "B.O.A.D." "B.O.A.D West African Development Bank"
   "OAS" "OAS Organization of American States"
   "OTCA" "OTCA Amazon Cooperation Treaty Organization"
   "CCAD" "Central American Commission for Environment and Development (CCAD)"
   "CEMAC" "CEMAC Economic and Monetary Community of Central Africa" ;k
   "China, People's Republic of" "China"
   "Kyrgyz Republic" "Kyrgystan"
   "Lao People's Democratic Republic" "Lao People's Dem. Rep."
   "Micronesia, Federated States of" "Micronesia (Federated States of)"
   "USA" "United States"
   "Britain" "United Kingdom"
   "Trinidad-Tobago" "Trinidad and Tobago"
   "UAE" "United Arab Emirates"
   "Sao Tome" "Sao Tome and Principe"
   "Bosnia" "Bosnia-Herzegovina"
   "Afghanistan, I.R. of" "Afghanistan"
   "Azerbaijan, Rep. of" "Azerbaijan"
   "Bahamas, The" "Bahamas"
   "Bahrain, Kingdom of" "Bahrain"
   "China,P.R.: Mainland" "China"
   "China,P.R.:Hong Kong" "China, Hong Kong SAR"
   "China,P.R.:Macao" "Macao"
   "Côte d'Ivoire" "Cote D'ivoire"
   "Curacao" "Curaçao"
   "French Territories: French Polynesia" "French Polynesia"
   "French Territories: New Caledonia" "New Caledonia"
   "Gambia, The" "Gambia"
   "Iran, I.R. of" "Iran"
   "Korea, Republic of" "South Korea"
   "Lao People's Dem.Rep" "Laos"
   "Marshall Islands, Republic of" "Marshall Islands"
   "São Tomé & Príncipe" "Sao Tome and Principe"
   "Serbia and Montenegro" "Serbia" ;approx
   "Serbia, Republic of" "Serbia"
   "Sint Maarten" "Saint Martin (Dutch part)"
   "South Sudan, Rep. of" "South Sudan"
   "St. Vincent & Grens." "Saint Vincent and the Grenadines"
   "Syrian Arab Republic" "Syria"
   "Taiwan Prov.of China" "Taiwan"
   "Vatican" "Holy See (the)"
   "Venezuela, Rep. Bol." "Venezuela"
   "West Bank and Gaza" "Palestinian Territory, Occupied"
   "Yemen, Republic of" "Yemen"
   "Africa not specified" "Africa"
   "Asia not specified" "Asia"
   "Europe not specified" "Europe"
   "Middle East not specified" "Middle East"
   "Middle East " "Middle East"
   "Western Hemisphere not specified" "Europe" ;approx
   "Countries and Areas not specified" "Unknown" ;approx
   "Special Categories" "International" ;approx
   "Европейский Союз" "European Union"
   "St Helena" "Saint Helena, Ascension and Tristan da Cunha"
   "Reunion" "Réunion"
   "Hong Kong (China)" "China, Hong Kong SAR"
   "Korea, Democratic People's Republic" "North Korea"
   "China, People's Republic" "China"
   "Libyan Arab Jamah" "Libya"
   "Syrian Arab Rep" "Syria"
   "Korea Republic" "South Korea"
   "Iran, Islamic Republic" "Iran"
   "Trinidad & Tobago" "Trinidad and Tobago"
   "Guinea Bissau" "Guinea-Bissau"
   "Antigua & Barbuda" "Antigua and Barbuda"
   "St Kitts & Nevis" "Saint Kitts and Nevis"
   "Taiwan (China)" "Taiwan"
   "Micronesia, Federated States" "Federated States of Micronesia"
   "Saint Vincent & The Grenadines" "Saint Vincent and the Grenadines"
   "Palestine (West Bank, Gaza)" "Palestinian Territory, Occupied"
   "Czech Rep" "Czech Republic"
   "Virgin Islands (US)" "US Virgin Islands"
   "Czechia" "Czech Republic"
   "Eswatini" "Swaziland"
   "eSwatini" "Swaziland"
   "Israel and West Bank" "Israel" ; approx
   "Micronesia" "Federated States of Micronesia"
   "Bundesrepublik Deutschland" "Germany"
   "M├®xico" "Mexico"

   ;Congo related mapping
   "Congo, Republic" "Democratic Republic of Congo"
   "Congo Democratic Republic" "Democratic Republic of Congo"
   "Democratic Republic of Congo" "Democratic Republic of Congo"
   "Zaire/Congo, Dem Rep (PREVIOUS)" "Democratic Republic of Congo"
   "Congo, Dem. Rep. of" "Democratic Republic of Congo"
   "Kongo" "Democratic Republic of Congo"
   "Kongo, Dem. Re." "Democratic Republic of Congo"
   "DR Congo (Zaire)" "Democratic Republic of Congo" ;k
   "Dem. Rep. of the Congo" "Democratic Republic of Congo" ;k
   "The Democratic Republic of the Congo" "Democratic Republic of Congo" ;k
   "Zaire" "Democratic Republic of Congo" ;k
   "Congo" "Democratic Republic of Congo"
   "The Democratic Republic of Congo" "Democratic Republic of Congo"
   "Congo Republic" "Republic of Congo"
   "Congo, Republic of" "Republic of Congo"
   "Republic of the Congo" "Republic of Congo"
   "People's Republic of the Congo" "Republic of Congo"})

(defn lookup [country-name]
  (if (nil? country-name) nil
      (let [desc (country->desc country-name)
            country-mapping-name (country-mapping country-name)
            result-desc
            (cond desc
                  desc
                  country-mapping-name
                  (country->desc country-mapping-name))]
        result-desc)))

(defn center-for-name [country-name]
  (when-let [country-desc (lookup country-name)]
    (let [{:keys [lat lng]} country-desc]
      [lat lng])))