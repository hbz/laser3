package de.laser.survey

import de.laser.addressbook.Address
import de.laser.Org
import de.laser.OrgRole
import de.laser.addressbook.Person
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.annotations.RefdataInfo
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
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
    Date reminderMailDate

    boolean orgInsertedItself = false

    @Deprecated
    Person person

    Address address

    @RefdataInfo(cat = RDConstants.E_INVOICE_PORTAL)
    RefdataValue eInvoicePortal
    String eInvoiceLeitwegId
    String eInvoiceLeitkriterium
    String peppolReceiverId

    static constraints = {
        ownerComment(nullable: true, blank: false)
        priceComment(nullable: true, blank: false)
        finishDate  (nullable:true)
        reminderMailDate (nullable:true)
        person (nullable:true)
        address (nullable:true)
        eInvoicePortal(nullable:true)
        eInvoiceLeitwegId(nullable:true)
        eInvoiceLeitkriterium(nullable:true)
        peppolReceiverId (nullable: true)
    }

    static mapping = {
        id column: 'surorg_id'
        version column: 'surorg_version'

        surveyConfig column: 'surorg_surveyconfig_fk', index: 'surorg_surveyconfig_idx'
        org column: 'surorg_org_fk', index: 'surorg_org_idx'
        priceComment column: 'surorg_pricecomment', type: 'text'
        ownerComment column: 'surorg_owner_comment', type: 'text'
        dateCreated column: 'surorg_date_created'
        lastUpdated column: 'surorg_last_updated'
        finishDate  column: 'surorg_finish_date'
        reminderMailDate column: 'surorg_reminder_mail_date'

        orgInsertedItself column: 'org_inserted_itself'

        person column: 'surorg_person_fk',      index: 'surorg_person_idx'
        address column: 'surorg_address_fk',    index: 'surorg_address_idx'
        eInvoicePortal      column:'surorg_e_invoice_portal_fk', index: 'surorg_e_invoice_portal_idx'
        eInvoiceLeitwegId column: 'surorg_e_invoice_leitweg_id'
        eInvoiceLeitkriterium column: 'surorg_e_invoice_leitkriterium'
        peppolReceiverId column: 'surorg_peppol_receiver_id'
    }

    def afterDelete() {
        BeanStore.getDeletionService().deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id, this.class.simpleName)
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
