package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent

@Secured(['IS_AUTHENTICATED_FULLY'])
class OnixplLicenseController extends AbstractDebugController {

    def springSecurityService
    def contextService

    @Secured(['ROLE_ADMIN'])
    def index() {
        def user = User.get(springSecurityService.principal.id)
        // def onixplLicense = OnixplLicense.get(params.id)
//        if ( ! onixplLicense.hasPerm("view",user) ) {
//            log.debug("return 401....");
//            response.sendError(401);
//            return
//        }

        def ghost_license = OnixplLicense.findByTitle(grails.util.Holders.config.onix_ghost_license)
        def licenses = ghost_license?[params.id,ghost_license.id] : params.id
        forward (action:'matrix', params:[Compare:"Compare", id:"compare",compareAll:true,selectedLicenses:licenses],controller:"onixplLicenseCompare")
    }

    @Secured(['ROLE_ADMIN'])
    def notes() {
        log.debug("license id:${params.id}");
        def user = User.get(springSecurityService.principal.id)
        def onixplLicense = OnixplLicense.get(params.id)
//        if ( ! onixplLicense.hasPerm("view",user) ) {
//            response.sendError(401);
//            return
//        }
        [onixplLicense: onixplLicense, user: user]
    }

    @Secured(['ROLE_ADMIN'])
    def documents() {
        log.debug("license id:${params.id}");
        def user = User.get(springSecurityService.principal.id)
        def onixplLicense = OnixplLicense.get(params.id)
//        if ( ! onixplLicense.hasPerm("view",user) ) {
//            response.sendError(401);
//            return
//        }
        [onixplLicense: onixplLicense, user: user]
    }

    @Secured(['ROLE_ADMIN'])
    def history() {
        log.debug("license id:${params.id}");
        def user = User.get(springSecurityService.principal.id)
        def onixplLicense = OnixplLicense.get(params.id)

//        if ( ! onixplLicense.hasPerm("view",user) ) {
//            response.sendError(401);
//            return
//        }
        def max = params.max ?: 20;
        def offset = params.offset ?: 0;

        def qry_params = [onixplLicense.class.name, "${onixplLicense.id}"]
        def historyLines = AuditLogEvent.executeQuery("select e from AuditLogEvent as e where className=? and persistedObjectId=? order by id desc", qry_params, [max:max, offset:offset]);
        def historyLinesTotal = AuditLogEvent.executeQuery("select e.id from AuditLogEvent as e where className=? and persistedObjectId=?",qry_params).size()
        [onixplLicense: onixplLicense, user: user, max: max, offset: offset, historyLines: historyLines, historyLinesTotal: historyLinesTotal]
    }

    @Secured(['ROLE_ADMIN'])
    def permissionInfo() {
        def user = User.get(springSecurityService.principal.id)
        def onixplLicense = OnixplLicense.get(params.id)
        [onixplLicense: onixplLicense, user: user]
    }

    @Secured(['ROLE_ADMIN'])
    def list() {
        params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSizeTMP()
        [onixplLicenseInstanceList: OnixplLicense.list(params), onixplLicenseInstanceTotal: OnixplLicense.count()]
    }
}
