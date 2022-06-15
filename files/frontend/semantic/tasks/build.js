/*******************************
 *         Build Task
 *******************************/
var del = require('del');


var
    // dependencies
    gulp     = require('gulp'),


    // config
    install  = require('./config/project/install')
;

//Kontante deklarieren mit einer Quelle und einem Ziel und angeben, dass man gulp benötigt
const {
  src,
  dest,
  parallel,
  series,
  watch
} = require('gulp');

const orgs = [
  'laser',
  'accessibility',
];

//////////////////////////////////////////////////////
// Copy theme.config from theme folder to place where gulp needs it
//////////////////////////////////////////////////////
function copyThemeLaser(cb) {
  var org = 'laser';
  return src('./src/themes/' + org + '/theme.config').pipe(dest('./src/'));
  //cb();
}
function copyThemeAccessibility(cb) {
  var  org = 'accessibility';
  return src('./src/themes/' + org + '/theme.config').pipe(dest('./src/'));
  //cb();
}
//////////////////////////////////////////////////////

//////////////////////////////////////////////////////
// CSS is build from less and put in temp folder
//////////////////////////////////////////////////////

//////////////////////////////////////////////////////
// CSS is copied to folder where grails need it
//////////////////////////////////////////////////////
function copyOutputCssLaser(cb) {

  const org = 'laser';
  return gulp.src('./temp/**/*.css')
  .pipe(gulp.dest('../../../grails-app/assets/themes/'+org));

};
function copyOutputCssAccessibility(cb) {
  const org = 'accessibility';
  return gulp.src('./temp/**/*.css')
  .pipe(gulp.dest('../../../grails-app/assets/themes/'+org));

};
//////////////////////////////////////////////////////

/////////////////////////////////////////////////////
// Javascript
/////////////////////////////////////////////////////
function copyJavascript() {
  return gulp.src('./temp/**/*.js')
  .pipe(gulp.dest('../../../grails-app/assets/themes/javascript'));
}
/////////////////////////////////////////////////////
// Assets
/////////////////////////////////////////////////////
function copyAssets() {
  // copy assets
  return gulp.src('./src/themes/default/assets/**/*.*')
  .pipe(gulp.dest('../../../grails-app/assets/themes/assets'));
}

function clear() {
  return del([
    './src/theme.config'
  ]);

}

module.exports = function (callback) {
  // Tasks to define the execution of the functions simultaneously or in series
  parallel( series(clear, copyThemeLaser, 'build-css', copyOutputCssLaser, clear, copyThemeAccessibility, 'build-css', copyOutputCssAccessibility),
      series('build-javascript',copyJavascript),
      series('build-assets', copyAssets))(callback);
}
