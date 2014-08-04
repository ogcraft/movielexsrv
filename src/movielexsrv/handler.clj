(ns movielexsrv.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [movielexsrv.routes.home :refer [home-routes]]
            [movielexsrv.models.db :as db]))

(defn init []
  (println "movielexsrv is starting")
  (println "CWD: " (System/getProperty "user.dir"))
  (db/load-host-config)
  (db/connect-riak)
  (db/users-bucket-create)
  (println "users-bucket-create")
  (db/votes-bucket-create)
  (db/load-movies-data))
  
(defn destroy []
  (println "movielexsrv is shutting down"))
  
(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes app-routes)
      (handler/site)
      (wrap-base-url)))


