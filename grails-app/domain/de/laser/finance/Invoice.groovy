package de.laser.finance

import com.k_int.kbplus.GenericOIDService
import de.laser.Org
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

import javax.persistence.Transient

/**
 * Represents an invoice item which may be linked to {@link CostItem}s.
 */
class Invoice {

  Date      dateOfInvoice
  Date      dateOfPayment
  Date      datePassedToFinance
  Date      startDate
  Date      endDate
  String    invoiceNumber
  Org       owner
  String    description

  Date dateCreated
  Date lastUpdated

  static mapping = {
                      id column:'inv_id'
                 version column:'inv_version'
           dateOfInvoice column:'inv_date_of_invoice'
           dateOfPayment column:'inv_date_of_payment'
     datePassedToFinance column:'inv_date_passed_to_finance'
           invoiceNumber column:'inv_number'
               startDate column:'inv_start_date'
                 endDate column:'inv_end_date'
                   owner column:'inv_owner'
         description column:'inv_description', type:'text'

      dateCreated column: 'inv_date_created'
      lastUpdated column: 'inv_last_updated'
  }

  static constraints = {
          dateOfInvoice(nullable:true)
          dateOfPayment(nullable:true)
    datePassedToFinance(nullable:true)
          invoiceNumber(blank:false)
              startDate (nullable:true)
                endDate (nullable:true)
                  owner(blank:false)
            description(nullable: true, blank: false)

      // Nullable is true, because values are already in the database
      lastUpdated (nullable: true)
      dateCreated (nullable: true)
  }

    /**
     * Use {@link de.laser.ControlledListService#getInvoiceNumbers(java.util.Map)} instead
     */
    @Deprecated
    @Transient
    static def refdataFind(GrailsParameterMap params) {
        Org owner = Org.findByShortcode(params.shortcode)
        if (! owner) {
            return []
        }
        GenericOIDService genericOIDService = (GenericOIDService) Holders.grailsApplication.mainContext.getBean('genericOIDService')

        genericOIDService.getOIDMapList(
                Invoice.findAllByOwnerAndInvoiceNumberIlike(owner, "%${params.q}%", params),
                'invoiceNumber'
        )
    }
}
