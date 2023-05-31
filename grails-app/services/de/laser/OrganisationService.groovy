package de.laser

import de.laser.auth.Role
import de.laser.auth.User
import de.laser.config.ConfigMapper
import de.laser.properties.PropertyDefinition
import de.laser.remote.ApiSource
import de.laser.storage.RDStore
import de.laser.utils.AppUtils
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
    InstAdmService instAdmService
    UserService userService
    GokbService gokbService

    List<String> errors = []

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
     * Dumps the errors occurred during creation as an outputable string
     * @return the error list as a string joined by HTML line breaks for frontend display
     */
    String dumpErrors() {
        String out = errors.join('<br>')
        errors = []
        out
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

    Map<String, Map> getWekbOrgRecords(GrailsParameterMap params, Map result) {
        Map<String, Object> queryParams = [componentType: "Org"]
        if (params.curatoryGroup || params.providerRole) {
            if(params.curatoryGroup)
                queryParams.curatoryGroupExact = params.curatoryGroup.replaceAll('&','ampersand').replaceAll('\\+','%2B').replaceAll(' ','%20')
            if(params.providerRole)
                queryParams.role = RefdataValue.get(params.providerRole).value.replaceAll(' ','%20')
        }
        Map<String, Object> wekbResult = gokbService.doQuery(result, [max: 10000, offset: 0], queryParams)
        if(wekbResult.recordsCount > 0)
            wekbResult.records.collectEntries { Map wekbRecord -> [wekbRecord.uuid, wekbRecord] }
        else [:]
    }

}
