package de.laser

import de.laser.reporting.myInstitution.CostItemFilter
import de.laser.reporting.myInstitution.LicenseFilter
import de.laser.reporting.myInstitution.OrganisationFilter
import de.laser.reporting.myInstitution.SubscriptionFilter
import de.laser.reporting.myInstitution.base.BaseConfig
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.commons.lang3.RandomStringUtils

@Transactional
class ReportingService {

    def contextService

    // ----- MyInstitutionController.reporting() -----

    void doFilter(Map<String, Object> result, GrailsParameterMap params) {

        result.filter = params.filter
        result.token  = params.token ?: RandomStringUtils.randomAlphanumeric(16)

        result.cfgQueryList = [:]
        result.cfgQuery2List = [:]

        if (params.filter == BaseConfig.KEY_COSTITEM) {
            doFilterCostItem(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_LICENSE) {
            doFilterLicense(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_ORGANISATION) {
            doFilterOrganisation(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_SUBSCRIPTION) {
            doFilterSubscription(result, params.clone() as GrailsParameterMap)
        }
    }

    // -----

    void doFilterCostItem(Map<String, Object> result, GrailsParameterMap params) {

        CostItemFilter filter = new CostItemFilter()
        result.filterResult = filter.filter(params)

        result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_COSTITEM ).base.query.default )
    }

    void doFilterLicense(Map<String, Object> result, GrailsParameterMap params) {

        LicenseFilter filter = new LicenseFilter()
        result.filterResult = filter.filter(params)

        BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).keySet().each{ pk ->
            result.cfgQueryList.putAll(BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).get( pk ).query.default )
        }

        result.cfgQuery2List.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).base.query2 ) // Verteilung
    }

    void doFilterOrganisation(Map<String, Object> result, GrailsParameterMap params) {

        OrganisationFilter filter = new OrganisationFilter()
        result.filterResult = filter.filter(params)

        if (params.get('filter:org_source').contains('providerAndAgency')) {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.providerAndAgency )
        }
        else if (params.get('filter:org_source').contains('provider')) {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.provider )
        }
        else if (params.get('filter:org_source').contains('agency')) {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.agency )
        }
        else {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.default )
        }

        result.cfgQuery2List.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query2 ) // Verteilung
    }

    void doFilterSubscription(Map<String, Object> result, GrailsParameterMap params) {

        SubscriptionFilter filter = new SubscriptionFilter()
        result.filterResult = filter.filter(params)

        BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).keySet().each{ pk ->
            result.cfgQueryList.putAll(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).get( pk ).query.default )
        }

        result.cfgQuery2List.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).base.query2 ) // Verteilung
    }

    List getCachedFilterIdList(String prefix, GrailsParameterMap params) {

        List<Long> idList = params?.filterCache?.data?.get(prefix + 'IdList')?.collect { it as Long }
        return idList ?: []
    }
}
