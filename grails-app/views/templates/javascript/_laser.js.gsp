// templates/javascript/_laser.js.gsp

laser = {
    gspLocale: "${message(code:'default.locale.label')}",
    gspDateFormat: "${message(code:'default.date.format.notime').toLowerCase()}",
    gspAjaxLookupUrl: "<g:createLink controller='ajaxJson' action='lookup'/>",
    gspSpotlightSearchUrl: "<g:createLink controller='search' action='spotlightSearch'/>"
}
