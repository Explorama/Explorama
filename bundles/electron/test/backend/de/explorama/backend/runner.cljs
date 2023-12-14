(ns de.explorama.backend.runner
  (:require [de.explorama.backend.algorithms.data.future-data-test]
            [de.explorama.backend.algorithms.data.redo-test]
            [de.explorama.backend.algorithms.data.train-data-test]
            [de.explorama.backend.algorithms.prediction-registry.expdb-backend-test]
            [de.explorama.backend.expdb.ac-api-test]
            [de.explorama.backend.expdb.indexed-db-test]
            [de.explorama.backend.expdb.simple-db-test]
            [de.explorama.shared.indicator.transform-test]
            [de.explorama.shared.mosaic.group-by-layout-test]
            [de.explorama.shared.search.date-utils-test]
            [doo.runner :refer-macros [doo-tests]]))

(doo-tests 'de.explorama.shared.indicator.transform-test
           'de.explorama.shared.mosaic.group-by-layout-test
           'de.explorama.shared.search.date-utils-test
           'de.explorama.backend.expdb.ac-api-test
           'de.explorama.backend.expdb.simple-db-test
           'de.explorama.backend.expdb.indexed-db-test
           'de.explorama.backend.algorithms.data.future-data-test
           'de.explorama.backend.algorithms.data.redo-test
           'de.explorama.backend.algorithms.data.train-data-test
           'de.explorama.backend.algorithms.prediction-registry.expdb-backend-test)