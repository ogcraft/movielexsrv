(ns movielexsrv.tools.utils
  	(:require
    	clojure.contrib.io
    	clojure.java.io
			clojure.java.shell
      clojure.string
			[clj-time.core :as time]
		  [clj-time.format :as time-format]
		  ;[clj-time.coerce :as time-coerce :exclude [extend second]]
			[cheshire.core :as json]
			[clj-http.client :as client])
  	  (:import java.io.PushbackReader)
      (:import java.net.InetAddress)
   	  (:import java.io.FileReader))

(defn gethostname []
	(let [full-name (:out (clojure.java.shell/sh "hostname"))]
    		(clojure.string/trim-newline 
			(first (clojure.string/split full-name #"\.")))))

(defn ver->int [v]
  (map read-string (clojure.string/split v #"\.")))


(defn upload-movie [m]
	(client/post "http://movielex.com/api/put-movie" {:form-params m :content-type :json}))

(defn upload-movie-from-file [fname]
	(if (.exists (clojure.contrib.io/as-file fname))
		(if-let [ms (load-file fname)]
			(map upload-movie ms))))


