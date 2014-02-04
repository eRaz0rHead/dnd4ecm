(ns dndscraper.items
 (:require [net.cgrand.enlive-html :as html]
           [dndscraper.html :as dh]))


(defn ^:private  item-col [node i]
  (html/select node [:.magicitem  (html/attr= :class (str "mic" i ))]))

 (defn ^:private  tflat- [type id]
   (remove empty?
     (for [i (range 1 6) ]
      (flatten (remove clojure.string/blank? (map #(first (:content %)) (item-col (dh/detail type id) i)))))))

 (defn  ^:private  item-lvl-header- [ id]
   (apply :content (html/select (dh/detail "item" id) [:#headerlevel])))

 (defn ^:private  item-lvl-table- [id]
   (let [l (tflat- "item" id)]
     (if (empty? l) nil
       (sort-by first (let [ss (map seq l)]
          (apply map vector ss))))))

 (defn item-levels [id]
   (let [tbl (item-lvl-table- id)]
     (if (nil? tbl)
       (item-lvl-header- id)
       tbl)))
 