package com.k_int.kbplus

import de.laser.base.AbstractBase
import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation

import javax.persistence.Transient

class CostItemElementConfiguration extends AbstractBase {

    @RefdataAnnotation(cat = RDConstants.COST_ITEM_ELEMENT)
    RefdataValue costItemElement

    @RefdataAnnotation(cat = RDConstants.COST_CONFIGURATION)
    RefdataValue elementSign

    Org  forOrganisation
    Date dateCreated
    Date lastUpdated

    def contextService

    @Transient
    def springSecurityService

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
        costItemElement     (blank: false)
        elementSign         (blank: false)
        forOrganisation     (blank: false)
    }

    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }
    @Override
    def beforeUpdate() {
        super.beforeUpdateHandler()
    }
}
