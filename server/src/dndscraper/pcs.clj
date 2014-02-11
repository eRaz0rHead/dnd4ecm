(ns dndscraper.pcs
 (:require [net.cgrand.enlive-html :as e]
           [dndscraper.util :only local-file]
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
   (e/at (pc-file char-name) [:loot :RulesElement]   fix-url ))

(defn fix-n-save [char-name]
  (spit  (str "resources/fixed-chars/" char-name ".dnd4e")
         (render (fixed-items char-name))))


(defn attr [node name]
    (get (:attrs node) name))


(defn content[x]
  (clojure.string/trim  (first (:content x))) )

;(def mba (first ( powerlist "Arranais"  )))

(defn safekw [s]
  (keyword (clojure.string/replace (clojure.string/trim s) " " "_")))

(defn stat-atts [char]
  (for [x  (e/select (pc-file char) [:Stat]) ]
    {
     :name  (map (fn [x] (attr x :name))  (e/select x [:alias]) )
     :value (clojure.string/trim (e/attr-values x :value))
     }
    ))

(defn weapon-atts [one-weapon]
  (let [m { :Weapon (attr one-weapon :name) }]
    (merge m
           (apply merge
                  (for [x  (e/select  one-weapon [ :Weapon e/any ]) ]
                    (when (:content x)  { (:tag x) (content x) } )
                    )))))

(defn power-atts [node]
  (let [m { :Power (attr node :name) }]
    (merge m
           (apply merge
                  (for [x  (e/select  node [:specific]) ]
                    (when (:content x ) { (safekw (attr x :name)) (content x) })
                  ))
           { :Weapons (map weapon-atts (e/select node [:Weapon])) }
           )))

(defn stats [char]
  (apply merge
     (flatten
       (map
         (fn [m]
           (let [alias-list (:name m)]
             (for [nm alias-list]
	              { (safekw nm) (first (:value m))})))  (stat-atts char)))))


(defn powers [char]
  (for [p (e/select (pc-file char)  [:Power])]
     (power-atts p)))


(for [power (powers "Arranais")]
  (prn "Power : " (:Power power)
     (for [w  (:Weapons  power) ]
         (str (:Weapon w) ":  +"  (:AttackBonus w) " vs. "  (:Defense w)
              " Damage :"  (:Damage w)
              "\n")
         )
      (when (:Hit power )  (str "Hit:" (:Hit power)))
      (when (:Effect power ) (str "Effect:" (:Effect power)))))


(powers "Arranais")

