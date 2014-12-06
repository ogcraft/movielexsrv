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

(defn upper-menu-ru []
  [:div {:class "menu"}
    [:table {:class "topmenu-table" :border "0" :cellpadding "2" :cellspacing "0" :width "100%"}
      [:tr
        [:td {:style "width:100px"}
         [:div {:class "logo"}
          [:a {:href "/ru/"}
            [:img {:src "http://movielex.com/ru/images/logo.svg" :width "150%" :height "150%"}]]]]
       [:td {:style "width:50px;padding-top:10px;" :align "center"}[:a {:style "color:#eee" :href "/ru/index.html"} "главная"]]
       [:td {:style "width: 50px;padding-top: 10px;" :align "center"} [:a {:style "color: orange" :href "/api/cinema-page/ru"} "кинозал"]]
       [:td {:style "width: 100px;padding-top: 10px;" :align "center"} [:a {:style "color: #eee" :href "/ru/help.html"} "помощь и контакты"]]
       [:td {:style "width: 50px;padding-top: 10px;" :align "center"} [:a {:style "color: #eee" :href "/ru/demo.html"} "демо"]]
       [:td {:style "width: 200px;padding-top: 10px;" :align "center"} [:a {:style "color: #eee" :href ""}]]
       [:td {:style "width: 50px;padding-top: 10px;" :align "center"} [:a {:href "/ru/index.html"}
        [:img {:src "http://movielex.com/files/flags/flag-ru.png" :style "height: 20px;width: 20px;" :alt "ru"}]]]
       [:td {:style "width: 50px;padding-top: 10px;" :align "center"} [:a {:href "/en/index.html"}
        [:img {:src "http://movielex.com/files/flags/flag-en.png" :style "height: 20px;width: 20px;"}]]]]]])

(defn upper-menu-en []
  [:div {:class "menu"}
   [:table {:class "topmenu-table" :border "0" :cellpadding "2" :cellspacing "0" :width "100%"}
    [:tr
     [:td {:style "width:100px"}
      [:div {:class "logo"}
       [:a {:href "/en/"}
        [:img {:src "http://movielex.com/ru/images/logo.svg" :width "150%" :height "150%"}]]]]
     [:td {:style "width:50px;padding-top:10px;" :align "center"}[:a {:style "color:#eee" :href "/en/index.html"} "home"]]
     [:td {:style "width: 50px;padding-top: 10px;" :align "center"} [:a {:style "color: orange" :href "/api/cinema-page/en"} "movies"]]
     [:td {:style "width: 100px;padding-top: 10px;" :align "center"} [:a {:style "color: #eee" :href ""} ]]
     [:td {:style "width: 50px;padding-top: 10px;" :align "center"} [:a {:style "color: #eee" :href ""} ]]
     [:td {:style "width: 200px;padding-top: 10px;" :align "center"} [:a {:style "color: #eee" :href ""}]]
     [:td {:style "width: 50px;padding-top: 10px;" :align "center"} [:a {:href "/ru/index.html"}
                                                                     [:img {:src "http://movielex.com/files/flags/flag-ru.png" :style "height: 20px;width: 20px;" :alt "ru"}]]]
     [:td {:style "width: 50px;padding-top: 10px;" :align "center"} [:a {:href "/en/index.html"}
                                                                     [:img {:src "http://movielex.com/files/flags/flag-en.png" :style "height: 20px;width: 20px;"}]]]]]])

(defn promo-header-ru []
  [:div {:class "header-promo-cinema"}
   [:a {:href  "http://play.google.com/store/apps/details?id=com.movielex.movielexapp"
        :class "upload-btn topupload"}
    (e/image {:style "height: 50px;width: 150px;"} "http://movielex.com/ru/images/upload.png")]])

(defn promo-header-en []
  [:div {:class "header-promo-cinema-en"}
   [:a {:href  "http://play.google.com/store/apps/details?id=com.movielex.movielexapp"
        :class "upload-btn topupload"}
    (e/image {:style "height: 50px;width: 150px;"} "http://movielex.com/en/images/upload.png")]])

(defn saveandgo-ru []
  [:div {:class "saveandgo"}
   [:a {:class "upload-btn"
        :href  "http://play.google.com/store/apps/details?id=com.movielex.movielexapp"}
    (e/image {:style "height: 50px;width: 150px;"} "http://movielex.com/ru/images/upload.png")]
   [:div {:class "gotocinema"} "... и пойти в кино!"]])

(defn saveandgo-en []
  [:div {:class "saveandgo"}
   [:a {:class "upload-btn"
        :href  "http://play.google.com/store/apps/details?id=com.movielex.movielexapp"}
    (e/image {:style "height: 50px;width: 150px;"} "http://movielex.com/en/images/upload.png")]
   [:div {:class "gotocinema"} "... and watch a movie!"]])

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


(defn single-translation-render [t]
  (conj [:td {:style "vertical-align:middle;text-align:center;"} [:p {:style "margin-left : 20px;"} (:lang t)]]
	[:td {:style "vertical-align:middle;text-align:center;"}
	 (e/image {:style "margin:1px;float:right;width:15px;height:15px"} (:img t) (:lang t))]))

(defn translation-render-ru [m]
  [:tr {:style "float:left"}
   [:td "Переводы: "]
   (let [ts (:translations m)]
     (map single-translation-render ts))])

(defn translation-render-en [m]
  [:tr {:style "float:left"}
   [:td "Available translations: "]
   (let [ts (:translations m)]
     (map single-translation-render ts))])

(defn single-movie-render-ru [m]
  [:table ;{:border "1",:bordercolor "white", :cellspacing "0", :cellpadding "0"}
   [:tr
    [:td
     [:div {:class "movie-entry"}
      [:div {:class "title"} (:title m)]
      (e/image (:img m))
      [:div {:class "year-released"} (str "год: " (:year-released m))]
      [:div {:class "duration"} (str "время: " (:duration m))]
      [:br]
      [:div {:class "desc"}
       (:desc m)]]]]
   (translation-render-ru m)])

(defn single-movie-render-en [m]
  [:table ;{:border "1",:bordercolor "white", :cellspacing "0", :cellpadding "0"}
   [:tr
    [:td
     [:div {:class "movie-entry"}
      [:div {:class "title"} (:title m)]
      (e/image (:img m))
      [:div {:class "year-released"} (str "year: " (:year-released m))]
      [:div {:class "duration"} (str "duration: " (:duration m))]
      [:br]
      [:div {:class "desc"}
       (:desc m)]]]]
   (translation-render-en m)])

(defn movies-render-ru [movies]
  [:div {:class "columnContainer"}
   [:div {:class "column" :id "left"}
    [:h1 "Наши фильмы"]
                                        ;[:table ;{:border "1",:bordercolor "white", :cellspacing "0", :cellpadding "0"}
    (map single-movie-render-ru movies)]])

(defn movies-render-en [movies]
  [:div {:class "columnContainer"}
   [:div {:class "column" :id "left"}
    [:h1 "Movies"]
                                        ;[:table ;{:border "1",:bordercolor "white", :cellspacing "0", :cellpadding "0"}
    (map single-movie-render-en movies)]])


(defn cinema-page-ru [movies]
  (p/html5 {:lang "ru"}
           (head "MovieLex - Кинозал")
           [:body
            [:div {:class "wrap"}
             (upper-menu-ru)
             (promo-header-ru)
             [:div {:class "after_header"}]
             (movies-render-ru movies)
             [:div {:class "hr960"} [:hr]]
             (saveandgo-ru)
             (footer)]]))
(defn cinema-page-en [movies]
  (p/html5 {:lang "en"}
           (head "MovieLex - Movies")
           [:body
            [:div {:class "wrap"}
             (upper-menu-en)
             (promo-header-en)
             [:div {:class "after_header"}]
             (movies-render-en movies)
             [:div {:class "hr960"} [:hr]]
             (saveandgo-en)
             (footer)]]))

(defn generate-cinema-page [lang]
  (let [movies (filter #(not (= (:id %) "1"))
                       (db/get-movies-active lang))
        cinema-page {"ru" cinema-page-ru, "en" cinema-page-en}]
    ((get cinema-page lang "en") movies)))


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
