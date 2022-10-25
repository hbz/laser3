// bundle: assets/javascripts/accessibility.js
//
//= require /javascript/semantic.min.js         //--> assets/themes
//= require modules/r2d2.js
//= require modules/c3po.js
//= require modules/decksaver.js
//= require modules/tooltip.js
//= require modules/bb8.js
//= require modules/a11y/collections/menu.js
//= require modules/a11y/elements/icon.js
//= require modules/a11y/modules/modal.js
//= require modules/why.js
//= require modules/verticalNavi.js
//= require modules/responsiveTables.js

console.log('+ bundle: accessibility.js')

// here we go ..

$(function () {
    r2d2.go();
    bb8.go();
    a11yMenu.go();
    a11yIcon.go();
    verticalNavi.go();
    responsiveTables.go();

    $('main.ui.main').removeClass('hidden');
})
setTextareaHeight.go();
