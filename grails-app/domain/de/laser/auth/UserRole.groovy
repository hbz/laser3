package de.laser.auth

import org.codehaus.groovy.util.HashCodeHelper

import javax.persistence.Transient

/**
 * The linking between {@link User}s and {@link Role}s, ensuring permissions for users
 */
class UserRole implements Serializable, Comparable {

	private static final long serialVersionUID = 1

	Date dateCreated
	Date lastUpdated

	static belongsTo = [user: User, role: Role]

	static constraints = {
		role validator: { Role r, UserRole ur ->
			if (ur.user?.id) {
				if (exists(ur.user.id, r.id)) {
					return ['userRole.exists']
				}
			}
		}
		lastUpdated nullable: true
		dateCreated nullable: true
	}

	static mapping = {
		cache           true
		id 				composite: ['role', 'user']
		version 		false
		role      		column: 'ur_role_fk'
		user            column: 'ur_user_fk'
		lastUpdated     column: 'ur_last_updated'
		dateCreated     column: 'ur_date_created'
	}

	/**
	 * Compares a user role by user and role equity
	 * @param other the entity to comapre with
	 * @return are the two entities equal?
	 */
	//used where???
	@Override
	boolean equals(other) {
		if (other instanceof UserRole) {
			other.user.id == user.id && other.role.id == role.id
		}
		false //fallback
	}

	/**
	 * Gets a hash code by the {@link User}'s and {@link Role}'s ids
	 * @return the hash code generated
	 */
	//used where???
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

	/**
	 * Does a user exist with the given role?
	 * @param userId the {@link User} ID to check
	 * @param roleId the {@link Role} ID to check
	 * @return whether the given combination exists, i.e. if there is a user who has the given role assigned
	 */
	static boolean exists(long userId, long roleId) {
		UserRole.get(userId, roleId) != null
	}

	/**
	 * Gets the user role entry with the given combination of user and role IDs
	 * @param userId the {@link User} ID
	 * @param roleId the {@link Role} ID
	 * @return the user role record matching the two IDs
	 */
	static UserRole get(long userId, long roleId) {
		UserRole.get(User.load(userId), Role.load(roleId))
	}

	/**
	 * Gets the user role entry with the given combination of user and role objects
	 * (= the same as {@link #get(long, long)}, but with objects instead of database IDs)
	 * @param user the {@link User}
	 * @param role the {@link Role}
	 * @return the user role entry linking the two objects
	 */
	static UserRole get(User user, Role role) {
		UserRole.find('from UserRole ur where ur.user=:user and ur.role=:role', [ user: user, role: role ])
	}

	/**
	 * Grants the given role to the given user
	 * @param user the {@link User} whom the role should be granted to
	 * @param role the {@link Role} to grant
	 * @return the new link
	 */
	static UserRole create(User user, Role role, boolean flush = false) {
		UserRole.withTransaction {
			UserRole instance = new UserRole(user: user, role: role)
			instance.save()
			instance
		}
	}

	/**
	 * Revokes the given role from the given user
	 * @param u the {@link User} whom the role should be revoked from
	 * @param r the {@link Role} to be revoked
	 * @return was the removal successful?
	 */
	static int remove(User u, Role r) {
		int status = 0
		if (u != null && r != null) {
			status = UserRole.where { user == u && role == r }.deleteAll() as int
		}
		status
	}

	/**
	 * Revoke all roles from the given user
	 * @param u the {@link User} who should lose all his privileges
	 * @return the count of database entries removed
	 */
	static int removeAll(User u) {
		u == null ? 0 : UserRole.where { user == u }.deleteAll() as int
	}

	/**
	 * Revoke the given role from every user
	 * @param r the {@link Role} to be revoked from everyone
	 * @return the count of database entries removed
	 */
	static int removeAll(Role r) {
		r == null ? 0 : UserRole.where { role == r }.deleteAll() as int
	}

	/**
	 * Gets a comparable string of this entry, used by the comparator
	 * @return the {@link User}'s display name and {@link Role}'s name
	 */
	@Transient
	String getSortString() {
		return user.display + ' ' + role.authority
	}

	/**
	 * Stringify this entry
	 * @return the user id and role id, separated by comma
	 */
	@Override
	String toString() {
		try {
			return '(' + user?.id + ',' + role?.id + ')'
		} catch (Exception e) {
			return '(' + user?.id + ', ?)'
		}
	}

	/**
	 * Compared the given entry by their strings generated by {@link #getSortString}
	 * @param obj the other instance to compare to
	 * @return the comparison result (1, 0 or -1)
	 */
	@Transient
	int compareTo(obj) {
		sortString.compareTo(((UserRole) obj)?.getSortString())
	}
}
