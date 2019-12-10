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
            if(cp in ['startDate','endDate']) {
                Calendar calA = Calendar.getInstance(), calB = Calendar.getInstance()
                calA.setTime((Date) this[cp])
                calB.setTime((Date) covB[cp])
                if(!(calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) && calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR))) {
                    diffs.field = cp
                    diffs.event = 'updated'
                    diffs.target = this
                    diffs.oldValue = this[cp]
                    diffs.newValue = covB[cp]
                }
            }
            else {
                if(this[cp] != covB[cp]) {
                    diffs.field = cp
                    diffs.event = 'updated'
                    diffs.target = this
                    diffs.oldValue = this[cp]
                    diffs.newValue = covB[cp]
                }
            }
        }
        diffs
    }

}
