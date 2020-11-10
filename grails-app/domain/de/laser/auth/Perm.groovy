package de.laser.auth

class Perm {

    String code

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
