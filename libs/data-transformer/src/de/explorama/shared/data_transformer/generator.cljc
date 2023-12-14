(ns de.explorama.shared.data-transformer.generator)

(defprotocol Generator
  (state [this])
  (finalize
    [this data format])
  (fact
    [this name type value])
  (context
    [this global-id type name])
  (context-ref [this global-id rel-type rel-name])
  (location
    [this pos])
  (date
    [this type value])
  (text
    [this value])
  (texts
    [this texts])
  (datasource
    [this global-id name opts]
    [this global-id name])
  (item [this global-id features])
  (feature [this global-id facts locations context-refs dates texts])
  (data [this contexts datasource items]))
