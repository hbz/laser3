package com.k_int.kbplus


import com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupItem
import de.laser.AuditConfig
import de.laser.IssueEntitlementCoverage
import de.laser.exceptions.CreationException
import de.laser.helper.ConfigUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.helper.ServerUtils
import de.laser.interfaces.ShareSupport
import grails.transaction.Transactional
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.i18n.LocaleContextHolder

import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat

@Transactional
class OrganisationService {

    def contextService
    def messageSource
    def exportService
    def institutionsService
    def grailsApplication
    def instAdmService
    def userService
    def subscriptionService
    def globalSourceSyncService
    List<String> errors = []

    void initMandatorySettings(Org org) {
        log.debug("initMandatorySettings for org ${org.id}") //org.id call crashes when called from sync

        if (OrgSettings.get(org, OrgSettings.KEYS.NATSTAT_SERVER_ACCESS) == OrgSettings.SETTING_NOT_FOUND) {
            OrgSettings.add(org, OrgSettings.KEYS.NATSTAT_SERVER_ACCESS,
                    RefdataValue.getByValueAndCategory('No', RDConstants.Y_N)
            )
        }
        if (OrgSettings.get(org, OrgSettings.KEYS.NATSTAT_SERVER_API_KEY) == OrgSettings.SETTING_NOT_FOUND) {
            OrgSettings.add(org, OrgSettings.KEYS.NATSTAT_SERVER_API_KEY,'')
        }
        if (OrgSettings.get(org, OrgSettings.KEYS.NATSTAT_SERVER_REQUESTOR_ID) == OrgSettings.SETTING_NOT_FOUND) {
            OrgSettings.add(org, OrgSettings.KEYS.NATSTAT_SERVER_REQUESTOR_ID, '')
        }
        if (OrgSettings.get(org, OrgSettings.KEYS.OAMONITOR_SERVER_ACCESS) == OrgSettings.SETTING_NOT_FOUND) {
            OrgSettings.add(org, OrgSettings.KEYS.OAMONITOR_SERVER_ACCESS,
                    RefdataValue.getByValueAndCategory('No', RDConstants.Y_N)
            )
        }

        // called after
        // new Org.save()
        // does not work unless session is not flushed what causes crashes in sync
    }

    /**
     * Exports organisation data in the given format. It can be specified if higher education titles should be outputted or not.
     * Do NOT mix this method with exportOrgs of MyInstitutionController which is for consortia subscription members! That method should be generalised as well!!
     * @param orgs - the {@link List} of {@link Org}s
     * @param message - the title of the Excel sheet (not used in csv)
     * @param addHigherEducationTitles - add columns library type, library network, funder type, federal state, country with respective values
     * @param format - the format (xls or csv) to generate the output in
     * @return a String containing the CSV output or the Excel sheet containing the output
     */
    def exportOrg(List orgs, message, boolean addHigherEducationTitles, String format) {
        def titles = [messageSource.getMessage('org.sortname.label',null,LocaleContextHolder.getLocale()),messageSource.getMessage('org.shortname.label',null, LocaleContextHolder.getLocale()),'Name']
        RefdataValue orgSector = RefdataValue.getByValueAndCategory('Higher Education', RDConstants.ORG_SECTOR)
        RefdataValue orgType = RefdataValue.getByValueAndCategory('Provider', RDConstants.ORG_TYPE)
        if(addHigherEducationTitles) {
            titles.add(messageSource.getMessage('org.libraryType.label',null,LocaleContextHolder.getLocale()))
            titles.add(messageSource.getMessage('org.libraryNetwork.label',null,LocaleContextHolder.getLocale()))
            titles.add(messageSource.getMessage('org.funderType.label',null,LocaleContextHolder.getLocale()))
            titles.add(messageSource.getMessage('org.region.label',null,LocaleContextHolder.getLocale()))
            titles.add(messageSource.getMessage('org.country.label',null,LocaleContextHolder.getLocale()))
        }
        RefdataValue generalContact = RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
        RefdataValue responsibleAdmin = RefdataValue.getByValueAndCategory('Responsible Admin', RDConstants.PERSON_FUNCTION)
        RefdataValue billingContact = RefdataValue.getByValueAndCategory('Functional Contact Billing Adress', RDConstants.PERSON_FUNCTION)
        titles.addAll(['ISIL','WIB-ID','EZB-ID',generalContact.getI10n('value'),responsibleAdmin.getI10n('value'),billingContact.getI10n('value')])
        Set<PropertyDefinition> propertyDefinitions = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.org)
        titles.addAll(exportService.loadPropListHeaders(propertyDefinitions))
        List orgData = []
        Map<Org,Map<String,String>> identifiers = [:]
        List identifierList = Identifier.executeQuery("select ident, ident.org from Identifier ident where ident.org in (:orgs) and ident.ns.ns in (:namespaces)",[orgs:orgs,namespaces:['wibid','ezb','ISIL']])
        identifierList.each { row ->
            Identifier io = (Identifier) row[0]
            Org o = (Org) row[1]
            Map<String,String> orgIdentifiers = identifiers[o]
            if(!orgIdentifiers)
                orgIdentifiers = [:]
            orgIdentifiers[io.ns.ns] = io.value
            identifiers[o] = orgIdentifiers
        }
        Map<Org,Map<String,List<String>>> contacts = [:]
        List contactList = Contact.executeQuery("select c.content, pr.org, pr.functionType from PersonRole pr join pr.prs p join p.contacts c where pr.org in (:orgs) and pr.functionType in (:functionTypes) and c.contentType = :type and p.isPublic = true",[orgs:orgs,functionTypes:[generalContact,responsibleAdmin,billingContact],type: RDStore.CCT_EMAIL])
        contactList.each { row ->
            String c = row[0]
            Org o = (Org) row[1]
            RefdataValue funcType = (RefdataValue) row[2]
            Map<String,List<String>> orgContacts = contacts[o]
            if(!orgContacts)
                orgContacts = [:]
            List<String> emails = orgContacts[funcType.value]
            if(!emails) {
                emails = []
            }
            emails << c
            orgContacts[funcType.value] = emails
            contacts[o] = orgContacts
        }
        switch(format) {
            case "xls":
            case "xlsx":
                orgs.each{ org ->
                    List row = []
                    Map<String,String> furtherData = [isil: identifiers[org]?.ISIL,
                                                      wib: identifiers[org]?.wibid,
                                                      ezb: identifiers[org]?.ezb,
                                                      generalContact: contacts[org]?.get("General contact person")?.join(";"),
                                                      responsibleAdmin: contacts[org]?.get("Responsible Admin")?.join(";"),
                                                      billingContact: contacts[org]?.get("Functional Contact Billing Adress")?.join(";")]
                    //Sortname
                    row.add([field: org.sortname ?: '',style: null])
                    //Shortname
                    row.add([field: org.shortname ?: '',style: null])
                    //Name
                    row.add([field: org.name ?: '',style: null])
                    if(addHigherEducationTitles) {
                        //libraryType
                        row.add([field: org.libraryType?.getI10n('value') ?: ' ',style: null])
                        //libraryNetwork
                        row.add([field: org.libraryNetwork?.getI10n('value') ?: ' ',style: null])
                        //funderType
                        row.add([field: org.funderType?.getI10n('value') ?: ' ',style: null])
                        //region
                        row.add([field: org.region?.getI10n('value') ?: ' ',style: null])
                        //country
                        row.add([field: org.country?.getI10n('value') ?: ' ',style: null])
                    }
                    //get identifiers of namespaces ISIL/isil, WIB-ID, ezb-id
                    row.add([field: furtherData.isil ?: '', style: null])
                    row.add([field: furtherData.wib ?: '', style: null])
                    row.add([field: furtherData.ezb ?: '', style: null])
                    //General contact
                    row.add([field: furtherData.generalContact ?: '', style: null])
                    //Responsible admin
                    row.add([field: furtherData.responsibleAdmin ?: '', style: null])
                    //Billing contact
                    row.add([field: furtherData.billingContact ?: '', style: null])
                    row.addAll(exportService.processPropertyListValues(propertyDefinitions,format,org))
                    orgData.add(row)
                }
                Map sheetData = [:]
                sheetData[message] = [titleRow:titles,columnData:orgData]
                return exportService.generateXLSXWorkbook(sheetData)
            case "csv":
                orgs.each{ org ->
                    List row = []
                    Map<String,String> furtherData = [isil: identifiers[org]?.ISIL,
                                                      wib: identifiers[org]?.wibid,
                                                      ezb: identifiers[org]?.ezb,
                                                      generalContact: contacts[org]?.get("General contact person")?.join(";"),
                                                      responsibleAdmin: contacts[org]?.get("Responsible Admin")?.join(";"),
                                                      billingContact: contacts[org]?.get("Functional Contact Billing Adress")?.join(";")]
                    //Sortname
                    row.add(org.sortname ? org.sortname.replaceAll(',','') : '')
                    //Shortname
                    row.add(org.shortname ? org.shortname.replaceAll(',','') : '')
                    //Name
                    row.add(org.name ? org.name.replaceAll(',','') : '')
                    if(addHigherEducationTitles) {
                        //libraryType
                        row.add(org.libraryType?.getI10n('value') ?: ' ')
                        //libraryNetwork
                        row.add(org.libraryNetwork?.getI10n('value') ?: ' ')
                        //funderType
                        row.add(org.funderType?.getI10n('value') ?: ' ')
                        //region
                        row.add(org.region?.getI10n('value') ?: ' ')
                        //country
                        row.add(org.country?.getI10n('value') ?: ' ')
                    }
                    //get identifiers of namespaces ISIL/isil, WIB-ID, ezb-id
                    row.add(furtherData.isil ?: '')
                    row.add(furtherData.wib ?: '')
                    row.add(furtherData.ezb ?: '')
                    //General contact
                    row.add(furtherData.generalContact ?: '')
                    //Responsible admin
                    row.add(furtherData.responsibleAdmin ?: '')
                    //Billing contact
                    row.add(furtherData.billingContact ?: '')
                    row.addAll(exportService.processPropertyListValues(propertyDefinitions,format,org))
                    orgData.add(row)
                }
                return exportService.generateSeparatorTableString(titles,orgData,',')
        }

    }

    /**
     * Removes the combo link from the given department to the context org and sets the removed flag for the department
     * to be removed. Beware: deletion means the setting of a flag which hides the given object from the search lists.
     * @param department - the given department ({@link Org}) to remove
     * @return - the success or failure flag of removal
     */
    boolean removeDepartment(Org department) {
        Org contextOrg = contextService.org
        Combo combo = Combo.findByFromOrgAndToOrgAndType(department,contextOrg, RDStore.COMBO_TYPE_DEPARTMENT)
        if(combo.delete()) {
            department.status = RDStore.O_STATUS_DELETED
            return department.save()
        }
        else return false
    }

    /**
     * Fetches for the given user and context org the pending requests which is a map of lists with users having made a request to join the context org.
     * Here is distinct between local INST_ADMins and global admins.
     * @param user - the {@link User} requesting the requests
     * @param ctxOrg - the context {@link Org} for
     * @return a {@link Map} of structure [pendingRequests: {@link List}<{@link User}>,pendingRequestsForGivenInstAdmins:{@link List}<{@link User}>]
     */
    Map<String, Object> getPendingRequests(User user, Org ctxOrg) {

        def result = [
                pendingRequests: [],
                pendingRequestsForGivenInstAdmins: []
                ]

        if (!user || !ctxOrg) {
            return result
        }

        if (!user.hasRole('ROLE_ADMIN')) {
            // INST_ADM: contextOrg and combo referenced orgs

            List<Org> orgList = Org.executeQuery('SELECT c.fromOrg from Combo c WHERE c.toOrg = :ctx', [ctx: ctxOrg])
            orgList.add(ctxOrg)

            result.pendingRequests = UserOrg.executeQuery(
                    'SELECT uo FROM UserOrg uo WHERE uo.status = :status AND uo.org in (:orgList)',
                    [status: UserOrg.STATUS_PENDING, orgList: orgList],
                    [sort: 'dateRequested']
            )
        }
        else {
            // ROLE_ADMIN, ROLE_YODA

            List<UserOrg> pendingRequests = UserOrg.findAllByStatus(UserOrg.STATUS_PENDING, [sort: 'dateRequested'])

            pendingRequests.each { pr ->
                def instAdmGiven = User.executeQuery(
                        "SELECT admin FROM UserOrg uo JOIN uo.user admin " +
                                "WHERE uo.org = :prOrg AND uo.formalRole = :instAdmRole AND uo.status = :frStatus", [
                        prOrg      : pr.org,
                        instAdmRole: Role.findByAuthorityAndRoleType('INST_ADM', 'user'),
                        frStatus   : UserOrg.STATUS_APPROVED
                ]
                )
                if (!instAdmGiven) {
                    result.pendingRequests << pr
                } else {
                    result.pendingRequestsForGivenInstAdmins << pr
                }
            }
        }

        result
    }

    /**
     * This method creates a basic data set for a given {@link Org}. Depending on the orgType of the {@link Org}, the set for a consortium or for an institution will be created.
     * The method should only be used on QA since it is based on a dataset stored in that database and serves as presentation and testing environment for new customers.
     * Important: the domain object creation follows the structure:
     * [mainParams: [
     *      ...data for the main object...
     *      ],
     *  addParams: [
     *      ...data for objects related to the main object, e.g. providers, etc...
     *      ]
     * ]
     * For {@link License}s, we have a third field after the addParams - baseLicense - which contains a license base to copy.
     * @param current - the {@link Org} for which the dataset should be generated
     * @return is the set successfully created?
     */
    boolean setupBasicTestData(Org current) {
        try {
            log.info("check if data exists for org...")
            //if(!OrgRole.findAllByOrg(current) && !Person.findAllByTenant(current) && !CostItem.findAllByOwner(current) && !PropertyDefinition.findAllByTenant(current)) {
                /*
                data to be setup:
                a) for institutions: Org Mustereinrichtung
                    Subscriptions
                    Licenses
                    Cost Items
                b) for consortia: Org Musterkonsortium
                    Subscriptions
                    Licenses
                    Cost Items
             */
                GregorianCalendar cal = new GregorianCalendar()
                SimpleDateFormat sdf = new SimpleDateFormat('yyyy-MM-dd')
                Map<String,Date> dates = [:]
                cal.setTimeInMillis(System.currentTimeMillis())
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                dates.defaultStartDate = cal.getTime()
                cal.set(Calendar.MONTH, Calendar.DECEMBER)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                dates.defaultEndDate = cal.getTime()
                cal.set(Calendar.MONTH, Calendar.MAY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                dates.startDateCurrent = cal.getTime()
                dates.endDateCurrent = dates.defaultEndDate
                cal.set(Calendar.MONTH, Calendar.APRIL)
                dates.startDateExpired = cal.getTime()
                cal.set(Calendar.DAY_OF_MONTH, 30)
                dates.endDateExpired = cal.getTime()
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.add(Calendar.YEAR, -1)
                dates.previousYearRingStart = cal.getTime()
                cal.add(Calendar.YEAR, 2)
                dates.nextYearRingStart = cal.getTime()
                cal.set(Calendar.MONTH, Calendar.DECEMBER)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                cal.add(Calendar.YEAR, -2)
                dates.previousYearRingEnd = cal.getTime()
                cal.add(Calendar.YEAR, 2)
                dates.nextYearRingEnd = cal.getTime()
                Map<String,RefdataValue> localRDStoreSubscriptionForm = [:]
                RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM).each { rdv ->
                    localRDStoreSubscriptionForm.put(rdv.value,rdv)
                }
                Map<String,RefdataValue> localRDStoreSubscriptionResource = [:]
                RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE).each { rdv ->
                    localRDStoreSubscriptionResource.put(rdv.value,rdv)
                }
                Map<String,RefdataValue> localRDStoreCostItemStatus = [:]
                RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_STATUS).each { rdv ->
                    localRDStoreCostItemStatus.put(rdv.value,rdv)
                }
                Map<String,RefdataValue> localRDStoreCostItemElement = [:]
                RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_ELEMENT).each { rdv ->
                    localRDStoreCostItemElement.put(rdv.value,rdv)
                }
                Map<String,RefdataValue> localRDStoreCurrency = [
                        'USD': RefdataValue.getByValueAndCategory('USD','Currency'),
                        'GBP': RefdataValue.getByValueAndCategory('GBP','Currency'),
                        'EUR': RefdataValue.getByValueAndCategory('EUR','Currency')
                ]
                Map<String,RefdataValue> localRDStoreGender = [
                        'Female': RefdataValue.getByValueAndCategory('Female', RDConstants.GENDER),
                        'Male': RefdataValue.getByValueAndCategory('Male', RDConstants.GENDER),
                        'Third Gender': RefdataValue.getByValueAndCategory('Third Gender', RDConstants.GENDER)
                ]
                Map<String,RefdataValue> localRDStoreContactType = [
                        'Personal': RefdataValue.getByValueAndCategory('Personal', RDConstants.CONTACT_TYPE),
                        'Job-related': RefdataValue.getByValueAndCategory('Job-related', RDConstants.CONTACT_TYPE)
                ]
                Map<String,Org> exampleOrgs = [
                        'Musteranbieter A': Org.findByGlobalUID('org:c188b5ab-9867-4c6c-ba22-3a134a9aaee8'), //check
                        'Musteranbieter B': Org.findByGlobalUID('org:ceab4b2c-e02c-4218-9841-6d4de7f45ef3'), //check
                        'Musteranbieter E-Books': Org.findByGlobalUID('org:91da75a1-0d6c-44c6-8ca0-42a00f8ac7e2'), //check
                        'Musteranbieter E-Journals': Org.findByGlobalUID('org:0b90d99f-9621-422a-8696-0dbfedac4cb2')  //check
                ]
                Map<String,Package> examplePackages = [
                        'duz: Deutsche Universitätszeitung': Package.findByGlobalUID('package:af054973-443d-42d9-b561-9e80f0f234ba'), //check
                        'Brepols: Bibliography of British and Irish History': Package.findByGlobalUID('package:1e8092c7-494b-4633-b06f-1a22708b7a7a'), //check
                        'Naxos: Naxos Music Library World': Package.findByGlobalUID('package:8a6f1483-6231-49dd-b3df-dbb418f84563'), //check
                        'UTB Alle Titel': Package.findByGlobalUID('package:8e356b83-4142-4bc5-a377-dc18351744f3'), //check
                        'American Chemical Society: American Chemical Society Journals': Package.findByGlobalUID('package:b108178b-f27c-455d-9061-c8837905dc65'), //check
                        'EBSCO: SportDiscus': Package.findByGlobalUID('package:348a270f-715d-4ee1-b3b2-e28db9e8c0a3') //check
                ]
                RefdataValue licenseType = RefdataValue.getByValueAndCategory('Actual', RDConstants.LICENSE_TYPE)
                Map generalData = [dates:dates,
                                   sdf:sdf,
                                   licenseType: licenseType,
                                   subscriptionForms:localRDStoreSubscriptionForm,
                                   subscriptionResources:localRDStoreSubscriptionResource,
                                   costItemStatus:localRDStoreCostItemStatus,
                                   costItemElements:localRDStoreCostItemElement,
                                   currencies:localRDStoreCurrency,
                                   gender:localRDStoreGender,
                                   contactTypes:localRDStoreContactType,
                                   exampleOrgs:exampleOrgs,
                                   examplePackages:examplePackages]
                if(current.hasPerm("ORG_INST")) {
                    return setupTestDataForInst(generalData,current)
                }
                else if(current.hasPerm("ORG_CONSORTIUM")) {
                    return setupTestDataForCons(generalData,current)
                }
                else {
                    errors.add("Kein Kundentyp gegeben!")
                    return false
                }
            //}
            //else {
                //errors.add("Diese Einrichtung verfügt bereits über einen Testdatensatz!")
                //return false
            //}
        }
        catch (CreationException e) {
            log.error(e.printStackTrace())
            errors.add(e.getMessage())
            return false
        }
        return true
    }

    boolean setupTestDataForInst(Map generalData,Org current) {
        //set up parent subscriptions
        //pray that there will be no UID change on the example objects!! - there was. Damn it. Reset everything ...
        Subscription underConsiderationDatenAParent = Subscription.findByGlobalUID("subscription:9195762f-a33d-4c02-8ea1-d94ac24b9a1b") //check
        Subscription currentDatenAParent = Subscription.findByGlobalUID("subscription:0f43143c-851b-4623-b3d9-063d86d30640") //check
        Subscription eBookPickParent = Subscription.findByGlobalUID("subscription:a00738d6-5134-4bdb-8e9c-70aefcfd4d06") //check
        Subscription journalPaketExtremParent = Subscription.findByGlobalUID('subscription:bef14b4a-897a-4bf8-880d-6c9eee7de2a5') //check
        Org consortium = Org.findByGlobalUID('org:0b3311f1-a307-476c-91c9-ee5b44765bd2') //check
        if(!CostItemElementConfiguration.findAllByForOrganisation(current)) {
            log.info("creating cost item element configurations ...")
            Set<CostItemElementConfiguration> ciecs = [
                    new CostItemElementConfiguration(forOrganisation: current,
                            costItemElement: generalData.costItemElements.get('price: provider price'),
                            elementSign: RDStore.CIEC_POSITIVE),
                    new CostItemElementConfiguration(forOrganisation: current,
                            costItemElement: generalData.costItemElements.get('price: list price'),
                            elementSign: RDStore.CIEC_NEUTRAL),
                    new CostItemElementConfiguration(forOrganisation: current,
                            costItemElement: generalData.costItemElements.get('discount: multiyear discount'),
                            elementSign: RDStore.CIEC_NEGATIVE)
            ]
            ciecs.each { ciec ->
                if(!ciec.save()) {
                    errors.add(ciec.errors.toString())
                    return false
                }
            }
        }
        if(!PropertyDefinitionGroup.findAllByTenant(current)) {
            log.info("creating property definition groups ...")
            List propertyDefinitionGroups = [
                    [group:new PropertyDefinitionGroup(name: 'Fernleihe', description: 'Fernleihbedingungen', ownerType: License.class.name, tenant: current, isVisible: true),
                     items:[PropertyDefinition.getByNameAndDescr('ILL record keeping required', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('ILL electronic', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('ILL print or fax', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('ILL secure electronic transmission', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('ILL term note', PropertyDefinition.LIC_PROP)]
                    ],
                    [group:new PropertyDefinitionGroup(name: 'Archivrecht', description: 'Welches Archivrecht ist gegeben?', ownerType: License.class.name, tenant: current, isVisible: true),
                     items:[PropertyDefinition.getByNameAndDescr('Archival Copy Content', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('Archival Copy: Cost', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('Archival Copy: Permission', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('Archival Copy: Time', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('Archiving rights', PropertyDefinition.LIC_PROP)]
                    ],
                    [group:new PropertyDefinitionGroup(name: 'Zugriffsbedingungen', description: 'Welche Zugriffsmöglichkeiten sind erlaubt?', ownerType: License.class.name, tenant: current, isVisible: true),
                     items:[PropertyDefinition.getByNameAndDescr('Walk-in Access', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('Walk-in User Term Note', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('Wifi Access', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('Remote Access', PropertyDefinition.LIC_PROP)]
                    ],
                    [group:new PropertyDefinitionGroup(name: 'Fremdsysteme', description: '', ownerType: Subscription.class.name, tenant: current, isVisible: true),
                     items:[PropertyDefinition.getByNameAndDescr('DBIS link', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('EZB tagging (yellow)', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('SFX entry', PropertyDefinition.SUB_PROP)]
                    ],
                    [group:new PropertyDefinitionGroup(name: 'Statistik', description: '', ownerType: Subscription.class.name, tenant: current, isVisible: true),
                     items:[PropertyDefinition.getByNameAndDescr('Statistic', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Statistics Link', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Statistic access', PropertyDefinition.SUB_PROP)]
                    ]
            ]
            propertyDefinitionGroups.each { pdg ->
                if(pdg.group.save()) {
                    pdg.items.each { pd ->
                        PropertyDefinitionGroupItem pdgi = new PropertyDefinitionGroupItem(propDef: pd, propDefGroup: pdg.group)
                        if(!pdgi.save()) {
                            errors.add(pdgi.errors.toString())
                            return false
                        }
                    }
                }
                else {
                    errors.add(pdg.group.errors.toString())
                    return false
                }
            }
        }
        if(!Person.findAllByTenant(current)) {
            log.info("creating private contacts ...")
            Map franzEric = [mainParams: [
                    first_name:'Franz',
                    last_name:'Eric',
                    gender: generalData.gender.get('Male'),
                    contactType: RDStore.CONTACT_TYPE_PERSONAL,
                    tenant: current,
                    isPublic: false
            ], addParams: [
                    contact: [[contentType:RDStore.CCT_EMAIL,
                               type: generalData.contactTypes.get('Job-related'),
                               content: 'e.franz@anbieter.de'
                              ]],
                    personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter E-Books'),
                                   functionType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
                    ]
            ]
            ]
            Map nameVorname = [mainParams:[
                    first_name: 'Vorname',
                    last_name: 'Name',
                    gender: generalData.gender.get('Third Gender'),
                    contactType: RDStore.CONTACT_TYPE_PERSONAL,
                    tenant: current,
                    isPublic: false
            ],
                               addParams: [
                                       contact: [[contentType: RDStore.CCT_PHONE,
                                                  type: generalData.contactTypes.get('Job-related'),
                                                  content: '00 49 021 456 789'
                                                 ]],
                                       personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter A'),
                                                      functionType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS],
                                                     [org:generalData.exampleOrgs.get('Musteranbieter A'),
                                                      positionType: RefdataValue.getByValueAndCategory('Sales Director', RDConstants.PERSON_POSITION)]
                                       ]
                               ]
            ]
            Map technik = [mainParams: [
                    last_name: 'Technik',
                    contactType: RDStore.CONTACT_TYPE_FUNCTIONAL,
                    tenant: current,
                    isPublic: false
            ],
                           addParams: [
                                   contact:[[contentType: RDStore.CCT_EMAIL,
                                             type: generalData.contactTypes.get('Job-related'),
                                             content: 'tech@support.com'
                                            ]],
                                   personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter A'),
                                                  functionType: RefdataValue.getByValueAndCategory('Technical Support', RDConstants.PERSON_FUNCTION)]]]
            ]
            Set<Map> personalContacts = [franzEric,nameVorname,technik]
            personalContacts.each { contact ->
                createObject('Person',contact,consortium,current)
            }
        }
        log.info("creating consortial member subscriptions ...")
        String testDatenAChildIdentifier = UUID.randomUUID().toString()
        String currentDatenAChildIdentifier = UUID.randomUUID().toString()
        String expiredJournalPaketIdentifier = UUID.randomUUID().toString()
        String currentJournalPaketIdentifier = UUID.randomUUID().toString()
        String journalPaketExtremChildIdentifier = UUID.randomUUID().toString()
        String eBookPickChildIdentifier = UUID.randomUUID().toString()
        String musterdatenbankIdentifier = UUID.randomUUID().toString()
        Map testDatenAChildParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: "Daten A (Test)",
                             startDate: generalData.dates.startDateExpired,
                             endDate: generalData.dates.endDateExpired,
                             status: RefdataValue.getByValueAndCategory('Test Access', RDConstants.SUBSCRIPTION_STATUS),
                             identifier: testDatenAChildIdentifier,
                             isPublic: false,
                             instanceOf: underConsiderationDatenAParent,
                             form: generalData.subscriptionForms.get('test')],
                addParams: [:]
        ]
        Map currentDatenAChildParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: "Daten A",
                             startDate: generalData.dates.startDateCurrent,
                             endDate: generalData.dates.endDateCurrent,
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: currentDatenAChildIdentifier,
                             isPublic: false,
                             instanceOf: currentDatenAParent,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('mixed')],
                addParams: [/*provider:generalData.exampleOrgs.get('Musteranbieter A'),*/
                            costItems:[
                                    [sub: 'instanceOfCurrent',
                                     billingSum: 2000,
                                     currency: generalData.currencies.get('USD'),
                                     currencyRate: 1.0,
                                     taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                     startDate: generalData.dates.startDateCurrent,
                                     endDate: generalData.dates.endDateCurrent,
                                     status: generalData.costItemStatus.get('Actual'),
                                     costItemElement: generalData.costItemElements.get('price: consortial price'),
                                     costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                     isVisibleForSubscriber: true]
                            ]
                ]
        ]
        Map eBookPickChildParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: "E-Book-Pick",
                             startDate: generalData.dates.defaultStartDate,
                             endDate: generalData.dates.defaultEndDate,
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: eBookPickChildIdentifier,
                             isPublic: false,
                             instanceOf: eBookPickParent,
                             form: generalData.subscriptionForms.get('singlePurchase'),
                             resource: generalData.subscriptionResources.get('ebookSingle')],
                addParams: [packages:[[pkg:generalData.examplePackages.get('UTB Alle Titel')]
                ],
                            /*provider:generalData.exampleOrgs.get('Musteranbieter E-Books'),*/
                            costItems:[
                                    [sub:'instanceOfCurrent',
                                     billingSum: 24000,
                                     currencyRate: 1.0,
                                     currency: generalData.currencies.get('EUR'),
                                     taxKey: CostItem.TAX_TYPES.TAXABLE_7,
                                     startDate: generalData.dates.defaultStartDate,
                                     endDate: generalData.dates.defaultEndDate,
                                     status: generalData.costItemStatus.get('Actual'),
                                     costItemElement: generalData.costItemElements.get('price: consortial price'),
                                     costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                     isVisibleForSubscriber: true],
                                    [sub:'currentOwn',
                                     billingSum: 25680,
                                     currencyRate: 1.0,
                                     currency: generalData.currencies.get('EUR'),
                                     status: generalData.costItemStatus.get('Actual'),
                                     startDate: generalData.dates.defaultStartDate,
                                     endDate: generalData.dates.defaultEndDate]
                            ]
                ]
        ]
        Map expiredJournalPaketParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_LOCAL,
                             name: "Journal-Paket",
                             startDate: generalData.dates.previousYearRingStart,
                             endDate: generalData.dates.previousYearRingEnd,
                             manualCancellationDate: generalData.sdf.parse('2019-10-31'),
                             status: RDStore.SUBSCRIPTION_EXPIRED,
                             identifier: expiredJournalPaketIdentifier,
                             isPublic: false,
                             form: RefdataValue.getByValueAndCategory('purchaseOngoing',RDConstants.SUBSCRIPTION_FORM),
                             resource: RefdataValue.getByValueAndCategory('ejournalPackage', RDConstants.SUBSCRIPTION_RESOURCE)],
                addParams: [packages:[[pkg: Package.findByGlobalUID('package:b108178b-f27c-455d-9061-c8837905dc65')]], //check
                            provider:generalData.exampleOrgs.get('Musteranbieter E-Journals'),
                            customProperties:[[type:PropertyDefinition.getByNameAndDescr('DBIS link', PropertyDefinition.SUB_PROP).id,urlValue:'https://dbis..de']]
                ]
        ]
        Map currentJournalPaketParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_LOCAL,
                             name: "Journal-Paket",
                             startDate: generalData.dates.defaultStartDate,
                             endDate: generalData.dates.defaultEndDate,
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: currentJournalPaketIdentifier,
                             isPublic: false,
                             form: RefdataValue.getByValueAndCategory('purchaseOngoing', RDConstants.SUBSCRIPTION_FORM),
                             resource: RefdataValue.getByValueAndCategory('ejournalPackage',RDConstants.SUBSCRIPTION_RESOURCE)],
                addParams: [packages:[[pkg: generalData.examplePackages.get('American Chemical Society: American Chemical Society Journals')]],
                            provider:generalData.exampleOrgs.get('Musteranbieter E-Journals')]
        ]
        Map journalPaketExtremChildParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: "Journal-Paket_Extrem",
                             startDate: generalData.dates.startDateCurrent,
                             endDate: generalData.dates.endDateCurrent,
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: journalPaketExtremChildIdentifier,
                             isPublic: false,
                             form: RefdataValue.getByValueAndCategory('purchaseOngoing', RDConstants.SUBSCRIPTION_FORM),
                             resource: RefdataValue.getByValueAndCategory('ejournalPackage', RDConstants.SUBSCRIPTION_RESOURCE),
                             instanceOf: journalPaketExtremParent],
                addParams: [:]/*provider:generalData.exampleOrgs.get('Musteranbieter E-Journals')*/
        ]
        Map musterdatenbankParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_LOCAL,
                             name: "Musterdatenbank",
                             startDate: generalData.dates.defaultStartDate,
                             endDate: generalData.dates.defaultEndDate,
                             manualCancellationDate: generalData.sdf.parse('2020-09-30'),
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: musterdatenbankIdentifier,
                             isPublic: false,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('database')],
                addParams: [packages:[[pkg:generalData.examplePackages.get('EBSCO: SportDiscus')]],
                            provider:generalData.exampleOrgs.get('Musteranbieter A'),
                            costItems:[
                                    [sub:'currentOwn',
                                     billingSum:5700,
                                     currency:generalData.currencies.get('EUR'),
                                     currencyRate:1.0,
                                     localSum:5700,
                                     taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                     subPkg: generalData.examplePackages.get('EBSCO: SportDiscus'),
                                     startDate: generalData.dates.defaultStartDate,
                                     endDate: generalData.dates.defaultEndDate,
                                     status: generalData.costItemStatus.get('Actual'),
                                     costItemElement: generalData.costItemElements.get('price: provider price'),
                                     costItemElementConfiguration: RDStore.CIEC_POSITIVE]
                            ],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('DBIS link', PropertyDefinition.SUB_PROP).id,urlValue:'http://dbis.uni-regensburg.de/...'],
                                    [type:PropertyDefinition.getByNameAndDescr('Statistic', PropertyDefinition.SUB_PROP).id,stringValue:'Counter 4'],
                                    [type:PropertyDefinition.getByNameAndDescr('Statistics Link', PropertyDefinition.SUB_PROP).id,urlValue:'http://www.123.de']
                            ]
                ]
        ]
        Set<Map> subscriptionSet = [testDatenAChildParams,
                                    currentDatenAChildParams,
                                    eBookPickChildParams,
                                    expiredJournalPaketParams,
                                    currentJournalPaketParams,
                                    journalPaketExtremChildParams,
                                    musterdatenbankParams]
        subscriptionSet.each { sub ->
            Set<Long> subs = OrgRole.executeQuery('select oo.sub.id from OrgRole oo where oo.sub.name = :name and oo.sub.status = :status and oo.org = :current',[name:sub.mainParams.name,status:sub.mainParams.status,current:current])
            if(!subs)
                createObject('Subscription',sub,consortium,current)
            else {
                Subscription subToModify = Subscription.get(subs[0])
                switch(subToModify.name) {
                    case testDatenAChildParams.mainParams.name: testDatenAChildIdentifier = subToModify.identifier
                        subToModify.instanceOf = underConsiderationDatenAParent
                        break
                    case currentDatenAChildParams.mainParams.name: currentDatenAChildIdentifier = subToModify.identifier
                        subToModify.instanceOf = currentDatenAParent
                        break
                    case eBookPickChildParams.mainParams.name: eBookPickChildIdentifier = subToModify.identifier
                        subToModify.instanceOf = eBookPickParent
                        break
                    case expiredJournalPaketParams.mainParams.name:
                    case currentJournalPaketParams.mainParams.name:
                        if(subToModify.status == RDStore.SUBSCRIPTION_EXPIRED)
                            expiredJournalPaketIdentifier = subToModify.identifier
                        else if(subToModify.status == RDStore.SUBSCRIPTION_CURRENT)
                            currentJournalPaketIdentifier = subToModify.identifier
                        break
                    case journalPaketExtremChildParams.mainParams.name: journalPaketExtremChildIdentifier = subToModify.identifier
                        subToModify.instanceOf = journalPaketExtremParent
                        break
                    case musterdatenbankParams.mainParams.name: musterdatenbankIdentifier = subToModify.identifier
                        break
                }
                subToModify.save(flush:true) //needed because of list processing
            }
        }
        //globalSourceSyncService.cleanUpGorm()
        log.info("creating links between consortia member subscriptions ...")
        Map dateALinking = [linkType: RDStore.LINKTYPE_FOLLOWS,objectType:Subscription.class.name,source:Subscription.findByIdentifier(currentDatenAChildIdentifier).id,destination:Subscription.findByIdentifier(testDatenAChildIdentifier).id,owner:current]
        Map journalPaketLinking = [linkType: RDStore.LINKTYPE_FOLLOWS,objectType:Subscription.class.name,source:Subscription.findByIdentifier(currentJournalPaketIdentifier).id,destination:Subscription.findByIdentifier(expiredJournalPaketIdentifier).id,owner:current]
        Set<Map> linkSet = [dateALinking,journalPaketLinking]
        linkSet.each { link ->
            if(!Links.findBySourceAndDestination(link.source,link.destination))
                setupLinking(link)
        }
        log.info("creating consortia member licenses ...")
        Map rahmenvertragChildParams = [
                ownedSubscriptions: [currentDatenAChildIdentifier, journalPaketExtremChildIdentifier],
                baseLicense: License.findByGlobalUID("license:5de157bc-e13c-44f3-8c23-eaabccfad1d1") //check
        ]
        Map rahmenvertragEbooksChildParams = [
                ownedSubscriptions: [eBookPickChildIdentifier],
                baseLicense: License.findByGlobalUID("license:ae64e09e-e462-46cb-939d-ffd1a7d5d0df") //check
        ]
        Map journalVertragParams = [
                mainParams: [reference: "Journal_Vertrag",
                             type: generalData.licenseType,
                             status: RDStore.LICENSE_CURRENT,
                             startDate: generalData.sdf.parse('2017-01-01')],
                addParams: [ownedSubscriptions:[expiredJournalPaketIdentifier,currentJournalPaketIdentifier],
                            licensors: [
                                    [generalData.exampleOrgs.get('Musteranbieter E-Journals')]
                            ],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('ILL electronic', PropertyDefinition.LIC_PROP).id,refValue:RDStore.PERM_PERM_EXPL,paragraph:'ist alles erlaubt'],
                                    [type:PropertyDefinition.getByNameAndDescr('Archival Copy Content', PropertyDefinition.LIC_PROP).id,refValue:RefdataValue.getByValueAndCategory('Data', RDConstants.LICENSE_ARC_ARCHIVAL_COPY_CONTENT)],
                                    [type:PropertyDefinition.getByNameAndDescr('Archiving rights', PropertyDefinition.LIC_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('Walk-in Access', PropertyDefinition.LIC_PROP).id,refValue: RDStore.PERM_PERM_EXPL],
                                    [type:PropertyDefinition.getByNameAndDescr('Wifi Access', PropertyDefinition.LIC_PROP).id,refValue:RDStore.YN_YES]
                            ],
                            licenseDocuments: [[docstoreUUID:'ce60e58d-1009-4c1b-bd0c-1bbb8f6910d3']]
                ]
        ]
        Map datenbankMusterParams = [
                mainParams: [reference: "Datenbank-Muster",
                             type: generalData.licenseType,
                             status: RDStore.LICENSE_CURRENT,
                             startDate: generalData.sdf.parse('2019-01-01')],
                addParams: [ownedSubscriptions:[musterdatenbankIdentifier],
                            licensors: [
                                    [generalData.exampleOrgs.get('Musteranbieter A')]
                            ],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('ILL electronic', PropertyDefinition.LIC_PROP).id,refValue:RDStore.PERM_PERM_EXPL],
                                    [type:PropertyDefinition.getByNameAndDescr('Archiving rights', PropertyDefinition.LIC_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('Walk-in Access', PropertyDefinition.LIC_PROP).id,refValue: RDStore.PERM_SILENT,note:'per Mail geregelt – s. Dokument'],
                                    [type:PropertyDefinition.getByNameAndDescr('Wifi Access', PropertyDefinition.LIC_PROP).id,refValue:RDStore.YN_YES]
                            ],
                            tasks: [
                                    [title: 'Mehrjahresvariante verhandeln',
                                     status: RefdataValue.getByValueAndCategory('Open', RDConstants.TASK_STATUS),
                                     endDate: generalData.sdf.parse('2021-01-01'),
                                     responsibleOrg: current,
                                     description:'Es sollte mit der nächsten Lizenzverlängerung auch eine Mehrjahresvariante geben, die vertraglich festgehalten wird.']
                            ],
                            licenseDocuments: [[docstoreUUID:'d633d527-27f0-43a7-adfd-5ad316a211e7'],[docstoreUUID:'31af3209-d681-4d21-854d-089ddcf76731']]
                ]
        ]
        Set<Map> consortialLicenseSet = [rahmenvertragChildParams,
                                         rahmenvertragEbooksChildParams]
        Set<Map> licWithoutBaseSet = [journalVertragParams,
                                      datenbankMusterParams]
        consortialLicenseSet.each { cl ->
            linkSubscriptionsToLicense(cl.baseLicense,cl.ownedSubscriptions,current)
        }
        licWithoutBaseSet.each { lwob ->
            if(!License.executeQuery('select oo.lic from OrgRole oo where oo.lic.reference = :licName and oo.org = :ctx',[licName:lwob.mainParams.reference,ctx:current]))
                createObject('License',(Map) lwob,consortium,current)
        }
        return true
    }

    boolean setupTestDataForCons(Map generalData,Org current) {
        Map<String,PropertyDefinition> privateProperties = [:]
        Object[] argv0 = [current.name]
        if(!PropertyDefinition.findAllByTenant(current)) {
            log.info("creating private properties ...")

            Map<String, Object> map1 = [
                    token       : "Quellensteuer-Befreiung",
                    category    : PropertyDefinition.SUB_PROP,
                    type        : "class com.k_int.kbplus.RefdataValue",
                    rdc         : RDConstants.Y_N_O,
                    tenant      : current.globalUID,
                    i10n        : [
                            name_de: "Quellensteuer-Befreiung",
                            name_en: "Quellensteuer-Befreiung",
                            expl_de: "Hat der Anbieter für dieses Produkt eine Befreiung der Quellensteuer erwirkt?",
                            expl_en: "Hat der Anbieter für dieses Produkt eine Befreiung der Quellensteuer erwirkt?"
                    ]
            ]

            Map<String, Object> map2 = [
                    token       : "BGA",
                    category    : PropertyDefinition.ORG_PROP,
                    type        : "class com.k_int.kbplus.RefdataValue",
                    rdc         : RDConstants.Y_N,
                    tenant      : current.globalUID,
                    i10n        : [
                            name_de: "BGA",
                            name_en: "BGA",
                            expl_de: "Betrieb gewerblicher Art",
                            expl_en: "Betrieb gewerblicher Art"
                    ]
            ]

            Map<String, Object> map3 = [
                    token       : "EGP Nr.",
                    category    : PropertyDefinition.ORG_PROP,
                    type        : "class java.lang.Integer",
                    tenant      : current.globalUID,
                    i10n        : [
                            name_de: "EGP Nr.",
                            name_en: "EGP Nr.",
                            expl_de: "ID für das SAP System des rechtlichen Trägers",
                            expl_en: "ID für das SAP System des rechtlichen Trägers"
                    ]
            ]
            Set<PropertyDefinition> privatePropertyDefMaps = [
                    PropertyDefinition.construct(map1),
                    PropertyDefinition.construct(map2),
                    PropertyDefinition.construct(map3)
            ]
            /*
            Set<PropertyDefinition> privatePropertyDefMaps = [
                    new PropertyDefinition(name:'Quellensteuer-Befreiung',
                            tenant:current,
                            refdataCategory:RDConstants.Y_N_O,
                            descr:'Subscription Property',
                            type:'class com.k_int.kbplus.RefdataValue',
                            expl:'Hat der Anbieter für dieses Produkt eine Befreiung der Quellensteuer erwirkt?'),
                    new PropertyDefinition(name:'BGA',
                            tenant:current,
                            refdataCategory:RDConstants.Y_N,
                            descr:'Organisation Property',
                            type:'class com.k_int.kbplus.RefdataValue',
                            expl:'Betrieb gewerblicher Art'),
                    new PropertyDefinition(name:'EGP Nr.',
                            tenant:current,
                            descr:'Organisation Property',
                            type:'class java.lang.Integer',
                            expl:'ID für das SAP System des rechtlichen Trägers')
            ]
            */
            privatePropertyDefMaps.each { propDef ->
                if(propDef.save()) {
                    privateProperties.put(propDef.name,propDef)
                }
                else {
                    errors.add(propDef.errors.toString())
                    return false
                }
            }
        }
        else {
            PropertyDefinition.findAllByTenant(current).each { PropertyDefinition pd ->
                privateProperties.put(pd.name,pd)
            }
        }

        if(!PropertyDefinitionGroup.findAllByTenant(current)) {
            log.info("creating property definition groups ...")
            List propertyDefinitionGroups = [
                    [group:new PropertyDefinitionGroup(name: 'ausgeübtes Recht', description: 'Welches Recht wird angewandt?', ownerType: License.class.name, tenant: current, isVisible: true),
                     items:[PropertyDefinition.getByNameAndDescr('Governing law', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('Governing jurisdiction', PropertyDefinition.LIC_PROP)]
                    ],
                    [group:new PropertyDefinitionGroup(name: 'Fernleihe', description: 'ist eine Fernleihe erlaubt?', ownerType: License.class.name, tenant: current, isVisible: true),
                     items:[PropertyDefinition.getByNameAndDescr('ILL record keeping required', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('ILL electronic', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('ILL print or fax', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('ILL secure electronic transmission', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('ILL term note', PropertyDefinition.LIC_PROP)]
                    ],
                    [group:new PropertyDefinitionGroup(name: 'GASCO', description: 'Merkmale, die den GASCO-Monitor steuern', ownerType: Subscription.class.name, tenant: current, isVisible: true),
                     items:[PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('GASCO display name', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('GASCO negotiator name', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('GASCO information link', PropertyDefinition.SUB_PROP)]
                    ],
                    [group:new PropertyDefinitionGroup(name: 'meinKonsortium', description: 'alle für meine Konsortialstelle relevanten Merkmale', ownerType: Subscription.class.name, tenant: current, isVisible: true),
                     items:[PropertyDefinition.getByNameAndDescr('Open country-wide', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Restricted user group', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Perennial term', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Perennial term checked', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Due date for volume discount', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Newcomer discount', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Price increase', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Pricing advantage by licensing of another product', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Private institutions', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Product dependency', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Discount', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Scale of discount', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Calculation of discount', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Billing done by provider', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Time of billing', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('reverse charge', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Sim-User Number', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Partial payment', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Time span for testing', PropertyDefinition.SUB_PROP),
                            PropertyDefinition.getByNameAndDescr('Joining during the period', PropertyDefinition.SUB_PROP)]
                    ],
                    [group:new PropertyDefinitionGroup(name: 'Open Access', description: 'Open Access vereinbart', ownerType: License.class.name, tenant: current, isVisible: true),
                     items:[PropertyDefinition.getByNameAndDescr('OA Note', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('OA Last Date', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('OA First Date', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('Offsetting', PropertyDefinition.LIC_PROP),
                            PropertyDefinition.getByNameAndDescr('Open Access', PropertyDefinition.LIC_PROP)]
                    ]
            ]
            propertyDefinitionGroups.each { pdg ->
                if(pdg.group.save()) {
                    pdg.items.each { pd ->
                        PropertyDefinitionGroupItem pdgi = new PropertyDefinitionGroupItem(propDef: pd, propDefGroup: pdg.group)
                        if(!pdgi.save()) {
                            errors.add(pdgi.errors.toString())
                            return false
                        }
                    }
                }
                else {
                    errors.add(pdg.group.errors.toString())
                    return false
                }
            }
        }
        Org modelMember = Org.findByName(messageSource.getMessage('org.setup.modelOrgName',argv0,LocaleContextHolder.getLocale()))
        if(!modelMember) {
            log.info("creating model member org ...")
            Object[] argv1 = [current.shortname]
            modelMember = new Org(name: messageSource.getMessage('org.setup.modelOrgName',argv0,LocaleContextHolder.getLocale()),
                    sortname: messageSource.getMessage('org.setup.modelOrgSortname',argv1,LocaleContextHolder.getLocale()),
                    url: 'www.mustereinichtung.de', urlGov: 'www.muster_uni.de', status: RDStore.O_STATUS_CURRENT,
                    libraryType: RefdataValue.getByValueAndCategory('Universität', RDConstants.LIBRARY_TYPE),
                    libraryNetwork: RefdataValue.getByValueAndCategory('No Network', RDConstants.LIBRARY_NETWORK),
                    region: current.region ,country: current.country,
                    orgType: [RDStore.OT_INSTITUTION],
                    sector: RefdataValue.getByValueAndCategory('Higher Education', RDConstants.ORG_SECTOR))
            if(!modelMember.save()) {
                errors.add(modelMember.errors.toString())
                return false
            }
            modelMember.setDefaultCustomerType()
            OrgProperty op = new OrgProperty(owner: modelMember, type: privateProperties.get('BGA'), refValue: RDStore.YN_YES, tenant: modelMember, isPublic: false)
            if(!op.save()) {
                errors.add(op.errors.toString())
                return false
            }
            Map legalPatronAddressMap = [type:RefdataValue.getByValueAndCategory('Legal patron address', RDConstants.ADDRESS_TYPE),
                                         name:'Rechtlicher Träger',
                                         street_1:'Universitätsstraße',
                                         street_2:'1',
                                         zipcode:'55555',
                                         city:'Musterhausen',
                                         region:RefdataValue.getByValueAndCategory('North Rhine-Westphalia',
                                                 RDConstants.REGIONS_DE),
                                         country:RefdataValue.getByValueAndCategory('DE', RDConstants.COUNTRY),
                                         org:modelMember]
            Map postalAddressMap = [type:RefdataValue.getByValueAndCategory('Postal address', RDConstants.ADDRESS_TYPE),
                                    name:'Bibliothek',
                                    additionFirst:'Erwerbungsabteilung',
                                    street_1:'Musterstraße',
                                    street_2:'1',
                                    zipcode:'55555',
                                    city:'Musterhausen',
                                    region:RefdataValue.getByValueAndCategory('North Rhine-Westphalia',RDConstants
                                            .REGIONS_DE),
                                    country:RefdataValue.getByValueAndCategory('DE', RDConstants.COUNTRY),
                                    org:modelMember]
            Set<Map> addresses = [legalPatronAddressMap,postalAddressMap]
            addresses.each { addressData ->
                Address address = new Address(addressData)
                if(!address.save()) {
                    errors.add(address.errors.toString())
                    return false
                }
            }
        }
        Org member1Aachen = Org.findByGlobalUID('org:51c0f85a-0e96-43d4-ad6f-70011114643f') //check
        Org member2Zittau = Org.findByGlobalUID('org:31e65f48-a279-403e-8b58-42605a88e9a3') //check
        Org member3Munich = Org.findByGlobalUID('org:da7fedd0-c5c3-4430-a249-12450d55efd5') //check
        Org member4Greifswald = Org.findByGlobalUID('org:e1c1179f-29d0-4dcd-853e-f28c25476f17') //check
        Org member5Bremen = Org.findByGlobalUID('org:e6b3bd22-1a93-47c7-aa92-a8ad9b607b46') //check
        Set<Org> consortialMembers = [member1Aachen,member2Zittau,member3Munich,member4Greifswald,member5Bremen,modelMember]
        consortialMembers.each { member ->
            if(!Combo.findByFromOrgAndToOrg(member,current)) {
                Combo memberCombo = new Combo(fromOrg:member,toOrg:current,type:RDStore.COMBO_TYPE_CONSORTIUM)
                if(!memberCombo.save()) {
                    errors.add(memberCombo.errors.toString())
                    return false
                }
            }
        }
        Map<String,Map> consortialPartnerIdentifiers = [modelMember:[:],member1Aachen:[:],member2Zittau:[:],member3Munich:[:],member4Greifswald:[:],member5Bremen:[:]]
        consortialPartnerIdentifiers.each { k,v ->
            consortialPartnerIdentifiers[k].expiredDatenAIdentifier = UUID.randomUUID() //ex subscription #53
            consortialPartnerIdentifiers[k].currentDatenAIdentifier = UUID.randomUUID() //ex subscription #56
            consortialPartnerIdentifiers[k].testDatenAIdentifier = UUID.randomUUID() //ex subscription #80
            consortialPartnerIdentifiers[k].currentDatenbankIdentifier = UUID.randomUUID() //ex subscription #41
            consortialPartnerIdentifiers[k].currentDatenbank2Identifier = UUID.randomUUID() //ex subscription #61
            consortialPartnerIdentifiers[k].intendedDatenbank2Identifier = UUID.randomUUID() //ex subscription #67
            consortialPartnerIdentifiers[k].eBookPickIdentifier = UUID.randomUUID() //ex subscription #44
            consortialPartnerIdentifiers[k].journalPaketExtremIdentifier = UUID.randomUUID() //ex subscription #17
        }
        if(!CostItemElementConfiguration.findByForOrganisation(current)) {
            log.info("creating cost item element configurations ...")
            Set<CostItemElementConfiguration> ciecs = [
                    new CostItemElementConfiguration(forOrganisation: current,
                            costItemElement: generalData.costItemElements.get('price: consortial price'),
                            elementSign: RDStore.CIEC_POSITIVE),
                    new CostItemElementConfiguration(forOrganisation: current,
                            costItemElement: generalData.costItemElements.get('price: list price'),
                            elementSign: RDStore.CIEC_POSITIVE),
                    new CostItemElementConfiguration(forOrganisation: current,
                            costItemElement: generalData.costItemElements.get('discount: consortial discount'),
                            elementSign: RDStore.CIEC_NEGATIVE),
                    new CostItemElementConfiguration(forOrganisation: current,
                            costItemElement: generalData.costItemElements.get('special funds: central funding'),
                            elementSign: RDStore.CIEC_NEUTRAL)
            ]
            ciecs.each { ciec ->
                if(!ciec.save()) {
                    errors.add(ciec.errors.toString())
                    return false
                }
            }
        }
        if(!Person.findByTenant(current)) {
            log.info("creating private contacts ...")
            Map erwerbung = [mainParams:[
                    last_name: 'Erwerbung',
                    contactType: RDStore.CONTACT_TYPE_FUNCTIONAL,
                    tenant: current,
                    isPublic: false
            ],
                             addParams: [
                                     contact: [[contentType: RDStore.CCT_EMAIL,
                                                type: generalData.contactTypes.get('Job-related'),
                                                content: 'erwerbung@xxx.de']
                                     ],
                                     personRoles: [[org:member5Bremen,
                                                    functionType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]]
                             ]
            ]
            Map jamesFrank = [mainParams:[
                    first_name: 'James',
                    last_name: 'Frank',
                    gender: generalData.gender.get('Male'),
                    contactType: RDStore.CONTACT_TYPE_PERSONAL,
                    tenant: current,
                    isPublic: false
            ],
                              addParams: [
                                      contact: [[contentType: RDStore.CCT_EMAIL,
                                                 type: generalData.contactTypes.get('Job-related'),
                                                 content: 'f.james@xyz.com'],
                                                [contentType: RDStore.CCT_PHONE,
                                                 type: generalData.contactTypes.get('Job-related'),
                                                 content: '0045-4567 345']
                                      ],
                                      personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter E-Journals'),
                                                     functionType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]]
                              ]
            ]
            Map peterKlein = [mainParams:[
                    first_name: 'Peter',
                    last_name: 'Klein',
                    gender: generalData.gender.get('Male'),
                    contactType: RDStore.CONTACT_TYPE_PERSONAL,
                    tenant: current,
                    isPublic: false
            ],
                              addParams: [
                                      contact: [[contentType: RDStore.CCT_EMAIL,
                                                 type: generalData.contactTypes.get('Job-related'),
                                                 content: 'p.klein@anbieter.de']],
                                      personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter A'),
                                                     functionType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]]
                              ]
            ]
            Map miaMeyer = [mainParams:[
                    first_name: 'Mia',
                    last_name: 'Meyer',
                    gender: generalData.gender.get('Female'),
                    contactType: RDStore.CONTACT_TYPE_PERSONAL,
                    tenant: current,
                    isPublic: false
            ],
                            addParams: [
                                    contact: [[contentType: RDStore.CCT_EMAIL,
                                               type: generalData.contactTypes.get('Job-related'),
                                               content: 'meyer@gg.de']],
                                    personRoles: [[org:member4Greifswald,
                                                   functionType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]]
                            ]
            ]
            Map peterMiller = [mainParams:[
                    first_name: 'Peter',
                    last_name: 'Miller',
                    gender: generalData.gender.get('Male'),
                    contactType: RDStore.CONTACT_TYPE_PERSONAL,
                    tenant: current,
                    isPublic: false
            ],
                               addParams: [
                                       contact: [[contentType: RDStore.CCT_EMAIL,
                                                  type: generalData.contactTypes.get('Job-related'),
                                                  content: 'miller@e-books.com']],
                                       personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter E-Books'),
                                                      functionType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]]
                               ]
            ]
            Map rechnungsadresse = [mainParams: [
                    last_name: 'Rechnungsadresse',
                    contactType: RDStore.CONTACT_TYPE_FUNCTIONAL,
                    tenant: current,
                    isPublic: false
            ],
                                    addParams: [
                                            address: [[type:RefdataValue.getByValueAndCategory('Postal address', RDConstants.ADDRESS_TYPE),
                                                       additionFirst:'Erwerbungsabteilung',
                                                       street_1:'Musterhaus',
                                                       street_2:'1',
                                                       zipcode:'11111',
                                                       city:'Bremen',
                                                       region:RefdataValue.getByValueAndCategory('Bremen',RDConstants
                                                               .REGIONS_DE),
                                                       country:RefdataValue.getByValueAndCategory('DE', RDConstants.COUNTRY)]],
                                            personRoles: [[org:member5Bremen,
                                                           functionType: RefdataValue.getByValueAndCategory('Functional Contact Postal Address',RDConstants.PERSON_FUNCTION)]]]
            ]
            Map technischerSupport = [mainParams: [
                    last_name: 'Technischer Support',
                    contactType: RDStore.CONTACT_TYPE_FUNCTIONAL,
                    tenant: current,
                    isPublic: false
            ],
                                      addParams: [
                                              contact:[[contentType: RDStore.CCT_EMAIL,
                                                        type: generalData.contactTypes.get('Job-related'),
                                                        content: 'it_support@wef.com']],
                                              personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter E-Books'),
                                                             functionType: RefdataValue.getByValueAndCategory('Technical Support',RDConstants.PERSON_FUNCTION)]]]
            ]
            Map franzEmil = [mainParams: [
                    first_name:'franz',
                    last_name:'emil',
                    gender: generalData.gender.get('Third Gender'),
                    contactType: RDStore.CONTACT_TYPE_PERSONAL,
                    tenant: current,
                    isPublic: false
            ],
                             addParams: [
                                     contact: [[contentType:RDStore.CCT_EMAIL,
                                                type: generalData.contactTypes.get('Job-related'),
                                                content: 'stats@eugf.com']],
                                     personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter E-Books'),
                                                    functionType: RefdataValue.getByValueAndCategory('Statistical Support',RDConstants.PERSON_FUNCTION)]]]
            ]
            Map annaMueller = [mainParams:[
                    first_name: 'anna',
                    last_name: 'müller',
                    gender: generalData.gender.get('Female'),
                    contactType: RDStore.CONTACT_TYPE_PERSONAL,
                    tenant: current,
                    isPublic: false
            ],
                               addParams: [
                                       contact: [[contentType: RDStore.CCT_EMAIL,
                                                  type: generalData.contactTypes.get('Job-related'),
                                                  content: 'a.mueller@muster.de']],
                                       personRoles: [[org:member1Aachen,
                                                      functionType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]]
                               ]
            ]
            Map salesTeam = [mainParams: [
                    last_name: 'sales-team',
                    contactType: RDStore.CONTACT_TYPE_FUNCTIONAL,
                    tenant: current,
                    isPublic: false
            ],
                             addParams: [
                                     contact:[[contentType: RDStore.CCT_EMAIL,
                                               type: generalData.contactTypes.get('Job-related'),
                                               content: 'sales_team@muster.de']],
                                     personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter B'),
                                                    functionType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]]]
            ]
            Map samSmith = [mainParams:[
                    first_name: 'sam',
                    last_name: 'smith',
                    gender: generalData.gender.get('Male'),
                    contactType: RDStore.CONTACT_TYPE_PERSONAL,
                    tenant: current,
                    isPublic: false
            ], addParams: [
                    contact: [[contentType: RDStore.CCT_EMAIL,
                               type: generalData.contactTypes.get('Job-related'),
                               content: 'rechnungen@ewewf.com']],
                    personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter E-Journals'),
                                   functionType: RefdataValue.getByValueAndCategory('Functional Contact Billing Adress',RDConstants.PERSON_FUNCTION)]]]
            ]
            Set<Map> personalContacts = [erwerbung,jamesFrank,peterKlein,miaMeyer,peterMiller,rechnungsadresse,technischerSupport,franzEmil,annaMueller,salesTeam,samSmith]
            personalContacts.each { contact ->
                createObject('Person',contact,current,null)
            }
        }
        log.info("creating consortial licenses ...")
        Map rahmenvertragParams = [
                mainParams: [reference: "Rahmenvertrag",
                             type: generalData.licenseType,
                             status: RDStore.LICENSE_CURRENT,
                             startDate: generalData.sdf.parse('2017-01-01')],
                addParams: [licensors: [
                        [generalData.exampleOrgs.get('Musteranbieter A')]
                ],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('ILL electronic', PropertyDefinition.LIC_PROP).id,refValue:RDStore.PERM_PERM_EXPL,isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Governing jurisdiction', PropertyDefinition.LIC_PROP).id,stringValue:'Berlin',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Open Access', PropertyDefinition.LIC_PROP).id,refValue:RefdataValue.getByValueAndCategory('No Open Access', RDConstants.LICENSE_OA_TYPE),isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Invoicing', PropertyDefinition.LIC_PROP).id,dateValue: null,paragraph: 'Immer im Laufzeitjahr...']
                            ],
                            createMemberLicense: true,
                            licenseDocuments: [[docstoreUUID:'c39aef73-5dae-48ab-9656-08d8a2d4dea3',isShared:true],[docstoreUUID:'3d08595f-2069-44e6-ae27-ad31b499e365']]
                ]
        ]
        Map rahmenvertragEBookParams = [
                mainParams: [reference: "Rahmenvertrag eBook",
                             type: generalData.licenseType,
                             status: RDStore.LICENSE_CURRENT,
                             startDate: generalData.sdf.parse('2019-01-01')],
                addParams: [licensors: [
                        [generalData.exampleOrgs.get('Musteranbieter E-Books')]
                ],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('ILL secure electronic transmission', PropertyDefinition.LIC_PROP).id,refValue:RDStore.PERM_PROH_EXPL,isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Governing law', PropertyDefinition.LIC_PROP).id,stringValue:'deutsches Recht',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Open Access', PropertyDefinition.LIC_PROP).id,
                                        refValue:RefdataValue.getByValueAndCategory('No Open Access', RDConstants.LICENSE_OA_TYPE), isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Metadata delivery', PropertyDefinition.LIC_PROP).id,stringValue: null]
                            ],
                            createMemberLicense: true,
                            licenseDocuments: [[docstoreUUID:'7a3384ac-a5f4-4e06-a2af-dbb63062ef02',isShared:true],[docstoreUUID:'ac0de3a6-a726-4da1-b17a-782fc225a3fc']]
                ]
        ]
        Set<Map> consortialLicenseParamsSet = [rahmenvertragParams,rahmenvertragEBookParams]
        Map<String,License> consortialLicenses = [:]
        consortialLicenseParamsSet.each { licParam ->
            License lic = License.findByReference(licParam.mainParams.reference)
            if(!lic)
                lic = (License) createObject("License",licParam,current,null)
            consortialLicenses.put(lic.reference,lic)
            License.executeQuery("select oo.lic from OrgRole oo where oo.lic.reference = :memberRef and oo.org = :consortium",[memberRef:"${lic.reference} (Teilnehmervertrag)",consortium:current]).each { memLicObj ->
                License memberLic = (License) memLicObj
                consortialLicenses.put(memberLic.reference,memberLic)
            }
        }
        log.info("creating consortial subscriptions ...")
        String expiredDatenAIdentifier = UUID.randomUUID() //ex subscription #53
        String currentDatenAIdentifier = UUID.randomUUID() //ex subscription #56
        String testDatenAIdentifier = UUID.randomUUID() //ex subscription #80
        String currentDatenbankIdentifier = UUID.randomUUID() //ex subscription #41
        String currentDatenbank2Identifier = UUID.randomUUID() //ex subscription #61
        String intendedDatenbank2Identifier = UUID.randomUUID() //ex subscription #67
        String eBookPickIdentifier = UUID.randomUUID() //ex subscription #44
        String journalPaketExtremIdentifier = UUID.randomUUID() //ex subscription #17
        Map expiredDatenAParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: 'Daten A',
                             startDate: generalData.dates.previousYearRingStart,
                             endDate: generalData.dates.previousYearRingEnd,
                             manualCancellationDate: generalData.sdf.parse('2019-10-31'),
                             status: RDStore.SUBSCRIPTION_EXPIRED,
                             identifier: expiredDatenAIdentifier,
                             isPublic: false,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('mixed'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('duz: Deutsche Universitätszeitung')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter A'),
                            sharedProperties:['name','form','resource'],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO information link', PropertyDefinition.SUB_PROP).id,urlValue:'https://www...',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Restricted user group', PropertyDefinition.SUB_PROP).id,stringValue:'Universitäten'],
                                    [type:PropertyDefinition.getByNameAndDescr('Newcomer discount', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES,note:'25% auf den Jahrespreis'],
                                    [type:PropertyDefinition.getByNameAndDescr('Price rounded', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_NO],
                                    [type:PropertyDefinition.getByNameAndDescr('Billing done by provider', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_NO],
                                    [type:PropertyDefinition.getByNameAndDescr('Sim-User Number', PropertyDefinition.SUB_PROP).id,stringValue:'unlimitiert',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Time span for testing', PropertyDefinition.SUB_PROP).id,stringValue:'90']
                            ],
                            subscriptionMembers: [[subMember:member1Aachen,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member1Aachen.expiredDatenAIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member1Aachen.expiredDatenAIdentifier),
                                                                billingSum:1500,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.previousYearRingStart,
                                                                endDate: generalData.dates.previousYearRingEnd,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member3Munich,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member3Munich.expiredDatenAIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member3Munich.expiredDatenAIdentifier),
                                                                billingSum:1500,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.previousYearRingStart,
                                                                endDate: generalData.dates.previousYearRingEnd,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]]
                            ],
                ]
        ]
        Map currentDatenAParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: 'Daten A',
                             startDate: generalData.dates.defaultStartDate,
                             endDate: generalData.dates.defaultEndDate,
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: currentDatenAIdentifier,
                             isPublic: false,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('mixed'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('duz: Deutsche Universitätszeitung')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter A'),
                            sharedProperties:['name','form','resource'],
                            subscriptionDocuments:[[docstoreUUID:'cef94ff5-16b4-4470-8165-4e198865d232']],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO information link', PropertyDefinition.SUB_PROP).id,urlValue:'https://www...'],
                                    [type:PropertyDefinition.getByNameAndDescr('Restricted user group', PropertyDefinition.SUB_PROP).id,stringValue:'Universitäten'],
                                    [type:PropertyDefinition.getByNameAndDescr('Newcomer discount', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES,note:'25% auf den Jahrespreis'],
                                    [type:PropertyDefinition.getByNameAndDescr('Price rounded', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_NO],
                                    [type:PropertyDefinition.getByNameAndDescr('Billing done by provider', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_NO],
                                    [type:PropertyDefinition.getByNameAndDescr('Sim-User Number', PropertyDefinition.SUB_PROP).id,stringValue:'unlimitiert'],
                                    [type:PropertyDefinition.getByNameAndDescr('Time span for testing', PropertyDefinition.SUB_PROP).id,stringValue:'90']
                            ],
                            subscriptionMembers: [[subMember:member1Aachen,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member1Aachen.currentDatenAIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member1Aachen.currentDatenAIdentifier),
                                                                billingSum:3500,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member5Bremen,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member5Bremen.currentDatenAIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member5Bremen.currentDatenAIdentifier),
                                                                billingSum:3500,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member4Greifswald,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member4Greifswald.currentDatenAIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member4Greifswald.currentDatenAIdentifier),
                                                                billingSum:3500,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:modelMember,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.modelMember.currentDatenAIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.modelMember.currentDatenAIdentifier),
                                                                billingSum:2000,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.startDateCurrent,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member3Munich,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member3Munich.currentDatenAIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member3Munich.currentDatenAIdentifier),
                                                                billingSum:3500,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]]
                            ],
                ]
        ]
        Map testDatenAParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: 'Daten A (Test)',
                             status: RefdataValue.getByValueAndCategory('Test Access', RDConstants.SUBSCRIPTION_STATUS),
                             identifier: testDatenAIdentifier,
                             isPublic: false,
                             form: generalData.subscriptionForms.get('test')],
                addParams: [subscriptionMembers:[[subMember:modelMember,
                                                  subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                  startDate:generalData.dates.startDateExpired,
                                                  endDate:generalData.dates.endDateExpired,
                                                  subIdentifier:consortialPartnerIdentifiers.modelMember.testDatenAIdentifier],
                                                 [subMember:member3Munich,
                                                  subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                  startDate:generalData.sdf.parse('2019-05-01'),
                                                  endDate:generalData.sdf.parse('2019-05-31'),
                                                  subIdentifier:consortialPartnerIdentifiers.member3Munich.testDatenAIdentifier],
                                                 [subMember:member4Greifswald,
                                                  subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                  startDate:generalData.dates.startDateExpired,
                                                  endDate:generalData.dates.endDateExpired,
                                                  subIdentifier:consortialPartnerIdentifiers.member4Greifswald.testDatenAIdentifier]]]
        ]
        Map currentDatenbankParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: 'Datenbank',
                             startDate: generalData.dates.defaultStartDate,
                             endDate: generalData.dates.defaultEndDate,
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: currentDatenbankIdentifier,
                             isPublic: false,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('database'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('Brepols: Bibliography of British and Irish History')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter B'),
                            sharedProperties:['name','form','resource'],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO information link', PropertyDefinition.SUB_PROP).id,urlValue:'https://www...',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Price rounded', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_NO],
                                    [type:PropertyDefinition.getByNameAndDescr('Discount', PropertyDefinition.SUB_PROP).id,stringValue:'22 %'],
                                    [type:PropertyDefinition.getByNameAndDescr('Scale of discount', PropertyDefinition.SUB_PROP).id,stringValue:null],
                                    [type:PropertyDefinition.getByNameAndDescr('Billing done by provider', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_NO,isShared:true]
                            ],
                            subscriptionMembers: [[subMember:member1Aachen,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member1Aachen.currentDatenbankIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member1Aachen.currentDatenbankIdentifier),
                                                                billingSum:3000,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member4Greifswald,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member4Greifswald.currentDatenbankIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member4Greifswald.currentDatenbankIdentifier),
                                                                billingSum:2500,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true],
                                                               [sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member4Greifswald.currentDatenbankIdentifier),
                                                                billingSum:2500,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('special funds: central funding'),
                                                                costItemElementConfiguration: RDStore.CIEC_NEUTRAL,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member3Munich,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member3Munich.currentDatenbankIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member3Munich.currentDatenbankIdentifier),
                                                                billingSum:2000,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('special funds: central funding'),
                                                                costItemElementConfiguration: RDStore.CIEC_NEUTRAL,
                                                                isVisibleForSubscriber: true],
                                                               [sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member3Munich.currentDatenbankIdentifier),
                                                                billingSum:4000,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member2Zittau,
                                                   startDate:generalData.dates.startDateExpired,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member2Zittau.currentDatenbankIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member2Zittau.currentDatenbankIdentifier),
                                                                billingSum:2500,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]]
                            ],
                ]
        ]
        Map currentDatenbank2Params = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: 'Datenbank 2',
                             startDate: generalData.dates.defaultStartDate,
                             endDate: generalData.dates.defaultEndDate,
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: currentDatenbank2Identifier,
                             isPublic: false,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('database'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('Naxos: Naxos Music Library World')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter A'),
                            sharedProperties:['name','form','resource'],
                            subscriptionDocuments:[[docstoreUUID:'dc9a8b48-355e-4c47-8ffb-6bcbed527ba0',isShared:true]],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO information link', PropertyDefinition.SUB_PROP).id,urlValue:null,isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO negotiator name', PropertyDefinition.SUB_PROP).id,stringValue:'Allianzlizenz Team'],
                                    [type:PropertyDefinition.getByNameAndDescr('Perennial term', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_NO,note:'opt out Klausel vorhanden',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Perennial term checked', PropertyDefinition.SUB_PROP).id,refValue:null,isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Price rounded', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('Time of billing', PropertyDefinition.SUB_PROP).id,stringValue:'Vorauszahlung',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Sim-User Number', PropertyDefinition.SUB_PROP).id,stringValue:'unlimitiert',isShared:true]
                            ],
                            subscriptionMembers: [[subMember:member1Aachen,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   endDate:generalData.dates.nextYearRingEnd,
                                                   subIdentifier:consortialPartnerIdentifiers.member1Aachen.currentDatenbank2Identifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member1Aachen.currentDatenbank2Identifier),
                                                                billingSum:4500,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAX_NOT_APPLICABLE,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.nextYearRingEnd,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member5Bremen,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member5Bremen.currentDatenbank2Identifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member5Bremen.currentDatenbank2Identifier),
                                                                billingSum:5000,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAX_NOT_APPLICABLE,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member4Greifswald,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   endDate:generalData.dates.nextYearRingEnd,
                                                   subIdentifier:consortialPartnerIdentifiers.member4Greifswald.currentDatenbank2Identifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member4Greifswald.currentDatenbank2Identifier),
                                                                billingSum:1000,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAX_NOT_APPLICABLE,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.nextYearRingEnd,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member3Munich,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   endDate:generalData.dates.nextYearRingEnd,
                                                   subIdentifier:consortialPartnerIdentifiers.member3Munich.currentDatenbank2Identifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member3Munich.currentDatenbank2Identifier),
                                                                billingSum:5000,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAX_NOT_APPLICABLE,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.nextYearRingEnd,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member2Zittau,
                                                   endDate:generalData.dates.nextYearRingEnd,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member2Zittau.currentDatenbank2Identifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member2Zittau.currentDatenbank2Identifier),
                                                                billingSum:2500,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAX_NOT_APPLICABLE,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.nextYearRingEnd,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]]
                            ],
                ]
        ]
        Map intendedDatenbank2Params = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: 'Datenbank 2',
                             startDate: generalData.dates.nextYearRingStart,
                             endDate: generalData.dates.nextYearRingEnd,
                             status: RDStore.SUBSCRIPTION_INTENDED,
                             identifier: intendedDatenbank2Identifier,
                             isPublic: false,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('database'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('Naxos: Naxos Music Library World')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter A'),
                            sharedProperties:['name','form','resource'],
                            subscriptionDocuments:[[docstoreUUID:'1b28ccb2-9fd4-4652-8c6c-02f9df41f653',isShared:true]],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO information link', PropertyDefinition.SUB_PROP).id,urlValue:null,isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO negotiator name', PropertyDefinition.SUB_PROP).id,stringValue:'Allianzlizenz Team'],
                                    [type:PropertyDefinition.getByNameAndDescr('Perennial term', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_NO,note:'opt out Klausel vorhanden',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Perennial term checked', PropertyDefinition.SUB_PROP).id,refValue:null],
                                    [type:PropertyDefinition.getByNameAndDescr('Price rounded', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('Time of billing', PropertyDefinition.SUB_PROP).id,stringValue:'Vorauszahlung',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Sim-User Number', PropertyDefinition.SUB_PROP).id,stringValue:'unlimitiert',isShared:true]
                            ],
                            subscriptionMembers: [[subMember:member1Aachen,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member1Aachen.intendedDatenbank2Identifier],
                                                  [subMember:member4Greifswald,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member4Greifswald.intendedDatenbank2Identifier],
                                                  [subMember:member3Munich,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member3Munich.intendedDatenbank2Identifier],
                                                  [subMember:member2Zittau,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member2Zittau.intendedDatenbank2Identifier]
                            ],
                ]
        ]
        Map eBookPickParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: 'E-Book-Pick',
                             startDate: generalData.dates.defaultStartDate,
                             endDate: generalData.dates.defaultEndDate,
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: eBookPickIdentifier,
                             isPublic: false,
                             form: generalData.subscriptionForms.get('singlePurchase'),
                             resource: generalData.subscriptionResources.get('ebookSingle'),
                             owner: consortialLicenses.get('Rahmenvertrag eBook')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('UTB Alle Titel')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter E-Books'),
                            sharedProperties:['name','form','resource'],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO information link', PropertyDefinition.SUB_PROP).id,urlValue:'https://musterlink.de',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Open country-wide', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_NO,note:'regionales Konsortium'],
                                    [type:PropertyDefinition.getByNameAndDescr('Due date for volume discount', PropertyDefinition.SUB_PROP).id,dateValue:generalData.sdf.parse('2020-11-30')],
                                    [type:PropertyDefinition.getByNameAndDescr('Price rounded', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('Sim-User Number', PropertyDefinition.SUB_PROP).id,stringValue:'unlimitiert',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Time span for testing', PropertyDefinition.SUB_PROP).id,stringValue:'60'],
                                    [type:PropertyDefinition.getByNameAndDescr('Joining during the period', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES]
                            ],
                            privateProperties: [
                                    [type:PropertyDefinition.findByNameAndTenant('Quellensteuer-Befreiung',current).id,refValue:RefdataValue.getByValueAndCategory('Planed',RDConstants.Y_N_O),note:'der Anbieter hat dies beantragt']
                            ],
                            subscriptionMembers: [[subMember:member1Aachen,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag eBook (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member1Aachen.eBookPickIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member1Aachen.eBookPickIdentifier),
                                                                billingSum:5000,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_7,
                                                                subPkg: generalData.examplePackages.get('UTB Alle Titel'),
                                                                startDate: generalData.dates.previousYearRingStart,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member4Greifswald,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag eBook (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member4Greifswald.eBookPickIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member4Greifswald.eBookPickIdentifier),
                                                                billingSum:22000,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_7,
                                                                subPkg: generalData.examplePackages.get('UTB Alle Titel'),
                                                                startDate: generalData.dates.previousYearRingStart,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true],
                                                               [sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member4Greifswald.eBookPickIdentifier),
                                                                billingSum:22000,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_7,
                                                                subPkg: generalData.examplePackages.get('UTB Alle Titel'),
                                                                startDate: generalData.dates.previousYearRingStart,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('special funds: central funding'),
                                                                costItemElementConfiguration: RDStore.CIEC_NEUTRAL,
                                                                isVisibleForSubscriber: true]
                                                   ]],
                                                  [subMember:member3Munich,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag eBook (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member3Munich.eBookPickIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member3Munich.eBookPickIdentifier),
                                                                billingSum:110000,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_7,
                                                                subPkg: generalData.examplePackages.get('UTB Alle Titel'),
                                                                startDate: generalData.dates.previousYearRingStart,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:modelMember,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag eBook (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.modelMember.eBookPickIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.modelMember.eBookPickIdentifier),
                                                                billingSum:24000,
                                                                currency:generalData.currencies.get('EUR'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_7,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]]
                            ],
                ]
        ]
        Map journalPaketExtremParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                             name: 'Journal-Paket_Extrem',
                             startDate: generalData.dates.defaultStartDate,
                             endDate: generalData.dates.defaultEndDate,
                             manualCancellationDate: generalData.sdf.parse('2020-10-31'),
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: journalPaketExtremIdentifier,
                             isPublic: false,
                             form: generalData.subscriptionForms.get('purchaseOngoing'),
                             resource: generalData.subscriptionResources.get('ejournalPackage'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('American Chemical Society: American Chemical Society Journals')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter E-Journals'),
                            sharedProperties:['name','form','resource'],
                            customProperties: [
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('GASCO information link', PropertyDefinition.SUB_PROP).id,urlValue:'https://www.hallo.de',isShared:true],
                                    [type:PropertyDefinition.getByNameAndDescr('Private institutions', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.getByNameAndDescr('Calculation of discount', PropertyDefinition.SUB_PROP).id,refValue:RDStore.YN_YES,note:'die Gesamtzahl der Teilnehmer pro Modul wird für die Findung der erreichten Rabattstufe summiert'],
                                    [type:PropertyDefinition.getByNameAndDescr('Sim-User Number', PropertyDefinition.SUB_PROP).id,stringValue:'unlimitiert',isShared:true]
                            ],
                            privateProperties: [
                                    [type:PropertyDefinition.findByNameAndTenant('Quellensteuer-Befreiung',current).id,refValue:RefdataValue.getByValueAndCategory('Unclear', RDConstants.Y_N_O),note:'dringend klären']
                            ],
                            tasks: [
                                    [title: 'Statistiken Counter5 & Sushi',
                                     status: RefdataValue.getByValueAndCategory('Open', RDConstants.TASK_STATUS),
                                     endDate: generalData.sdf.parse('2020-06-30'),
                                     responsibleOrg: current,
                                     description:'dringend mit dem Anbieter die Lieferung von Counter 5 via Sushi besprechen']
                            ],
                            subscriptionMembers: [[subMember:member1Aachen,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member1Aachen.journalPaketExtremIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member1Aachen.journalPaketExtremIdentifier),
                                                                billingSum:5600,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:modelMember,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag'),
                                                   subIdentifier:consortialPartnerIdentifiers.modelMember.journalPaketExtremIdentifier],
                                                  [subMember:member3Munich,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member3Munich.journalPaketExtremIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member3Munich.journalPaketExtremIdentifier),
                                                                billingSum:1200,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]],
                                                  [subMember:member2Zittau,
                                                   subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                   subIdentifier:consortialPartnerIdentifiers.member2Zittau.journalPaketExtremIdentifier,
                                                   costItems: [[sub:Subscription.findByIdentifier(consortialPartnerIdentifiers.member2Zittau.journalPaketExtremIdentifier),
                                                                billingSum:5600,
                                                                currency:generalData.currencies.get('USD'),
                                                                currencyRate:1.0,
                                                                localSum:0,
                                                                taxKey: CostItem.TAX_TYPES.TAXABLE_19,
                                                                startDate: generalData.dates.defaultStartDate,
                                                                endDate: generalData.dates.defaultEndDate,
                                                                status: generalData.costItemStatus.get('Actual'),
                                                                costItemElement: generalData.costItemElements.get('price: consortial price'),
                                                                costItemElementConfiguration: RDStore.CIEC_POSITIVE,
                                                                isVisibleForSubscriber: true]]]
                            ],
                ]
        ]
        Set<Map> consortialSubscriptionParams = [expiredDatenAParams,currentDatenAParams,testDatenAParams,currentDatenbankParams,currentDatenbank2Params,intendedDatenbank2Params,eBookPickParams,journalPaketExtremParams]
        consortialSubscriptionParams.each { consortialSubscription ->
            Set<Subscription> sub = OrgRole.executeQuery('select oo.sub from OrgRole oo where oo.sub.name = :name and oo.sub.status = :status and oo.org = :current',[name:consortialSubscription.mainParams.name,status:consortialSubscription.mainParams.status,current:current])
            if(!sub)
                createObject('Subscription',consortialSubscription,current,null)
            else {
                switch(sub[0].name) {
                    case expiredDatenAParams.mainParams.name:
                    case currentDatenAParams.mainParams.name:
                    case testDatenAParams.mainParams.name:
                        if(sub[0].status == RDStore.SUBSCRIPTION_EXPIRED)
                            expiredDatenAIdentifier = sub[0].identifier
                        if(sub[0].status == RDStore.SUBSCRIPTION_CURRENT)
                            currentDatenAIdentifier = sub[0].identifier
                        if(sub[0].status == RefdataValue.getByValueAndCategory('Test Access',RDConstants.SUBSCRIPTION_TYPE))
                            testDatenAIdentifier = sub[0].identifier
                        break
                    case currentDatenbankParams.mainParams.name: currentDatenbankIdentifier = sub[0].identifier
                        break
                    case currentDatenbank2Params.mainParams.name:
                    case intendedDatenbank2Params.mainParams.name:
                        if(sub[0].status == RDStore.SUBSCRIPTION_CURRENT)
                            currentDatenbank2Identifier = sub[0].identifier
                        if(sub[0].status == RDStore.SUBSCRIPTION_INTENDED)
                            intendedDatenbank2Identifier = sub[0].identifier
                        break
                    case eBookPickParams.mainParams.name: eBookPickIdentifier = sub[0].identifier
                        break
                    case journalPaketExtremParams.mainParams.name: journalPaketExtremIdentifier = sub[0].identifier
                        break
                }
            }
        }
        log.info("creating contacts with specific responsibilities ...")
        Map antonGross = [mainParams:[
                first_name: 'Anton',
                last_name: 'Gross',
                gender: generalData.gender.get('Male'),
                contactType: RDStore.CONTACT_TYPE_PERSONAL,
                tenant: current,
                isPublic: false
        ],
                          addParams: [
                                  contact: [[contentType: RDStore.CCT_EMAIL,
                                             type: generalData.contactTypes.get('Job-related'),
                                             content: 'gross@anbieter.de'],
                                            [contentType: RDStore.CCT_PHONE,
                                             type: generalData.contactTypes.get('Job-related'),
                                             content: '123 456 789']],
                                  personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter A'),
                                                 functionType: RefdataValue.getByValueAndCategory('Contact Person', RDConstants.PERSON_FUNCTION)],
                                                [org:generalData.exampleOrgs.get('Musteranbieter A'),
                                                 responsibilityType: RefdataValue.getByValueAndCategory('Specific subscription editor', RDConstants.PERSON_RESPONSIBILITY),
                                                 sub:Subscription.findByIdentifier(currentDatenbank2Identifier)]]
                          ]
        ]
        Set<Map> personalContactsWithSpecificResponsibilities = [antonGross]
        //this loop can be executed only if the objects to which the responsibility is bound to has been created already
        personalContactsWithSpecificResponsibilities.each { contact ->
            if(!Person.findByTenantAndFirst_nameAndLast_name(current,antonGross.first_name,antonGross.last_name))
                createObject('Person',contact,current,null)
        }
        log.info("create subscription linkings ...")
        setupLinking([owner:current,source:Subscription.findByIdentifier(currentDatenAIdentifier).id,destination:Subscription.findByIdentifier(currentDatenbank2Identifier).id,objectType:Subscription.class.name,linkType:RefdataValue.getByValueAndCategory('is condition for',RDConstants.LINK_TYPE)])
        Set<Map> subIdentifiersWithChildrenToLink = [
                [source:currentDatenAIdentifier,destination:expiredDatenAIdentifier],
                [source:intendedDatenbank2Identifier,destination:currentDatenbank2Identifier]
        ]
        subIdentifiersWithChildrenToLink.each { pair ->
            Subscription source = Subscription.findByIdentifier(pair.source)
            Subscription destination = Subscription.findByIdentifier(pair.destination)
            if(!Links.findBySourceAndDestination(source.id,destination.id))
                setupLinking([owner:current,source:source.id,destination:destination.id,objectType:Subscription.class.name,linkType:RDStore.LINKTYPE_FOLLOWS])
            source.derivedSubscriptions.each { childSub ->
                childSub.refresh()
                Org childSubscriber = childSub.orgRelations.find { it.roleType == RDStore.OR_SUBSCRIBER_CONS }.org
                Set<Subscription> childPairs = Subscription.executeQuery('select oo.sub from OrgRole oo where oo.sub.instanceOf = :destination and oo.org = :member',[destination:destination,member:childSubscriber])
                if(childPairs) {
                    Subscription childPair = childPairs.first()
                    if(!Links.findBySourceAndDestination(childSub.id,childPair.id))
                        setupLinking([owner:current,source:childSub.id,destination:childPair.id,objectType:Subscription.class.name,linkType:RDStore.LINKTYPE_FOLLOWS])
                }
            }
        }
    }

    /**
     * Creates an object with the given params and creates orgRoles for the given consortium and member organisations
     * @param objectType - the string key for the type of object to be created
     * @param params - the parameter map for the object to be processed. Expected structure:
     * [mainParams: params,
     *  addParams: [related object map]]
     *  where mainParams means the parameter map for the domain object itself and addParams additional params
     *  to be processed after the domain object creation
     * @param consortium - the consortium, can be the example consortium or the new consortium to be added
     * @param member - the member, can be the example member or the new member to be added
     * @throws CreationException
     */
    def createObject(String objectType,Map params,Org consortium, Org member) throws CreationException {
        def obj
        OrgRole memberRole
        OrgRole consRole
        //some integrity checks which should raise an exception ...
        if(!params.mainParams)
            throw new CreationException("Domain parameter map missing! What should I process?")
        switch(objectType) {
            case 'Subscription': obj = new Subscription(params.mainParams)
                switch(obj.type) {
                    case RDStore.SUBSCRIPTION_TYPE_CONSORTIAL:
                        if(member)
                            memberRole = new OrgRole(org:member,sub:obj,roleType:RDStore.OR_SUBSCRIBER_CONS)
                        consRole = new OrgRole(org:consortium,sub:obj,roleType:RDStore.OR_SUBSCRIPTION_CONSORTIA)
                        //obj.orgRelations = [memberRole,consRole]
                        obj.isSlaved = true
                        break
                    case RDStore.SUBSCRIPTION_TYPE_LOCAL:
                        memberRole = new OrgRole(org:member,sub:obj,roleType:RDStore.OR_SUBSCRIBER)
                        //obj.orgRelations = [memberRole]
                        break
                }
                break
            case 'License': obj = new License(params.mainParams)
                if(member) {
                    //this is the local counterpart for the consortial member license org role setup at the end of setupBasicTestData()
                    memberRole = new OrgRole(org:member,lic:obj,roleType:RDStore.OR_LICENSEE)
                    //obj.orgLinks = [memberRole]
                }
                else if(consortium) {
                    //this is the org role setup for consortia
                    consRole = new OrgRole(org:consortium,lic:obj,roleType:RDStore.OR_LICENSING_CONSORTIUM)
                    //obj.orgLinks = [consRole]
                }
                break
            case 'Person': obj = new Person(params.mainParams)
                break
            default: throw new CreationException("No domain class mapping found; implement it or check the type key!")
                break
        }
        log.info("attempting to save ${obj}")
        if(obj.save()) {
            if(memberRole && !memberRole.save()) {
                throw new CreationException(memberRole.errors)
            }
            if(consRole && !consRole.save()) {
                throw new CreationException(consRole.errors)
            }
        }
        else {
            throw new CreationException(obj.errors)
        }
        //now process the additional params
        if(params.addParams) {
            params.addParams.each { k,v ->
                switch(objectType) {
                    case 'Subscription':
                        switch(k) {
                            case 'packages': v.each { entry ->
                                    if(!entry.pkg)
                                        throw new CreationException("Package data missing!")
                                    else {
                                        Package pkg = (Package) entry.pkg
                                        if(entry.issueEntitlementISBNs) {
                                            pkg.addToSubscription(obj,false)
                                            List<TitleInstancePackagePlatform> tippSubset = TitleInstancePackagePlatform.executeQuery("select tipp from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg and tipp.id in (select ident.ti from Identifier ident where ident.ns.ns = 'isbn' and ident.value in (:idSet))", [pkg:pkg,idSet:entry.issueEntitlementISBNs])
                                            RefdataValue ieCurrent = RDStore.TIPP_STATUS_CURRENT
                                            tippSubset.each { tipp ->
                                                IssueEntitlement ie = new IssueEntitlement(status:ieCurrent,
                                                        subscription: obj,
                                                        tipp: tipp,
                                                        accessStartDate: tipp.accessStartDate,
                                                        accessEndDate: tipp.accessEndDate,
                                                        ieReason: 'Automatically copied when creating subscription',
                                                        acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED
                                                )
                                                if(!ie.save()) {
                                                    throw new CreationException(ie.errors)
                                                }
                                                else {
                                                    tipp.coverages.each { tippCoverage ->
                                                        IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage(
                                                                startDate: tippCoverage.startDate,
                                                                startVolume: tippCoverage.startVolume,
                                                                startIssue: tippCoverage.startIssue,
                                                                endDate: tippCoverage.endDate,
                                                                endVolume: tippCoverage.endVolume,
                                                                endIssue: tippCoverage.endIssue,
                                                                embargo: tippCoverage.embargo,
                                                                coverageDepth: tippCoverage.coverageDepth,
                                                                coverageNote: tippCoverage.coverageNote,
                                                                issueEntitlement: ie
                                                        )
                                                        if(!ieCoverage.save()) {
                                                            throw new CreationException(ieCoverage.errors)
                                                        }
                                                    }
                                                    log.info("issue entitlement ${ie} added to subscription ${obj}")
                                                }
                                            }
                                        }
                                        else {
                                            pkg.addToSubscription(obj,true)
                                        }
                                    }
                                }
                                break
                            case 'provider': OrgRole providerRole = new OrgRole(org:v,sub:obj,roleType:RDStore.OR_PROVIDER)
                                if(!providerRole.save())
                                    throw new CreationException(providerRole.errors)
                                break
                            case 'costItems':
                                v.each { entry ->
                                    switch(entry.sub) {
                                        case 'instanceOfCurrent': entry.owner = consortium
                                            break
                                        case 'currentOwn': entry.owner = member
                                            break
                                        default: throw new CreationException("Cost item ownership is not determined!")
                                            break
                                    }
                                    entry.sub = obj
                                    createCostItem(entry)
                                }
                                break
                            //beware: this switch processes only custom properties which are NOT shared by a consortial parent subscription!
                            case 'customProperties':
                            case 'privateProperties':
                                boolean isPublic = false
                                if(k == 'customProperties')
                                    isPublic = true
                                // causing a session mismatch
                                // AbstractProperty newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY,obj,PropertyDefinition.get(v.type))
                                v.each { property ->
                                    SubscriptionProperty newProp = new SubscriptionProperty(owner:obj,type:PropertyDefinition.get(property.type),isPublic:isPublic)
                                    newProp.note = property.note ?: null
                                    if(property.stringValue) {
                                        newProp.stringValue = property.stringValue
                                    }
                                    else if(property.urlValue) {
                                        newProp.urlValue = new URL(property.urlValue)
                                    }
                                    else if(property.decValue) {
                                        newProp.decValue = Double.parseDouble(property.decValue)
                                    }
                                    else if(property.intValue) {
                                        newProp.intValue = Integer.parseInt(property.intValue)
                                    }
                                    else if(property.refValue) {
                                        newProp.refValue = property.refValue
                                    }
                                    else if(property.dateValue) {
                                        newProp.dateValue = property.dateValue
                                    }
                                    if(!newProp.save())
                                        throw new CreationException(newProp.errors)
                                    if(property.isShared)
                                        AuditConfig.addConfig(newProp,AuditConfig.COMPLETE_OBJECT)
                                }
                                break
                            /*case 'privateProperties':
                                v.each { property ->
                                    SubscriptionPrivateProperty newProp = new SubscriptionPrivateProperty(owner:obj,type:PropertyDefinition.get(property.type))
                                    newProp.note = property.note ?: null
                                    if(property.stringValue) {
                                        newProp.stringValue = property.stringValue
                                    }
                                    else if(property.urlValue) {
                                        newProp.urlValue = new URL(property.urlValue)
                                    }
                                    else if(property.decValue) {
                                        newProp.decValue = Double.parseDouble(property.decValue)
                                    }
                                    else if(property.intValue) {
                                        newProp.intValue = Integer.parseInt(property.intValue)
                                    }
                                    else if(property.refValue) {
                                        newProp.refValue = property.refValue
                                    }
                                    else if(property.dateValue) {
                                        newProp.dateValue = property.dateValue
                                    }
                                    if(!newProp.save())
                                        throw new CreationException(newProp.errors)
                                }
                                break*/
                            case 'subscriptionMembers':
                                List synShareTargetList = []
                                v.each { entry ->
                                    Org subMember = (Org) entry.subMember
                                    Date startDate = entry.startDate ?: obj.startDate
                                    Date endDate = entry.endDate ?: obj.endDate
                                    RefdataValue status = entry.status ?: obj.status
                                    Subscription consSub = new Subscription(name: obj.name,
                                            startDate: startDate,
                                            endDate: endDate,
                                            manualCancellationDate: obj.manualCancellationDate,
                                            status: status,
                                            resource: obj.resource,
                                            form: obj.form,
                                            kind: obj.kind,
                                            type: obj.type,
                                            isPublicForApi: obj.isPublicForApi,
                                            isMultiYear: obj.isMultiYear,
                                            hasPerpetualAccess: obj.hasPerpetualAccess,
                                            owner: entry.subOwner,
                                            instanceOf: obj,
                                            identifier: entry.subIdentifier,
                                            globalUID: null)
                                    //InvokerHelper.setProperties(consSub,obj.properties)
                                    if(!consSub.save())
                                        throw new CreationException(consSub.errors)
                                    memberRole = new OrgRole(org:subMember,sub:consSub,roleType:RDStore.OR_SUBSCRIBER_CONS)
                                    //OrgRole memberLicenseRole = new OrgRole(org:subMember,lic:entry.subOwner,roleType: RDStore.OR_LICENSEE_CONS)
                                    consRole = new OrgRole(org:consortium,sub:consSub,roleType:RDStore.OR_SUBSCRIPTION_CONSORTIA)
                                    if(!memberRole.save()) {
                                        throw new CreationException(memberRole.errors)
                                    }
                                    /*if(!memberLicenseRole.save()) {
                                        throw new CreationException(memberLicenseRole.errors)
                                    }*/
                                    if(!consRole.save()) {
                                        throw new CreationException(consRole.errors)
                                    }
                                    List<OrgRole> providersAndAgencies = OrgRole.findAllBySubAndRoleTypeInList(obj,[RDStore.OR_PROVIDER,RDStore.OR_AGENCY])
                                    providersAndAgencies.each { orgRole ->
                                        OrgRole providerAgencyRole = new OrgRole(org: orgRole.org,sub: consSub,roleType: orgRole.roleType)
                                        if(!providerAgencyRole.save())
                                            throw new CreationException(providerAgencyRole.errors)
                                    }
                                    synShareTargetList.add(consSub)
                                    SubscriptionProperty.findAllByOwner(obj).each { scp ->
                                        AuditConfig ac = AuditConfig.getConfig(scp)
                                        if(ac) {
                                            //I do not understand what the difference in SubscriptionController is, so, I will not distinct here between multipleOccurrence or not
                                            AbstractPropertyWithCalculatedLastUpdated prop = new SubscriptionProperty(owner:consSub,type:scp.type)
                                            prop = scp.copyInto(prop)
                                            prop.instanceOf = scp
                                            prop.save()
                                        }
                                    }
                                    entry.costItems.each { ciParams ->
                                        ciParams.sub = consSub
                                        ciParams.owner = consortium
                                        createCostItem(ciParams)
                                    }
                                }
                                obj.refresh()
                                obj.syncAllShares(synShareTargetList)
                                break
                            case 'sharedProperties': v.each { property ->
                                    AuditConfig.addConfig(obj,property)
                                }
                                break
                            case 'subscriptionDocuments': v.each { entry ->
                                createDocument(entry, obj)
                                }
                                break
                            case 'tasks': v.each { t ->
                                Task task = new Task(subscription: obj,title: t.title,description: t.description,endDate: t.endDate,status: t.status,responsibleOrg: t.responsibleOrg ?: null, responsibleUser: t.responsibleUser ?: null, creator: contextService.user, systemCreateDate: new Date(), createDate: new Date())
                                if(!task.save()) {
                                    throw new CreationException(task.errors)
                                }
                            }
                                break
                        }
                        break
                    case 'License':
                        switch (k) {
                            //beware: THIS switch is processing only local licenses! Consortial member licenses are being processed above as no new object needs to be created!
                            case 'ownedSubscriptions': linkSubscriptionsToLicense(obj,v,null)
                                break
                            case 'licensors': v.each { licensor ->
                                    OrgRole licensorRole = new OrgRole(org:licensor,lic:obj,roleType:RDStore.OR_LICENSOR)
                                    if(!licensorRole.save())
                                        throw new CreationException(licensorRole.errors)
                                }
                                break
                            case 'customProperties':
                                // causing a session mismatch
                                // AbstractProperty newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY,obj,PropertyDefinition.get(v.type))
                                v.each { property ->
                                    LicenseProperty newProp = new LicenseProperty(owner:obj,type:PropertyDefinition.get(property.type))
                                    if(property.stringValue) {
                                        newProp.stringValue = property.stringValue
                                    }
                                    else if(property.urlValue) {
                                        newProp.urlValue = new URL(property.urlValue)
                                    }
                                    else if(property.decValue) {
                                        newProp.decValue = Double.parseDouble(property.decValue)
                                    }
                                    else if(property.intValue) {
                                        newProp.intValue = Integer.parseInt(property.intValue)
                                    }
                                    else if(property.refValue) {
                                        newProp.refValue = property.refValue
                                    }
                                    else if(property.dateValue) {
                                        newProp.dateValue = property.dateValue
                                    }
                                    newProp.paragraph = property.paragraph ?: null
                                    newProp.note = property.note ?: null
                                    if(!newProp.save())
                                        throw new CreationException(newProp.errors)
                                    if(property.isShared)
                                        AuditConfig.addConfig(newProp,AuditConfig.COMPLETE_OBJECT)
                                }
                                break
                            case 'tasks': v.each { t ->
                                    Task task = new Task(license: obj,title: t.title,description: t.description,endDate: t.endDate,status: t.status,responsibleOrg: t.responsibleOrg ?: null, responsibleUser: t.responsibleUser ?: null, creator: contextService.user, createDate: new Date(), systemCreateDate: new Date())
                                    if(!task.save()) {
                                        throw new CreationException(task.errors)
                                    }
                                }
                                break
                            case 'licenseDocuments': v.each { entry ->
                                    createDocument(entry, obj)
                                }
                                break
                            case 'createMemberLicense':
                                Map licenseParams = [
                                        lic_name: "${obj.reference} (Teilnehmervertrag)",
                                        isSlaved: RDStore.YN_YES,
                                        asOrgType: [RDStore.OT_CONSORTIUM.id.toString()],
                                        consortium: consortium,
                                        copyStartEnd: true
                                ]
                                //process only case two of LicenseController.processAddMembers()
                                institutionsService.copyLicense(obj,licenseParams,InstitutionsService.CUSTOM_PROPERTIES_ONLY_INHERITED)
                                break
                        }
                        break
                    case 'Person':
                        v.each { entry ->
                            def prsObj
                            switch (k) {
                                case 'contact':
                                    prsObj = new Contact(entry)
                                    break
                                case 'address':
                                    prsObj = new Address(entry)
                                    break
                                case 'personRoles':
                                    prsObj = new PersonRole(entry)
                                    break
                            }
                            prsObj.prs = obj
                            if(!prsObj.save())
                                throw new CreationException(prsObj.errors)
                        }
                        break
                    default: throw new CreationException("No domain class mapping found; implement it or check the type key!")
                        break
                }
            }
        }
        //process shared custom subcription properties
        switch(objectType) {
            case 'Subscription':
                if(obj.instanceOf) {
                    List<Subscription> synShareTargetList = [obj]
                    SubscriptionProperty.findAllByOwner(obj.instanceOf).each { scp ->
                        AuditConfig ac = AuditConfig.getConfig(scp)
                        if(ac) {
                            //I do not understand what the difference in SubscriptionController is, so, I will not distinct here between multipleOccurrence or not
                            AbstractPropertyWithCalculatedLastUpdated prop = new SubscriptionProperty(owner:obj,type:scp.type)
                            prop = scp.copyInto(prop)
                            prop.instanceOf = scp
                            prop.save()
                        }
                    }
                    obj.instanceOf.syncAllShares(synShareTargetList)
                }
                break
        }
        obj
    }

    /**
     * This method sets up {@link Links} according to the given parameter map
     * @param linkParams - the parameter map containing the information necessary for the link. See the definition of {@link Links} to see the parameters expected
     * @throws CreationException
     */
    void setupLinking(Map linkParams) throws CreationException {
        Links link = new Links(linkParams)
        if(!link.save())
            throw new CreationException(link.errors)
    }

    /**
     * This method links a given {@link License} with a given {@link Subscription}
     * @param owner - the {@link License} owning the {@link Subscription}
     * @param subIdentifierList - the {@link List} identifier of the {@link Subscription} to be linked
     * @throws CreationException
     */
    void linkSubscriptionsToLicense(License owner, List subIdentifierList, Org member) throws CreationException {
        subIdentifierList.each { subIdentifier ->
            Subscription subInstance = Subscription.findByIdentifier(subIdentifier)
            if(!subInstance) {
                throw new CreationException("Wrong subscription key inserted: ${subIdentifier}")
            }
            subscriptionService.setOrgLicRole(subInstance,owner,false)
            /*
            subInstance.owner = owner
            if(member) {
                //this is the consortial complimentary part for the license org role processing switch in createObject
                OrgRole memberRole = new OrgRole(lic:owner,org:member,roleType:RDStore.OR_LICENSEE_CONS)
                if(!memberRole.save()) {
                    errors.add(memberRole.errors.toString())
                    return false
                }
            }
            if(!subInstance.save(flush: true))
                throw new CreationException(subInstance.errors)
             */
        }
    }

    /**
     * This method creates a {@link CostItem} according to the given {@link Map} of parameters
     * @param costItemParams - the {@link Map} containing the parameters for the {@link CostItem}
     * @throws CreationException
     */
    void createCostItem(Map costItemParams) throws CreationException {
        CostItem ci = new CostItem(costItemStatus: costItemParams.status,
                owner: costItemParams.owner,
                sub: costItemParams.sub,
                subPkg: SubscriptionPackage.findByPkgAndSubscription(costItemParams.subPkg,costItemParams.obj) ?: null,
                costInBillingCurrency: costItemParams.billingSum,
                billingCurrency: costItemParams.currency,
                costInLocalCurrency: costItemParams.localSum ?: 0,
                currencyRate: costItemParams.currencyRate,
                taxKey: costItemParams.taxKey,
                startDate: costItemParams.startDate,
                endDate: costItemParams.endDate,
                costItemElement: costItemParams.costItemElement,
                costItemElementConfiguration: costItemParams.costItemElementConfiguration,
                isVisibleForSubscriber: costItemParams.isVisibleForSubscriber ?: false
        )
        if(ci && !ci.save()) {
            throw new CreationException(ci.errors)
        }
    }

    void createDocument(entry, obj) throws CreationException {
        try {
            String fPath = ConfigUtils.getDocumentStorageLocation() ?: '/tmp/laser'
            Doc originalDoc = Doc.findByUuid(entry.docstoreUUID)
            Path source = Paths.get("${fPath}/${originalDoc.uuid}")
            Doc docContent = new Doc()
            InvokerHelper.setProperties(docContent,originalDoc.properties)
            docContent.uuid = null
            if(docContent.save()) {
                Path destination = Paths.get("${fPath}/${docContent.uuid}")
                Files.copy(source,destination)
                DocContext copyContext = new DocContext(owner:docContent,doctype:docContent.type)
                if(obj instanceof License)
                    copyContext.license = obj
                else if(obj instanceof Subscription)
                    copyContext.subscription = obj
                copyContext.isShared = entry.isShared ?: false
                if(!copyContext.save())
                    throw new CreationException(copyContext.errors)
                ((ShareSupport) obj).updateShare(copyContext)
            }
            else {
                throw new CreationException(docContent.errors)
            }
        }
        catch (NoSuchFileException e) {
            throw new CreationException("The file ${e.getMessage()} does not exist! Please check the docstoreUUID you would like to copy!")
        }
        catch (Exception e) {
            throw new CreationException(e.printStackTrace())
        }
    }

    /**
     * Dumps the errors occurred during creation as an outputable string
     * @return the error list as a string joined by HTML line breaks for frontend display
     */
    String dumpErrors() {
        String out = errors.join('<br>')
        errors = []
        out
    }

    void createOrgsFromScratch() {
        def currentServer = ServerUtils.getCurrentServer()
        Map<String,Role> customerTypes = [konsorte:Role.findByAuthority('ORG_BASIC_MEMBER'),
                                          institut:Role.findByAuthority('ORG_BASIC_MEMBER'),
                                          singlenutzer:Role.findByAuthority('ORG_INST'),
                                          kollektivnutzer:Role.findByAuthority('ORG_INST_COLLECTIVE'),
                                          konsortium:Role.findByAuthority('ORG_CONSORTIUM')]
        RefdataValue institution = RefdataValue.getByValueAndCategory('Institution', RDConstants.ORG_TYPE)
        RefdataValue consortium = RefdataValue.getByValueAndCategory('Consortium', RDConstants.ORG_TYPE)
        RefdataValue department = RefdataValue.getByValueAndCategory('Department', RDConstants.ORG_TYPE)
        //create home org
        Org hbz = Org.findByName('hbz Konsortialstelle Digitale Inhalte')
        if(!hbz) {
            hbz = createOrg([name: 'hbz Konsortialstelle Digitale Inhalte',shortname: 'hbz Konsortium', sortname: 'Köln, hbz', orgType: [consortium], sector: RDStore.O_SECTOR_HIGHER_EDU])
            if(!hbz.hasErrors()) {
                OrgSettings.add(hbz,OrgSettings.KEYS.CUSTOMER_TYPE,customerTypes.konsortium)
                grailsApplication.config.systemUsers.each { su ->
                    User admin = User.findByUsername(su.name)
                    instAdmService.createAffiliation(admin,hbz,Role.findByAuthority('INST_ADM'),UserOrg.STATUS_APPROVED,null)
                    admin.getSetting(UserSettings.KEYS.DASHBOARD,hbz)
                }
            }
            else if(hbz.hasErrors()) {
                log.error(hbz.errors)
                //log.error(e.getStackTrace())
            }
        }
        if(currentServer == ServerUtils.SERVER_QA) { //include SERVER_LOCAL when testing in local environment
            Map<String,Map> modelOrgs = [konsorte: [name:'Musterkonsorte',shortname:'Muster', sortname:'Musterstadt, Muster', orgType: [institution]],
                                         institut: [name:'Musterinstitut',orgType: [department]],
                                         singlenutzer: [name:'Mustereinrichtung',sortname:'Musterstadt, Uni', orgType: [institution]],
                                         kollektivnutzer: [name:'Mustereinrichtung Kollektiv',shortname:'Mustereinrichtung Kollektiv',sortname:'Musterstadt, Kollektiv',orgType: [institution]],
                                         konsortium: [name:'Musterkonsortium',shortname:'Musterkonsortium',orgType: [consortium]]]
            Map<String,Map> testOrgs = [konsorte: [name:'Testkonsorte',shortname:'Test', sortname:'Teststadt, Test',orgType: [institution]],
                                        institut: [name:'Testinstitut',orgType: [department]],
                                        singlenutzer: [name:'Testeinrichtung',sortname:'Teststadt, Uni',orgType: [institution]],
                                        kollektivnutzer: [name:'Testeinrichtung Kollektiv',shortname:'Testeinrichtung Kollektiv',sortname:'Teststadt, Kollektiv',orgType: [institution]],
                                        konsortium: [name:'Testkonsortium',shortname:'Testkonsortium',orgType: [consortium]]]
            Map<String,Map> QAOrgs = [konsorte: [name:'QA-Konsorte',shortname:'QA', sortname:'QA-Stadt, QA',orgType: [institution]],
                                      institut: [name:'QA-Institut',orgType: [department]],
                                      singlenutzer: [name:'QA-Einrichtung',sortname:'QA-Stadt, Uni',orgType: [institution]],
                                      kollektivnutzer: [name:'QA-Einrichtung Kollektiv',shortname:'QA-Einrichtung Kollektiv',sortname:'QA-Stadt, Kollektiv',orgType: [institution]],
                                      konsortium: [name:'QA-Konsortium',shortname:'QA-Konsortium',orgType: [consortium]]]
            [modelOrgs,testOrgs,QAOrgs].each { Map<String,Map> orgs ->
                Map<String,Org> orgMap = [:]
                orgs.each { String customerType, Map orgData ->
                    Org org = createOrg(orgData)
                    if(!org.hasErrors()) {
                        //other ones are covered by Org.setDefaultCustomerType()
                        if (customerType in ['singlenutzer', 'kollektivnutzer', 'konsortium']) {
                            OrgSettings.add(org, OrgSettings.KEYS.CUSTOMER_TYPE, customerTypes[customerType])
                            if (customerType == 'konsortium') {
                                Combo c = new Combo(fromOrg: Org.findByName(orgs.konsorte.name), toOrg: org, type: RDStore.COMBO_TYPE_CONSORTIUM)
                                c.save()
                            } else if (customerType == 'kollektivnutzer') {
                                Combo c = new Combo(fromOrg: Org.findByName(orgs.institut.name), toOrg: org, type: RDStore.COMBO_TYPE_DEPARTMENT)
                                c.save()
                            }
                        }
                        orgMap[customerType] = org
                    }
                    else if(org.hasErrors())
                        log.error(org.erros)
                    //log.error(e.getStackTrace())
                }
                userService.setupAdminAccounts(orgMap)
            }
        }
        else if(currentServer == ServerUtils.SERVER_DEV) {
            userService.setupAdminAccounts([konsortium:hbz])
        }
    }

    Org createOrg(Map params) {
        Org obj = new Org(name: params.name,shortname: params.shortname, sortname: params.sortname, orgType: params.orgType, sector: params.orgSector)
        if(obj.save()) {
            initMandatorySettings(obj)
        }
        obj
    }

}
