package de.laser.finance

import de.laser.IssueEntitlement
import de.laser.RefdataValue
import de.laser.TitleInstancePackagePlatform
import de.laser.base.AbstractBase
import de.laser.helper.RDConstants
import de.laser.annotations.RefdataAnnotation

import javax.persistence.Transient

class PriceItem extends AbstractBase {

    @RefdataAnnotation(cat = RDConstants.CURRENCY)
    RefdataValue listCurrency

    @RefdataAnnotation(cat = RDConstants.CURRENCY)
    RefdataValue localCurrency

    BigDecimal listPrice
    BigDecimal localPrice

    Date startDate
    Date endDate

    Date dateCreated
    Date lastUpdated

    @Transient
    static Set<String> equivalencyProperties = [
            'startDate',
            'endDate',
            'listCurrency'
    ]

    static belongsTo = [tipp: TitleInstancePackagePlatform,
                        issueEntitlement: IssueEntitlement]

    static mapping = {
        id                  column: 'pi_id'
        globalUID           column: 'pi_guid'
        listPrice           column: 'pi_list_price'
        listCurrency        column: 'pi_list_currency_rv_fk'
        localPrice          column: 'pi_local_price'
        localCurrency       column: 'pi_local_currency_rv_fk'
        startDate           column: 'pi_start_date'
        endDate             column: 'pi_end_date'
        tipp                column: 'pi_tipp_fk'
        issueEntitlement    column: 'pi_ie_fk'
        lastUpdated         column: 'pi_last_updated'
        dateCreated         column: 'pi_date_created'
    }

    static constraints = {
        globalUID           (blank: false, unique: true, maxSize: 255)
        tipp                (nullable: true)
        issueEntitlement    (nullable: true)
        listPrice           (nullable: true)
        listCurrency        (nullable: true)
        localPrice          (nullable: true)
        localCurrency       (nullable: true)
        startDate           (nullable: true)
        endDate             (nullable: true)
        lastUpdated         (nullable: true)
        dateCreated         (nullable: true)
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

    PriceItem findEquivalent(Collection<PriceItem> list) {
        PriceItem equivalent
        for (String k : equivalencyProperties) {
            equivalent = list.find { PriceItem pi -> pi[k] == this[k] }
            if (equivalent) {
                println "Price item ${equivalent.id} considered as equivalent to ${this.id}"
                break
            }
        }
        equivalent
    }
}
