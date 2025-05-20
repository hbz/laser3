package de.laser

import grails.plugin.springsecurity.SpringSecurityUtils

class UrlMappings {

    static excludes = [ "/static/**" ]

    static mappings = {

        "/"             (uri: SpringSecurityUtils.securityConfig.auth.loginFormUrl)
        "/robots.txt"   (controller: 'public', action: 'robots')

        // gasco

        "/gasco"                (controller: 'public', action: 'gasco')
        "/gasco/details/$id"    (controller: 'public', action: 'gascoDetails')
        "/gasco/json"           (controller: 'public', action: 'gascoJson')

        // ajax

        "/ajax/json/$action?/$id?"      (controller: 'ajaxJson')
        "/ajax/html/$action?/$id?"      (controller: 'ajaxHtml')
        "/ajax/$action?/$id?"           (controller: 'ajax')

        // api

        "/api/$version/specs.yaml"          (controller: 'api', action: 'loadSpecs')
        "/api/$version/changelog.md"        (controller: 'api', action: 'loadChangelog')
        "/api/$version/ezb/$obj"            (controller: 'api', action: 'dispatch') { section = "ezb" }
        "/api/$version/ezb/$obj/$cmd"       (controller: 'api', action: 'dispatch') { section = "ezb" }
        "/api/$version/oamonitor/$obj"      (controller: 'api', action: 'dispatch') { section = "oamonitor" }
        "/api/$version/oamonitor/$obj/$cmd" (controller: 'api', action: 'dispatch') { section = "oamonitor" }
        "/api/$version/statistic/$obj"      (controller: 'api', action: 'dispatch') { section = "statistic" }
        "/api/$version/statistic/$obj/$cmd" (controller: 'api', action: 'dispatch') { section = "statistic" }
        "/api/$version/$obj"                (controller: 'api', action: 'dispatch')

        // --

        "/lic/$action?/$id?"                (controller: 'license')
        "/org/$action?/$id?"                (controller: 'organisation')
        "/sub/$action?/$id?"                (controller: 'subscription')

        // finance

        "/myInstitution/finance"            (controller: 'finance', action: 'index')

        name subfinance:                "/subscription/$sub/finance/"           (controller: 'finance', action: 'subFinancialData')
        name subfinanceEditCI:          "/subscription/$sub/editCostItem/$id"   (controller: 'finance', action: 'editCostItem')
        name subfinanceCopyCI:          "/subscription/$sub/copyCostItem/$id"   (controller: 'finance', action: 'copyCostItem')

        // survey

        "/surveyconfig/show/$id"            (controller: 'survey', action: 'redirectSurveyConfig')

        // default

        "/$controller/$action?/$id?" ()

        // statusCodes

        "500"       (controller: 'statusCode', action: 'error')
        "401"       (controller: 'statusCode', action: 'forbidden')
        "403"       (controller: 'statusCode', action: 'forbidden')
        "404"       (controller: 'statusCode', action: 'notFound')
        "405"       (controller: 'statusCode', action: 'error')

        //        "/**"       (controller: 'statusCode', action: 'fallback')
    }
}
