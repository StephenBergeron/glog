(ns glog.core-test
  (:require [clojure.test :as t]
            [glog.core :as sut]
            [clojure.set :as set]
            [clojure.data.json :as json]
            [clojure.data :as data]
            [clojure.java.shell :as shell]))

(def glog
  "The glog file content in a tabloid data structure."
  (map json/read-str
       (clojure.string/split
        (slurp (System/getenv "___glog_file")) #"\n")))

;; ;; ---
;; ;; TODO - need to add string delimiter with
;; ;;   left delimiter  "msg":
;; ;;   right delimiter "}"
;; ;; TODO - round the log file timestamp to the minute
;; ;;   As far as the sequence is correct, we don't care about fraction of seconds
;; (defn wfc-log [file]
;;   (map json/read-str
;;        (clojure.string/split
;;         (slurp file) #"\n")))

(t/deftest glog-type-test
  (t/testing
      "The glog tabloid structure is a lazy sequence of some Map."
    (t/is (= clojure.lang.LazySeq (type glog)))
    (let [tlg (type (last glog))
          pam (= clojure.lang.PersistentArrayMap tlg)
          phm (= clojure.lang.PersistentHashMap tlg)]
      (t/is (or pam phm) ))))

(t/deftest glog-cardinality-test
  (t/testing
      "The cardinality of glog is sufficient to do some analysis."
    (do
      (printf "cardinality glog: %s%n" (count glog))
      (t/is (> (count glog) 30)))))

(t/deftest glog-cloudapp-test
  (t/testing
      "There are at least one cloudapp to analyze."
    (let [capps (distinct (map (fn [x] (get x "wfm_cloudappname")) glog))]
      (do
        (doseq [app capps]
          (printf "cloudapp: %s%n" app))
        (t/is (> (count capps) 0))))))

(defn delta-chain
  "Take a raw tabloid structure and compute the change for each step delta."
  [tabl]
  (loop [p0  0
         p1  1
         acc [(first tabl)]] ; the first item must be fully visible
    (if (< p1 (count tabl))
        (let [h0 (nth tabl p0)
              h1 (nth tabl p1)
              [_ delta _]  (data/diff h0 h1)]
          (recur p1 (+ p1 1) (conj acc delta)))
        (lazy-seq acc))))

(def glog-delta (delta-chain glog))


(t/deftest glog-delta-cardinality-test
  (t/testing
      "The cardinality of glog-delta is the same as the original tabloid."
    (let [fdelta (str (System/getenv "___glog_file") ".delta.json") ]
      (do
        (printf "Cardinality glog-delta: %s%n" (count glog-delta))
        (spit fdelta (json/write-str glog-delta))
        (t/is (= (count glog-delta) (count glog)))))))




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
  (delta-chain header))

(def glog-preproc-raw
  (interleave header-visibility details))

(def glog-preproc
  (remove nil? glog-preproc-raw))




;; (t/deftest adhoc-test
;;   (t/testing "Run some adhoc evaluation."
;;     (do
;;       ;(printf "header: %n")
;;       ;(print header)
;;       (printf "interleave: %n")
;;       (clojure.pprint/pprint glog-preproc)
;;       (spit (str (System/getenv "___glog_file") ".analysis.json") (json/write-str glog-preproc))
;;       ;(printf "header-visibility: %n")
;;       ;(clojure.pprint/pprint header-visibility)
;;       ;; (printf "tdh240: %n")
;;       ;; (clojure.pprint/pprint tdh240)
;;       ;; (printf "detail: %n")
;;       ;; (clojure.pprint/pprint details)
;;       ;;(printf "header: %n")
;;       ;;(clojure.pprint/pprint header)
;;       (t/is (> 42 13 ))
;;       )))


(t/deftest cardinality-header-test
  (t/testing
      "The cardinality of the header tabloid is the same as the original tabloid."
    (t/is (= (count glog) (count header) ))))

(t/deftest cardinality-detail-test
  (t/testing
      "The cardinality of the details tabloid is the same as the original tabloid."
    (t/is (= (count glog) (count details)))))

(t/deftest glog-contains-data-test
  (t/testing
      "Ensure that we have some data in the glog."
    (let [card  (count glog)
          card2 (count glog-preproc-raw)
          card3 (count glog-preproc)]
      (do
        (printf "cardinality glog-preproc-raw: %s%n" card2)
        (printf "cardinality glog-preproc:     %s%n" card3)
        (spit (str (System/getenv "___glog_file") ".analysis.json") (json/write-str glog-preproc))
        (t/is (> card 30 ))))))

(t/deftest glog-preproc-raw-cardinality-test
  (t/testing
      "The cardinality of glog-preproc in raw format is twice the original tabloid."
    (t/is (= (count glog-preproc-raw) (* 2 (count glog))))))

(t/deftest glog-preproc-cardinality-test
  (t/testing
      "The cardinality of glog-preproc is smaller than the raw."
    (t/is (< (count glog-preproc) (count glog-preproc-raw)))))
