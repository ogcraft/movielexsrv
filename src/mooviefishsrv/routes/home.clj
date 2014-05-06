(ns mooviefishsrv.routes.home  
  (:require [compojure.core :refer :all]
            [liberator.core :refer [defresource resource]]
            [cheshire.core :refer [generate-string]]
            [noir.io :as io]
            [clojure.java.io :refer [file]]
            [mooviefishsrv.models.db :as db]))

(defresource home-txt
  :service-available? true
  :handle-ok "Welcome to MoovieFish!"
  :etag "fixed-etag" 
  :available-media-types ["text/plain"])

(defresource get-movies [lang]
  :allowed-methods [:get]
  :handle-ok (fn [ _ ] 
    (println "get-movies :handle-ok lang: " lang) 
    (generate-string 
      (db/movie-list))) 
  :available-media-types ["application/json"])

(defresource get-movies-html
    :available-media-types ["text/html"]

    :exists?
    (fn [context]
      [(io/get-resource "/movie-list.html")
       {::file (file (str (io/resource-path) "/movie-list.html"))}])

    :handle-ok
    (fn [{{{resource :resource} :route-params} :request}]
      (println "get-movies-html :handle-ok")
      (clojure.java.io/input-stream (io/get-resource "/movie-list.html")))
    :last-modified
    (fn [{{{resource :resource} :route-params} :request}]
      (.lastModified (file (str (io/resource-path) "/movie-list.html")))))


;(context "/documents" [] (defroutes documents-routes
;    (GET "/" [] "get all documents")
;    (POST "/" {body :body} (str "create new document" body))
;    (context "/:id" [id] (defroutes document-routes
;      (GET "/" [] (str "get doc by id: " id))
;      (PUT "/" {body :body} (str "update doc with id: " id "with body:" body))
;      (DELETE "/" [] (str "delete doc with id: " id))
;    ))))


(defroutes home-routes
  (ANY "/" request home-txt)
  ;(ANY "/add-movie" request add-movie)
  (GET "/movies/:lang" [lang] (get-movies lang))
  (GET "/movie-list" request get-movies-html))

