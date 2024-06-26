console.log('+ bundle: laser.js');

// bundle: assets/javascripts/laser.js
//
//= require /javascript/semantic.min.js         //--> assets/themes
//
//= require modules/r2d2.js
//= require modules/c3po.js
//= require modules/tooltip.js
//= require modules/bb8.js
//= require modules/a11y/collections/menu.js
//= require modules/a11y/elements/icon.js
//= require modules/a11y/modules/modal.js
//= require modules/docs.js
//= require modules/wysiwyg.js
//= require modules/why.js
//= require modules/verticalNavi.js
//= require modules/responsiveTables.js
//= require modules/textareaHeight.js
//= require modules/system.js

// here we go ..

$(function () {
    JSPC.modules.go( 'r2d2', 'bb8', 'a11yMenu', 'a11yIcon', 'verticalNavi', 'responsiveTables', 'textareaHeight', 'system' );

    $('main.ui.main').removeClass('hidden');
})