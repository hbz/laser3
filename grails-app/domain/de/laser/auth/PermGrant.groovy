package de.laser.auth

/**
 * This is the linking table between {@link Perm}s and {@link Role}s. It is a n:n relation
 */
class PermGrant {

    static belongsTo = [perm: Perm, role: Role]

    static mapping = {
        cache   true
        version false
        id      column:'pmgr_id'
        perm    column:'pmgr_perm_fk'
        role    column:'pmgr_role_fk'
    }
}
