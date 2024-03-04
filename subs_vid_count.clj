#!/usr/bin/env bb

(ns subs-vid-count
  (:require [babashka.http-client :as http]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.pprint :refer [pprint]]
            [clojure.edn :as edn]))

(def api-url (System/getenv "api_url"))
(def api-key (System/getenv "api_key"))

(as-> "subs_list.edn" $
  (with-open [reader (io/reader $)]
    (edn/read (java.io.PushbackReader. reader)))
  (get $ "subscriptions")
  (map #(http/post (str api-url "/getSubscription?apiKey=" api-key) {:headers {"Accept" "application/json"
                                                                               "Content-Type" "application/json"}
                                                                     :body (json/generate-string {"name" (get %1 "name")})}) $)
  (map #(json/parse-string (:body %1)) $)
  (map #(let [subscription (get %1 "subscription")
              files (get %1 "files")]
          {:name (get subscription "name")
           :vid-count (count files)}) $)
  (first $)
  (println $))
