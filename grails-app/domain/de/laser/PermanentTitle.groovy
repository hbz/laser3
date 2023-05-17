package de.laser

class PermanentTitle {

    Org owner
    Subscription subscription
    IssueEntitlement issueEntitlement
    TitleInstancePackagePlatform tipp

    Date dateCreated
    Date lastUpdated

    static constraints = {

    }

    static mapping = {
        id column: 'pt_id'
        version column: 'pt_version'

        dateCreated column: 'pt_date_created'
        lastUpdated column: 'pt_last_updated'

        owner column: 'pt_owner_fk'
        subscription column: 'pt_subscription_fk'
        issueEntitlement column: 'pt_ie_fk'
        tipp column: 'pt_tipp_fk'
    }
}
