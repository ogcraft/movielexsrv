(ns movielexsrv.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [liberator.core :refer [defresource resource]]
            [liberator.dev :as ldev]
            [movielexsrv.routes.home :refer [public-routes private-routes private-routes1 movielexapp-routes]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [movielexsrv.models.db :as db]))

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

; a dummy in-memory user "database"
(def admins {"ogcraft" {:username "ogcraft"
                        :password (creds/hash-bcrypt "ogcraft")
                        :roles    #{::admin}}
             "pavela"  {:username "pavela"
                        :password (creds/hash-bcrypt "pavela")
                        :roles    #{::admin}}})

(def app
  (-> (routes public-routes private-routes private-routes1 movielexapp-routes)
      (handler/site)
      (wrap-base-url)))


