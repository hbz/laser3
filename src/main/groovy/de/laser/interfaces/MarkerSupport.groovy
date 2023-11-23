package de.laser.interfaces

import de.laser.auth.User
import de.laser.convenience.Marker

/**
 * The interface for object markers. Object markers are to store a user watchlist for
 * objects coming from the we:kb knowledge base; possible objects for marking are:
 * <ul>
 *     <li>{@link de.laser.Org}</li>
 *     <li>{@link de.laser.Package}</li>
 *     <li>{@link de.laser.Platform}</li>
 * </ul>
 */
interface MarkerSupport {

    /**
     * Sets the marker for the underlying object for given user of the given type
     * @param user the {@link User} for which the implementing object should be marked
     * @param type the {@link Marker.TYPE} of marker to record
     */
    void setMarker(User user, Marker.TYPE type)

    /**
     * Removes the given marker with the given type for the implementing object from the user's watchlist
     * @param user the {@link User} from whose watchlist the object marker should be removed
     * @param type the {@link Marker.TYPE} of marker to remove
     */
    void removeMarker(User user, Marker.TYPE type)

    /**
     * Checks if the implementing object is being marked for the given user with the given marker type
     * @param user the {@link User} whose watchlist should be checked
     * @param type the {@link Marker.TYPE} of the marker to check
     * @return true if the implementing object is marked, false otherwise
     */
    boolean isMarked(User user, Marker.TYPE type)

}
