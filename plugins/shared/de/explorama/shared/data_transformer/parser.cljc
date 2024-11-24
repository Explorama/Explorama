(ns de.explorama.shared.data-transformer.parser)

(defprotocol Parser
  (parse [this options file])
  (header [this options file])
  (count-rows [this options file]))