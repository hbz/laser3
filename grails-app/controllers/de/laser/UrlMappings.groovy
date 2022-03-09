class UrlMappings {

    static mappings = {

        "/"         (controller: 'public', action: 'index')
        "/gasco"    (controller: 'public', action: 'gasco')
        "/ebooks"   (controller: 'ebookCatalogue', action: 'index')

        // ajax

        "/ajax/json/$action?/$id?"      (controller: 'ajaxJson')
        "/ajax/html/$action?/$id?"      (controller: 'ajaxHtml')
        "/ajax/$action?/$id?"           (controller: 'ajax')

        // api

        "/api/push/orgs"                    (controller: 'api', action: 'importInstitutions', parseRequest: false)

        "/api/$version/specs.yaml"          (controller: 'api', action: 'loadSpecs')
        "/api/$version/changelog.md"        (controller: 'api', action: 'loadChangelog')
        "/api/$version/ezb/$obj"            (controller: 'api', action: 'dispatch') { section = "ezb" }
        "/api/$version/oamonitor/$obj"      (controller: 'api', action: 'dispatch') { section = "oamonitor" }
        "/api/$version/oamonitor/$obj/$cmd" (controller: 'api', action: 'dispatch') { section = "oamonitor" }
        "/api/$version/statistic/$obj"      (controller: 'api', action: 'dispatch') { section = "statistic" }
        "/api/$version/statistic/$obj/$cmd" (controller: 'api', action: 'dispatch') { section = "statistic" }
        "/api/$version/$obj"                (controller: 'api', action: 'dispatch')

        "/lic/$action?/$id?"        (controller: 'license')
        "/org/$action?/$id?"        (controller: 'organisation')

        "/surveyconfig/show/$id"        (controller: 'survey', action: 'redirectSurveyConfig')

        //"/myInstitution/tipview/$id"    (controller: 'myInstitution', action: 'tip')
        "/myInstitution/finance"        (controller: 'finance', action: 'index')
        name subfinance:                "/subscription/$sub/finance/"   (controller: 'finance', action: 'subFinancialData')
        name subfinanceEditCI:          "/subscription/$sub/editCostItem/$id"   (controller: 'finance', action: 'editCostItem')
        name subfinanceCopyCI:          "/subscription/$sub/copyCostItem/$id"   (controller: 'finance', action: 'copyCostItem')

        // serverCodes

        "500"       (controller: 'serverCodes', action: 'error')
        "401"       (controller: 'serverCodes', action: 'forbidden')
        "403"       (controller: 'serverCodes', action: 'error')
        "404"       (controller: 'serverCodes', action: 'notFound')
        "405"       (controller: 'serverCodes', action: 'error')

        // default

        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }
    }
}
