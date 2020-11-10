package de.laser.auth

class PermGrant {

    static belongsTo = [perm: Perm, role: Role]

    static mapping = {
        cache   true
        version false
    }

    static constraints = {
        perm    unique: false
        role    unique: false
    }
}
