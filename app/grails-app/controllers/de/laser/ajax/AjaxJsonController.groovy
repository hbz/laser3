package de.laser.ajax

import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Platform
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.SubscriptionPackage
import com.k_int.kbplus.auth.User
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.properties.PropertyDefinition

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Secured(['permitAll'])
class AjaxJsonController {

    /**
     * only json rendering here ..
     * no object manipulation
     *
     */

    def contextService
    def controlledListService
    def dataConsistencyService
    def genericOIDService

    def test() {
        Map<String, Object> result = [status: 'ok']
        result.id = params.id
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def consistencyCheck() {
        List result = dataConsistencyService.ajaxQuery(params.key, params.key2, params.value)
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def checkCascade() {
        Map<String, Object> result = [sub:true, subPkg:true, ie:true]
        if (!params.subscription && ((params.package && params.issueEntitlement) || params.issueEntitlement)) {
            result.sub = false
            result.subPkg = false
            result.ie = false
        }
        else if (params.subscription) {
            Subscription sub = (Subscription) genericOIDService.resolveOID(params.subscription)
            if (!sub) {
                result.sub = false
                result.subPkg = false
                result.ie = false
            }
            else if (params.issueEntitlement) {
                if (!params.package || params.package.contains('null')) {
                    result.subPkg = false
                    result.ie = false
                }
                else if (params.package && !params.package.contains('null')) {
                    SubscriptionPackage subPkg = (SubscriptionPackage) genericOIDService.resolveOID(params.package)
                    if(!subPkg || subPkg.subscription != sub) {
                        result.subPkg = false
                        result.ie = false
                    }
                    else {
                        IssueEntitlement ie = (IssueEntitlement) genericOIDService.resolveOID(params.issueEntitlement)
                        if(!ie || ie.subscription != subPkg.subscription || ie.tipp.pkg != subPkg.pkg) {
                            result.ie = false
                        }
                    }
                }
            }
        }
        render result as JSON
    }

    def getBooleans() {
        List result = [
                [value: 1, text: RDStore.YN_YES.getI10n('value')],
                [value: 0, text: RDStore.YN_NO.getI10n('value')]
        ]
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def getLinkedLicenses() {
        render controlledListService.getLinkedObjects([destination:params.subscription, sourceType: License.class.name, linkTypes:[RDStore.LINKTYPE_LICENSE], status:params.status]) as JSON
    }

    @Secured(['ROLE_USER'])
    def getLinkedSubscriptions() {
        render controlledListService.getLinkedObjects([source:params.license, destinationType: Subscription.class.name, linkTypes:[RDStore.LINKTYPE_LICENSE], status:params.status]) as JSON
    }

    @Secured(['ROLE_USER'])
    def getRegions() {
        List<RefdataValue> result = []
        if (params.country) {
            List<Long> countryIds = params.country.split(',')
            countryIds.each {
                switch (RefdataValue.get(it).value) {
                    case 'DE':
                        result.addAll( RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_DE]) )
                        break;
                    case 'AT':
                        result.addAll( RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_AT]) )
                        break;
                    case 'CH':
                        result.addAll( RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_CH]) )
                        break;
                }
            }
        }
        result = result.flatten()

        render result as JSON // TODO -> check response; remove unnecessary information! only id and value_<x>?
    }

    @Secured(['ROLE_USER'])
    def lookupBudgetCodes() {
        render controlledListService.getBudgetCodes(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupCombined() {
        render controlledListService.getElements(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupInvoiceNumbers() {
        render controlledListService.getInvoiceNumbers(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupIssueEntitlements() {
        params.checkView = true
        if(params.sub != "undefined") {
            render controlledListService.getIssueEntitlements(params) as JSON
        } else {
            Map entry = ["results": []]
            render entry as JSON
        }
    }

    @Secured(['ROLE_USER'])
    def lookupLicenses() {
        render controlledListService.getLicenses(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupOrderNumbers() {
        render controlledListService.getOrderNumbers(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupProvidersAgencies() {
        render controlledListService.getProvidersAgencies(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupProviderAndPlatforms() {
        List result = []

        List<Org> provider = Org.executeQuery('SELECT o FROM Org o JOIN o.orgType ot WHERE ot = :ot', [ot: RDStore.OT_PROVIDER])
        provider.each{ prov ->
            Map<String, Object> pp = [name: prov.name, value: prov.class.name + ":" + prov.id, platforms:[]]

            Platform.findAllByOrg(prov).each { plt ->
                pp.platforms.add([name: plt.name, value: plt.class.name + ":" + plt.id])
            }
            result.add(pp)
        }
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupReferences() {
        render controlledListService.getReferences(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupSubscriptions() {
        render controlledListService.getSubscriptions(params) as JSON
    }

    @Secured(['ROLE_USER'])
    def lookupSubscriptionPackages() {
        if (params.ctx != "undefined") {
            render controlledListService.getSubscriptionPackages(params) as JSON
        }
        else {
            render [:] as JSON
        }
    }

    @Secured(['ROLE_USER'])
    def lookupTitleGroups() {
        params.checkView = true
        if(params.sub != "undefined") {
            render controlledListService.getTitleGroups(params) as JSON
        } else {
            Map empty = [results: []]
            render empty as JSON
        }
    }

    def searchPropertyAlternativesByOID() {
        List<Map<String, Object>> result = []
        PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.oid)

        List<PropertyDefinition> queryResult = PropertyDefinition.findAllWhere(
                descr: pd.descr,
                refdataCategory: pd.refdataCategory,
                type: pd.type,
                multipleOccurrence: pd.multipleOccurrence,
                tenant: pd.tenant
        )//.minus(pd)

        queryResult.each { it ->
            PropertyDefinition rowobj = GrailsHibernateUtil.unwrapIfProxy(it)
            if (pd.isUsedForLogic) {
                if (it.isUsedForLogic) {
                    result.add([value: "${rowobj.class.name}:${rowobj.id}", text: "${it.getI10n('name')}"])
                }
            }
            else {
                if (! it.isUsedForLogic) {
                    result.add([value: "${rowobj.class.name}:${rowobj.id}", text: "${it.getI10n('name')}"])
                }
            }
        }
        if (result.size() > 1) {
            result.sort{ x,y -> x.text.compareToIgnoreCase y.text }
        }

        render result as JSON
    }

    @DebugAnnotation(test = 'hasRole("ROLE_ADMIN") || hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasRole('ROLE_ADMIN') || ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def checkExistingUser() {
        Map<String, Object> result = [result: false]
        if (params.input) {
            List<User> checkList = User.executeQuery("select u from User u where u.username = lower(:searchTerm)", [searchTerm:params.input])
            result.result = checkList.size() > 0
        }
        render result as JSON
    }
}