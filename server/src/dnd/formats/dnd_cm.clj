(ns dnd.formats.dnd-cm
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip :as zf]
            [net.cgrand.enlive-html :as e]

            [dndscraper.xml :as x]
            [clojure.pprint :as pp]

            )
  (:use [clojure.data.zip.xml] ))



(defn read-encounter [encounter]
  (first (zip/xml-zip (xml/parse (str "resources/encounters/" encounter ".xml")))))


(defn import-encounter [encounter]
  (x/pivot (read-encounter encounter)))

(defn export-encounter [encounter]
  (apply str (e/emit* (e/html (x/unpivot encounter))))
  )


;  (def example  (load-encounter "Example"))
;  (pp/pprint  example)
;  (-> example :encounter :combatant count)
;  (pp/pprint (unpivot (load-encounter "Example")))
;  (spit "compare.xml" (to-str (load-encounter "Example")))
