(ns bench.core
  (:require [clj-uuid :as uuid]
            [cheshire.core :refer :all]
            [clojure.java.jdbc :as jdbc] 
            [hikari-cp.core :refer :all]
            [org.httpkit.server :refer :all]
            [clojure.core.async :refer [chan go >! <!!]])
  (:gen-class))
  
(set! *warn-on-reflection* true)
   
(def datasource-options {:adapter            "postgresql"
                         :username           "bench"
                         :password           "bench"
                         :database-name      "bench"
                         :server-name        "127.0.0.1"})

(def datasource
  (make-datasource datasource-options))

(defn query-data []
  (jdbc/with-db-connection [conn {:datasource datasource}]
    (let [rows (jdbc/query conn "SELECT txt FROM tst LIMIT 1")]
      (or (first rows) {:txt nil}))))
      
(defn insert-data []
  (jdbc/with-db-connection [conn {:datasource datasource}]
    (let [data {:txt (str (uuid/v1))}]
      (jdbc/insert! conn :tst data)
      data)))
      
(defn update-data []
  (jdbc/with-db-connection [conn {:datasource datasource}]
    (let [data (str (uuid/v1))]
      (jdbc/execute! conn ["UPDATE tst SET txt=? WHERE id in (SELECT id FROM tst LIMIT 1)" data])
      {:txt data})))

(defn delete-data []
  (jdbc/with-db-connection [conn {:datasource datasource}]
    (let [rows (jdbc/query conn "DELETE FROM tst WHERE id in (SELECT id FROM tst LIMIT 1) RETURNING id")]
      (or (first rows) {:id nil}))))

(defn work [c]
  (go (>! c 
    (let [coin (rand-int 4)]
      (condp = coin
        0 (query-data)
        1 (insert-data)
        2 (update-data)
        3 (delete-data)
        {:error "error"})))))

(defn handler [req]
  (let [c (chan)]
    (work c)
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (generate-string (<!! c))}))

(defn async-handler [req]
  (with-channel req channel   
    (let [c (chan)]
      (work c)
      (send! channel {:status 200
                      :headers {"Content-Type" "application/json"}
                      :body    (generate-string (<!! c))}))))

(def routes {"/" handler 
             "/async" async-handler})

(defn router [req]
  (let [handler (routes (:uri req))]
    (if handler 
      (handler req) 
      {:status 404})))

(defn -main[]
    (run-server router {:port 8080}))
