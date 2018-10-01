package de.laser.interfaces

interface Permissions {

    def isEditableBy(user)

    def isVisibleBy(user)

    def hasPerm(perm, user)

    //def checkPermissions(perm, user)
}
