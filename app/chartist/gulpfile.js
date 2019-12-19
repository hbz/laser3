 
var gulp = require('gulp');
var sass = require('gulp-sass');
 
sass.compiler = require('node-sass');
 
gulp.task('build', function () {
  return gulp.src('./src/chartist.scss')
    .pipe(sass().on('error', sass.logError))
    .pipe(gulp.dest('./dest/chartist.css'));
});
 
gulp.task('sass:watch', function () {
  gulp.watch('./sass/**/*.scss', ['sass']);
});