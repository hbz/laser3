package com.k_int.kbplus.auth

class Role {

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
        authority blank: false, unique: true
        roleType blank: false, nullable: true
    }

    static def refdataFind(params) {
        def result = [];
        def ql = null;
        ql = Role.findAllByAuthorityIlikeAndRoleType("${params.q}%", "global", params)

        if (ql) {
            ql.each { id ->
                result.add([id: "${id.class.name}:${id.id}", text: "${id.authority}"])
            }
        }

        result
    }
}
