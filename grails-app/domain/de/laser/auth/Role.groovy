package de.laser.auth

import de.laser.I10nTranslation
import de.laser.Org
import de.laser.base.AbstractI10n
import groovy.util.logging.Slf4j

/**
 * A role a {@link User} or an {@link Org} may have, attributing certain {@link Perm}s, depending to the {@link PermGrant}s granted to the role
 */
//@GrailsCompileStatic
//@EqualsAndHashCode(includes='authority')
@Slf4j
class Role extends AbstractI10n {

	/**
	 * the name of the role
	 */
	String authority
	String authority_de
	String authority_en
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
		authority 	 			     blank: false, unique: true
		authority_de nullable: true, blank: false
		authority_en nullable: true, blank: false
		roleType	 nullable: true, blank: false
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
