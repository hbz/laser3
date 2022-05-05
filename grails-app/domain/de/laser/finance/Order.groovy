package de.laser.finance

import com.k_int.kbplus.GenericOIDService
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

      // Nullable is true, because values are already in the database
      lastUpdated (nullable: true)
      dateCreated (nullable: true)
  }

    /**
     * Use {@link de.laser.ControlledListService#getOrderNumbers(java.util.Map)} instead
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
