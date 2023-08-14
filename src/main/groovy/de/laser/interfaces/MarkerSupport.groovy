package de.laser.interfaces

import de.laser.auth.User

interface MarkerSupport {

    void setMarker()

    void removeMarker()

    boolean isMarkedForUser(User user)
//    boolean isMarkedForUser(User user, Favorite.TYPE type)
}
