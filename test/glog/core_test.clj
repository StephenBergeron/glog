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

(def lowpass
  "Each glog entries have a common set of keys.  This set is suitable to
  focus on large block of execution.  There are strong analogies with
  lowpass filter in signal theory. Even the ordering of the column name
  is similar to the frequency or a wavenumber."
  ["wfm_cloudappname" "db_dump" "context" "simulation" ])

(def header
  (map (fn [x] (select-keys x lowpass)) glog))

(def details
  (map (fn [x] (dissoc x lowpass)) glog))



(defn glog-header-internal
  "Extract the header for the current datum"
  [previous current]
  ;; Everything that belongs to the lowpass
  ;; and that is different from the previous
  ;; is assembled into the glog-header
  nil)



;; (def header-construct
;;   (let [s1 glog
;;         s2 glog]))

(t/deftest adhoc-test
  (t/testing "Run some adhoc evaluation."
    (do
      (printf "header: %n")
      (clojure.pprint/pprint details)
      (t/is (> 42 13 )))))

(t/deftest glog-type-test
  (t/testing "The tabloid structure is a lazy sequence of some Map."
    (t/is (= clojure.lang.LazySeq (type glog)))
    (t/is (= clojure.lang.PersistentArrayMap (type (last glog))))))


(t/deftest glog-contains-data-test
  (t/testing "Ensure that we have some data in the glog."
    (let [card (count glog)]
      (do
        (printf "glog cardinality: %s%n" card)
        (t/is (> card 30 ))))))
