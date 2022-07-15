package de.laser


import de.laser.base.AbstractCoverage

/**
 * The coverage record for a local journal holding entry. The fields are the same as in {@link TIPPCoverage} as all of them may differ from the global level
 * (see {@link IssueEntitlement} vs. {@link TitleInstancePackagePlatform})
 */
class IssueEntitlementCoverage extends AbstractCoverage {

    Date dateCreated
    Date lastUpdated

    static belongsTo = [issueEntitlement: IssueEntitlement]

    static constraints = {
        startDate     (nullable:true)
        startVolume   (nullable:true, blank:true)
        startIssue    (nullable:true, blank:true)
        endDate       (nullable:true)
        endVolume     (nullable:true, blank:true)
        endIssue      (nullable:true, blank:true)
        embargo       (nullable:true, blank:true)
        coverageDepth (nullable:true, blank:true)
        coverageNote  (nullable:true, blank:true)
        lastUpdated   (nullable: true)
        dateCreated   (nullable: true)
    }

    static mapping = {
        id column: 'ic_id'
        version column: 'ic_version'
        startDate column:'ic_start_date',      index: 'ic_dates_idx'
        startVolume column:'ic_start_volume'
        startIssue column:'ic_start_issue'
        endDate column:'ic_end_date',        index: 'ic_dates_idx'
        endVolume column:'ic_end_volume'
        endIssue column:'ic_end_issue'
        embargo column:'ic_embargo'
        coverageDepth column:'ic_coverage_depth'
        coverageNote column:'ic_coverage_note',type: 'text'
        issueEntitlement column:'ic_ie_fk', index: 'ic_ie_idx'
        lastUpdated column:'ic_last_updated'
        dateCreated column:'ic_date_created'
        sort startDate: 'asc', startVolume: 'asc', startIssue: 'asc', endDate: 'asc', endVolume: 'asc', endIssue: 'asc'
    }
}
