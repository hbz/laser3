package de.laser

/**
 * Issue entitlements may be organised in groups; this is the grouping unit where issue entitlements may be sorted in.
 * Issue entitlement groups are useful for organising titles of a package within a narrower range; use cases are cost distributions
 * (a {@link de.laser.finance.CostItem}) or to see in survey pickings which group was more populate than others, e.g. title purchases are
 * organised in phases, a consortium may then observe which member picked which title from which phase etc., it is then possible to
 * deduct which phases were more popular among members and which not
 * An issue entitlement group is assigned to a {@link Subscription} and may be defined there
 * @see IssueEntitlementGroupItem
 */
class IssueEntitlementGroup {

    Date dateCreated
    Date lastUpdated

    String name
    String description

    //Org owner

    static belongsTo = [sub: Subscription]

    static hasMany = [items: IssueEntitlementGroupItem]

    static constraints = {
        name        (blank: false, unique: 'sub')
        description (nullable: true, blank: true)
        sub         (unique: 'name')
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
