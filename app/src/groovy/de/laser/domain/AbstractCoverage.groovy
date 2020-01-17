package de.laser.domain

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

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
                if(this[cp] != null && covB[cp] != null) {
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
                    /*
                    Means that one of the coverage dates is null or became null.
                    Cases to cover: null -> date (covA == null, covB instanceof Date)
                    date -> null (covA instanceof Date, covB == null)
                     */
                    //
                    if(this[cp] != null && covB[cp] == null) {
                        calA.setTime((Date) this[cp])
                        diffs.field = cp
                        diffs.event = 'updated'
                        diffs.target = this
                        diffs.oldValue = this[cp]
                        diffs.newValue = null
                    }
                    else if(this[cp] == null && covB[cp] != null) {
                        calB.setTime((Date) covB[cp])
                        diffs.field = cp
                        diffs.event = 'updated'
                        diffs.target = this
                        diffs.oldValue = null
                        diffs.newValue = covB[cp]
                    }
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

    AbstractCoverage findEquivalent(Collection<AbstractCoverage> list) {
        AbstractCoverage equivalent
        for (String k : controlledProperties) {
            equivalent = list.find { it[k] == this[k] }
            if (equivalent) {
                println "Coverage statement ${equivalent.id} considered as equivalent to ${this.id}"
                break
            }
        }
        equivalent
    }
}
