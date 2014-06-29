(ns mooviefishsrv.models.db
  	(:require 
    	clojure.contrib.io 
    	clojure.java.io
  		[com.ashafa.clutch :as couch]
  		[clj-time.core :as time]
		[clj-time.format :as time-format]
		[clojurewerkz.welle.core    :as wc]
        [clojurewerkz.welle.buckets :as wb]
        [clojurewerkz.welle.kv      :as kv]
  		[cheshire.core :refer [generate-string parse-stream]])
   	(:import java.io.PushbackReader)
   	(:import java.io.FileReader))
 
;(def db "http://192.168.10.122:5984/mvfishtest")
;(def db "http://olegg-linux:5984/mvfishtest")

(def riak_url "http://192.168.14.101:8098/riak")
;(def riak_url "http://127.0.0.1:8098/riak")
(def conn (wc/connect riak_url))
;(def votes-bucket (wb/update conn "votes.backet" {:last-write-wins true}))
(def votes-bucket "votes.backet")

(def date-formatter (time-format/formatters :date-hour-minute-second))
(def mvf-base "http://mooviefish.com/files")
(def cwd (System/getProperty "user.dir"))

(def movies-data "data/movies.data")
(def movies (atom {}))

(def users-data "data/users.data")
(def users (atom {}))

; (defn serialize
; 	"Print a data structure to a file so that we may read it in later."
;   	[data-structure #^String filename]
;   	(clojure.contrib.io/with-out-writer
;     	(java.io.File. filename)
;     	(binding [*print-dup* true] (prn data-structure))))

; ;; This allows us to then read in the structure at a later time, like so:
; (defn deserialize [filename]
;   	(with-open [r (PushbackReader. (FileReader. filename))]
;     	(read r)))

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
			{   :id id
				:shortname shortname,
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
	(if (.exists (clojure.contrib.io/as-file fname))
		(if-let [ms (load-file fname)]
			(reset! movies (into {} (for [m ms] [(:id m) m])))
			(reset! movies {}))
		(spit "" fname)))

(defn store-movies [fname]
	(println "Storing movies to " fname)
	(spit fname (binding [*print-dup* true] @movies)))
	;(spit fname (.toString (binding [*print-dup* true] @movies))))

(defn load-movies-data []
	(load-movies movies-data)
	(println "Loaded " (movie-count) " movies"))

(defn store-movies-data []
	(store-movies movies-data)
	(println "Stored " (movie-count) " movies"))

(defn users-count []
	(count @users))

(defn store-users [fname]
	(println "Storing users to " fname)
	(spit fname (binding [*print-dup* true] @users)))

(defn load-users [fname]
	(println "load-users from " fname)
	(if (.exists (clojure.contrib.io/as-file fname))
		(if-let [us (load-file fname)]
			(reset! users us)
			(reset! users {}))
		(spit fname "")))

(defn load-users-data []
	(load-users users-data)
	(println "Loaded " (users-count) " users"))

(defn store-users-data []
	(store-users users-data)
	(println "Stored " (users-count) " users"))

(defn add-user [did]
	(if (not (contains? @users did))
			(swap! users assoc did {})))

;; {11 {:mids {:dates [#<DateTime 2014-06-18T12:59:36.148Z> ]}}}
(defn add-movie-to-user [u mid]
	(let [ 	dates (get-in u [:mids mid :dates] [])
			t (time-format/unparse date-formatter (time/now))]
		(prn dates)
		(assoc-in u [:mids mid :dates] (conj dates t))))

(defn update-users-with-movie [did mid]
	(swap! users assoc did (add-movie-to-user (@users did) mid))
	(store-users-data))

(defn check-permission [did mid]
	true)

(defn acquire-movie [did mid]
	(let [ 	did (read-string did)
			mid (read-string mid)]
		(add-user did)
        (let [permission (check-permission did mid)]
        	(if permission
        		(update-users-with-movie did mid))
           	{:permission permission, :did did, :id mid})))

(defn votes-bucket-create [] 
	(wb/update conn votes-bucket {:last-write-wins true}))

(defn inc-vote [lang did vote] 
	(let [old-vote (get vote lang 0)] 
		(assoc vote lang (inc old-vote))))

(defn translation-vote [lang did mid]
	(let [{:keys [has-value? result]} (kv/fetch conn votes-bucket mid)
		  initial_vote {lang 1}]
		;(prn "result: " result " has-value?: " has-value?)
  		(if has-value? 
  			(let [ new_vote (inc-vote lang did (:value (first result)))] 
  				(kv/store conn votes-bucket mid new_vote {:content-type "application/clojure"})
  				{:id mid :vote new_vote})
  			(do (kv/store conn votes-bucket mid initial_vote {:content-type "application/clojure"})
				{:id mid :vote initial_vote}))))

(defn get-translation-vote [mid]
	(let [{:keys [has-value? result]} (kv/fetch conn votes-bucket mid)]
		;(prn "result: " result " has-value?: " has-value?)
  		(if has-value? 
  			(let [ vote (:value (first result))] 
  				{:id mid :vote vote})
  			{:id mid :vote {}})))

(defn get-stats []
	{:movies-num (movie-count), :users-num (users-count)})
