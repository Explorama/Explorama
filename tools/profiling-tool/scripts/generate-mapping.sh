#!/bin/bash

builder_path="../../cli-data-transformer/target/builder.sh"

bash $builder_path gen-mapping csv.clj ../gen_raw_data/data-a-1k.csv csv-mapping-a.clj

echo "Replace Placeholder with Data-A and source-data-a"

bash $builder_path gen-mapping csv.clj ../gen_raw_data/data-a-1k.csv csv-mapping-b.clj

echo "Replace Placeholder with Data-B and source-data-b"
