package de.laser.auth

import grails.gorm.DetachedCriteria
import org.codehaus.groovy.util.HashCodeHelper

import javax.persistence.Transient

//@GrailsCompileStatic
//@ToString(cache=true, includeNames=true, includePackage=false)
class UserRole implements Serializable, Comparable {

	private static final long serialVersionUID = 1

    User user
    Role role

	Date dateCreated
	Date lastUpdated

	static constraints = {
		user nullable: false, blank: false
		role nullable: false, blank: false, validator: { Role r, UserRole ur ->
			if (ur.user?.id) {
				if (UserRole.exists(ur.user.id, r.id)) {
					return ['userRole.exists']
				}
			}
		}
		lastUpdated	nullable: true, blank: false
		dateCreated nullable: true, blank: false
	}

	static mapping = {
		cache           true
		id 				composite: ['role', 'user']
		version 		false
		lastUpdated     column: 'ur_last_updated'
		dateCreated     column: 'ur_date_created'
	}

	@Override
	boolean equals(other) {
		if (other instanceof UserRole) {
			other.user.id == user.id && other.role.id == role.id
		}
	}

    @Override
	int hashCode() {
	    int hashCode = HashCodeHelper.initHash()
        if (user) {
            hashCode = HashCodeHelper.updateHash(hashCode, user.id)
		}
		if (role) {
		    hashCode = HashCodeHelper.updateHash(hashCode, role.id)
		}
		hashCode
	}

	static UserRole get(long userId, long roleId) {
		criteriaFor(userId, roleId).get()
	}

	static boolean exists(long userId, long roleId) {
		criteriaFor(userId, roleId).count()
	}

	private static DetachedCriteria criteriaFor(long userId, long roleId) {
		UserRole.where {
			user == User.load(userId) &&
			role == Role.load(roleId)
		}
	}

	static UserRole create(User user, Role role, boolean flush = false) {
		def instance = new UserRole(user: user, role: role)
		instance.save(flush: flush)
		instance
	}

	static boolean remove(User u, Role r) {
		if (u != null && r != null) {
			UserRole.where { user == u && role == r }.deleteAll()
		}
	}

	static int removeAll(User u) {
		u == null ? 0 : UserRole.where { user == u }.deleteAll() as int
	}

	static int removeAll(Role r) {
		r == null ? 0 : UserRole.where { role == r }.deleteAll() as int
	}

	@Transient
	String getSortString() {
		return user.display + ' ' + role.authority
	}

	@Override
	String toString() {
		return '(' + user.id + ',' + role.id + ')'
	}

	@Transient
	int compareTo(obj) {
		sortString.compareTo(obj?.sortString)
	}
}
