package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition

class SurveyOrg {


    SurveyConfig surveyConfig
    Org org

    String priceComment
    Date dateCreated
    Date lastUpdated


    static constraints = {
        priceComment (nullable:true, blank:false)
    }

    static mapping = {
        id column: 'surorg_id'
        version column: 'surorg_version'

        surveyConfig column: 'surorg_surveyconfig_fk'
        org column: 'surorg_org_fk'
        priceComment column: 'surorg_pricecomment', type: 'text'
        dateCreated column: 'surorg_date_created'
        lastUpdated column: 'surorg_last_updated'
    }

    def checkPerennialTerm()
    {
        def checkPerennialTerm = false
        if(surveyConfig.subscription)
        {
            def property = PropertyDefinition.findByName("Mehrjahreslaufzeit ausgewählt")

            if(property?.type == 'class com.k_int.kbplus.RefdataValue'){
                def sub = surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(org)

                if(sub.getCalculatedSuccessor() || sub?.customProperties?.find{it?.type?.id == property?.id}?.refValue == RefdataValue.getByValueAndCategory('Yes', property?.refdataCategory)){
                    checkPerennialTerm = true
                }
            }
        }
        return checkPerennialTerm
    }

    def hasOrgSubscription()
    {
        def hasOrgSubscription = false
        if(surveyConfig.subscription) {
            Subscription.findAllByInstanceOf(surveyConfig.subscription).each { s ->
                def ors = OrgRole.findAllWhere( sub: s, org: this.org )
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
