// templates/javascript/_jspc.dict.js.gsp

<%@ page import="java.util.Locale;java.util.List" %>
<%
    Locale localeDe = new Locale.Builder().setLanguage("de").build()
    Locale localeEn = new Locale.Builder().setLanguage("en").build()

    List<String> translations = [
            'confirm.dialog.clearUp',
            'confirm.dialog.concludeBinding',
            'confirm.dialog.delete',
            'confirm.dialog.inherit',
            'confirm.dialog.ok',
            'confirm.dialog.share',
            'confirm.dialog.unlink',
            'default.actions.label',
            'default.informations',
            'link.readless',
            'link.readmore',
            'loc.January', 'loc.February', 'loc.March', 'loc.April', 'loc.May', 'loc.June', 'loc.July', 'loc.August', 'loc.September', 'loc.October', 'loc.November', 'loc.December',
            'loc.weekday.short.Sunday','loc.weekday.short.Monday','loc.weekday.short.Tuesday','loc.weekday.short.Wednesday','loc.weekday.short.Thursday','loc.weekday.short.Friday','loc.weekday.short.Saturday',
            'property.select.loadMore',
            'property.select.noMatches',
            'property.select.placeholder',
            'property.select.searching',
            'search.API.heading.noResults',
            'search.API.logging',
            'search.API.maxResults',
            'search.API.method',
            'search.API.noEndpoint',
            'search.API.noTemplate',
            'search.API.serverError',
            'search.API.source',
            'statusbar.hideButtons.tooltip',
            'statusbar.showButtons.tooltip',
            'xEditable.button.cancel',
            'xEditable.button.ok',
            'responsive.table.selectElement',
            'pagination.keyboardInput.validation.integer',
            'pagination.keyboardInput.validation.smaller',
            'pagination.keyboardInput.validation.biggerZero'
    ]

    println """

JSPC.dict = {
    get: function (key, lang) {
        return JSPC.dict[key][lang]
    },"""

    translations.eachWithIndex { it, index ->
        String tmp = "    '${it}' : { "
        tmp =  tmp + "de: '" + message(code: "${it}", locale: localeDe) + "', en: '" + message(code: "${it}", locale: localeEn) + "'"
        tmp =  tmp + (index < translations.size() - 1 ? " }, " : " }")
        println raw(tmp)
    }
    println "} "

%>