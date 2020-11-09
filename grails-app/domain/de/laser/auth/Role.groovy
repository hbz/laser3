package de.laser.auth

import de.laser.traits.I10nTrait

//@GrailsCompileStatic
//@EqualsAndHashCode(includes='authority')
//@ToString(includes='authority', includeNames=true, includePackage=false)
class Role implements I10nTrait {

	String authority
	String roleType

	static mapping = {
		cache 	true
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

	static def refdataFind(params) {
		List result = []
		String authority = "${params.q}%"

		List<Role> ql = Role.findAllByAuthorityIlikeAndRoleType(authority, 'global', params)
		ql.each { id ->
			result.add([id: "${id.class.name}:${id.id}", text: "${id.authority}"])
		}

		result
	}
}
