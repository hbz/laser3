package de.laser

import de.laser.storage.RDStore
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

/**
 * This is a controller for test functions
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class DevController  {

    ContextService contextService
    LicenseService licenseService

    /**
     * @return the frontend view with sample area for frontend developing and showcase
     */
    @Secured(['ROLE_ADMIN'])
    def frontend() {
        Map<String, Object> result = [user: contextService.getUser(), institution: contextService.getOrg()]
        result
    }

    /**
     * @return the frontend view with sample area for frontend developing and showcase
     */
    @Secured(['ROLE_ADMIN'])
    def klodav() {
        Map<String, Object> result = [user: contextService.getUser(), institution: contextService.getOrg(), view: (params.id ?: 'index')]
        String view = params.id ? 'klodav/' + params.id : 'klodav/index'
        render view: view, model: result
    }

    /**
     * JavaScript call area
     */
    @Secured(['ROLE_ADMIN'])
    def jse() {
        if (params.xhr_full) {
            render template: 'jse_xhr_full'
        }
        else if (params.xhr) {
            render template: 'jse_xhr'
        }
        else {
            render view: 'jse'
        }
    }

    @Secured(['ROLE_ADMIN'])
    def onixValidationPrecheck() {
        Org institution = contextService.getOrg()
        License license = License.get(params.id)
        Map<String, Object> result = [validationErrors: licenseService.precheckValidation(license, institution)]
        if(result.validationErrors == null) {
            redirect controller: 'license', action: 'show', params: [export: 'onix', id: params.id]
        }
        else
            render result as JSON
    }

    @Secured(['ROLE_YODA'])
    def queryOutputChecker() {
        Set<Subscription> result = Subscription.executeQuery("select s from Subscription s join s.packages sp where ((s.endDate is not null and s.endDate >= :now) or s.hasPerpetualAccess = true) and s.holdingSelection = :entire and sp.pkg = :pkg and s.instanceOf = null", [now: new Date(), entire: RDStore.SUBSCRIPTION_HOLDING_ENTIRE, pkg: de.laser.wekb.Package.findByGokbId('a3f41aef-8316-442e-99e9-29e2f011fc22')])
        flash.message = "subs concerned: ${result.collect { Subscription s -> "${s.id} => ${s.name} => ${s.getSubscriberRespConsortia().collect { Org oo -> "${oo.name} (${oo.sortname})" }.join(',')}" }.join('<br>')}"
        redirect controller: 'yoda', action: 'index'
    }
}
