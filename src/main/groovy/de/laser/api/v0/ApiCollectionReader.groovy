package de.laser.api.v0


import de.laser.DocContext
import de.laser.Identifier
import de.laser.IssueEntitlement
import de.laser.Org
import de.laser.OrgRole
import de.laser.PersonRole
import de.laser.SubscriptionPackage
import de.laser.TitleInstancePackagePlatform
import de.laser.finance.CostItem
import de.laser.finance.CostItemGroup
import de.laser.properties.LicenseProperty
import de.laser.properties.PropertyDefinition
import de.laser.Address
import de.laser.Contact
import de.laser.oap.OrgAccessPoint
import de.laser.api.v0.entities.ApiDoc
import de.laser.api.v0.entities.ApiIssueEntitlement
import de.laser.IssueEntitlementCoverage
import de.laser.storage.RDStore
import groovy.util.logging.Slf4j

/**
 * This class delivers given lists as maps of stubs or full objects
 */
@Slf4j
class ApiCollectionReader {

    /**
     * Processes a collection of addresses and builds a collection of entries for API output
     * @param list the {@link Collection} of {@link Address}es to output
     * @param allowedTypes the conditions which permit output of a value
     * @return a {@link Collection} of address details
     */
    static Collection<Object> getAddressCollection(Collection<Address> list, allowedTypes) {
        Collection<Object> result = []

        list.each { it ->   // de.laser.Address
            Map<String, Object> tmp = [:]

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
            tmp.lastUpdated     = ApiToolkit.formatInternalDate(it.lastUpdated)

            // RefdataValues
            tmp.region       = it.region?.value
            tmp.country     = it.country?.value
            tmp.type        = it.type?.collect{ it.value }

            tmp = ApiToolkit.cleanUp(tmp, true, false)

            if(ApiReader.NO_CONSTRAINT == allowedTypes || (allowedTypes.findAll{it in it.type?.value} != [])) {
                result << tmp
            }
        }
        result
    }

    /**
     * Processes a collection of contacts and builds a collection of entries for API output
     * @param list the {@link Collection} of {@link Contact}s to output
     * @param allowedTypes the conditions which permit output of a value
     * @return a {@link Collection} of contact details
     */
    static Collection<Object> getContactCollection(Collection<Contact> list, allowedTypes) {
        Collection<Object> result = []

        list.each { it ->       // de.laser.Contact
            Map<String, Object> tmp = [:]

            tmp.content         = it.content
            tmp.lastUpdated     = ApiToolkit.formatInternalDate(it.lastUpdated)

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

    /**
     * Builds a map of cost item API records from the filtered list of {@link CostItem}s
     * @param filteredList a pre-filtered and cleared list of public {@link CostItem}s
     * @return a {@link Collection} of {@link Map}s of cost item records for API output
     */
    static Collection<Object> getCostItemCollection(Collection<CostItem> filteredList) {
        Collection<Object> result = []

        filteredList.each { it ->

            Map<String, Object> tmp     = [:]

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

            tmp.calculatedType      = it._getCalculatedType()
            tmp.datePaid            = ApiToolkit.formatInternalDate(it.datePaid)
            tmp.invoiceDate         = ApiToolkit.formatInternalDate(it.invoiceDate)
            tmp.financialYear       = it.financialYear
            tmp.startDate           = ApiToolkit.formatInternalDate(it.startDate)
            tmp.endDate             = ApiToolkit.formatInternalDate(it.endDate)
            tmp.dateCreated         = ApiToolkit.formatInternalDate(it.dateCreated)
            tmp.lastUpdated         = ApiToolkit.formatInternalDate(it.lastUpdated)
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
            tmp.invoiceNumber       = it.invoice?.invoiceNumber // retrieveInvoiceMap(it.invoice) // de.laser.finance.Invoice
            // tmp.issueEntitlement    = ApiIssueEntitlement.retrieveIssueEntitlementMap(it.issueEntitlement, ApiReader.IGNORE_ALL, context) // de.laser.IssueEntitlement
            tmp.orderNumber         = it.order?.orderNumber // retrieveOrderMap(it.order) // de.laser.finance.Order
            // tmp.owner               = ApiStubReader.retrieveOrganisationStubMap(it.owner, context) // com.k_int.kbplus.Org
            // tmp.sub                 = ApiStubReader.requestSubscriptionStub(it.sub, context) // com.k_int.kbplus.Subscription // RECURSION ???
            // tmp.package             = ApiStubReader.retrieveSubscriptionPackageStubMixed(it.subPkg, ApiReader.IGNORE_SUBSCRIPTION, context) // de.laser.SubscriptionPackage
            //tmp.surveyOrg
            //tmp.subPkg

            result << ApiToolkit.cleanUp(tmp, true, true)
        }

        result
    }

    /**
     * Builds a collection of custom (= general) properties for the given object and respecting the settings of the requestor institution
     * @param list the {@link Collection} of properties to enumerate
     * @param generic the object (one of {@link de.laser.Subscription}, {@link de.laser.License}, {@link Org}, {@link de.laser.Person} or {@link de.laser.Platform})
     * @param context the requestor institution ({@link Org})
     * @return a {@link Collection} of {@link Map}s containing property details for API output
     */
    static Collection<Object> getCustomPropertyCollection(Collection<Object> list, def generic, Org context) {
        Collection<Object> result = []

        if (generic.metaClass.getMetaMethod("getCalculatedPropDefGroups")) {
            def groups = generic.getCalculatedPropDefGroups(context)
            List tmp = []

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

        list.each { it ->       // com.k_int.kbplus.<x>CustomProperty
            Map<String, Object> tmp = [:]

            tmp.token       = it.type?.name     // de.laser.properties.PropertyDefinition.String
            tmp.scope       = it.type?.descr    // de.laser.properties.PropertyDefinition.String
            tmp.note        = it.note
            tmp.isPublic    = "Yes" // derived to substitute private properties tentant

            if (it.dateValue) {
                tmp.value   = ApiToolkit.formatInternalDate(it.dateValue)
            }
            else {
                tmp.value   = (it.stringValue ?: (it.intValue ?: (it.decValue ?: (it.refValue?.value ?: (it.urlValue ?: null))))) // RefdataValue
            }

            tmp.type = PropertyDefinition.validTypes[it.type.type]['en']

            if (it.type.isRefdataValueType()) {
                tmp.refdataCategory = it.type.refdataCategory
            }

            if (it instanceof LicenseProperty) {
                tmp.paragraph = it.paragraph
            }
            tmp = ApiToolkit.cleanUp(tmp, true, false)
            result << tmp
        }
        result
    }

    /**
     * Builds a collection of document records for API output
     * @param list a {@link List} of document relations outgoing from the given object
     * @return a {@link Collection} of document map stubs
     * @see DocContext
     * @see ApiDoc#getDocumentMap(de.laser.Doc)
     */
    static Collection<Object> getDocumentCollection(Collection<DocContext> list) {
        Collection<Object> result = []
        list.each { it -> // de.laser.DocContext
            result << ApiDoc.getDocumentMap(it.owner)
        }
        result
    }

    /**
     * Builds a collection of identifier records for API output
     * @param list a {@link List} of {@link Identifier}s
     * @return a {@link Collection} of identifier namespace:value pairs
     */
    static Collection<Object> getIdentifierCollection(Collection<Identifier> list) {
        Collection<Object> result = []
        list.each { it ->   // de.laser.Identifier
            Map<String, Object> tmp = [:]

            tmp.put( 'namespace', it.ns?.ns )
            tmp.put( 'value', it.value )

            tmp = ApiToolkit.cleanUp(tmp, true, true)
            result << tmp
        }
        result
    }

    /**
     * Puts together a collection of issue entitlement stubs which belong to the given subscription package
     * @param subPkg the {@link de.laser.SubscriptionPackage} to process
     * @param ignoreRelation should relations followed up (and stubs returned) or not?
     * @param context the requesting institution ({@link Org}) whose perspective should be taken
     * @return a {@link Collection<Object>} reflecting the result
     */
    static Collection<Object> getIssueEntitlementCollection(SubscriptionPackage subPkg, ignoreRelation, Org context){
        Collection<Object> result = []

        List<IssueEntitlement> ieList = IssueEntitlement.executeQuery(
                'select ie from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub join tipp.pkg pkg ' +
                        ' where sub = :sub and pkg = :pkg and tipp.status != :statusTipp and ie.status != :statusIe',
                [sub: subPkg.subscription, pkg: subPkg.pkg, statusTipp: RDStore.TIPP_STATUS_DELETED, statusIe: RDStore.TIPP_STATUS_DELETED]
        )
        ieList.each{ ie ->
            result << ApiIssueEntitlement.getIssueEntitlementMap(ie, ignoreRelation, context) // de.laser.IssueEntitlement
        }

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Builds a collection of maps reflecting the given {@link IssueEntitlementCoverage} collection for API output
     * @param list a {@link Collection} of {@link IssueEntitlementCoverage}s to be processed for API output
     * @return a {@link Collection} of {@link Map}s reflecting the issue entitlement coverage collection
     */
    static Collection<Object> getIssueEntitlementCoverageCollection(Collection<IssueEntitlementCoverage> list) {
        Collection<Object> result = []

        list?.each { it -> // de.laser.IssueEntitlementCoverage
            result << ApiUnsecuredMapReader.getIssueEntitlementCoverageMap(it)
        }

        result
    }

    /**
     * Delivers a package stub map with the issue entitlements (!) belonging to each package subscribed
     * @param list the {@link Collection} of {@link SubscriptionPackage}s which should be returned along with the respective holdings
     * @param context the requesting institution ({@link Org}) whose perspective is going to be taken during the checks
     * @return a {@link Collection<Object>} reflecting the packages and holdings
    */
    static Collection<Object> getPackageWithIssueEntitlementsCollection(Collection<SubscriptionPackage> list, Org context) {  // TODO - TODO - TODO
        Collection<Object> result = []

        list.each { subPkg ->
            Map<String, Object> pkg = ApiUnsecuredMapReader.getPackageStubMap(subPkg.pkg) // com.k_int.kbplus.Package
            result << pkg

            //if (pkg != Constants.HTTP_FORBIDDEN) {
                pkg.issueEntitlements = getIssueEntitlementCollection(subPkg, ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE, context)
            //}
        }

        return ApiToolkit.cleanUp(result, true, false)
    }

    /**
     * Collects the information on the other ends of the outgoing organisation relations list and returns the stub map
     * of informations about the objects linked
     * @param list a {@link List} of {@link OrgRole}s to process
     * @param ignoreRelationType should futher relations be ignored?
     * @param context the requestor institution ({@link Org}) whose perspective is going to be taken during the checks
     * @return a {@link Collection} of map entries reflecting the information about the outgoing relations
     */
    static Collection<Object> getOrgLinkCollection(Collection<OrgRole> list, ignoreRelationType, Org context) { // TODO
        Collection<Object> result = []

        list.each { it ->   // de.laser.OrgRole
            Map<String, Object> tmp = [:]

            tmp.endDate     = ApiToolkit.formatInternalDate(it.endDate)
            tmp.startDate   = ApiToolkit.formatInternalDate(it.startDate)

            // RefdataValues
            tmp.roleType    = it.roleType?.value

            // References
            if (it.org && (ApiReader.IGNORE_ORGANISATION != ignoreRelationType)) {
                tmp.organisation = ApiUnsecuredMapReader.getOrganisationStubMap(it.org) // com.k_int.kbplus.Org
            }
            if (it.lic && (ApiReader.IGNORE_LICENSE != ignoreRelationType)) {
                tmp.license = ApiStubReader.requestLicenseStub(it.lic, context) // com.k_int.kbplus.License
            }
            if (it.pkg && (ApiReader.IGNORE_PACKAGE != ignoreRelationType)) {
                tmp.package = ApiUnsecuredMapReader.getPackageStubMap(it.pkg) // com.k_int.kbplus.Package
            }
            if (it.sub && (ApiReader.IGNORE_SUBSCRIPTION != ignoreRelationType)) {
                tmp.subscription = ApiStubReader.requestSubscriptionStub(it.sub, context) // com.k_int.kbplus.Subscription
            }
            if (it.tipp && (ApiReader.IGNORE_TITLE != ignoreRelationType)) {
                tmp.title = ApiUnsecuredMapReader.getTitleStubMap(it.tipp) // de.laser.titles.TitleInstancePackagePlatform
            }

            result << ApiToolkit.cleanUp(tmp, true, false)
        }
        result
    }

    /**
     * Builds a collection of map entries reflecting the given {@link Collection} of {@link OrgAccessPoint}s
     * @param list a {@link Collection} of {@link OrgAccessPoint}s to process
     * @return a {@link Collection} of entries reflecting the access point records
     */
    static Collection<Object> getOrgAccessPointCollection(Collection<OrgAccessPoint> list) {
        Collection<Object> result = []
        list.each { it -> // com.k_int.kbplus.OrgAccessPoint
            result << ApiUnsecuredMapReader.getOrgAccessPointStubMap(it)
        }
        result
    }

    /**
     * Builds a collection of private properties for the given object and respecting the settings of the requestor institution
     * @param list the {@link Collection} of properties to enumerate
     * @param context the requestor institution ({@link Org})
     * @return a {@link Collection} of {@link Map}s containing property details for API output
     */
    static Collection<Object> getPrivatePropertyCollection(Collection list, Org context) {
        Collection<Object> result = []

        list.findAll{ (it.owner.id == context.id || it.type.tenant?.id == context.id) && it.tenant?.id == context.id && it.isPublic == false }?.each { it ->       // com.k_int.kbplus.<x>PrivateProperty
            Map<String, Object> tmp = [:]

            tmp.token   = it.type.name     // de.laser.properties.PropertyDefinition.String
            tmp.scope   = it.type.descr    // de.laser.properties.PropertyDefinition.String
            tmp.note    = it.note
            //tmp.tenant          = ApiStubReader.resolveOrganisationStub(it.tenant, context) // com.k_int.kbplus.Org

            if (it.dateValue) {
                tmp.value   = ApiToolkit.formatInternalDate(it.dateValue)
            }
            else {
                tmp.value   = (it.stringValue ?: (it.intValue ?: (it.decValue ?: (it.refValue?.value ?: (it.urlValue ?: null))))) // RefdataValue
            }

            tmp.type = PropertyDefinition.validTypes[it.type.type]['en']

            if (it.type.isRefdataValueType()) {
                tmp.refdataCategory = it.type.refdataCategory
            }

            //tmp.dateCreated = ApiToolkit.formatInternalDate(it.dateCreated)
            //tmp.lastUpdated = ApiToolkit.formatInternalDate(it._getCalculatedLastUpdated())

            if (it instanceof LicenseProperty) {
                tmp.paragraph = it.paragraph
            }

            if(it.type.tenant?.id == context.id) {
                tmp.isPublic    = "No" // derived to substitute tentant
                result << ApiToolkit.cleanUp(tmp, true, false)
            }
        }
        result
    }

    /**
     * Collects the properties (general and private) of the given object and outputs the collections
     * @param generic the object (one of {@link de.laser.Subscription}, {@link de.laser.License}, {@link Org}, {@link de.laser.Package} or {@link de.laser.Platform})
     * @param context the requesting institution ({@link Org}) whose perspective is going to be taken during checks
     * @param ignoreFlag should certain properties being left out from output (private or custom)?
     * @return a {@link Collection} of both general and private properties
     */
    static Collection<Object> getPropertyCollection(Object generic, Org context, def ignoreFlag) {
        Collection<Object> cp = getCustomPropertyCollection(generic.propertySet, generic, context)
        Collection<Object> pp = getPrivatePropertyCollection(generic.propertySet, context)

        if (ignoreFlag == ApiReader.IGNORE_CUSTOM_PROPERTIES) {
            return pp
        }
        else if (ignoreFlag == ApiReader.IGNORE_PRIVATE_PROPERTIES) {
            return cp
        }

        pp.each { cp << it }
        cp
    }

    /**
     * Builds a list of entries showing person links from a certain object
     * @param list a {@link List} of links pointing to contact entities
     * @param allowedAddressTypes the types of addresses which can be returned
     * @param allowedContactTypes the types of contacts which can be returned
     * @param context the requesting institution ({@link Org}) whose perspective is going to be taken during checks
     * @return a {@link List} of map entries reflecting the contact entity details
     * @see de.laser.Person
     */
    static Collection<Object> getPrsLinkCollection(Collection<PersonRole> list, allowedAddressTypes, allowedContactTypes, Org context) {  // TODO check context
        List result = []
        List tmp = []

        list.each { it ->

            // nested prs
            if(it.prs) {
                String x = it.prs.globalUID
                def person = tmp.find {it.globalUID == x}

                if(!person) {
                    person = ApiMapReader.getPersonMap(it.prs, allowedAddressTypes, allowedContactTypes, context) // de.laser.Person

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

                Map<String, Object> role    = [:] // de.laser.PersonRole
                role.startDate              = ApiToolkit.formatInternalDate(it.start_date)
                role.endDate                = ApiToolkit.formatInternalDate(it.end_date)

                // RefdataValues
                role.functionType           = it.functionType?.value
                role.positionType           = it.positionType?.value

                if(! person.roles) {
                    person.roles = []
                }
                if (role.functionType || role.positionType) {
                    person.roles << ApiToolkit.cleanUp(role, true, false)
                }


                // TODO responsibilityType
                /*if (role.responsibilityType) {
                    // References
                    //if (it.org) {
                    //    role.organisation = ApiStubReader.resolveOrganisationStub(it.org, context) // com.k_int.kbplus.Org
                    //}

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
                        role.title = ApiStubReader.resolveTitleStub(it.title) // de.laser.titles.TitleInstance
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
     * Builds a collection of title entries for API output.
     * Access rights due wrapping object
     * @param list a {@link Collection} of {@link TitleInstancePackagePlatform}
     * @param ignoreRelation should further relations be followed up?
     * @param context the requesting institution ({@link Org}) whose perspective is going to be taken during checks
     * @return a {@link Collection<Object>} reflecting the titles in the list
     */
    static Collection<Object> getTippCollection(Collection<TitleInstancePackagePlatform> list, def ignoreRelation, Org context) {
        Collection<Object> result = []

        list.each { it -> // de.laser.TitleInstancePackagePlatform
            result << ApiMapReader.getTippMap(it, ignoreRelation, context)
        }

        result
    }

    /**
     * Collects for the given list of subscription packages the stubs for API output
     * @param list a {@link List} of {@link SubscriptionPackage}s to be processed
     * @param ignoreRelation should further relations being followed up?
     * @param context the requesting institution ({@link Org}) whose prespective is going to be taken for checks
     * @return a {@link Collection} of maps reflecting the packages with their entitlements
     */
    static Collection<Object> getSubscriptionPackageStubCollection(Collection<SubscriptionPackage> list, def ignoreRelation, Org context) {
        Collection<Object> result = []

        if (! list) {
            return null
        }

        list.each { it -> // de.laser.SubscriptionPackage
            result << ApiStubReader.requestSubscriptionPackageStubMixed(it, ignoreRelation, context)
        }
        result
    }
}
