package de.laser

import de.laser.properties.OrgProperty
import de.laser.properties.PropertyDefinition
import de.laser.remote.ApiSource
import de.laser.storage.RDStore
import de.laser.traces.DeletedObject
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

/**
 * This service handles specific organisation-related matters
 */
@Transactional
class OrganisationService {

    ContextService contextService
    MessageSource messageSource
    ExportService exportService
    GokbService gokbService

    List<String> errors = []

    static String RESULT_BLOCKED            = 'RESULT_BLOCKED'
    static String RESULT_SUCCESS            = 'RESULT_SUCCESS'
    static String RESULT_ERROR              = 'RESULT_ERROR'

    /**
     * Initialises mandatory keys for a new organisation
     * @param org the {@link Org} to be set up
     */
    void initMandatorySettings(Org org) {
        log.debug("initMandatorySettings for org ${org.id}") //org.id call crashes when called from sync

        if (OrgSetting.get(org, OrgSetting.KEYS.NATSTAT_SERVER_ACCESS) == OrgSetting.SETTING_NOT_FOUND) {
            OrgSetting.add(org, OrgSetting.KEYS.NATSTAT_SERVER_ACCESS, RDStore.YN_NO)
        }
        if (OrgSetting.get(org, OrgSetting.KEYS.NATSTAT_SERVER_API_KEY) == OrgSetting.SETTING_NOT_FOUND) {
            OrgSetting.add(org, OrgSetting.KEYS.NATSTAT_SERVER_API_KEY,'')
        }
        if (OrgSetting.get(org, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID) == OrgSetting.SETTING_NOT_FOUND) {
            OrgSetting.add(org, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID, '')
        }
        if (OrgSetting.get(org, OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS) == OrgSetting.SETTING_NOT_FOUND) {
            OrgSetting.add(org, OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS, RDStore.YN_NO)
        }
        if (OrgSetting.get(org, OrgSetting.KEYS.EZB_SERVER_ACCESS) == OrgSetting.SETTING_NOT_FOUND) {
            OrgSetting.add(org, OrgSetting.KEYS.EZB_SERVER_ACCESS, RDStore.YN_NO)
        }

        // called after
        // new Org.save()
        // does not work unless session is not flushed what causes crashes in sync
    }

    /**
     * Exports organisation data in the given format. It can be specified if higher education titles should be outputted or not.
     * @param orgs the {@link List} of {@link Org}s
     * @param message the title of the Excel sheet (not used in csv)
     * @param addHigherEducationTitles add columns library type, library network, funder type, federal state, country with respective values
     * @param format the format (xls or csv) to generate the output in
     * @return a String containing the CSV output or the Excel sheet containing the output
     */
    def exportOrg(List orgs, message, boolean addHigherEducationTitles, String format) {
        Locale locale = LocaleUtils.getCurrentLocale()

        List<String> titles = [
                messageSource.getMessage('org.sortname.label',null, locale),
                'Name'
        ]
        if(addHigherEducationTitles) {
            titles.add(messageSource.getMessage('org.libraryType.label',null, locale))
            titles.add(messageSource.getMessage('org.libraryNetwork.label',null, locale))
            titles.add(messageSource.getMessage('org.funderType.label',null, locale))
            titles.add(messageSource.getMessage('org.region.label',null, locale))
            titles.add(messageSource.getMessage('org.country.label',null, locale))
        }
        RefdataValue generalContact     = RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
        RefdataValue responsibleAdmin   = RDStore.PRS_FUNC_RESPONSIBLE_ADMIN
        RefdataValue billingContact     = RDStore.PRS_FUNC_FC_BILLING_ADDRESS
        titles.addAll(['ISIL','WIB-ID','EZB-ID',generalContact.getI10n('value')])
        if(addHigherEducationTitles)
            titles.add(responsibleAdmin.getI10n('value'))
        titles.add(billingContact.getI10n('value'))
        Set<PropertyDefinition> propertyDefinitions = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
        titles.addAll(exportService.loadPropListHeaders(propertyDefinitions))
        List orgData = []
        Map<Org,Map<String,String>> identifiers = [:]
        List identifierList = Identifier.executeQuery("select ident, ident.org from Identifier ident where ident.org in (:orgs) and ident.ns.ns in (:namespaces) and ident.value != null and ident.value != :unknown",[orgs:orgs,namespaces:['wibid','ezb','ISIL'],unknown:IdentifierNamespace.UNKNOWN])
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
        List contactList = Contact.executeQuery("select c.content, pr.org, pr.functionType from PersonRole pr join pr.prs p join p.contacts c where pr.org in (:orgs) and pr.functionType in (:functionTypes) and c.contentType = :type and p.isPublic = true",[orgs:orgs, functionTypes:[generalContact, responsibleAdmin, billingContact], type: RDStore.CCT_EMAIL])
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
            case [ "xls", "xlsx" ]:
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
                    if(addHigherEducationTitles)
                        row.add([field: furtherData.responsibleAdmin ?: '', style: null])
                    //Billing contact
                    row.add([field: furtherData.billingContact ?: '', style: null])
                    row.addAll(exportService.processPropertyListValues(propertyDefinitions, format, org, null, null, null))
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
                    if(addHigherEducationTitles)
                        row.add(furtherData.responsibleAdmin ?: '')
                    //Billing contact
                    row.add(furtherData.billingContact ?: '')
                    row.addAll(exportService.processPropertyListValues(propertyDefinitions, format, org, null, null, null))
                    orgData.add(row)
                }
                return exportService.generateSeparatorTableString(titles,orgData,',')
        }

    }

    /**
     * Helper method to group reader numbers by their key property which is a temporal unit
     * @param readerNumbers the {@link List} of {@link ReaderNumber}s to group
     * @param keyProp may be a dueDate or semester; a temporal unit to group the reader numbers by
     * @return the {@link Map} with the grouped result entries
     */
    Map<String,Map<String,ReaderNumber>> groupReaderNumbersByProperty(List<ReaderNumber> readerNumbers,String keyProp) {
        Map<String,Map<String,ReaderNumber>> result = [:]
        readerNumbers.each { ReaderNumber number ->
            Map<String,ReaderNumber> numberRow = result.get(number[keyProp])
            if(!numberRow) {
                numberRow = [:]
            }
            numberRow.put(number.referenceGroup.getI10n("value"),number)
            result.put(number[keyProp],numberRow)
        }
        result
    }

    /**
     * Gets all platforms where a provider {@link Org} is assigned to, ordered by name, sortname and platform name
     * @return the ordered {@link List} of {@link Platform}s
     */
    List<Platform> getAllPlatforms() {
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        Set<String> uuids = []
        Map<String, Object> result = gokbService.doQuery([user: contextService.getUser(), editUrl: apiSource.editUrl], [max: '1000', offset: '0'], [componentType: 'Platform', status: 'Current'])
        uuids.addAll(result.records.collect { Map platRecord -> platRecord.uuid })
        Platform.executeQuery('select p from Platform p join p.org o where p.gokbId in (:uuids) and p.org is not null order by o.name, o.sortname, p.name', [uuids: uuids])
    }

    /**
     * Gets a (filtered) map of provider records from the we:kb
     * @param params the request parameters
     * @param result a result generics map, containing also configuration params for the request
     * @return a {@link Map} of structure [providerUUID: providerRecord] containing the request results
     */
    Map<String, Map> getWekbOrgRecords(GrailsParameterMap params, Map result) {
        Map<String, Map> records = [:]
        Set<String> componentTypes = ['Org', 'Vendor']
        componentTypes.each { String componentType ->
            Map<String, Object> queryParams = [componentType: componentType]
            if (params.curatoryGroup || params.providerRole) {
                if(params.curatoryGroup)
                    queryParams.curatoryGroupExact = params.curatoryGroup.replaceAll('&','ampersand').replaceAll('\\+','%2B').replaceAll(' ','%20')
                if(params.providerRole)
                    queryParams.role = RefdataValue.get(params.providerRole).value.replaceAll(' ','%20')
            }
            Map<String, Object> wekbResult = gokbService.doQuery(result, [max: 10000, offset: 0], queryParams)
            if(wekbResult.recordsCount > 0)
                records.putAll(wekbResult.records.collectEntries { Map wekbRecord -> [wekbRecord.uuid, wekbRecord] })
        }
        records
    }

    Map<String, Object> getNamespacesWithValidations() {
        Map<String, Object> result = [:]
        Locale locale = LocaleUtils.getCurrentLocale()
        IdentifierNamespace.findAllByValidationRegexIsNotNull().each { IdentifierNamespace idns ->
            result[idns.id] = [pattern: idns.validationRegex, prompt: messageSource.getMessage("validation.${idns.ns.replaceAll(' ','_')}Match", null, locale), placeholder: messageSource.getMessage("identifier.${idns.ns.replaceAll(' ','_')}.info", null, locale)]
        }
        result
    }

    /**
     * Merges the given two organisations; displays eventual attached objects
     * @param org the organisation which should be merged
     * @param replacement the organisation to merge with
     * @param dryRun should the merge avoided and only information be fetched?
     * @return a map returning the information about the organisation
     */
    Map<String, Object> mergeOrganisations(Org org, Org replacement, boolean dryRun) {

        Map<String, Object> result = [:]

        // gathering references

        List ids            = new ArrayList(org.ids)
        List outgoingCombos = new ArrayList(org.outgoingCombos)
        List incomingCombos = new ArrayList(org.incomingCombos)

        List orgLinks      = new ArrayList(org.links)

        List addresses      = new ArrayList(org.addresses)
        List contacts       = new ArrayList(org.contacts)
        List prsLinks       = new ArrayList(org.prsLinks)
        List docContexts    = new ArrayList(org.documents)
        List platforms      = new ArrayList(org.platforms)
        List tipps          = TitleInstancePackagePlatform.executeQuery('select oo.tipp.id from OrgRole oo where oo.org = :source and oo.tipp != null', [source: org])

        List customProperties       = new ArrayList(org.propertySet.findAll { it.type.tenant == null })
        List privateProperties      = new ArrayList(org.propertySet.findAll { it.type.tenant != null })

        // collecting information

        result.info = []

        //result.info << ['Links: Orgs', links, FLAG_BLOCKER]

        result.info << ['Identifikatoren', ids]
        result.info << ['Combos (out)', outgoingCombos]
        result.info << ['Combos (in)', incomingCombos]

        result.info << ['OrgRoles', orgLinks]

        result.info << ['Adressen', addresses]
        result.info << ['Kontaktdaten', contacts]
        result.info << ['Personen', prsLinks]
        result.info << ['Dokumente', docContexts]   // delete ? docContext->doc
        result.info << ['Plattformen', platforms]
        result.info << ['Titel', tipps]
        //result.info << ['TitleInstitutionProvider (inst)', tips, FLAG_BLOCKER]
        //result.info << ['TitleInstitutionProvider (provider)', tipsProviders, FLAG_BLOCKER]
        //result.info << ['TitleInstitutionProvider (provider)', tipsProviders, FLAG_SUBSTITUTE]

        result.info << ['Allgemeine Merkmale', customProperties]
        result.info << ['Private Merkmale', privateProperties]


        // checking constraints and/or processing

        result.mergeable = true

        if (dryRun || ! result.mergeable) {
            return result
        }
        else {
            Org.withTransaction { status ->

                try {
                    Map<String, Object> genericParams = [source: org, target: replacement]
                    // identifiers
                    org.ids.clear()
                    ids.each { Identifier id ->
                        id.org = replacement
                        id.save()
                    }

                    org.outgoingCombos.clear()
                    org.incomingCombos.clear()
                    outgoingCombos.each { Combo c ->
                        c.fromOrg = replacement
                        c.save()
                    }
                    incomingCombos.each { Combo c ->
                        c.toOrg = replacement
                        c.save()
                    }

                    // orgTypes
                    //org.orgType.clear()
                    //orgTypes.each{ tmp -> tmp.delete() }
                    org.links.clear()
                    orgLinks.each { OrgRole oo ->
                        Map<String, Object> checkParams = [target: replacement, roleType: oo.roleType]
                        String targetClause = ''
                        if(oo.sub) {
                            targetClause = 'oo.sub = :sub'
                            checkParams.sub = oo.sub
                        }
                        else if(oo.lic) {
                            targetClause = 'oo.lic = :lic'
                            checkParams.lic = oo.lic
                        }
                        else if(oo.pkg) {
                            targetClause = 'oo.pkg = :pkg'
                            checkParams.pkg = oo.pkg
                        }
                        else if(oo.tipp) {
                            targetClause = 'oo.tipp = :tipp'
                            checkParams.tipp = oo.tipp
                        }
                        List orgRoleCheck = OrgRole.executeQuery('select oo from OrgRole oo where oo.org = :target and oo.roleType = :roleType and '+targetClause, checkParams)
                        if(!orgRoleCheck) {
                            oo.org = replacement
                            oo.save()
                        }
                        else {
                            oo.delete()
                        }
                    }

                    // orgSettings
                    /*
                    Set<String> specialAccess = []
                    Set<OrgSetting.KEYS> specGrants = [OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS, OrgSetting.KEYS.EZB_SERVER_ACCESS]
                    specGrants.each { OrgSetting.KEYS specGrant ->
                        if(OrgSetting.get(org, specGrant) == RDStore.YN_YES) {
                            specialAccess.addAll(ApiToolkit.getOrgsWithSpecialAPIAccess(specGrant))
                        }
                    }
                    */
                    OrgSetting.executeUpdate('delete from OrgSetting os where os.org = :source', [source: org])

                    // addresses
                    org.addresses.clear()
                    log.debug("${Address.executeUpdate('update Address a set a.org = :target where a.org = :source', genericParams)} addresses updated")

                    // contacts
                    org.contacts.clear()
                    log.debug("${Contact.executeUpdate('update Contact c set c.org = :target where c.org = :source', genericParams)} contacts updated")

                    // private properties
                    //org.privateProperties.clear()
                    //privateProperties.each { tmp -> tmp.delete() }

                    // custom properties
                    org.propertySet.clear()
                    log.debug("${OrgProperty.executeUpdate('update OrgProperty op set op.owner = :target where op.owner = :source', genericParams)} properties updated")

                    org.altnames.clear()
                    log.debug("${AlternativeName.executeUpdate('update AlternativeName alt set alt.org = :target where alt.org = :source', genericParams)} alternative names updated")
                    AlternativeName.construct([name: org.name, org: replacement])

                    org.delete()

                    DeletedObject.withTransaction {
                        DeletedObject.construct(org)
                    }
                    status.flush()

                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    log.error 'error while merging org ' + org.id + ' .. rollback: ' + e.message
                    e.printStackTrace()
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                }
            }
        }

        result
    }
}
