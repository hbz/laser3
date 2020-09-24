package de.laser


import com.k_int.kbplus.Org
import de.laser.api.v0.ApiManager
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiToolkit
import de.laser.helper.Constants
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.slurpersupport.GPathResult
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

@Secured(['permitAll']) // TODO
class ApiController {

    def springSecurityService
    ContextService contextService
    ApiService apiService

    ApiController(){
        super()
    }

    // @Secured(['ROLE_API', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        log.debug("API")
        Map<String, Object> result = fillRequestMap(params)

        switch ( (params.version ?: 'v0').toLowerCase() ) {
            default:
                result.apiVersion = 'v0'
                render view: 'v0', model: result
                break
        }
    }

    def loadSpecs() {
        Map<String, Object> result = fillRequestMap(params)

        switch ( (params.version ?: 'v0').toLowerCase() ) {
            default:
                result.apiVersion = 'v0'
                render view: '/swagger/v0/laser.yaml.gsp', model: result
                break
        }
    }

    def loadChangelog() {
        Map<String, Object> result = fillRequestMap(params)

        switch ( (params.version ?: 'v0').toLowerCase() ) {
            default:
                result.apiVersion = 'v0'
                render view: '/swagger/v0/changelog.md.gsp', model: result
                break
        }
    }

    def dispatch() {
        switch ( (params.version ?: 'v0').toLowerCase() ) {
            default:
                v0()
                break
        }
    }

    private Map<String, Object> fillRequestMap (GrailsParameterMap params) {
        Map<String, Object> result = [:]
        Org org

        if (springSecurityService.isLoggedIn()) {
            org = contextService.getOrg()
        }

        def apiKey = OrgSetting.get(org, OrgSetting.KEYS.API_KEY)
        def apiPass = OrgSetting.get(org, OrgSetting.KEYS.API_PASSWORD)

        result.apiKey       = (apiKey != OrgSetting.SETTING_NOT_FOUND) ? apiKey.getValue() : ''
        result.apiPassword  = (apiPass != OrgSetting.SETTING_NOT_FOUND) ? apiPass.getValue() : ''
        result.apiContext   = org?.globalUID ?: ''

        result
    }

    @Secured(['ROLE_API', 'IS_AUTHENTICATED_FULLY'])
    def importInstitutions() {
        log.info("import institutions via xml .. ROLE_API required")

        def xml = "(Code: 0) - Errare humanum est"
        def rawText = request.getReader().getText()

        if (request.method == 'POST') {

            if(rawText) {
                xml = new XmlSlurper().parseText(rawText)
                assert xml instanceof GPathResult
                apiService.makeshiftOrgImport(xml)
            }
            else {
                xml = "(Code: 1) - Ex nihilo nihil fit"
            }
        }
        render xml
    }

    @Secured(['ROLE_API', 'IS_AUTHENTICATED_FULLY'])
    def setupLaserData() {
        log.info("import institutions via xml .. ROLE_API required")

        def xml = "(Code: 0) - Errare humanum est"
        def rawText = request.getReader().getText()

        if (request.method == 'POST') {

            if(rawText) {
                xml = new XmlSlurper().parseText(rawText)
                assert xml instanceof GPathResult
                apiService.setupLaserData(xml)
            }
            else {
                xml = "(Code: 1) - Ex nihilo nihil fit"
            }
        }
        render xml
    }

    @Secured(['ROLE_API', 'IS_AUTHENTICATED_FULLY'])
    def importSubscriptions() {
        log.info("import subscriptions via xml .. ROLE_API required")
        // TODO: in progress - erms-746
        def xml = "(Code: 0) - Errare humanum est"
        def rawText = request.getReader().getText()

        if (request.method == 'POST') {

            if(rawText) {
                xml = new XmlSlurper().parseText(rawText)
                assert xml instanceof GPathResult
                apiService.makeshiftSubscriptionImport(xml)
            }
            else {
                xml = "(Code: 1) - Ex nihilo nihil fit"
            }
        }
        render xml
    }

    /**
     * API endpoint
     *
     * @return
     */
    @Secured(['permitAll']) // TODO
    def v0() {
        Org apiOrg = (Org) request.getAttribute('authorizedApiOrg')
        boolean debugMode = request.getAttribute('debugMode')

        log.debug("API Call [${apiOrg?.id}] - " + params)

        def result
        boolean hasAccess = false
        long startTimeMillis = System.currentTimeMillis()

        String obj     = params.get('obj')
        String query   = params.get('q', '')
        String value   = params.get('v', '')
        String context = params.get('context')
        String format

        String section = params.get('section')
        if (section) {
            obj = section + '/' + obj
        }
        String cmd = params.get('cmd')
        if (cmd) {
            obj = obj + '/' + cmd
        }

        Org contextOrg = null // TODO refactoring

        if (apiOrg) {
            // checking role permission
            def apiLevel = OrgSetting.get(apiOrg, OrgSetting.KEYS.API_LEVEL)

            if (apiLevel != OrgSetting.SETTING_NOT_FOUND) {
                if ("GET" == request.method) {
                    hasAccess = (apiLevel.getValue() in ApiToolkit.getReadingApiLevels())
                }
                else if ("POST" == request.method) {
                    hasAccess = (apiLevel.getValue() in ApiToolkit.getWritingApiLevels())
                }
            }

            // getting context (fallback)
            if (params.get('context')) {
                contextOrg = Org.findWhere(globalUID: params.get('context'))
            }
            else {
                contextOrg = apiOrg
            }
        }

        if (!contextOrg || !hasAccess) {
            result = Constants.HTTP_FORBIDDEN
        }
        else if (!obj) {
            result = Constants.HTTP_BAD_REQUEST
        }

        // delegate api calls

        if (! result) {
            if ('GET' == request.method) {
                if (! (query && value) && ! ApiReader.SIMPLE_QUERIES.contains(obj)) {
                    result = Constants.HTTP_BAD_REQUEST
                }
                else {
                    switch(request.getHeader('accept')) {
                        case Constants.MIME_APPLICATION_JSON:
                        case Constants.MIME_TEXT_JSON:
                            format = Constants.MIME_APPLICATION_JSON
                            break
                        case Constants.MIME_APPLICATION_XML:
                        case Constants.MIME_TEXT_XML:
                            format = Constants.MIME_APPLICATION_XML
                            break
                        case Constants.MIME_TEXT_PLAIN:
                            format = Constants.MIME_TEXT_PLAIN
                            break
                        case Constants.MIME_ALL:
                            format = Constants.MIME_ALL
                            break
                        default:
                            format = 'you_shall_not_pass'
                            break
                    }

                    result = ApiManager.read(
                            (String) obj,
                            (String) query,
                            (String) value,
                            (Org) contextOrg,
                            format
                    )

                    if (result instanceof Doc) {
                        response.contentType = result.mimeType

                        if (result.contentType == Doc.CONTENT_TYPE_STRING) {
                            response.setHeader('Content-Disposition', 'attachment; filename="' + result.title + '"')
                            response.outputStream << result.content
                        }
                        else if (result.contentType == Doc.CONTENT_TYPE_FILE) {
                            result.render(response, result.filename)
                        }
                        response.outputStream.flush()
                        return
                    }
                }
            }
            /*
            else if ('POST' == request.method) {
                def postBody = request.getAttribute("authorizedApiPostBody")
                def data = (postBody ? new JSON().parse(postBody) : null)

                if (! data) {
                    result = Constants.HTTP_BAD_REQUEST
                }
                else {
                    result = ApiManager.write((String) obj, data, (User) user, (Org) contextOrg)
                }
            }
            */
            else {
                result = Constants.HTTP_NOT_IMPLEMENTED
            }
        }
        Map<String, Object> respStruct = ApiManager.buildResponse(request, obj, query, value, context, contextOrg, result)

        JSON json   = (JSON) respStruct.json
        int status  = (int) respStruct.status

        String responseTime = ((System.currentTimeMillis() - startTimeMillis) / 1000).toString()

        response.setContentType(Constants.MIME_APPLICATION_JSON)
        response.setCharacterEncoding(Constants.UTF8)
        response.setHeader("Laser-Api-Version", ApiManager.VERSION.toString())
        response.setStatus(status)

        if (debugMode) {
            response.setHeader("Laser-Api-Debug-Mode", "true")
            response.setHeader("Laser-Api-Debug-Result-Length", json.toString().length().toString())
            response.setHeader("Laser-Api-Debug-Result-Time", responseTime)

            if (json.target instanceof List) {
                response.setHeader("Laser-Api-Debug-Result-Size", json.target.size().toString())
            }
        }

        if (json.target instanceof List) {
            log.debug("API Call [${apiOrg?.id}] - (Code: ${status}, Time: ${responseTime}, Items: ${json.target.size().toString()})")
        }
        else {
            log.debug("API Call [${apiOrg?.id}] - (Code: ${status}, Time: ${responseTime}, Length: ${json.toString().length().toString()})")
        }

        render json.toString(true)
    }
}