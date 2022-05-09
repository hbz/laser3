package de.laser.survey

import de.laser.Org
import de.laser.OrgRole
import de.laser.Subscription
import de.laser.storage.BeanStore
import de.laser.storage.RDStore

/**
 * This class represents a participation of an institution at a survey. As every object being surveyed has its own configuration,
 * the participation is being attached to the configuration (-> {@link SurveyConfig}) and not the general information object (-> {@link SurveyOrg})!
 * @see Org
 * @see SurveyConfig
 */
class SurveyOrg {

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
        BeanStore.getDeletionService().deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id, this.class.simpleName)
    }

    /**
     * Checks if the participant has for a given subscription survey's subscription a multi-year term running; this is an essential criteria for renewal surveys
     * @return true if the participant institution has subscribed perennially the subscription being focus of this survey, false otherwise
     * @see Subscription
     * @see SurveyConfig
     * @see Org
     */
    boolean existsMultiYearTerm() {
        boolean existsMultiYearTerm = false
        Subscription sub = surveyConfig.subscription

        if (sub) {
            Subscription subMuliYear = Subscription.executeQuery("select sub" +
                    " from Subscription sub " +
                    " join sub.orgRelations orgR " +
                    " where orgR.org = :org and orgR.roleType in :roleTypes " +
                    " and sub.instanceOf = :instanceOfSub" +
                    " and sub.isMultiYear = true and sub.endDate != null and (EXTRACT (DAY FROM (sub.endDate - NOW())) > 366)",
                    [org          : org,
                     roleTypes    : [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS],
                     instanceOfSub: sub])[0]

            if (subMuliYear) {
                return true
            }
        }


 /*       if (sub) {
            def subChild = sub.getDerivedSubscriptionBySubscribers(org)

            if (subChild?.isCurrentMultiYearSubscriptionNew()) {
                existsMultiYearTerm = true
                return existsMultiYearTerm
            }
        }*/
        return existsMultiYearTerm
    }

    @Deprecated
    boolean hasOrgSubscription() {
        boolean hasOrgSubscription = false
        if (surveyConfig.subscription) {
            Subscription.findAllByInstanceOf(surveyConfig.subscription).each { s ->
                List<OrgRole> ors = OrgRole.findAllWhere(sub: s, org: this.org)
                ors.each { OrgRole or ->
                    if (or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS]) {
                        hasOrgSubscription = true
                    }
                }
            }
        }
        return hasOrgSubscription

    }
}
