(ns dnd.formats.adv-tools
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip :as zf]
            [net.cgrand.enlive-html :as e]

            [dndscraper.xml :as x]
            [clojure.pprint :as pp]
            [dnd.data.combatant :as cbt]

            )
  (:use [clojure.data.zip.xml]
        [clojure.string :only [split]]))


(defn read-monster [monster]
  (zip/xml-zip (xml/parse (str "resources/monsters/" monster ".monster"))))

(def Vaerlan (read-monster "Vaerlan"))

;Vaerlan

(defn transform [monster]
  (e/at monster
         [(e/attr? :FinalValue)]  #(assoc % :content
                                     (concat (:content %)
                                             (e/html [:FinalValue  (:FinalValue (:attrs %))])))
        ))

;(pp/pprint (transform Vaerlan))

;(pp/pprint (map x/pivot (transform Vaerlan)))
