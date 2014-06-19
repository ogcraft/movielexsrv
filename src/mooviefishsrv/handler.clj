(ns mooviefishsrv.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [mooviefishsrv.routes.home :refer [home-routes]]
            [mooviefishsrv.models.db :as db]))

(defn init []
  (println "mooviefishsrv is starting")
  (println "CWD: " (System/getProperty "user.dir"))
  (db/load-movies-data)
  (db/load-users-data))

(defn destroy []
  (println "mooviefishsrv is shutting down")
  (db/store-users-data))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes app-routes)
      (handler/site)
      (wrap-base-url)))


