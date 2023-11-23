package de.laser.interfaces

import de.laser.auth.User

/**
 * The method capsuling methods for object permission control. Checked are permissions
 * for reading and writing. Objects on which access rights are being checked (= implementing these methods) are:
 * <ul>
 *     <li>{@link de.laser.License}</li>
 *     <li>{@link de.laser.Subscription}</li>
 * </ul>
 */
interface Permissions {

    /**
     * Checks if the implementing object is editable by the given user
     * @param user the {@link User} whose grants should be checked
     * @return true if the implementing object is editable, false otherwise
     */
    boolean isEditableBy(User user)

    /**
     * Checks if the implementing object is visible for the given user
     * @param user the {@link User} whose grants should be checked
     * @return true if the implementing object is visible, false otherwise
     */
    boolean isVisibleBy(User user)

    /**
     * Checks if the given permission has been granted to the given user for the implementing object
     * @param perm the permission string to check the grant of
     * @param user the {@link User} whose right should be checked
     * @return true if the given permission has been granted to the given user for the implementing object, false otherwise
     */
    boolean hasPerm(String perm, User user)
}
