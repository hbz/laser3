package com.k_int.kbplus.auth

import com.k_int.kbplus.GenericOIDService
import de.laser.traits.I10nTrait
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

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
        GenericOIDService genericOIDService = (GenericOIDService) Holders.grailsApplication.mainContext.getBean('genericOIDService')

        genericOIDService.getOIDMapList(
                Role.findAllByAuthorityIlikeAndRoleType("${params.q}%", "global", params),
                'authority'
        )
    }
}
