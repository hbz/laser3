class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        "/lic/$action?/$id?"(controller: 'license')

        "/myInstitutions/$shortcode/$action"(controller: 'myInstitutions')
        "/myInstitutions/$shortcode/$action/$id"(controller: 'myInstitutions')
        "/myInstitutions/$shortcode/dashboard"(controller: 'myInstitutions', action: 'instdash')
        "/myInstitutions/$shortcode/finance"(controller: 'finance', action: 'index')
        name subfinance: "/subscriptionDetails/$sub/finance/"(controller: 'finance', action: 'index')
        "/myInstitutions/$shortcode/tipview/$id"(controller: 'myInstitutions', action: 'tip')

        "/ajax/$action?/$id?"(controller: 'ajax')

        // new custom api
        "/api/spec"        (uri: '/rest/v0/laser.yaml')
        "/api/v0/$obj"     (controller: 'api', action: 'v0')

        // "/"(controller:"home")
        "/"(view: "/publichome")

        "/about"(view: "/about")
        "/terms-and-conditions"(view: "/terms-and-conditions")
        "/privacy-policy"(view: "/privacy-policy")
        "/freedom-of-information-policy"(view: "/freedom-of-information-policy")
        "/contact-us"(view: "/contact-us")
        "/publichome"(view: "/publichome")
        "/signup"(view: "/signup")
        "/noHostPlatformUrl"(view: "/noHostPlatformUrl")
        "/oai/$id"(controller: 'oai', action: 'index')

        "500"(view: '/serverCodes/error')
        "401"(view: '/serverCodes/forbidden')
        "403"(view: '/serverCodes/error')
        "404"(view: '/serverCodes/notFound404')

    }
}
