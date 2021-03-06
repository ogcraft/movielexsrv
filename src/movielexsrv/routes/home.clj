(ns movielexsrv.routes.home
  (:require [compojure.core :refer :all]
            [liberator.core :refer [defresource resource]]
            [cheshire.core :as json]
            [hiccup.core :as h]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :refer [file]]
            [movielexsrv.models.db :as db]
            [movielexsrv.admins :as admins :refer (admins)]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [movielexsrv.tools.sitegen :as sitegen]))

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
    (json/generate-string (db/get-movies-active lang)))

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
    (json/generate-string (db/get-movies-new lang)))

  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET")))

  :available-media-types ["application/json"])

(defresource get-movie [lang id]
  :allowed-methods [:get]
  :handle-ok (fn [ ctx ]
    (println "get-movie :handle-ok lang: " lang " id: " id)
    (json/generate-string (db/get-movie lang id)))
  :available-media-types ["application/json"]
  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET"))))

(defresource get-movie-full [id]
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
               (let [media-type
                     (get-in ctx [:representation :media-type])]
                     (condp = media-type
                     "application/json" (json/generate-string (db/get-movie id))
                     "text/html" (h/html (db/render-movie-html (db/get-movie id)))
                     nil)))
  :available-media-types ["text/html" "application/json"]
  :as-response (fn [d ctx]
                 (-> (liberator.representation/as-response d ctx)
                     (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                     (assoc-in [:headers "Access-Control-Allow-Methods"] "GET"))))

(defresource get-movies-full []
             :allowed-methods [:get]
             :handle-ok (fn [ctx]
                          (let [media-type
                                (get-in ctx [:representation :media-type])]
                            (condp = media-type
                              "application/json" (json/generate-string (db/query-movies))
                              "text/html" (h/html (db/render-movies-html (db/query-movies)))
                              nil)))
             :available-media-types ["text/html" "application/json"]
             :as-response (fn [d ctx]
                            (-> (liberator.representation/as-response d ctx)
                                (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                                (assoc-in [:headers "Access-Control-Allow-Methods"] "GET"))))

(defresource movie-new-form []
             :allowed-methods [:get]
             :handle-ok (fn [ctx]
                          (let [media-type
                                (get-in ctx [:representation :media-type])]
                                "text/html" (h/html
                                              (db/render-movie-new-form))))
             :available-media-types ["text/html"]
             :as-response (fn [d ctx]
                            (-> (liberator.representation/as-response d ctx)
                                (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                                (assoc-in [:headers "Access-Control-Allow-Methods"] "GET"))))

(defresource movie-new-update []
             :allowed-methods [:post]

             :handle-ok (fn [ctx]
                         ; (pprint ctx)
                        (let [params (get-in ctx [:request :params])]
                          ;(json/generate-string { :result "true", :reason "handle-ok" :id (ctx ::id)})))
             "text/html" (h/html (db/render-movie-html (db/get-movie (ctx ::id))))))

:handle-created (fn [ctx]
                          ; (pprint ctx)
                       (let [params (get-in ctx [:request :params])]
                         ;(json/generate-string { :result "true", :reason "handle-created" :id (ctx ::id)})))
                         "text/html" (h/html (db/render-movie-html (db/get-movie (ctx ::id))))))

             :post! (fn [ctx]
                      ;(pprint ctx)
                      (let [params (get-in ctx [:request :params])]
                        (assoc ctx ::id (db/do-movie-new-update params))))

             :as-response (fn [d ctx]
                            (-> (liberator.representation/as-response d ctx)
                                (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                                (assoc-in [:headers "Access-Control-Allow-Methods"] "POST")))
             :available-media-types ["application/json" "text/html"])

(defresource movie-as-json-form [id]
             :allowed-methods [:get]
             :handle-ok (fn [ctx]
                          (let [media-type
                                (get-in ctx [:representation :media-type])]
                            "text/html" (h/html
                                          (db/render-movie-full-as-json id))))
             :available-media-types ["text/html"]
             :as-response (fn [d ctx]
                            (-> (liberator.representation/as-response d ctx)
                                (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                                (assoc-in [:headers "Access-Control-Allow-Methods"] "GET"))))

(defresource put-movie-json-from-form []
             :allowed-methods [:post]

             :handle-ok (fn [ctx]
                          (let [params (get-in ctx [:request :params])]
                            "text/html" (h/html (db/render-movie-html (db/get-movie (ctx ::id))))))

             :handle-created (fn [ctx]
                               (let [params (get-in ctx [:request :params])]
                                 "text/html" (h/html (db/render-movie-html (db/get-movie (ctx ::id))))))

             :post! (fn [ctx]
                      (let [params (get-in ctx [:request :params])]
                        (assoc ctx ::id (db/do-movie-update-json params))))

             :as-response (fn [d ctx]
                            (-> (liberator.representation/as-response d ctx)
                                (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                                (assoc-in [:headers "Access-Control-Allow-Methods"] "POST")))
             :available-media-types ["application/json" "text/html"])


(defresource put-movie []
             :allowed-methods [:put ]
             :handle-ok (fn [ ctx ]
                            (json/generate-string "Ok"))
             :put! (fn [ctx]
                     (let [body (slurp (get-in ctx [:request :body]))]
                       (db/put-movie body)))
             :handle-created (fn [ ctx ]
                               (json/generate-string { :result "true", :reason ""}))
             :available-media-types ["application/json"]
             :as-response (fn [d ctx]
                            (-> (liberator.representation/as-response d ctx)
                                (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                                (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, PUT"))))

(defresource acquire-movie [did mid]
  :allowed-methods [:get]
  :handle-ok (fn [ ctx ]
    (println "acquire-movie :handle-ok did: " did " id: " mid)
    (json/generate-string (db/acquire-movie did mid)))
  :available-media-types ["application/json"]
  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET"))))

(defresource translation-vote [lang did mid]
  :allowed-methods [:get]
  :handle-ok (fn [ ctx ]
    (println "translation-vote :handle-ok lang:" lang " did: " did " id: " mid)
    (json/generate-string (db/translation-vote lang did mid)))
  :available-media-types ["application/json"]
  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, POST"))))

(defresource get-translation-vote [mid]
  :allowed-methods [:get]
  :handle-ok (fn [ ctx ]
    (println "get-translation-vote :handle-ok id: " mid)
    (json/generate-string (db/get-translation-vote mid)))
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
                  (json/generate-string u)))
  :put! (fn [ctx]
             (let [body (slurp (get-in ctx [:request :body]))]
               (db/put-user uid body)))
               ;(json/generate-string {:user_id "11", :result "false", :reason "Not valid"})))
  :handle-created (fn [ ctx ]
                    (json/generate-string (db/validate-user uid)))
                    ;(json/generate-string {:user_id uid, :result "true", :reason ""}))

  :available-media-types ["application/json"]
  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, PUT"))))

(defresource get-users
  :allowed-methods [:get]

  ;:handle-ok  (fn [ctx]
  ;              (pprint (db/query-users))
  ;              (json/generate-string (db/query-users)))

  :handle-ok #(let [media-type
                        (get-in % [:representation :media-type])]
                    (condp = media-type
                      "application/json" (json/generate-string (db/query-users))
                      "text/html" (h/html (db/render-users-html (db/query-users)))
                      ;{:message "You requested a media type"
                      ; :media-type media-type}
                      nil))

  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, POST")))
  :available-media-types ["text/html" "application/json"])

(defresource get-stats
  :allowed-methods [:get]

  :handle-ok (fn [ctx]
    (println "get-stats")
    (json/generate-string (db/get-stats)))

  :as-response (fn [d ctx]
                  (-> (liberator.representation/as-response d ctx)
                      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                      (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, POST")))
  :available-media-types ["application/json"])


(defresource generate-cinema-page [lang]
             :allowed-methods [:get]

             :handle-ok (fn [ctx]
                          (sitegen/generate-cinema-page lang))
             :as-response (fn [d ctx]
                            (-> (liberator.representation/as-response d ctx)
                                (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                                (assoc-in [:headers "Access-Control-Allow-Methods"] "GET")))
             :available-media-types ["text/html"])

(defresource api-main-page []
             :allowed-methods [:get]
             :handle-ok (fn [ctx]
                          (db/render-api-main (:request ctx)))
             :as-response (fn [d ctx]
                            (-> (liberator.representation/as-response d ctx)
                                (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                                (assoc-in [:headers "Access-Control-Allow-Methods"] "GET")))
             :available-media-types ["text/html"])

;(defresource get-movies-html
;    :available-media-types ["text/html"]
;
;    :exists?
;    (fn [context]
;      [(io/get-resource start-html)
;       {::file (file (str (io/resource-path) start-html))}])
;
;    :handle-ok
;    (fn [{{{resource :resource} :route-params} :request}]
;      (println "get-movies-html :handle-ok")
;      (clojure.java.io/input-stream (io/get-resource start-html)))
;    :last-modified
;    (fn [{{{resource :resource} :route-params} :request}]
;      (.lastModified (file (str (io/resource-path) start-html)))))


(defresource mytest
  :allowed-methods [:get :post]

  :handle-ok (fn [ctx]
                 (println "test ------------------ ok")
                 (pprint ctx)
                 (str ctx))

  :post! (fn [ctx]
               (println "test ------------------ post")
               (pprint ctx)
               (str ctx))

  :as-response (fn [d ctx]
                   (-> (liberator.representation/as-response d ctx)
                       (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
                       (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, POST")))
  :available-media-types ["application/json"])

(comment
(defroutes home-routes
            (context "/api" []
                    (GET  "/movies/:lang"               [lang] (get-movies-active lang))
                    (GET  "/movies-new/:lang"           [lang] (get-movies-new lang))
                    (GET  "/movie/:lang/:mid"           [lang mid] (get-movie lang mid))
                    (GET  "/movie-full/:mid"            [mid] (get-movie-full mid))
                    (GET  "/movie/new"                  [] (movie-new-form))
                    (POST "/movie/new/update"           [] (movie-new-update))
                    (GET  "/movie-json/:mid"            [mid] (movie-as-json-form mid))
                    (POST "/movie-full/update-json"     [] (put-movie-json-from-form))
                    (GET  "/movies-full"                [] (get-movies-full))
                    (ANY  "/put-movie"                  [] (put-movie))
                    (GET  "/acquire/:did/:mid"          [did mid] (acquire-movie did mid))
                    (GET  "/translation-vote/:lang/:did/:mid" [lang did mid] (translation-vote lang did mid))
                    (GET  "/get-translation-vote/:mid" [mid] (get-translation-vote mid))
                    (ANY  "/user/:uid"                  [uid] (put-user uid))
                    (GET  "/users"                      [] get-users)
                    (GET  "/stats"                      [] get-stats)
                    (GET  "/cinema-page/:lang"          [lang] (generate-cinema-page lang))
                    (ANY  "/test" [] mytest))
           (POST "/posttest" {params :params} (str params)))
)

; MovieLexApp routes
;GETMOVIES_REST = "%s/api/movies/%s";
;GETMOVIEDETAIL_REST = "%s/api/movie/%s/%s";
;PUTUSERID_REST = "%s/api/user/%s";
;ACQUIRE_MOVIE_REST = "%s/api/acquire/%s/%s";
;GETACQUIRE_PERMISSION_REST = "%s/api/acquire/%s/%s";

(defroutes movielexapp-routes
           (context "/api" []
                    (GET  "/movies/:lang"                     [lang] (get-movies-active lang))
                    (GET  "/movie/:lang/:mid"                 [lang mid] (get-movie lang mid))
                    (GET  "/acquire/:did/:mid"                [did mid] (acquire-movie did mid))
                    (ANY  "/user/:uid"                        [uid] (put-user uid))))
;                    (GET  "/movies-new/:lang"                [lang] (get-movies-new lang))))

(defroutes public-routes
           (context "/api" []
                    (GET  "/translation-vote/:lang/:did/:mid" [lang did mid] (translation-vote lang did mid))
                    (GET  "/get-translation-vote/:mid"        [mid] (get-translation-vote mid))
                    (GET  "/cinema-page/:lang"                [lang] (generate-cinema-page lang))
                    (ANY  "/test"                             [] mytest))
           (ANY "/" request (ring.util.response/redirect "/api/login"))
           (POST "/posttest" {params :params} (str params)))


(defroutes admin-routes
           (context "/api" []
                    (GET  "/movie-full/:mid" [mid] (friend/authorize #{:movielexsrv.admins/admin} (get-movie-full mid)))
                    (GET  "/movie/new"  [] (friend/authorize #{:movielexsrv.admins/admin} (movie-new-form)))
                    (POST "/movie/new/update"  [] (friend/authorize #{:movielexsrv.admins/admin} (movie-new-update)))
                    (GET  "/movie-json/:mid"  [mid] (friend/authorize #{:movielexsrv.admins/admin} (movie-as-json-form mid)))
                    (POST "/movie-full/update-json"  [] (friend/authorize #{:movielexsrv.admins/admin} (put-movie-json-from-form)))
                    (GET  "/movies-full" [] (friend/authorize #{:movielexsrv.admins/admin} (get-movies-full)))
                    (ANY  "/put-movie"  [] (friend/authorize #{:movielexsrv.admins/admin} (put-movie)))
                    (GET  "/users"  [] (friend/authorize #{:movielexsrv.admins/admin} get-users))
                    (GET  "/stats" [] (friend/authorize #{:movielexsrv.admins/admin} get-stats))
                    (GET  "/"   [] (friend/authorize #{:movielexsrv.admins/admin} (api-main-page)))
                    (GET  "/logout" req
                          (friend/logout*
                            (ring.util.response/redirect
                              (str (:context req) "/"))))
                    (GET  "/login" req        (h/html (db/render-login-form)))))

(def secured-routes (friend/authenticate
                      admin-routes
                      {;:allow-anon? false
                       :login-uri "/api/login"
                       :default-landing-uri "/api"
                       :unauthorized-redirect-uri "/api/login"
                       :credential-fn #(creds/bcrypt-credential-fn admins/admins %)
                       :workflows [(workflows/interactive-form)]}))


