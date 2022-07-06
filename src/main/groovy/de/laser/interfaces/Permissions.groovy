package de.laser.interfaces

import de.laser.auth.User

interface Permissions {

    boolean isEditableBy(User user)

    boolean isVisibleBy(User user)

    boolean hasPerm(String perm, User user)
}
