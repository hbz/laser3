package de.laser


import de.laser.annotations.DebugInfo
import de.laser.helper.Params
import de.laser.storage.RDStore
import de.laser.utils.SwissKnife
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
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def compareLicenses() {
        Map<String, Object> result = [:]

        params.status = params.status ?: [RDStore.LICENSE_CURRENT.id]
        result.objects = []
        params.tab = params.tab ?: "compareElements"

        if (params.selectedObjects) {
            result.objects = License.findAllByIdInList( Params.getLongList(params, 'selectedObjects') )
        }

        if (params.tab == "compareProperties") {
            result = result + compareService.compareProperties(result.objects)
        }

        result
    }

    /**
     * Compares subscriptions against each other
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def compareSubscriptions() {
        Map<String, Object> result = [:]
        SwissKnife.setPaginationParams(result, params, contextService.getUser())

        params.status = params.status ?: [RDStore.SUBSCRIPTION_CURRENT.id]
        result.objects = []
        params.tab = params.tab ?: "compareElements"

        if (params.selectedObjects) {
            result.objects = Subscription.findAllByIdInList( Params.getLongList(params, 'selectedObjects') )
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
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def loadNextBatch() {
        Map<String, Object> result = [:]
        SwissKnife.setPaginationParams(result, params, contextService.getUser())

        result.objects = Subscription.findAllByIdInList(params.list('selectedObjects[]').collect { Long.valueOf(it) })
        params.status = params.status ?: [RDStore.SUBSCRIPTION_CURRENT.id]
        result = result + compareService.compareEntitlements(params, result)

        result.ies = result.ies.sort { genericOIDService.resolveOID(it.key).name }
        render template: "compareEntitlementRow", model: result
    }
}
