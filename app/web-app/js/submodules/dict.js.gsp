
// submodules/dict.js

<%@ page import="java.util.Locale;java.util.List" %>
<%
    Locale localeDe = new Locale.Builder().setLanguage("de").build()
    Locale localeEn = new Locale.Builder().setLanguage("en").build()

    List<String> translations = [
        'statusbar.hideButtons.tooltip',
        'statusbar.showButtons.tooltip',
        'default.informations',
        'default.actions.label',
        'property.select.placeholder',
        'property.select.searching',
        'property.select.loadMore',
        'property.select.noMatches',
        'confirm.dialog.delete',
        'confirm.dialog.unlink',
        'confirm.dialog.share',
        'confirm.dialog.inherit',
        'confirm.dialog.ok',
        'confirm.dialog.concludeBinding',
        'confirm.dialog.clearUp',
        'loc.January', 'loc.February', 'loc.March', 'loc.April', 'loc.May', 'loc.June', 'loc.July', 'loc.August', 'loc.September', 'loc.October', 'loc.November', 'loc.December',
        'loc.weekday.short.Sunday','loc.weekday.short.Monday','loc.weekday.short.Tuesday','loc.weekday.short.Wednesday','loc.weekday.short.Thursday','loc.weekday.short.Friday','loc.weekday.short.Saturday'
    ]

println """

dict = {
    get: function (key, lang) {
        return dict[key][lang]
    },"""

    translations.eachWithIndex { it, index ->
    println "    '${it}' : {"
    println "        de: '" + message(code: "${it}", locale: localeDe) + "',"
    println "        en: '" + message(code: "${it}", locale: localeEn) + "' "
    println (index < translations.size() - 1 ? "    }, " : "    }")
}
println "} "

%>