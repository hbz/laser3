package com.k_int.kbplus.auth

import com.k_int.kbplus.Org
import javax.persistence.Transient
import org.apache.commons.lang.builder.HashCodeBuilder

class UserOrg implements Comparable {

    static STATUS_PENDING       = 0
    static STATUS_APPROVED      = 1
    static STATUS_REJECTED      = 2
    // static STATUS_AUTO_APPROVED = 3
    // static STATUS_CANCELLED     = 4

    int status  // 0=Pending, 1=Approved, 2=Rejected

    Long dateRequested
    Long dateActioned

    Org org
    User user
    Role formalRole

    static constraints = {
        dateActioned    (nullable: true)
        dateRequested   (nullable: true)
        formalRole      (nullable: true)
    }

    @Transient
    String getSortString() {
        return org?.name + ' ' + formalRole?.authority
    }

    @Transient
    int compareTo(obj) {
        sortString.compareTo(obj?.sortString)
    }

    int hashCode() {
        def builder = new HashCodeBuilder()

        if (user) {
            builder.append(user.id)
        }
        if (org) {
            builder.append(org.id)
        }
        if (formalRole) {
            builder.append(formalRole.id)
        }

        builder.toHashCode()
    }
}

