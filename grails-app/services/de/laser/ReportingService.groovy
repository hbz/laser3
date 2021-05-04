package de.laser

import de.laser.reporting.myInstitution.CostItemConfig
import de.laser.reporting.myInstitution.CostItemFilter
import de.laser.reporting.myInstitution.LicenseConfig
import de.laser.reporting.myInstitution.LicenseFilter
import de.laser.reporting.myInstitution.OrganisationConfig
import de.laser.reporting.myInstitution.OrganisationFilter
import de.laser.reporting.myInstitution.SubscriptionConfig
import de.laser.reporting.myInstitution.SubscriptionFilter
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

        if (params.filter == CostItemConfig.KEY) {
            doFilterCostItem(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == LicenseConfig.KEY) {
            doFilterLicense(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == OrganisationConfig.KEY) {
            doFilterOrganisation(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == SubscriptionConfig.KEY) {
            doFilterSubscription(result, params.clone() as GrailsParameterMap)
        }
    }

    // -----

    void doFilterCostItem(Map<String, Object> result, GrailsParameterMap params) {

        CostItemFilter filter = new CostItemFilter()
        result.filterResult = filter.filter(params)

        result.cfgQueryList.putAll( CostItemConfig.getCurrentConfig().base.query.default )
    }

    void doFilterLicense(Map<String, Object> result, GrailsParameterMap params) {

        LicenseFilter filter = new LicenseFilter()
        result.filterResult = filter.filter(params)

        result.cfgQueryList.putAll( LicenseConfig.getCurrentConfig().base.query.default )

        if (LicenseConfig.getCurrentConfig().licensor) {
            result.cfgQueryList.putAll(LicenseConfig.getCurrentConfig().licensor.query.default )
        }

        result.cfgQuery2List.putAll( LicenseConfig.getCurrentConfig().base.query2 ) // Verteilung
    }

    void doFilterOrganisation(Map<String, Object> result, GrailsParameterMap params) {

        OrganisationFilter filter = new OrganisationFilter()
        result.filterResult = filter.filter(params)

        if (params.get('filter:org_source').contains('providerAndAgency')) {
            result.cfgQueryList.putAll( OrganisationConfig.getCurrentConfig().base.query.providerAndAgency )
        }
        else if (params.get('filter:org_source').contains('provider')) {
            result.cfgQueryList.putAll( OrganisationConfig.getCurrentConfig().base.query.provider )
        }
        else if (params.get('filter:org_source').contains('agency')) {
            result.cfgQueryList.putAll( OrganisationConfig.getCurrentConfig().base.query.agency )
        }
        else {
            result.cfgQueryList.putAll( OrganisationConfig.getCurrentConfig().base.query.default )
        }

        result.cfgQuery2List.putAll( OrganisationConfig.getCurrentConfig().base.query2 ) // Verteilung
    }

    void doFilterSubscription(Map<String, Object> result, GrailsParameterMap params) {

        SubscriptionFilter filter = new SubscriptionFilter()
        result.filterResult = filter.filter(params)

        result.cfgQueryList.putAll( SubscriptionConfig.getCurrentConfig().base.query.default )

        if (SubscriptionConfig.getCurrentConfig().member) {
            result.cfgQueryList.putAll(SubscriptionConfig.getCurrentConfig().member.query.default )
        }
        if (SubscriptionConfig.getCurrentConfig().consortium) {
            result.cfgQueryList.putAll(SubscriptionConfig.getCurrentConfig().consortium.query.default )
        }
        if (SubscriptionConfig.getCurrentConfig().provider) {
            result.cfgQueryList.putAll(SubscriptionConfig.getCurrentConfig().provider.query.default )
        }
        if (SubscriptionConfig.getCurrentConfig().agency) {
            result.cfgQueryList.putAll(SubscriptionConfig.getCurrentConfig().agency.query.default )
        }

        result.cfgQuery2List.putAll( SubscriptionConfig.getCurrentConfig().base.query2 ) // Verteilung
    }

    List getCachedFilterIdList(String prefix, GrailsParameterMap params) {

        List<Long> idList = params?.filterCache?.data?.get(prefix + 'IdList')?.collect { it as Long }
        return idList ?: []
    }
}
