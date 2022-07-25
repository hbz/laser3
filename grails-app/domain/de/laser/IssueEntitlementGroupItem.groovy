package de.laser

/**
 * This is an assigment of an {@link IssueEntitlement} to an {@link IssueEntitlementGroup}
 */
class IssueEntitlementGroupItem {

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            ieGroup:    IssueEntitlementGroup,
            ie:         IssueEntitlement
    ]

    static mapping = {
        id              column: 'igi_id'
        version         column: 'igi_version'
        ieGroup         column: 'igi_ie_group_fk'
        ie              column: 'igi_ie_fk'
        lastUpdated     column: 'igi_last_updated'
        dateCreated     column: 'igi_date_created'
    }

    static constraints = {
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }
}
