package de.laser.interfaces

/**
 * An enum containing object types. These object types are being used for logic.
 * Current classes using those types:
 * <ul>
 *     <li>{@link de.laser.finance.CostItem}</li>
 *     <li>{@link de.laser.License}</li>
 *     <li>{@link de.laser.Subscription}</li>
 * </ul>
 */
interface CalculatedType {

    public static final String TYPE_LOCAL          = 'Local'
    public static final String TYPE_CONSORTIAL     = 'Consortial'
    public static final String TYPE_ADMINISTRATIVE = 'Administrative'
    public static final String TYPE_PARTICIPATION  = 'Participation'
    public static final String TYPE_UNKOWN         = 'Unknown'

    /**
     * Gets the calculated type (= internally evaluated type) of the given instance
     * @return the calculated type enum string
     */
    String _getCalculatedType()
}
