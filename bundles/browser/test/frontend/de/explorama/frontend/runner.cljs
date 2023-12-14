(ns de.explorama.frontend.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [de.explorama.frontend.algorithms.components.parameter-test]
            [de.explorama.frontend.algorithms.components.helper-test]
            [de.explorama.frontend.algorithms.operations.redo-test]
            [de.explorama.frontend.data-atlas.db-utils-test]
            [de.explorama.shared.indicator.transform-test]
            [de.explorama.frontend.indicator.management-test]
            [de.explorama.frontend.map.operations.redo-test]
            [de.explorama.frontend.map.impl.openlayers.util-test]
            [de.explorama.frontend.mosaic.data-structure.impl-tests]
            [de.explorama.frontend.mosaic.data-structure.data-format-test]
            [de.explorama.frontend.mosaic.data-structure.nested-test]
            [de.explorama.frontend.mosaic.operations.nested-filter-test]
            [de.explorama.shared.mosaic.group-by-layout-test]
            [de.explorama.frontend.projects.projects-test]
            [de.explorama.frontend.search.core-test]
            [de.explorama.shared.search.date-utils-test]
            [de.explorama.frontend.woco.details-view-test]
            [de.explorama.frontend.woco.notifications-test]
            [de.explorama.frontend.woco.filter-test]
            [de.explorama.frontend.woco.operations-test]
            [de.explorama.shared.interval.validation-test]))



(doo-tests
 ;algorithms tests
 'de.explorama.frontend.algorithms.components.parameter-test
 'de.explorama.frontend.algorithms.components.helper-test
 'de.explorama.frontend.algorithms.operations.redo-test
 ;data-atlas tests
 'de.explorama.frontend.data-atlas.db-utils-test
 ;indicator tests
 'de.explorama.frontend.indicator.management-test
 'de.explorama.shared.indicator.transform-test
 ;map tests
 'de.explorama.frontend.map.operations.redo-test
 'de.explorama.frontend.map.impl.openlayers.util-test
 ;mosaic tests
 'de.explorama.frontend.mosaic.data-structure.impl-tests
 'de.explorama.frontend.mosaic.data-structure.data-format-test
 'de.explorama.frontend.mosaic.data-structure.nested-test
 'de.explorama.frontend.mosaic.operations.nested-filter-test
 'de.explorama.shared.mosaic.group-by-layout-test
 ;projects tests
 'de.explorama.frontend.projects.projects-test
 ;search tests
 'de.explorama.frontend.search.core-test
 'de.explorama.shared.search.date-utils-test
 ;woco tests
 'de.explorama.frontend.woco.details-view-test
 'de.explorama.frontend.woco.notifications-test
 'de.explorama.frontend.woco.filter-test
 'de.explorama.frontend.woco.operations-test

 ;shared
 'de.explorama.shared.interval.validation-test)
