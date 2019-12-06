package de.laser.domain

import javax.persistence.Transient

abstract class AbstractCoverage {

    Long id
    Date startDate
    String startVolume
    String startIssue
    Date endDate
    String endVolume
    String endIssue
    String embargo
    String coverageDepth
    String coverageNote

    @Transient
    private static Set<String> controlledProperties = [
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

    Map<String,Object> compareWith(Map<String,Object> covB) {
        Map<String,Object> diffs = [:]
        controlledProperties.each { cp ->
            if(this[cp] != covB[cp]) {
                diffs.field = cp
                diffs.event = 'updated'
                diffs.target = this
                diffs.oldValue = this[cp]
                diffs.newValue = covB[cp]
            }
        }
        diffs
    }

}
