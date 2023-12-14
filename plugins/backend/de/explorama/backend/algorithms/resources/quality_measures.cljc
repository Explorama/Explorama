(ns de.explorama.backend.algorithms.resources.quality-measures)

(def value
  {;;;------ Measures are designed as follows ---------------------------------------------------
  ;;; :ModelOrAlgorithmName {:model :OtherKeyDefinedInThisFile
  ;;;                        :prediction {"MetricNameAsSeenInAlgoritms" {:type -see Options below-
  ;;;                                                                    :header true/false
  ;;;                                                                    :value -see Options below-}}
  ;;;                        :data {}}
  ;;; :model is optional; a model can be named anything and can be used to set metrics
  ;;;    across many algorithms (for a list of algorithms see algorithms.edn)
  ;;;    setting the same metric in an algorithm again will override its model setting
  ;;; :data works like :prediction, but on the input data (currently not implemented)
  ;;; :header is optional and per default false; the marked metric will decide the
  ;;;    overall grading; when multiple are set the worst result of all ":header true"
  ;;;    metrics decides the overall grading of the model
  ;;;------ Options explained ------------------------------------------------------------------
  ;;; :range (as :type) may be given as vector [x, y, z] (as :value), resulting in 
  ;;;    yellow between x / y, green between y / z and red in every other case.
  ;;; :compare (as :type) may be given as any vector (as :value), resulting in green
  ;;;    if the result is equal to one of the given values and red otherwise.
  ;;;------ Settings ---------------------------------------------------------------------------

  ;; Tests
   :trend {:prediction {"P-VALUE" {:type :range
                                   :header true
                                   :value [0.05, 0.01, 0]}}}

  ;; linear
   :linear-model {:data {}
                  :prediction {"R2" {:type :range
                                     :header true
                                     :value [0.4, 0.6, 0.9]}}}
   :lmmr  {:model :linear-model}
   :lr-skl  {:model :linear-model}
   :er {:model :linear-model}
   :pnr {:model :linear-model}
   :nlr {:model :linear-model}
   :linear-regression {:model :linear-model}

  ;; linear time-series
   :linear-timeseries-model {:data {}
                             :prediction {}}
   :lmtts {:model :linear-timeseries-model}
   :lr-apache {:model :linear-model}
   :arima {:model :linear-timeseries-model}
   :aarima {:model :linear-timeseries-model}})
