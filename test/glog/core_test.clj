(ns glog.core-test
  (:require [clojure.test :as t]
            [glog.core :as sut]
            [clojure.set :as set]
            [clojure.data.json :as json]))

(def glog
  "The glog file content in a tabloid data structure."
  (map json/read-str
       (clojure.string/split
        (slurp (System/getenv "___glog_file")) #"\n")))

(def common-keys
  "Each glog entries have a common set of keys"
  (reduce set/intersection (map (fn [x] (into #{} (keys x))) glog)))

(defn glog-header
  "Extract the header for the current datum"
  [previous current]
  ;; Everything that belongs to the common-keys
  ;; and that is different from the previous
  ;; is assembled into the glog-header
  nil)

;; (def header-construct
;;   (let [s1 glog
;;         s2 glog]))


(t/deftest glog-type-test
  (t/testing "The tabloid structure is a lazy sequence of some Map."
    (t/is (= clojure.lang.LazySeq (type glog)))
    (t/is (= clojure.lang.PersistentArrayMap (type (last glog))))))


(t/deftest glog-contains-data-test
  (t/testing "Ensure that we have some data in the glog."
    (t/is (> (count glog) 10))))

(t/deftest common-keys-are-well-known-test
  (t/testing "The common keys are well known"
    (t/is (=
           #{"ts" "db_dump" "context" "simulation" "wfm_cloudappname"}
           common-keys))))
