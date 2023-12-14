(ns de.explorama.frontend.map.utils
  (:require [cljs.tools.reader.edn :refer [read-string]]
            [clojure.string :as str]
            [de.explorama.frontend.map.configs.util :refer [translated-layer->raw]]
            [de.explorama.frontend.map.paths :as geop]
            [goog.crypt]
            [goog.crypt.Md5]
            [goog.string]
            [goog.string.format]))

(defn string->md5-hex
  [s]
  (goog.crypt/byteArrayToHex
   (let [md5 (goog.crypt.Md5.)]
     (.update md5 (goog.crypt/stringToUtf8ByteArray s))
     (.digest md5))))

(defn- short-name- [name]
  (if (str/includes? name "-")
    (let [split-names (map first (str/split name #"-"))]
      (str (str/join ". " split-names) "."))
    (str (first name) ".")))

(defn shorten-name [name]
  (let [split-name (str/split name #" ")
        names (drop-last split-name)
        surname (peek split-name)
        short-name (str/join " "
                             (map short-name- names))]
    (str short-name " " surname)))

(defn rgb-hex-parser
  "Parse a hex-string to a vector with numbers
   String will be splitted in Pairs
   example Input/Output: '#ffffff' -> [255 255 255]"
  [hex-string]
  (if (or (= "#fff" hex-string)
          (nil? hex-string))
    [255 255 255]
    (let [string (str/replace hex-string #"#" "")
          [r g b] (re-seq #".{1,2}" string)]
      [(read-string (str "0x" r))
       (read-string (str "0x" g))
       (read-string (str "0x" b))])))

(defn rgb->hex
  "Convert an RGB color map to a hexadecimal color."
  [[r g b]]
  (letfn [(hex-part [v]
                    (-> (goog.string/format "%2s" (.toString v 16))
                        (str/replace " " "0")))]
    (apply str "#" (map hex-part [r g b]))))

(defn font-color
  [color]
  (let [red (js/parseInt (subs color 1 3) 16)
        green (js/parseInt (subs color 3 5) 16)
        blue (js/parseInt (subs color 5 7) 16)
        brightness (+ (* red 0.299) (* green 0.587) (* blue 0.114))]
    (if (< 180 brightness)
      "#000000"
      "#FFFFFF")))

(defn marker-layout-id->desc [db frame-id layout-id]
  (let [selected-layouts (get-in db (geop/selected-marker-layouts frame-id))
        raw-marker-layouts (get-in db geop/raw-marker-layouts)
        temp-marker-layouts (get-in db (geop/temp-raw-marker-layouts frame-id))]
    (or (get raw-marker-layouts layout-id)
        (get temp-marker-layouts layout-id)
        (translated-layer->raw (get selected-layouts layout-id)))))

(defn feature-layer-id->desc [db frame-id feature-layer-id]
  (let [selected-feature-layers (get-in db (geop/selected-feature-layers frame-id))
        raw-feature-layers (get-in db geop/raw-feature-layers)
        temp-feature-layers (get-in db (geop/temp-raw-feature-layers frame-id))]
    (or (get raw-feature-layers feature-layer-id)
        (get temp-feature-layers feature-layer-id)
        (translated-layer->raw (get selected-feature-layers feature-layer-id)))))

         