(ns mooviefishsrv.views.layout
  (:require [hiccup.page :refer [html5 include-css]]))

(defn common [& body]
  (html5
    [:head
     [:title "mooviefishsrv"]
     (include-css "/css/screen.css")]
    [:body body]))
