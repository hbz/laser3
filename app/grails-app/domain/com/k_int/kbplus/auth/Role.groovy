package com.k_int.kbplus.auth

import de.laser.traits.I10nTrait
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

class Role implements I10nTrait {

    String authority
    String roleType
    Set grantedPermissions = []

    static mapping = {
        cache true
    }

    static hasMany = [
            grantedPermissions: PermGrant
    ]

    static mappedBy = [
            grantedPermissions: "role"
    ]

    static constraints = {
        authority   blank: false, unique: true
        roleType    blank: false, nullable: true
    }

    static def refdataFind(GrailsParameterMap params) {
        List<Map<String, Object>> result = []
        List<Role> ql = Role.findAllByAuthorityIlikeAndRoleType("${params.q}%", "global", params)

        ql.each { id ->
            result.add([id: "${id.class.name}:${id.id}", text: "${id.authority}"])
        }

        result
    }
}
