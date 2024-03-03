#!/usr/bin/env bb

(ns ytdl-material-scripts
  (:require [babashka.http-client :as http]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.pprint :refer [pprint]]))

(def api-url (System/getenv "api_url"))
(def api-key (System/getenv "api_key"))

(as-> (str api-url "/getSubscriptions?apiKey=" api-key) $
  (http/post $ {:headers {"Accept" "application/json"}})
  (json/parse-string (:body $))
  (with-open [out (io/writer "subs-list.edn")]
    (pprint $ out)))
