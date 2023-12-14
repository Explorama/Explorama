module.exports = function (grunt) {
	const sass = require('sass');

	grunt.loadNpmTasks('grunt-contrib-connect');
	grunt.loadNpmTasks('grunt-contrib-watch');
	grunt.loadNpmTasks('grunt-contrib-copy');
	grunt.loadNpmTasks('grunt-contrib-clean');
	grunt.loadNpmTasks('grunt-contrib-cssmin');
	grunt.loadNpmTasks('grunt-sass');
	grunt.loadNpmTasks('grunt-svg-css');
	grunt.loadNpmTasks('grunt-svgmin');
	grunt.loadNpmTasks('grunt-inline-css');

	grunt.initConfig({

		/* ############
		   DEV ENV
		############ */

		// Start local web servers
		connect: {
			// Separate server and live reloading for email development
			emails: {
				options: {
					port: 8082,
					base: 'emails',
					hostname: 'localhost',
					livereload: 35730
				}
			}
		},

		// Watch files/folders and trigger tasks on change
		watch: {
			scss: {
				files: ['src/scss/**/*.scss'],
				tasks: ['sass:dist'],
				options: {
					livereload: true
				}
			},
			img: {
				files: ['src/img/*'],
				tasks: ['copy:img'],
				options: {
					livereload: true,
					//spawn: false
				}
			},
			img_mosaic: {
				files: ['src/img/mosaic/*'],
				tasks: ['copy:img_mosaic'],
				options: {
					livereload: true,
					//spawn: false
				}
			},
			img_svg: {
				files: ['src/img/svg-img/*'],
				tasks: ['copy:img_svg'],
				options: {
					livereload: true,
					//spawn: false
				}
			},
			fonts: {
				files: ['src/fonts'],
				tasks: ['copy:fonts'],
				options: {
					livereload: true,
					//spawn: false
				}
			},
			dist: {
				files: ['dist/**/*'],
				tasks: ['copy:dist_other', 'copy:mockups_dist_browser_dev'],
				options: {
					livereload: true,
					//spawn: false
				}
			},
			other: {
				files: ['other/**/*'],
				tasks: ['copy:mockups_other'],
				options: {
					livereload: true,
					//spawn: false
				}
			},
			html: {
				files: ['mockups/**/*.html'],
				options: {
					livereload: true
				}
			},
			svg: {
				files: ['src/img/svg/*.svg'],
				tasks: ['svgmin'],
				options: {
					livereload: true,
					//spawn: false
				}
			},
			svgmin: {
				files: ['src/img/svg/svgmin/*.svg'],
				tasks: ['svgcss:iconmap'],
				options: {
					livereload: true
				}
			},
			emails_scss: {
				files: ['emails/src/scss/*.scss'],
				tasks: ['sass:emails'],
				options: {
					livereload: 35730
				}
			},
			emails_html: {
				files: ['emails/src/html/*.html'],
				options: {
					livereload: 35730
				}
			}
		},

		// Copy files tasks
		copy: {
			// fonts to dist
			fonts: {
				files: [{
					expand: true,
					cwd: 'src/fonts',
					src: '*',
					dest: 'dist/fonts'
				}]
			},
			// img to dist
			img: {
				files: [{
					expand: true,
					cwd: 'src/img/',
					src: ['*.jpg', '*.png', '*.svg', '*.gif'],
					dest: 'dist/img/'
				}]
			},
			img_mosaic: {
				files: [{
					expand: true,
					cwd: 'src/img/mosaic/',
					src: ['*.jpg', '*.png', '*.svg', '*.gif'],
					dest: 'dist/img/mosaic/'
				}]
			},
			img_svg: {
				files: [{
					expand: true,
					cwd: 'src/img/svg-img/',
					src: ['*.svg'],
					dest: 'dist/img/svg/'
				}]
			},
			dist_other: {
				files: [{
					expand: true,
					cwd: 'other',
					src: ['**/*'],
					dest: 'dist/css'
				}]
			},
			mockups_dist_browser_dev: {
				files: [{
					expand: true,
					cwd: 'other',
					src: ['**/*'],
					dest: '../bundles/browser/resources/public/css'
				}]
			}
		},

		/* ############
		   CLEAN
		############ */

		// Delete copied/generated files if source file is deleted
		clean: null, // path is set using the watch event

		/* ############
		   SASS
		############ */

		// Compile SCSS to CSS
		sass: {
			options: {
				implementation: sass,
				outputStyle: 'expanded'
			},
			dist: {
				files: [{
					expand: true,
					cwd: 'src/scss/',
					src: ['*.scss'],
					dest: 'dist/css/',
					ext: '.css',
					rename: function (dest, src) {
						return dest + '3_' + src;
					}
				}]
			},
			emails: {
				files: [{
					expand: true,
					cwd: 'emails/src/scss/',
					src: ['*.scss'],
					dest: 'emails/src/css/',
					ext: '.css'
				}]
			}
		},
		cssmin: {
			target: {
				files: [{
					expand: true,
					cwd: 'dist/css/',
					src: ['*.css', '1_vendor/*.css', '!1_vendor/*.min.css', '!*.min.css'],
					dest: 'dist/css/',
					ext: '.css'
				}]
			}
		},

		/* ############
		   SVG ICONS
		############ */

		// Minify svg files
		svgmin: {
			options: {
				plugins: [
					{
						removeViewBox: false
					},
					{
						removeUselessStrokeAndFill: false
					}
				]
			},
			dist: {
				files: [{
					expand: true,
					cwd: 'src/img/svg',
					src: ['*.svg'],
					dest: 'src/img/svg/svgmin'
				}]
			}
		},

		// Convert svg files into single data-uri svg map
		svgcss: {
			iconmap: {
				options: {
					cssprefix: '',
					csstemplate: 'css.hbs',
					previewhtml: null
				},
				files: {
					'src/scss/base/_iconmap.scss': ['src/img/svg/svgmin/*.svg']
				}
			}
		},

		/* ############
		   EMAILS
		############ */

		// Inline css for emails
		inlinecss: {
			main: {
				options: {
				},
				files: [{
					expand: true,
					cwd: 'emails/src/html',
					src: '*.html',
					dest: 'emails/dist/',
					ext: '.html'
				}]
			}
		}
	});

	// TODO: Currently only works when deleting one file at a time + has some other bugs
	/*
	grunt.event.on('watch', function(action, filepath) {
		if (action === 'deleted') {
			var target = '';
			if (filepath.startsWith('src\\img\\svg\\')) {
				target = filepath.replace('svg', 'svg\\svgmin');
			} else if (filepath.startsWith('src')) {
				target = filepath.replace('src', 'dist');
				if (filepath.includes('svg-img\\')) {
					target = filepath.replace('svg-img', 'svg');
				}
			}
			grunt.log.writeln(target);
			grunt.config('clean', [target]);
			grunt.task.run('clean');
		}
	});
	*/

	// Default: Build files for dist, then start web server and watch files for changes
	grunt.registerTask('default', ['build', 'connect', 'watch']);
	// Dev: Start web server and watch files for changes
	grunt.registerTask('dev', ['connect', 'watch']);
	// Init: Initialize repository for first setup
	grunt.registerTask('init', ['svgmin', 'svgcss', 'sass', 'copy', 'inlinecss']);
	// Build: Build files for dist (done automatically when developing with the web server running)
	grunt.registerTask('build', ['svgmin', 'svgcss', 'sass:dist', 'copy:fonts', 'copy:img', 'copy:img_mosaic', 'copy:img_svg', 'copy:dist_other']);
	grunt.registerTask('prod-build', ['svgmin', 'svgcss', 'sass:dist', 'copy:fonts', 'copy:img', 'copy:img_mosaic', 'copy:img_svg', 'copy:dist_other', 'cssmin']);
	// Emails: Generate html emails with inlined css
	grunt.registerTask('emails', ['inlinecss']); // TODO: automatically convert embedded images to base64
};