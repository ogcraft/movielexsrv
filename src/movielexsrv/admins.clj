(ns movielexsrv.admins
  (:require [cemerick.friend.credentials :as creds]))

; a dummy in-memory user "database"
(def admins {"ogcraft"  {:username "ogcraft"
                         :password (creds/hash-bcrypt "c20h25n3o")
                         ;:password "$2a$10$qDK4cTg5y0pmbct/LBQCZOW28/dT3lWMUnG.xcKfKQevKlUnugIaO"
                         :roles    #{::admin}}
             "webadmin" {:username "webadmin"
                         ;:password "$2a$10$qDK4cTg5y0pmbct/LBQCZOW28/dT3lWMUnG.xcKfKQevKlUnugIaO"
                         :password (creds/hash-bcrypt "c2h5oh")
                         :roles    #{::admin}}})
