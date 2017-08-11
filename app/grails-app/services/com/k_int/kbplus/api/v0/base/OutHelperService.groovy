package com.k_int.kbplus.api.v0.base

import com.k_int.kbplus.*
import com.k_int.kbplus.api.v0.LicenseService
import com.k_int.kbplus.api.v0.MainService
import groovy.util.logging.Log4j

@Log4j
class OutHelperService {

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

    // ################### HELPER ###################

    /**
     * @param list
     * @param removeEmptyValues
     * @param removeEmptyLists
     * @return
     */
    def cleanUp(list, removeNullValues, removeEmptyLists) {
        if(!list) {
            return null
        }
        Collection<String> values = list.values()

        if(removeNullValues){
            while (values.remove(null));
        }
        if(removeEmptyLists){
            while (values.remove([]));
        }
        list
    }

    /**
     * Resolving list<type> of items to stubs. Delegate context to gain access
     *
     * @param list
     * @param type
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def resolveStubs(def list, def type, Org context) {
        def result = []
        if(list) {
            list.each { it ->
                if(LICENSE_STUB == type) {
                    result << resolveLicenseStub(it, context)
                }
                else if(PACKAGE_STUB == type) {
                    result << resolvePackageStub(it, context)
                }
                else if(SUBSCRIPTION_STUB == type) {
                    result << resolveSubscriptionStub(it, context)
                }
            }
        }
        result
    }

    // ################### STUBS ###################

    def resolveClusterStub(Cluster cluster) {
        def result = [:]
        if(cluster) {
            result.id           = cluster.id
            result.name         = cluster.name
        }
        return cleanUp(result, true, true)
    }

    def resolveLicenseStub(License lic, Org context) {
        def result = [:]
        def hasAccess = false

        if (!lic) {
            return null
        }

        lic.getOrgLinks().each { orgRole ->
            // TODO check orgRole.roleType
            if (orgRole.getOrg().id == context?.id) {
                hasAccess = true
            }
        }
        if (hasAccess) {
            result.globalUID    = lic.globalUID
            result.impId        = lic.impId
            result.reference    = lic.reference
            result.sortableReference = lic.sortableReference

            // References
            result.identifiers = resolveIdentifiers(lic.ids) // com.k_int.kbplus.IdentifierOccurrence

            result = cleanUp(result, true, true)
        }

        return (hasAccess ? result : MainService.FORBIDDEN)
    }

    /**
     * @return MAP | MainService.FORBIDDEN
     */
    def resolveOrganisationStub(Org org, Org context) {
        def result = [:]
        def hasAccess = false

        if (!org) {
            return null
        }

        // TODO check orgRole.roleType
        if (org.id == context?.id) {
            hasAccess = true
        }
        if (hasAccess) {
            result.globalUID    = org.globalUID
            result.name         = org.name
            result.shortcode    = org.shortcode

            // References
            result.identifiers = resolveIdentifiers(org.ids) // com.k_int.kbplus.IdentifierOccurrence

            result = cleanUp(result, true, true)
        }

        return (hasAccess ? result : MainService.FORBIDDEN)
    }

    /**
     * @return MAP | MainService.FORBIDDEN
     */
    def resolvePackageStub(Package pkg, Org context) {
        def result = [:]
        def hasAccess = false

        if (!pkg) {
            return null
        }

        pkg.getOrgs().each { orgRole ->
            // TODO check orgRole.roleType
            if (orgRole.getOrg().id == context?.id) {
                hasAccess = true
            }
        }
        if (hasAccess) {
            result.globalUID    = pkg.globalUID
            result.name         = pkg.name
            result.identifier   = pkg.identifier
            result.impId        = pkg.impId

            // References
            result.identifiers = resolveIdentifiers(pkg.ids) // com.k_int.kbplus.IdentifierOccurrence

            result = cleanUp(result, true, true)
        }

        return (hasAccess ? result : MainService.FORBIDDEN)
    }

    def resolvePlatformStub(Platform pform) {
        def result = [:]
        if(pform) {
            result.globalUID    = pform.globalUID
            result.impId        = pform.impId
            result.name         = pform.name
            result.normname     = pform.normname
        }
        return cleanUp(result, true, true)
    }

    /**
     * @return MAP | MainService.FORBIDDEN
     */
    def resolveSubscriptionStub(Subscription sub, Org context) {
        def result = [:]
        def hasAccess = false

        if (!sub) {
            return null
        }

        sub.getOrgRelations().each { orgRole ->
            // TODO check orgRole.roleType
            if (orgRole.getOrg().id == context?.id) {
                hasAccess = true
            }
        }
        if (hasAccess) {
            result.globalUID    = sub.globalUID
            result.name         = sub.name
            result.identifier   = sub.identifier
            result.impId        = sub.impId

            // References
            result.identifiers = resolveIdentifiers(sub.ids) // com.k_int.kbplus.IdentifierOccurrence

            result = cleanUp(result, true, true)
        }

        return (hasAccess ? result : MainService.FORBIDDEN)
    }

    def resolveSubscriptionPackageStub(SubscriptionPackage subpkg, ignoreRelation, Org context) {
        if(subpkg) {
            if(IGNORE_SUBSCRIPTION == ignoreRelation) {
                return resolvePackageStub(subpkg.pkg, context)
            }
            else if(IGNORE_PACKAGE == ignoreRelation) {
                return resolveSubscriptionStub(subpkg.subscription, context)
            }
        }
        return null
    }

    def resolveSubscriptionPackageStubs(def list, def ignoreRelation, Org context) {
        def result = []
        if (!list) {
            return null
        }

        list.each { it -> // com.k_int.kbplus.SubscriptionPackage
            result << resolveSubscriptionPackageStub(it, ignoreRelation, context)
        }
        result
    }

    def resolveTitleStub(TitleInstance title) {
        def result = [:]

        result.globalUID    = title.globalUID
        result.impId        = title.impId
        result.title        = title.title
        result.normTitle    = title.normTitle

        // References
        result.identifiers = resolveIdentifiers(title.ids) // com.k_int.kbplus.IdentifierOccurrence

        return cleanUp(result, true, true)
    }

    // ################### FULL OBJECTS ###################

    def resolveAddresses(list, allowedTypes) {
        def result = []

        list.each { it ->   // com.k_int.kbplus.Address
            def tmp         = [:]
            tmp.street1     = it.street_1
            tmp.street2     = it.street_2
            tmp.pob         = it.pob
            tmp.zipcode     = it.zipcode
            tmp.city        = it.city
            tmp.state       = it.state
            tmp.country     = it.country

            // RefdataValues
            tmp.type        = it.type?.value

            tmp = cleanUp(tmp, true, false)

            if(NO_CONSTRAINT == allowedTypes || allowedTypes.contains(it.type?.value)) {
                result << tmp
            }
        }
        result
    }
/*
    def resolveCluster(Cluster cluster) {
        def result = [:]

        // TODO
        def allowedAddressTypes = ["Postal address", "Billing address", "Delivery address"]
        def allowedContactTypes = ["Job-related", "Personal"]

        if(cluster) {
            result.id           = cluster.id
            result.name         = cluster.name
            result.definition   = cluster.definition

            // References
            def context = null // TODO: use context
            result.organisations    = resolveOrgLinks(cluster.orgs, IGNORE_CLUSTER, context) // com.k_int.kbplus.OrgRole
            // TODO
            result.persons          = resolvePrsLinks(
                    cluster.prsLinks, allowedAddressTypes, allowedContactTypes, true, true
            ) // com.k_int.kbplus.PersonRole
        }
        return cleanUp(result, true, true)
    }
*/
    def resolveContacts(list, allowedTypes) {
        def result = []

        list.each { it ->       // com.k_int.kbplus.Contact
            def tmp             = [:]
            tmp.content         = it.content

            // RefdataValues
            tmp.category        = it.contentType?.value
            tmp.type            = it.type?.value

            tmp = cleanUp(tmp, true, false)

            if(NO_CONSTRAINT == allowedTypes || allowedTypes.contains(it.type?.value)) {
                result << tmp
            }
        }
        result
    }

    @Deprecated
    def resolveCostItems(list) {  // TODO
        def result = []

        list.each { it ->               // com.k_int.kbplus.CostItem
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
            tmp.invoice             = resolveInvoice(it.invoice) // com.k_int.kbplus.Invoice
            tmp.issueEntitlement    = resolveIssueEntitlement(it.issueEntitlement, IGNORE_ALL, context) // com.k_int.kbplus.issueEntitlement
            tmp.order               = resolveOrder(it.order) // com.k_int.kbplus.Order
            tmp.owner               = resolveOrganisationStub(it.owner, context) // com.k_int.kbplus.Org
            tmp.sub                 = resolveSubscriptionStub(it.sub, context) // com.k_int.kbplus.Subscription // RECURSION ???
            tmp.package             = resolveSubscriptionPackageStub(it.subPkg, IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.SubscriptionPackage
            result << tmp
        }

        /*
        User lastUpdatedBy
        User createdBy
        */
        result
    }

    def resolveCustomProperties(list) {
        def result = []

        list.each { it ->       // com.k_int.kbplus.<x>CustomProperty
            def tmp             = [:]
            tmp.name            = it.type?.name     // com.k_int.kbplus.PropertyDefinition.String
            tmp.description     = it.type?.descr    // com.k_int.kbplus.PropertyDefinition.String
            tmp.value           = (it.stringValue ? it.stringValue : (it.intValue ? it.intValue : (it.decValue ? it.decValue : (it.refValue?.value ? it.refValue?.value : null)))) // RefdataValue
            tmp.note            = it.note
            tmp.isPublic        = "Yes" // derived to substitute private properties tentant

            if (it instanceof LicenseCustomProperty) {
                tmp.paragraph = it.paragraph
            }

            tmp = cleanUp(tmp, true, false)
            result << tmp
        }
        result
    }

    /**
     * Access rights due wrapping resource
     *
     * @param com.k_int.kbplus.Doc doc
     * @return Map
     */
    def resolveDocument(Doc doc) {
        def result = [:]

        if(doc) {
            result.content  = doc.content
            result.filename = doc.filename
            result.mimeType = doc.mimeType
            result.title    = doc.title
            result.uuid     = doc.uuid

            // RefdataValues
            result.type     = doc.type?.value
        }

        return cleanUp(result, true, true)
    }

    def resolveDocuments(def list) {
        def result = []
        list.each { it -> // com.k_int.kbplus.DocContext
            result << resolveDocument(it.owner)
        }
        result
    }

    def resolveIdentifiers(list) {
        def result = []
        list.each { it ->   // com.k_int.kbplus.IdentifierOccurrence
            def tmp         = [:]
            tmp.value       = it.identifier?.value
            tmp.namespace   = it.identifier?.ns?.ns

            tmp = cleanUp(tmp, true, true)
            result << tmp
        }
        result
    }

    def resolveInvoice(Invoice invoice) {
        def result = [:]
        if(!invoice) {
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
        result.owner               = resolveOrganisationStub(invoice.owner, context) // com.k_int.kbplus.Org

        return cleanUp(result, true, true)
    }

    /**
     * Access rights due wrapping object
     *
     * @param com.k_int.kbplus.IssueEntitlement ie
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def resolveIssueEntitlement(IssueEntitlement ie, def ignoreRelation, Org context) {
        def result = [:]
        if (!ie) {
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
        result.status           = ie.status?.value

        // References
        if (ignoreRelation != IGNORE_ALL) {
            if (ignoreRelation != IGNORE_TIPP) {
                result.tipp = resolveTipp(ie.tipp, IGNORE_NONE, context) // com.k_int.kbplus.TitleInstancePackagePlatform
            }
            if (ignoreRelation != IGNORE_SUBSCRIPTION) {
                result.subscription = resolveSubscriptionStub(ie.subscription, context) // com.k_int.kbplus.Subscription
            }
        }

        return cleanUp(result, true, true)
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
     * @return
    */
    def resolvePackagesWithIssueEntitlements(def list, Org context) {  // TODO - TODO - TODO
        def result = []

        list.each { ie -> // com.k_int.kbplus.IssueEntitlement
            def tippPkg  = ie.tipp.pkg

            def x = tippPkg.id
            def pkg = result.find { it.id == x }

            if (!pkg) {
                pkg = resolvePackageStub(tippPkg, context) // com.k_int.kbplus.Package
                pkg.issueEntitlements = []
                result << pkg
            }

            def newIE = resolveIssueEntitlement(ie, IGNORE_ALL, context)
            def newTIPP = resolveTipp(ie.tipp, IGNORE_ALL, context)
            newIE.tipp = newTIPP
            pkg.issueEntitlements << newIE
        }
        return cleanUp(result, true, true)
    }

    /**
     * Access rights due wrapping object
     *
     * @param com.k_int.kbplus.License lic
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def resolveLicense(License lic, def ignoreRelation, Org context) {
        if (!lic) {
            return null
        }

        LicenseService licenseService // local declaration, otherwise error due circular service initialization
        return licenseService.getLicense(lic, ignoreRelation, context)
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

        return cleanUp(result, true, true)
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
     * @return Map | MainService.FORBIDDEN
     */
    def resolveOnixplLicense(OnixplLicense opl, License lic, Org context) {
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
            result.document = resolveDocument(opl.doc) // com.k_int.kbplus.Doc
            //result.licenses = resolveLicenseStubs(opl.licenses) // com.k_int.kbplus.License
            //result.xml = opl.xml // XMLDoc // TODO
            result = cleanUp(result, true, true)
        }

        return (hasAccess ? result : MainService.FORBIDDEN)
    }

    def resolveOrder(Order order) {
        def result = [:]
        if (!order) {
            return null
        }
        result.id           = order.id
        result.orderNumber  = order.orderNumber

        // References
        def context = null // TODO: use context
        result.owner        = resolveOrganisationStub(order.owner, context) // com.k_int.kbplus.Org

        return cleanUp(result, true, true)
    }

    def resolveOrgLinks(def list, ignoreRelationType, Org context) { // TODO
        def result = []

        list.each { it ->   // com.k_int.kbplus.OrgRole
            def tmp         = [:]
            tmp.endDate     = it.endDate
            tmp.startDate   = it.startDate

            // RefdataValues
            tmp.roleType    = it.roleType?.value

            // References
            if (it.org && (IGNORE_ORGANISATION != ignoreRelationType)) {
                tmp.organisation = resolveOrganisationStub(it.org, context) // com.k_int.kbplus.Org
            }
            if (it.cluster && (IGNORE_CLUSTER != ignoreRelationType)) {
                tmp.cluster = resolveClusterStub(it.cluster) // com.k_int.kbplus.Cluster
            }
            if (it.lic && (IGNORE_LICENSE != ignoreRelationType)) {
                tmp.license = resolveLicenseStub(it.lic, context) // com.k_int.kbplus.License
            }
            if (it.pkg && (IGNORE_PACKAGE != ignoreRelationType)) {
                tmp.package = resolvePackageStub(it.pkg, context) // com.k_int.kbplus.Package
            }
            if (it.sub && (IGNORE_SUBSCRIPTION != ignoreRelationType)) {
                tmp.subscription = resolveSubscriptionStub(it.sub, context) // com.k_int.kbplus.Subscription
            }
            if (it.title && (IGNORE_TITLE != ignoreRelationType)) {
                tmp.title = resolveTitleStub(it.title) // com.k_int.kbplus.TitleInstance
            }

            result << cleanUp(tmp, true, false)
        }
        result
    }

    def resolvePerson(Person prs, allowedContactTypes, allowedAddressTypes, Org context) {
        def result             = [:]

        if(prs) {
            result.globalUID       = prs.globalUID
            result.firstName       = prs.first_name
            result.middleName      = prs.middle_name
            result.lastName        = prs.last_name

            // RefdataValues
            result.gender          = prs.gender?.value
            result.isPublic        = prs.isPublic?.value

            // References
            result.contacts     = resolveContacts(prs.contacts, allowedContactTypes) // com.k_int.kbplus.Contact
            result.addresses    = resolveAddresses(prs.addresses, allowedAddressTypes) // com.k_int.kbplus.Address
            result.properties   = resolvePrivateProperties(prs.privateProperties, context) // com.k_int.kbplus.PersonPrivateProperty
        }
        return cleanUp(result, true, true)
    }

    /**
     * Access rights due wrapping object
     *
     * @param com.k_int.kbplus.Platform pform
     * @return
     */
    def resolvePlatform(Platform pform) {
        def result = [:]

        if (pform) {
            result.globalUID        = pform.globalUID
            result.impId            = pform.impId
            result.name             = pform.name
            result.normname         = pform.normname
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
        return cleanUp(result, true, true)
    }

    /**
     * Access rights due wrapping object
     */
    def resolvePlatformTipps(def list) {
        def result = []

        list.each { it -> // com.k_int.kbplus.PlatformTIPP
            def tmp = [:]
            tmp.titleUrl = it.titleUrl
            tmp.rel      = it.rel

            result << tmp
        }

        return cleanUp(result, true, true)
    }

    def resolvePrivateProperties(def list, Org context) { // TODO check context
        def result = []

        list.each { it ->       // com.k_int.kbplus.<x>PrivateProperty
            def tmp             = [:]
            tmp.name            = it.type?.name  // com.k_int.kbplus.PropertyDefinition.String
            tmp.description     = it.type?.descr // com.k_int.kbplus.PropertyDefinition.String
            //tmp.tenant          = resolveOrganisationStub(it.tenant, context) // com.k_int.kbplus.Org
            tmp.value           = (it.stringValue ? it.stringValue : (it.intValue ? it.intValue : (it.decValue ? it.decValue : (it.refValue?.value ? it.refValue?.value : null)))) // RefdataValue
            tmp.note            = it.note

            if(it.tenant?.id == context.id) {
                tmp.isPublic    = "No" // derived to substitute tentant
                result << cleanUp(tmp, true, false)
            }
        }
        result
    }

    def resolveProperties(def generic, Org context) {
        def cp = resolveCustomProperties(generic.customProperties)
        def pp = resolvePrivateProperties(generic.privateProperties, context)

        pp.each { cp << it }
        cp
    }

    def resolvePrsLinks(def list, allowedAddressTypes, allowedContactTypes, Org context) {  // TODO check context
        def result = []
        def tmp = []

        list.each { it ->

            // nested prs
            if(it.prs) {
                def x = it.prs.id
                def person = result.find {it.id == x}

                if(!person) {
                    person = resolvePerson(it.prs, allowedAddressTypes, allowedContactTypes, context) // com.k_int.kbplus.Person

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
                    person.roles << cleanUp(role, true, false)
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
     * @return Map
     */
    def resolveTipp(TitleInstancePackagePlatform tipp, def ignoreRelation, Org context) {
        def result = [:]
        if(!tipp) {
            return null
        }

        result.globalUID        = tipp.globalUID
        result.accessStartDate  = tipp.accessStartDate
        result.accessEndDate    = tipp.accessEndDate
        result.coreStatusStart  = tipp.coreStatusStart
        result.coreStatusEnd    = tipp.coreStatusEnd
        result.coverageDepth    = tipp.coverageDepth
        result.coverageNote     = tipp.coverageNote
        result.embargo          = tipp.embargo
        result.endDate          = tipp.endDate
        result.endVolume        = tipp.endVolume
        result.endIssue         = tipp.endIssue
        result.hostPlatformURL  = tipp.hostPlatformURL
        result.impId            = tipp.impId
        result.rectype          = tipp.rectype
        result.startDate        = tipp.startDate
        result.startIssue       = tipp.startIssue
        result.startVolume      = tipp.startVolume

        // RefdataValues
        result.status           = tipp.status?.value
        result.option           = tipp.option?.value
        result.delayedOA        = tipp.delayedOA?.value
        result.hybridOA         = tipp.hybridOA?.value
        result.statusReason     = tipp.statusReason?.value
        result.payment          = tipp.payment?.value

        // References
        result.additionalPlatforms = resolvePlatformTipps(tipp.additionalPlatforms) // com.k_int.kbplus.PlatformTIPP
        result.identifiers      = resolveIdentifiers(tipp.ids)       // com.k_int.kbplus.IdentifierOccurrence
        result.platform         = resolvePlatformStub(tipp.platform) // com.k_int.kbplus.Platform
        result.title            = resolveTitleStub(tipp.title)       // com.k_int.kbplus.TitleInstance

        if (ignoreRelation != IGNORE_ALL) {
            if (ignoreRelation != IGNORE_PACKAGE) {
                result.package = resolvePackageStub(tipp.pkg, context) // com.k_int.kbplus.Package
            }
            if (ignoreRelation != IGNORE_SUBSCRIPTION) {
                result.subscription = resolveSubscriptionStub(tipp.sub, context) // com.k_int.kbplus.Subscription
            }
        }
        //result.derivedFrom      = resolveTippStub(tipp.derivedFrom)  // com.k_int.kbplus.TitleInstancePackagePlatform
        //result.masterTipp       = resolveTippStub(tipp.masterTipp)   // com.k_int.kbplus.TitleInstancePackagePlatform

        return cleanUp(result, true, true)
    }

    /**
     * Access rights due wrapping object
     *
     * @param list
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return Map
     */
    def resolveTipps(def list, def ignoreRelation, Org context) {
        def result = []
        if(list) {
            list.each { it -> // com.k_int.kbplus.TitleInstancePackagePlatform
                result << resolveTipp(it, ignoreRelation, context)
            }
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

        return cleanUp(result, true, true)
    }
    */
}
