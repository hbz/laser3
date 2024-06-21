package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.auth.User
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.convenience.Marker
import de.laser.interfaces.DeleteFlag
import de.laser.interfaces.MarkerSupport
import de.laser.properties.LicenseProperty
import de.laser.properties.OrgProperty
import de.laser.properties.PersonProperty
import de.laser.properties.PlatformProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.ProviderProperty
import de.laser.properties.SubscriptionProperty
import de.laser.properties.VendorProperty
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyResult
import de.laser.workflow.WfChecklist
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

    Org createdBy
    Org legallyObligedBy

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

    Date retirementDate
    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    SortedSet supportedLibrarySystems
    SortedSet electronicBillings
    SortedSet invoiceDispatchs
    SortedSet electronicDeliveryDelays

    static mappedBy = [
            addresses: 'vendor',
            packages: 'vendor',
            altnames: 'vendor',
            propertySet: 'owner',
            links: 'vendor',
            prsLinks: 'vendor',
            ids: 'vendor',
            documents: 'vendor',
            ingoingCombos: 'to',
            outgoingCombos: 'from',
            supportedLibrarySystems: 'vendor',
            electronicBillings: 'vendor',
            invoiceDispatchs: 'vendor',
            electronicDeliveryDelays: 'vendor'
    ]

    static hasMany = [
            addresses: Address,
            packages: PackageVendor,
            altnames: AlternativeName,
            propertySet: VendorProperty,
            links: VendorRole,
            prsLinks: PersonRole,
            ids: Identifier,
            documents: DocContext,
            ingoingCombos: VendorLink,
            outgoingCombos: VendorLink,
            supportedLibrarySystems: LibrarySystem,
            electronicBillings: ElectronicBilling,
            invoiceDispatchs: InvoiceDispatch,
            electronicDeliveryDelays: ElectronicDeliveryDelayNotification
    ]

    static mapping = {
        id column: 'ven_id'
        version column: 'ven_version'

        gokbId column: 'ven_gokb_id', index: 'ven_gokb_idx'
        globalUID column: 'ven_guid', index: 'ven_guid_idx'
        name column: 'ven_name'
        sortname column: 'ven_sortname'
        status column: 'ven_status_rv_fk'
        createdBy column:'ven_created_by_fk'
        legallyObligedBy column: 'ven_legally_obliged_by_fk'
        retirementDate column: 'ven_retirement_date'
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
        retirementDate              (nullable: true)
        researchPlatformForEbooks   (nullable: true)
        prequalificationVOLInfo     (nullable: true)
        lastUpdatedCascading        (nullable: true)
        createdBy                   (nullable: true)
        legallyObligedBy            (nullable: true)
    }

    @Transient
    int getProvidersCount(){
        Provider.executeQuery("select count(distinct pkg.provider) from Package pkg join pkg.vendors pv where pv.vendor = :vendor", [vendor: this])[0]
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
        return RDStore.VENDOR_STATUS_REMOVED.id == status.id
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
            result = name <=> v.name
        if(!result)
            result = id <=> v.id
        result
    }

    /**
     * Is the toString() implementation; returns the name of this organisation
     * @return the name of this organisation
     */
    @Override
    String toString() {
        name
    }

    static Vendor convertFromAgency(Org agency) {
        Vendor v = null
        if(agency.gokbId) {
            v = Vendor.findByGokbId(agency.gokbId)
            if(v) {
                v.globalUID = agency.globalUID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase())
                v.legallyObligedBy = null
            }
        }
        if(!v)
            v = Vendor.findByGlobalUID(agency.globalUID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase()))
        if(!v)
            v = new Vendor(globalUID: agency.globalUID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase()))
        v.name = agency.name
        v.sortname = agency.sortname
        v.gokbId = agency.gokbId //for the case vendors have already recorded as orgs by sync
        if(!agency.gokbId && agency.createdBy)
            v.createdBy = agency.createdBy
        if(!agency.gokbId && agency.legallyObligedBy)
            v.legallyObligedBy = agency.legallyObligedBy
        v.homepage = agency.url
        switch(agency.status) {
            case RDStore.ORG_STATUS_CURRENT: v.status = RDStore.VENDOR_STATUS_CURRENT
                break
            case RDStore.ORG_STATUS_DELETED: v.status = RDStore.VENDOR_STATUS_DELETED
                break
            case RDStore.ORG_STATUS_RETIRED: v.status = RDStore.VENDOR_STATUS_RETIRED
                break
            case RDStore.ORG_STATUS_REMOVED: v.status = RDStore.VENDOR_STATUS_REMOVED
                break
            default: v.status = RDStore.VENDOR_STATUS_CURRENT
                break
        }
        v.dateCreated = agency.dateCreated
        if(!v.save()) {
            log.error(v.getErrors().getAllErrors().toListString())
            null
        }
        agency.altnames.each { AlternativeName altName ->
            if(!v.altnames?.find { AlternativeName altnameNew -> altnameNew.name == altName.name }) {
                altName.vendor = v
                altName.org = null
                altName.save()
            }
        }
        agency.addresses.each { Address a ->
            a.vendor = v
            a.org = null
            a.save()
        }
        //log.debug("${DocContext.executeUpdate('update DocContext dc set dc.vendor = :vendor, dc.targetOrg = null, dc.org = null where dc.targetOrg = :agency or dc.org = :agency', [vendor: v, agency: agency])} documents updated")
        Identifier.findAllByOrg(agency).each { Identifier id ->
            if(!Identifier.findByVendorAndValue(v, id.value)) {
                id.vendor = v
                id.org = null
                id.save()
            }
        }
        /*
        agency.prsLinks.each { PersonRole pr ->
            pr.vendor = v
            pr.org = null
            pr.save()
        }
        */
        Person.findAllByTenant(agency).each { Person pe ->
            pe.delete()
        }
        Marker.findAllByOrg(agency).each { Marker m ->
            m.ven = v
            m.org = null
            m.save()
        }
        Task.findAllByOrg(agency).each { Task t ->
            t.vendor = v
            t.org = null
            t.save()
        }
        log.debug("${WfChecklist.executeUpdate('update WfChecklist wf set wf.vendor = :vendor, wf.org = null where wf.org = :agency', [vendor: v, agency: agency])} workflow checkpoints updated")
        //those property definitions should not exist actually ...
        LicenseProperty.executeUpdate('delete from LicenseProperty lp where lp.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        OrgProperty.executeUpdate('delete from OrgProperty op where op.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        ProviderProperty.executeUpdate('delete from ProviderProperty pp where pp.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        VendorProperty.executeUpdate('delete from VendorProperty vp where vp.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        SubscriptionProperty.executeUpdate('delete from SubscriptionProperty sp where sp.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        SurveyResult.executeUpdate('delete from SurveyResult sr where sr.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        PropertyDefinition.executeUpdate('delete from PropertyDefinition pd where pd.tenant = :agency', [agency: agency])
        OrgProperty.findAllByOwner(agency).each { OrgProperty op ->
            PropertyDefinition type = PropertyDefinition.findByNameAndDescrAndTenant(op.type.name, PropertyDefinition.VEN_PROP, op.type.tenant)
            if(!VendorProperty.findByOwnerAndTypeAndTenant(v, type, op.tenant)) {
                VendorProperty vp = new VendorProperty(owner: v, type: type)
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
        }
        v
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
