(ns dnd.server.core

  (:require [ring.util.response :refer [file-response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [compojure.core :refer [defroutes GET PUT]]
            [compojure.route :as route]
            [compojure.handler :as handler]

            )
)


(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})



(defn index []
  (file-response "index.html" {:root "web"}))



(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})


(defroutes routes
  (GET "/" [] (index))
  ;(GET "/classes" [] (classes))
  ;(PUT "/class/:id/update"
  ;  {params :params edn-params :edn-params}
  ;  (update-class (:id params) edn-params))
  (route/files "/" {:root "resources/public"}))

(def app
  (-> routes
      wrap-edn-params))

(defonce server
  (run-jetty #'app {:port 8080 :join? false}))

