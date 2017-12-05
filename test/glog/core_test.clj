(ns glog.core-test
  (:require [clojure.test :as t]
            [glog.core :as sut]
            [clojure.set :as set]
            [clojure.data.json :as json]))


(def all-records
  (map json/read-str (clojure.string/split (slurp "/home/stn/cache/glog.json") #"\n")))

(def common-keys
  (reduce set/intersection (map (fn [x] (into #{} (keys x))) all-records)))

;; (def header-construct
;;   (let [s1 all-records
;;         s2 all-records]))


(t/deftest slurp-1-test
  (t/testing
      (t/is (= clojure.lang.LazySeq (type all-records)))))

(t/deftest slurp-3-test
  (t/testing
      (t/is (= 69 (count all-records)))))

(t/deftest slurp-5-test
  (t/testing
      (t/is (=
             #{"ts" "db_dump" "context" "simulation" "wfm_cloudappname"}
             common-keys))))
