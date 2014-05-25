(ns mooviefishsrv.routes.home  
  (:require [compojure.core :refer :all]
            [liberator.core :refer [defresource resource]]
            [cheshire.core :refer [generate-string]]
            [noir.io :as io]
            [clojure.java.io :refer [file]]
            [mooviefishsrv.models.db :as db]))

(def start-html "/index.html")

(defresource home-txt
  :service-available? true
  :handle-ok "Welcome to MoovieFish!"
  :etag "fixed-etag" 
  :available-media-types ["text/plain"])

(defresource get-movies [lang]
  :allowed-methods [:get]
  :handle-ok (fn [ _ ] 
    (println "get-movies :handle-ok lang: " lang) 
    (generate-string (db/get-movies lang)))
  :available-media-types ["application/json"])

(defresource get-movie [lang id]
  :allowed-methods [:get]
  :handle-ok (fn [ _ ] 
    (println "get-movie :handle-ok lang: " lang " id: " id) 
    (generate-string (db/get-movie lang id)))
  :available-media-types ["application/json"])

(defresource get-movies-html
    :available-media-types ["text/html"]

    :exists?
    (fn [context]
      [(io/get-resource start-html)
       {::file (file (str (io/resource-path) start-html))}])

    :handle-ok
    (fn [{{{resource :resource} :route-params} :request}]
      (println "get-movies-html :handle-ok")
      (clojure.java.io/input-stream (io/get-resource start-html)))
    :last-modified
    (fn [{{{resource :resource} :route-params} :request}]
      (.lastModified (file (str (io/resource-path) start-html)))))


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
  (context "/api" []
  (GET "/movies/:lang" [lang] (get-movies lang))
  (GET "/movie/:lang/:id" [lang id] (get-movie lang id))
  (GET "/" request get-movies-html)))

