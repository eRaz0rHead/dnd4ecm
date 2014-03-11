(ns dnd.data.combatant
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip :as zf]
            [net.cgrand.enlive-html :as e]
            [dndscraper.xml :as x]
            [clojure.pprint :as pp]
            [dndscraper.util :as u]
            )
  )

(use 'clojure.reflect)

(defrecord Power [id name action usage type keywords description url
                  ;; TODO - add decision-tree of Attack/Effect/Hit etc from parsing description.
                  ])

(defrecord AbilityScores [strength dexterity constitution intelligence wisdom charisma])

(defrecord Stats [^AbilityScores abilities level role speed regen resist vuln immune ])

(defrecord Combatant
  [id
   name
   ^Stats stats
   defenses
   max-hps
   current-hps
   temp-hps
   init-bonus
   init-roll
   init
   powers
   effects
   ; other ; to hold information otherwise lost during round-tripping
   ])



(defn basis [m]
  (map keyword (.. m (getMethod "getBasis" nil) (invoke nil nil))))


(defn sel-record [m rec]
  (select-keys m (basis rec))
  )


(defn collapse [m ks]
  (first
   (set
    (map u/parse-int
         (remove nil? ((apply juxt ks) m))
     ))))

(defn h-abilities [a]
  {:strength  (collapse a [:strength :str :STR  :Strength])
   :dexterity  (collapse a [:dexterity :dex :DEX :Dexterity])
   :constitution (collapse a [:constitution :con :CON :Constitution])
   :intelligence (collapse a [:intelligence :int :INT :Intelligence])
   :wisdom (collapse a [:wisdom :wis :WIS :Wisdom])
   :charisma (collapse a [:charisma :cha :CHA :Charisma])
   })

(defn to-abilities [a]
  (cond
   (sequential? a) (apply ->AbilityScores a)
   (map? a)        (map->AbilityScores (h-abilities a))
   ))

(defn combatant [m]
  (let [s         (:stats m)
        a         (:abilities s )
        abilities (to-abilities a)
        stats     (merge s (sel-record m Stats) {:abilities abilities})
        powers    (:power m)
        remaining (apply dissoc m (flatten [(basis AbilityScores)(basis Combatant)(basis Stats) :power]))
        ]
    (map->Combatant
     (merge remaining
            (sel-record m Combatant)
            {:stats stats :powers powers}
            ))))


;Griff

