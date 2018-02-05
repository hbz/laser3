package de.laser.domain

interface Permissions {

    def isEditableBy(user)

    def hasPerm(perm, user)

    //def checkPermissions(perm, user)
}
