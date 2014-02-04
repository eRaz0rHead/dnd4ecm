(ns dndscraper.util
  (:import (java.nio.file Path PathMatcher FileSystems)))
  
(def fs (FileSystems/getDefault ))

(defn parse-int [s]
  (Integer. (re-find  #"\d+" s )))

; U+00A0
 (defn compress-whitespace [s] 
  (clojure.string/replace  s #"[\p{Z}\s]+"  " " ))
 
 
(def match-xml
  (.getPathMatcher fs "glob:**.xml"))

(defn xmlf? [f]
  (.matches match-xml (.toPath f)))

(defn local-file [type id]
  (clojure.java.io/file "resources/saved/" type  (str id ".html")))

(defn local-type-list []
  (let [directory (clojure.java.io/file "resources/")]
    (map (fn [f] (first (clojure.string/split (.getName f) #"\." ))) (filter xmlf? (.listFiles directory)))))






(defn fetch-data [url out-file]
  (let  [con    (-> url java.net.URL. .openConnection)
         fields (reduce (fn [h v] 
                          (assoc h (.getKey v) (into [] (.getValue v))))
                        {} (.getHeaderFields con))
         size   (first (fields "Content-Length"))
         in     (java.io.BufferedInputStream. (.getInputStream con))
         out    (java.io.BufferedOutputStream. 
                 (java.io.FileOutputStream. out-file))
         buffer (make-array Byte/TYPE 1024)]
    (prn fields)
    (loop [g (.read in buffer)
           r 0]
      (if-not (= g -1)
        (do
          ;(println r "/" size)
          (.write out buffer 0 g)
          (recur (.read in buffer) (+ r g)))))
    (.close in)
    (.close out)
    (.disconnect con)))



