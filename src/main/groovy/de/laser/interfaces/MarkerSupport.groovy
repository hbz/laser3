package de.laser.interfaces

import de.laser.auth.User
import de.laser.convenience.Marker

interface MarkerSupport {

    void setMarker(User user, Marker.TYPE type)

    void removeMarker(User user, Marker.TYPE type)

    boolean isMarked(User user, Marker.TYPE type)

}
