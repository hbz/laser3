package de.laser

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

}
