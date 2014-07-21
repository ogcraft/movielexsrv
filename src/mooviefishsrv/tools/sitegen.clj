(ns mooviefishsrv.tools.sitegen
	(:require 
    	clojure.contrib.io 
    	clojure.java.io
    	[clojure.pprint :refer [pprint]]
    	selmer.parser
    	[mooviefishsrv.models.db :as db]
  		[clj-time.core :as time]
		[clj-time.format :as time-format]
		[net.cgrand.enlive-html :as html]))

(def index-template 	{:ru "site-template/ru/index.selmer.html" })
(def cinema-template  	{:ru "site-template/ru/cinema.selmer.html"})

(defn file-exists? [fn] 
	(.exists (clojure.java.io/as-file fn)))

;(def cinema-page-ru (slurp cinema-html-ru))

(defn generate-cinema-page [lang]
	(let [	active-movies 	(db/get-movies-active lang)
			new-movies 		(db/get-movies-new lang)
			p 				(slurp ((keyword lang) cinema-template))]
		(selmer.parser/render p 
			{:active-movies active-movies :new-movies new-movies})))

(defn ttt []
	(db/load-movies-data)

	;(prn "Movies: " (count @db/movies))
	;(let [  rr (html/html-resource (java.io.StringReader. (slurp cinema-html)))
	;		current-movies (html/select rr [:ul.left-movies :div.movie-entry])
	;		new-movies (html/select rr [:ul.right-movies :div.movie-entry])]
	;	(prn "current-movies: " (count current-movies))
	;	(prn "new-movies: " (count new-movies))
	;)
	
	;(html/defsnippet 
	;	movie-active-snippet (java.io.StringReader. (slurp cinema-html))
	;	{[[:ul.left-movies] [:li]]}
	;	[active-movies]
	;	[:div] (html/content "aaaaaa"))
	;	[:span.author] (html/content (:author post))
	;	[:div.post-body] (html/content (:body post)))

	;(html/deftemplate 
	;	cinema-template (java.io.StringReader. (slurp cinema-html))
	;	[active-movies new-movies]
	;	[:ul.left-movies] (html/content "<p>current movies</p>") 
	;	[:ul.right-movies] (html/content "<p>new movies</p>"))

	;(apply str (cinema-template (db/get-movies-active "en") (db/get-movies-new "en")))
	;(cinema-template (db/get-movies-active "en") (db/get-movies-new "en"))
	;(html/transform cinema-template [:li.movie-entry]  
    ;    (html/clone-for [i (range 4)] identity))
	;(prn cinema-page)
	;(pprint (first (db/get-movies-new "ru")))
	(generate-cinema-page "ru")
)

