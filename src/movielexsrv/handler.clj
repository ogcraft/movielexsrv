(ns movielexsrv.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [liberator.core :refer [defresource resource]]
            [liberator.dev :as ldev]
            [movielexsrv.routes.home :refer [public-routes secured-routes movielexapp-routes]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [movielexsrv.models.db :as db]
            [movielexsrv.admins :as admins :refer (admins)]
            [hiccup.core :as h]))

(defn init []
  (println "movielexsrv is starting")
  (println "CWD: " (System/getProperty "user.dir"))
  (db/load-host-config)
  (db/connect-riak)
  (db/users-bucket-create)
  (db/movies-bucket-create)
  (db/votes-bucket-create))
  ;(db/load-movies-data))
  
(defn destroy []
  (println "movielexsrv is shutting down"))
  
;(defroutes app-routes
;  (route/resources "/")
;  (route/not-found "Not Found"))

(def app
  (-> (routes public-routes movielexapp-routes secured-routes)
      (handler/site)
      (wrap-base-url)))


