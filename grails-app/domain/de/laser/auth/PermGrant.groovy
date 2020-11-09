package de.laser.auth

class PermGrant {

    //Perm perm
    //Role role

    static belongsTo = [perm: Perm, role: Role]

    static mapping = {
        cache   true
    }

    static constraints = {
        perm    unique: false
        role    unique: false
    }
}
