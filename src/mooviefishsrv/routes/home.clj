(ns mooviefishsrv.routes.home
  (:require [compojure.core :refer :all]
            [mooviefishsrv.views.layout :as layout]))

(defn home []
  (layout/common [:h1 "Hello OG!"]))

(defroutes home-routes
  (GET "/" [] (home)))
