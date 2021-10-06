package de.laser.auth

/**
 * Class to retain permissions to role types; this ensures the permission cascade for users and orgs
 */
class Perm {

    String code //the perm being granted

    static mapping = {
        cache   true
        version false
    }

    static constraints = {
        code    blank: false, unique: true
    }

    static hasMany = [
            grantedTo: PermGrant
    ]

    static mappedBy = [
            grantedTo: 'perm'
    ]
}
