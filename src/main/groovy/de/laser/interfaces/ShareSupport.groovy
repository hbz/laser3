package de.laser.interfaces

import de.laser.traits.ShareableTrait

/**
 * This interface keeps the methods for sharing of documents ({@link de.laser.DocContext}) among
 * {@link de.laser.Subscription}s and {@link de.laser.License}s
 */
interface ShareSupport {

    /**
     * Checks if the implementing object is a consortial parent object and if the given relation is being shared
     * @param sharedObject the object to be shared
     * @return true if the conditions are met, false otherwise
     */
    boolean checkSharePreconditions(ShareableTrait sharedObject)

    /**
     * Checks whether the implementing object is a consortial parent object
     * @return true if the implementing object is of type {@link CalculatedType#TYPE_CONSORTIAL}, false otherwise
     */
    boolean showUIShareButton()

    /**
     * Toggles sharing for the given object
     * @param sharedObject the object implementing {@link ShareableTrait} whose sharing should be toggled
     */
    void updateShare(ShareableTrait sharedObject)

    /**
     * Processes each shareable object of the implementing object and toggles sharing on each of them
     * @param targets the member objects on which new objects should be attached
     */
    void syncAllShares(List<ShareSupport> targets)
}
