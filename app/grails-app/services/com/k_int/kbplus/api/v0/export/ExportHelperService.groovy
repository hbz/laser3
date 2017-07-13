package com.k_int.kbplus.api.v0.export

import com.k_int.kbplus.*
import groovy.util.logging.Log4j

@Log4j
class ExportHelperService {

    final static NO_CONSTRAINT          = "NO_CONSTRAINT"

    // type of stub to return
    final static LICENSE_STUB           = "LICENSE_STUB"
    final static PACKAGE_STUB           = "PACKAGE_STUB"
    final static SUBSCRIPTION_STUB      = "SUBSCRIPTION_STUB"

    // resolving type
    final static SUBPKG_PACKAGE         = "SUBPKG_PACKAGE"
    final static SUBPKG_SUBSCRIPTION    = "SUBPKG_SUBSCRIPTION"

    // ignoring relation source
    final static IGNORE_CLUSTER         = "IGNORE_CLUSTER"
    final static IGNORE_LICENSE         = "IGNORE_LICENSE"
    final static IGNORE_ORGANISATION    = "IGNORE_ORGANISATION"
    final static IGNORE_PACKAGE         = "IGNORE_PACKAGE"
    final static IGNORE_SUBSCRIPTION    = "IGNORE_SUBSCRIPTION"
    final static IGNORE_TITLE           = "IGNORE_TITLE"

    // ################### HELPER ###################

    def hasAccess(def orgRoles, Org context) {
        def hasAccess = false

        orgRoles?.each { orgRole ->
            // TODO check orgRole.roleType
            if (orgRole.getOrg().id == context?.id) {
                hasAccess = true
            }
        }

        hasAccess
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
            result.definition   = cluster.definition
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
            result.id           = lic.id
            result.contact      = lic.contact
            result.licenseUrl   = lic.licenseUrl
            result.reference    = lic.reference

            result = cleanUp(result, true, true)
        }

        return (hasAccess ? result : ApiService.FORBIDDEN)
    }

    /**
     *
     * @param com.k_int.kbplus.Org org
     * @param com.k_int.kbplus.Org context
     * @return MAP | ApiService.FORBIDDEN
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
            result.id             = org.id
            result.name           = org.name
            result.shortcode      = org.shortcode

            // References
            result.identifiers    = resolveIdentifiers(org.ids) // com.k_int.kbplus.IdentifierOccurrence
            result = cleanUp(result, true, true)
        }

        return (hasAccess ? result : ApiService.FORBIDDEN)
    }

    /**
     *
     * @param com.k_int.kbplus.Package pkg
     * @param com.k_int.kbplus.Org context
     * @return MAP | ApiService.FORBIDDEN
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
            result.id           = pkg.id
            result.name         = pkg.name
            result.identifier   = pkg.identifier
            result.vendorURL    = pkg.vendorURL

            result = cleanUp(result, true, true)
        }

        return (hasAccess ? result : ApiService.FORBIDDEN)
    }

    def resolvePlatformStub(Platform pform) {
        def result = [:]
        if(pform) {
            result.id           = pform.id
            result.name         = pform.name
            result.normname     = pform.normname
        }
        return cleanUp(result, true, true)
    }

    /**
     *
     * @param com.k_int.kbplus.Subscription sub
     * @param com.k_int.kbplus.Org context
     * @return MAP | ApiService.FORBIDDEN
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
            result.id           = sub.id
            result.name         = sub.name
            result.identifier   = sub.identifier

            // References
            result.identifiers  = resolveIdentifiers(sub.ids) // com.k_int.kbplus.IdentifierOccurrence

            result = cleanUp(result, true, true)
        }

        return (hasAccess ? result : ApiService.FORBIDDEN)
    }

    def resolveSubscriptionPackageStub(SubscriptionPackage subpkg, resolver, Org context) {
        if(subpkg) {
            if(SUBPKG_PACKAGE == resolver) {
                return resolvePackageStub(subpkg.pkg, context)
            }
            else if(SUBPKG_SUBSCRIPTION == resolver) {
                return resolveSubscriptionStub(subpkg.subscription, context)
            }
        }
        return null
    }

    def resolveSubscriptionPackageStubs(def list, def resolver, Org context) {
        def result = [:]
        if (!list) {
            return null
        }

        list.each { it -> // com.k_int.kbplus.SubscriptionPackage
            result << resolveSubscriptionPackageStub(it, resolver, context)
        }
        result
    }

    def resolveTippStub(TitleInstancePackagePlatform tipp) {
        def result = [:]
        if(tipp) {
            result.id           = tipp.id

            // References
            result.identifiers  = resolveIdentifiers(tipp.ids) // com.k_int.kbplus.IdentifierOccurrence
        }

        return cleanUp(result, true, true)
    }

    def resolveTitleStub(TitleInstance title) {
        def result = [:]

        result.id             = title.id
        result.title          = title.title
        result.normTitle      = title.normTitle

        // References
        result.identifiers  = resolveIdentifiers(title.ids) // com.k_int.kbplus.IdentifierOccurrence

        return cleanUp(result, true, true)
    }

    // ################### FULL OBJECTS ###################

    def resolveAddresses(list, allowedTypes) {
        def result = []

        list.each { it ->   // com.k_int.kbplus.Address
            def tmp         = [:]
            tmp.id          = it.id
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
            result.organisations    = resolveOrgLinks(cluster.orgs, IGNORE_CLUSTER) // com.k_int.kbplus.OrgRole
            // TODO
            result.persons          = resolvePrsLinks(
                    cluster.prsLinks, allowedAddressTypes, allowedContactTypes, true, true
            ) // com.k_int.kbplus.PersonRole
        }
        return cleanUp(result, true, true)
    }

    def resolveContacts(list, allowedTypes) {
        def result = []

        list.each { it ->       // com.k_int.kbplus.Contact
            def tmp             = [:]
            tmp.id              = it.id
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
            tmp.invoice             = resolveInvoice(it.invoice)                // com.k_int.kbplus.Invoice
            tmp.issueEntitlement    = resolveIssueEntitlement(it.issueEntitlement) // com.k_int.kbplus.issueEntitlement
            tmp.order               = resolveOrder(it.order)                    // com.k_int.kbplus.Order
            tmp.owner               = resolveOrganisationStub(it.owner)         // com.k_int.kbplus.Org
            tmp.sub                 = resolveSubscriptionStub(it.sub)           // com.k_int.kbplus.Subscription // RECURSION ???
            tmp.package             = resolveSubscriptionPackageStub(it.subPkg, SUBPKG_PACKAGE) // com.k_int.kbplus.SubscriptionPackage
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
            tmp.id              = it.id
            tmp.name            = it.type?.name     // com.k_int.kbplus.PropertyDefinition.String
            tmp.description     = it.type?.descr    // com.k_int.kbplus.PropertyDefinition.String
            tmp.value           = (it.stringValue ? it.stringValue : (it.intValue ? it.intValue : (it.decValue ? it.decValue : (it.refValue?.value ? it.refValue?.value : null)))) // RefdataValue
            tmp.note            = it.note

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

    // valid
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
        result.owner               = resolveOrganisationStub(invoice.owner) // com.k_int.kbplus.Org

        return cleanUp(result, true, true)
    }

    def resolveIssueEntitlement(IssueEntitlement ie) {
        // TODO: implement
        def result = [:]
        if (!ie) {
            return null
        }

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
        result.subscription     = resolveSubscriptionStub(ie.subscription) // com.k_int.kbplus.Subscription
        result.tipp             = resolveTipp(ie.subscription) // com.k_int.kbplus.TitleInstancePackagePlatform

        //return cleanUp(result, true, true)
        result
    }

    def resolveIssueEntitlements(list) {
        def result = []
        if(list) {
            list.each { it -> // com.k_int.kbplus.IssueEntitlement
                result << resolveIssueEntitlement(it)
            }
        }
        result
    }

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

        result.fromLic  = resolveLicenseStub(link.fromLic) // com.k_int.kbplus.License
        result.toLic    = resolveLicenseStub(link.toLic) // com.k_int.kbplus.License

        return cleanUp(result, true, true)
    }

    def resolveLinks(list) {
        def result = []
        if(list) {
            list.each { it -> // com.k_int.kbplus.Link
                result << resolveLink(it)
            }
        }
        result
    }

    /**
     * Access rights due wrapping license
     *
     * @param com.k_int.kbplus.OnixplLicense opl
     * @param com.k_int.kbplus.License lic
     * @param com.k_int.kbplus.Org context
     * @return Map | ApiService.FORBIDDEN
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
            result.id       = opl.id
            result.lastmod  = opl.lastmod
            result.title    = opl.title

            // References
            result.document = resolveDocument(opl.doc) // com.k_int.kbplus.Doc
            //result.licenses = resolveLicenseStubs(opl.licenses) // com.k_int.kbplus.License
            //result.xml = opl.xml // XMLDoc // TODO
            result = cleanUp(result, true, true)
        }

        return (hasAccess ? result : ApiService.FORBIDDEN)
    }

    // valid
    def resolveOrder(Order order) {
        def result = [:]
        if (!order) {
            return null
        }
        result.id           = order.id
        result.orderNumber  = order.orderNumber

        // References
        result.owner        = resolveOrganisationStub(order.owner) // com.k_int.kbplus.Org

        return cleanUp(result, true, true)
    }

    def resolveOrgLinks(def list, ignoreRelationType, Org context) { // TODO
        def result = []

        list.each { it ->   // com.k_int.kbplus.OrgRole
            def tmp         = [:]
            tmp.endDate     = it.endDate
            tmp.startDate   = it.startDate
            tmp.title       = it.title

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

    @Deprecated
    def resolvePerson(Person prs, allowedContactTypes, allowedAddressTypes) {
        def result             = [:]

        if (prs) {
            result.id              = prs.id
            result.firstName       = prs.first_name
            result.middleName      = prs.middle_name
            result.lastName        = prs.last_name

            // RefdataValues
            result.gender          = prs.gender?.value
            result.isPublic        = prs.isPublic?.value

            // References
            result.contacts     = resolveContacts(prs.contacts, allowedContactTypes) // com.k_int.kbplus.Contact
            result.addresses    = resolveAddresses(prs.addresses, allowedAddressTypes) // com.k_int.kbplus.Address
            // TODO: result.properties   = resolvePrivateProperties(prs.privateProperties) // com.k_int.kbplus.PersonPrivateProperty
        }
        return cleanUp(result, true, true)
    }
    // NEU
    def resolvePerson(Person prs, allowedContactTypes, allowedAddressTypes, Org context) {
        def result             = [:]

        if(prs) {
            result.id              = prs.id
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

    def resolvePlatform(Platform pform) {
        def result = [:]

        if (pform) {
            result.id               = pform.id
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

    @Deprecated
    def resolvePrivateProperties(list) {
        def result = []

        list.each { it ->       // com.k_int.kbplus.<x>PrivateProperty
            def tmp             = [:]
            tmp.id              = it.id
            tmp.name            = it.type?.name  // com.k_int.kbplus.PropertyDefinition.String
            tmp.description     = it.type?.descr // com.k_int.kbplus.PropertyDefinition.String
            tmp.tenant          = resolveOrganisationStub(it.tenant) // com.k_int.kbplus.Org
            tmp.value           = (it.stringValue ? it.stringValue : (it.intValue ? it.intValue : (it.decValue ? it.decValue : (it.refValue?.value ? it.refValue?.value : null)))) // RefdataValue
            tmp.note            = it.note

            tmp = cleanUp(tmp, true, false)
            result << tmp
        }
        result
    }
    // NEU
    def resolvePrivateProperties(def list, Org context) { // TODO check context
        def result = []

        list.each { it ->       // com.k_int.kbplus.<x>PrivateProperty
            def tmp             = [:]
            tmp.id              = it.id
            tmp.name            = it.type?.name  // com.k_int.kbplus.PropertyDefinition.String
            tmp.description     = it.type?.descr // com.k_int.kbplus.PropertyDefinition.String
            tmp.tenant          = resolveOrganisationStub(it.tenant) // com.k_int.kbplus.Org
            tmp.value           = (it.stringValue ? it.stringValue : (it.intValue ? it.intValue : (it.decValue ? it.decValue : (it.refValue?.value ? it.refValue?.value : null)))) // RefdataValue
            tmp.note            = it.note

            if(it.tenant?.id == context.id) {
                result << cleanUp(tmp, true, false)
            }
        }
        result
    }

    @Deprecated
    def resolveProperties(object) {
        def cp = resolveCustomProperties(object.customProperties)
        def pp = resolvePrivateProperties(object.privateProperties)

        pp.each { cp << it }
        cp
    }
    // NEU
    def resolveProperties(def generic, Org context) {
        def cp = resolveCustomProperties(generic.customProperties)
        def pp = resolvePrivateProperties(generic.privateProperties, context)

        pp.each { cp << it }
        cp
    }

    @Deprecated
    def resolvePrsLinks(list, allowedAddressTypes, allowedContactTypes, showFunctions, showResponsibilities) {
        def result = []

        list.each { it ->

            // export only if function and/or responsiblity is given
            if((showFunctions && it.functionType?.value) || (showResponsibilities && it.responsibilityType?.value)) {

                // nested prs
                if(it.prs) {
                    def x = it.prs.id
                    def person = result.find {it.id == x}

                    if(!person) {
                        person = resolvePerson(it.prs, allowedAddressTypes, allowedContactTypes) // com.k_int.kbplus.Person

                        // export only public
                        if("No" != person.isPublic?.value) {
                            result << person
                        }
                        // TODO check if isPublic == false && membership@tenant
                    }

                    if(!person.roles) {
                        person.roles = []
                    }

                    def role                    = [:] // com.k_int.kbplus.PersonRole
                    role.id                     = it.id
                    role.startDate              = it.start_date
                    role.endDate                = it.end_date

                    // RefdataValues
                    role.functionType           = it.functionType?.value
                    role.responsibilityType     = it.responsibilityType?.value

                    role = cleanUp(role, true, false)
                    person.roles << role

                    // References
                    if (it.org) {
                        role.organisation = resolveOrganisationStub(it.org) // com.k_int.kbplus.Org
                    }
                    if (it.cluster) {
                        role.cluster = resolveClusterStub(it.cluster) // com.k_int.kbplus.Cluster
                    }
                    if (it.lic) {
                        role.license = resolveLicenseStub(it.lic) // com.k_int.kbplus.License
                    }
                    if (it.pkg) {
                        role.package = resolvePackageStub(it.pkg) // com.k_int.kbplus.Package
                    }
                    if (it.sub) {
                        role.subscription = resolveSubscriptionStub(it.sub) // com.k_int.kbplus.Subscription
                    }
                    if (it.title) {             // com.k_int.kbplus.???
                        role.title               = [:]
                        role.title.id            = it.title?.id
                        role.title.title         = it.title?.title
                        role.title.normTitle     = it.title?.normTitle

                        role.title = cleanUp(role.title, true, false)
                    }
                }
            }
        }
        result
    }

    // NEU
    def resolvePrsLinks(def list, allowedAddressTypes, allowedContactTypes, Org context) {  // TODO check context
        def result = []

        list.each { it ->

            // nested prs
            if(it.prs) {
                def x = it.prs.id
                def person = result.find {it.id == x}

                if(!person) {
                    person = resolvePerson(it.prs, allowedAddressTypes, allowedContactTypes, context) // com.k_int.kbplus.Person

                    // export public
                    if("No" != person.isPublic?.value?.toString()) {
                        result << person
                    }
                    // or private if tenant = context
                    else {
                        if(it.prs.tenant?.id == context.id) {
                            result << person
                        }
                    }
                }

                if(!person.roles) {
                    person.roles = []
                }

                def role                    = [:] // com.k_int.kbplus.PersonRole
                role.id                     = it.id
                role.startDate              = it.start_date
                role.endDate                = it.end_date

                // RefdataValues
                role.functionType           = it.functionType?.value
                role.responsibilityType     = it.responsibilityType?.value

                role = cleanUp(role, true, false)
                person.roles << role

                // References
                if (it.org) {
                    role.organisation = resolveOrganisationStub(it.org) // com.k_int.kbplus.Org
                }
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
            }
        }
        result
    }

    def resolveTipp(TitleInstancePackagePlatform tipp) {
        def result = [:]
        if(!tipp) {
            return null
        }
        result.id               = tipp.id
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
        //result.additionalPlatforms  = tipp.additionalPlatforms          // com.k_int.kbplus.PlatformTIPP

        result.identifiers      = resolveIdentifiers(tipp.ids)          // com.k_int.kbplus.IdentifierOccurrence
        result.package          = resolvePackageStub(tipp.pkg)          // com.k_int.kbplus.Package
        result.platform         = resolvePlatformStub(tipp.platform)    // com.k_int.kbplus.Platform
        result.title            = resolveTitle(tipp.title)              // com.k_int.kbplus.TitleInstance
        result.subscription     = resolveSubscriptionStub(tipp.sub)     // com.k_int.kbplus.Subscription

        result.derivedFrom      = resolveTippStub(tipp.derivedFrom) // com.k_int.kbplus.TitleInstancePackagePlatform
        result.masterTipp       = resolveTippStub(tipp.masterTipp)  // com.k_int.kbplus.TitleInstancePackagePlatform

        return cleanUp(result, true, true)
    }

    def resolveTipps(list) {
        def result = []
        if(list) {
            list.each { it -> // com.k_int.kbplus.TitleInstancePackagePlatform
                result << resolveTipp(it)
            }
        }
        result
    }

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
        result.status         = title.status?.value
        result.type           = title.type?.value

        // References
        result.identifiers  = resolveIdentifiers(title.ids) // com.k_int.kbplus.IdentifierOccurrence

        // TODO
        //tipps:  TitleInstancePackagePlatform,
        //orgs:   OrgRole,
        //historyEvents: TitleHistoryEventParticipant,
        //prsLinks: PersonRole

        return cleanUp(result, true, true)
    }

    /**
     *
     * @param list
     * @param removeEmptyValues
     * @param removeEmptyLists
     * @return
     */
    def cleanUp(list, removeNullValues, removeEmptyLists) {
        Collection<String> values = list.values()

        if(removeNullValues){
            while (values.remove(null));
        }
        if(removeEmptyLists){
            while (values.remove([]));
        }
        list
    }
}
