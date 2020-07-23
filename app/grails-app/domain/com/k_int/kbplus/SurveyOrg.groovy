package com.k_int.kbplus


import javax.persistence.Transient

class SurveyOrg {

    @Transient
    def deletionService

    SurveyConfig surveyConfig
    Org org

    String priceComment
    String ownerComment
    Date dateCreated
    Date lastUpdated

    Date finishDate


    static constraints = {
        ownerComment(nullable: true, blank: false)
        priceComment(nullable: true, blank: false)
        finishDate  (nullable:true)
    }

    static mapping = {
        id column: 'surorg_id'
        version column: 'surorg_version'

        surveyConfig column: 'surorg_surveyconfig_fk'
        org column: 'surorg_org_fk'
        priceComment column: 'surorg_pricecomment', type: 'text'
        ownerComment column: 'surorg_owner_comment', type: 'text'
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

            if (subChild?.isCurrentMultiYearSubscriptionNew()) {
                existsMultiYearTerm = true
                return existsMultiYearTerm
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
