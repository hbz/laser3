package de.laser.api.v0

import com.k_int.kbplus.*
import de.laser.api.v0.entities.ApiDoc
import de.laser.api.v0.entities.ApiIssueEntitlement
import de.laser.helper.Constants
import groovy.util.logging.Log4j

@Log4j
class ApiCollectionReader {

    // ################### FULL OBJECTS ###################

    static Collection<Object> retrieveAddressCollection(Collection<Address> list, allowedTypes) {
        def result = []

        list?.each { it ->   // com.k_int.kbplus.Address
            def tmp             = [:]
            tmp.street1         = it.street_1
            tmp.street2         = it.street_2
            tmp.pob             = it.pob
            tmp.pobZipcode      = it.pobZipcode
            tmp.pobCity         = it.pobCity
            tmp.zipcode         = it.zipcode
            tmp.city            = it.city
            tmp.name            = it.name
            tmp.additionFirst   = it.additionFirst
            tmp.additionSecond  = it.additionSecond
            tmp.lastUpdated     = it.lastUpdated

            // RefdataValues
            tmp.state       = it.state?.value
            tmp.country     = it.country?.value
            tmp.type        = it.type?.value

            tmp = ApiToolkit.cleanUp(tmp, true, false)

            if(ApiReader.NO_CONSTRAINT == allowedTypes || allowedTypes.contains(it.type?.value)) {
                result << tmp
            }
        }
        result
    }

    static Collection<Object> retrieveContactCollection(Collection<Contact> list, allowedTypes) {
        def result = []

        list?.each { it ->       // com.k_int.kbplus.Contact
            def tmp             = [:]
            tmp.content         = it.content
            tmp.lastUpdated     = it.lastUpdated

            // RefdataValues
            tmp.category        = it.contentType?.value
            tmp.type            = it.type?.value

            tmp = ApiToolkit.cleanUp(tmp, true, false)

            if(ApiReader.NO_CONSTRAINT == allowedTypes || allowedTypes.contains(it.type?.value)) {
                result << tmp
            }
        }
        result
    }

    // TODO: oaMonitor
    static Collection<Object> retrieveCostItemCollection(Collection<CostItem> list) {
        def result = []

        list?.each { it ->               // com.k_int.kbplus.CostItem

            // TODO: isVisibleForSubscriber
            // TODO: finalCostRounding
            // TODO: budgetcodes

            def tmp                     = [:]
            tmp.globalUID               = it.globalUID
            tmp.costInBillingCurrency   = it.costInBillingCurrency
            tmp.costInLocalCurrency     = it.costInLocalCurrency
            tmp.currencyRate            = it.currencyRate
            tmp.costTitle               = it.costTitle
            tmp.costDescription         = it.costDescription
            //tmp.includeInSubscription   = it.includeInSubscription
            tmp.reference               = it.reference

            tmp.costInLocalCurrencyAfterTax     = it.getCostInLocalCurrencyAfterTax()
            tmp.costInBillingCurrencyAfterTax   = it.getCostInBillingCurrencyAfterTax()

            tmp.calculatedType      = it.getCalculatedType()
            tmp.datePaid            = it.datePaid
            tmp.invoiceDate         = it.invoiceDate
            tmp.financialYear       = it.financialYear
            tmp.startDate           = it.startDate
            tmp.endDate             = it.endDate
            tmp.dateCreated         = it.dateCreated
            tmp.lastUpdated         = it.lastUpdated
            tmp.taxRate             = it.taxKey?.taxRate

            // RefdataValues
            tmp.billingCurrency     = it.billingCurrency?.value
            tmp.costItemCategory    = it.costItemCategory?.value
            tmp.costItemElement     = it.costItemElement?.value
            tmp.costItemElementConfiguration = it.costItemElementConfiguration?.value
            tmp.costItemStatus      = it.costItemStatus?.value
            tmp.taxCode             = it.taxKey?.taxType?.value

            // References
            //def context = null // TODO: use context
            tmp.budgetCodes         = CostItemGroup.findAllByCostItem(it).collect{ it.budgetCode?.value }.unique()
            tmp.copyBase            = it.copyBase?.globalUID
            tmp.invoiceNumber       = it.invoice?.invoiceNumber // retrieveInvoiceMap(it.invoice) // com.k_int.kbplus.Invoice
            // tmp.issueEntitlement    = ApiIssueEntitlement.retrieveIssueEntitlementMap(it.issueEntitlement, ApiReader.IGNORE_ALL, context) // com.k_int.kbplus.issueEntitlement
            tmp.orderNumber         = it.order?.orderNumber // retrieveOrderMap(it.order) // com.k_int.kbplus.Order
            // tmp.owner               = ApiStubReader.retrieveOrganisationStubMap(it.owner, context) // com.k_int.kbplus.Org
            // tmp.sub                 = ApiStubReader.requestSubscriptionStub(it.sub, context) // com.k_int.kbplus.Subscription // RECURSION ???
            // tmp.package             = ApiStubReader.retrieveSubscriptionPackageStubMixed(it.subPkg, ApiReader.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.SubscriptionPackage
            //tmp.surveyOrg
            //tmp.subPkg

            result << ApiToolkit.cleanUp(tmp, true, true)
        }

        result
    }

    static Collection<Object> retrieveCustomPropertyCollection(Collection<Object> list, def generic, Org context) {
        def result = []

        if (generic.metaClass.getMetaMethod("getCalculatedPropDefGroups")) {
            def groups = generic.getCalculatedPropDefGroups(context)
            def tmp = []

            // [PropertyDefinitionGroup, ..]
            groups.global?.each { it ->
                if (it.isVisible) {
                    tmp.addAll(it.getCurrentProperties(generic))
                }
            }
            // [[PropertyDefinitionGroup, PropertyDefinitionGroupBinding], ..]
            groups.local?.each { it ->
                if (it[0].isVisible) {
                    tmp.addAll(it[0].getCurrentProperties(generic))
                }
            }
            // [[PropertyDefinitionGroup, PropertyDefinitionGroupBinding], ..]
            groups.member?.each { it ->
                if (it[1].isVisibleForConsortiaMembers) {
                    tmp.addAll(it[0].getCurrentProperties(generic))
                }
            }

            // [<x>CustomProperty, ..]
            if (groups.orphanedProperties) {
                tmp.addAll(groups.orphanedProperties)
            }

            list = tmp.unique()
        }

        list?.each { it ->       // com.k_int.kbplus.<x>CustomProperty
            def tmp             = [:]
            tmp.name            = it.type?.name     // com.k_int.kbplus.PropertyDefinition.String
            tmp.description     = it.type?.descr    // com.k_int.kbplus.PropertyDefinition.String
            tmp.explanation     = it.type?.expl     // com.k_int.kbplus.PropertyDefinition.String
            tmp.value           = (it.stringValue ?: (it.intValue ?: (it.decValue ?: (it.refValue?.value ?: (it.urlValue ?: (it.dateValue ?: null)))))) // RefdataValue

            if (it.type.type == RefdataValue.toString()) {
                tmp.refdataCategory = it.type.refdataCategory
            }

            tmp.note            = it.note
            tmp.isPublic        = "Yes" // derived to substitute private properties tentant

            if (it instanceof LicenseCustomProperty) {
                tmp.paragraph = it.paragraph
            }

            tmp = ApiToolkit.cleanUp(tmp, true, false)
            result << tmp
        }
        result
    }

    static Collection<Object> retrieveDocumentCollection(Collection<DocContext> list) {
        def result = []
        list?.each { it -> // com.k_int.kbplus.DocContext
            result << ApiDoc.retrieveDocumentMap(it.owner)
        }
        result
    }

    static Collection<Object> retrieveIdentifierCollection(Collection<Identifier> list) {
        def result = []
        list?.each { it ->   // com.k_int.kbplus.IdentifierOccurrence
            def tmp = [:]
            tmp.put( 'namespace', it.ns?.ns )
            tmp.put( 'value', it.value )

            tmp = ApiToolkit.cleanUp(tmp, true, true)
            result << tmp
        }
        result
    }

    static Map<String, Object> retrieveInvoiceMap(Invoice invoice) {
        def result = [:]
        if(! invoice) {
            return null
        }
        result.id                  = invoice.id
        result.dateOfPayment       = invoice.dateOfPayment
        result.dateOfInvoice       = invoice.dateOfInvoice
        result.datePassedToFinance = invoice.datePassedToFinance
        result.endDate             = invoice.endDate
        result.invoiceNumber       = invoice.invoiceNumber
        result.startDate           = invoice.startDate
        result.lastUpdated         = invoice.lastUpdated

        // References
        def context = null // TODO: use context
        result.owner               = ApiStubReader.retrieveOrganisationStubMap(invoice.owner, context) // com.k_int.kbplus.Org

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @param com.k_int.kbplus.SubscriptionPackage subPkg
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return Collection<Object>
     */
    static Collection<Object> retrieveIssueEntitlementCollection(SubscriptionPackage subPkg, ignoreRelation, Org context){
        def result = []

        List<IssueEntitlement> ieList = IssueEntitlement.executeQuery(
                'select ie from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub join tipp.pkg pkg ' +
                        ' where sub = :sub and pkg = :pkg', [sub: subPkg.subscription, pkg: subPkg.pkg]
        )
        ieList.each{ ie ->
            result << ApiIssueEntitlement.retrieveIssueEntitlementMap(ie, ignoreRelation, context) // com.k_int.kbplus.IssueEntitlement
        }

        /* 0.51
        def tipps = TitleInstancePackagePlatform.findAllByPkg(subPkg.pkg)
        tipps.each{ tipp ->
            def ie = IssueEntitlement.findBySubscriptionAndTipp(subPkg.subscription, tipp)
            if (ie) {
                result << ApiCollectionReader.resolveIssueEntitlement(ie, ignoreRelation, context) // com.k_int.kbplus.IssueEntitlement
            }
        }
        */

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @param list
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return

    def resolveIssueEntitlements(def list, def ignoreRelation, Org context) {
        def result = []
        if(list) {
            list.each { it -> // com.k_int.kbplus.IssueEntitlement
                result << resolveIssueEntitlement(it, ignoreRelation, context)
            }
        }
        result
    }
*/
    /**
     *
     * @param list
     * @param com.k_int.kbplus.Org context
     * @return Collection<Object>
    */
    static Collection<Object> retrievePackageWithIssueEntitlementsCollection(Collection<SubscriptionPackage> list, Org context) {  // TODO - TODO - TODO
        def result = []

        list?.each { subPkg ->
            def pkg = ApiStubReader.retrievePackageStubMap(subPkg.pkg, context) // com.k_int.kbplus.Package
            result << pkg

            if (pkg != Constants.HTTP_FORBIDDEN) {
                pkg.issueEntitlements = retrieveIssueEntitlementCollection(subPkg, ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE, context)
            }
        }

        return ApiToolkit.cleanUp(result, true, false)
    }

    /* not used
    def resolveLink(Link link) {
        def result = [:]
        if (!link) {
            return null
        }
        result.id   = link.id

        // RefdataValues
        result.status   = link.status?.value
        result.type     = link.type?.value
        result.isSlaved = link.isSlaved?.value

        def context = null // TODO: use context
        result.fromLic  = ApiStubReader.resolveLicenseStub(link.fromLic, context) // com.k_int.kbplus.License
        result.toLic    = ApiStubReader.resolveLicenseStub(link.toLic, context) // com.k_int.kbplus.License

        return ApiToolkit.cleanUp(result, true, true)
    }
    */

    /* not used
    def resolveLinks(list) {
        def result = []
        if(list) {
            list.each { it -> // com.k_int.kbplus.Link
                result << resolveLink(it)
            }
        }
        result
    }
    */

    static Map<String, Object> retrieveOrderMap(Order order) {
        def result = [:]
        if (!order) {
            return null
        }
        result.id           = order.id
        result.orderNumber  = order.orderNumber
        result.lastUpdated  = order.lastUpdated

        // References
        def context = null // TODO: use context
        result.owner        = ApiStubReader.retrieveOrganisationStubMap(order.owner, context) // com.k_int.kbplus.Org

        return ApiToolkit.cleanUp(result, true, true)
    }

    static Collection<Object> retrieveOrgLinkCollection(Collection<OrgRole> list, ignoreRelationType, Org context) { // TODO
        def result = []

        list?.each { it ->   // com.k_int.kbplus.OrgRole
            def tmp         = [:]
            tmp.endDate     = it.endDate
            tmp.startDate   = it.startDate

            // RefdataValues
            tmp.roleType    = it.roleType?.value

            // References
            if (it.org && (ApiReader.IGNORE_ORGANISATION != ignoreRelationType)) {
                tmp.organisation = ApiStubReader.retrieveOrganisationStubMap(it.org, context) // com.k_int.kbplus.Org
            }
            if (it.cluster && (ApiReader.IGNORE_CLUSTER != ignoreRelationType)) {
                tmp.cluster = ApiStubReader.retrieveClusterStubMap(it.cluster) // com.k_int.kbplus.Cluster
            }
            if (it.lic && (ApiReader.IGNORE_LICENSE != ignoreRelationType)) {
                tmp.license = ApiStubReader.requestLicenseStub(it.lic, context) // com.k_int.kbplus.License
            }
            if (it.pkg && (ApiReader.IGNORE_PACKAGE != ignoreRelationType)) {
                tmp.package = ApiStubReader.retrievePackageStubMap(it.pkg, context) // com.k_int.kbplus.Package
            }
            if (it.sub && (ApiReader.IGNORE_SUBSCRIPTION != ignoreRelationType)) {
                tmp.subscription = ApiStubReader.requestSubscriptionStub(it.sub, context) // com.k_int.kbplus.Subscription
            }
            if (it.title && (ApiReader.IGNORE_TITLE != ignoreRelationType)) {
                tmp.title = ApiStubReader.retrieveTitleStubMap(it.title) // com.k_int.kbplus.TitleInstance
            }

            result << ApiToolkit.cleanUp(tmp, true, false)
        }
        result
    }

    static Map<String, Object> retrievePersonMap(Person prs, allowedContactTypes, allowedAddressTypes, Org context) {
        def result = [:]

        if(prs) {
            result.globalUID       = prs.globalUID
            result.firstName       = prs.first_name
            result.middleName      = prs.middle_name
            result.lastName        = prs.last_name
            result.title           = prs.title
            result.lastUpdated     = prs.lastUpdated

            // RefdataValues
            result.gender          = prs.gender?.value
            result.isPublic        = prs.isPublic ? 'Yes' : 'No'
            result.contactType     = prs.contactType?.value
            result.roleType        = prs.roleType?.value

            // References
            result.contacts     = retrieveContactCollection(prs.contacts, allowedContactTypes) // com.k_int.kbplus.Contact
            result.addresses    = retrieveAddressCollection(prs.addresses, allowedAddressTypes) // com.k_int.kbplus.Address
            result.properties   = retrievePrivatePropertyCollection(prs.privateProperties, context) // com.k_int.kbplus.PersonPrivateProperty
        }
        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Access rights due wrapping object
     *
     * @param com.k_int.kbplus.Platform pform
     * @return
     */
    static Map<String, Object> retrievePlatformMap(Platform pform) {
        def result = [:]

        if (pform) {
            result.globalUID        = pform.globalUID
            result.impId            = pform.impId
            result.gokbId           = pform.gokbId
            result.name             = pform.name
            result.normName         = pform.normname
            result.primaryUrl       = pform.primaryUrl
            result.provenance       = pform.provenance
            result.dateCreated      = pform.dateCreated
            result.lastUpdated      = pform.lastUpdated

            // RefdataValues
            result.type                 = pform.type?.value
            result.status               = pform.status?.value
            result.serviceProvider      = pform.serviceProvider?.value
            result.softwareProvider     = pform.softwareProvider?.value

            // References
            //result.tipps = pform.tipps
        }
        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Access rights due wrapping object
     */
    static Collection<Object> retrievePlatformTippCollection(Collection<PlatformTIPP> list) {
        def result = []

        list?.each { it -> // com.k_int.kbplus.PlatformTIPP
            def tmp = [:]
            tmp.titleUrl = it.titleUrl
            tmp.rel      = it.rel

            result << tmp
        }

        return ApiToolkit.cleanUp(result, true, true)
    }

    static Collection<Object> retrievePrivatePropertyCollection(Collection list, Org context) {
        def result = []

        list?.findAll{ it.owner.id == context.id || it.type.tenant?.id == context.id}?.each { it ->       // com.k_int.kbplus.<x>PrivateProperty
            def tmp             = [:]
            tmp.name            = it.type?.name     // com.k_int.kbplus.PropertyDefinition.String
            tmp.description     = it.type?.descr    // com.k_int.kbplus.PropertyDefinition.String
            tmp.explanation     = it.type?.expl     // com.k_int.kbplus.PropertyDefinition.String
            //tmp.tenant          = ApiStubReader.resolveOrganisationStub(it.tenant, context) // com.k_int.kbplus.Org
            tmp.value           = (it.stringValue ?: (it.intValue ?: (it.decValue ?: (it.refValue?.value ?: (it.urlValue ?: (it.dateValue ?: null)))))) // RefdataValue
            tmp.note            = it.note

            if (it instanceof LicensePrivateProperty) {
                tmp.paragraph = it.paragraph
            }

            if(it.type.tenant?.id == context.id) {
                tmp.isPublic    = "No" // derived to substitute tentant
                result << ApiToolkit.cleanUp(tmp, true, false)
            }
        }
        result
    }

    static retrievePropertyCollection(Object generic, Org context, def ignoreFlag) {
        def cp = retrieveCustomPropertyCollection(generic.customProperties, generic, context)
        def pp = retrievePrivatePropertyCollection(generic.privateProperties, context)

        if (ignoreFlag == ApiReader.IGNORE_CUSTOM_PROPERTIES) {
            return pp
        }
        else if (ignoreFlag == ApiReader.IGNORE_PRIVATE_PROPERTIES) {
            return cp
        }

        pp.each { cp << it }
        cp
    }

    static Collection<Object> retrievePrsLinkCollection(Collection<PersonRole> list, allowedAddressTypes, allowedContactTypes, Org context) {  // TODO check context
        def result = []
        def tmp = []

        list?.each { it ->

            // nested prs
            if(it.prs) {
                def x = it.prs.id
                def person = result.find {it.id == x}

                if(!person) {
                    person = retrievePersonMap(it.prs, allowedAddressTypes, allowedContactTypes, context) // com.k_int.kbplus.Person

                    // export public
                    if("No" != person.isPublic?.value?.toString()) {
                        tmp << person
                    }
                    // or private if tenant = context
                    else {
                        if(it.prs.tenant?.id == context.id) {
                            tmp << person
                        }
                    }
                }

                def role                    = [:] // com.k_int.kbplus.PersonRole
                role.startDate              = it.start_date
                role.endDate                = it.end_date

                // RefdataValues
                role.functionType           = it.functionType?.value
                //role.responsibilityType     = it.responsibilityType?.value

                if(!person.roles) {
                    person.roles = []
                }
                if (role.functionType) {
                    person.roles << ApiToolkit.cleanUp(role, true, false)
                }


                // TODO responsibilityType
                /*if (role.responsibilityType) {
                    // References
                    //if (it.org) {
                    //    role.organisation = ApiStubReader.resolveOrganisationStub(it.org, context) // com.k_int.kbplus.Org
                    //}

                    if (it.cluster) {
                        role.cluster = ApiStubReader.resolveClusterStub(it.cluster) // com.k_int.kbplus.Cluster
                    }
                    if (it.lic) {
                        role.license = ApiStubReader.resolveLicenseStub(it.lic, context) // com.k_int.kbplus.License
                    }
                    if (it.pkg) {
                        role.package = ApiStubReader.resolvePackageStub(it.pkg, context) // com.k_int.kbplus.Package
                    }
                    if (it.sub) {
                        role.subscription = ApiStubReader.resolveSubscriptionStub(it.sub, context) // com.k_int.kbplus.Subscription
                    }
                    if (it.title) {
                        role.title = ApiStubReader.resolveTitleStub(it.title) // com.k_int.kbplus.TitleInstance
                    }
                }*/
            }
        }

        // export only persons with valid roles
        tmp.each{ person ->
            if (! person.roles.isEmpty()) {
                result << person
            }
        }

        result
    }

    /**
     * Access rights due wrapping object. Some relations may be blocked
     *
     * @param com.k_int.kbplus.TitleInstancePackagePlatform tipp
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveTippMap(TitleInstancePackagePlatform tipp, def ignoreRelation, Org context) {
        def result = [:]
        if (!tipp) {
            return null
        }

        result.globalUID        = tipp.globalUID
        //result.accessStartDate  = tipp.accessStartDate     // duplicate information in IE
        //result.accessEndDate    = tipp.accessEndDate       // duplicate information in IE
        //result.coreStatusStart  = tipp.coreStatusStart     // duplicate information in IE
        //result.coreStatusEnd    = tipp.coreStatusEnd       // duplicate information in IE
        //result.coverageDepth    = tipp.coverageDepth       // duplicate information in IE
        //result.coverageNote     = tipp.coverageNote        // duplicate information in IE
        //result.embargo          = tipp.embargo             // duplicate information in IE
        //result.endDate          = tipp.endDate             // duplicate information in IE
        //result.endVolume        = tipp.endVolume           // duplicate information in IE
        //result.endIssue         = tipp.endIssue            // duplicate information in IE
        result.hostPlatformURL  = tipp.hostPlatformURL
        result.impId            = tipp.impId
        result.gokbId           = tipp.gokbId
        result.lastUpdated      = tipp.lastUpdated
        //result.rectype          = tipp.rectype    // legacy; not needed ?
        //result.startDate        = tipp.startDate           // duplicate information in IE
        //result.startIssue       = tipp.startIssue          // duplicate information in IE
        //result.startVolume      = tipp.startVolume          // duplicate information in IE

        // RefdataValues
        result.status           = tipp.status?.value
        result.option           = tipp.option?.value
        result.delayedOA        = tipp.delayedOA?.value
        result.hybridOA         = tipp.hybridOA?.value
        result.statusReason     = tipp.statusReason?.value
        result.payment          = tipp.payment?.value

        // References
        result.additionalPlatforms  = retrievePlatformTippCollection(tipp.additionalPlatforms) // com.k_int.kbplus.PlatformTIPP
        result.identifiers          = retrieveIdentifierCollection(tipp.ids)       // com.k_int.kbplus.Identifier
        result.platform             = ApiStubReader.retrievePlatformStubMap(tipp.platform) // com.k_int.kbplus.Platform
        result.title                = ApiStubReader.retrieveTitleStubMap(tipp.title)       // com.k_int.kbplus.TitleInstance

        if (ignoreRelation != ApiReader.IGNORE_ALL) {
            if (ignoreRelation != ApiReader.IGNORE_PACKAGE) {
                result.package = ApiStubReader.retrievePackageStubMap(tipp.pkg, context) // com.k_int.kbplus.Package
            }
            if (ignoreRelation != ApiReader.IGNORE_SUBSCRIPTION) {
                result.subscription = ApiStubReader.requestSubscriptionStub(tipp.sub, context) // com.k_int.kbplus.Subscription
            }
        }
        //result.derivedFrom      = ApiStubReader.resolveTippStub(tipp.derivedFrom)  // com.k_int.kbplus.TitleInstancePackagePlatform
        //result.masterTipp       = ApiStubReader.resolveTippStub(tipp.masterTipp)   // com.k_int.kbplus.TitleInstancePackagePlatform

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Access rights due wrapping object
     *
     * @param list
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return Collection<Object>
     */
    static Collection<Object> retrieveTippCollection(Collection<TitleInstancePackagePlatform> list, def ignoreRelation, Org context) {
        def result = []

        list?.each { it -> // com.k_int.kbplus.TitleInstancePackagePlatform
            result << retrieveTippMap(it, ignoreRelation, context)
        }

        result
    }

    /* not used
    def resolveTitle(TitleInstance title) {
        def result = [:]
        if (!title) {
            return null
        }

        result.id               = title.id
        result.title            = title.title
        result.normTitle        = title.normTitle
        result.keyTitle         = title.keyTitle
        result.sortTitle        = title.sortTitle
        result.impId            = title.impId
        result.dateCreated      = title.dateCreated
        result.lastUpdated      = title.lastUpdated

        // RefdataValues

        result.status       = title.status?.value
        result.type         = title.type?.value

        // References

        result.identifiers  = resolveIdentifiers(title.ids) // com.k_int.kbplus.IdentifierOccurrence

        // TODO
        //tipps:  TitleInstancePackagePlatform,
        //orgs:   OrgRole,
        //historyEvents: TitleHistoryEventParticipant,
        //prsLinks: PersonRole

        return ApiToolkit.cleanUp(result, true, true)
    }
    */
}
