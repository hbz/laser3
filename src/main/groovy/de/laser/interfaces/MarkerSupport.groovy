package de.laser.interfaces

import de.laser.auth.User
import de.laser.convenience.Favorite

interface MarkerSupport {

    void setMarker(User user, Favorite.TYPE type)

    void removeMarker(User user, Favorite.TYPE type)

    boolean isMarked(User user, Favorite.TYPE type)

}
