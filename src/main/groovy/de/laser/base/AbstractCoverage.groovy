package de.laser.base

import grails.gorm.dirty.checking.DirtyCheck

import javax.persistence.Transient

/**
 * This is an abstract class containing central properties both for global package-level and issue entitlement-level coverage statements.
 */
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

    /**
     * Locates in the given list the equivalent to the current coverage statement.
     * Matching is done on the properties defined in {@link #equivalencyProperties} in the order defined in the set.
     * The first match is being returned
     * @param list a {@link List} of statements in which an equivalent should be found
     * @return an equivalent coverage statement or null if none found
     */
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
