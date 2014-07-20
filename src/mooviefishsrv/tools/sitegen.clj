(ns mooviefishsrv.tools.sitegen
	(:require 
    	clojure.contrib.io 
    	clojure.java.io
    	[mooviefishsrv.models.db :as db]
  		[clj-time.core :as time]
		[clj-time.format :as time-format]
		[net.cgrand.enlive-html :as html]))

(def index-html "site-template/ru/index.html")
(def cinema-html "site-template/ru/cinema.html")

(defn file-exists? [fn] 
	(.exists (clojure.java.io/as-file fn)))

(defn ttt []
	(db/load-movies-data)
	(prn "Movies: " (count @db/movies))
	(let [  rr (html/html-resource (java.io.StringReader. (slurp cinema-html)))
			current-movies (html/select rr [:ul.left-movies :div.movie-entry])
			new-movies (html/select rr [:ul.right-movies :div.movie-entry])]
		(prn "current-movies: " (count current-movies))
		(prn "new-movies: " (count new-movies))
	)
	;(html/deftemplate 
	;	cinema-template (java.io.StringReader. (slurp cinema-html))
	;	[movies] 
	;
	;	)
	
)

