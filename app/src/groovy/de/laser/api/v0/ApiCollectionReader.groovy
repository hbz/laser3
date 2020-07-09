package de.laser.api.v0

import com.k_int.kbplus.*
import com.k_int.properties.PropertyDefinition
import de.laser.api.v0.entities.ApiDoc
import de.laser.api.v0.entities.ApiIssueEntitlement
import de.laser.IssueEntitlementCoverage
import de.laser.helper.RDStore
import groovy.util.logging.Log4j

@Log4j
class ApiCollectionReader {

    static Collection<Object> getAddressCollection(Collection<Address> list, allowedTypes) {
        Collection<Object> result = []

        list.each { it ->   // com.k_int.kbplus.Address
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
            tmp.type        = it.type?.value

            tmp = ApiToolkit.cleanUp(tmp, true, false)

            if(ApiReader.NO_CONSTRAINT == allowedTypes || allowedTypes.contains(it.type?.value)) {
                result << tmp
            }
        }
        result
    }

    static Collection<Object> getContactCollection(Collection<Contact> list, allowedTypes) {
        Collection<Object> result = []

        list.each { it ->       // com.k_int.kbplus.Contact
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

            tmp.calculatedType      = it.getCalculatedType()
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

            tmp.token       = it.type?.name     // com.k_int.kbplus.PropertyDefinition.String
            tmp.scope       = it.type?.descr    // com.k_int.kbplus.PropertyDefinition.String
            tmp.note        = it.note
            tmp.isPublic    = "Yes" // derived to substitute private properties tentant

            if (it.dateValue) {
                tmp.value   = ApiToolkit.formatInternalDate(it.dateValue)
            }
            else {
                tmp.value   = (it.stringValue ?: (it.intValue ?: (it.decValue ?: (it.refValue?.value ?: (it.urlValue ?: null))))) // RefdataValue
            }

            tmp.type = PropertyDefinition.validTypes2[it.type.type]['en']

            if (it.type.type == RefdataValue.toString()) {
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

    static Collection<Object> getDocumentCollection(Collection<DocContext> list) {
        Collection<Object> result = []
        list.each { it -> // com.k_int.kbplus.DocContext
            result << ApiDoc.getDocumentMap(it.owner)
        }
        result
    }

    static Collection<Object> getIdentifierCollection(Collection<Identifier> list) {
        Collection<Object> result = []
        list.each { it ->   // com.k_int.kbplus.IdentifierOccurrence
            Map<String, Object> tmp = [:]

            tmp.put( 'namespace', it.ns?.ns )
            tmp.put( 'value', it.value )

            tmp = ApiToolkit.cleanUp(tmp, true, true)
            result << tmp
        }
        result
    }

    /**
     * @param com.k_int.kbplus.SubscriptionPackage subPkg
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return Collection<Object>
     */
    static Collection<Object> getIssueEntitlementCollection(SubscriptionPackage subPkg, ignoreRelation, Org context){
        Collection<Object> result = []

        List<IssueEntitlement> ieList = IssueEntitlement.executeQuery(
                'select ie from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub join tipp.pkg pkg ' +
                        ' where sub = :sub and pkg = :pkg and tipp.status != :statusTipp and ie.status != :statusIe',
                [sub: subPkg.subscription, pkg: subPkg.pkg, statusTipp: RDStore.TIPP_STATUS_DELETED, statusIe: RDStore.TIPP_STATUS_DELETED]
        )
        ieList.each{ ie ->
            result << ApiIssueEntitlement.getIssueEntitlementMap(ie, ignoreRelation, context) // com.k_int.kbplus.IssueEntitlement
        }

        return ApiToolkit.cleanUp(result, true, true)
    }

    static Collection<Object> getIssueEntitlementCoverageCollection(Collection<IssueEntitlementCoverage> list) {
        Collection<Object> result = []

        list?.each { it -> // com.k_int.kbplus.IssueEntitlementCoverage
            result << ApiUnsecuredMapReader.getIssueEntitlementCoverageMap(it)
        }

        result
    }

    /**
     *
     * @param list
     * @param com.k_int.kbplus.Org context
     * @return Collection<Object>
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

    static Collection<Object> getOrgLinkCollection(Collection<OrgRole> list, ignoreRelationType, Org context) { // TODO
        Collection<Object> result = []

        list.each { it ->   // com.k_int.kbplus.OrgRole
            Map<String, Object> tmp = [:]

            tmp.endDate     = ApiToolkit.formatInternalDate(it.endDate)
            tmp.startDate   = ApiToolkit.formatInternalDate(it.startDate)

            // RefdataValues
            tmp.roleType    = it.roleType?.value

            // References
            if (it.org && (ApiReader.IGNORE_ORGANISATION != ignoreRelationType)) {
                tmp.organisation = ApiUnsecuredMapReader.getOrganisationStubMap(it.org) // com.k_int.kbplus.Org
            }
            if (it.cluster && (ApiReader.IGNORE_CLUSTER != ignoreRelationType)) {
                tmp.cluster = ApiUnsecuredMapReader.getClusterStubMap(it.cluster) // com.k_int.kbplus.Cluster
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
            if (it.title && (ApiReader.IGNORE_TITLE != ignoreRelationType)) {
                tmp.title = ApiUnsecuredMapReader.getTitleStubMap(it.title) // com.k_int.kbplus.TitleInstance
            }

            result << ApiToolkit.cleanUp(tmp, true, false)
        }
        result
    }

    static Collection<Object> getPrivatePropertyCollection(Collection list, Org context) {
        Collection<Object> result = []

        list.findAll{ (it.owner.id == context.id || it.type.tenant?.id == context.id) && it.tenant?.id == context.id && it.isPublic == false }?.each { it ->       // com.k_int.kbplus.<x>PrivateProperty
            Map<String, Object> tmp = [:]

            tmp.token   = it.type.name     // com.k_int.kbplus.PropertyDefinition.String
            tmp.scope   = it.type.descr    // com.k_int.kbplus.PropertyDefinition.String
            tmp.note    = it.note
            //tmp.tenant          = ApiStubReader.resolveOrganisationStub(it.tenant, context) // com.k_int.kbplus.Org

            if (it.dateValue) {
                tmp.value   = ApiToolkit.formatInternalDate(it.dateValue)
            }
            else {
                tmp.value   = (it.stringValue ?: (it.intValue ?: (it.decValue ?: (it.refValue?.value ?: (it.urlValue ?: null))))) // RefdataValue
            }

            tmp.type = PropertyDefinition.validTypes2[it.type.type]['en']

            if (it.type.type == RefdataValue.toString()) {
                tmp.refdataCategory = it.type.refdataCategory
            }

            //tmp.dateCreated = ApiToolkit.formatInternalDate(it.dateCreated)
            //tmp.lastUpdated = ApiToolkit.formatInternalDate(it.getCalculatedLastUpdated())

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

    static Collection<Object> getPropertyCollection(Object generic, Org context, def ignoreFlag) {
        Collection<Object> cp = getCustomPropertyCollection(generic.customProperties, generic, context)
        Collection<Object> pp = getPrivatePropertyCollection(generic.customProperties, context)

        if (ignoreFlag == ApiReader.IGNORE_CUSTOM_PROPERTIES) {
            return pp
        }
        else if (ignoreFlag == ApiReader.IGNORE_PRIVATE_PROPERTIES) {
            return cp
        }

        pp.each { cp << it }
        cp
    }

    static Collection<Object> getPrsLinkCollection(Collection<PersonRole> list, allowedAddressTypes, allowedContactTypes, Org context) {  // TODO check context
        List result = []
        List tmp = []

        list.each { it ->

            // nested prs
            if(it.prs) {
                String x = it.prs.globalUID
                def person = tmp.find {it.globalUID == x}

                if(!person) {
                    person = ApiMapReader.getPersonMap(it.prs, allowedAddressTypes, allowedContactTypes, context) // com.k_int.kbplus.Person

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

                Map<String, Object> role    = [:] // com.k_int.kbplus.PersonRole
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
     * Access rights due wrapping object
     *
     * @param list
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return Collection<Object>
     */
    static Collection<Object> getTippCollection(Collection<TitleInstancePackagePlatform> list, def ignoreRelation, Org context) {
        Collection<Object> result = []

        list.each { it -> // com.k_int.kbplus.TitleInstancePackagePlatform
            result << ApiMapReader.getTippMap(it, ignoreRelation, context)
        }

        result
    }

    static Collection<Object> getSubscriptionPackageStubCollection(Collection<SubscriptionPackage> list, def ignoreRelation, Org context) {
        Collection<Object> result = []

        if (! list) {
            return null
        }

        list.each { it -> // com.k_int.kbplus.SubscriptionPackage
            result << ApiStubReader.requestSubscriptionPackageStubMixed(it, ignoreRelation, context)
        }
        result
    }
}
