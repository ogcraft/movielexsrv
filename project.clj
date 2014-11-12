(defproject movielexsrv "0.1.4"
  :description "MovieLex site back-end"
  :url "http://movielex.com/"
  ;; CLJ AND CLJS source code path
  :source-paths ["src" "src-cljs"]
  
  :dependencies [[org.clojure/clojure "1.6.0"]
  [org.clojure/clojure-contrib "1.2.0"]
  [clj-time "0.7.0"]
  [compojure "1.1.6"]
  [hiccup "1.0.5"]
  [liberator "0.11.0"]
  [cheshire "5.3.1"]
  [lib-noir "0.8.2"]
  [com.ashafa/clutch "0.4.0-RC1"]
  [ring-server "0.3.1"]
  [domina "1.0.2"]
  [enlive "1.1.5"]
  [selmer "0.6.9"]
  [com.novemberain/welle "3.0.0"]
  [clj-http "1.0.1"]
  [org.clojure/clojurescript "0.0-2197"]]

  :plugins [[lein-ring "0.8.10"] 
            [lein-cljsbuild "1.0.3"]]
  :ring {:handler movielexsrv.handler/app
    :nrepl {:start? true :port 9998}
    :init movielexsrv.handler/init
    :destroy movielexsrv.handler/destroy}
    :aot :all
    :auto-clean false
    :profiles
    {:production
      {:ring
      {:open-browser? false, :stacktraces? false, :auto-reload? false}}
        :dev
      {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.2.1"]]}}
  ;; cljsbuild options configuration
  :cljsbuild {:builds
              [{;; CLJS source code path
                :source-paths ["src-cljs"]
                ;; Google Closure (CLS) options configuration
                :compiler {;; CLS generated JS script filename
                           :output-to "resources/public/js/movielexsrv.js"
                           ;; minimal JS optimization directive
                           :optimizations :whitespace
                           ;; generated JS code prettyfication
                           :pretty-print true}}]})
