package de.laser


import de.laser.api.v0.ApiManager
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiToolkit
import de.laser.storage.Constants
import de.laser.helper.DateUtils
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.http.HttpStatus

/**
 * This controller manages calls to the API
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class ApiController {

    ContextService contextService
    ExportService exportService

    /**
     * Call to the API landing page
     */
    @Secured(['permitAll'])
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

    /**
     * Call to the API specification file
     */
    @Secured(['permitAll'])
    def loadSpecs() {
        Map<String, Object> result = fillRequestMap(params)

        switch ( (params.version ?: 'v0').toLowerCase() ) {
            default:
                result.apiVersion = 'v0'
                render view: '/swagger/v0/laser.yaml.gsp', model: result
                break
        }
    }

    /**
     * Call to the API changelog file
     */
    @Secured(['permitAll'])
    def loadChangelog() {
        Map<String, Object> result = fillRequestMap(params)

        switch ( (params.version ?: 'v0').toLowerCase() ) {
            default:
                result.apiVersion = 'v0'
                render view: '/swagger/v0/changelog.md.gsp', model: result
                break
        }
    }

    /**
     * Delivers the current version
     */
    @Secured(['permitAll'])
    def dispatch() {
        switch ( (params.version ?: 'v0').toLowerCase() ) {
            default:
                v0()
                break
        }
    }

    /**
     * Fills the request map with the credentials passed by context. The mandatory presence of a context institution ensures
     * that credentials are supplied iff the user is logged in when calling the API by using this method. A context institution requires
     * a login to be present
     * @param params (unused) the request parameter
     * @return a {@link Map} containing api key, password and context if a context is supplied; when context is missing, the map values are empty
     */
    private Map<String, Object> fillRequestMap (GrailsParameterMap params) {
        Map<String, Object> result = [:]
        Org org = contextService.getOrg()

        def apiKey = OrgSetting.get(org, OrgSetting.KEYS.API_KEY)
        def apiPass = OrgSetting.get(org, OrgSetting.KEYS.API_PASSWORD)

        result.apiKey       = (apiKey != OrgSetting.SETTING_NOT_FOUND) ? apiKey.getValue() : ''
        result.apiPassword  = (apiPass != OrgSetting.SETTING_NOT_FOUND) ? apiPass.getValue() : ''
        result.apiContext   = org?.globalUID ?: ''

        result
    }

    /**
     * This is the main entry point for API calls. The request is being delegated to the service methods handling the request
     * and authorisation is being done here as well. The response type is being determined based on the request header and the
     * response delivered accordingly. The request is being logged in the server in order to enable bug retracing and usage of
     * the API
     */
    @Secured(['permitAll'])
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
        String changedFrom = params.get('changedFrom', '')
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
                        case [ Constants.MIME_APPLICATION_JSON, Constants.MIME_TEXT_JSON ]:
                            format = Constants.MIME_APPLICATION_JSON
                            break
                        case [ Constants.MIME_APPLICATION_XML, Constants.MIME_TEXT_XML ]:
                            format = Constants.MIME_APPLICATION_XML
                            break
                        case Constants.MIME_TEXT_PLAIN:
                            format = Constants.MIME_TEXT_PLAIN
                            break
                        case Constants.MIME_TEXT_TSV:
                            format = Constants.MIME_TEXT_TSV
                            break
                        case Constants.MIME_ALL:
                            format = Constants.MIME_ALL
                            break
                        default:
                            format = 'you_shall_not_pass'
                            break
                    }
                    Date changedDate = changedFrom ? DateUtils.getFixedSDF_yymd().parse(changedFrom) : null
                    result = ApiManager.read(
                            (String) obj,
                            (String) query,
                            (String) value,
                            (Org) contextOrg,
                            format,
                            changedDate
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
        if(format == Constants.MIME_TEXT_TSV) {
            String respStruct
            int status = HttpStatus.BAD_REQUEST.value()
            if(result instanceof Map) {
                respStruct = exportService.generateSeparatorTableString(result.titleRow, result.columnData, '\t')
                status = HttpStatus.OK.value()
            }
            else {
                respStruct = result
                switch(result) {
                    case Constants.HTTP_FORBIDDEN: HttpStatus.FORBIDDEN.value()
                        break
                    case Constants.HTTP_INTERNAL_SERVER_ERROR: HttpStatus.INTERNAL_SERVER_ERROR.value()
                        break
                }
            }
            String responseTime = ((System.currentTimeMillis() - startTimeMillis) / 1000).toString()
            response.setContentType(Constants.MIME_TEXT_TSV)
            response.setCharacterEncoding(Constants.UTF8)
            response.setHeader("Laser-Api-Version", ApiManager.VERSION.toString())
            response.setStatus(status)
            render respStruct
            /*
            if (debugMode) {
                response.setHeader("Laser-Api-Debug-Mode", "true")
                response.setHeader("Laser-Api-Debug-Result-Length", json.toString().length().toString())
                response.setHeader("Laser-Api-Debug-Result-Time", responseTime)

                if (json.target instanceof List) {
                    response.setHeader("Laser-Api-Debug-Result-Size", json.target.size().toString())
                }
            }*/
        }
        else {
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
}