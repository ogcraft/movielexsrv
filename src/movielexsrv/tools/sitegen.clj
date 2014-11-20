(ns movielexsrv.tools.sitegen
	(:require
    	clojure.contrib.io
    	clojure.java.io
    	[clojure.pprint :refer [pprint]]
    	selmer.parser
    	[movielexsrv.models.db :as db]
  		[clj-time.core :as time]
		[clj-time.format :as time-format]
		[net.cgrand.enlive-html :as html]
		[hiccup.core :as h]
		[hiccup.page :as p]
		[hiccup.element :as e]
		[hiccup.form :as form]))

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

;;;;;;;;;;;;;;;;   ciname-page

(defn head [title]
	[:head
	 [:title title]
	 [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]
	 [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
		(p/include-css "http://movielex.com/ru/css/styles.css")])

(defn upper-menu []
	[:div {:class "menu"}
		[:div {:class "logo"}
			[:a {:href "/ru/"}
			 (e/image {:width "150%" :height "150%"}
				 "http://movielex.com/ru/images/logo.svg")]]
		[:div
			[:ul {:class "topmenu"}
	 			[:li [:a {:href "/ru/"} "главная"]]
				[:li [:a {:id "active" :href "/ru/cinema.html"} "кинозал"]]
				[:li [:a {:href "/ru/help.html"} "помощь и контакты"]]
				[:li [:a {:href "/ru/demo.html"} "демо"]]]]])


(defn promo-header []
	[:div {:class "header-promo-cinema"}
		[:a {	:href "http://play.google.com/store/apps/details?id=com.movielex.movielexapp"
			 		:class "upload-btn topupload"}
			(e/image {} "http://movielex.com/ru/images/upload.png")]])

(defn movies-render []
	)

(defn saveandgo []
	[:div {:class "saveandgo"}
		[:a {:class "upload-btn"
			 :href "http://play.google.com/store/apps/details?id=com.movielex.movielexapp"}
	 		(e/image {} "http://movielex.com/ru/images/upload.png")]
	[:div {:class "gotocinema"} "... и пойти в кино!"]])

(defn footer []
	[:div {:class "footer"}
	[:div {:class "copyright"} "&copy; 2014 MovieLex"]
	[:ul {:class "socials"}
	[:li {:class "vk"}
	 [:a {:href "http://vk.com/club74637279"}
		(e/image {} "http://movielex.com/ru/images/vk.png")]]
[:li {:class "lj"}
 [:a {:href "http://movielex.livejournal.com/"}
	(e/image {} "http://movielex.com/ru/images/lj.png")]]
[:li {:class "fb"}
 [:a {:href "https://www.facebook.com/groups/266837963509823"}
	(e/image {} "http://movielex.com/ru/images/fb.png")]]
[:li {:class "ok"}
 [:a {:href "http://www.odnoklassniki.ru/group/51966989238419"}
	(e/image {} "http://movielex.com/ru/images/ok.png")]]]
[:div {:class "email"} "movielex.com@gmail.com"]])

(defn cinema-page-ru [movies]
	(p/html5 {:lang "ru"}
		(head "MovieLex - Кинозал")
		[:body
		 [:div {:class="wrap"}
		 (upper-menu)
		 (promo-header)
			[:div {:class "after_header"}]
			(movies-render)
			[:div {:class "hr960"} [:hr]]
			(saveandgo)
		 (footer)]]))

(defn generate-cinema-page1 [lang]
	(let [ 	movies (filter #(not (= (:id %) "1"))
												 (db/get-movies-active lang))]
		(cinema-page-ru movies)))
