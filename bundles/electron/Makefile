.DEFAULT_GOAL := build-win

dist_root=../../dist/electron
dist_folder=${dist_root}/prepared
ifeq ($(OS), Windows_NT) 
	curr_platform:=win 
else 
	curr_platform:=linux 
endif

install-builder:
	npm install -g electron-builder

install:
	npm install

assets:
	bash gather-assets.sh ${build_mode}

prebuild-node-modules:
	bash prebuild-node-modules.sh ${target_platform} ${build_mode}

compile:
	lein with-profile build cljsbuild once min-main min-frontend min-backend

clean-before:
	if [ "$(build_mode)" = "prod" ]; then \
	    echo "rm -rf ${dist_root}"; \
	    rm -rf ${dist_root}; \
	fi
	rm -rf node_modules
	rm -rf target 
	rm -rf resources/public/js
	rm -rf resources/main.js

dev: build_mode=dev
dev: target_platform=${curr_platform}
dev: clean-before install assets prebuild-node-modules
	lein with-profile dev figwheel main-dev dev-frontend dev-backend

dev-app:
	@if [ "$curr_platform" = "win" ]; then\
		node_modules/.bin/electron.cmd ./resources/main.js;\
	else\
		node_modules/.bin/electron ./resources/main.js;\
	fi

prepare-test: clean-before install assets 
	npm install -g karma-cli 
	npm install karma@6.4.1 karma-coverage karma-chrome-launcher karma-cljs-test karma-junit-reporter karma-htmlfile-reporter --save-dev

test: build_mode=dev
test: prepare-test
	lein with-profile test doo node test-backend once
	lein with-profile test doo chrome-headless test-frontend once

test-backend: build_mode=dev
test-backend: prepare-test
	lein with-profile test doo node test-backend once

test-frontend: build_mode=dev
test-frontend: prepare-test
	lein with-profile test doo chrome-headless test-frontend once

prepare-prod: build_mode=prod
prepare-prod: clean-before install assets prebuild-node-modules compile
	cp clean_modules.sh ${dist_folder}/
	cp -rf resources/public/app ${dist_folder}/public/
	cd ${dist_folder} && bash clean_modules.sh
	cd ${dist_folder} && rm clean_modules.sh
	cd ${dist_folder} && npm install
	cd ${dist_folder} && npm install -g modclean
	rm -rf ${dist_folder}/public/js/frontend
	rm -rf ${dist_folder}/public/js/backend
	rm -rf ${dist_folder}/public/js/main

bundle-win: install-builder
	cd ${dist_folder} && electron-builder --win
	cd ${dist_folder} && mv dist/*.exe ../
	cd ${dist_root} && rm -rf prepared

build-win: target_platform=win
build-win: prepare-prod bundle-win

bundle-linux: install-builder
	cd ${dist_folder} && electron-builder --linux
	cd ${dist_folder} && mv dist/*.AppImage ../
	cd ${dist_root} && rm -rf prepared
	
build-linux: target_platform=linux	
build-linux: prepare-prod bundle-linux

.PHONY: install
