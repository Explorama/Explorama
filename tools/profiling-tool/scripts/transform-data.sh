#!/bin/bash

builder_path="../../cli-data-transformer/target/builder.sh"

bash $builder_path gen csv-mapping-a.clj ../gen_raw_data/data-a-1k.csv ../gen_resources/exp-import/data-a-1k.json
bash $builder_path gen csv-mapping-a.clj ../gen_raw_data/data-a-10k.csv ../gen_resources/exp-import/data-a-10k.json
bash $builder_path gen csv-mapping-a.clj ../gen_raw_data/data-a-100k.csv ../gen_resources/exp-import/data-a-100k.json
bash $builder_path gen csv-mapping-a.clj ../gen_raw_data/data-a-1m.csv ../gen_resources/exp-import/data-a-1m.json

bash $builder_path gen csv-mapping-b.clj ../gen_raw_data/data-b-1k.csv ../gen_resources/exp-import/data-b-1k.json
bash $builder_path gen csv-mapping-b.clj ../gen_raw_data/data-b-10k.csv ../gen_resources/exp-import/data-b-10k.json
bash $builder_path gen csv-mapping-b.clj ../gen_raw_data/data-b-100k.csv ../gen_resources/exp-import/data-b-100k.json
bash $builder_path gen csv-mapping-b.clj ../gen_raw_data/data-b-1m.csv ../gen_resources/exp-import/data-b-1m.json
