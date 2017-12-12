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
  (loop [p0  0
         p1  1
         acc [(first header)]]           ; the first item must have a visible header
    (if (< p1 (count header))
        (let [h0 (nth header p0)
              h1 (nth header p1)
              [_ delta _]  (data/diff h0 h1)
              epsilon (if (nil? delta) delta (into delta {"header" true "emphasize" "-----"}))]
          (recur p1 (+ p1 1) (conj acc epsilon)))
        (lazy-seq acc))))

(def glog-preproc
  (interleave header-visibility details))



(t/deftest adhoc-test
  (t/testing "Run some adhoc evaluation."
    (do
      ;(printf "header: %n")
      ;(print header)
      (printf "interleave: %n")
      (clojure.pprint/pprint glog-preproc)
      ;(printf "header-visibility: %n")
      ;(clojure.pprint/pprint header-visibility)
      ;; (printf "tdh240: %n")
      ;; (clojure.pprint/pprint tdh240)
      ;; (printf "detail: %n")
      ;; (clojure.pprint/pprint details)
      ;;(printf "header: %n")
      ;;(clojure.pprint/pprint header)
      (t/is (> 42 13 ))
      )))


(t/deftest glog-preproc-test
  (t/testing "The cardinality of glog-preproc is twice the original tabloid."
    (t/is (= (count glog-preproc) (* 2 (count glog))))))

(t/deftest cardinality-interleave-test
  (t/testing "The cardinality of the interleave tabloid is the same as the header and the details."
    (t/is (= (count (interleave header details)) (+ (count header) (count details))))))

(t/deftest cardinality-header-test
  (t/testing "The cardinality of the header tabloid is the same as the original tabloid."
    (t/is (= (count glog) (count header) ))))

(t/deftest cardinality-detail-test
  (t/testing "The cardinality of the details tabloid is the same as the original tabloid."
    (t/is (= (count glog) (count details)))))

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
