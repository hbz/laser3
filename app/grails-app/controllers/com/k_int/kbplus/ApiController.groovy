package com.k_int.kbplus

import com.k_int.kbplus.auth.*
import de.laser.domain.Constants
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll']) // TODO
class ApiController {

    def springSecurityService
    ApiService apiService

    ApiController(){
        super()
    }

    // @Secured(['ROLE_API', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        log.debug("API")

        def result = [:]
        if (springSecurityService.isLoggedIn()) {
            def user = User.get(springSecurityService.principal.id)
            result.apiKey = user?.apikey
            result.apiSecret = user?.apisecret
        }

        switch ( (params.version ?: 'v0').toLowerCase() ) {
            case '1':
            case 'v1':
                render view: 'v1', model: result
                break
            default:
                render view: 'v0', model: result
                break
        }
    }

    // @Secured(['ROLE_API', 'IS_AUTHENTICATED_FULLY'])
    def loadSpec() {
        switch ( (params.version ?: 'v0').toLowerCase() ) {
            case '1':
            case 'v1':
                render view: '/swagger/v1/laser.yaml.gsp'
                break
            default:
                render view: '/swagger/v0/laser.yaml.gsp'
                break
        }
    }

    // @Secured(['ROLE_API', 'IS_AUTHENTICATED_FULLY'])
    def dispatch() {
        switch ( (params.version ?: 'v0').toLowerCase() ) {
            case '1':
            case 'v1':
                v1()
                break
            default:
                v0()
                break
        }
    }

    @Secured(['ROLE_API', 'IS_AUTHENTICATED_FULLY'])
    def uploadBibJson() {
        def result = [:]
        log.debug("uploadBibJson");
        log.debug("Auth request from ${request.getRemoteAddr()}");
        if (request.getRemoteAddr() == '127.0.0.1') {
            if (request.method.equalsIgnoreCase("post")) {
                result.message = "Working...";
                def candidate_identifiers = []
                request.JSON.identifier.each { i ->
                    if (i.type == 'ISSN' || i.type == 'eISSN' || i.type == 'DOI') {
                        candidate_identifiers.add([namespace: i.type, value: i.id]);
                    }
                }
                if (candidate_identifiers.size() > 0) {
                    log.debug("Lookup using ${candidate_identifiers}");
                    def title = TitleInstance.findByIdentifier(candidate_identifiers)
                    if (title != null) {
                        log.debug("Located title ${title}  Current identifiers: ${title.ids}");
                        result.matchedTitleId = title.id
                        if (title.getIdentifierValue('jusp') != null) {
                            result.message = "jusp ID already present against title";
                        } else {
                            log.debug("Adding jusp Identifier to title");
                            def jid = request.JSON.identifier.find { it.type == 'jusp' }
                            log.debug("Add identifier identifier ${jid}");
                            if (jid != null) {
                                result.message = "Adding jusp ID ${jid.id}to title";
                                def new_jusp_id = Identifier.lookupOrCreateCanonicalIdentifier('jusp', "${jid.id}");
                                def new_io = new IdentifierOccurrence(identifier: new_jusp_id, ti: title).save(flush: true);
                            } else {
                                result.message = "Unable to locate JID in BibJson record";
                            }
                        }
                    } else {
                        result.message = "Unable to locate title on matchpoints : ${candidate_identifiers}";
                    }
                } else {
                    result.message = "No matchable identifiers. ${request.JSON.identifier}";
                }

            } else {
                result.message = "non post";
            }
        } else {
            result.message = "uploadBibJson only callable from 127.0.0.1";
        }
        render result as JSON
    }

    // Assert a core status against a title/institution. Creates TitleInstitutionProvider objects
    // For all known combinations.
    @Secured(['ROLE_API', 'IS_AUTHENTICATED_FULLY'])
    def assertCore() {
        // Params:     inst - [namespace:]code  Of an org [mandatory]
        //            title - [namespace:]code  Of a title [mandatory]
        //         provider - [namespace:]code  Of an org [optional]
        log.debug("assertCore(${params})");
        def result = [:]
        if (request.getRemoteAddr() == '127.0.0.1') {
            if ((params.inst?.length() > 0) && (params.title?.length() > 0)) {
                def inst = Org.lookupByIdentifierString(params.inst);
                def title = TitleInstance.lookupByIdentifierString(params.title);
                def provider = params.provider ? Org.lookupByIdentifierString(params.provider) : null;
                def year = params.year?.trim()

                log.debug("assertCore ${params.inst}:${inst} ${params.title}:${title} ${params.provider}:${provider}");

                if (title && inst) {

                    def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                    if (provider) {
                    } else {
                        log.debug("Calculating all known providers for this title");
                        def providers = TitleInstancePackagePlatform.executeQuery('''select distinct orl.org 
from TitleInstancePackagePlatform as tipp join tipp.pkg.orgs as orl
where tipp.title = ? and orl.roleType.value=?''', [title, 'Content Provider']);

                        providers.each {
                            log.debug("Title ${title} is provided by ${it}");
                            def tiinp = TitleInstitutionProvider.findByTitleAndInstitutionAndprovider(title, inst, it)
                            if (tiinp == null) {
                                log.debug("Creating new TitleInstitutionProvider");
                                tiinp = new TitleInstitutionProvider(title: title, institution: inst, provider: it).save(flush: true, failOnError: true)
                            }

                            log.debug("Got tiinp:: ${tiinp}");
                            def startDate = sdf.parse("${year}-01-01T00:00:00");
                            def endDate = sdf.parse("${year}-12-31T23:59:59");
                            tiinp.extendCoreExtent(startDate, endDate);
                        }
                    }
                }
            } else {
                result.message = "ERROR: missing mandatory parameter: inst or title";
            }
        } else {
            result.message = "ERROR: this call is only usable from within the KB+ system network"
        }
        render result as JSON
    }
    /*
    * Create a CSV containing all JUSP title IDs with the institution they belong to
    */

    @Secured(['ROLE_API', 'IS_AUTHENTICATED_FULLY'])
    def fetchAllTips() {

        def jusp_ti_inst = TitleInstitutionProvider.executeQuery("""
   select jusp_institution_id.identifier.value, jusp_title_id.identifier.value, dates,tip_ti.id, 
   (select jusp_provider_id.identifier.value from tip_ti.provider.ids as jusp_provider_id where jusp_provider_id.identifier.ns.ns='juspsid' )
    from TitleInstitutionProvider tip_ti
      join tip_ti.institution.ids as jusp_institution_id,
    TitleInstitutionProvider tip_inst
      join tip_inst.title.ids as jusp_title_id,
    TitleInstitutionProvider tip_date
      join tip_date.coreDates as dates
    where jusp_title_id.identifier.ns.ns='jusp'
        and tip_ti = tip_inst
        and tip_inst = tip_date
        and jusp_institution_id.identifier.ns.ns='jusplogin' order by jusp_institution_id.identifier.value 
     """)

        def date = new java.text.SimpleDateFormat(session.sessionPreferences?.globalDateFormat)
        date = date.format(new Date())
        response.setHeader("Content-disposition", "attachment; filename=\"kbplus_jusp_export_${date}.csv\"")
        response.contentType = "text/csv"
        def out = response.outputStream
        def currentTip = null
        def dates_concat = ""
        out.withWriter { writer ->
            writer.write("JUSP Institution ID,JUSP Title ID,JUSP Provider, Core Dates\n")
            Iterator iter = jusp_ti_inst.iterator()
            while (iter.hasNext()) {
                def it = iter.next()
                if (currentTip == it[3]) {
                    dates_concat += ", ${it[2]}"
                } else if (currentTip) {
                    writer.write("\"${dates_concat}\"\n\"${it[0]}\",\"${it[1]}\",\"${it[4] ?: ''}\",")
                    dates_concat = "${it[2]}"
                    currentTip = it[3]
                } else {
                    writer.write("\"${it[0]}\",\"${it[1]}\",\"${it[4] ?: ''}\",")
                    dates_concat = "${it[2]}"
                    currentTip = it[3]
                }
                if (!iter.hasNext()) {
                    writer.write("\"${dates_concat}\"\n")
                }
            }

            writer.flush()
            writer.close()
        }
        out.close()
    }

    // Accept a single mandatorty parameter which is the namespace:code for an institution
    // If found, return a JSON report of each title for that institution
    // Also accept an optional parameter esn [element set name] with values full of brief[the default]
    // Example:  http://localhost:8080/laser/api/institutionTitles?orgid=jusplogin:shu
    @Secured(['ROLE_API', 'IS_AUTHENTICATED_FULLY'])
    def institutionTitles() {

        def result = [:]
        result.titles = []

        if (params.orgid) {
            def name_components = params.orgid.split(':')
            if (name_components.length == 2) {
                // Lookup org by ID
                def orghql = "select org from Org org where exists ( select io from IdentifierOccurrence io, Identifier id, IdentifierNamespace ns where io.org = org and id.ns = ns and io.identifier = id and ns.ns = ? and id.value like ? )"
                def orgs = Org.executeQuery(orghql, [name_components[0], name_components[1]])
                if (orgs.size() == 1) {
                    def org = orgs[0]

                    def today = new Date()

                    // Find all TitleInstitutionProvider where institution = org
                    def titles = TitleInstitutionProvider.executeQuery('select tip.title.title, tip.title.id, count(cd) from TitleInstitutionProvider as tip left join tip.coreDates as cd where tip.institution = ? and cd.startDate < ? and cd.endDate > ?',
                            [org, today, today]);
                    titles.each { tip ->
                        result.titles.add([title: tip[0], tid: tip[1], isCore: tip[2]]);
                    }
                } else {
                    log.message = "Unable to locate Org with ID ${params.orgid}";
                }
            } else {
                result.message = "Invalid orgid. Format orgid as namespace:value, for example jusplogin:shu"
            }
        }

        render result as JSON
    }

    @Secured(['ROLE_API_WRITER', 'IS_AUTHENTICATED_FULLY'])
    def importInstitutions() {
        log.info("import institutions via xml .. ROLE_API_WRITER required")

        def xml = "(Code: 0) - Errare humanum est"
        def rawText = request.getReader().getText()

        if (request.method == 'POST') {

            if(rawText) {
                xml = new XmlSlurper().parseText(rawText)
                assert xml instanceof groovy.util.slurpersupport.GPathResult
                apiService.makeshiftOrgImport(xml)
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
        log.debug("API Call: " + params)

        def result
        def hasAccess = false
        def apiManager = de.laser.api.v0.ApiMainClass

        def obj     = params.get('obj')
        def query   = params.get('q')
        def value   = params.get('v', '')
        def context = params.get('context')
        def format


        Org contextOrg = null
        User user = (User) request.getAttribute('authorizedApiUser')

        if (user) {
            // checking role permission
            def dmRole = UserRole.findAllWhere(user: user, role: Role.findByAuthority('ROLE_API_DATAMANAGER'))

            if ("GET" == request.method) {
                def readRole = UserRole.findAllWhere(user: user, role: Role.findByAuthority('ROLE_API_READER'))
                hasAccess = ! (dmRole.isEmpty() && readRole.isEmpty())
            }
            else if ("POST" == request.method) {
                def writeRole = UserRole.findAllWhere(user: user, role: Role.findByAuthority('ROLE_API_WRITER'))
                hasAccess = ! (dmRole.isEmpty() && writeRole.isEmpty())
            }

            // getting context

            if (context) {
                user.authorizedAffiliations.each { uo -> //  com.k_int.kbplus.auth.UserOrg
                    def org = Org.findWhere(id: uo.org.id, shortcode: params.get('context'))
                    if (org) {
                        contextOrg = org
                    }
                }
            }
            else if (user.authorizedAffiliations.size() == 1) {
                def uo = user.authorizedAffiliations[0]
                contextOrg = Org.findWhere(id: uo.org.id)
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
                if (!query || !value) {
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
                        default:
                            format = Constants.MIME_ALL
                            break
                    }

                    result = apiManager.read((String) obj, (String) query, (String) value, (User) user, (Org) contextOrg, format)

                    if (result instanceof Doc) {
                        if (result.contentType == Doc.CONTENT_TYPE_STRING) {
                            response.contentType = result.mimeType
                            response.setHeader('Content-Disposition', 'attachment; filename="' + result.title + '"')
                            response.outputStream << result.content
                            response.outputStream.flush()
                            return
                        }
                        else if (result.contentType == Doc.CONTENT_TYPE_BLOB) {
                            response.contentType = result.mimeType
                            response.setHeader('Content-Disposition', 'attachment; filename="' + result.title + '-' + result.filename + '"')
                            response.setHeader('Content-Length', "${result.getBlobSize()}")
                            response.outputStream << result.getBlobData()
                            response.outputStream.flush()
                            return
                        }
                    }
                }
            }
            else if ('POST' == request.method) {
                def postBody = request.getAttribute("authorizedApiUsersPostBody")
                def data = (postBody ? new JSON().parse(postBody) : null)

                if (! data) {
                    result = Constants.HTTP_BAD_REQUEST
                }
                else {
                    result = apiManager.write((String) obj, data, (User) user, (Org) contextOrg)
                }
            }
            else {
                result = Constants.HTTP_NOT_IMPLEMENTED
            }
        }
        def responseStruct = apiManager.buildResponse(request, obj, query, value, context, contextOrg, result)

        def responseJson = responseStruct[0]
        def responseCode = responseStruct[1]

        response.setContentType(Constants.MIME_APPLICATION_JSON)
        response.setCharacterEncoding(Constants.UTF8)
        response.setHeader("Debug-Result-Length", responseJson.toString().length().toString())
        response.setStatus(responseCode)

        render responseJson.toString(true)
    }

    /**
     * API endpoint
     *
     * @return
     */
    @Secured(['permitAll']) // TODO
    def v1() {
        log.debug("API Call: " + params)

        def result
        def hasAccess = false
        def apiManager = de.laser.api.v1.ApiMainClass

        def obj     = params.get('obj')
        def query   = params.get('q')
        def value   = params.get('v', '')
        def context = params.get('context')
        def format


        Org contextOrg = null
        User user = (User) request.getAttribute('authorizedApiUser')

        if (user) {
            // checking role permission
            def dmRole = UserRole.findAllWhere(user: user, role: Role.findByAuthority('ROLE_API_DATAMANAGER'))

            if ("GET" == request.method) {
                def readRole = UserRole.findAllWhere(user: user, role: Role.findByAuthority('ROLE_API_READER'))
                hasAccess = ! (dmRole.isEmpty() && readRole.isEmpty())
            }
            else if ("POST" == request.method) {
                def writeRole = UserRole.findAllWhere(user: user, role: Role.findByAuthority('ROLE_API_WRITER'))
                hasAccess = ! (dmRole.isEmpty() && writeRole.isEmpty())
            }

            // getting context

            if (context) {
                user.authorizedAffiliations.each { uo -> //  com.k_int.kbplus.auth.UserOrg
                    def org = Org.findWhere(id: uo.org.id, shortcode: params.get('context'))
                    if (org) {
                        contextOrg = org
                    }
                }
            }
            else if (user.authorizedAffiliations.size() == 1) {
                def uo = user.authorizedAffiliations[0]
                contextOrg = Org.findWhere(id: uo.org.id)
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
                if (!query || !value) {
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
                        default:
                            format = Constants.MIME_ALL
                            break
                    }

                    result = apiManager.read((String) obj, (String) query, (String) value, (User) user, (Org) contextOrg, format)

                    if (result instanceof Doc) {
                        if (result.contentType == Doc.CONTENT_TYPE_STRING) {
                            response.contentType = result.mimeType
                            response.setHeader('Content-Disposition', 'attachment; filename="' + result.title + '"')
                            response.outputStream << result.content
                            response.outputStream.flush()
                            return
                        }
                        else if (result.contentType == Doc.CONTENT_TYPE_BLOB) {
                            response.contentType = result.mimeType
                            response.setHeader('Content-Disposition', 'attachment; filename="' + result.title + '-' + result.filename + '"')
                            response.setHeader('Content-Length', "${result.getBlobSize()}")
                            response.outputStream << result.getBlobData()
                            response.outputStream.flush()
                            return
                        }
                    }
                }
            }
            else if ('POST' == request.method) {
                def postBody = request.getAttribute("authorizedApiUsersPostBody")
                def data = (postBody ? new JSON().parse(postBody) : null)

                if (! data) {
                    result = Constants.HTTP_BAD_REQUEST
                }
                else {
                    result = apiManager.write((String) obj, data, (User) user, (Org) contextOrg)
                }
            }
            else {
                result = Constants.HTTP_NOT_IMPLEMENTED
            }
        }
        def responseStruct = apiManager.buildResponse(request, obj, query, value, context, contextOrg, result)

        def responseJson = responseStruct[0]
        def responseCode = responseStruct[1]

        response.setContentType(Constants.MIME_APPLICATION_JSON)
        response.setCharacterEncoding(Constants.UTF8)
        response.setHeader("Debug-Result-Length", responseJson.toString().length().toString())
        response.setStatus(responseCode)

        render responseJson.toString(true)
    }
}