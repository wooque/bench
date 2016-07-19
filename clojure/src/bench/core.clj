(ns bench.core
  (:require [clj-uuid :as uuid] 
            [clojure.java.jdbc :as jdbc] 
            [hikari-cp.core :refer :all]
            [org.httpkit.server :refer :all])
  (:gen-class))
   
(def datasource-options {:adapter            "postgresql"
                         :username           "bench"
                         :password           "bench"
                         :database-name      "bench"
                         :server-name        "127.0.0.1"})

(def datasource
  (make-datasource datasource-options))

(defn get-data []
  (jdbc/with-db-connection [conn {:datasource datasource}]
    (let [rows (jdbc/query conn "SELECT * FROM tst")]
      (println rows)))
  (close-datasource datasource))

(defn handler [req]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello World!"})

(defn async-handler [req]
  (with-channel req channel 
      (send! channel {:status 200
                      :headers {"Content-Type" "text/plain"}
                      :body    "Hello World!"})))


(def routes {"/" handler 
             "/async" async-handler})

(defn router [req]
  (let [handler (routes (:uri req))]
    (if handler 
      (handler req) 
      {:status 404})))

(defn -main[]
    (run-server router {:port 8080}))
