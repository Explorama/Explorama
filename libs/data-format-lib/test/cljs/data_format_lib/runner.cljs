(ns data-format-lib.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [data-format-lib.filter]
            [data-format-lib.core-test]
            [data-format-lib.data-test]
            [data-format-lib.date-filter-test]
            [data-format-lib.standard-filter-test]
            [data-format-lib.simplified-view-test]
            [data-format-lib.operations-indicator-test]
            [data-format-lib.operations-test]
            [data-format-lib.operations-mosaic-test]))

(doo-tests 'data-format-lib.core-test
           'data-format-lib.data-test
           'data-format-lib.date-filter-test
           'data-format-lib.operations-indicator-test
           'data-format-lib.operations-test
           'data-format-lib.standard-filter-test
           'data-format-lib.simplified-view-test
           'data-format-lib.operations-mosaic-test)
