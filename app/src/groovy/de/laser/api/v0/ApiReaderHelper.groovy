package de.laser.api.v0

import com.k_int.kbplus.*
import de.laser.api.v0.entities.*
import de.laser.helper.Constants
import groovy.util.logging.Log4j

@Log4j
class ApiReaderHelper {

    final static NO_CONSTRAINT          = "NO_CONSTRAINT"
    final static LICENSE_STUB           = "LICENSE_STUB"

    // type of stub to return
    final static PACKAGE_STUB           = "PACKAGE_STUB"
    final static SUBSCRIPTION_STUB      = "SUBSCRIPTION_STUB"

    // ignoring relations
    final static IGNORE_ALL             = "IGNORE_ALL"  // cutter for nested objects
    final static IGNORE_NONE            = "IGNORE_NONE" // placeholder, if needed
    final static IGNORE_CLUSTER         = "IGNORE_CLUSTER"

    final static IGNORE_LICENSE         = "IGNORE_LICENSE"
    final static IGNORE_ORGANISATION    = "IGNORE_ORGANISATION"
    final static IGNORE_PACKAGE         = "IGNORE_PACKAGE"
    final static IGNORE_SUBSCRIPTION    = "IGNORE_SUBSCRIPTION"
    final static IGNORE_TITLE           = "IGNORE_TITLE"
    final static IGNORE_TIPP            = "IGNORE_TIPP"

    final static IGNORE_SUBSCRIPTION_AND_PACKAGE = "IGNORE_SUBSCRIPTION_AND_PACKAGE"


    // ################### HELPER ###################

    /**
     * Resolving collection of items to stubs. Delegate context to gain access
     *
     * @param Collection<Object> list
     * @param type
     * @param com.k_int.kbplus.Org context
     * @return Collection<Object>
     */
    static Collection<Object> retrieveStubCollection(Collection<Object> list, def type, Org context) {
        def result = []

        list?.each { it ->
            if(LICENSE_STUB == type) {
                result << requestLicenseStub(it, context)
            }
            else if(PACKAGE_STUB == type) {
                result << retrievePackageStubMap(it, context)
            }
            else if(SUBSCRIPTION_STUB == type) {
                result << requestSubscriptionStub(it, context)
            }
        }

        result
    }

    // ################### STUBS ###################

    @Deprecated
    static Map<String, Object> retrieveClusterStubMap(Cluster cluster) {
        def result = [:]
        if (cluster) {
            result.id           = cluster.id
            result.name         = cluster.name
        }
        return ApiToolkit.cleanUp(result, true, true)
    }

    static requestLicenseStub(License lic, Org context) {
        requestLicenseStub(lic, context, false)
    }

    static requestLicenseStub(License lic, Org context, boolean hasAccess) {
        def result = [:]

        if (!lic) {
            return null
        }

        if (! hasAccess) {
            hasAccess = ApiLicense.calculateAccess(lic, context, hasAccess)
        }

        if (hasAccess) {
            result.globalUID    = lic.globalUID
            result.impId        = lic.impId
            result.reference    = lic.reference
            result.normReference    = lic.sortableReference
            // erms-888
            result.calculatedType   = lic.getCalculatedType()

            // References
            result.identifiers = retrieveIdentifierCollection(lic.ids) // com.k_int.kbplus.IdentifierOccurrence

            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? result : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveOrganisationStubMap(Org org, Org context) {
        if (!org) {
            return null
        }

        def result = [:]
        result.globalUID    = org.globalUID
        result.gokbId       = org.gokbId
        result.name         = org.name

        // References
        result.identifiers = retrieveIdentifierCollection(org.ids) // com.k_int.kbplus.IdentifierOccurrence

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> retrievePackageStubMap(Package pkg, Org context) {
        if (!pkg) {
            return null
        }

        def result = [:]
        result.globalUID    = pkg.globalUID
        result.name         = pkg.name
        //result.identifier   = pkg.identifier // TODO refactor legacy
        result.impId        = pkg.impId
        result.gokbId       = pkg.gokbId

        // References
        result.identifiers = retrieveIdentifierCollection(pkg.ids) // com.k_int.kbplus.IdentifierOccurrence

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> retrievePlatformStubMap(Platform pform) {
        def result = [:]
        if (pform) {
            result.globalUID    = pform.globalUID
            result.impId        = pform.impId
            result.gokbId       = pform.gokbId
            result.name         = pform.name
            result.normname     = pform.normname
            result.primaryUrl   = pform.primaryUrl
        }
        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return MAP | Constants.HTTP_FORBIDDEN
     */
    static requestSubscriptionStub(Subscription sub, Org context) {
        requestSubscriptionStub(sub, context, false)
    }

    static requestSubscriptionStub(Subscription sub, Org context, boolean hasAccess) {
        def result = [:]

        if (!sub) {
            return null
        }

        if (! hasAccess) {
            hasAccess = ApiSubscription.calculateAccess(sub, context, hasAccess)
        }

        if (hasAccess) {
            result.globalUID    = sub.globalUID
            result.name         = sub.name
            //result.identifier   = sub.identifier // TODO refactor identifier
            result.impId        = sub.impId
            // erms-888
            result.calculatedType = sub.getCalculatedType()

            // References
            result.identifiers = retrieveIdentifierCollection(sub.ids) // com.k_int.kbplus.IdentifierOccurrence

            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? result : Constants.HTTP_FORBIDDEN)
    }

    static retrieveSubscriptionPackageStubMixed(SubscriptionPackage subpkg, ignoreRelation, Org context) {
        if (subpkg) {
            if (IGNORE_SUBSCRIPTION == ignoreRelation) {
                return retrievePackageStubMap(subpkg.pkg, context)
            }
            else if (IGNORE_PACKAGE == ignoreRelation) {
                return requestSubscriptionStub(subpkg.subscription, context)
            }
        }
        return null
    }

    static Collection<Object> retrieveSubscriptionPackageStubCollection(Collection<SubscriptionPackage> list, def ignoreRelation, Org context) {
        def result = []
        if (! list) {
            return null
        }

        list?.each { it -> // com.k_int.kbplus.SubscriptionPackage
            result << retrieveSubscriptionPackageStubMixed(it, ignoreRelation, context)
        }
        result
    }

    static Map<String, Object> retrieveTitleStubMap(TitleInstance title) {
        def result = [:]

        result.globalUID    = title.globalUID
        result.impId        = title.impId
        result.gokbId       = title.gokbId
        result.title        = title.title
        result.normTitle    = title.normTitle
        result.type         = title.type?.value

        // References
        result.identifiers = retrieveIdentifierCollection(title.ids) // com.k_int.kbplus.IdentifierOccurrence

        return ApiToolkit.cleanUp(result, true, true)
    }

    // ################### FULL OBJECTS ###################

    static Collection<Object> retrieveAddressCollection(Collection<Address> list, allowedTypes) {
        def result = []

        list?.each { it ->   // com.k_int.kbplus.Address
            def tmp         = [:]
            tmp.street1     = it.street_1
            tmp.street2     = it.street_2
            tmp.pob         = it.pob
            tmp.pobZipcode  = it.pobZipcode
            tmp.pobCity     = it.pobCity
            tmp.zipcode     = it.zipcode
            tmp.city        = it.city
            tmp.name        = it.name
            tmp.additionFirst  = it.additionFirst
            tmp.additionSecond = it.additionSecond

            // RefdataValues
            tmp.state       = it.state?.value
            tmp.country     = it.country?.value
            tmp.type        = it.type?.value

            tmp = ApiToolkit.cleanUp(tmp, true, false)

            if(NO_CONSTRAINT == allowedTypes || allowedTypes.contains(it.type?.value)) {
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

            // RefdataValues
            tmp.category        = it.contentType?.value
            tmp.type            = it.type?.value

            tmp = ApiToolkit.cleanUp(tmp, true, false)

            if(NO_CONSTRAINT == allowedTypes || allowedTypes.contains(it.type?.value)) {
                result << tmp
            }
        }
        result
    }

    @Deprecated
    static Collection<Object> retrieveCostItemCollection(Collection<CostItem> list) {  // TODO
        def result = []

        list?.each { it ->               // com.k_int.kbplus.CostItem
            def tmp                     = [:]
            tmp.id                      = it.id
            tmp.costInBillingCurrency   = it.costInBillingCurrency
            tmp.costInLocalCurrency     = it.costInLocalCurrency
            tmp.costDescription         = it.costDescription
            tmp.includeInSubscription   = it.includeInSubscription
            tmp.reference               = it.reference

            tmp.datePaid            = it.datePaid
            tmp.startDate           = it.startDate
            tmp.endDate             = it.endDate
            tmp.dateCreated         = it.dateCreated
            tmp.lastUpdated         = it.lastUpdated

            // RefdataValues
            tmp.billingCurrency     = it.billingCurrency?.value
            tmp.costItemCategory    = it.costItemCategory?.value
            tmp.costItemElement     = it.costItemElement?.value
            tmp.costItemStatus      = it.costItemStatus?.value
            tmp.taxCode             = it.taxCode?.value

            // References
            def context = null // TODO: use context
            tmp.invoice             = retrieveInvoiceMap(it.invoice) // com.k_int.kbplus.Invoice
            tmp.issueEntitlement    = retrieveIssueEntitlementMap(it.issueEntitlement, IGNORE_ALL, context) // com.k_int.kbplus.issueEntitlement
            tmp.order               = retrieveOrderMap(it.order) // com.k_int.kbplus.Order
            tmp.owner               = retrieveOrganisationStubMap(it.owner, context) // com.k_int.kbplus.Org
            tmp.sub                 = requestSubscriptionStub(it.sub, context) // com.k_int.kbplus.Subscription // RECURSION ???
            tmp.package             = retrieveSubscriptionPackageStubMixed(it.subPkg, IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.SubscriptionPackage
            result << tmp
        }

        /*
        User lastUpdatedBy
        User createdBy
        */
        result
    }

    static Collection<Object> retrieveCustomPropertyCollection(Collection<Object> list, def generic, Org context) {
        def result = []

        if (generic.metaClass.getMetaMethod("getCalculatedPropDefGroups")) {
            def groups = generic.getCalculatedPropDefGroups(context)
            def tmp = []

            groups.global?.each { it ->
                if (it.visible?.value == 'Yes') {
                    tmp.addAll(it.getCurrentProperties(generic))
                }
            }
            groups.local?.each { it ->
                if (it.visible?.value == 'Yes') {
                    tmp.addAll(it.getCurrentProperties(generic))
                }
            }
            groups.members?.each { it ->
                if (it.visibleForConsortiaMembers?.value == 'Yes') {
                    tmp.addAll(it.getCurrentProperties(generic))
                }
            }

            // use all custom properties as fallback if no group found
            if (! groups.fallback) {
                list = tmp.unique()
            }
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

    /**
     * Access rights due wrapping resource
     *
     * @param com.k_int.kbplus.Doc doc
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveDocumentMap(Doc doc) {
        def result = [:]

        if (doc) {
            result.content  = doc.content
            result.filename = doc.filename
            result.mimeType = doc.mimeType
            result.title    = doc.title
            result.uuid     = doc.uuid

            // RefdataValues
            result.type     = doc.type?.value
        }

        return ApiToolkit.cleanUp(result, true, true)
    }

    static Collection<Object> retrieveDocumentCollection(Collection<DocContext> list) {
        def result = []
        list?.each { it -> // com.k_int.kbplus.DocContext
            result << retrieveDocumentMap(it.owner)
        }
        result
    }

    static Collection<Object> retrieveIdentifierCollection(Collection<IdentifierOccurrence> list) {
        def result = []
        list?.each { it ->   // com.k_int.kbplus.IdentifierOccurrence
            def tmp = [:]
            tmp.put( 'namespace', it.identifier?.ns?.ns )
            tmp.put( 'value', it.identifier?.value )

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

        // References
        def context = null // TODO: use context
        result.owner               = retrieveOrganisationStubMap(invoice.owner, context) // com.k_int.kbplus.Org

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Access rights due wrapping object
     *
     * @param com.k_int.kbplus.IssueEntitlement ie
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveIssueEntitlementMap(IssueEntitlement ie, def ignoreRelation, Org context) {
        def result = [:]
        if (! ie) {
            return null
        }

        result.globalUID        = ie.globalUID
        result.accessStartDate  = ie.accessStartDate
        result.accessEndDate    = ie.accessEndDate
        result.startDate        = ie.startDate
        result.startVolume      = ie.startVolume
        result.startIssue       = ie.startIssue
        result.endDate          = ie.endDate
        result.endVolume        = ie.endVolume
        result.endIssue         = ie.endIssue
        result.embargo          = ie.embargo
        result.coverageDepth    = ie.coverageDepth
        result.coverageNote     = ie.coverageNote
        result.ieReason         = ie.ieReason
        result.coreStatusStart  = ie.coreStatusStart
        result.coreStatusEnd    = ie.coreStatusEnd

        // RefdataValues
        result.coreStatus       = ie.coreStatus?.value
        result.medium           = ie.medium?.value
        //result.status           = ie.status?.value // legacy; not needed ?

        // References
        if (ignoreRelation != IGNORE_ALL) {
            if (ignoreRelation == IGNORE_SUBSCRIPTION_AND_PACKAGE) {
                result.tipp = retrieveTippMap(ie.tipp, IGNORE_ALL, context) // com.k_int.kbplus.TitleInstancePackagePlatform
            }
            else {
                if (ignoreRelation != IGNORE_TIPP) {
                    result.tipp = retrieveTippMap(ie.tipp, IGNORE_NONE, context)
                    // com.k_int.kbplus.TitleInstancePackagePlatform
                }
                if (ignoreRelation != IGNORE_SUBSCRIPTION) {
                    result.subscription = requestSubscriptionStub(ie.subscription, context)
                    // com.k_int.kbplus.Subscription
                }
            }
        }

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
            def pkg = retrievePackageStubMap(subPkg.pkg, context) // com.k_int.kbplus.Package
            result << pkg

            if (pkg != Constants.HTTP_FORBIDDEN) {
                pkg.issueEntitlements = ApiReader.retrieveIssueEntitlementCollection(subPkg, ApiReaderHelper.IGNORE_SUBSCRIPTION_AND_PACKAGE, context)
            }
        }

        return ApiToolkit.cleanUp(result, true, false)
    }

    /**
     * Access rights due wrapping object
     *
     * @param com.k_int.kbplus.License lic
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return
     */
    static requestLicense(License lic, def ignoreRelation, Org context) {
        if (!lic) {
            return null
        }

        return ApiReader.retrieveLicenseMap(lic, ignoreRelation, context)
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
        result.fromLic  = resolveLicenseStub(link.fromLic, context) // com.k_int.kbplus.License
        result.toLic    = resolveLicenseStub(link.toLic, context) // com.k_int.kbplus.License

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

    /**
     * Access rights due wrapping license
     *
     * @param com.k_int.kbplus.OnixplLicense opl
     * @param com.k_int.kbplus.License lic
     * @param com.k_int.kbplus.Org context
     * @return Map | Constants.HTTP_FORBIDDEN
     */
    static requestOnixplLicense(OnixplLicense opl, License lic, Org context) {
        def result = [:]
        def hasAccess = false

        if (!opl) {
            return null
        }

        if (opl.getLicenses().contains(lic)) {
            lic.orgLinks.each { orgRole ->
                // TODO check orgRole.roleType
                if (orgRole.getOrg().id == context?.id) {
                    hasAccess = true
                }
            }
        }

        if (hasAccess) {
            //result.id       = opl.id
            result.lastmod  = opl.lastmod
            result.title    = opl.title

            // References
            result.document = retrieveDocumentMap(opl.doc) // com.k_int.kbplus.Doc
            //result.licenses = resolveLicenseStubs(opl.licenses) // com.k_int.kbplus.License
            //result.xml = opl.xml // XMLDoc // TODO
            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? result : Constants.HTTP_FORBIDDEN)
    }

    static Map<String, Object> retrieveOrderMap(Order order) {
        def result = [:]
        if (!order) {
            return null
        }
        result.id           = order.id
        result.orderNumber  = order.orderNumber

        // References
        def context = null // TODO: use context
        result.owner        = retrieveOrganisationStubMap(order.owner, context) // com.k_int.kbplus.Org

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
            if (it.org && (IGNORE_ORGANISATION != ignoreRelationType)) {
                tmp.organisation = retrieveOrganisationStubMap(it.org, context) // com.k_int.kbplus.Org
            }
            if (it.cluster && (IGNORE_CLUSTER != ignoreRelationType)) {
                tmp.cluster = retrieveClusterStubMap(it.cluster) // com.k_int.kbplus.Cluster
            }
            if (it.lic && (IGNORE_LICENSE != ignoreRelationType)) {
                tmp.license = requestLicenseStub(it.lic, context) // com.k_int.kbplus.License
            }
            if (it.pkg && (IGNORE_PACKAGE != ignoreRelationType)) {
                tmp.package = retrievePackageStubMap(it.pkg, context) // com.k_int.kbplus.Package
            }
            if (it.sub && (IGNORE_SUBSCRIPTION != ignoreRelationType)) {
                tmp.subscription = requestSubscriptionStub(it.sub, context) // com.k_int.kbplus.Subscription
            }
            if (it.title && (IGNORE_TITLE != ignoreRelationType)) {
                tmp.title = resolveTitleStubMap(it.title) // com.k_int.kbplus.TitleInstance
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

            // RefdataValues
            result.gender          = prs.gender?.value
            result.isPublic        = prs.isPublic?.value
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
            //tmp.tenant          = resolveOrganisationStub(it.tenant, context) // com.k_int.kbplus.Org
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

    static retrievePropertyCollection(Object generic, Org context) {
        def cp = retrieveCustomPropertyCollection(generic.customProperties, generic, context)
        def pp = retrievePrivatePropertyCollection(generic.privateProperties, context)

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
                    //    role.organisation = resolveOrganisationStub(it.org, context) // com.k_int.kbplus.Org
                    //}

                    if (it.cluster) {
                        role.cluster = resolveClusterStub(it.cluster) // com.k_int.kbplus.Cluster
                    }
                    if (it.lic) {
                        role.license = resolveLicenseStub(it.lic, context) // com.k_int.kbplus.License
                    }
                    if (it.pkg) {
                        role.package = resolvePackageStub(it.pkg, context) // com.k_int.kbplus.Package
                    }
                    if (it.sub) {
                        role.subscription = resolveSubscriptionStub(it.sub, context) // com.k_int.kbplus.Subscription
                    }
                    if (it.title) {
                        role.title = resolveTitleStub(it.title) // com.k_int.kbplus.TitleInstance
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
        result.identifiers          = retrieveIdentifierCollection(tipp.ids)       // com.k_int.kbplus.IdentifierOccurrence
        result.platform             = retrievePlatformStubMap(tipp.platform) // com.k_int.kbplus.Platform
        result.title                = resolveTitleStubMap(tipp.title)       // com.k_int.kbplus.TitleInstance

        if (ignoreRelation != IGNORE_ALL) {
            if (ignoreRelation != IGNORE_PACKAGE) {
                result.package = retrievePackageStubMap(tipp.pkg, context) // com.k_int.kbplus.Package
            }
            if (ignoreRelation != IGNORE_SUBSCRIPTION) {
                result.subscription = requestSubscriptionStub(tipp.sub, context) // com.k_int.kbplus.Subscription
            }
        }
        //result.derivedFrom      = resolveTippStub(tipp.derivedFrom)  // com.k_int.kbplus.TitleInstancePackagePlatform
        //result.masterTipp       = resolveTippStub(tipp.masterTipp)   // com.k_int.kbplus.TitleInstancePackagePlatform

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
