/*******************************
          Build Task
*******************************/

var
  // dependencies
  gulp         = require('gulp-help')(require('gulp')),
  runSequence  = require('run-sequence'),

  // config
  config       = require('./config/user'),
  install      = require('./config/project/install'),

  // task sequence
  tasks        = []
;


// sub-tasks
if(config.rtl) {
  require('./collections/rtl')(gulp);
}
require('./collections/build')(gulp);

// 2themes: Festlegung der zwei Themes
const orgs = ['accessibility', 'laser'];

module.exports = function(callback) {

  console.info('Building Semantic');

  if( !install.isSetup() ) {
    console.error('Cannot find semantic.json. Run "gulp install" to set-up Semantic');
    return 1;
  }

  // check for right-to-left (RTL) language
  if(config.rtl === true || config.rtl === 'Yes') {
    gulp.start('build-rtl');
    return;
  }

  if(config.rtl == 'both') {
    tasks.push('build-rtl');
  }


  for (var i = 0; i < orgs.length; i++) {

    const org = orgs[i];

    // 1.) Die theme.config wird dahin kopiert, wo es die Build Engine erwartet
    gulp.task('copy theme.config '+ org, function() {
      return gulp.src('./src/themes/'+org+'/theme.config')
          .pipe(gulp.dest('./src/'));
    });
    //************************ CSS *********************************
    // 2.) Die Build-CSS wird aufgerufen und in den Ordner temp zwischengespeichert
    gulp.task('build css '+org, ['build-css']);

    // 3.) Die gebauten CSS-Files werden in die entsprechenden 'dist' Folder gespeichert
    gulp.task('copy output css '+org, ['build css '+org], function() {
      return gulp.src('./temp/**/*.css')
          .pipe(gulp.dest('../../../grails-app/assets/themes/'+org));
    });

    tasks.push('copy theme.config '+org);
    tasks.push('copy output css '+org);
  }

  //************************ JAVASCRIPT *************************
  // 4.) Die gebauten Javascript-Files werden in die entsprechenden 'dist' Folder gespeichert
  gulp.task('copy output javascript', ['build-javascript','package compressed js','package uncompressed js'], function() {
    return gulp.src('./temp/**/*.js')
        .pipe(gulp.dest('../../../grails-app/assets/themes/javascript'));
  });
  //************************ ASSETS *************************
  // 5.) Nur die Assets-Files aus dem default-Theme werden in den entsprechenden 'dist' Folder gespeichert
  gulp.task('copy assets', function() {
    // copy assets
    return gulp.src('./src/themes/default/assets/**/*.*')
        .pipe(gulp.dest('../../../grails-app/assets/themes/assets'));
  });
  tasks.push('copy output javascript');
  tasks.push('copy assets');

  // läd alle tasks in ein Array- ES6 Spread Operator
  runSequence(...tasks, callback);
};
