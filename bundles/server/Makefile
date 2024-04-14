.DEFAULT_GOAL := build

dist_root=../../dist
dist_folder=${dist_root}/server
target_folder=${dist_root}/Explorama-server
dist_benchmark_folder=${dist_root}/server-profiling

install:
	npm install

assets:
	bash gather-assets.sh ${build_mode}

compile:
	lein uberjar

clean-before:
	if [ "$(build_mode)" = "prod" ] || [ "$(build_mode)" = "benchmark" ]; then \
	    rm -rf ${dist_folder}; \
		rm -rf ${dist_benchmark_folder}; \
		mkdir -p ${dist_root}; \
	    cd ${dist_root} && rm -rf Explorama-server.zip; \
	elif [ "$(build_mode)" = "dev" ]; then \
		rm -rf resources/public/js/woco.js; \
		rm -rf resources/public/js/compiled; \
	elif [ "$(build_mode)" = "dev-benchmark" ]; then \
		rm -rf resources/public/js/woco-benchmark.js; \
		rm -rf resources/public/js/compiled-benchmark; \
	fi
	rm -rf node_modules
	rm -rf target

dev: build_mode=dev
dev: clean-before install assets
	lein repl

dev-benchmark: build_mode=dev-benchmark
dev-benchmark: clean-before install assets
	lein with-profile dev-benchmark figwheel dev-benchmark

prepare-test:
	npm install -g karma-cli 
	npm install karma@6.4.1 karma-coverage karma-chrome-launcher karma-cljs-test karma-junit-reporter karma-htmlfile-reporter --save-dev

test-backend: lein test

test-frontend: prepare-test
	lein test-frontend

test: prepare-test
	lein test-frontend
	lein test

prepare-prod: build_mode=prod
prepare-prod: clean-before install assets compile
	rm -rf ${dist_folder}/prepared/resources/public/js/woco-sources
	rm -rf ${dist_folder}/prepared/classes
	rm -rf ${dist_folder}/prepared/cljsbuild-crossover
	rm -rf ${dist_folder}/prepared/stale
	rm -rf ${dist_folder}/prepared/explorama-0.0.0.jar
	mv ${dist_folder}/prepared ${target_folder}

bundle:  
	cd ${dist_root} && zip -q -r Explorama-server.zip Explorama-server
	mv ${target_folder} ${dist_folder}

build: prepare-prod bundle
