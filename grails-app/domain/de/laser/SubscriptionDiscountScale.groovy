package de.laser

/**
 * This class represents discount scales for subscriptions, i.e. keeps track for a given subscription eventual discounts, usually
 * linked to a certain level of subscribers
 * To each discount, a note may be attached
 */
class SubscriptionDiscountScale {

    Subscription subscription
    String name
    String discount
    String note

    Date dateCreated
    Date lastUpdated

    static mapping = {
        id column:'sds_id'
        version column:'sds_version'

        subscription column:'sds_sub_fk',  index: 'sds_sub_idx'

        name column: 'sds_name'
        discount column: 'sds_discount'
        note column: 'sds_note', type: 'text'

        dateCreated column: 'sp_date_created'
        lastUpdated column: 'sp_last_updated'
    }

    static belongsTo = [
            subscription: Subscription
    ]


    static constraints = {
        note         (nullable:true)
    }

    /**
     * Outputs the discount scale as human-readable string
     * @return the discount scale as concatenated string
     */
    @Override
    String toString() {
        name + ' (' + discount + ')'
    }

}
