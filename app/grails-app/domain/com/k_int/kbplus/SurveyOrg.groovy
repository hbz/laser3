package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition

import javax.persistence.Transient

class SurveyOrg {

    @Transient
    def deletionService

    SurveyConfig surveyConfig
    Org org

    String priceComment
    Date dateCreated
    Date lastUpdated

    Date finishDate


    static constraints = {
        priceComment(nullable: true, blank: false)
        finishDate (nullable:true, blank:false)
    }

    static mapping = {
        id column: 'surorg_id'
        version column: 'surorg_version'

        surveyConfig column: 'surorg_surveyconfig_fk'
        org column: 'surorg_org_fk'
        priceComment column: 'surorg_pricecomment', type: 'text'
        dateCreated column: 'surorg_date_created'
        lastUpdated column: 'surorg_last_updated'
        finishDate  column: 'surorg_finish_date'
    }

    def afterDelete() {
        deletionService.deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id)
    }

    boolean existsMultiYearTerm() {
        boolean existsMultiYearTerm = false
        Subscription sub = surveyConfig.subscription
        if (sub) {
            def subChild = sub?.getDerivedSubscriptionBySubscribers(org)
            PropertyDefinition property = PropertyDefinition.findByName("Perennial term checked")

            if (subChild?.isCurrentMultiYearSubscription()) {
                existsMultiYearTerm = true
                return existsMultiYearTerm
            }

            if (property?.type == 'class com.k_int.kbplus.RefdataValue') {
                if (subChild?.customProperties?.find {
                    it?.type?.id == property?.id
                }?.refValue == RefdataValue.getByValueAndCategory('Yes', property?.refdataCategory)) {
                    existsMultiYearTerm = true
                    return existsMultiYearTerm
                }
            }
        }
        return existsMultiYearTerm
    }

    boolean hasOrgSubscription() {
        boolean hasOrgSubscription = false
        if (surveyConfig.subscription) {
            Subscription.findAllByInstanceOf(surveyConfig.subscription).each { s ->
                List<OrgRole> ors = OrgRole.findAllWhere(sub: s, org: this.org)
                ors.each { or ->
                    if (or.roleType?.value in ['Subscriber', 'Subscriber_Consortial']) {
                        hasOrgSubscription = true
                    }
                }
            }
        }
        return hasOrgSubscription

    }
}
