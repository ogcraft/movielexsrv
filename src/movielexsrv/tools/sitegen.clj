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
	(let [	active-movies 	(filter #(not (= (:shortname %) "movielexdemo"))
								(db/get-movies-active lang))
			new-movies 		(db/get-movies-new lang)
			p 				(slurp ((keyword lang) cinema-template))]
		(selmer.parser/render p
			{:active-movies active-movies :new-movies new-movies})))

(defn write-cinema-page [lang path]
	(spit (str path "/cinema.html") (generate-cinema-page "ru")))

(defn load-movies-and-generate-cinema [lang path]
	(db/load-movies-data)
	(write-cinema-page lang path))

