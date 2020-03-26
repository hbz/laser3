package de.laser.domain

import com.k_int.kbplus.TitleInstancePackagePlatform
import de.laser.traits.AuditableTrait

class TIPPCoverage extends AbstractCoverage implements AuditableTrait {

    Date dateCreated
    Date lastUpdated

    static belongsTo = [tipp: TitleInstancePackagePlatform]

    static constraints = {
        startDate(nullable:true, blank:true)
        startVolume(nullable:true, blank:true)
        startIssue(nullable:true, blank:true)
        endDate(nullable:true, blank:true)
        endVolume(nullable:true, blank:true)
        endIssue(nullable:true, blank:true)
        embargo(nullable:true, blank:true)
        coverageDepth(nullable:true, blank:true)
        coverageNote(nullable:true, blank:true)
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    static mapping = {
        id column:'tc_id'
        version column:'tc_version'
        startDate column:'tc_start_date', index: 'tc_dates_idx'
        startVolume column:'tc_start_volume'
        startIssue column:'tc_start_issue'
        endDate column:'tc_end_date', index: 'tc_dates_idx'
        endVolume column:'tc_end_volume'
        endIssue column:'tc_end_issue'
        embargo column:'tc_embargo'
        coverageDepth column:'tc_coverage_depth'
        coverageNote column:'tc_coverage_note', type: 'text'
        tipp column:'tc_tipp_fk'
        lastUpdated column:'tc_last_updated'
        dateCreated column:'tc_date_created'
        sort startDate: 'asc', startVolume: 'asc', startIssue: 'asc', endDate: 'asc', endVolume: 'asc', endIssue: 'asc'
    }

}
