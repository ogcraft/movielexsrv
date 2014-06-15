(ns mooviefishsrv.models.db
  (:require  
  	[com.ashafa.clutch :as couch]
  	[cheshire.core :refer [generate-string parse-stream]]))
  ;(:use mooviefishsrv.models.movies))

;(def db "http://192.168.10.122:5984/mvfishtest")
;(def db "http://olegg-linux:5984/mvfishtest")

(def mvf-base "http://mooviefish.com/files")
(def cwd (System/getProperty "user.dir"))

(def movies-data "data/movies.data")
;(def movies (load-file movies-data))
(def movies (atom {}))

(def users-data "data/users.data")

(def users (atom {}))


;(def movies (parse-stream (clojure.java.io/reader "/tmp/movie_list.json")))

(defn make-abs-url [id u]
	(if (nil? u) 
		u 
		(str mvf-base "/" id "/" u)))

(defn select-desc-by-lang [lang desc] 
	(let [ d (first (for [ e desc :when (= (:lang e) lang)] e))]
		(if (nil? d) 
			(first (for [ e desc :when (= (:lang e) "en")] e))
			d)))

(defn update-url-in-translation [id t]
	(let [	f (:file t) 
			i (:img  t)]
		(assoc t :file (make-abs-url id f) :img  (make-abs-url id i))))

(defn get-short-desc [lang movie]
	(let [{:keys [shortname descriptions id fpkeys-file translations]} movie] 
		(let [ 	desc (select-desc-by-lang lang descriptions)
				update-url-in-translation-with-id (partial update-url-in-translation id)]
			{   :shortname shortname,
				:title (:title desc)
				:fpkeys-file (make-abs-url id (:en fpkeys-file))
				:img (make-abs-url id (:img desc))
				:desc (:desc desc)
				:translations (map update-url-in-translation-with-id translations)})))

;(defn movie-list [] 
;	(map :doc (filter :doc 
;		(couch/all-documents db {:include_docs true}))))

(defn movie-count []
	(count @movies))

(defn get-movies [lang]
	(map #(get-short-desc lang %) (vals @movies)))

;(defn get-movie [lang id]
;	(let [ mid (read-string id) ]
;		(get-short-desc lang (first (filter #(= (:id %) mid) movies)))))

(defn get-movie [lang id]
	(let [ mid (read-string id) ]
		(get-short-desc lang (get @movies mid))))

(defn load-movies [fname]
	(println "load-movies from " fname)
	(let [ms (load-file fname)]
		(reset! movies (into {} (for [m ms] [(:id m) m])))))

(defn store-movies [fname]
	(println "Storing movies to " fname)
	(spit fname (.toString (vals @movies))))

(defn load-movies-data []
	(load-movies movies-data)
	(println "Loaded " (movie-count) " movies"))

(defn store-movies-data []
	(store-movies movies-data)
	(println "Stored " (movie-count) " movies"))

(defn add-user [did]
	(println "add-user: " did)
	(if (not (contains? @users :did))
		(swap! users assoc did {:mids {}})))

(defn check-permission [did id]
	true)

(defn aquire-movie [did id]
	(let [ 	did (read-string did)
			mid (read-string id)]
		(add-user did)
        (let [permission (check-permission did id)]
           {:permission permission, :did did, :id mid})))
