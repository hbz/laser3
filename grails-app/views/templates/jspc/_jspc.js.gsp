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
            stompUrl: "${createLink(uri: de.laser.custom.CustomWebSocketMessageBrokerConfig.WS_STOMP)}",
            topicStatusUrl: "${de.laser.custom.CustomWebSocketMessageBrokerConfig.WS_TOPIC_STATUS}",
        }
    },

    modules : { // -- module registry
        registry : new Map(),

        add : function (label, module) {
            if (! JSPC.modules.registry.get (label) ) {
                console.log ('  > adding module ' + label + ' (' + (JSPC.modules.registry.size+1) + ')');
                JSPC.modules.registry.set (label, module);
                if (window[label] != module) {
                    console.warn ('  > module OVERRIDES existing property ? ' + label);
                    window[label] = module;
                }
            }
            else { console.log ('  > module EXISTS and ignored ? ' + label); }
        },
        go : function ( /*labels*/ ) {
            console.log ('JSPC.modules.go( ' + arguments.length + ' modules )')
            let i = 0;
            for (let label of arguments) {
                if (JSPC.modules.registry.get (label)) {
                    console.log ('>> running module ' + label + ' (' + (++i) + ')');
                    JSPC.modules.registry.get (label).go();
                    why.tap();
                }
                else { console.log ('>> module NOT found ? ' + label ); }
            }
        }
    },

    app : { // -- dynamic logic container
    },

    callbacks : {
        modal : { // -- dynamic storage; search modalCallbackFunction@r2d2.js for more information
            show : {
            }
        },
        dynPostFunc : function () { console.log('JSPC.callbacks.dynPostFunc - default') }
    },

    dict : { // -- js translations
        registry : {
<%
    Locale localeDe = de.laser.utils.LocaleUtils.getLocaleDE()
    Locale localeEn = de.laser.utils.LocaleUtils.getLocaleEN()

    List<String> translations = [
            'confirm.dialog.clearUp',
            'confirm.dialog.concludeBinding',
            'confirm.dialog.delete',
            'confirm.dialog.inherit',
            'confirm.dialog.ok',
            'confirm.dialog.share',
            'confirm.dialog.unlink',
            'copied',
            'default.actions.label',
            'default.informations',
            'dropdown.message.addResult',
            'link.readless',
            'link.readmore',
            'loc.January', 'loc.February', 'loc.March', 'loc.April', 'loc.May', 'loc.June', 'loc.July', 'loc.August', 'loc.September', 'loc.October', 'loc.November', 'loc.December',
            'loc.weekday.short.Sunday','loc.weekday.short.Monday','loc.weekday.short.Tuesday','loc.weekday.short.Wednesday','loc.weekday.short.Thursday','loc.weekday.short.Friday','loc.weekday.short.Saturday',
            'pagination.keyboardInput.validation.integer',
            'pagination.keyboardInput.validation.smaller',
            'pagination.keyboardInput.validation.biggerZero',
            'property.select.loadMore',
            'property.select.noMatches',
            'property.select.placeholder',
            'property.select.searching',
            'responsive.table.selectElement',
            'search.API.heading.noResults',
            'search.API.logging',
            'search.API.maxResults',
            'search.API.method',
            'search.API.noEndpoint',
            'search.API.noTemplate',
            'search.API.serverError',
            'search.API.source',
            'select2.placeholder',
            'select2.noMatchesFound',
            'statusbar.hideButtons.tooltip',
            'statusbar.showButtons.tooltip',
            'xEditable.button.cancel',
            'xEditable.button.ok'
    ]
    translations.eachWithIndex { it, index ->
        String tmp = "            '${it}' : { "
        tmp =  tmp + "de: '" + message(code: "${it}", locale: localeDe) + "', en: '" + message(code: "${it}", locale: localeEn) + "'"
        tmp =  tmp + (index < translations.size() - 1 ? " }, " : " }")
        println raw(tmp)
    }
%>
        },
        get : function (key, lang) {
            return JSPC.dict.registry[key][lang]
        },
    },

    helper : { // -- snippets only
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