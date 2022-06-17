package de.laser.finance

import de.laser.Org
import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.base.AbstractBase
import de.laser.storage.RDConstants

/**
 * An calculation sign configuration class for an {@link Org}: an {@link Org} may specify, to a given cost item element, how the cost items in it should be calculated (add, negative, substract).
 * The cost item element is a {@link RefdataValue} of category {@link RDConstants#COST_ITEM_ELEMENT}, the operand sign a {@link RefdataValue} of category {@link RDConstants#COST_CONFIGURATION}
 */
class CostItemElementConfiguration extends AbstractBase {

    @RefdataInfo(cat = RDConstants.COST_ITEM_ELEMENT)
    RefdataValue costItemElement

    @RefdataInfo(cat = RDConstants.COST_CONFIGURATION)
    RefdataValue elementSign

    Org  forOrganisation
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id                  column: 'ciec_id'
        version             column: 'ciec_version'
        globalUID           column: 'ciec_guid'
        costItemElement     column: 'ciec_cie_rv_fk'
        elementSign         column: 'ciec_cc_rv_fk'
        forOrganisation     column: 'ciec_org_fk'
        autoTimestamp true
    }

    static constraints = {
        globalUID           (nullable: true, blank: false, unique: true, maxSize: 255)
    }

    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }
    @Override
    def beforeUpdate() {
        super.beforeUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }
}
