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
//@ToString(includes='authority', includeNames=true, includePackage=false)
@Slf4j
class Role extends AbstractI10n {

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


	// FROM: I10nTrait, TODO: remove
	// returning virtual property for template tags; laser:select
	@Deprecated
	def propertyMissing(String name) {
		String[] parts = name.split("_")
		if (parts.size() == 2) {
			String fallback = this."${parts[0]}"
			String i10n = I10nTranslation.get(this, parts[0], parts[1])
			return (i10n ? i10n : "${fallback}")
		} else {
			log.debug '---> propertyMissing( ' + name + ' )'
			return name
		}
	}
}
