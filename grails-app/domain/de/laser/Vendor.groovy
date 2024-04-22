package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.auth.User
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.convenience.Marker
import de.laser.interfaces.DeleteFlag
import de.laser.interfaces.MarkerSupport
import de.laser.properties.OrgProperty
import de.laser.properties.VendorProperty
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

    String homepage
    boolean webShopOrders = false
    boolean xmlOrders = false
    boolean ediOrders = false
    boolean paperInvoice = false
    boolean managementOfCredits = false
    boolean processingOfCompensationPayments = false
    boolean individualInvoiceDesign = false
    boolean technicalSupport = false
    boolean shippingMetadata = false
    boolean forwardingUsageStatisticsFromPublisher = false
    boolean activationForNewReleases = false
    boolean exchangeOfIndividualTitles = false
    String researchPlatformForEbooks
    boolean prequalificationVOL = false
    String prequalificationVOLInfo

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    SortedSet supportedLibrarySystems
    SortedSet electronicBillings
    SortedSet invoiceDispatchs
    SortedSet electronicDeliveryDelays

    static mappedBy = [
            contacts: 'vendor',
            addresses: 'vendor',
            packages: 'vendor',
            propertySet: 'owner',
            supportedLibrarySystems: 'vendor',
            electronicBillings: 'vendor',
            invoiceDispatchs: 'vendor',
            electronicDeliveryDelays: 'vendor',
    ]

    static hasMany = [
            contacts: Contact,
            addresses: Address,
            propertySet: VendorProperty,
            packages: PackageVendor,
            supportedLibrarySystems: LibrarySystem,
            electronicBillings: ElectronicBilling,
            invoiceDispatchs: InvoiceDispatch,
            electronicDeliveryDelays: ElectronicDeliveryDelayNotification
    ]

    static mapping = {
        id column: 'ven_id'
        version column: 'ven_version'

        gokbId column: 'ven_gokb_id', index: 'ven_gokb_idx'
        globalUID column: 'ven_guid'
        name column: 'ven_name'
        sortname column: 'ven_sortname'
        status column: 'ven_status_rv_fk'
        homepage column: 'ven_homepage'
        webShopOrders column: 'ven_web_shop_orders'
        xmlOrders column: 'ven_xml_orders'
        ediOrders column: 'vel_edi_orders'
        paperInvoice column: 'ven_paper_invoice'
        managementOfCredits column: 'ven_management_of_credits'
        processingOfCompensationPayments column: 'ven_processing_of_compensation_payments'
        individualInvoiceDesign column: 'ven_individual_invoice_design'
        technicalSupport column: 'ven_technical_support'
        shippingMetadata column: 'ven_shipping_metadata'
        forwardingUsageStatisticsFromPublisher column: 'ven_forwarding_usage_statistics_from_publisher'
        activationForNewReleases column: 'ven_activation_new_releases'
        exchangeOfIndividualTitles column: 'ven_exchange_individual_titles'
        researchPlatformForEbooks column: 'ven_research_platform_ebooks', type: 'text'
        prequalificationVOL column: 'ven_prequalification_vol'
        prequalificationVOLInfo column: 'ven_prequalification_vol_info', type: 'text'
        dateCreated column: 'ven_date_created'
        lastUpdated column: 'ven_last_updated'
        lastUpdatedCascading column: 'ven_last_updated_cascading'
    }

    static constraints = {
        gokbId                      (unique: true, nullable: true)
        globalUID                   (unique: true)
        sortname                    (nullable: true)
        homepage                    (nullable: true, maxSize: 512)
        researchPlatformForEbooks   (nullable: true)
        prequalificationVOLInfo     (nullable: true)
        lastUpdatedCascading        (nullable: true)
    }

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

    static Vendor convertFromAgency(Org agency) {
        Vendor v = null
        if(agency.gokbId)
            v = Vendor.findByGokbId(agency.gokbId)
        if(!v)
            v = new Vendor(globalUID: agency.globalUID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase()))
        v.name = agency.name
        v.sortname = agency.sortname
        v.gokbId = agency.gokbId //for the case vendors have already recorded as orgs by sync
        v.homepage = agency.url
        if(agency.status == RDStore.ORG_STATUS_CURRENT)
            v.status = RDStore.VENDOR_STATUS_CURRENT
        if(agency.status == RDStore.ORG_STATUS_DELETED)
            v.status = RDStore.VENDOR_STATUS_DELETED
        if(agency.status == RDStore.ORG_STATUS_RETIRED)
            v.status = RDStore.VENDOR_STATUS_RETIRED
        if(agency.status == RDStore.ORG_STATUS_REMOVED)
            v.status = RDStore.VENDOR_STATUS_REMOVED
        v.dateCreated = agency.dateCreated
        agency.contacts.each { Contact c ->
            c.vendor = v
            c.org = null
            c.save()
        }
        agency.addresses.each { Address a ->
            a.vendor = v
            a.org = null
            a.save()
        }
        PersonRole.findAllByOrg(agency).each { PersonRole pr ->
            pr.vendor = v
            pr.org = null
            pr.save()
        }
        OrgProperty.findAllByOwner(agency).each { OrgProperty op ->
            VendorProperty vp = new VendorProperty()
            if(op.dateValue)
                vp.dateValue = op.dateValue
            if(op.decValue)
                vp.decValue = op.decValue
            if(op.intValue)
                vp.intValue = op.intValue
            if(op.refValue)
                vp.refValue = op.refValue
            if(op.stringValue)
                vp.stringValue = op.stringValue
            if(op.urlValue)
                vp.urlValue = op.urlValue
            vp.note = op.note
            vp.tenant = op.tenant
            vp.dateCreated = op.dateCreated
            vp.lastUpdated = op.lastUpdated
            vp.save()
        }
        v.save()
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

    boolean isLibrarySystemSupported(String lsB) {
        if(!supportedLibrarySystems)
            false
        else {
            supportedLibrarySystems.collect { LibrarySystem lsA -> lsA.librarySystem.value }.contains(lsB)
        }
    }

    boolean hasElectronicBilling(String ebB) {
        if(!electronicBillings)
            false
        else {
            electronicBillings.collect { ElectronicBilling ebA -> ebA.invoicingFormat.value }.contains(ebB)
        }
    }

    boolean hasElectronicDeliveryDelayNotification(String eddnB) {
        if(!electronicDeliveryDelays)
            false
        else {
            electronicDeliveryDelays.collect { ElectronicDeliveryDelayNotification eddnA -> eddnA.delayNotification.value }.contains(eddnB)
        }
    }

    boolean hasInvoiceDispatch(String idiB) {
        if(!invoiceDispatchs)
            false
        else {
            invoiceDispatchs.collect { InvoiceDispatch idiA -> idiA.invoiceDispatch.value }.contains(idiB)
        }
    }
}
