(ns mooviefishsrv.models.db
  (:require  
  	[com.ashafa.clutch :as couch]
  	[cheshire.core :refer [generate-string parse-stream]]))

(def db "http://192.168.10.122:5984/mvfishtest")
;(def db "http://olegg-linux:5984/mvfishtest")

(def mvf-base "http://mooviefish.com/files")

;(def movies (parse-stream (clojure.java.io/reader "/tmp/movie_list.json")))

(defn make-abs-url [u]
	(str mvf-base "/" u))

(defn select-desc-by-lang [lang desc] 
	(first (for [ e desc :when (= (:lang e) "ru")] e)))

(defn update-url-in-translation [t]
	(let [f (:file t)]
		(assoc t :flie (make-abs-url f)))
	t)

(defn get-short-desc [lang movie]
	(let [{:keys [shortname descriptions _id fpkeys-file translations]} movie] 
		(let [ desc (select-desc-by-lang lang descriptions)]
			{   :shortname shortname,
				:title (:title desc)
				:fpkeys-file (make-abs-url fpkeys-file)
				:img (make-abs-url (:img desc))
				:desc (:desc desc)
				:translations (map update-url-in-translation translations)})))

(defn movie-list [] 
	(couch/all-documents db {:include_docs true}))

(defn get-docs []
	(map :doc  
		(filter :doc (movie-list))))

