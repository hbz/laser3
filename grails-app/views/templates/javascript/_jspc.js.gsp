// templates/javascript/_jspc.js.gsp

JSPC = {
    currLanguage : $('html').attr('lang'),

    gsp : { // -- var injection
        locale: "${message(code:'default.locale.label')}",
        dateFormat: "${message(code:'default.date.format.notime').toLowerCase()}",
        ajaxLookupUrl: "<g:createLink controller='ajaxJson' action='lookup'/>",
        spotlightSearchUrl: "<g:createLink controller='search' action='spotlightSearch'/>",
    },

    callbacks : {
        modal : { // -- dynamic storage; search modalCallbackFunction@r2d2.js for more information
            show : {
            }
        },
        ajaxPostFunc : function () { console.log('JSPC.callbacks.ajaxPostFunc - default') }
    },

    helper : { // -- often used snippets
        goBack : function() {
            window.history.back();
        },
        formatDate : function (input) {
            if (input.match(/^\d{2}[\.\/-]\d{2}[\.\/-]\d{2,4}$/)) {
                let inArr = input.split(/[\.\/-]/g);
                return inArr[2] + "-" + inArr[1] + "-" + inArr[0];
            }
            else {
                return input;
            }
        }
    }
}