(ns dnd.format.dnd-cm
 (:require [net.cgrand.enlive-html :as e]
           [instaparse.core :as insta]
           [dndscraper.html :as dh]
           [dndscraper.util :as util]))



 (defn load-encounter [encounter]
  (e/xml-resource (clojure.java.io/file  "resources/encounters/" (str encounter ".xml"))))

 (def example (load-encounter "Example"))

   (map :tag (e/select example [:statblock ] ) )
