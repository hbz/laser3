package de.laser.auth

class PermGrant {

    Perm perm
    Role role

    static mapping = {
        cache   true
    }

    static constraints = {
        perm    unique: false
        role    unique: false
    }
}
