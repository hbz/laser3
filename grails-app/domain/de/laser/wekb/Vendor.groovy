package de.laser.wekb

import de.laser.addressbook.Address
import de.laser.AlternativeName
import de.laser.DocContext
import de.laser.Identifier
import de.laser.Org
import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.RefdataValue
import de.laser.Task
import de.laser.annotations.RefdataInfo
import de.laser.auth.User
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.convenience.Marker
import de.laser.interfaces.DeleteFlag
import de.laser.interfaces.MarkerSupport
import de.laser.properties.OrgProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.VendorProperty
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfigVendor
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
    boolean prequalification = false
    String prequalificationInfo

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
            electronicDeliveryDelays: 'vendor',
            surveys: 'vendor'
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
            electronicDeliveryDelays: ElectronicDeliveryDelayNotification,
            surveys: SurveyConfigVendor
    ]

    static mapping = {
        id column: 'ven_id'
        version column: 'ven_version'

        gokbId column: 'ven_gokb_id', index: 'ven_gokb_idx'
        laserID column: 'ven_guid', index: 'ven_guid_idx'
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
        prequalification column: 'ven_prequalification'
        prequalificationInfo column: 'ven_prequalification_info', type: 'text'
        dateCreated column: 'ven_date_created'
        lastUpdated column: 'ven_last_updated'
        lastUpdatedCascading column: 'ven_last_updated_cascading'
    }

    static constraints = {
        gokbId                      (unique: true, nullable: true)
        laserID                     (unique: true)
        sortname                    (nullable: true)
        homepage                    (nullable: true, maxSize: 512)
        retirementDate              (nullable: true)
        researchPlatformForEbooks   (nullable: true)
        prequalificationInfo        (nullable: true)
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
        //BeanStore.getDeletionService().deleteDocumentFromIndex(this.laserID, this.class.simpleName)
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
        int result = name <=> v.name
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

    /**
     * Gets the property definition groups defined by the given institution for the organisation to be viewed
     * @param contextOrg the institution whose property definition groups should be loaded
     * @return a {@link Map} of property definition groups, ordered by sorted, global, local and orphaned property definitions
     * @see de.laser.properties.PropertyDefinition
     * @see de.laser.properties.PropertyDefinitionGroup
     */
    Map<String, Object> getCalculatedPropDefGroups(Org contextOrg) {
        BeanStore.getPropertyService().getCalculatedPropDefGroups(this, contextOrg)
    }

    /**
     * Gets all public contact persons of this vendor
     * @return a {@link List} of public {@link Person}s
     */
    List<Person> getPublicPersons() {
        Person.executeQuery(
                "select distinct p from Person as p inner join p.roleLinks pr where p.isPublic = true and pr.vendor = :vendor",
                [vendor: this]
        )
    }

    static Vendor convertFromAgency(Org agency) {
        Vendor v = null
        if(agency.gokbId) {
            v = Vendor.findByGokbId(agency.gokbId)
            if(v) {
                v.laserID = agency.laserID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase())
                v.legallyObligedBy = null
            }
        }
        if(!v)
            v = Vendor.findByLaserID(agency.laserID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase()))
        if(!v)
            v = new Vendor(laserID: agency.laserID.replace(Org.class.simpleName.toLowerCase(), Vendor.class.simpleName.toLowerCase()))
        v.name = agency.name
        v.sortname = agency.sortname
        v.gokbId = agency.gokbId //for the case vendors have already recorded as orgs by sync
        if(!agency.gokbId && agency.createdBy)
            v.createdBy = agency.createdBy
        if(!agency.gokbId && agency.legallyObligedBy)
            v.legallyObligedBy = agency.legallyObligedBy
        v.homepage = agency.url
        switch(agency.status) { // ERMS-6224 - removed org.status
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
        /*
        ProviderProperty.executeUpdate('delete from ProviderProperty pp where pp.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        VendorProperty.executeUpdate('delete from VendorProperty vp where vp.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        SubscriptionProperty.executeUpdate('delete from SubscriptionProperty sp where sp.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        LicenseProperty.executeUpdate('delete from LicenseProperty lp where lp.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        OrgProperty.executeUpdate('delete from OrgProperty op where op.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        SurveyResult.executeUpdate('delete from SurveyResult sr where sr.type in (select pd from PropertyDefinition pd where pd.tenant = :agency)', [agency: agency])
        */
        PropertyDefinition.executeUpdate('delete from PropertyDefinition pd where pd.tenant = :agency', [agency: agency])
        OrgProperty.findAllByOwner(agency).each { OrgProperty op ->
            PropertyDefinition type = PropertyDefinition.findByNameAndDescrAndTenant(op.type.name, PropertyDefinition.VEN_PROP, op.type.tenant)
            String valueFilter = ''
            Map<String, Object> propParams = [owner: v, type: type, tenant: op.tenant]
            if(op.dateValue) {
                propParams.value = op.dateValue
                valueFilter = 'and vp.dateValue = :value'
            }
            if(op.decValue) {
                propParams.value = op.decValue
                valueFilter = 'and vp.decValue = :value'
            }
            if(op.longValue) {
                propParams.value = op.longValue
                valueFilter = 'and vp.longValue = :value'
            }
            if(op.refValue) {
                propParams.value = op.refValue
                valueFilter = 'and vp.refValue = :value'
            }
            if(op.stringValue) {
                propParams.value = op.stringValue
                valueFilter = 'and vp.stringValue = :value'
            }
            if(op.urlValue) {
                propParams.value = op.urlValue
                valueFilter = 'and vp.urlValue = :value'
            }
            if(!VendorProperty.executeQuery('select vp from VendorProperty vp where vp.owner = :owner and vp.type = :type and vp.tenant = :tenant '+valueFilter, propParams)) {
                VendorProperty vp = new VendorProperty(owner: v, type: type)
                if(op.dateValue)
                    vp.dateValue = op.dateValue
                if(op.decValue)
                    vp.decValue = op.decValue
                if(op.longValue)
                    vp.longValue = op.longValue
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
     * Displays this vendor's name according to the dropdown naming convention as specified <a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">here</a>
     * @return this vendor's name according to the dropdown naming convention
     */
    String dropdownNamingConvention(){
        String result = ''
        if (BeanStore.getContextService().getOrg().isCustomerType_Inst()){
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
