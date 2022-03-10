package de.laser.base

import grails.gorm.dirty.checking.DirtyCheck

import javax.persistence.Transient

@DirtyCheck
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
    static Set<String> equivalencyProperties = [
            'startDate',
            'startVolume',
            'endDate',
            'endVolume'
    ]

    AbstractCoverage findEquivalent(Collection<AbstractCoverage> list) {
        AbstractCoverage equivalent
        for (String k : equivalencyProperties) {
            equivalent = list.find { AbstractCoverage cov -> cov[k] == this[k] }
            if (equivalent) {
                //println "Coverage statement ${equivalent.id} considered as equivalent to ${this.id}"
                break
            }
        }
        equivalent
    }
}
