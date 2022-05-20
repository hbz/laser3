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

    final static TYPE_LOCAL          = 'Local'
    final static TYPE_CONSORTIAL     = 'Consortial'
    final static TYPE_ADMINISTRATIVE = 'Administrative'
    final static TYPE_PARTICIPATION  = 'Participation'
    final static TYPE_UNKOWN         = 'Unknown'

    /**
     * Gets the calculated type (= internally evaluated type) of the given instance
     * @return the calculated type enum string
     */
    String _getCalculatedType()
}
