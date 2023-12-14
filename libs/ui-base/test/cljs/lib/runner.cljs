(ns lib.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [lib.core-test]
            [lib.utils.data-exchange-test]))

(doo-tests 'lib.core-test
           'lib.utils.data-exchange-test)
