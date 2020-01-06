var gulp = require('gulp');
var sass = require('gulp-sass');
var postcss = require('gulp-postcss');
sass.compiler = require('node-sass');

gulp.task('build', function () {
    return gulp.src('./src/chartist.scss')
        .pipe(sass().on('error', sass.logError))
        .pipe(gulp.dest('./dist/css'))
        .pipe(postcss())
        .pipe(gulp.dest('../web-app/chartist/css'));
});