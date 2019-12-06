package de.laser.domain

import com.k_int.kbplus.IssueEntitlement

class IssueEntitlementCoverage extends AbstractCoverage {

    static belongsTo = [issueEntitlement: IssueEntitlement]

    static constraints = {
        startDate     (nullable:true, blank:true)
        startVolume   (nullable:true, blank:true)
        startIssue    (nullable:true, blank:true)
        endDate       (nullable:true, blank:true)
        endVolume     (nullable:true, blank:true)
        endIssue      (nullable:true, blank:true)
        embargo       (nullable:true, blank:true)
        coverageDepth (nullable:true, blank:true)
        coverageNote  (nullable:true, blank:true)
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
        issueEntitlement column:'ic_ie_fk'
        sort startDate: 'asc', startVolume: 'asc', startIssue: 'asc', endDate: 'asc', endVolume: 'asc', endIssue: 'asc'
    }
}
