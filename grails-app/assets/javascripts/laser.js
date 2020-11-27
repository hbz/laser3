// FileName: laser.js
//
//= require /javascript/semantic.min.js         // themes
//= require modules/tmp_semui.js
//= require modules/r2d2.js
//= require modules/c3po.js
//= require modules/decksaver.js
//= require modules/tooltip.js
//= require modules/bb8.js
//= require modules/a11y/collections/menu.js
//= require modules/a11y/elements/icon.js
//= require modules/jsqtk.js

console.log('+ laser.js')

// javascript page controller

JSPC = {
    CB : { // storage for dynamic callbacks
        modal : {
            show : {
            }
        }
    }
}

// here we go ..

$(document).ready(function() {
    r2d2.go();
    bb8.go();
    tooltip.go();
    a11yMenu.go();
    a11yIcon.go();
})