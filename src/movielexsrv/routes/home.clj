(ns movielexsrv.routes.home
  (:require [compojure.core :refer :all]
            [liberator.core :refer [defresource resource]]
            [cheshire.core :refer [generate-string]]
            [noir.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :refer [file]]
            [movielexsrv.models.db :as db]))

(def start-html "/index.html")

(defresource home-txt
  :service-available? true
  :handle-ok "Welcome to MovieLex!"
  :etag "fixed-etag"
  :available-media-types ["text/plain"])

(defresource get-movies-active [lang]
  :allowed-methods [:get]

  :handle-ok (fn [ ctx ]
    (println "get-movies-active :handle-ok lang: " lang)
    (generate-string (db/get-movies-active lang)))

  :as-response (fn [d ctx]
                  ;(pprint (liberator.representation/as-response d ctx))
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET")))

  :available-media-types ["application/json"])

(defresource get-movies-new [lang]
  :allowed-methods [:get]

  :handle-ok (fn [ ctx ]
    (println "get-movies-new :handle-ok lang: " lang)
    (generate-string (db/get-movies-new lang)))

  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET")))

  :available-media-types ["application/json"])

(defresource get-movie [lang id]
  :allowed-methods [:get]
  :handle-ok (fn [ ctx ]
    (println "get-movie :handle-ok lang: " lang " id: " id)
    (generate-string (db/get-movie lang id)))
  :available-media-types ["application/json"]
  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET"))))

(defresource acquire-movie [did mid]
  :allowed-methods [:get]
  :handle-ok (fn [ ctx ]
    (println "acquire-movie :handle-ok did: " did " id: " mid)
    (generate-string (db/acquire-movie did mid)))
  :available-media-types ["application/json"]
  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET"))))

(defresource translation-vote [lang did mid]
  :allowed-methods [:get]
  :handle-ok (fn [ ctx ]
    (println "translation-vote :handle-ok lang:" lang " did: " did " id: " mid)
    (generate-string (db/translation-vote lang did mid)))
  :available-media-types ["application/json"]
  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, POST"))))

(defresource get-translation-vote [mid]
  :allowed-methods [:get]
  :handle-ok (fn [ ctx ]
    (println "get-translation-vote :handle-ok id: " mid)
    (generate-string (db/get-translation-vote mid)))
  :available-media-types ["application/json"]
  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, POST"))))

(defresource put-user [uid]
  :allowed-methods [:put :get]
  :handle-ok (fn [ ctx ]
                (let [u (db/fetch-user uid)]
                  ;(prn "put-user handle-ok" u)
                  (generate-string u)))
  :put! (fn [ctx]
             (let [body (slurp (get-in ctx [:request :body]))]
               (db/put-user uid body)))

  :available-media-types ["application/json"]
  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, PUT"))))

(defresource get-users
  :allowed-methods [:get]

  :handle-ok  (fn [ctx]
                (pprint (db/query-users))
                (generate-string (db/query-users)))

  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, POST")))
  :available-media-types ["application/json"])

(defresource get-stats
  :allowed-methods [:get]

  :handle-ok (fn [ctx]
    (println "get-stats")
    (generate-string (db/get-stats)))

  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, POST")))
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

;(defresource test-route
;  :allowed-methods [:get]
;  :handle-ok (fn [ _ ]
;    (prn "test :handle-ok request: " request))
;  :available-media-types ["application/json"])

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
  (GET "/movies/:lang" [lang] (get-movies-active lang))
  (GET "/movies-new/:lang" [lang] (get-movies-new lang))
  (GET "/movie/:lang/:mid" [lang mid] (get-movie lang mid))
  (GET "/acquire/:did/:mid" [did mid] (acquire-movie did mid))
  (GET "/translation-vote/:lang/:did/:mid" [lang did mid] (translation-vote lang did mid))
  (GET "/get-translation-vote/:mid" [mid] (get-translation-vote mid))
  (ANY "/user/:uid" [uid] (put-user uid))
  (GET  "/users" [] get-users)
  (GET "/stats" [] get-stats)
  (GET "/test" request (str request))
  (GET "/" request get-movies-html)))

