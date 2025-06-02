package de.laser


import de.laser.finance.CostItem
import de.laser.finance.CostItemElementConfiguration
import de.laser.annotations.DebugInfo
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages an institution's cost configurations
 * @see CostItem
 * @see CostItemElementConfiguration
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class CostConfigurationController {

    ContextService contextService
    FinanceService financeService
    GenericOIDService genericOIDService

    /**
     * Gets the current list of an institution's cost configurations
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    Map<String, Object> index() {
        Map<String, Object> result = [:]

        List costItemElementConfigurations = []
        List<RefdataValue> costItemElements = RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_ELEMENT)

        costItemElements.each { cie ->
            CostItemElementConfiguration currentSetting = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(RefdataValue.getByValueAndCategory(cie.value, RDConstants.COST_ITEM_ELEMENT), contextService.getOrg())
            if(currentSetting) {
                costItemElementConfigurations.add(currentSetting)
            }
        }
        result.editable = contextService.isInstEditor()
        result.costItemElementConfigurations = costItemElementConfigurations
        result.costItemElements = costItemElements

        result
    }

    /**
     * Opens the creation modal, filtering out those cost item elements for which a configuration already exists
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    Object createNewConfiguration() {
        Map<String, Object> result = [editable:true] //the user clicking here is already authenticated
        Set<RefdataValue> costItemElements = RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_ELEMENT)
        Set<RefdataValue> elementsAlreadyTaken = []

        costItemElements.each { cie ->
            CostItemElementConfiguration currentSetting = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(RefdataValue.getByValueAndCategory(cie.value, RDConstants.COST_ITEM_ELEMENT), contextService.getOrg())
            if(currentSetting) {
                elementsAlreadyTaken.add(cie)
            }
        }
        costItemElements.removeAll(elementsAlreadyTaken)

        result.formUrl = g.createLink([controller:'costConfiguration',action:'processConfigurationCreation'])
        result.costItemElements = costItemElements
        result.elementSigns = RefdataCategory.getAllRefdataValues(RDConstants.COST_CONFIGURATION)

        render template: '/templates/newCostItemElementConfiguration', model: result
    }

    /**
     * Controller call for creating a new cost item element configuration
     * @return the list view
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def processConfigurationCreation() {
        financeService.processConfigurationCreation(params)
        redirect action: 'index'
    }

    /**
     * Controller call for deleting a cost item element configuration
     * @return the list view
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def deleteCostConfiguration() {
        if(params.ciec) {
            CostItemElementConfiguration ciec = CostItemElementConfiguration.get(params.ciec)
            if(ciec)
                financeService.deleteCostConfiguration(ciec)
        }
        else {
            flash.error = message(code: 'costConfiguration.delete.noCiec') as String
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * This call triggers a bulk setting of every cost item which has no cost item element configuration yet, assigning
     * the given cost item element configuration to each of them
     * @return the list view
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def setAllCostItems() {
        RefdataValue cie = RefdataValue.get(params.long('cie'))
        Org org = contextService.getOrg()
        def concernedCostItems = CostItem.findAllByOwnerAndCostItemElementAndCostItemElementConfigurationAndCostItemStatusNotEqual(org,cie,null, RDStore.COST_ITEM_DELETED).collect {it.id}
        CostItemElementConfiguration ciec = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(cie,org)
        if(concernedCostItems) {
            CostItem.executeUpdate('UPDATE CostItem ci SET ci.costItemElementConfiguration = :ciec WHERE ci.id IN (:cci)',[ciec:ciec.elementSign,cci:concernedCostItems])
            flash.message = message(code:'costConfiguration.configureAllCostItems.done') as String
        }
        else flash.message = message(code:'costConfiguration.configureAllCostItems.nothingToDo') as String
        redirect(url: request.getHeader('referer'))
    }

}
