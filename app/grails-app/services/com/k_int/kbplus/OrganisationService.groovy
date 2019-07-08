package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupItem
import de.laser.AuditConfig
import de.laser.exceptions.CreationException
import de.laser.helper.RDStore
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
    def globalSourceSyncService
    def dataloadService
    def grailsApplication
    List<String> errors = []

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
        def orgSector = RefdataValue.getByValueAndCategory('Higher Education','OrgSector')
        def orgType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')
        if(addHigherEducationTitles) {
            titles.add(messageSource.getMessage('org.libraryType.label',null,LocaleContextHolder.getLocale()))
            titles.add(messageSource.getMessage('org.libraryNetwork.label',null,LocaleContextHolder.getLocale()))
            titles.add(messageSource.getMessage('org.funderType.label',null,LocaleContextHolder.getLocale()))
            titles.add(messageSource.getMessage('org.federalState.label',null,LocaleContextHolder.getLocale()))
            titles.add(messageSource.getMessage('org.country.label',null,LocaleContextHolder.getLocale()))
        }
        def propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
        propList.sort { a, b -> a.name.compareToIgnoreCase b.name}
        propList.each {
            titles.add(it.name)
        }
        List orgData = []
        switch(format) {
            case "xls":
            case "xlsx":
                orgs.each{  org ->
                    List row = []
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
                        //federalState
                        row.add([field: org.federalState?.getI10n('value') ?: ' ',style: null])
                        //country
                        row.add([field: org.country?.getI10n('value') ?: ' ',style: null])
                    }
                    propList.each { pd ->
                        def value = ''
                        org.customProperties.each{ prop ->
                            if(prop.type.descr == pd.descr && prop.type == pd) {
                                if(prop.type.type == Integer.toString()){
                                    value = prop.intValue.toString()
                                }
                                else if (prop.type.type == String.toString()){
                                    value = prop.stringValue ?: ''
                                }
                                else if (prop.type.type == BigDecimal.toString()){
                                    value = prop.decValue.toString()
                                }
                                else if (prop.type.type == Date.toString()){
                                    value = prop.dateValue.toString()
                                }
                                else if (prop.type.type == RefdataValue.toString()) {
                                    value = prop.refValue?.getI10n('value') ?: ''
                                }
                            }
                        }
                        org.privateProperties.each{ prop ->
                            if(prop.type.descr == pd.descr && prop.type == pd) {
                                if(prop.type.type == Integer.toString()){
                                    value = prop.intValue.toString()
                                }
                                else if (prop.type.type == String.toString()){
                                    value = prop.stringValue ?: ''
                                }
                                else if (prop.type.type == BigDecimal.toString()){
                                    value = prop.decValue.toString()
                                }
                                else if (prop.type.type == Date.toString()){
                                    value = prop.dateValue.toString()
                                }
                                else if (prop.type.type == RefdataValue.toString()) {
                                    value = prop.refValue?.getI10n('value') ?: ''
                                }
                            }
                        }
                        row.add([field: value, style: null])
                    }
                    orgData.add(row)
                }
                Map sheetData = [:]
                sheetData[message] = [titleRow:titles,columnData:orgData]
                return exportService.generateXLSXWorkbook(sheetData)
            case "csv":
                orgs.each{  org ->
                    List row = []
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
                        //federalState
                        row.add(org.federalState?.getI10n('value') ?: ' ')
                        //country
                        row.add(org.country?.getI10n('value') ?: ' ')
                    }
                    propList.each { pd ->
                        def value = ''
                        org.customProperties.each{ prop ->
                            if(prop.type.descr == pd.descr && prop.type == pd) {
                                if(prop.type.type == Integer.toString()){
                                    value = prop.intValue.toString()
                                }
                                else if (prop.type.type == String.toString()){
                                    value = prop.stringValue ?: ''
                                }
                                else if (prop.type.type == BigDecimal.toString()){
                                    value = prop.decValue.toString()
                                }
                                else if (prop.type.type == Date.toString()){
                                    value = prop.dateValue.toString()
                                }
                                else if (prop.type.type == RefdataValue.toString()) {
                                    value = prop.refValue?.getI10n('value') ?: ''
                                }
                            }
                        }
                        org.privateProperties.each{ prop ->
                            if(prop.type.descr == pd.descr && prop.type == pd) {
                                if(prop.type.type == Integer.toString()){
                                    value = prop.intValue.toString()
                                }
                                else if (prop.type.type == String.toString()){
                                    value = prop.stringValue ?: ''
                                }
                                else if (prop.type.type == BigDecimal.toString()){
                                    value = prop.decValue.toString()
                                }
                                else if (prop.type.type == Date.toString()){
                                    value = prop.dateValue.toString()
                                }
                                else if (prop.type.type == RefdataValue.toString()) {
                                    value = prop.refValue?.getI10n('value') ?: ''
                                }
                            }
                        }
                        row.add(value.replaceAll(',',';'))
                    }
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
            if(!OrgRole.findAllByOrg(current) && !Person.findAllByTenant(current) && !CostItem.findAllByOwner(current) && !PropertyDefinition.findAllByTenant(current)) {
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
                dates.startDateCurrent = sdf.parse('2019-05-01') //as you wish ... but do not forget to update these dates regularly!!
                dates.endDateCurrent = sdf.parse('2019-12-31')
                dates.startDateExpired = sdf.parse('2019-04-01')
                dates.endDateExpired = sdf.parse('2019-04-30')
                dates.previousYearRingStart = sdf.parse('2018-01-01')
                dates.previousYearRingEnd = sdf.parse('2018-12-31')
                dates.nextYearRingStart = sdf.parse('2020-01-01')
                dates.nextYearRingEnd = sdf.parse('2020-12-31')
                Map<String,RefdataValue> localRDStoreSubscriptionForm = [:]
                RefdataCategory.getAllRefdataValues('Subscription Form').each { rdv ->
                    localRDStoreSubscriptionForm.put(rdv.value,rdv)
                }
                Map<String,RefdataValue> localRDStoreSubscriptionResource = [:]
                RefdataCategory.getAllRefdataValues('Subscription Resource').each { rdv ->
                    localRDStoreSubscriptionResource.put(rdv.value,rdv)
                }
                Map<String,RefdataValue> localRDStoreCostItemStatus = [:]
                RefdataCategory.getAllRefdataValues('CostItemStatus').each { rdv ->
                    localRDStoreCostItemStatus.put(rdv.value,rdv)
                }
                Map<String,RefdataValue> localRDStoreCostItemElement = [:]
                RefdataCategory.getAllRefdataValues('CostItemElement').each { rdv ->
                    localRDStoreCostItemElement.put(rdv.value,rdv)
                }
                Map<String,RefdataValue> localRDStoreCurrency = [
                        'USD': RefdataValue.getByValueAndCategory('USD','Currency'),
                        'GBP': RefdataValue.getByValueAndCategory('GBP','Currency'),
                        'EUR': RefdataValue.getByValueAndCategory('EUR','Currency')
                ]
                Map<String,RefdataValue> localRDStoreGender = [
                        'Female': RefdataValue.getByValueAndCategory('Female','Gender'),
                        'Male': RefdataValue.getByValueAndCategory('Male','Gender'),
                        'Third Gender': RefdataValue.getByValueAndCategory('Third Gender','Gender')
                ]
                Map<String,RefdataValue> localRDStoreContactType = [
                        'Personal': RefdataValue.getByValueAndCategory('Personal','ContactType'),
                        'Job-related': RefdataValue.getByValueAndCategory('Job-related','ContactType')
                ]
                Map<String,Org> exampleOrgs = [
                        'Musteranbieter A': Org.findByGlobalUID('org:e54e95c8-7aed-443d-ac8b-5d0fc2b6a0da'),
                        'Musteranbieter B': Org.findByGlobalUID('org:6580cb0e-fe72-4156-b26b-05054317970f'),
                        'Musteranbieter E-Books': Org.findByGlobalUID('org:4c439fa3-7a2f-4b35-88a1-ffa083977c7b'),
                        'Musteranbieter E-Journals': Org.findByGlobalUID('org:da08b534-1155-4fc0-84df-d5af279302aa')
                ]
                Map<String,Package> examplePackages = [
                        'duz: Deutsche Universitätszeitung': Package.findByGlobalUID('package:018739b7-6a48-4c1f-84f0-19cc435c25e9'),
                        'Brepols: Bibliography of British and Irish History': Package.findByGlobalUID('package:53af32d6-c183-46f5-95f7-67d7f2dbf197'),
                        'Naxos: Naxos Music Library World': Package.findByGlobalUID('package:ea781079-02f5-4205-bac7-a2d6ca96fd7e'),
                        'Thieme: Thieme eRef Lehrbuecher': Package.findByGlobalUID('package:a101ebd3-3886-482b-b162-1c68c53db27a'),
                        'American Chemical Society: American Chemical Society Journals': Package.findByGlobalUID('package:16fdec2c-adba-4dc4-bab6-e012853fa7b8'),
                        'EBSCO: SportDiscus': Package.findByGlobalUID('package:2f8adf87-b113-4311-bfbb-22c8fbe77c12')
                ]
                RefdataValue licenseType = RefdataValue.getByValueAndCategory('Actual','License Type')
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
                if(RDStore.OT_INSTITUTION.id in current.getallOrgTypeIds()) {
                    return setupTestDataForInst(generalData,current)
                }
                else if(RDStore.OT_CONSORTIUM.id in current.getallOrgTypeIds()) {
                    return setupTestDataForCons(generalData,current)
                }
                else {
                    errors.add("Kein Einrichtungstyp gegeben!")
                    return false
                }
            }
            else {
                errors.add("Diese Einrichtung verfügt bereits über einen Testdatensatz!")
                return false
            }
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
        Subscription underConsiderationDatenAParent = Subscription.findByGlobalUID("subscription:550361c5-bc89-415e-b9ff-8505aa39d2c3") //pray that there will be no UID change on the example objects!!
        Subscription currentDatenAParent = Subscription.findByGlobalUID("subscription:314c14f2-53f9-475b-80a8-34fd58828cc3")
        Subscription eBookPickParent = Subscription.findByGlobalUID("subscription:b9db1e2f-7ea6-4e28-8284-1a1a3ce960fb")
        Subscription journalPaketExtremParent = Subscription.findByGlobalUID('subscription:833c5da6-b77a-4c36-98cc-32292adde93c')
        Org consortium = Org.findByGlobalUID('org:fe0cc5f7-21a4-4047-8c11-842da0d23277')
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
        log.info("creating property definition groups ...")
        List propertyDefinitionGroups = [
                [group:new PropertyDefinitionGroup(name: 'Fernleihe', description: 'Fernleihbedingungen', ownerType: License.class.name, tenant: current, visible: RDStore.YN_YES),
                 items:[PropertyDefinition.findByName('ILL record keeping required'),
                        PropertyDefinition.findByName('ILL electronic'),
                        PropertyDefinition.findByName('ILL print or fax'),
                        PropertyDefinition.findByName('ILL secure electronic transmission'),
                        PropertyDefinition.findByName('ILL term note')]
                ],
                [group:new PropertyDefinitionGroup(name: 'Archivrecht', description: 'Welches Archivrecht ist gegeben?', ownerType: License.class.name, tenant: current, visible: RDStore.YN_YES),
                 items:[PropertyDefinition.findByName('Archival Copy Content'),
                        PropertyDefinition.findByName('Archival Copy: Cost'),
                        PropertyDefinition.findByName('Archival Copy: Permission'),
                        PropertyDefinition.findByName('Archival Copy: Time'),
                        PropertyDefinition.findByName('Archiving rights')]
                ],
                [group:new PropertyDefinitionGroup(name: 'Zugriffsbedingungen', description: 'Welche Zugriffsmöglichkeiten sind erlaubt?', ownerType: License.class.name, tenant: current, visible: RDStore.YN_YES),
                 items:[PropertyDefinition.findByName('Walk-in Access'),
                        PropertyDefinition.findByName('Walk-in User Term Note'),
                        PropertyDefinition.findByName('Wifi Access'),
                        PropertyDefinition.findByName('Remote Access')]
                ],
                [group:new PropertyDefinitionGroup(name: 'Fremdsysteme', description: '', ownerType: Subscription.class.name, tenant: current, visible: RDStore.YN_YES),
                 items:[PropertyDefinition.findByName('DBIS-Link'),
                        PropertyDefinition.findByName('EZB Gelbschaltung'),
                        PropertyDefinition.findByName('SFX-Eintrag')]
                ],
                [group:new PropertyDefinitionGroup(name: 'Statistik', description: '', ownerType: Subscription.class.name, tenant: current, visible: RDStore.YN_YES),
                 items:[PropertyDefinition.findByName('Statistik'),
                        PropertyDefinition.findByName('Statistics Link'),
                        PropertyDefinition.findByName('Statistikzugang')]
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
        log.info("creating private contacts ...")
        Map franzEric = [mainParams: [
                first_name:'Franz',
                last_name:'Eric',
                gender: generalData.gender.get('Male'),
                contactType: RDStore.CONTACT_TYPE_PERSONAL,
                tenant: current,
                isPublic: RDStore.YN_NO
        ],
                         addParams: [
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
                isPublic: RDStore.YN_NO
        ],
                           addParams: [
                                   contact: [[contentType: RDStore.CCT_PHONE,
                                              type: generalData.contactTypes.get('Job-related'),
                                              content: '00 49 021 456 789'
                                             ]],
                                   personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter A'),
                                                  functionType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS],
                                                 [org:generalData.exampleOrgs.get('Musteranbieter A'),
                                                  positionType: RefdataValue.getByValueAndCategory('Sales Director','Person Position')]
                                   ]
                           ]
        ]
        Map technik = [mainParams: [
                last_name: 'Technik',
                contactType: RDStore.CONTACT_TYPE_FUNCTIONAL,
                tenant: current,
                isPublic: RDStore.YN_NO
        ],
                       addParams: [
                               contact:[[contentType: RDStore.CCT_EMAIL,
                                         type: generalData.contactTypes.get('Job-related'),
                                         content: 'tech@support.com'
                                        ]],
                               personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter A'),
                                              functionType: RefdataValue.getByValueAndCategory('Technical Support','Person Function')]]]
        ]
        Set<Map> personalContacts = [franzEric,nameVorname,technik]
        personalContacts.each { contact ->
            createObject('Person',contact,consortium,current)
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
                             status: RefdataValue.getByValueAndCategory('Test Access','Subscription Status'),
                             identifier: testDatenAChildIdentifier,
                             isPublic: RDStore.YN_NO,
                             impId: UUID.randomUUID().toString(),
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
                             isPublic: RDStore.YN_NO,
                             impId: UUID.randomUUID().toString(),
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
                             isPublic: RDStore.YN_NO,
                             impId: UUID.randomUUID().toString(),
                             instanceOf: eBookPickParent,
                             form: generalData.subscriptionForms.get('singlePurchase'),
                             resource: generalData.subscriptionResources.get('ebookSingle')],
                addParams: [packages:[[pkg:generalData.examplePackages.get('Thieme: Thieme eRef Lehrbuecher'),
                                       issueEntitlementISBNs:['9783131906953',
                                                              '9783131934345',
                                                              '9783132412743',
                                                              '9783132404069',
                                                              '9783132414310',
                                                              '9783131928634',
                                                              '9783131920317',
                                                              '9783132407060',
                                                              '9783131915160',
                                                              '9783131945730',
                                                              '9783131911810',
                                                              '9783132412651',
                                                              '9783131945228',
                                                              '9783132406360']
                                      ]
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
                             manualCancellationDate: generalData.sdf.parse('2018-10-31'),
                             status: RDStore.SUBSCRIPTION_EXPIRED,
                             identifier: expiredJournalPaketIdentifier,
                             isPublic: RDStore.YN_NO,
                             impId: UUID.randomUUID().toString(),
                             form: RefdataValue.getByValueAndCategory('purchaseOngoing','Subscription Form'),
                             resource: RefdataValue.getByValueAndCategory('ejournalPackage','Subscription Resource')],
                addParams: [packages:[[pkg: Package.findByGlobalUID('package:f7f44d9f-f1c7-4be2-89af-303fe82fff2a')]],
                            provider:generalData.exampleOrgs.get('Musteranbieter E-Journals'),
                            customProperties:[[type:PropertyDefinition.findByName('DBIS-Link').id,urlValue:'https://dbis..de']]
                ]
        ]
        Map currentJournalPaketParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_LOCAL,
                             name: "Journal-Paket",
                             startDate: generalData.dates.defaultStartDate,
                             endDate: generalData.dates.defaultEndDate,
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: currentJournalPaketIdentifier,
                             isPublic: RDStore.YN_NO,
                             impId: UUID.randomUUID().toString(),
                             form: RefdataValue.getByValueAndCategory('purchaseOngoing','Subscription Form'),
                             resource: RefdataValue.getByValueAndCategory('ejournalPackage','Subscription Resource')],
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
                             isPublic: RDStore.YN_NO,
                             impId: UUID.randomUUID().toString(),
                             form: RefdataValue.getByValueAndCategory('purchaseOngoing','Subscription Form'),
                             resource: RefdataValue.getByValueAndCategory('ejournalPackage','Subscription Resource'),
                             instanceOf: journalPaketExtremParent],
                addParams: [:]/*provider:generalData.exampleOrgs.get('Musteranbieter E-Journals')*/
        ]
        Map musterdatenbankParams = [
                mainParams: [type: RDStore.SUBSCRIPTION_TYPE_LOCAL,
                             name: "Musterdatenbank",
                             startDate: generalData.dates.defaultStartDate,
                             endDate: generalData.dates.defaultEndDate,
                             manualCancellationDate: generalData.sdf.parse('2019-09-30'),
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: musterdatenbankIdentifier,
                             isPublic: RDStore.YN_NO,
                             impId: UUID.randomUUID().toString(),
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
                                    [type:PropertyDefinition.findByName('DBIS-Link').id,urlValue:'http://dbis.uni-regensburg.de/...'],
                                    [type:PropertyDefinition.findByName('Statistik').id,stringValue:'Counter 4'],
                                    [type:PropertyDefinition.findByName('Statistics Link').id,urlValue:'http://www.123.de']
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
            createObject('Subscription',sub,consortium,current)
        }
        log.info("creating links between consortia member subscriptions ...")
        Map dateALinking = [linkType: RDStore.LINKTYPE_FOLLOWS,objectType:Subscription.class.name,source:Subscription.findByIdentifier(currentDatenAChildIdentifier).id,destination:Subscription.findByIdentifier(testDatenAChildIdentifier).id,owner:current]
        Map journalPaketLinking = [linkType: RDStore.LINKTYPE_FOLLOWS,objectType:Subscription.class.name,source:Subscription.findByIdentifier(currentJournalPaketIdentifier).id,destination:Subscription.findByIdentifier(expiredJournalPaketIdentifier).id,owner:current]
        Set<Map> linkSet = [dateALinking,journalPaketLinking]
        linkSet.each { link ->
            setupLinking(link)
        }
        log.info("creating consortia member licenses ...")
        Map rahmenvertragChildParams = [
                ownedSubscriptions: [currentDatenAChildIdentifier, journalPaketExtremChildIdentifier],
                baseLicense: License.findByGlobalUID("license:06700ed7-3b76-496d-866c-74daee74064b")
        ]
        Map rahmenvertragEbooksChildParams = [
                ownedSubscriptions: [eBookPickChildIdentifier],
                baseLicense: License.findByGlobalUID("license:48b5cf70-11b3-46d6-b9c1-dfac3b45120c")
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
                                    [type:PropertyDefinition.findByName('ILL electronic').id,refValue:RDStore.PERM_PERM_EXPL,paragraph:'ist alles erlaubt'],
                                    [type:PropertyDefinition.findByName('Archival Copy Content').id,refValue:RefdataValue.getByValueAndCategory('Data','License.Arc.ArchivalCopyContent')],
                                    [type:PropertyDefinition.findByName('Archiving rights').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('Walk-in Access').id,refValue: RDStore.PERM_PERM_EXPL],
                                    [type:PropertyDefinition.findByName('Wifi Access').id,refValue:RDStore.YN_YES]
                            ],
                            licenseDocuments: [[docstoreUUID:'bdcd4684-2a56-4a7f-b06e-20ffaa431ff1']]
                ]
        ]
        Map datenbankMusterParams = [
                mainParams: [reference: "Datenbank-Muster",
                             type: generalData.licenseType,
                             status: RDStore.LICENSE_CURRENT,
                             startDate: generalData.sdf.parse('2018-01-01')],
                addParams: [ownedSubscriptions:[musterdatenbankIdentifier],
                            licensors: [
                                    [generalData.exampleOrgs.get('Musteranbieter A')]
                            ],
                            customProperties: [
                                    [type:PropertyDefinition.findByName('ILL electronic').id,refValue:RDStore.PERM_PERM_EXPL],
                                    [type:PropertyDefinition.findByName('Archiving rights').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('Walk-in Access').id,refValue: RDStore.PERM_SILENT,note:'per Mail geregelt – s. Dokument'],
                                    [type:PropertyDefinition.findByName('Wifi Access').id,refValue:RDStore.YN_YES]
                            ],
                            tasks: [
                                    [title: 'Mehrjahresvariante verhandeln',
                                     status: RefdataValue.getByValueAndCategory('Open','Task Status'),
                                     endDate: generalData.sdf.parse('2020-01-01'),
                                     responsibleOrg: current,
                                     description:'Es sollte mit der nächsten Lizenzverlängerung auch eine Mehrjahresvariante geben, die vertraglich festgehalten wird.']
                            ],
                            licenseDocuments: [[docstoreUUID:'4628f269-abe7-488d-b602-04df2fb8140c'],[docstoreUUID:'aaf878d5-bca6-4472-964b-085a2cd122fd']]
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
            createObject('License',(Map) lwob,consortium,current)
        }
        return true
    }

    boolean setupTestDataForCons(Map generalData,Org current) {
        log.info("creating private properties ...")
        Set<PropertyDefinition> privatePropertyDefMaps = [
                new PropertyDefinition(name:'Quellensteuer-Befreiung',
                        tenant:current,
                        refdataCategory:'YNO',
                        descr:'Subscription Property',
                        type:'class com.k_int.kbplus.RefdataValue',
                        expl:'Hat der Anbieter für dieses Produkt eine Befreiung der Quellensteuer erwirkt?'),
                new PropertyDefinition(name:'BGA',
                        tenant:current,
                        refdataCategory:'YN',
                        descr:'Organisation Property',
                        type:'class com.k_int.kbplus.RefdataValue',
                        expl:'Betrieb gewerblicher Art'),
                new PropertyDefinition(name:'EGP Nr.',
                        tenant:current,
                        descr:'Organisation Property',
                        type:'class java.lang.Integer',
                        expl:'ID für das SAP System des rechtlichen Trägers')
        ]
        Map<String,PropertyDefinition> privateProperties = [:]
        privatePropertyDefMaps.each { propDef ->
            if(propDef.save()) {
                privateProperties.put(propDef.name,propDef)
            }
            else {
                errors.add(propDef.errors.toString())
                return false
            }
        }
        log.info("creating property definition groups ...")
        List propertyDefinitionGroups = [
                [group:new PropertyDefinitionGroup(name: 'ausgeübtes Recht', description: 'Welches Recht wird angewandt?', ownerType: License.class.name, tenant: current, visible: RDStore.YN_YES),
                 items:[PropertyDefinition.findByName('Governing law'),
                        PropertyDefinition.findByName('Governing jurisdiction')]
                ],
                [group:new PropertyDefinitionGroup(name: 'Fernleihe', description: 'ist eine Fernleihe erlaubt?', ownerType: License.class.name, tenant: current, visible: RDStore.YN_YES),
                 items:[PropertyDefinition.findByName('ILL record keeping required'),
                        PropertyDefinition.findByName('ILL electronic'),
                        PropertyDefinition.findByName('ILL print or fax'),
                        PropertyDefinition.findByName('ILL secure electronic transmission'),
                        PropertyDefinition.findByName('ILL term note')]
                ],
                [group:new PropertyDefinitionGroup(name: 'GASCO', description: 'Merkmale, die den GASCO-Monitor steuern', ownerType: Subscription.class.name, tenant: current, visible: RDStore.YN_YES),
                 items:[PropertyDefinition.findByName('GASCO Entry'),
                        PropertyDefinition.findByName('GASCO-Anzeigename'),
                        PropertyDefinition.findByName('GASCO-Verhandlername'),
                        PropertyDefinition.findByName('GASCO-Information-Link')]
                ],
                [group:new PropertyDefinitionGroup(name: 'meinKonsortium', description: 'alle für meine Konsortialstelle relevanten Merkmale', ownerType: Subscription.class.name, tenant: current, visible: RDStore.YN_YES),
                 items:[PropertyDefinition.findByName('Bundesweit offen'),
                        PropertyDefinition.findByName('Eingeschränkter Benutzerkreis'),
                        PropertyDefinition.findByName('Mehrjahreslaufzeit'),
                        PropertyDefinition.findByName('Mehrjahreslaufzeit ausgewählt'),
                        PropertyDefinition.findByName('Mengenrabatt Stichtag'),
                        PropertyDefinition.findByName('Neueinsteigerrabatt'),
                        PropertyDefinition.findByName('Preis gerundet'),
                        PropertyDefinition.findByName('Preisvorteil durch weitere Produktteilnahme'),
                        PropertyDefinition.findByName('Private Einrichtungen'),
                        PropertyDefinition.findByName('Produktabhängigkeit'),
                        PropertyDefinition.findByName('Rabatt'),
                        PropertyDefinition.findByName('Rabattstaffel'),
                        PropertyDefinition.findByName('Rabatt Zählung'),
                        PropertyDefinition.findByName('Rechnungsstellung durch Anbieter'),
                        PropertyDefinition.findByName('Rechnungszeitpunkt'),
                        PropertyDefinition.findByName('reverse charge'),
                        PropertyDefinition.findByName('Simuser Zahl'),
                        PropertyDefinition.findByName('Teilzahlung'),
                        PropertyDefinition.findByName('Testzeitraum'),
                        PropertyDefinition.findByName('Unterjähriger Einstieg')]
                ],
                [group:new PropertyDefinitionGroup(name: 'Open Access', description: 'Open Access vereinbart', ownerType: License.class.name, tenant: current, visible: RDStore.YN_YES),
                 items:[PropertyDefinition.findByName('OA Note'),
                        PropertyDefinition.findByName('OA Last Date'),
                        PropertyDefinition.findByName('OA First Date'),
                        PropertyDefinition.findByName('Offsetting'),
                        PropertyDefinition.findByName('Open Access')]
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
        log.info("creating model member org ...")
        Object[] argv0 = [current.name]
        Object[] argv1 = [current.shortname]
        Org modelMember = new Org(name: messageSource.getMessage('org.setup.modelOrgName',argv0,LocaleContextHolder.getLocale()),
                sortname: messageSource.getMessage('org.setup.modelOrgSortname',argv1,LocaleContextHolder.getLocale()),
                url: 'www.mustereinichtung.de', urlGov: 'www.muster_uni.de', status: RDStore.O_STATUS_CURRENT,
                libraryType: RefdataValue.getByValueAndCategory('Universität','Library Type'),
                libraryNetwork: RefdataValue.getByValueAndCategory('No Network','Library Network'),
                federalState: current.federalState ,country: current.country,
                sector: RefdataValue.getByValueAndCategory('Higher Education','OrgSector'))
        if(!modelMember.save()) {
            errors.add(modelMember.errors.toString())
            return false
        }
        modelMember.addToOrgType(RDStore.OT_INSTITUTION)
        modelMember.setDefaultCustomerType()
        OrgPrivateProperty opp = new OrgPrivateProperty(owner: modelMember, type: privateProperties.get('BGA'), refValue: RDStore.YN_YES)
        if(!opp.save()) {
            errors.add(opp.errors.toString())
            return false
        }
        Map legalPatronAddressMap = [type:RefdataValue.getByValueAndCategory('Legal patron address','AddressType'),
                                     name:'Rechtlicher Träger',
                                     street_1:'Univesitätsstraße',
                                     street_2:'1',
                                     zipcode:'55555',
                                     city:'Musterhausen',
                                     state:RefdataValue.getByValueAndCategory('North Rhine-Westphalia','Federal State'),
                                     country:RefdataValue.getByValueAndCategory('DE','Country'),
                                     org:modelMember]
        Map postalAddressMap = [type:RefdataValue.getByValueAndCategory('Postal address','AddressType'),
                                name:'Bibliothek',
                                additionFirst:'Erwerbungsabteilung',
                                street_1:'Musterstraße',
                                street_2:'1',
                                zipcode:'55555',
                                city:'Musterhausen',
                                state:RefdataValue.getByValueAndCategory('North Rhine-Westphalia','Federal State'),
                                country:RefdataValue.getByValueAndCategory('DE','Country'),
                                org:modelMember]
        Set<Map> addresses = [legalPatronAddressMap,postalAddressMap]
        addresses.each { addressData ->
            Address address = new Address(addressData)
            if(!address.save()) {
                errors.add(address.errors.toString())
                return false
            }
        }
        Org member1Aachen = Org.findByGlobalUID('org:d0b6c3c0-58b5-4fc3-8791-0dafa51f62da')
        Org member2Zittau = Org.findByGlobalUID('org:c76f73dc-fb33-4b04-b0b7-4934f7a75572')
        Org member3Munich = Org.findByGlobalUID('org:d7fa36bd-5b91-4c21-887c-db9c73440455')
        Org member4Greifswald = Org.findByGlobalUID('org:7f8f28a0-7af8-4229-a554-aceca1cb1c27')
        Org member5Bremen = Org.findByGlobalUID('org:876920e0-2f7a-41fc-9a09-b0a0d3817409')
        Set<Org> consortialMembers = [member1Aachen,member2Zittau,member3Munich,member4Greifswald,member5Bremen,modelMember]
        consortialMembers.each { member ->
            Combo memberCombo = new Combo(fromOrg:member,toOrg:current,type:RDStore.COMBO_TYPE_CONSORTIUM)
            if(!memberCombo.save()) {
                errors.add(memberCombo.errors.toString())
                return false
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
        log.info("creating private contacts ...")
        Map erwerbung = [mainParams:[
                last_name: 'Erwerbung',
                contactType: RDStore.CONTACT_TYPE_FUNCTIONAL,
                tenant: current,
                isPublic: RDStore.YN_NO
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
                isPublic: RDStore.YN_NO
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
                isPublic: RDStore.YN_NO
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
                isPublic: RDStore.YN_NO
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
                isPublic: RDStore.YN_NO
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
                isPublic: RDStore.YN_NO
        ],
                                addParams: [
                                        address: [[type:RefdataValue.getByValueAndCategory('Postal address','AddressType'),
                                                   additionFirst:'Erwerbungsabteilung',
                                                   street_1:'Musterhaus',
                                                   street_2:'1',
                                                   zipcode:'11111',
                                                   city:'Bremen',
                                                   state:RefdataValue.getByValueAndCategory('Bremen','Federal State'),
                                                   country:RefdataValue.getByValueAndCategory('DE','Country')]],
                                        personRoles: [[org:member5Bremen,
                                                       functionType: RefdataValue.getByValueAndCategory('Functional Contact Postal Address','Person Function')]]]
        ]
        Map technischerSupport = [mainParams: [
                last_name: 'Technischer Support',
                contactType: RDStore.CONTACT_TYPE_FUNCTIONAL,
                tenant: current,
                isPublic: RDStore.YN_NO
        ],
                                  addParams: [
                                          contact:[[contentType: RDStore.CCT_EMAIL,
                                                    type: generalData.contactTypes.get('Job-related'),
                                                    content: 'it_support@wef.com']],
                                          personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter E-Books'),
                                                         functionType: RefdataValue.getByValueAndCategory('Technical Support','Person Function')]]]
        ]
        Map franzEmil = [mainParams: [
                first_name:'franz',
                last_name:'emil',
                gender: generalData.gender.get('Third Gender'),
                contactType: RDStore.CONTACT_TYPE_PERSONAL,
                tenant: current,
                isPublic: RDStore.YN_NO
        ],
                         addParams: [
                                 contact: [[contentType:RDStore.CCT_EMAIL,
                                            type: generalData.contactTypes.get('Job-related'),
                                            content: 'stats@eugf.com']],
                                 personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter E-Books'),
                                                functionType: RefdataValue.getByValueAndCategory('Statistical Support','Person Function')]]]
        ]
        Map annaMueller = [mainParams:[
                first_name: 'anna',
                last_name: 'müller',
                gender: generalData.gender.get('Female'),
                contactType: RDStore.CONTACT_TYPE_PERSONAL,
                tenant: current,
                isPublic: RDStore.YN_NO
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
                isPublic: RDStore.YN_NO
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
                isPublic: RDStore.YN_NO
        ], addParams: [
                contact: [[contentType: RDStore.CCT_EMAIL,
                           type: generalData.contactTypes.get('Job-related'),
                           content: 'rechnungen@ewewf.com']],
                personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter E-Journals'),
                               functionType: RefdataValue.getByValueAndCategory('Functional Contact Billing Adress','Person Function')]]]
        ]
        Set<Map> personalContacts = [erwerbung,jamesFrank,peterKlein,miaMeyer,peterMiller,rechnungsadresse,technischerSupport,franzEmil,annaMueller,salesTeam,samSmith]
        personalContacts.each { contact ->
            createObject('Person',contact,current,null)
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
                                    [type:PropertyDefinition.findByName('ILL electronic').id,refValue:RDStore.PERM_PERM_EXPL,isShared:true],
                                    [type:PropertyDefinition.findByName('Governing jurisdiction').id,stringValue:'Berlin',isShared:true],
                                    [type:PropertyDefinition.findByName('Open Access').id,refValue:RefdataValue.getByValueAndCategory('No Open Access','License.OA.Type'),isShared:true],
                                    [type:PropertyDefinition.findByName('Invoicing').id,dateValue: null,paragraph: 'Immer im Laufzeitjahr...']
                            ],
                            createMemberLicense: true,
                            licenseDocuments: [[docstoreUUID:'f81a8371-7b2b-46f5-9608-bc2f3b3b80f7',isShared:true],[docstoreUUID:'24b8fd93-1297-4bca-bb91-3d0c582da808']]
                ]
        ]
        Map rahmenvertragEBookParams = [
                mainParams: [reference: "Rahmenvertrag eBook",
                             type: generalData.licenseType,
                             status: RDStore.LICENSE_CURRENT,
                             startDate: generalData.sdf.parse('2018-01-01')],
                addParams: [licensors: [
                        [generalData.exampleOrgs.get('Musteranbieter E-Books')]
                ],
                            customProperties: [
                                    [type:PropertyDefinition.findByName('ILL secure electronic transmission').id,refValue:RDStore.PERM_PROH_EXPL,isShared:true],
                                    [type:PropertyDefinition.findByName('Governing law').id,stringValue:'deutsches Recht',isShared:true],
                                    [type:PropertyDefinition.findByName('Open Access').id,refValue:RefdataValue.getByValueAndCategory('No Open Access','License.OA.Type'),isShared:true],
                                    [type:PropertyDefinition.findByName('Metadata delivery').id,stringValue: null]
                            ],
                            createMemberLicense: true,
                            licenseDocuments: [[docstoreUUID:'82f60372-b993-4415-959c-a8f4613405a8',isShared:true],[docstoreUUID:'3ae2fd52-6572-46d3-af47-bdb7ea32033a']]
                ]
        ]
        Set<Map> consortialLicenseParamsSet = [rahmenvertragParams,rahmenvertragEBookParams]
        Map<String,License> consortialLicenses = [:]
        consortialLicenseParamsSet.each { licParam ->
            License lic = (License) createObject("License",licParam,current,null)
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
                             manualCancellationDate: generalData.sdf.parse('2018-10-31'),
                             status: RDStore.SUBSCRIPTION_EXPIRED,
                             identifier: expiredDatenAIdentifier,
                             isPublic: RDStore.YN_NO,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('mixed'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('duz: Deutsche Universitätszeitung')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter A'),
                            sharedProperties:['name','form','resource'],
                            customProperties: [
                                    [type:PropertyDefinition.findByName('GASCO Entry').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('GASCO-Information-Link').id,urlValue:'https://www...',isShared:true],
                                    [type:PropertyDefinition.findByName('Eingeschränkter Benutzerkreis').id,stringValue:'Universitäten'],
                                    [type:PropertyDefinition.findByName('Neueinsteigerrabatt').id,refValue:RDStore.YN_YES,note:'25% auf den Jahrespreis'],
                                    [type:PropertyDefinition.findByName('Preis gerundet').id,refValue:RDStore.YN_NO],
                                    [type:PropertyDefinition.findByName('Rechnungsstellung durch Anbieter').id,refValue:RDStore.YN_NO],
                                    [type:PropertyDefinition.findByName('Simuser Zahl').id,stringValue:'unlimitiert',isShared:true],
                                    [type:PropertyDefinition.findByName('Testzeitraum').id,stringValue:'90']
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
                             isPublic: RDStore.YN_NO,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('mixed'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('duz: Deutsche Universitätszeitung')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter A'),
                            sharedProperties:['name','form','resource'],
                            subscriptionDocuments:[[docstoreUUID:'e45afb86-b5ef-49af-bd52-e93a7871a7b1']],
                            customProperties: [
                                    [type:PropertyDefinition.findByName('GASCO Entry').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('GASCO-Information-Link').id,urlValue:'https://www...'],
                                    [type:PropertyDefinition.findByName('Eingeschränkter Benutzerkreis').id,stringValue:'Universitäten'],
                                    [type:PropertyDefinition.findByName('Neueinsteigerrabatt').id,refValue:RDStore.YN_YES,note:'25% auf den Jahrespreis'],
                                    [type:PropertyDefinition.findByName('Preis gerundet').id,refValue:RDStore.YN_NO],
                                    [type:PropertyDefinition.findByName('Rechnungsstellung durch Anbieter').id,refValue:RDStore.YN_NO],
                                    [type:PropertyDefinition.findByName('Simuser Zahl').id,stringValue:'unlimitiert'],
                                    [type:PropertyDefinition.findByName('Testzeitraum').id,stringValue:'90']
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
                             status: RefdataValue.getByValueAndCategory('Test Access','Subscription Status'),
                             identifier: testDatenAIdentifier,
                             isPublic: RDStore.YN_NO,
                             impId: UUID.randomUUID().toString(),
                             form: generalData.subscriptionForms.get('test')],
                addParams: [subscriptionMembers:[[subMember:modelMember,
                                                  subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                  startDate:generalData.dates.startDateExpired,
                                                  endDate:generalData.dates.endDateExpired,
                                                  subIdentifier:consortialPartnerIdentifiers.modelMember.testDatenAIdentifier],
                                                 [subMember:member3Munich,
                                                  subOwner:consortialLicenses.get('Rahmenvertrag (Teilnehmervertrag)'),
                                                  startDate:generalData.sdf.parse('2018-05-01'),
                                                  endDate:generalData.sdf.parse('2018-05-31'),
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
                             isPublic: RDStore.YN_NO,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('database'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('Brepols: Bibliography of British and Irish History')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter B'),
                            sharedProperties:['name','form','resource'],
                            customProperties: [
                                    [type:PropertyDefinition.findByName('GASCO Entry').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('GASCO-Information-Link').id,urlValue:'https://www...',isShared:true],
                                    [type:PropertyDefinition.findByName('Preis gerundet').id,refValue:RDStore.YN_NO],
                                    [type:PropertyDefinition.findByName('Rabatt').id,stringValue:'22 %'],
                                    [type:PropertyDefinition.findByName('Rabattstaffel').id,stringValue:null],
                                    [type:PropertyDefinition.findByName('Rechnungsstellung durch Anbieter').id,refValue:RDStore.YN_NO,isShared:true]
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
                             isPublic: RDStore.YN_NO,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('database'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('Naxos: Naxos Music Library World')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter A'),
                            sharedProperties:['name','form','resource'],
                            subscriptionDocuments:[[docstoreUUID:'b2a5135f-97e6-4c27-ad04-419dfb5987df',isShared:true]],
                            customProperties: [
                                    [type:PropertyDefinition.findByName('GASCO Entry').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('GASCO-Information-Link').id,urlValue:null,isShared:true],
                                    [type:PropertyDefinition.findByName('GASCO-Verhandlername').id,stringValue:'Allianzlizenz Team'],
                                    [type:PropertyDefinition.findByName('Mehrjahreslaufzeit').id,refValue:RDStore.YN_NO,note:'opt out Klausel vorhanden',isShared:true],
                                    [type:PropertyDefinition.findByName('Mehrjahreslaufzeit ausgewählt').id,refValue:null,isShared:true],
                                    [type:PropertyDefinition.findByName('Preis gerundet').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('Rechnungszeitpunkt').id,stringValue:'Vorauszahlung',isShared:true],
                                    [type:PropertyDefinition.findByName('Simuser Zahl').id,stringValue:'unlimitiert',isShared:true]
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
                             isPublic: RDStore.YN_NO,
                             form: generalData.subscriptionForms.get('license'),
                             resource: generalData.subscriptionResources.get('database'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('Naxos: Naxos Music Library World')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter A'),
                            sharedProperties:['name','form','resource'],
                            subscriptionDocuments:[[docstoreUUID:'bddb3e64-4c15-4861-ac9f-8b7e1fd8f609',isShared:true]],
                            customProperties: [
                                    [type:PropertyDefinition.findByName('GASCO Entry').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('GASCO-Information-Link').id,urlValue:null,isShared:true],
                                    [type:PropertyDefinition.findByName('GASCO-Verhandlername').id,stringValue:'Allianzlizenz Team'],
                                    [type:PropertyDefinition.findByName('Mehrjahreslaufzeit').id,refValue:RDStore.YN_NO,note:'opt out Klausel vorhanden',isShared:true],
                                    [type:PropertyDefinition.findByName('Mehrjahreslaufzeit ausgewählt').id,refValue:null],
                                    [type:PropertyDefinition.findByName('Preis gerundet').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('Rechnungszeitpunkt').id,stringValue:'Vorauszahlung',isShared:true],
                                    [type:PropertyDefinition.findByName('Simuser Zahl').id,stringValue:'unlimitiert',isShared:true]
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
                             isPublic: RDStore.YN_NO,
                             form: generalData.subscriptionForms.get('singlePurchase'),
                             resource: generalData.subscriptionResources.get('ebookSingle'),
                             owner: consortialLicenses.get('Rahmenvertrag eBook')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('Thieme: Thieme eRef Lehrbuecher')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter E-Books'),
                            sharedProperties:['name','form','resource'],
                            customProperties: [
                                    [type:PropertyDefinition.findByName('GASCO Entry').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('GASCO-Information-Link').id,urlValue:'https://musterlink.de',isShared:true],
                                    [type:PropertyDefinition.findByName('Bundesweit offen').id,refValue:RDStore.YN_NO,note:'regionales Konsortium'],
                                    [type:PropertyDefinition.findByName('Mengenrabatt Stichtag').id,dateValue:generalData.sdf.parse('2019-11-30')],
                                    [type:PropertyDefinition.findByName('Preis gerundet').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('Simuser Zahl').id,stringValue:'unlimitiert',isShared:true],
                                    [type:PropertyDefinition.findByName('Testzeitraum').id,stringValue:'60'],
                                    [type:PropertyDefinition.findByName('Unterjähriger Einstieg').id,refValue:RDStore.YN_YES]
                            ],
                            privateProperties: [
                                    [type:PropertyDefinition.findByNameAndTenant('Quellensteuer-Befreiung',current).id,refValue:RefdataValue.getByValueAndCategory('Planed','YNO'),note:'der Anbieter hat dies beantragt']
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
                                                                subPkg: generalData.examplePackages.get('Thieme: Thieme eRef Lehrbuecher'),
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
                                                                subPkg: generalData.examplePackages.get('Thieme: Thieme eRef Lehrbuecher'),
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
                                                                subPkg: generalData.examplePackages.get('Thieme: Thieme eRef Lehrbuecher'),
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
                                                                subPkg: generalData.examplePackages.get('Thieme: Thieme eRef Lehrbuecher'),
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
                             manualCancellationDate: generalData.sdf.parse('2019-10-31'),
                             status: RDStore.SUBSCRIPTION_CURRENT,
                             identifier: journalPaketExtremIdentifier,
                             isPublic: RDStore.YN_NO,
                             form: generalData.subscriptionForms.get('purchaseOngoing'),
                             resource: generalData.subscriptionResources.get('ejournalPackage'),
                             owner: consortialLicenses.get('Rahmenvertrag')],
                addParams: [packages: [[pkg:generalData.examplePackages.get('American Chemical Society: American Chemical Society Journals')]],
                            provider: generalData.exampleOrgs.get('Musteranbieter E-Journals'),
                            sharedProperties:['name','form','resource'],
                            customProperties: [
                                    [type:PropertyDefinition.findByName('GASCO Entry').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('GASCO-Information-Link').id,urlValue:'https://www.hallo.de',isShared:true],
                                    [type:PropertyDefinition.findByName('Private Einrichtungen').id,refValue:RDStore.YN_YES],
                                    [type:PropertyDefinition.findByName('Rabatt Zählung').id,refValue:RDStore.YN_YES,note:'die Gesamtzahl der Teilnehmer pro Modul wird für die Findung der erreichten Rabattstufe summiert'],
                                    [type:PropertyDefinition.findByName('Simuser Zahl').id,stringValue:'unlimitiert',isShared:true]
                            ],
                            privateProperties: [
                                    [type:PropertyDefinition.findByNameAndTenant('Quellensteuer-Befreiung',current).id,refValue:RefdataValue.getByValueAndCategory('Unclear','YNO'),note:'dringend klären']
                            ],
                            tasks: [
                                    [title: 'Statistiken Counter5 & Sushi',
                                     status: RefdataValue.getByValueAndCategory('Open','Task Status'),
                                     endDate: generalData.sdf.parse('2019-06-30'),
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
            createObject('Subscription',consortialSubscription,current,null)
        }
        log.info("creating contacts with specific responsibilities ...")
        Map antonGross = [mainParams:[
                first_name: 'Anton',
                last_name: 'Gross',
                gender: generalData.gender.get('Male'),
                contactType: RDStore.CONTACT_TYPE_PERSONAL,
                tenant: current,
                isPublic: RDStore.YN_NO
        ],
                          addParams: [
                                  contact: [[contentType: RDStore.CCT_EMAIL,
                                             type: generalData.contactTypes.get('Job-related'),
                                             content: 'gross@anbieter.de'],
                                            [contentType: RDStore.CCT_PHONE,
                                             type: generalData.contactTypes.get('Job-related'),
                                             content: '123 456 789']],
                                  personRoles: [[org:generalData.exampleOrgs.get('Musteranbieter A'),
                                                 functionType: RefdataValue.getByValueAndCategory('Contact Person','Person Function')],
                                                [org:generalData.exampleOrgs.get('Musteranbieter A'),
                                                 responsibilityType: RefdataValue.getByValueAndCategory('Specific subscription editor','Person Responsibility'),
                                                 sub:Subscription.findByIdentifier(currentDatenbank2Identifier)]]
                          ]
        ]
        Set<Map> personalContactsWithSpecificResponsibilities = [antonGross]
        //this loop can be executed only if the objects to which the responsibility is bound to has been created already
        personalContactsWithSpecificResponsibilities.each { contact ->
            createObject('Person',contact,current,null)
        }
        log.info("create subscription linkings ...")
        setupLinking([owner:current,source:Subscription.findByIdentifier(currentDatenAIdentifier).id,destination:Subscription.findByIdentifier(currentDatenbank2Identifier).id,objectType:Subscription.class.name,linkType:RefdataValue.getByValueAndCategory('is condition for','Link Type')])
        Set<Map> subIdentifiersWithChildrenToLink = [
                [source:currentDatenAIdentifier,destination:expiredDatenAIdentifier],
                [source:intendedDatenbank2Identifier,destination:currentDatenbank2Identifier]
        ]
        subIdentifiersWithChildrenToLink.each { pair ->
            Subscription source = Subscription.findByIdentifier(pair.source)
            Subscription destination = Subscription.findByIdentifier(pair.destination)
            setupLinking([owner:current,source:source.id,destination:destination.id,objectType:Subscription.class.name,linkType:RDStore.LINKTYPE_FOLLOWS])
            source.derivedSubscriptions.each { childSub ->
                Org childSubscriber = childSub.orgRelations.find { it.roleType == RDStore.OR_SUBSCRIBER_CONS }.org
                Subscription childPair = destination.derivedSubscriptions.find { it.orgRelations.find { it.org == childSubscriber } }
                setupLinking([owner:current,source:childPair.id,destination:childPair.id,objectType:Subscription.class.name,linkType:RDStore.LINKTYPE_FOLLOWS])
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
                        obj.isSlaved = RDStore.YN_YES
                        break
                    case RDStore.SUBSCRIPTION_TYPE_LOCAL:
                        memberRole = new OrgRole(org:member,sub:obj,roleType:RDStore.OR_SUBSCRIBER)
                        break
                }
                break
            case 'License': obj = new License(params.mainParams)
                if(member) {
                    //this is the local counterpart for the consortial member license org role setup at the end of setupBasicTestData()
                    memberRole = new OrgRole(org:member,lic:obj,roleType:RDStore.OR_LICENSEE)
                }
                else if(consortium) {
                    //this is the org role setup for consortia
                    consRole = new OrgRole(org:consortium,lic:obj,roleType:RDStore.OR_LICENSING_CONSORTIUM)
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
                                        GlobalRecordInfo gri = GlobalRecordInfo.findByUuid(pkg.gokbId)
                                        GlobalRecordTracker grt = GlobalRecordTracker.findByOwner(gri)
                                        //let's kill the server a bit ... that should keep some packages always up-to-date!! - to be activated as soon as ERMS-1177 is terminated
                                        //globalSourceSyncService.initialiseTracker(grt)
                                        //dataloadService.updateFTIndexes()
                                        if(entry.issueEntitlementISBNs) {
                                            pkg.addToSubscription(obj,false)
                                            List<TitleInstancePackagePlatform> tippSubset = TitleInstancePackagePlatform.executeQuery("select tipp from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg and tipp.id in (select io.ti from IdentifierOccurrence io join io.identifier i where i.ns.ns = 'isbn' and i.value in (:idSet))",[pkg:pkg,idSet:entry.issueEntitlementISBNs])
                                            RefdataValue ieCurrent = RDStore.TIPP_STATUS_CURRENT
                                            tippSubset.each { tipp ->
                                                IssueEntitlement ie = new IssueEntitlement(status:ieCurrent,
                                                        subscription: obj,
                                                        tipp: tipp,
                                                        accessStartDate: tipp.accessStartDate,
                                                        accessEndDate: tipp.accessEndDate,
                                                        startDate: tipp.startDate,
                                                        startVolume: tipp.startVolume,
                                                        startIssue: tipp.startIssue,
                                                        endDate: tipp.endDate,
                                                        endVolume: tipp.endVolume,
                                                        endIssue: tipp.endIssue,
                                                        embargo: tipp.embargo,
                                                        coverageDepth: tipp.coverageDepth,
                                                        coverageNote: tipp.coverageNote,
                                                        ieReason: 'Automatically copied when creating subscription'
                                                )
                                                if(!ie.save()) {
                                                    throw new CreationException(ie.errors)
                                                }
                                                else {
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
                                // causing a session mismatch
                                // AbstractProperty newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY,obj,PropertyDefinition.get(v.type))
                                v.each { property ->
                                    SubscriptionCustomProperty newProp = new SubscriptionCustomProperty(owner:obj,type:PropertyDefinition.get(property.type))
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
                            case 'privateProperties':
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
                                break
                            case 'subscriptionMembers':
                                List synShareTargetList = []
                                v.each { entry ->
                                    Org subMember = (Org) entry.subMember
                                    Date startDate = entry.startDate ?: obj.startDate
                                    Date endDate = entry.endDate ?: obj.endDate
                                    RefdataValue status = entry.status ?: obj.status
                                    Subscription consSub = new Subscription()
                                    InvokerHelper.setProperties(consSub,obj.properties)
                                    consSub.startDate = startDate
                                    consSub.endDate = endDate
                                    consSub.status = status
                                    consSub.owner = entry.subOwner
                                    consSub.instanceOf = obj
                                    consSub.identifier = entry.subIdentifier
                                    consSub.impId = UUID.randomUUID()
                                    consSub.globalUID = null
                                    if(!consSub.save())
                                        throw new CreationException(consSub.errors)
                                    memberRole = new OrgRole(org:subMember,sub:consSub,roleType:RDStore.OR_SUBSCRIBER_CONS)
                                    OrgRole memberLicenseRole = new OrgRole(org:subMember,lic:entry.subOwner,roleType: RDStore.OR_LICENSEE_CONS)
                                    consRole = new OrgRole(org:consortium,sub:consSub,roleType:RDStore.OR_SUBSCRIPTION_CONSORTIA)
                                    if(!memberRole.save()) {
                                        throw new CreationException(memberRole.errors)
                                    }
                                    if(!memberLicenseRole.save()) {
                                        throw new CreationException(memberLicenseRole.errors)
                                    }
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
                                    SubscriptionCustomProperty.findAllByOwner(obj).each { scp ->
                                        AuditConfig ac = AuditConfig.getConfig(scp)
                                        if(ac) {
                                            //I do not understand what the difference in SubscriptionController is, so, I will not distinct here between multipleOccurrence or not
                                            AbstractProperty prop = new SubscriptionCustomProperty(owner:consSub,type:scp.type)
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
                                Task task = new Task(subscription: obj,title: t.title,description: t.description,endDate: t.endDate,status: t.status,responsibleOrg: t.responsibleOrg ?: null, responsibleUser: t.responsibleUser ?: null, creator: contextService.user, createDate: new Date())
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
                                    LicenseCustomProperty newProp = new LicenseCustomProperty(owner:obj,type:PropertyDefinition.get(property.type))
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
                                    Task task = new Task(license: obj,title: t.title,description: t.description,endDate: t.endDate,status: t.status,responsibleOrg: t.responsibleOrg ?: null, responsibleUser: t.responsibleUser ?: null, creator: contextService.user, createDate: new Date())
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
                    SubscriptionCustomProperty.findAllByOwner(obj.instanceOf).each { scp ->
                        AuditConfig ac = AuditConfig.getConfig(scp)
                        if(ac) {
                            //I do not understand what the difference in SubscriptionController is, so, I will not distinct here between multipleOccurrence or not
                            AbstractProperty prop = new SubscriptionCustomProperty(owner:obj,type:scp.type)
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
                isVisibleForSubscriber: costItemParams.isVisibleForSubscriber
        )
        if(ci && !ci.save()) {
            throw new CreationException(ci.errors)
        }
    }

    void createDocument(entry, obj) throws CreationException {
        try {
            String fPath = grailsApplication.config.documentStorageLocation ?: '/tmp/laser'
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

}
