(ns dice.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [dice.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'dice.core-test))
    0
    1))
