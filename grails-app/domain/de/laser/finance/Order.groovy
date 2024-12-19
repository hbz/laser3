package de.laser.finance

import de.laser.GenericOIDService
import de.laser.Org
import de.laser.storage.BeanStore
import grails.web.servlet.mvc.GrailsParameterMap

import javax.persistence.Transient

class Order {

    String orderNumber
    Org owner

    Date dateCreated
    Date lastUpdated

  static mapping = {
              table 'ordering'
                id column:'ord_id'
           version column:'ord_version'
       orderNumber column:'ord_number'
             owner column:'ord_owner', index: 'ord_owner_idx'

      dateCreated column: 'ord_date_created'
      lastUpdated column: 'ord_last_updated'
  }

  static constraints = {
    orderNumber (blank:false)
    lastUpdated (nullable: true)
  }

    /**
     * @deprecated use {@link de.laser.ControlledListService#getOrderNumbers(grails.web.servlet.mvc.GrailsParameterMap)} instead
     */
    @Deprecated
    @Transient
    static def refdataFind(GrailsParameterMap params) {
        Org owner = Org.findByShortcode(params.shortcode)
        if (! owner) {
            return []
        }
        GenericOIDService genericOIDService = BeanStore.getGenericOIDService()

        genericOIDService.getOIDMapList(
                Order.findAllByOwnerAndOrderNumberIlike(owner,"%${params.q}%", params),
                'orderNumber'
        )
    }
}
