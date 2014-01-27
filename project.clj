(defproject om-test "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138" ]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha" ]

                 [om "0.3.0"]
                 [sablono "0.1.6"]
                 [com.facebook/react "0.8.0.1"]
                 ]

  :plugins [[lein-cljsbuild "1.0.1"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "om-test"
              :source-paths ["src"]
              :compiler {
                :output-to "web/main.js"
                :output-dir "web/out"
                :source-map "web/main.js.map"
                :optimizations :none
             }}]}
  )







