(ns movielexsrv.models.db
  (:require
    clojure.contrib.io
    clojure.java.io
    ;[com.ashafa.clutch :as couch]
    [clojure.pprint :refer [pprint]]
    [clj-time.core :as time]
    [clj-time.format :as time-format]
    [hiccup.core :as h]
    [hiccup.page :as p]
    [hiccup.form :as form]
    ;[clj-time.coerce :as time-coerce :exclude [extend second]]
    [clojurewerkz.welle.core :as wc]
    [clojurewerkz.welle.buckets :as wb]
    [clojurewerkz.welle.kv :as kv]
    [cheshire.core :as json]
    [movielexsrv.tools.utils :as utils])
  ;(:use clj-time.coerce)
  (:import java.io.PushbackReader)
  (:import java.io.FileReader))

(def state (atom {}))

(defn get-state [key]
  (@state key))

(defn update-state [key val]
  (swap! state assoc key val))

;(def votes-bucket (wb/update conn "votes.backet" {:last-write-wins true}))
(def votes-bucket "votes.backet")
(def users-bucket "users.backet")
(def movies-bucket "movies.backet")

;(def data-types #{"user" "movie"})

(def date-formatter (time-format/formatters :date-hour-minute-second))
(def mvf-base "http://movielex.com/files")
(def cwd (System/getProperty "user.dir"))

(def movies-data "data/movies.data")

(def config (atom {}))

(defn get-config [key]
  (@config key))

(defn connect-riak []
  (update-state :conn (wc/connect (get-config :riak-srv))))

(defn make-abs-url [id u]
  (if (nil? u)
    u
    (str mvf-base "/" id "/" u)))

(defn select-desc-by-lang [lang desc]
  (let [d (first (for [e desc :when (= (:lang e) lang)] e))]
    (if (nil? d)
      (first (for [e desc :when (= (:lang e) "en")] e))
      d)))

(defn update-url-in-translation [id t]
  (let [f (:file t)
        i (:img t)]
    (assoc t :file (make-abs-url id f) :img (make-abs-url "flags" i))))

(defn get-short-desc [lang movie]
  (let [{:keys [shortname movie-state descriptions id fpkeys-file src-url duration translations]} movie]
    (let [desc (select-desc-by-lang lang descriptions)
          update-url-in-translation-with-id (partial update-url-in-translation id)]
      {:id            id
       :shortname     shortname,
       :movie-state   movie-state,
       :title         (:title desc)
       :year-released (:year-released desc)
       :fpkeys-file   (make-abs-url id (:en fpkeys-file))
       :img           (make-abs-url id (:img desc))
       :desc          (:desc desc)
       :desc-short    (:desc-short desc)
       :src-url       (:src-url desc)
       :duration      (:duration desc)
       :translations  (map update-url-in-translation-with-id translations)})))

(defn get-movie-title [lang m]
  (:title (get-short-desc lang m)))

(defn get-movie-desc [lang m]
  (:desc (get-short-desc lang m)))

(defn get-movie-desc-short [lang m]
  (:desc-short (get-short-desc lang m)))


(defn movies-bucket-create []
  (wb/update (get-state :conn) movies-bucket {:last-write-wins true}))

(defn store-movie [m]
  (let [id (:id m)]
    (prn "store-movie: " id)
    (kv/store (get-state :conn) movies-bucket id m
              {:content-type "application/clojure" :indexes {:data-type "movie"}})))

(defn query-movies []
  (kv/index-query (get-state :conn) movies-bucket :data-type "movie"))

(defn fetch-movie [mid]
  (let [{:keys [has-value? result]} (kv/fetch (get-state :conn) movies-bucket mid)]
    (:value (first result))))

(defn collect-movies [ids]
  (map fetch-movie ids))

(defn get-movies [lang state]
  (let [movies (collect-movies (query-movies))
        movie-with-state (filter #(= (:movie-state %) state) movies)]
    (map #(get-short-desc lang %) (sort-by :id movie-with-state))))

(defn get-movies-full [state]
  (let [movies (collect-movies (query-movies))]
    (filter #(= (:movie-state %) state) movies)))

(defn movies-count []
  (count (query-movies)))

(defn get-movies-active [lang]
  (get-movies lang "active"))

(defn get-movies-new [lang]
  (get-movies lang "new"))

(defn get-movie
  ( [lang id]
    (let [m (fetch-movie id)]
      (get-short-desc lang m)))
  ( [id]
   (fetch-movie id)))

(defn put-movie [json]
  (prn json)
  (let [data (json/parse-string json true)]
    (do
      (prn data)
      (store-movie data))))

(defn load-movies-from-file [fname]
  (println "load-movies-from-file from " fname)
  (if (.exists (clojure.contrib.io/as-file fname))
    (if-let [ms (load-file fname)]
      (doseq [m ms] (store-movie m)))))

(defn movies-to-json [fname]
  (if (.exists (clojure.contrib.io/as-file fname))
    (if-let [ms (load-file fname)]
      (json/generate-string (second ms)))))

;;;;;; old version movie handling version
(comment

  (def movies1remove (atom {}))

    (defn load-movies1 [fname]
      (println "load-movies from " fname)
      (if (.exists (clojure.contrib.io/as-file fname))
        (if-let [ms (load-file fname)]
          (reset! movies1remove (into (sorted-map) (for [m ms] [(:id m) m])))
          (reset! movies1remove {}))
        (spit "" fname)))

    (defn store-movies1 [fname]
      (println "Storing movies to " fname)
      (spit fname (binding [*print-dup* true] @movies1remove)))


    (defn load-movies-data1 []
      (load-movies1 movies-data))

    (defn store-movies-data1 []
      (store-movies1 movies-data)
      (println "Stored " (movies-count) " movies"))

    (defn get-movies1 [lang state]
      (map #(get-short-desc lang %)
           (filter #(= (:movie-state %) state) (vals @movies1remove))))

    (defn get-movies-full1 [state]
      (filter #(= (:movie-state %) state) (vals @movies1remove)))

    (defn get-movie1 [lang id]
      (let [mid (read-string id)]
        (get-short-desc lang (get @movies1remove mid))))

    ) ; end of comment

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn load-config [fname]
  (if (.exists (clojure.contrib.io/as-file fname))
    (if-let [cfg (load-file fname)]
      (reset! config cfg)
      (reset! config {}))
    (spit "" fname)))

(defn load-host-config []
  (let [host (utils/gethostname)
        fname (str "data/" host ".config")]
    (println "Loading configuration: " fname)
    (load-config fname)))


(defn users-bucket-create []
  (wb/update (get-state :conn) users-bucket {:last-write-wins true}))

(defn user-exists? [did]
  (let [{:keys [has-value? result]} (kv/fetch (get-state :conn) users-bucket did)]
    has-value?))

(defn fetch-user [uid]
  (let [{:keys [has-value? result]} (kv/fetch (get-state :conn) users-bucket uid)
        user (:value (first result))]
    user))

(defn store-user [did u]
  ;(prn "store-user: " u)
  (kv/store (get-state :conn) users-bucket did u {:content-type "application/clojure" :indexes {:data-type "user"}}))

(defn delete-user [did]
  (prn "delete-user: " did)
  (kv/delete (get-state :conn) users-bucket did))

(defn put-user [uid user-data]
  (let [data (json/parse-string user-data true)
        u (fetch-user uid)
        data-with-uid (assoc data :uid uid)]
    (if u
      (do
        ;(prn "Existing user: " u " data:" data)
        (store-user uid (assoc u :user-data data-with-uid)))
      (store-user uid {:user-data data-with-uid}))))

(defn validate-user [uid]
  (if-let [u (fetch-user uid)]
    (let [appver (utils/ver->int (get-in u [:user-data :appver]))
          v1 (first appver)
          v2 (second appver)
          v3 (last appver)]
      (if (> v1 0)
        {:user_id uid, :result "true", :reason "Ok" :reason_code 0}
        {:user_id uid, :result "false", :reason "Unsupported version" :reason_code 1001}))
    {:user_id uid, :result "false", :reason "No found" :reason_code 1002}))

(defn query-users []
  (kv/index-query (get-state :conn) users-bucket :data-type "user"))

(defn make-user [user-data]
  (let [u (json/parse-string user-data true)
        did (:device_id u)]
    (store-user did u)))

(defn add-movie-to-user [u mid]
  (let [dates (get-in u [:mids mid :dates] [])
        t (time-format/unparse date-formatter (time/now))]
    ;(prn "add-movie-to-user: dates" dates)
    (assoc-in u [:mids mid :dates] (conj dates t))))

(defn update-users-with-movie [did mid]
  (let [{:keys [has-value? result]} (kv/fetch (get-state :conn) users-bucket did)
        user (:value (first result))]
    (if has-value?
      (store-user did
                  (add-movie-to-user user mid)))))

(defn check-permission [did mid]
  true)

(defn acquire-movie [did mid]
  (if (user-exists? did)
    (let [permission (check-permission did mid)]
      (if permission
        (update-users-with-movie did mid))
      {:permission permission, :did did, :id mid})
    {:permission false, :did did, :id mid}))

;;;;;;;;;;;;;;;;;;; Votes
(defn votes-bucket-create []
  (wb/update (get-state :conn) votes-bucket {:last-write-wins true}))

(defn inc-vote [lang did vote]
  (let [old-vote (get vote lang 0)]
    (assoc vote lang (inc old-vote))))

(defn translation-vote [lang did mid]
  (let [{:keys [has-value? result]} (kv/fetch (get-state :conn) votes-bucket mid)
        initial_vote {lang 1}]
    ;(prn "result: " result " has-value?: " has-value?)
    (if has-value?
      (let [new_vote (inc-vote lang did (:value (first result)))]
        (kv/store (get-state :conn) votes-bucket mid new_vote {:content-type "application/clojure"})
        {:id mid :vote new_vote})
      (do (kv/store (get-state :conn) votes-bucket mid initial_vote {:content-type "application/clojure"})
          {:id mid :vote initial_vote}))))

(defn get-translation-vote [mid]
  (let [{:keys [has-value? result]} (kv/fetch (get-state :conn) votes-bucket mid)]
    ;(prn "result: " result " has-value?: " has-value?)
    (if has-value?
      (let [vote (:value (first result))]
        {:id mid :vote vote})
      {:id mid :vote {}})))

(defn get-stats []
  {:movies-num (movies-count), :users-num (count (query-users))})


(defn init-local-repl []
  (load-host-config)
  (connect-riak)
  (get-stats))

(defn collect-users [uids]
  (map fetch-user uids))

(defn group-users-by-account-device [uids]
  (group-by #(get-in % [:user-data :account]) (collect-users uids)))

;          [:p (with-out-str (clojure.pprint/pprint u))])]))

(defn kv-table-row [k v c]
  [:tr
   [:td {:style "width: 100px"} k]
   [:td {:style "width: 700px"} v]
   [:td {:style "width: 100px"} c]])

(defn kv-table-5row [k1 v1 k2 v2 c]
  [:tr
   [:td {:style "width: 100px"} k1]
   [:td {:style "width: 200px"} v1]
   [:td {:style "width: 100px"} k2]
   [:td {:style "width: 200px"} v2]
   [:td {:style "width: 100px"} c]])

(defn render-movie-description [d]
  [:div.translation-view
   [:h3 "Language: " (:lang d)]
   [:table {:border "1", :width "70%", :bordercolor "brawn", :cellspacing "0", :cellpadding "2"}
    ;(for [[k v] d] (kv-table-row k v "ok"))
    (kv-table-row "lang" (:lang d) "ok")
    (kv-table-row "title" (:title d) "ok")
    (kv-table-row "src-url" (:src-url d) "ok")
    (kv-table-row "img" (:img d) "ok")
    (kv-table-row "year-released" (:year-released d) "ok")
    (kv-table-row "duration" (:duration d) "ok")
    (kv-table-row "desc" (:desc d) "ok")
    (kv-table-row "desc-short" (:desc-short d) "ok")]])

(defn render-movie-translation [d]
  [:div.description-view
   [:h3 "Language: " (:lang d)]
   [:table {:border "1", :width "70%", :bordercolor "brawn", :cellspacing "0", :cellpadding "2"}
    ;(for [[k v] d] (kv-table-row k v "ok"))
    (kv-table-row "lang" (:lang d) "ok")
    (kv-table-row "title" (:title d) "ok")
    (kv-table-row "img" (:img d) "ok")
    (kv-table-row "file" (:file d) "ok")
    (kv-table-row "desc" (:desc d) "ok")]])

(defn render-movie-html [m]
  (let [descriptions (:descriptions m)
        translations (:translations m)]
    [:div.movie-view
     [:h2 [:a {:href "/api/movies-full"} "All Movies"]]
     [:h2 (str "id: " (:id m) " | " (get-movie-title "en" m) " | " (get-movie-title "ru" m))]
     [:table {:border "1", :width "70%", :bordercolor "brawn", :cellspacing "0", :cellpadding "2"}
      (kv-table-row ":id-kp" (:id-kp m) "ok")
      (kv-table-row ":id-imdb" (:id-imdb m)  "ok")
      (kv-table-row ":shortname" (:shortname m) "ok")
      (kv-table-row ":movie-state" (:movie-state m) "ok")
      (kv-table-row ":fpkeys-file" (str (:fpkeys-file m)) "ok")]
      [:p (map render-movie-description descriptions)]
      [:p (map render-movie-translation translations)]
      [:h2 [:a {:href "/api/movies-full"} "All Movies"]]]))

(defn render-movie-title-row-html
  ( [m]
    (let [u (if (nil? m) "/api/movies-full" "/api/movie-full/")]
      [:a {:href (str u (:id m))}
        (str (:id m) " " (get-movie-title "en" m) " / " (get-movie-title "ru" m))]))
  ( [id m]
    (let [u (if (nil? m) "/api/movies-full" "/api/movie-full/")]
      [:a {:href (str u (:id m))}
      (str id " " (get-movie-title "en" m) " / " (get-movie-title "ru" m))])))

(defn render-movies-html [ms]
  [:h2 "Movies:"]
  [:ol
   (for [id ms] [:li (render-movie-title-row-html (fetch-movie id))])])

(defn render-user-data [ud]
  (if (not-empty ud)
    [:div.user-data
     [:table {:border "1", :width "70%", :bordercolor "brawn", :cellspacing "0", :cellpadding "2"} ;{:style "width:70%; border: 1px solid black;"}
      [:tr
       (reduce str (map #(str "<th>" % "</th>") (keys ud)))]
      [:tr
       (reduce str (map #(str "<td>" % "</td>") (vals ud)))]]]))

(defn render-movie-full-edit [id]
  (let [m (fetch-movie id)]
    (if (nil? m)
      (do
        [:head]
        [:body {:style "background: #EFEFEF"}
         [:title "Update Movie"]
         [:div.movie-view
          [:h2 [:a {:href "/api/movies-full"} "All Movies"]]
          [:h2 "Enter new movie"]
          (form/form-to [:post "/api/movie-full/update" :enctype "application/json"]
                        [:p (form/submit-button "UPDATE!")]
                        [:p (form/text-field {:border "1", :width "200pc", :bordercolor "brawn"} "movie-state" "Input here active or old")]
                        [:table {:border "1", :width "70%", :bordercolor "brawn", :cellspacing "0", :cellpadding "2"}
                         (kv-table-5row
                           "id-kp" (form/text-field {:style "width: 100%"} "id-kp" "Input here movie id-kp")
                           "id-imdb" (form/text-field {:style "width: 100%"} "id-imdb" "Input here movie id-imdb")
                           "ok")
                         (kv-table-5row
                           "shortname" (form/text-field {:style "width: 100%"} "shortname" "Input here movie shortname")
                           "fpkeys-file" (form/text-field {:style "width: 100%"} "fpkeys-file" "Input here movie fpkeys-file") "ok")
                         (kv-table-5row
                           "duration" (form/text-field {:style "width: 100%"} "duration" "Input here movie duration")
                           "year-released" (form/text-field {:style "width: 100%"} "year-released" "Input here movie year-released") "ok")]
                        (form/with-group :desc-ru
                                         (form/hidden-field "lang" "ru")
                                         [:div.translation-view
                                          [:h3 "Russian Description"]
                                          [:table {:border "1", :width "70%", :bordercolor "brawn", :cellspacing "0", :cellpadding "2"}
                                           (kv-table-row "Название" (form/text-field {:style "width: 100%"} "title" "Input here movie title") "ok")
                                           (kv-table-row "Описание" (form/text-area {:style "width: 100%" :rows "4"} "desc" "Input here movie desc") "ok")
                                           (kv-table-row "Описание короткое" (form/text-area {:style "width: 100%" :rows "4"} "desc-short" "Input here movie desc-short") "ok")]])
                        (form/with-group :desc-en
                                         (form/hidden-field "lang" "en")
                                         [:div.translation-view
                                          [:h3 "English Description"]
                                          [:table {:border "1", :width "70%", :bordercolor "brawn", :cellspacing "0", :cellpadding "2"}
                                           (kv-table-row "title" (form/text-field {:style "width: 100%"} "title" "Input here movie title") "ok")
                                           (kv-table-row "desc" (form/text-area {:style "width: 100%" :rows "4"} "desc" "Input here movie desc") "ok")
                                           (kv-table-row "desc-short" (form/text-area {:style "width: 100%" :rows "4"} "desc-short" "Input here movie desc-short") "ok")]]))
          ]])
      (do
        [:h2 "NOT NEW"]))))


(def testv1 {:movie-state "Input here active or old",
             :id-imdb "Input here movie id-imdb",
             :fpkeys-file "Input here movie fpkeys-file",
             :duration "Input here movie duration",
             :year-released "Input here movie year-released",
             :desc-ru
             {:desc "Input here movie desc",
              :lang "ru",
              :desc-short "Input here movie desc-short",
              :title "Input here movie title"},
             :shortname "Input here movie shortname",
             :id-kp "Input here movie id-kp",
             :desc-en
             {:desc "Input here movie desc",
              :title "Input here movie title",
              :desc-short "Input here movie desc-short",
              :lang "en"}})

(defn do-movie-full-update [params]
  (pprint params))

;(with-out-str (clojure.pprint/pprint ud))]]]))

;(def mmm {"kp496849" {:dates ["2014-08-31T18:21:43"]}, "kp103391" {:dates ["2014-08-31T17:15:29" "2014-08-31T17:16:36" "2014-08-31T18:04:49" "2014-08-31T18:42:01" "2014-08-31T18:46:35" "2014-08-31T19:34:10" "2014-09-01T19:00:21"]}, "kp646674" {:dates ["2014-08-09T06:09:03" "2014-08-09T12:44:03" "2014-08-31T18:14:06" "2014-08-31T18:57:29"]}, "1005" {:dates ["2014-08-08T12:33:10"]}, "1" {:dates ["2014-08-08T10:11:54" "2014-08-31T18:01:33" "2014-08-31T19:12:04" "2014-08-31T19:33:46" "2014-09-02T16:13:25"]}})

(defn render-mids [mids]
  (if (not-empty mids)
    [:div.mids
     [:ul
      (for [k (keys mids)]
        [:li
         [:em (render-movie-title-row-html (fetch-movie k))]
         [:br]
         (let [dates (get-in mids [k :dates])]
           (with-out-str (clojure.pprint/pprint dates)))])]]))

;(with-out-str (clojure.pprint/pprint mids))]))

(defn render-user-device [u]
  [:div
   [:h4 "Device:"]
   (render-user-data (u :user-data))
   [:h4 "Acquired Movies:"]
   (render-mids (u :mids))
   "<br/>"])

(defn render-user-html [kv]
  (let [acc (key kv)
        uvec (val kv)]
    [:div.user-view
     [:h3 "Account: " acc]
     (map render-user-device uvec)
     [:hr]]))

(defn render-users-html [uids]
  [:html
   [:head
    [:title (str "MovieLex Users: " (count uids))]]
   [:body
    [:h1 (str "MovieLex Users: " (count uids))]
    [:ul
     (for [kv (group-users-by-account-device uids)]
       [:li (render-user-html kv)])]]])

(defn ttt [] (clojure.pprint/pprint (render-users-html (query-users))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;(def users-data "data/users.data")
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
