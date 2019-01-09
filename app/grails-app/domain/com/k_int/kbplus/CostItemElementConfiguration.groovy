package com.k_int.kbplus

import com.k_int.kbplus.Org
import com.k_int.kbplus.auth.User
import de.laser.domain.AbstractBaseDomain

import javax.persistence.Transient

class CostItemElementConfiguration extends AbstractBaseDomain {

    RefdataValue costItemElement    //RefdataCategory 'CostItemElement'
    RefdataValue elementSign        //RefdataCategory 'Cost configuration'
    Org forOrganisation

    Date dateCreated
    User createdBy
    Date lastUpdated
    User lastUpdatedBy

    def contextService
    def messageSource

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
        costItemElement     (nullable: false, blank: false)
        elementSign         (nullable: false, blank: false)
        forOrganisation     (nullable: false, blank: false)
        lastUpdatedBy       (nullable: true)
        createdBy           (nullable: true)
    }

    def beforeInsert() {
        super.beforeInsert()

        def user = springSecurityService.getCurrentUser()
        if (user) {
            createdBy     = user
            lastUpdatedBy = user
        } else
            return false
    }

    def beforeUpdate() {
        super.beforeUpdate()

        def user = springSecurityService.getCurrentUser()
        if (user)
            lastUpdatedBy = user
        else
            return false
    }

}
