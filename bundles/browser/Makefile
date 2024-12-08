.DEFAULT_GOAL := build

dist_root=../../dist
target_folder=vite-target

install:
	npm install

assets:
	bash gather-assets.sh ${build_mode}

compile:
	npx shadow-cljs compile app
	vite build --mode production

clean-before:
	rm -rf assets
	rm -rf vite-target
	rm -rf node_modules
	rm -rf target

dev: build_mode=dev
dev: clean-before install assets
	npx shadow-cljs compile app
	vite dev &
	$! > vite.pid
	npx shadow-cljs watch app
	
vite-stop:
	kill `cat vite.pid`
	rm vite.pid

prepare-test:
	npm install -g karma-cli 
	npm install karma@6.4.1 karma-coverage karma-chrome-launcher karma-cljs-test karma-junit-reporter karma-htmlfile-reporter --save-dev

test: prepare-test
	lein test

test-watch:
	npx shadow-cljs watch test --config-merge '{:autorun true}'

prepare-prod: build_mode=prod
prepare-prod: clean-before install assets compile
	bb merge-build-assets.bb
	npx shadow-cljs release app

bundle:  
	cd ${target_folder} && zip -q -r Explorama-browser.zip *
	mkdir -p ${dist_root}/browser
	mv ${target_folder}/Explorama-browser.zip ${dist_root}/browser/

build: prepare-prod bundle
