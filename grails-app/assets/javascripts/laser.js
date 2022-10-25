// bundle: assets/javascripts/laser.js
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
//= require modules/docs.js
//= require modules/why.js
//= require modules/verticalNavi.js
//= require modules/responsiveTables.js
//= require modules/setTextareaHeight.js
//= require modules/system.js
//= require wysiwyg.js                          //--> bundle
//= require modules/wysiwyg.js
//= require spring-websocket.js                 //--> bundle

console.log('+ bundle: laser.js')

// here we go ..

$(function () {
    if (JSPC.vars.server === 'LOCAL') {
        r2d2.go();              why.tap();
        bb8.go();               why.tap();
        a11yMenu.go();          why.tap();
        a11yIcon.go();          why.tap();
        verticalNavi.go();      why.tap();
        responsiveTables.go();  why.tap();
        system.go();            why.tap();
    }
    else {
        r2d2.go();
        bb8.go();
        a11yMenu.go();
        a11yIcon.go();
        verticalNavi.go();
        responsiveTables.go();
        system.go();
    }

    $('main.ui.main').removeClass('hidden');
})
setTextareaHeight.go();