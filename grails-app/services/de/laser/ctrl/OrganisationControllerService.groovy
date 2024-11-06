package de.laser.ctrl


import de.laser.*
import de.laser.addressbook.Address
import de.laser.addressbook.Contact
import de.laser.auth.User
import de.laser.oap.OrgAccessPoint
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyOrg
import de.laser.utils.LocaleUtils
import de.laser.wekb.Platform
import grails.gorm.transactions.Transactional
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

/**
 * This service is a mirror of the {@link OrganisationController}, containing those controller methods
 * which manipulate data
 */
@Transactional
class OrganisationControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AccessPointService accessPointService
    ContextService contextService
    DocstoreService docstoreService
    FormService formService
    GokbService gokbService
    LinksGenerationService linksGenerationService
    MessageSource messageSource
    TaskService taskService
    WorkflowService workflowService

    PageRenderer groovyPageRenderer

    Map<String,Object> mailInfos(OrganisationController controller, GrailsParameterMap params) {
        User user = contextService.getUser()
        Org org = contextService.getOrg()
        Map<String, Object> result = [user:user,
                                      institution:org,
                                      contextOrg: org]


        if (params.id) {
            result.orgInstance = Org.get(params.id)
            if(result.orgInstance.id == org.id){
                return null
            }
            if (!contextService.getOrg().isCustomerType_Consortium() && !result.orgInstance.isCustomerType_Inst()) {
                return null
            }
        }
        else {
            return null
        }


        if (result.orgInstance) {
            String customerIdentifier = ''
            result.sub = Subscription.get(params.subscription)

            if(result.sub) {
                List contactListProvider = result.sub.providerRelations ? Contact.executeQuery("select c.content from PersonRole pr " +
                        "join pr.prs p join p.contacts c where pr.provider in :providers and " +
                        "pr.responsibilityType = :responsibilityType and c.contentType = :type and p.isPublic = false and p.tenant = :ctx and pr.sub = :obj",
                        [providers         : result.sub.providerRelations.provider,
                         responsibilityType: RDStore.PRS_RESP_SPEC_SUB_EDITOR,
                         type              : RDStore.CCT_EMAIL,
                         ctx               : result.contextOrg,
                         obj               : result.sub]) : null

                List contactListProviderAddressBook = result.sub.providerRelations ? Contact.executeQuery("select c.content from PersonRole pr " +
                        "join pr.prs p join p.contacts c where pr.provider in :providers and " +
                        "pr.functionType in (:functionTypes) and c.contentType = :type and p.isPublic = false and p.tenant = :ctx",
                        [providers         : result.sub.providerRelations.provider,
                         functionTypes: [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_CUSTOMER_SERVICE, RDStore.PRS_FUNC_INVOICING_CONTACT],
                         type              : RDStore.CCT_EMAIL,
                         ctx               : result.contextOrg]) : null

                List contactListProviderWekb = result.sub.providerRelations ? Contact.executeQuery("select c.content from PersonRole pr " +
                        "join pr.prs p join p.contacts c where pr.provider in :providers and " +
                        "pr.functionType in (:functionTypes) and c.contentType = :type and p.isPublic = true",
                        [providers    : result.sub.providerRelations.provider,
                         functionTypes: [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_CUSTOMER_SERVICE, RDStore.PRS_FUNC_INVOICING_CONTACT, RDStore.PRS_FUNC_SALES_MARKETING],
                         type         : RDStore.CCT_EMAIL]) : null

                contactListProvider = contactListProvider + contactListProviderAddressBook

                result.mailAddressOfProvider = contactListProvider ? contactListProvider.join("; ") : ''
                result.mailAddressOfProviderWekb = contactListProviderWekb ? contactListProviderWekb.join("; ") : ''

                List<Platform> platformList = Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription)', [subscription: result.sub])


                List<CustomerIdentifier> customerIdentifiers = CustomerIdentifier.findAllByCustomerAndPlatformInList(result.orgInstance, platformList)
                if (customerIdentifiers) {
                    customerIdentifier = customerIdentifiers.value.join('; ')
                }
            }

            result.mailText = ""

            ReaderNumber readerNumberStudents
            ReaderNumber readerNumberStaff
            ReaderNumber readerNumberFTE
            RefdataValue currentSemester = RefdataValue.getCurrentSemester()

            readerNumberStudents = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_STUDENTS, result.orgInstance, currentSemester)
            readerNumberStaff = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_SCIENTIFIC_STAFF, result.orgInstance, currentSemester)
            readerNumberFTE = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_FTE, result.orgInstance, currentSemester)

            if (!readerNumberStudents && !readerNumberStaff && !readerNumberFTE) {
                boolean nextSemester = false

                List<RefdataValue> refdataValueList = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.SEMESTER).reverse()
                for (int count = 0; count < refdataValueList.size(); count = count + 1) {
                    if (refdataValueList[count] == currentSemester) {
                        nextSemester = true
                    }
                    if (nextSemester) {
                        readerNumberStaff = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_SCIENTIFIC_STAFF, result.orgInstance, refdataValueList[count])
                        readerNumberStudents = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_STUDENTS, result.orgInstance, refdataValueList[count])
                        readerNumberFTE = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_FTE, result.orgInstance, refdataValueList[count])
                        if (readerNumberStudents || readerNumberStaff || readerNumberFTE) {
                            currentSemester = refdataValueList[count]
                            break
                        }
                    }
                }
            }

            RefdataValue generalContact     = RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
            RefdataValue responsibleAdmin   = RDStore.PRS_FUNC_RESPONSIBLE_ADMIN
            RefdataValue billingContact     = RDStore.PRS_FUNC_INVOICING_CONTACT

            List contactList = Contact.executeQuery("select c.content, pr.functionType from PersonRole pr " +
                    "join pr.prs p join p.contacts c where pr.org = :org and " +
                    "pr.functionType in (:functionTypes) and c.contentType = :type and p.isPublic = true",
                    [org: result.orgInstance,
                     functionTypes: [generalContact, responsibleAdmin, billingContact],
                     type: RDStore.CCT_EMAIL])

            List generalContactsList = []
            List responsibleAdminsList = []
            List billingContactsList = []

            contactList.each { row ->
                String c = row[0]
                if(generalContact == row[1]){
                    generalContactsList << c
                }
                else if(responsibleAdmin == row[1]){
                    responsibleAdminsList << c
                }
                else if(billingContact == row[1]){
                    billingContactsList << c
                }
            }

            String adressFilter = ' and a.pob = null and a.pobZipcode = null and a.pobCity = null'
            String postBoxFilter = ' and (a.pob is not null or a.pobZipcode is not null or a.pobCity is not null)'
            Set<Address> addressList = Address.executeQuery("select a from Address a join a.type type where type = :type and a.org = :org and a.tenant = null "+adressFilter, [org: result.orgInstance, type: RDStore.ADDRESS_TYPE_BILLING])
            Set<Address> postBoxList = Address.executeQuery("select a from Address a join a.type type where type = :type and a.org = :org and a.tenant = null "+postBoxFilter, [org: result.orgInstance, type: RDStore.ADDRESS_TYPE_BILLING])

            String billingAddress = addressList.collect { Address address -> address.getAddressForExport()}.join(";")
            String billingPostBox = postBoxList.collect { Address address -> address.getAddressForExport()}.join(";")

            if(params.surveyConfigID){
                SurveyConfig surveyConfig = params.surveyConfigID ? SurveyConfig.get(params.long('surveyConfigID')) : null
                if(surveyConfig && surveyConfig.invoicingInformation && surveyConfig.surveyInfo.owner == result.contextOrg){
                    SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, result.orgInstance)
                    if(surveyOrg.address) {
                        String adressSurveyFilter = ' and a.pob = null and a.pobZipcode = null and a.pobCity = null'
                        String postBoxSurveyFilter = ' and (a.pob is not null or a.pobZipcode is not null or a.pobCity is not null)'
                        Set<Address> addressSurveyList = Address.executeQuery("select a from Address a join a.type type where a.id = :adressID " + adressSurveyFilter, [adressID: surveyOrg.address.id])
                        Set<Address> postBoxSurveyList = Address.executeQuery("select a from Address a join a.type type where a.id = :adressID " + postBoxSurveyFilter, [adressID: surveyOrg.address.id])

                        billingAddress = addressSurveyList.collect { Address address -> address.getAddressForExport() }.join(";")
                        billingPostBox = postBoxSurveyList.collect { Address address -> address.getAddressForExport() }.join(";")
                    }

                    if(surveyOrg.person) {
                        billingContactsList = Contact.executeQuery("select c.content from PersonRole pr " +
                                "join pr.prs p join p.contacts c where pr.prs.id = :personId and c.contentType = :type",
                                [personId: surveyOrg.person.id, type: RDStore.CCT_EMAIL])
                    }
                }
            }

            String generalContacts = generalContactsList.join('; ')
            String responsibleAdmins = responsibleAdminsList.join('; ')
            String billingContacts = billingContactsList.join('; ')


            List accessPoints = []

            OrgAccessPoint.findAllByOrgAndAccessMethod(result.orgInstance, RDStore.ACCESS_POINT_TYPE_IP, [sort: ["name": 'asc']]).each { OrgAccessPoint accessPoint ->
                Map<String, Object> accessPointData = accessPoint.getAccessPointIpRanges()
                List ipv4Ranges = accessPointData.ipv4Ranges
                List ipv6Ranges = accessPointData.ipv6Ranges
                if (ipv4Ranges || ipv6Ranges) {
                    accessPoints << [name: "IPv4/IPv6 (${accessPoint.name})", ipv4Ranges: ipv4Ranges ? ipv4Ranges.ipRange : null, ipv6Ranges: ipv6Ranges ? ipv6Ranges.ipRange : null]
                }
            }

            OrgAccessPoint.findAllByOrgAndAccessMethod(result.orgInstance, RDStore.ACCESS_POINT_TYPE_PROXY, [sort: ["name": 'asc']]).each { OrgAccessPoint accessPoint ->
                Map<String, Object> accessPointData = accessPoint.getAccessPointIpRanges()
                List ipv4Ranges = accessPointData.ipv4Ranges
                List ipv6Ranges = accessPointData.ipv6Ranges
                if (ipv4Ranges || ipv6Ranges) {
                    accessPoints << [name: "Proxy IPv4/IPv6 (${accessPoint.name})", ipv4Ranges: ipv4Ranges ? ipv4Ranges.ipRange : null, ipv6Ranges: ipv6Ranges ? ipv6Ranges.ipRange : null]
                }
            }

            OrgAccessPoint.findAllByOrgAndAccessMethod(result.orgInstance, RDStore.ACCESS_POINT_TYPE_EZPROXY, [sort: ["name": 'asc']]).each { OrgAccessPoint accessPoint ->
                Map<String, Object> accessPointData = accessPoint.getAccessPointIpRanges()
                List ipv4Ranges = accessPointData.ipv4Ranges
                List ipv6Ranges = accessPointData.ipv6Ranges
                if (ipv4Ranges || ipv6Ranges) {
                    accessPoints << [name: "EZProxy IPv4/IPv6 (${accessPoint.name})", ipv4Ranges: ipv4Ranges ? ipv4Ranges.ipRange : null, ipv6Ranges: ipv6Ranges ? ipv6Ranges.ipRange : null, url: accessPoint.hasProperty('url') ? accessPoint.url : null]
                }
            }

            OrgAccessPoint.findAllByOrgAndAccessMethod(result.orgInstance, RDStore.ACCESS_POINT_TYPE_MAIL_DOMAIN, [sort: ["name": 'asc']]).each { OrgAccessPoint accessPoint ->
                def accessPointDataList = accessPoint.accessPointData
                if (accessPointDataList) {
                    accessPoints << [name: "MailDomain (${accessPoint.name})", mailDomains: accessPointDataList.data]
                }
            }

            OrgAccessPoint.findAllByOrgAndAccessMethod(result.orgInstance, RDStore.ACCESS_POINT_TYPE_SHIBBOLETH, [sort: ["name": 'asc']]).each { OrgAccessPoint accessPoint ->
                if (accessPoint.hasProperty('entityId')) {
                    accessPoints << [name: "Shibboleth (${accessPoint.name})", entityId: accessPoint.entityId]
                }
            }

            OrgAccessPoint.findAllByOrgAndAccessMethod(result.orgInstance, RDStore.ACCESS_POINT_TYPE_OA, [sort: ["name": 'asc']]).each { OrgAccessPoint accessPoint ->
                if (accessPoint.hasProperty('entityId')) {
                    accessPoints << [name: "OpenAthens (${accessPoint.name})", entityId: accessPoint.entityId]
                }
            }

            String vatID = result.orgInstance.getIdentifierByType(IdentifierNamespace.VAT)?.value

            result.language = params.newLanguage && params.newLanguage in [RDStore.LANGUAGE_DE.value, RDStore.LANGUAGE_EN.value] ? params.newLanguage : 'de'
            Locale language = new Locale(result.language)
            result.mailText = groovyPageRenderer.render view: '/mailTemplates/text/orgInfos', contentType: "text", encoding: "UTF-8", model: [language            : language,
                                                                                                                                              org                 : result.orgInstance,
                                                                                                                                              customerIdentifier  : customerIdentifier,
                                                                                                                                              sub                 : result.sub,
                                                                                                                                              readerNumberStudents: readerNumberStudents,
                                                                                                                                              readerNumberStaff   : readerNumberStaff,
                                                                                                                                              readerNumberFTE     : readerNumberFTE,
                                                                                                                                              currentSemester     : currentSemester,
                                                                                                                                              generalContacts     : generalContacts,
                                                                                                                                              responsibleAdmins   : responsibleAdmins,
                                                                                                                                              billingContacts     : billingContacts,
                                                                                                                                              accessPoints        : accessPoints,
                                                                                                                                              billingAddress       : billingAddress,
                                                                                                                                              billingPostBox: billingPostBox,
                                                                                                                                              vatID: vatID]

        } else {
            return null
        }
        result
    }


    //--------------------------------------------- member section -------------------------------------------------

    /**
     * Creates a new institution as member for the current consortium with the submitted parameters
     * @param controller the controller instance
     * @param params the input map containing the new institution's parameters
     * @return OK and the new institution details if the creation was successful, ERROR otherwise
     */
    Map<String,Object> createMember(OrganisationController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller,params)
        Org orgInstance
        Locale locale = LocaleUtils.getCurrentLocale()
        if(formService.validateToken(params)) {
            try {
                // createdBy will set by Org.beforeInsert()
                orgInstance = new Org(name: params.institution, status: RDStore.O_STATUS_CURRENT)
                orgInstance.save()
                Combo newMember = new Combo(fromOrg:orgInstance,toOrg:result.institution,type: RDStore.COMBO_TYPE_CONSORTIUM)
                newMember.save()
                orgInstance.setDefaultCustomerType()
//                orgInstance.addToOrgType(RDStore.OT_INSTITUTION) //RDStore adding causes a DuplicateKeyException - RefdataValue.getByValueAndCategory('Institution', RDConstants.ORG_TYPE)
                orgInstance.orgType_new = RDStore.OT_INSTITUTION
                result.orgInstance = orgInstance
                Object[] args = [messageSource.getMessage('org.institution.label',null,locale), orgInstance.name]
                result.message = messageSource.getMessage('default.created.message', args, locale)
                [result:result,status:STATUS_OK]
            }
            catch (Exception e) {
                log.error("Problem creating institution")
                log.error(e.printStackTrace())
                Object[] args = [orgInstance ? orgInstance.errors : 'unbekannt']
                result.message = messageSource.getMessage("org.error.createInstitutionError", args, locale)
                [result:result,status:STATUS_ERROR]
            }
        }
        else [result:null,status:STATUS_ERROR]
    }

    /**
     * Switches the consortial membership state between a consortium and a given institution
     * @param controller the controller instance
     * @param params the parameter map containing the combo link data
     * @return OK if the switch was successful, ERROR otherwise
     */
    Map<String, Object> toggleCombo(OrganisationController controller, GrailsParameterMap params) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Map<String, Object> result = getResultGenericsAndCheckAccess(controller, params)
        if (!result) {
            return [result:null, status:STATUS_ERROR]
        }
        if (!params.direction) {
            result.error = messageSource.getMessage('org.error.noToggleDirection',null,locale)
            return [result:result, status:STATUS_ERROR]
        }
        switch(params.direction) {
            case 'add':
                Map map = [toOrg: result.institution, fromOrg: Org.get(params.fromOrg), type: RDStore.COMBO_TYPE_CONSORTIUM]
                if (! Combo.findByToOrgAndFromOrgAndType(result.institution, Org.get(params.fromOrg), RDStore.COMBO_TYPE_CONSORTIUM)) {
                    Combo cmb = new Combo(map)
                    cmb.save()
                }
                break
            case 'remove':
                if(Subscription.executeQuery("from Subscription as s where exists ( select o from s.orgRelations as o where o.org in (:orgs) )", [orgs: [result.institution, Org.get(params.fromOrg)]])){
                    result.error = messageSource.getMessage('org.consortiaToggle.remove.notPossible.sub',null,locale)
                    return [result:result, status:STATUS_ERROR]
                }
                else if(License.executeQuery("from License as l where exists ( select o from l.orgRelations as o where o.org in (:orgs) )", [orgs: [result.institution, Org.get(params.fromOrg)]])){
                    result.error = messageSource.getMessage('org.consortiaToggle.remove.notPossible.lic',null,locale)
                    return [result:result, status:STATUS_ERROR]
                }
                else {
                    Combo cmb = Combo.findByFromOrgAndToOrgAndType(result.institution,
                            Org.get(params.fromOrg),
                            RDStore.COMBO_TYPE_CONSORTIUM)
                    cmb.delete()
                }
                break
        }
        [result:result, status:STATUS_OK]
    }

    //--------------------------------------------- workflows -------------------------------------------------

    /**
     * Gets the workflows linked to the given organisation
     * @param controller the controller instance
     * @param params the request parameter map
     * @return OK if the retrieval was successful, ERROR otherwise
     */
    Map<String,Object> workflows(OrganisationController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(controller, params)

        workflowService.executeCmdAndUpdateResult(result, params)

        [result: result, status: (result ? STATUS_OK : STATUS_ERROR)]
    }

    //--------------------------------------------- identifier section -------------------------------------------------

    /**
     * Deletes the given customer identifier
     * @param controller the controller instance
     * @param params the parameter map containing the identifier data
     * @return OK if the deletion was successful, ERROR otherwise
     */
    Map<String,Object> deleteCustomerIdentifier(OrganisationController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller,params)
        Locale locale = LocaleUtils.getCurrentLocale()
        CustomerIdentifier ci = CustomerIdentifier.get(params.long('deleteCI'))
        Org owner = ci.owner
        if (ci) {
            ci.delete()
            log.debug("CustomerIdentifier deleted: ${params}")
            [result:result,status:STATUS_OK]
        } else {
            if ( ! ci ) {
                Object[] args = [messageSource.getMessage('org.customerIdentifier',null,locale), params.deleteCI]
                result.error = messageSource.getMessage('default.not.found.message', args, locale)
            }
            log.error("CustomerIdentifier NOT deleted: ${params}; CustomerIdentifier not found or ContextOrg is not " +
                    "owner of this CustomerIdentifier and has no rights to delete it!")
            [result:result,status:STATUS_ERROR]
        }
    }

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Sets parameters which are used in many controller pages such as current user, context institution and perspectives
     * @param controller the controller instance
     * @param params the request parameter map
     * @return a result map containing the current user, institution, flags whether the view is that of the context
     * institution and which settings are available for the given call
     */
    Map<String, Object> getResultGenericsAndCheckAccess(OrganisationController controller, GrailsParameterMap params) {

        User user = contextService.getUser()
        Org org = contextService.getOrg()
        Map<String, Object> result = [user:user,
                                      institution:org,
                                      contextOrg: org,
                                      inContextOrg:true,
                                      isMyOrg:false,
                                      institutionalView:false,
                                      isGrantedOrgRoleAdminOrOrgEditor: SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'),
                                      isGrantedOrgRoleAdmin: SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'),
                                      contextCustomerType:org.getCustomerType()]

        //if(result.contextCustomerType == 'ORG_CONSORTIUM_BASIC')

        if (params.id) {
            result.orgInstance = Org.get(params.id)
            result.editable = controller._checkIsEditable(result.orgInstance)
            result.inContextOrg = result.orgInstance.id == org.id
            //this is a flag to check whether the page has been called for a consortia or inner-organisation member
            Combo checkCombo = Combo.findByFromOrgAndToOrg(result.orgInstance,org)
            if (checkCombo && checkCombo.type == RDStore.COMBO_TYPE_CONSORTIUM) {
                result.institutionalView = true
                result.isMyOrg = true //we make the existence of a combo relation condition to "my"
            }
            else if(!checkCombo) {
                checkCombo = Combo.findByToOrgAndFromOrgAndType(result.orgInstance, org, RDStore.COMBO_TYPE_CONSORTIUM)
                if(checkCombo) {
                    result.consortialView = true
                    result.isMyOrg = true
                }
            }
            //restrictions hold if viewed org is not the context org
//            if (!result.inContextOrg && !contextService.getOrg().isCustomerType_Consortium() && !SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
//                //restrictions further concern only single users or consortium members, not consortia
//                if (!contextService.getOrg().isCustomerType_Consortium() && result.orgInstance.isCustomerType_Inst()) {
//                    return null
//                }
//            }
            if (!result.inContextOrg && !SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
                //restrictions further concern only single users or consortium members, not consortia
                if (!(contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) && result.orgInstance.isCustomerType_Inst()) {
                    return null
                }
            }
        }
        else {
            result.editable = controller._checkIsEditable(org)
            result.orgInstance = result.institution
            result.inContextOrg = true
        }

        int tc1 = taskService.getTasksByResponsibilityAndObject(result.user, result.orgInstance).size()
        int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.orgInstance).size()
        result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''
        result.docsCount        = docstoreService.getDocsCount(result.orgInstance, result.contextOrg)
        result.notesCount       = docstoreService.getNotesCount(result.orgInstance, result.contextOrg)
        result.checklistCount   = workflowService.getWorkflowCount(result.orgInstance, result.contextOrg)

        result.links = linksGenerationService.getOrgLinks(result.orgInstance)
        Map<String, List> nav = (linksGenerationService.generateNavigation(result.orgInstance, true))
        result.navPrevOrg = nav.prevLink
        result.navNextOrg = nav.nextLink

        result
    }
}