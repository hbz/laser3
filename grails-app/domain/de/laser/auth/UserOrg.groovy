package de.laser.auth

import de.laser.Org
import org.codehaus.groovy.util.HashCodeHelper
import javax.persistence.Transient

/**
 * The affiliation link between a {@link User} and an {@link Org}, rights marked by a {@link Role}
 */
class UserOrg implements Comparable {

    Date dateCreated
    Date lastUpdated

    static transients = ['sortString'] // mark read-only accessor methods

    static belongsTo = [user: User, org: Org, formalRole: Role]

    static mapping = {
        cache           true
        lastUpdated     column: 'uo_last_updated'
        dateCreated     column: 'uo_date_created'
    }

    static constraints = {
        lastUpdated     nullable: true
        dateCreated     nullable: true
    }

    /**
     * Generates a comprable string by the {@link Org}'s name and {@link Role}'s {@link Role#authority} (name)
     * @return the concatenated string of {@link Org}'s name and {@link Role}'s {@link Role#authority} (name)
     */
    @Transient
    String getSortString() {
        return org.name + ' ' + formalRole.authority
    }

    /**
     * Compares the given entry by the sortable string generated by {@link #getSortString}
     * @param obj the object to compare with
     * @return the comparison result (1, 0 or -1)
     */
    @Transient
    int compareTo(obj) {
        sortString.compareTo(((UserOrg) obj)?.getSortString())
    }

    /**
     * Gets a hash code by the {@link User}'s, {@link Org}'s and {@link Role}'s ids
     * @return the hash code generated
     */
    //used where???
    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (user) {
            hashCode = HashCodeHelper.updateHash(hashCode, user.id)
        }
        if (org) {
            hashCode = HashCodeHelper.updateHash(hashCode, org.id)
        }
        if (formalRole) {
            hashCode = HashCodeHelper.updateHash(hashCode, formalRole.id)
        }
        hashCode
    }
}

