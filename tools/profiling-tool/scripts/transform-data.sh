#!/bin/bash

builder_path="../../cli-data-transformer/target/builder.sh"

bash $builder_path gen csv-mapping-a.clj ../gen_raw_data/data-a-1k.csv ../gen_resources/exp-import/data-a-1k.edn
bash $builder_path gen csv-mapping-a.clj ../gen_raw_data/data-a-10k.csv ../gen_resources/exp-import/data-a-10k.edn
bash $builder_path gen csv-mapping-a.clj ../gen_raw_data/data-a-100k.csv ../gen_resources/exp-import/data-a-100k.edn
bash $builder_path gen csv-mapping-a.clj ../gen_raw_data/data-a-1m.csv ../gen_resources/exp-import/data-a-1m.edn

bash $builder_path gen csv-mapping-b.clj ../gen_raw_data/data-b-1k.csv ../gen_resources/exp-import/data-b-1k.edn
bash $builder_path gen csv-mapping-b.clj ../gen_raw_data/data-b-10k.csv ../gen_resources/exp-import/data-b-10k.edn
bash $builder_path gen csv-mapping-b.clj ../gen_raw_data/data-b-100k.csv ../gen_resources/exp-import/data-b-100k.edn
bash $builder_path gen csv-mapping-b.clj ../gen_raw_data/data-b-1m.csv ../gen_resources/exp-import/data-b-1m.edn
