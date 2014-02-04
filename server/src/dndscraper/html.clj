(ns dndscraper.html
 (:require [net.cgrand.enlive-html :as html]
           [dndscraper.util :only local-file]))


(defn res [type id]
  (html/html-resource (dndscraper.util/local-file type id) ))

(defn detail [type id]
 (html/select (res type id) [:div#detail ]))
