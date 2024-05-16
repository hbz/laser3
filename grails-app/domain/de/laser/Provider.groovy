package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.auth.User
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.convenience.Marker
import de.laser.interfaces.DeleteFlag
import de.laser.interfaces.MarkerSupport
import de.laser.properties.OrgProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.ProviderProperty
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyInfo
import de.laser.workflow.WfChecklist

class Provider extends AbstractBaseWithCalculatedLastUpdated implements DeleteFlag, MarkerSupport, Comparable<Provider> {

    String name
    String sortname //maps to abbreviatedName
    String gokbId

    String kbartDownloaderURL
    String metadataDownloaderURL
    String homepage
    boolean paperInvoice = false
    boolean managementOfCredits = false
    boolean processingOfCompensationPayments = false
    boolean individualInvoiceDesign = false

    SortedSet altnames
    SortedSet electronicBillings
    SortedSet invoiceDispatchs
    SortedSet invoicingVendors

    @RefdataInfo(cat = RDConstants.PROVIDER_STATUS)
    RefdataValue status

    Date retirementDate
    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static transients = ['deleted']

    static hasMany = [
            contacts: Contact,
            addresses: Address,
            propertySet: ProviderProperty,
            altnames: AlternativeName,
            documents: DocContext,
            ids: Identifier,
            platforms: Platform,
            packages: Package,
            electronicBillings: ElectronicBilling,
            invoiceDispatchs: InvoiceDispatch,
            invoicingVendors: InvoicingVendor
    ]

    static mappedBy = [
            contacts: 'provider',
            addresses: 'provider',
            propertySet: 'owner',
            altnames: 'provider',
            documents: 'provider',
            ids: 'provider',
            platforms: 'provider',
            packages: 'provider',
            invoicingVendors: 'provider',
            electronicBillings: 'provider',
            invoiceDispatchs: 'provider'
    ]

    static mapping = {
        id column: 'prov_id'
        version column: 'prov_version'
        name column: 'prov_name', index: 'prov_name_idx'
        sortname column: 'prov_sortname', index: 'prov_sortname_idx'
        gokbId column: 'prov_gokb_id', type: 'text', index: 'prov_gokb_idx'
        globalUID column: 'prov_guid', index: 'prov_guid_idx'
        status column: 'prov_status_rv_fk'
        kbartDownloaderURL column: 'prov_kbart_downloader_url', type: 'text'
        metadataDownloaderURL column: 'prov_metadata_downloader_url', type: 'text'
        homepage column: 'prov_homepage'
        paperInvoice column: 'prov_paper_invoice'
        managementOfCredits column: 'prov_management_of_credits'
        processingOfCompensationPayments column: 'prov_processing_of_compensation_payments'
        individualInvoiceDesign column: 'prov_individual_invoice_design'
        retirementDate column: 'prov_retirement_date'
        dateCreated column: 'prov_date_created'
        lastUpdated column: 'prov_last_updated'
        lastUpdatedCascading column: 'prov_last_updated_cascading'
        platforms sort:'name', order:'asc', batchSize: 10
        packages sort:'name', order:'asc', batchSize: 10
    }

    static constraints = {
        gokbId                      (unique: true, nullable: true)
        globalUID                   (unique: true)
        sortname                    (nullable: true)
        kbartDownloaderURL          (nullable: true)
        metadataDownloaderURL       (nullable: true)
        homepage                    (nullable: true, maxSize: 512)
        retirementDate              (nullable: true)
        lastUpdatedCascading        (nullable: true)
    }

    @Override
    def afterInsert() {
        super.beforeInsertHandler()
    }

    @Override
    def afterUpdate() {
        super.beforeInsertHandler()
    }

    @Override
    def afterDelete() {
        super.beforeInsertHandler()

        //TODO implement ESIndex population
        //BeanStore.getDeletionService().deleteDocumentFromIndex(this.globalUID, this.class.simpleName)
    }

    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }

    @Override
    def beforeUpdate() {
        super.beforeInsertHandler()
    }

    @Override
    def beforeDelete() {
        super.beforeInsertHandler()
    }

    @Override
    boolean isDeleted() {
        return RDStore.PROVIDER_STATUS_REMOVED.id == status.id
    }

    @Override
    void setMarker(User user, Marker.TYPE type) {
        if (!isMarked(user, type)) {
            Marker m = new Marker(prov: this, user: user, type: type)
            m.save()
        }
    }

    @Override
    void removeMarker(User user, Marker.TYPE type) {
        withTransaction {
            Marker.findByProvAndUserAndType(this, user, type).delete(flush:true)
        }
    }

    @Override
    boolean isMarked(User user, Marker.TYPE type) {
        Marker.findByProvAndUserAndType(this, user, type) ? true : false
    }

    @Override
    int compareTo(Provider p) {
        int result = sortname <=> p.sortname
        if(!result)
            result = name <=> p.name
        if(!result)
            result = id <=> p.id
        result
    }

    static Provider convertFromOrg(Org provider) {
        Provider p = null
        if(provider.gokbId) {
            p = Provider.findByGokbId(provider.gokbId)
            if(p)
                p.globalUID = provider.globalUID.replace(Org.class.simpleName.toLowerCase(), Provider.class.simpleName.toLowerCase())
        }
        if(!p)
            p = Provider.findByGlobalUID(provider.globalUID.replace(Org.class.simpleName.toLowerCase(), Provider.class.simpleName.toLowerCase()))
        if(!p)
            p = new Provider(globalUID: provider.globalUID.replace(Org.class.simpleName.toLowerCase(), Provider.class.simpleName.toLowerCase()))
        p.name = provider.name
        p.sortname = provider.sortname
        p.gokbId = provider.gokbId //for the case providers have already recorded as orgs by sync
        p.homepage = provider.url
        switch(provider.status) {
            case RDStore.ORG_STATUS_CURRENT: p.status = RDStore.PROVIDER_STATUS_CURRENT
                break
            case RDStore.ORG_STATUS_DELETED: p.status = RDStore.PROVIDER_STATUS_DELETED
                break
            case RDStore.ORG_STATUS_RETIRED: p.status = RDStore.PROVIDER_STATUS_RETIRED
                break
            case RDStore.ORG_STATUS_REMOVED: p.status = RDStore.PROVIDER_STATUS_REMOVED
                break
            default: p.status = RDStore.PROVIDER_STATUS_CURRENT
                break
        }
        p.retirementDate = provider.retirementDate
        p.dateCreated = provider.dateCreated
        if(!p.save()) {
            log.error(p.getErrors().getAllErrors().toListString())
            null
        }
        provider.altnames.each { AlternativeName altName ->
            if(!p.altnames?.find { AlternativeName altnameNew -> altnameNew.name == altName.name }) {
                altName.provider = p
                altName.org = null
                altName.save()
            }
        }
        provider.contacts.each { Contact c ->
            if(!p.contacts?.find { Contact cOld -> cOld.content == c.content }) {
                c.provider = p
                c.org = null
                c.save()
            }
        }
        provider.addresses.each { Address a ->
            a.provider = p
            a.org = null
            a.save()
        }
        Identifier.findAllByOrg(provider).each { Identifier id ->
            id.provider = p
            id.org = null
            id.save()
        }
        Platform.findAllByOrg(provider).each { Platform pl ->
            pl.provider = p
            pl.save()
        }
        //log.debug("${DocContext.executeUpdate('update DocContext dc set dc.provider = :providerNew, dc.targetOrg = null, dc.org = null where dc.targetOrg = :provider or dc.org = :provider', [providerNew: p, provider: provider])} documents updated")
        /*
        provider.prsLinks.each { PersonRole pr ->
            pr.provider = p
            pr.org = null
            pr.save()
        }
        */
        Person.findAllByTenant(provider).each { Person pe ->
            if(!Person.executeQuery('select p from Person p join p.roleLinks pr where p.tenant = null and p.isPublic = true and p.last_name = :contactType and :provider in (pr.provider)', [provider: p, contactType: pe.last_name])) {
                pe.tenant = null
                pe.save()
            }
            else pe.delete()
        }
        Marker.findAllByOrg(provider).each { Marker m ->
            m.prov = p
            m.org = null
            m.save()
        }
        SurveyInfo.findAllByProviderOld(provider).each { SurveyInfo surin ->
            surin.provider = p
            surin.providerOld = null
            surin.save()
        }
        Task.findAllByOrg(provider).each { Task t ->
            t.provider = p
            t.org = null
            t.save()
        }
        log.debug("${WfChecklist.executeUpdate('update WfChecklist wf set wf.provider = :providerNew, wf.org = null where wf.org = :provider', [providerNew: p, provider: provider])} workflow checkpoints updated")
        //those property definitions should not exist actually ...
        PropertyDefinition.executeUpdate('delete from PropertyDefinition pd where pd.tenant = :provider', [provider: provider])
        OrgProperty.findAllByOwner(provider).each { OrgProperty op ->
            ProviderProperty pp = new ProviderProperty()
            if(op.dateValue)
                pp.dateValue = op.dateValue
            if(op.decValue)
                pp.decValue = op.decValue
            if(op.intValue)
                pp.intValue = op.intValue
            if(op.refValue)
                pp.refValue = op.refValue
            if(op.stringValue)
                pp.stringValue = op.stringValue
            if(op.urlValue)
                pp.urlValue = op.urlValue
            pp.note = op.note
            pp.tenant = op.tenant
            pp.dateCreated = op.dateCreated
            pp.lastUpdated = op.lastUpdated
            pp.save()
        }
        p
    }

    boolean hasElectronicBilling(String ebB) {
        if(!electronicBillings)
            false
        else {
            electronicBillings.collect { ElectronicBilling ebA -> ebA.invoicingFormat.value }.contains(ebB)
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
