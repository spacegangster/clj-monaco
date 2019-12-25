(ns clj-monaco.test-runner
  (:require
    [goog.object :as gobj]
    [cljs.test :refer-macros [run-tests]]
    [cljs-test-display.core :as td]
    [figwheel.main.testing :refer-macros [run-tests-async]]
    [clj-monaco.core-test]))

(when (= "/tests.html"
         (gobj/getValueByKeys goog/global "location" "pathname"))
  (run-tests
    (td/init! "root")
    'clj-monaco.core-test))


(defn -main [& args]
  (run-tests-async 10000))