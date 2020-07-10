package de.laser.auth

class PermGrant {

    Perm perm
    Role role

    static mapping = {
        cache   true
    }

    static constraints = {
        perm    blank: false, unique: false
        role    blank: false, unique: false
    }
}
