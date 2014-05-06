(ns mooviefishsrv.models.db
  (:require  
  	[com.ashafa.clutch :as couch]
  	[cheshire.core :refer [generate-string parse-stream]]))

(def db "http://192.168.10.122:5984/mvfishtest")
;(def db "http://olegg-linux:5984/mvfishtest")

;(def movies (atom ["Monsters" "Smurfs"]))
;(def movies (parse-stream (clojure.java.io/reader "/tmp/movie_list.json")))

(defn prepare-for-mobile [movies]
	(prn movies))

(defn movie-list [] 
	(couch/all-documents db {:include_docs true}))

