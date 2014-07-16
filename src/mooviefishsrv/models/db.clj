(ns mooviefishsrv.models.db
  	(:require 
    	clojure.contrib.io 
    	clojure.java.io
  		[com.ashafa.clutch :as couch]
  		[clj-time.core :as time]
		[clj-time.format :as time-format]
		;[clj-time.coerce :as time-coerce :exclude [extend second]]
		[clojurewerkz.welle.core    :as wc]
        [clojurewerkz.welle.buckets :as wb]
        [clojurewerkz.welle.kv      :as kv]
  		[cheshire.core :refer [generate-string parse-stream]])
  	;(:use clj-time.coerce)
   	(:import java.io.PushbackReader)
   	(:import java.io.FileReader))
 
;(def db "http://192.168.10.122:5984/mvfishtest")
;(def db "http://olegg-linux:5984/mvfishtest")

;(def riak_url "http://192.168.14.101:8098/riak")
(def riak_url "http://127.0.0.1:8098/riak")
(def conn (wc/connect riak_url))
;(def votes-bucket (wb/update conn "votes.backet" {:last-write-wins true}))
(def votes-bucket "votes.backet")
(def users-bucket "users.backet")

;(def data-types #{"user" "movie"})

(def date-formatter (time-format/formatters :date-hour-minute-second))
(def mvf-base "http://mooviefish.com/files")
(def cwd (System/getProperty "user.dir"))

(def movies-data "data/movies.data")
(def movies (atom {}))

(def users-data "data/users.data")

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
	(let [{:keys [shortname movie-state descriptions id fpkeys-file src-url duration translations]} movie] 
		(let [ 	desc (select-desc-by-lang lang descriptions)
				update-url-in-translation-with-id (partial update-url-in-translation id)]
			{   :id id
				:shortname shortname,
				:movie-state movie-state,
				:title (:title desc)
				:year-released (:year-released desc)
				:fpkeys-file (make-abs-url id (:en fpkeys-file))
				:img (make-abs-url id (:img desc))
				:desc (:desc desc)
				:desc-short (:desc-short desc)
				:src-url (:src-url desc)
				:duration (:duration desc)
				:translations (map update-url-in-translation-with-id translations)})))

(defn movie-count []
	(count @movies))

(defn get-movies [lang]
	(map #(get-short-desc lang %) (vals @movies)))

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


(defn users-bucket-create [] 
	(wb/update conn users-bucket {:last-write-wins true}))

(defn store-user [did u]
	(kv/store conn users-bucket did u {:content-type "application/clojure" :indexes {:data-type "user"}}))

(defn add-user [did]
	(let [{:keys [has-value? result]} (kv/fetch conn users-bucket did)]
		(if (not has-value?) 
			(do 
				(prn "add-user: Creating new user " did)
				(store-user did {})))))
				;:indexes {:data-type "user" :created-at #{(time-coerce/to-timestamp (time/now))}}}))))

(defn query-users []
	(kv/index-query conn users-bucket :data-type "user"))

;; {11 {:mids {:dates [#<DateTime 2014-06-18T12:59:36.148Z> ]}}}
(defn add-movie-to-user [u mid]
	(let [ 	dates (get-in u [:mids mid :dates] [])
			t (time-format/unparse date-formatter (time/now))]
		;(prn "add-movie-to-user: dates" dates)
		(assoc-in u [:mids mid :dates] (conj dates t))))


(defn update-users-with-movie [did mid]
	(let [{:keys [has-value? result]} (kv/fetch conn users-bucket did)
		   user (:value (first result))]
	(if has-value?
		(store-user did 
			(add-movie-to-user user mid)))))

(defn check-permission [did mid]
	true)

(defn acquire-movie [did mid]
	(add-user did)
    (let [permission (check-permission did mid)]
        (if permission
        	(update-users-with-movie did mid))
        {:permission permission, :did did, :id mid}))

;;;;;;;;;;;;;;;;;;; Votes 
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
	{:movies-num (movie-count), :users-num (count (query-users))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;(defn users-count-file []
;	(count @users))

;(defn store-users-file [fname]
;	(println "Storing users to " fname)
;	(spit fname (binding [*print-dup* true] @users)))

;(defn load-users-file [fname]
;	(println "load-users from " fname)
;	(if (.exists (clojure.contrib.io/as-file fname))
;		(if-let [us (load-file fname)]
;			(reset! users us)
;			(reset! users {}))
;		(spit fname "")))

;(defn load-users-data []
;	(load-users users-data)
;	(println "Loaded " (users-count) " users"))

;(defn store-users-data []
;	(store-users users-data)
;	(println "Stored " (users-count) " users"))

;(defn add-user-file [did]
;	(if (not (contains? @users did))
;			(swap! users assoc did {})))

;(defn update-users-with-movie [did mid]
;	(swap! users assoc did (add-movie-to-user (@users did) mid))
;	(store-users-data))
