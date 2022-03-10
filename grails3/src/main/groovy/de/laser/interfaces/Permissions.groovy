package de.laser.interfaces

interface Permissions {

    boolean isEditableBy(user)

    boolean isVisibleBy(user)

    boolean hasPerm(perm, user)

    //def checkPermissions(perm, user)
}
