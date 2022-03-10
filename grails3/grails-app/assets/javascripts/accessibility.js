// FileName: accessibility.js
//
//= require /javascript/semantic.min.js         //-- themes
//= require modules/r2d2.js
//= require modules/c3po.js
//= require modules/decksaver.js
//= require modules/tooltip.js
//= require modules/bb8.js
//= require modules/a11y/collections/menu.js
//= require modules/a11y/elements/icon.js
//= require modules/a11y/modules/modal.js
//= require modules/jsqtk.js
//= require modules/verticalNavi.js
//= require modules/responsiveTables.js

console.log('+ accessibility.js')

// here we go ..

$(function () {
    r2d2.go();
    bb8.go();
    tooltip.go();
    a11yMenu.go();
    a11yIcon.go();

    $('main.ui.main').removeClass('hidden');
    verticalNavi.go();
    responsiveTables.go();
})