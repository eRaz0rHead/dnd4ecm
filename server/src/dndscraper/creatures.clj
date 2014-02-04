(ns dndscraper.creatures
 (:require [net.cgrand.enlive-html :as e]
           [instaparse.core :as insta]
           [dndscraper.html :as dh]))
  

 (defn extract-attacks [s]
   (re-seq #": (\w+) vs. (\w+)" s))
 
 
   


 
 
