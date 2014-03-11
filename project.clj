(defproject om-test "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156" ]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha" ]

                 [org.clojure/data.zip "0.1.1"]
                 [instaparse "1.2.14"]
                 [enlive "1.1.5"]
                 [clj-webdriver "0.6.0"]
                 [korma "0.3.0-RC6"]

                 [om "0.3.6"]
                 [sablono "0.2.6"]
                 [com.facebook/react "0.8.0.1"]

                 [ring/ring-core "1.2.1"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 ]

  :plugins [[lein-cljsbuild "1.0.1"]]

  :source-paths ["src" "server/src"]

  :cljsbuild {
    :builds [{:id "om-dnd"
              :source-paths ["src"]
              :compiler {
                :output-to "web/main.js"
                :output-dir "web/out"
                :source-map "web/main.js.map"
                :optimizations :none
             }}]}
  )







