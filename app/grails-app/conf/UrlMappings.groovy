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
        "/api/v0/$obj"     (controller: 'api', action: 'v0')
        "/api/spec"        (uri: '/rest/v0/laser.yaml')
        
        "/"                     (view: "public/index")

        "/about"                (view: "public/about")
        "/contact-us"           (view: "public/contact-us")
        "/freedom-of-information-policy"(view: "public/freedom-of-information-policy")
        "/noHostPlatformUrl"    (view: "public/noHostPlatformUrl")
        "/privacy-policy"       (view: "public/privacy-policy")
        "/public"               (view: "public/index")
        "/signup"               (view: "public/signup")
        "/terms-and-conditions" (view: "public/terms-and-conditions")

        "/oai/$id"(controller: 'oai', action: 'index')

        "500"(view: '/serverCodes/error')
        "401"(view: '/serverCodes/forbidden')
        "403"(view: '/serverCodes/error')
        "404"(view: '/serverCodes/notFound404')

    }
}
