// templates/jspc/_jspc.js.gsp

JSPC = {

    app : { // -- dynamic logic container
    },

    callbacks : {
        modal : { // -- dynamic storage; search modalCallbackFunction@r2d2.js for more information
            onShow : {},
            onVisible : {}
        }
    },

    config : { // -- static var injection
        dateFormat: "${message(code:'default.date.format.notime').toLowerCase()}",
        language:   "${message(code:'default.locale.label').toLowerCase()}",
        server:     "${de.laser.utils.AppUtils.getCurrentServer()}",
        searchSpotlightSearch: "<g:createLink controller='search' action='spotlightSearch'/>",
        ajax: {
            openLogin:    "<g:createLink controller='ajaxOpen' action='login'/>",
            openMessages: "<g:createLink controller='ajaxOpen' action='messages'/>",
            openProfiler: "<g:createLink controller='ajaxOpen' action='profiler'/>",
            jsonLookup:   "<g:createLink controller='ajaxJson' action='lookup'/>",
            htmlDocumentPreview: "<g:createLink controller='ajaxHtml' action='documentPreview'/>"
        },
        jquery: jQuery().jquery
%{--        ws: {--}%
%{--            stompUrl: "${createLink(uri: de.laser.custom.CustomWebSocketMessageBrokerConfig.WS_STOMP)}",--}%
%{--            topicStatusUrl: "${de.laser.custom.CustomWebSocketMessageBrokerConfig.WS_TOPIC_STATUS}",--}%
%{--        }--}%
    },

    modules : { // -- module registry
        registry : new Map(),

        add : function (label, module) {
            if (! JSPC.modules.registry.get (label) ) {
                console.debug ('%c  > adding module ' + label + ' (' + (JSPC.modules.registry.size+1) + ')', 'color:grey');
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
                    console.debug ('%c> running module ' + label + ' (' + (++i) + ')', 'color:grey');
                    JSPC.modules.registry.get (label).go();
                    if ('LOCAL' == JSPC.config.server) { why.tap(); }
                }
                else { console.error ('> module NOT found ? ' + label ); }
            }
        }
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
            'confirm.dialog.unset',
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
    colors : { // -- charts
        palette: [ '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#c4c4c4' ],
        hex : {
            blue:       '#5470c6',
            green:      '#91cc75',
            yellow:     '#fac858',
            red:        '#ee6666',
            ice:        '#73c0de',
            darkgreen:  '#3ba272',
            orange:     '#fc8452',
            purple:     '#9a60b4',
            pink:       '#ea7ccc',
            grey:       '#d4d4d4'
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