// templates/jspc/_jspc.js.gsp

JSPC = {
    currLanguage : $('html').attr('lang'),

    vars : { // -- var injection
        dateFormat: "${message(code:'default.date.format.notime').toLowerCase()}",
        locale: "${message(code:'default.locale.label')}",
        server: "${de.laser.utils.AppUtils.getCurrentServer()}",
        searchSpotlightSearch: "<g:createLink controller='search' action='spotlightSearch'/>",
        ajax: {
            openMessages: "<g:createLink controller='ajaxOpen' action='messages'/>",
            openProfiler: "<g:createLink controller='ajaxOpen' action='profiler'/>",
            jsonLookup: "<g:createLink controller='ajaxJson' action='lookup'/>",
            htmlDocumentPreview: "<g:createLink controller='ajaxHtml' action='documentPreview'/>"
        },
        ws: {
            stompUrl: "${createLink(uri: de.laser.custom.CustomWebSocketConfig.WS_STOMP)}",
            topicStatusUrl: "${de.laser.custom.CustomWebSocketConfig.WS_TOPIC_STATUS}",
        }
    },

    modules : { // -- module registry
        registry : new Map(),

        add : function (label, module) {
            if (! JSPC.modules.registry.get (label) ) {
                console.log ('  /- adding module ' + (JSPC.modules.registry.size+1) + ' > ' + label);
                JSPC.modules.registry.set (label, module);
                if (window[label] != module) {
                    console.warn ('  /- module OVERRIDES existing property ? ' + label);
                    window[label] = module;
                }
            }
            else { console.log ('  /- module EXISTS and ignored ? ' + label); }
        },
        go : function ( /*labels*/ ) {
            console.log ('JSPC.modules.go( ' + arguments.length + ' )')
            let i = 0;
            for (let label of arguments) {
                if (JSPC.modules.registry.get (label)) {
                    console.log ('  \\- running module ' + (++i) + ' > ' + label);
                    JSPC.modules.registry.get (label).go();
                    why.tap();
                }
                else { console.log ('  \\- module NOT found ? ' + label ); }
            }
        }
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