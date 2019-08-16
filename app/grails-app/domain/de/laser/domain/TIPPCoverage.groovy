package de.laser.domain

import com.k_int.kbplus.TitleInstancePackagePlatform
import de.laser.traits.AuditableTrait

import javax.persistence.Transient

class TIPPCoverage extends AbstractCoverage implements AuditableTrait {

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
    }

    static mapping = {
        id column:'tc_id'
        version column:'tc_version'
        startDate column:'tc_start_date',    index: 'tc_dates_idx'
        startVolume column:'tc_start_volume'
        startIssue column:'tc_start_issue'
        endDate column:'tc_end_date',      index: 'tc_dates_idx'
        endVolume column:'tc_end_volume'
        endIssue column:'tc_end_issue'
        embargo column:'tc_embargo'
        coverageDepth column:'tc_coverage_depth'
        coverageNote column:'tc_coverage_note', type: 'text'
        tipp column:'tc_tipp_fk'
    }


    static controlledProperties = [
            'startDate',
            'startVolume',
            'startIssue',
            'endDate',
            'endVolume',
            'endIssue',
            'embargo',
            'coverageDepth',
            'coverageNote',
    ]

    @Transient
    def onChange = { oldMap,newMap ->
        log.debug("onChange Tipp coverage")
        checkCoverageChanges(oldMap,newMap)
        log.debug("onChange coverage completed")
    }

    static checkCoverageChanges(oldMap, newMap) {
        if(oldMap && newMap) {
            controlledProperties.each { cp ->
                log.debug("checking ${cp}")
                if ( oldMap[cp] != newMap[cp] ) {
                    tipp.raisePendingChange(oldMap,newMap,cp)
                }
            }
        }
    }

}
