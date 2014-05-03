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

(defresource get-movies
  :allowed-methods [:get]
  :handle-ok (fn [_] (db/movie-list)) 
  :available-media-types ["application/json"])

; (defresource add-movie
;   :allowed-methods [:post]
;   :malformed? (fn [context]
;                 (let [params (get-in context [:request :form-params])] 
;                   (empty? (get params "movie"))))
;   :handle-malformed "movie name cannot be empty!"
;   :post!  
;   (fn [context]             
;     (let [params (get-in context [:request :form-params])]
;       (swap! movies conj (get params "movie"))))
;   :handle-created (fn [_] (generate-string @movies))
;   :available-media-types ["application/json"])

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
  (ANY "/movies" request get-movies))
