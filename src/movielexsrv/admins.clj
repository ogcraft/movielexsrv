(ns movielexsrv.admins
  (:require [cemerick.friend.credentials :as creds]))

; a dummy in-memory user "database"
(def admins {"ogcraft" {:username "ogcraft"
                        :password (creds/hash-bcrypt "ogcraft")
                        :roles    #{::admin}}
             "pavela"  {:username "pavela"
                        :password (creds/hash-bcrypt "pavela")
                        :roles    #{::admin}}})
