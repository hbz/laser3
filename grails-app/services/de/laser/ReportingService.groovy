package de.laser

import de.laser.reporting.myInstitution.LicenseFilter
import de.laser.reporting.myInstitution.OrganisationFilter
import de.laser.reporting.myInstitution.SubscriptionFilter
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class ReportingService {

    def contextService

    Map<String, Object> filterLicense(GrailsParameterMap params) {
        LicenseFilter filter = new LicenseFilter()
        filter.filter(params.clone() as GrailsParameterMap)
    }

    Map<String, Object> filterOrganisation(GrailsParameterMap params) {
        OrganisationFilter filter = new OrganisationFilter()
        filter.filter(params.clone() as GrailsParameterMap)
    }

    Map<String, Object> filterSubscription(GrailsParameterMap params) {
        SubscriptionFilter filter = new SubscriptionFilter()
        filter.filter(params.clone() as GrailsParameterMap)
    }
}
