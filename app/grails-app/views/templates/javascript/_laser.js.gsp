// templates/javascript/_laser.js.gsp

var laser = {
    gspLocale: "${message(code:'default.locale.label')}",
    gspDateFormat: "${message(code:'default.date.format.notime').toLowerCase()}",
    gspAjaxLookupUrl: "<g:createLink controller='ajax' action='lookup'/>",
    gspSpotlightSearchUrl: "<g:createLink controller='search' action='spotlightSearch'/>"
}
