package de.laser

import com.k_int.kbplus.GenericOIDService
import de.laser.annotations.DebugAnnotation
import de.laser.helper.RDStore
import de.laser.helper.SwissKnife
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages calls when entities should be compared against each other
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class CompareController  {

    ContextService contextService
    CompareService compareService
    GenericOIDService genericOIDService

    /**
     * Compares licenses against each other
     */
    @DebugAnnotation(perm = "ORG_INST,ORG_CONSORTIUM", affil = "INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def compareLicenses() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.contextOrg = contextService.getOrg()
        result.institution = result.contextOrg
        params.status = params.status ?: [RDStore.LICENSE_CURRENT.id.toString()]

        result.objects = []

        params.tab = params.tab ?: "compareElements"

        if (params.selectedObjects) {
            result.objects = License.findAllByIdInList(params.list('selectedObjects').collect { Long.parseLong(it) })
        }

        if (params.tab == "compareProperties") {
            result = result + compareService.compareProperties(result.objects)
        }

        result
    }

    /**
     * Compares subscriptions against each other
     */
    @DebugAnnotation(perm = "ORG_INST,ORG_CONSORTIUM", affil = "INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def compareSubscriptions() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.contextOrg = contextService.getOrg()
        SwissKnife.setPaginationParams(result, params, result.user)
        result.institution = result.contextOrg
        params.status = params.status ?: [RDStore.SUBSCRIPTION_CURRENT.id.toString()]


        result.objects = []

        params.tab = params.tab ?: "compareElements"

        if (params.selectedObjects) {
            result.objects = Subscription.findAllByIdInList(params.list('selectedObjects').collect { Long.parseLong(it) })
        }

        if (params.tab == "compareProperties") {
            result = result + compareService.compareProperties(result.objects)
        }

        if (params.tab == "compareEntitlements") {
            result = result + compareService.compareEntitlements(params, result)

            result.ies = result.ies.sort { genericOIDService.resolveOID(it.key).name }
        }

        result
    }

    /**
     * As the list of titles per subscription may get very long, this is an AJAX-called method to load the next batch of entitlements
     * @return the issue entitlement table, starting with the entities from the given offset
     */
    @DebugAnnotation(perm = "ORG_INST,ORG_CONSORTIUM", affil = "INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def loadNextBatch() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.contextOrg = contextService.getOrg()
        SwissKnife.setPaginationParams(result, params, result.user)
        result.institution = result.contextOrg
        result.objects = Subscription.findAllByIdInList(params.list('selectedObjects[]').collect { Long.parseLong(it) })
        params.status = params.status ?: [RDStore.SUBSCRIPTION_CURRENT.id.toString()]
        result = result + compareService.compareEntitlements(params, result)

        result.ies = result.ies.sort { genericOIDService.resolveOID(it.key).name }
        render template: "compareEntitlementRow", model: result
    }
}
