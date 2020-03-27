package com.k_int.kbplus.auth

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import org.apache.commons.lang.builder.HashCodeBuilder

import javax.persistence.Transient

class UserRole implements Serializable, Comparable {

    User user
    Role role

    Date dateCreated
    Date lastUpdated

    static mapping = {
        id composite: ['role', 'user']
        version false
        lastUpdated     column: 'ur_last_updated'
        dateCreated     column: 'ur_date_created'
    }

    static constraints = {
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    boolean equals(other) {
        if (! (other instanceof UserRole)) {
            return false
        }

        other.user?.id == user?.id && other.role?.id == role?.id
    }

    int hashCode() {
        def builder = new HashCodeBuilder()

        if (user) {
            builder.append(user.id)
        }
        if (role) {
            builder.append(role.id)
        }
        builder.toHashCode()
    }

    static UserRole get(long userId, long roleId) {
        find 'from UserRole where user.id=:userId and role.id=:roleId', [userId: userId, roleId: roleId]
    }

    static UserRole create(User user, Role role, boolean flush = false) {
        new UserRole(user: user, role: role).save(flush: flush, insert: true)
    }

    static boolean remove(User user, Role role, boolean flush = false) {
        UserRole instance = UserRole.findByUserAndRole(user, role)
        if (! instance) {
            return false
        }

        instance.delete(flush: flush)
        true
    }

    static void removeAll(User user) {
        executeUpdate 'DELETE FROM UserRole WHERE user=:user', [user: user]
    }

    static void removeAll(Role role) {
        executeUpdate 'DELETE FROM UserRole WHERE role=:role', [role: role]
    }

    @Transient
    String getSortString() {
        return user?.display + ' ' + role?.authority
    }

    @Override
    String toString() {
        return '(' + user?.id + ',' + role?.id + ')'
    }

    @Transient
    int compareTo(obj) {
        sortString.compareTo(obj?.sortString)
    }
}
