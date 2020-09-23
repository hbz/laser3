package de.laser.finance

import com.k_int.kbplus.Org
import de.laser.RefdataValue
import de.laser.base.AbstractBase
import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation

class CostItemElementConfiguration extends AbstractBase {

    def contextService
    def springSecurityService

    @RefdataAnnotation(cat = RDConstants.COST_ITEM_ELEMENT)
    RefdataValue costItemElement

    @RefdataAnnotation(cat = RDConstants.COST_CONFIGURATION)
    RefdataValue elementSign

    Org  forOrganisation
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id                  column: 'ciec_id'
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
