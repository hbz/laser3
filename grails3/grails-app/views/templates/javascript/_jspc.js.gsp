// templates/javascript/_jspc.js.gsp

JSPC = {
    currLanguage : $('html').attr('lang'),

    vars : { // -- var injection
        locale: "${message(code:'default.locale.label')}",
        dateFormat: "${message(code:'default.date.format.notime').toLowerCase()}",
        ajaxProfilerUrl: "<g:createLink controller='ajaxOpen' action='profiler'/>",
        ajaxStatusUrl: "<g:createLink controller='ajaxOpen' action='status'/>",
        ajaxMessagesUrl: "<g:createLink controller='ajaxOpen' action='messages'/>",
        ajaxJsonLookupUrl: "<g:createLink controller='ajaxJson' action='lookup'/>",
        spotlightSearchUrl: "<g:createLink controller='search' action='spotlightSearch'/>",
    },

    app : { // -- logic container
    },

    callbacks : {
        modal : { // -- dynamic storage; search modalCallbackFunction@r2d2.js for more information
            show : {
            }
        },
        dynPostFunc : function () { console.log('JSPC.callbacks.dynPostFunc - default') }
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
        },
        contains : function (list, value) {
            return $.inArray(value, list) >= 0
        }
    }
}