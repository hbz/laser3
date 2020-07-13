class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        "/ajax/$action?/$id?"       (controller: 'ajax')

        "/lic/$action?/$id?"        (controller: 'license')
        "/org/$action?/$id?"        (controller: 'organisation')

        //"/myInstitution/tipview/$id"    (controller: 'myInstitution', action: 'tip')
        "/myInstitution/finance"        (controller: 'finance', action: 'index')
        name subfinance:                "/subscription/$sub/finance/"   (controller: 'finance', action: 'subFinancialData')
        name subfinanceEditCI:          "/subscription/$sub/editCostItem/$id"   (controller: 'finance', action: 'editCostItem')
        name subfinanceCopyCI:          "/subscription/$sub/copyCostItem/$id"   (controller: 'finance', action: 'copyCostItem')

        // api
        "/api/push/orgs"                    (controller: 'api', action: 'importInstitutions', parseRequest: false)

        "/api/$version/specs.yaml"          (controller: 'api', action: 'loadSpecs')
        "/api/$version/changelog.md"        (controller: 'api', action: 'loadChangelog')
        "/api/$version/oamonitor/$obj"      (controller: 'api', action: 'dispatch') { section = "oamonitor" }
        "/api/$version/oamonitor/$obj/$cmd" (controller: 'api', action: 'dispatch') { section = "oamonitor" }
        "/api/$version/statistic/$obj"      (controller: 'api', action: 'dispatch') { section = "statistic" }
        "/api/$version/statistic/$obj/$cmd" (controller: 'api', action: 'dispatch') { section = "statistic" }
        "/api/$version/$obj"                (controller: 'api', action: 'dispatch')

        "/"         (controller: 'public', action: 'index')
        "/gasco"    (controller: 'public', action: 'gasco')

        "500"       (view: '/serverCodes/error')
        "401"       (view: '/serverCodes/forbidden')
        "403"       (view: '/serverCodes/error')
        "404"       (view: '/serverCodes/notFound404')

    }
}
