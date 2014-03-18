(ns dndscraper.pcs
 (:require [net.cgrand.enlive-html :as e]
           [dndscraper.util :as u]
           [dndscraper.core :as core]))

(defn pc-file [char]
  (e/html-resource (clojure.java.io/file  "resources/example-chars/" (str char ".dnd4e"))))


(defn de-amp [url]
  (clojure.string/replace url "&amp;" "&"))


(def fix-url
 "fix the url to be certain it links correctly."
  #(let [name (:name (:attrs % ))
         url  (:url (:attrs % ))
         newAttrs ( assoc (:attrs % {}) :url (core/fix-link name (de-amp url)))  ]
       (assoc % :attrs newAttrs)
     ))

(defn render
  [nodes]
  (apply str (e/emit* nodes)))

(defn fixed-items [char-name]
   (e/at (pc-file char-name) [:loot [:RulesElement (e/attr? :url)]]  fix-url ))

(defn fix-n-save [char-name]
  (spit  (str "resources/fixed-chars/" char-name ".dnd4e")
         (render (fixed-items char-name))))


(defn attr [node name]
    (get (:attrs node) name))



;; TODO -- use enlive here instead.
(defn content[x]
  (clojure.string/trim  (first (:content x))) )

;(def mba (first ( powerlist "Arranais"  )))


(defn stat-atts [char]
  (map (fn [x]
         { :name  (map  #(attr % :name) (e/select x [:alias]) )
           :value (u/parse-int (clojure.string/trim (e/attr-values x :value))) } )
       (e/select (pc-file char) [:Stat])))



(stat-atts "Arranais")

(defn weapon-atts [a-weapon]
  (let [m { :Weapon (attr a-weapon :name) }]
    (merge m
           (into {} (map
                     #(when (:content %)  { (:tag %) (content %) } )
                     (e/select  a-weapon [ :Weapon e/any ]))
                 ))))


(defn power-atts [node]
  (let [m { :Power (attr node :name) }]
    (merge m
           (into {} (map
                     #(when (:content % ) { (u/safekw (attr % :name)) (content %) })
                     (e/select  node [:specific])))
           { :Weapons (map weapon-atts (e/select node [:Weapon])) }
           )))

; (power-atts (first (e/select (pc-file "Arranais")  [:Power])))


(defn stats [char]
  (apply merge
     (flatten
       (map
         (fn [m]
           (let [alias-list (:name m)]
             (for [nm alias-list]
	              { (u/safekw nm)  (:value m)})))  (stat-atts char)))))


(defn powers [char]
  (map power-atts (e/select (pc-file char)  [:Power])))

(defn print-powers [character]
  (for [power (powers character)]
    (apply str (flatten (apply conj [] ["Power : " (:Power power) " "]
                               (for [w  (:Weapons  power) ]
                                 ["{"(:Weapon w) ": +"  (:AttackBonus w) " vs. "  (:Defense w)
                                  ", Damage :"  (:Damage w)
                                  "}\n"]
                                 )
                               [(when (:Hit power )  (str "\nHit:" (:Hit power)))]
                               [(when (:Effect power ) (str "\nEffect:" (:Effect power)))]
                               ["\n"]
                               )))))


;(powers "Arranais")

(clojure.pprint/pprint
 (print-powers "GaranKel"))

