(ns de.explorama.profiling-tool.runner
  (:require
   [de.explorama.profiling-tool.benchmark]
   [de.explorama.profiling-tool.verticals.expdb-import]
   [doo.runner :refer-macros [doo-tests]]))

(doo-tests 'de.explorama.profiling-tool.verticals.expdb-import)