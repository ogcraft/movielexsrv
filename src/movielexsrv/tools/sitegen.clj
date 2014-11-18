(ns movielexsrv.tools.sitegen
	(:require
    	clojure.contrib.io
    	clojure.java.io
    	[clojure.pprint :refer [pprint]]
    	selmer.parser
    	[movielexsrv.models.db :as db]
  		[clj-time.core :as time]
		[clj-time.format :as time-format]
		[net.cgrand.enlive-html :as html]))

(def cinema-template  	{:ru "site-template/ru/cinema.selmer.html"})

(defn file-exists? [fn]
	(.exists (clojure.java.io/as-file fn)))

(defn generate-cinema-page [lang]
	(let [ 	movies (filter #(not (= (:id %) "1"))
															 (db/get-movies-active lang))
				  ;movies-splited (split-at (/ (count movies) 2) movies)
					p (slurp ((keyword lang) cinema-template))]
		(selmer.parser/render p
			{:active-movies movies})))

(defn write-cinema-page
	([lang path]
		(spit (str path "/cinema.html") (generate-cinema-page "ru")))
	([lang]
	 (let [remote-site-path "/var/www/movielex.com/public_html/ru"
				 path (if (file-exists? remote-site-path ) remote-site-path "/tmp" )
				 page (str path "/cinema.html")]
		 (println "Generating [" page "]")
		 (spit page (generate-cinema-page "ru"))
		 (str "File cinema.html generated at [" path "]"))))


(defn load-movies-and-generate-cinema [lang path]
	;(db/load-movies-data)
	(write-cinema-page lang path))

