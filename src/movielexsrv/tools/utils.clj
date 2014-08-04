(ns movielexsrv.tools.utils
  	(:require
    	clojure.contrib.io
    	clojure.java.io
      clojure.string
  		[clj-time.core :as time]
		  [clj-time.format :as time-format]
		  ;[clj-time.coerce :as time-coerce :exclude [extend second]]
			[cheshire.core :as json])
  	  (:import java.io.PushbackReader)
      (:import java.net.InetAddress)
   	  (:import java.io.FileReader))

(defn gethostname []
	(let [full-name (.getCanonicalHostName (java.net.InetAddress/getLocalHost))]
    (first (clojure.string/split full-name #"\."))))

