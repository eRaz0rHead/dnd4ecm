(ns dndscraper.xml
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip :as zf]
            [dndscraper.util :as util])
  (:use [clojure.data.zip.xml] ))




(defn- merge-fn [a b]
  (if (vector? a)
    (conj a b)
    [a b]
    ))


;; TODO -- Use insertion order for maps... consider https://github.com/flatland/ordered
(defn pivot [node]
  "Recurses through nodes produced through zip-xml,
  changing :tag into keys, and :content into values.
  Does not handle Attributes."
  (if (string? node)
    node
    (let [{:keys [tag attrs content]} node]
      (let [items (map pivot content)]
        {  tag (apply merge-with merge-fn items)}
        )) ))


(defn unpivot [node]
  "Recurses through a map and generates another map where :keys become
  :tag 'key' and values become :content (value).xml.
  Does not handle Attributes."
  (cond
   (string? node) node
   (vector? node) (vec (map unpivot node))
   (map? node) (map (fn [[k v]]
                      (if (sequential? v)
                        (map #(vector %1 %2) (repeatedly (fn [] k)) (unpivot v))
                        [k (unpivot v)]
                        )
                      )
                    node)
   ))



(defn zip-resource [type]
  (zip/xml-zip (xml/parse (str "resources/" type ".xml"))))

(defn id-for-name [n type]
  (xml1-> (zip-resource type) zf/descendants :Name (text= n ) zf/left-locs :ID text))

(defn all-ids [type]
  (map util/parse-int (xml-> (zip-resource type) zf/descendants  :ID text)))

 (defn all-conditions []
  (xml-> (zip-resource "Glossary") zf/descendants :Type (text= "Rules Condition") zf/left-locs :Name text))


(defn count-by-type [type]
  (count (all-ids type)))

(defn fname [char]
  (str "resources/example-chars/" char ".dnd4e"))
 ;;;;
 (defn zip-character [char]
  (zip/xml-zip (xml/parse (fname char))))


(defn all-loot [char]
   (xml-> (zip-character char)  :CharacterSheet :LootTally :loot :RulesElement ))


(defn print-loot [char]
  (for [loot (all-loot char)
        :let [name (attr loot :name)
              url  (attr loot :url) ]]
    {:name name
     :url url }))

