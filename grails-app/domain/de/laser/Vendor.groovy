package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.auth.User
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.convenience.Marker
import de.laser.interfaces.DeleteFlag
import de.laser.interfaces.MarkerSupport
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import groovy.util.logging.Slf4j

import javax.persistence.Transient

@Slf4j
class Vendor extends AbstractBaseWithCalculatedLastUpdated
        implements DeleteFlag, MarkerSupport, Comparable<Vendor> {

    String name
    String sortname //maps abbreviatedName
    String gokbId //maps uuid

    @RefdataInfo(cat = RDConstants.VENDOR_STATUS)
    RefdataValue status

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static mappedBy = [
            contacts: 'vendor',
            addresses: 'vendor'
    ]

    static hasMany = [
            contacts: Contact,
            addresses: Address,
            packages: PackageVendor
    ]

    static mapping = {
        id column: 'ven_id'
        version column: 'ven_version'

        gokbId column: 'ven_gokb_id', index: 'ven_gokb_idx'
        globalUID column: 'ven_guid'
        name column: 'ven_name'
        sortname column: 'ven_sortname'

        dateCreated column: 'ven_date_created'
        lastUpdated column: 'ven_last_updated'
        lastUpdatedCascading column: 'ven_last_updated_cascading'

        status column: 'ven_status_rv_fk'
    }

    static constraints = {
        gokbId                   (unique: true)
        globalUID                (unique: true)
        sortname                 (nullable: true)
        lastUpdatedCascading     (nullable: true)
    }

    static final Set<String> WEKB_PROPERTIES = ['homepage',
                                                'webShopOrders',
                                                'xmlOrders',
                                                'ediOrders',
                                                'paperInvoice',
                                                'managementOfCredits',
                                                'processingOfCompensationPayments',
                                                'individualInvoiceDesign',
                                                'technicalSupport',
                                                'shippingMetadata',
                                                'forwardingUsageStatisticsFromPublisher',
                                                'activationForNewReleases',
                                                'exchangeOfIndividualTitles',
                                                'researchPlatformForEbooks',
                                                'prequalificationVOL',
                                                'prequalificationVOLInfo',
                                                'supportedLibrarySystems',
                                                'electronicBillings',
                                                'invoiceDispatchs',
                                                'electronicDeliveryDelays']

    @Transient
    int getProvidersCount(){
        Org.executeQuery("select count(*) from Org o where o in (select oo.org from OrgRole oo join oo.pkg as pkg join pkg.vendors pv where pv.vendor = :vendor)", [vendor: this])[0]
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

    @Override
    def afterInsert() {
        super.afterInsertHandler()
    }

    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }

    @Override
    def afterDelete() {
        super.afterDeleteHandler()

        //TODO implement ESIndex population
        //BeanStore.getDeletionService().deleteDocumentFromIndex(this.globalUID, this.class.simpleName)
    }

    @Override
    boolean isDeleted() {
        return RDStore.ORG_STATUS_REMOVED.id == status.id
    }

    @Override
    void setMarker(User user, Marker.TYPE type) {
        if (!isMarked(user, type)) {
            Marker m = new Marker(ven: this, user: user, type: type)
            m.save()
        }
    }

    @Override
    void removeMarker(User user, Marker.TYPE type) {
        withTransaction {
            Marker.findByVenAndUserAndType(this, user, type).delete(flush:true)
        }
    }

    @Override
    boolean isMarked(User user, Marker.TYPE type) {
        Marker.findByVenAndUserAndType(this, user, type) ? true : false
    }

    @Override
    int compareTo(Vendor v) {
        int result = sortname <=> v.sortname
        if(!result)
            name <=> v.name
        if(!result)
            id <=> v.id
        result
    }

    /**
     * Substitution caller for {@link #dropdownNamingConvention(de.laser.Org)}; substitutes with the context institution
     * @return this organisation's name according to the dropdown naming convention (<a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">see here</a>)
     */
    String dropdownNamingConvention() {
        return dropdownNamingConvention(BeanStore.getContextService().getOrg())
    }

    /**
     * Displays this vendor's name according to the dropdown naming convention as specified <a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">here</a>
     * @param contextOrg the institution whose perspective should be taken
     * @return this vendor's name according to the dropdown naming convention
     */
    String dropdownNamingConvention(Org contextOrg){
        String result = ''
        if (contextOrg.isCustomerType_Inst()){
            if (name) {
                result += name
            }
        } else {
            if (sortname) {
                result += sortname
            }
            if (name) {
                result += ' (' + name + ')'
            }
        }
        result
    }
}
