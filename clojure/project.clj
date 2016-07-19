(defproject bench "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.postgresql/postgresql "9.4.1209"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [hikari-cp "1.7.2"]
                 [danlentz/clj-uuid "0.1.6"]
                 [http-kit "2.2.0"]]
  
  :global-vars {*warn-on-reflection* true}
  :main bench.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
