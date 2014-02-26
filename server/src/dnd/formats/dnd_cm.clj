(ns dnd.formats.dnd-cm
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip :as zf]
            [net.cgrand.enlive-html :as e]

            [dndscraper.xml :as x]
            [clojure.pprint :as pp]


            )
  (:use [clojure.data.zip.xml]
        [clojure.string :only [split]]))



(defn read-encounter [encounter]
  (first (zip/xml-zip (xml/parse (str "resources/encounters/" encounter ".xml")))))

(defn import-encounter [encounter]
  (x/pivot (read-encounter encounter)))

(defn export-encounter [encounter]
  (apply str (e/emit* (e/html (x/unpivot encounter))))
  )






(def example  (read-encounter "Example"))

;; NEXT STEP - actually convert maps ..
;; consider using enlive/at to transform the XML before using pivot?
;; compare with traversing post-pivoted map with zipper, or other recursion.

(defn regex-tag [regex]
  (e/pred #(re-matches regex (name (:tag %))))
  )


(defn split-action [node]
  (when-let [both (e/text node)]
    (let  [[act usage] (split both #";")
           usage (if (nil? usage) "" usage)]

      (e/html
       [:action [:type act] [:usage (.trim usage)]]
       )
      )))





(def transformed-example
 (e/at example
       {[:d_ac] [:d_will]} (e/wrap :defs)
       {[:s_str] [:s_cha]} (e/wrap :stats)
       [:act] split-action
       )
 )
; (set ( map :tag (e/select example [:power :> e/any])))
;  (set (e/select example [:power :act]))

;(split-action (first (e/select example [:power :act])))

;(-> example :encounter :combatant count)


;(pp/pprint (x/unpivot (import-encounter "Example")))


(pp/pprint (x/pivot (first transformed-example)))
;(spit "compare.xml" (export-encounter (import-encounter "Example")))

