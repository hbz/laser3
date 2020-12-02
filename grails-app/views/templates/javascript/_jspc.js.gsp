// templates/javascript/_jspc.js.gsp

JSPC = {
    currLanguage : $('html').attr('lang'),

    gsp : {
        locale: "${message(code:'default.locale.label')}",
        dateFormat: "${message(code:'default.date.format.notime').toLowerCase()}",
        ajaxLookupUrl: "<g:createLink controller='ajaxJson' action='lookup'/>",
        spotlightSearchUrl: "<g:createLink controller='search' action='spotlightSearch'/>",
    },

    callbacks : {
        modal : { // dynamic storage; search modalCallbackFunction@r2d2.js for more information
            show : {
            }
        },
        ajaxPostFunc : function () { console.log('JSPC.callbacks.ajaxPostFunc - default') }
    },

    goBack : function() {
        window.history.back();
    }
}