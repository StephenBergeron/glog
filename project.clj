(defproject glog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]]
  :plugins [[lein-auto "0.1.3"]
            [lein-ancient "0.6.8"]]
  :main ^:skip-aot glog.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
