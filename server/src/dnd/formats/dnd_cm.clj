(ns dnd.formats.dnd-cm
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



(defn split-action [node]
  (when-let [both (e/text node)]
    (let  [[act usage] (split both #";")
           usage (if (nil? usage) "" usage)]

      (e/html
       [:action [:type act] [:usage (.trim usage)]]
       )
      )))

(defn change-tag [tag]
  (e/do-> e/unwrap (e/wrap tag)
          ))


(defn match-tag [regex]
  (e/pred #(re-matches regex (name (:tag %))))
  )


(defn regex-tag [regex node]
  (let [s (name (:tag node))
        m (re-matcher regex s)]
    (second (re-matches regex s)))
  )

(defn regex-change-tags [re ]
  #(let [tag       (regex-tag re %)
         [content] (:content %)]
     (e/html
      [(keyword tag) content]
      )
     ))


(pp/pprint
 (e/at example
       [(match-tag #"s_(.*)")] (regex-change-tags #"s_(.*)")
       )
 )


(defn transform [enc]
  (e/at enc
        {[:d_ac] [:d_will]} (e/wrap :defenses)
        {[:s_str] [:s_cha]} (e/wrap :abilities)
        [:abilities] (e/wrap :stats)
        [(match-tag #"s_(.*)")] (regex-change-tags #"s_(.*)")
        [(match-tag #"d_(.*)")] (regex-change-tags #"d_(.*)")

        [:max_hp] (change-tag :max-hps)
        [:init] (change-tag :init-bonus)

        [:act] split-action
        )
  )


;;;;;;; RANDOM Testing stuff


(def transformed-example (transform example))
(pp/pprint (map x/pivot transformed-example))
;(pp/pprint
(group-by #(-> %  :power :action :type)
          (map x/pivot  (e/select transformed-example [:power]))
          )
;)
(pp/pprint
 (cbt/combatant
  (->
   (x/pivot (first (e/select transformed-example [:combatant])))
   :combatant :statblock
   )
  ))

;(spit "compare.xml" (export-encounter (import-encounter "Example")))

