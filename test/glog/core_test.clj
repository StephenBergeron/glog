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
  (map (fn [x] (apply (partial dissoc x) lowpass)) glog))

;; (def details
;;   (map (fn [x] (dissoc x "wfm_cloudappname" "db_dump")) glog))


(def tdh240
  (loop [pile  (reverse glog)
         futur (drop 1 pile)
         res   nil]
    (let [p (first pile)
          h (select-keys p lowpass)
          d (apply (partial dissoc p) lowpass)
          rho (concat res d h)]
      (if (empty? futur)
        rho
        (let [f  (first futur)
              hf (select-keys f lowpass)
              eta  (if (= h hf)
                   (concat res d '("------"))
                   (concat res d '("======")))]
          (recur (drop 1 pile) (drop 1 futur) eta))))))


;; (defn tdh238 [acc datum]
;;   (let [
;;         ]
;;     ) [d h_cond])

;; (def header-visibility
;;   "Extract the header for the current datum. The difficulty here is to
;;   deal with the [previous current] datum. My first attempt is to start
;;   from the end"
;;   ;; Everything that belongs to the lowpass
;;   ;; and that is different from the previous
;;   ;; is assembled into the glog-header
;;   ;; (map (fn [x] ({x ""}))
;;   ;; let's start from the end
;;   (reduce tdh238 (drop 1 (reverse glog)) (reverse glog)))



;; (def header-construct
;;   (let [s1 glog
;;         s2 glog]))

(t/deftest adhoc-test
  (t/testing "Run some adhoc evaluation."
    (do
      ;; (printf "reverse tdh240: %n")
      ;; (clojure.pprint/pprint (reverse tdh240))
      (printf "detail: %n")
      (clojure.pprint/pprint details)
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
