package de.laser.finance

import de.laser.IssueEntitlement
import de.laser.RefdataValue
import de.laser.TitleInstancePackagePlatform
import de.laser.base.AbstractBase
import de.laser.helper.RDConstants
import de.laser.annotations.RefdataAnnotation

import javax.persistence.Transient

/**
 * This object represents the list price / negotiated price for a title instance.
 * It may be global (i.e. package-wide, subscription-independent), then it is linked to a {@link TitleInstancePackagePlatform} or local (i.e. linked to a subscription), then, the {@link IssueEntitlement} is the owner of the item.
 * It thus cannot belong simultaneously to a {@link TitleInstancePackagePlatform} and to an {@link IssueEntitlement}
 */
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

    //the two entities exclude each other
    static belongsTo = [tipp: TitleInstancePackagePlatform,
                        issueEntitlement: IssueEntitlement]

    static mapping = {
        id                  column: 'pi_id', index:'pi_idx'
        globalUID           column: 'pi_guid'
        listPrice           column: 'pi_list_price'
        listCurrency        column: 'pi_list_currency_rv_fk', index: 'pi_list_currency_idx'
        localPrice          column: 'pi_local_price'
        localCurrency       column: 'pi_local_currency_rv_fk', index: 'pi_local_currency_idx'
        startDate           column: 'pi_start_date'
        endDate             column: 'pi_end_date'
        tipp                column: 'pi_tipp_fk', index: 'pi_tipp_idx'
        issueEntitlement    column: 'pi_ie_fk', index: 'pi_ie_idx'
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

    /**
     * Locates in a parallel {@link Collection} an equivalent item. See {@link #equivalencyProperties} for the fields eligible for an equivalency
     * @param list the list to be comapred against
     * @return the equivalent price item of the other list to this one
     */
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
