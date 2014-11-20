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
		 [:li [:a {:id "active" :href "/api/cinema-page/ru"} "кинозал"]]
		 [:li [:a {:href "/ru/help.html"} "помощь и контакты"]]
		 [:li [:a {:href "/ru/demo.html"} "демо"]]]]])

(defn promo-header []
	[:div {:class "header-promo-cinema"}
	 [:a {:href  "http://play.google.com/store/apps/details?id=com.movielex.movielexapp"
				:class "upload-btn topupload"}
		(e/image {} "http://movielex.com/ru/images/upload.png")]])

(defn saveandgo []
	[:div {:class "saveandgo"}
	 [:a {:class "upload-btn"
				:href  "http://play.google.com/store/apps/details?id=com.movielex.movielexapp"}
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

(comment {:movie-state "active",
 :desc-short
							"The growing and genetically evolving apes find themselves at a critical point with the human race.",
 :fpkeys-file
							"http://movielex.com/files/kp646674/dawn_planet_apes2-en.fpkeys",
 :desc
								"In the wake of a disaster that changed the world, the growing and genetically evolving apes find themselves at a critical point with the human race.",
 :duration "130 min / 02:10",
 :year-released "2014",
 :title "Dawn of the Planet of the Apes",
 :id "kp646674",
 :shortname "dawn_planet_apes2",
 :src-url "http://www.imdb.com/title/tt2103281/",
 :img "http://movielex.com/files/kp646674/tt2103281.jpg",
 :translations
								({:desc "",
									:file
												 "http://movielex.com/files/kp646674/dawn_planet_apes2-ru.trans",
									:title "Русский",
									:lang "ru",
									:img "http://movielex.com/files/flags/flag-ru.png"})})

(defn translation-render [t]
	(conj [:td {:style "vertical-align:middle;text-align:center;"} [:p {:style "margin-left : 20px;"} (:lang t)]]
	[:td {:style "vertical-align:middle;text-align:center;"}
	 (e/image {:style "margin:1px;float:right;width:15px;height:15px"} (:img t) (:lang t))]))

(defn single-movie-render [m]
	[:tr
	 [:td
		[:div {:class "movie-entry"}
		 [:div {:class "title"} (:title m)]
		 (e/image (:img m))
		 [:div {:class "year-released"} (str "год: " (:year-released m))]
		 [:div {:class "duration"} (str "время: " (:duration m))]
		 [:br]
		 [:div {:class "desc"} (:desc m)]
		 [:div {:class "translation"} ; :style "margin:10px; float:left;"}
			[:table ;{ :border "1px" :bordercolor "white"}
			 [:tr
				[:td "Переводы: "]
				(let [ts (:translations m)]
					(map translation-render ts))]]]]]])

(defn movies-render [movies]
	[:div {:class "columnContainer"}
	 [:div {:class "column" :id "left"}
		[:h1 "Наши фильмы"]
		[:table
		 (map single-movie-render movies)]]])


(defn cinema-page-ru [movies]
	(p/html5 {:lang "ru"}
					 (head "MovieLex - Кинозал")
					 [:body
						[:div {:class "wrap"}
						 (upper-menu)
						 (promo-header)
						 [:div {:class "after_header"}]
						 (movies-render movies)
						 [:div {:class "hr960"} [:hr]]
						 (saveandgo)
						 (footer)]]))

(defn generate-cinema-page [lang]
	(let [movies (filter #(not (= (:id %) "1"))
											 (db/get-movies-active lang))]
		(cinema-page-ru movies)))


(defn generate-cinema-page1 [lang]
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
