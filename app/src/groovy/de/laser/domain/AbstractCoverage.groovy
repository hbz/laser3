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
    private Set controlledProperties = ['startDate','startVolume','endDate','endVolume']

    @Transient
    AbstractCoverage findEquivalent(Collection list) {
        AbstractCoverage equivalent
        for(String k: controlledProperties) {
            equivalent = list.find { it[k] == this[k] }
            if(equivalent) {
                println "Coverage statement ${equivalent.id} considered as equivalent to ${this.id}"
                break
            }
        }
        equivalent
    }

}
