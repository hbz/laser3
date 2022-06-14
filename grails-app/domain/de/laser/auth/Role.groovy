package de.laser.auth

import de.laser.traits.I10nTrait
import de.laser.Org

/**
 * A role a {@link User} or an {@link Org} may have, attributing certain {@link Perm}s, depending to the {@link PermGrant}s granted to the role
 */
//@GrailsCompileStatic
//@EqualsAndHashCode(includes='authority')
//@ToString(includes='authority', includeNames=true, includePackage=false)
class Role implements I10nTrait {

	/**
	 * the name of the role
	 */
	String authority
	String roleType

	static mapping = {
		cache 	true
		version	false
	}

	static hasMany = [
			grantedPermissions: PermGrant
	]

	static mappedBy = [
			grantedPermissions: 'role'
	]

	static constraints = {
		authority 	blank: false, unique: true
		roleType	blank: false, nullable: true
	}

	/**
	 * Retrieves a list of role names for dropdown selection
	 * @param params the query params passed for lookup
	 * @return a {@link List} of {@link Map}s[id: text] containing role ids and names
	 */
	static def refdataFind(params) {
		//usage: AjaxJsonController.lookup by generic method
		List result = []
		String authority = "${params.q}%"

		List<Role> ql = Role.findAllByAuthorityIlikeAndRoleType(authority, 'global', params)
		ql.each { id ->
			result.add([id: "${id.class.name}:${id.id}", text: "${id.authority}"])
		}

		result
	}
}
