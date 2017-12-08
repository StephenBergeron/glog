(ns glog.core-test
  (:require [clojure.test :as t]
            [glog.core :as sut]
            [clojure.set :as set]
            [clojure.data.json :as json]
            [clojure.data :as data]))

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
  (map (fn [x] (apply (partial dissoc x) lowpass)) glog))

(def header-visibility
  (loop [p0 0
         p1 1
         acc [(first header)]]           ; the first item must have a visible header
    (if (< p1 (count header))
        (let [h0 (nth header p0)
              h1 (nth header p1)
              [delta1 delta2 delta3]  (data/diff h0 h1)]
          (if (nil? delta1)
              (recur p1 (+ p1 1) (conj acc {}))
              (recur p1 (+ p1 1) (conj acc h1))))
        (lazy-seq acc))))

(t/deftest adhoc-test
  (t/testing "Run some adhoc evaluation."
    (do
      (printf "header: %n")
      (print header)
      (printf "header-visibility: %n")
      (clojure.pprint/pprint header-visibility)
      ;; (printf "tdh240: %n")
      ;; (clojure.pprint/pprint tdh240)
      ;; (printf "detail: %n")
      ;; (clojure.pprint/pprint details)
      ;;(printf "header: %n")
      ;;(clojure.pprint/pprint header)
      (t/is (> 42 13 ))
      )))

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
