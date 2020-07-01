package de.laser


import com.k_int.kbplus.Subscription

class IssueEntitlementGroup {

    Date dateCreated
    Date lastUpdated

    String name
    String description

    //Org owner

    static belongsTo = [sub: Subscription]

    static hasMany = [items: IssueEntitlementGroupItem]

    static constraints = {
        name        (nullable: false, blank: false, unique: 'sub')
        description (nullable: true, blank: true)

        sub         (nullable: false, blank: false, unique: 'name')

    }

    static mapping = {
        id          column: 'ig_id'
        version     column: 'ig_version'
        dateCreated column: 'ig_date_created'
        lastUpdated column: 'ig_last_updated'

        name        column: 'ig_name'
        description column: 'ig_description', type: 'text'

        sub         column: 'ig_sub_fk'

    }
}
