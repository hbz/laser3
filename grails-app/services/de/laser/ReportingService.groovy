package de.laser

import de.laser.reporting.OrganisationFilter
import de.laser.reporting.SubscriptionFilter
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class ReportingService {

    def contextService

    Map<String, Object>  filterOrganisation(GrailsParameterMap params) {
        OrganisationFilter filter = new OrganisationFilter()
        filter.filter(params.clone() as GrailsParameterMap)
    }

    Map<String, Object>  filterSubscription(GrailsParameterMap params) {
        SubscriptionFilter filter = new SubscriptionFilter()
        filter.filter(params.clone() as GrailsParameterMap)
    }

    static String getFormFieldType(Map<String, Object> objConfig, String fieldName) {

        String fieldType = '' // [ property, refdata ]
        objConfig.form.each {
            if (it.keySet().contains(fieldName)) {
                fieldType = it.get(fieldName)
            }
        }
        fieldType
    }
}
