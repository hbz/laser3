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

    void doFilterCostItem(Map<String, Object> result, GrailsParameterMap params) {

        CostItemFilter filter = new CostItemFilter()
        result.filterResult = filter.filter(params)

        result.cfgQueryList.putAll( CostItemConfig.CONFIG.base.query )
    }

    void doFilterLicense(Map<String, Object> result, GrailsParameterMap params) {

        LicenseFilter filter = new LicenseFilter()
        result.filterResult = filter.filter(params)

        result.cfgQueryList.putAll( LicenseConfig.CONFIG.base.query )
        result.cfgQueryList.putAll( LicenseConfig.CONFIG.licensor.query )

        result.cfgQuery2List.putAll( LicenseConfig.CONFIG.base.query2 ) // Verteilung
    }

    void doFilterOrganisation(Map<String, Object> result, GrailsParameterMap params) {

        OrganisationFilter filter = new OrganisationFilter()
        result.filterResult = filter.filter(params)

        result.cfgQueryList.putAll( OrganisationConfig.CONFIG.base.query )

        result.cfgQuery2List.putAll( OrganisationConfig.CONFIG.base.query2 ) // Verteilung
    }

    void doFilterSubscription(Map<String, Object> result, GrailsParameterMap params) {

        SubscriptionFilter filter = new SubscriptionFilter()
        result.filterResult = filter.filter(params)

        result.cfgQueryList.putAll( SubscriptionConfig.CONFIG.base.query )
        result.cfgQueryList.putAll( SubscriptionConfig.CONFIG.member.query )
        result.cfgQueryList.putAll( SubscriptionConfig.CONFIG.provider.query )

        result.cfgQuery2List.putAll( SubscriptionConfig.CONFIG.base.query2 ) // Verteilung
    }

    List getCachedFilterIdList(String prefix, GrailsParameterMap params) {

        List<Long> idList = params?.filterCache?.data?.get(prefix + 'IdList')?.collect { it as Long }
        return idList ?: []
    }
}
