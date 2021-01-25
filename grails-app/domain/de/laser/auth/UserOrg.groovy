package de.laser.auth

import de.laser.Org
import org.codehaus.groovy.util.HashCodeHelper
import javax.persistence.Transient

//@GrailsCompileStatic
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

    @Transient
    String getSortString() {
        return org.name + ' ' + formalRole.authority
    }

    @Transient
    int compareTo(obj) {
        sortString.compareTo(((UserOrg) obj)?.getSortString())
    }

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

