(ns e2e.fixtures.dataset)

;; Browser-bundle-specific facts about the sample datasources the suite
;; drives through the UI. Kept in one place so a future server-bundle run
;; of these specs only has to change values here.

(def netflix-name "Netflix")
(def netflix-event-count "323 Events")

(def import-name "e2e-import")
(def import-event-count "3 Events")

(def units-import-name "e2e-units")
(def units-event-count "3 Events")
(def units-fact-column "runtime")
(def units-fact-unit "min")
(def units-fact-header "runtime (min)")

(def tool-count 7)
