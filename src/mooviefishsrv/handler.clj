(ns mooviefishsrv.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [mooviefishsrv.routes.home :refer [home-routes]]
            [mooviefishsrv.models.db :refer [load-movies-data store-movies-data]]))

(defn init []
  (println "mooviefishsrv is starting")
  (println "CWD: " (System/getProperty "user.dir"))
  (load-movies-data))

(defn destroy []
  (println "mooviefishsrv is shutting down"))
  ;(store-movies-data))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes app-routes)
      (handler/site)
      (wrap-base-url)))


