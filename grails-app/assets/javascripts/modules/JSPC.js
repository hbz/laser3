
// modules/JSPC.js

JSPC = {
    currLanguage : $('html').attr('lang'),

    CB : { // storage for dynamic callbacks
        modal : {
            show : {
            }
        },
        ajaxPostFunc : function () { console.log('JSPC.CB.ajaxPostFunc - default') }
    }
}