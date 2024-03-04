#!/usr/bin/env bb

(ns restart-downloads
  (:require [babashka.http-client :as http]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.pprint :refer [pprint]]
            [clojure.walk :as walk]))

(def api-url (System/getenv "api_url"))
(def api-key (System/getenv "api_key"))
(def dl-limit (Integer/parseInt (System/getenv "dl_limit")))
(def wait-seconds (Integer/parseInt (System/getenv "wait_seconds")))

(declare handle-downloads)

; splits list into finished and unfinished
(defn funished-unfinished [to-split]
  (reduce (fn [{finished :finished unfinished :unfinished} curr]
            (if (get curr "finished")
              {:finished (conj finished curr) :unfinished unfinished}
              {:finished finished :unfinished (conj unfinished curr)})) {:finished [] :unfinished []} to-split))

(defn paused-unpaused [to-split]
  (reduce (fn [{paused :paused unpaused :unpaused} curr]
            (if (get curr "paused")
              {:paused (conj paused curr) :unpaused unpaused}
              {:paused paused :unpaused (conj unpaused curr)})) {:paused [] :unpaused []} to-split))

(loop []
  (as-> (str api-url "/downloads?apiKey=" api-key) $
    (http/post $ {:headers {"Accept" "application/json"}})
    (json/parse-string (:body $))
    (get $ "downloads")
    (funished-unfinished $)
    (if (= (count (:unfinished $)) 0)
      (println "No unfinished downloads. Exiting.")
      (as-> (:unfinished $) $$
        (paused-unpaused $$)
        (do
          (dorun (map #(pprint (walk/keywordize-keys (select-keys %1 ["title" "sub_name" "percent_complete"]))) (:unpaused $$)))
          $$)
        (let [in-progress (count (:unpaused $$))
              to-start-count (- dl-limit in-progress)
              to-start (take to-start-count (:paused $$))]
          (if (< in-progress dl-limit)
            (do
              (println "Kicking off some new downloads")
              (dorun (map #(http/post (str api-url "/resumeDownload?apiKey=" api-key) {:headers {"Content-Type" "application/json"}
                                                                                       :body (json/generate-string {"download_uid" (get %1 "uid")})}) to-start)))
            (println "Already enough downloads in progress. Skipping.")))
        (println (str "Unfinished downloads: " (count (:unfinished $))))
        (println)
        (println)
        (Thread/sleep (* wait-seconds 1000))
        (recur)))))
