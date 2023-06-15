package de.laser


import de.laser.auth.User
import de.laser.finance.*
import de.laser.interfaces.CalculatedType
import de.laser.stats.Fact
import de.laser.storage.RDStore
import de.laser.oap.OrgAccessPoint
import de.laser.properties.*
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import de.laser.system.SystemProfiler
import de.laser.titles.TitleHistoryEvent
import de.laser.titles.TitleHistoryEventParticipant
import de.laser.traces.DeletedObject
import de.laser.utils.LocaleUtils
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient

/**
 * This service handles safe complex object deletion
 */
//@Transactional
@Slf4j
class DeletionService {

    ESWrapperService ESWrapperService
    SubscriptionService subscriptionService
    ContextService contextService

    static boolean DRY_RUN                  = true

    static String RESULT_BLOCKED            = 'RESULT_BLOCKED'
    static String RESULT_SUCCESS            = 'RESULT_SUCCESS'
    static String RESULT_ERROR              = 'RESULT_ERROR'
    static String RESULT_SUBSTITUTE_NEEDED  = 'RESULT_SUBSTITUTE_NEEDED'

    static String RESULT_CUSTOM             = 'RESULT_CUSTOM'

    static String FLAG_WARNING      = 'yellow'
    static String FLAG_SUBSTITUTE   = 'teal'
    static String FLAG_BLOCKER      = 'red'

    /**
     * Deletes the given license; displays eventual attached objects which may cause conflicts
     * @param lic the license to delete
     * @param dryRun should the deletion avoided and only information be fetched?
     * @return a map returning the information about the license
     */
    Map<String, Object> deleteLicense(License lic, boolean dryRun) {

        Map<String, Object> result = [:]

        // gathering references

        List links = Links.where { sourceLicense == lic || destinationLicense == lic }.findAll()

        List ref_instanceOf         = License.findAllByInstanceOf(lic)

        List tasks                  = Task.findAllByLicense(lic)
        List propDefGroupBindings   = PropertyDefinitionGroupBinding.findAllByLic(lic)
        AuditConfig ac              = AuditConfig.getConfig(lic)

        List ids            = new ArrayList(lic.ids)
        List docContexts    = new ArrayList(lic.documents)
        List oRoles         = new ArrayList(lic.orgRelations)
        List pRoles         = new ArrayList(lic.prsLinks)
        //List packages       = new ArrayList(lic.pkgs)  // Package
        //List pendingChanges = new ArrayList(lic.pendingChanges)
        List privateProps   = new ArrayList(lic.propertySet.findAll { LicenseProperty lp -> lp.type.tenant != null })
        List customProps    = new ArrayList(lic.propertySet.findAll { LicenseProperty lp -> lp.type.tenant == null })

        // collecting informations

        result.info = []
        result.info << ['Referenzen: Einrichtungen', ref_instanceOf, FLAG_BLOCKER]

        result.info << ['Links: Verträge bzw. Lizenzen', links]
        result.info << ['Aufgaben', tasks]
        result.info << ['Merkmalsgruppen', propDefGroupBindings]
        //result.info << ['Lizenzen', links.findAll { row -> row.linkType == RDStore.LINKTYPE_LICENSE }]
        result.info << ['Vererbungskonfigurationen', ac ? [ac] : []]

        // lic.onixplLicense

        result.info << ['Identifikatoren', ids]
        result.info << ['Dokumente', docContexts]  // delete ? docContext->doc
        result.info << ['Organisationen', oRoles]
        result.info << ['Personen', pRoles]     // delete ? personRole->person
        //result.info << ['Pakete', packages]
        //result.info << ['Anstehende Änderungen', pendingChanges]
        result.info << ['Private Merkmale', lic.propertySet.findAll { it.type.tenant != null }]
        result.info << ['Allgemeine Merkmale', lic.propertySet.findAll { it.type.tenant == null }]

        // checking constraints and/or processing

        result.deletable = true

        result.info.each { it ->
            if (! it.get(1).isEmpty() && it.size() == 3 && it.get(2) == FLAG_BLOCKER) {
                result.status = RESULT_BLOCKED
                result.deletable = false
            }
        }

        if (dryRun || ! result.deletable) {
            return result
        }
        else if (ref_instanceOf) {
            result.status = RESULT_BLOCKED
            result.referencedBy_instanceOf = ref_instanceOf
        }
        else {
            License.withTransaction { status ->

                try {
                    // ----- remove foreign key references and/or shares
                    // ----- remove foreign key references and/or shares

                    // documents
                    docContexts.each{ tmp ->
                        List changeList = DocContext.findAllBySharedFrom(tmp)
                        changeList.each { tmp2 ->
                            tmp2.sharedFrom = null
                            tmp2.save()
                        }
                    }
                    // org roles
                    oRoles.each{ tmp ->
                        List changeList = OrgRole.findAllBySharedFrom(tmp)
                        changeList.each { tmp2 ->
                            tmp2.sharedFrom = null
                            tmp2.save()
                        }
                    }
                    // custom properties
                    customProps.each{ tmp ->
                        List changeList = LicenseProperty.findAllByInstanceOf(tmp)
                        changeList.each { tmp2 ->
                            tmp2.instanceOf = null
                            tmp2.save()
                        }
                    }
                    /* packages
                    packages.each{ tmp ->
                        tmp.license = null
                        tmp.save()
                    }*/

                    // ----- delete foreign objects
                    // ----- delete foreign objects

                    // lic.onixplLicense

                    // links
                    links.each{ tmp -> tmp.delete() }

                    // tasks
                    tasks.each{ tmp -> tmp.delete() }

                    // property groups and bindings
                    propDefGroupBindings.each{ tmp -> tmp.delete() }

                    // inheritance
                    AuditConfig.removeConfig(lic)

                    // ----- clear collections and delete foreign objects
                    // ----- clear collections and delete foreign objects

                    // identifiers
                    lic.ids.clear()
                    ids.each{ tmp ->
                        Identifier.executeUpdate('delete from Identifier id where id.instanceOf = :tmp',[tmp: tmp])
                        AuditConfig.removeConfig(tmp)
                        tmp.delete()
                    }

                    // documents
                    lic.documents.clear()
                    docContexts.each { tmp -> tmp.delete() }

                    // org roles
                    lic.orgRelations.clear()
                    Set<String> delRelations = []
                    oRoles.each { tmp ->
                        delRelations << tmp.org.globalUID
                        tmp.delete()
                    }

                    // person roles
                    lic.prsLinks.clear()
                    pRoles.each { tmp -> tmp.delete() }

                    // pending changes
                    //lic.pendingChanges.clear()
                    //pendingChanges.each { tmp -> tmp.delete() }

                    // private properties
                    //lic.privateProperties.clear()

                    // custom properties
                    lic.propertySet.clear()
                    /*customProps.each { tmp -> // incomprehensible fix
                        tmp.owner = null
                        tmp.save()
                    }*/
                    customProps.each { tmp -> tmp.delete() }
                    privateProps.each { tmp -> tmp.delete() }

                    lic.delete()
                    DeletedObject.withTransaction {
                        if(lic.isPublicForApi)
                            DeletedObject.construct(lic, delRelations)
                    }
                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    log.error 'error while deleting license ' + lic.id + ' .. rollback: ' + e.message
                    e.printStackTrace()
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                    result.license = lic //needed for redirection
                }
            }
        }

        result
    }

    /**
     * Deletes the given subscription; displays eventual attached objects which may cause conflicts
     * @param sub the subscription to delete
     * @param dryRun should the deletion avoided and only information be fetched?
     * @return a map returning the information about the subscription
     */
    Map<String, Object> deleteSubscription(Subscription sub, boolean dryRun) {

        Map<String, Object> result = [:]

        // gathering references

        List ref_instanceOf = Subscription.findAllByInstanceOf(sub)

        List links = Links.where { sourceSubscription == sub || destinationSubscription == sub }.findAll()

        List tasks                  = Task.findAllBySubscription(sub)
        List propDefGroupBindings   = PropertyDefinitionGroupBinding.findAllBySub(sub)
        AuditConfig ac              = AuditConfig.getConfig(sub)

        List ids            = new ArrayList(sub.ids)
        List docContexts    = new ArrayList(sub.documents)
        List oRoles         = new ArrayList(sub.orgRelations)
        List pRoles         = new ArrayList(sub.prsLinks)
        List subPkgs        = new ArrayList(sub.packages)
        List pendingChanges = new ArrayList(PendingChange.findAllBySubscription(sub))

        List ies            = IssueEntitlement.where { subscription == sub }.findAll()
                            // = new ArrayList(sub.issueEntitlements)

        Org contextOrg = contextService.getOrg()
        List nonDeletedCosts = new ArrayList(sub.costItems.findAll { CostItem ci -> ci.costItemStatus != RDStore.COST_ITEM_DELETED && ci.owner == contextOrg })
        List deletedCosts   = new ArrayList(sub.costItems.findAll { CostItem ci -> ci.costItemStatus == RDStore.COST_ITEM_DELETED || ci.owner != contextOrg })
        List oapl           = new ArrayList(sub.packages?.oapls)
        List privateProps   = new ArrayList(sub.propertySet.findAll { it.type.tenant != null })
        List customProps    = new ArrayList(sub.propertySet.findAll { it.type.tenant == null })

        List surveys        = sub.instanceOf ? SurveyOrg.findAllByOrgAndSurveyConfig(sub.getSubscriber(), SurveyConfig.findAllBySubscription(sub.instanceOf)) : SurveyConfig.findAllBySubscription(sub)

        SurveyInfo surveyInfo
        // collecting informations

        result.info = []

        result.info << ['Referenzen: Einrichtungen', ref_instanceOf, FLAG_BLOCKER]
        result.info << ['Referenzen: Vorgänger/Nachfolger', links.findAll { it.linkType == RDStore.LINKTYPE_FOLLOWS }]

        result.info << ['Links: Lizenzen', links.findAll { it.linkType == RDStore.LINKTYPE_LICENSE }]
        result.info << ['Aufgaben', tasks]
        result.info << ['Merkmalsgruppen', propDefGroupBindings]
        result.info << ['Vererbungskonfigurationen', ac ? [ac] : []]

        result.info << ['Identifikatoren', ids]
        result.info << ['Dokumente', docContexts]   // delete ? docContext->doc
        result.info << ['Organisationen', oRoles]
        result.info << ['Personen', pRoles]       // delete ? personRole->person
        result.info << ['Pakete', subPkgs]
        result.info << ['Anstehende Änderungen', pendingChanges]
        result.info << ['IssueEntitlements', ies]
        //TODO is a temporary solution for ERMS-2535 and is subject of refactoring!
        result.info << ['nicht gelöschte Kosten', nonDeletedCosts, FLAG_BLOCKER]
        result.info << ['gelöschte Kosten', deletedCosts]
        result.info << ['OrgAccessPointLink', oapl]
        result.info << ['Private Merkmale', sub.propertySet.findAll { it.type.tenant != null }]
        result.info << ['Allgemeine Merkmale', sub.propertySet.findAll { it.type.tenant == null }]
        result.info << ['Umfragen', surveys, sub.instanceOf ? FLAG_WARNING : FLAG_BLOCKER]

        // checking constraints and/or processing

        result.deletable = true

        result.info.each { it ->
            if (! it.get(1).isEmpty() && it.size() == 3 && it.get(2) == FLAG_BLOCKER) {
                result.status = RESULT_BLOCKED
                result.deletable = false
            }
        }

        if (dryRun || ! result.deletable) {
            return result
        }
        else if (ref_instanceOf) {
            result.status = RESULT_BLOCKED
            result.referencedBy_instanceOf = ref_instanceOf
        }
        else {
            Subscription.withTransaction { status ->

                try {
                    // ----- remove foreign key references and/or shares
                    // ----- remove foreign key references and/or shares

                    // documents
                    docContexts.each{ tmp ->
                        List changeList = DocContext.findAllBySharedFrom(tmp)
                        changeList.each { tmp2 ->
                            tmp2.sharedFrom = null
                            tmp2.save()
                        }
                    }
                    // org roles
                    oRoles.each{ tmp ->
                        List changeList = OrgRole.findAllBySharedFrom(tmp)
                        changeList.each { tmp2 ->
                            tmp2.sharedFrom = null
                            tmp2.save()
                        }
                    }
                    // custom properties
                    customProps.each{ tmp ->
                        List changeList = SubscriptionProperty.findAllByInstanceOf(tmp)
                        changeList.each { tmp2 ->
                            tmp2.instanceOf = null
                            tmp2.save()
                        }
                    }

                    // ----- delete foreign objects
                    // ----- delete foreign objects

                    // links
                    links.each{ tmp -> tmp.delete() }

                    // tasks
                    tasks.each{ tmp -> tmp.delete() }

                    // property groups and bindings
                    propDefGroupBindings.each{ tmp -> tmp.delete() }

                    // inheritance
                    AuditConfig.removeConfig(sub)

                    // ----- clear collections and delete foreign objects
                    // ----- clear collections and delete foreign objects

                    // identifiers
                    sub.ids.clear()
                    ids.each{ tmp -> tmp.delete() }

                    // documents
                    sub.documents.clear()
                    docContexts.each { tmp -> tmp.delete() }

                    // org roles
                    sub.orgRelations.clear()
                    Set<String> delRelations = []
                    oRoles.each { tmp ->
                        if(tmp.roleType != RDStore.OR_SUBSCRIBER_CONS_HIDDEN)
                            delRelations << tmp.org.globalUID
                        tmp.delete()
                    }

                    // person roles
                    sub.prsLinks.clear()
                    pRoles.each { tmp -> tmp.delete() }

                    // subscription packages
                    sub.packages.clear()
                    subPkgs.each{ tmp ->
                        tmp.oapls?.each{ item ->
                            item.delete()
                        }
                        tmp.delete()
                    }

                    // pending changes
                    PendingChange.executeUpdate('delete from PendingChange pc where pc.subscription = :sub', [sub: sub])

                    // issue entitlements
                    // sub.issueEntitlements.clear()
                    ies.each { tmp ->

                        tmp.coverages?.each{ coverage ->
                            coverage.delete()
                        }
                        tmp.delete()
                    }

                    //cost items
                    sub.costItems.clear()
                    deletedCosts.each { tmp ->
                        tmp.sub = null
                        tmp.save()
                    }

                    // private properties
                    //sub.privateProperties.clear()

                    // custom properties
                    sub.propertySet.clear()
                    /*customProps.each { tmp -> // incomprehensible fix
                        tmp.owner = null
                        tmp.save()
                    }*/
                    customProps.each { tmp -> tmp.delete() }
                    privateProps.each { tmp -> tmp.delete() }

                    // ----- keep foreign object, change state
                    // ----- keep foreign object, change state

                    nonDeletedCosts.each{ tmp ->
                        tmp.costItemStatus = RDStore.COST_ITEM_DELETED
                        tmp.sub = null
                        tmp.subPkg = null
                        tmp.issueEntitlement = null
                        tmp.save()
                    }

                    surveys.each{ tmp ->

                        if(tmp instanceof SurveyConfig){

                            SurveyResult.findAllBySurveyConfig(tmp).each { tmp2 -> tmp2.delete() }

                            SurveyConfigProperties.findAllBySurveyConfig(tmp).each { tmp2 -> tmp2.delete() }

                            SurveyOrg.findAllBySurveyConfig(tmp).each { tmp2 ->

                                CostItem.findAllBySurveyOrg(tmp2).each { tmp3 -> tmp3.delete() }
                                tmp2.delete()
                            }

                            DocContext.findAllBySurveyConfig(tmp).each { tmp2 -> tmp2.delete() }

                            Task.findAllBySurveyConfig(tmp).each { tmp2 -> tmp2.delete() }

                            if(tmp.surveyInfo.surveyConfigs.size() == 1){
                                surveyInfo = tmp.surveyInfo
                            }

                            tmp.delete()
                        }

                        if(tmp instanceof SurveyOrg){

                            CostItem.findAllBySurveyOrg(tmp).each { tmp2 -> tmp2.delete() }

                            SurveyResult.findAllByParticipantAndSurveyConfig(tmp.org, tmp.surveyConfig).each { tmp2 -> tmp2.delete() }

                            tmp.delete()

                        }
                    }

                    if (surveyInfo){
                        surveyInfo.delete()
                    }

                    sub.delete()

                    DeletedObject.withTransaction {
                        if(sub.isPublicForApi)
                            DeletedObject.construct(sub, delRelations)
                    }
                    status.flush()

                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    log.error 'error while deleting subscription ' + sub.id + ' .. rollback: ' + e.message
                    e.printStackTrace()
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                }
            }
        }

        result
    }

    /**
     * Deletes the given organisation; displays eventual attached objects which may cause conflicts
     * @param org the organisation to delete
     * @param replacement unused
     * @param dryRun should the deletion avoided and only information be fetched?
     * @return a map returning the information about the organisation
     */
    Map<String, Object> deleteOrganisation(Org org, Org replacement, boolean dryRun) {

        Map<String, Object> result = [:]

        // gathering references

        List ids            = new ArrayList(org.ids)
        List outgoingCombos = new ArrayList(org.outgoingCombos)
        List incomingCombos = new ArrayList(org.incomingCombos)

        List orgTypes      = new ArrayList(org.orgType)
        List orgLinks      = new ArrayList(org.links)
        List orgSettings   = OrgSetting.findAllWhere(org: org)
        List userSettings  = UserSetting.findAllWhere(orgValue: org)

        List addresses      = new ArrayList(org.addresses)
        List contacts       = new ArrayList(org.contacts)
        List prsLinks       = new ArrayList(org.prsLinks)
        List persons        = Person.findAllByTenant(org)
        List affiliations   = new ArrayList(org.affiliations)
        List docContexts    = new ArrayList(org.documents)
        List platforms      = new ArrayList(org.platforms)
        //List tips           = TitleInstitutionProvider.findAllByInstitution(org)
        //List tipsProviders  = TitleInstitutionProvider.findAllByProvider(org)

        List customProperties       = new ArrayList(org.propertySet.findAll { it.type.tenant == null })
        List privateProperties      = new ArrayList(org.propertySet.findAll { it.type.tenant != null })
        List propertyDefinitions    = PropertyDefinition.findAllByTenant(org)
        List propDefGroups          = PropertyDefinitionGroup.findAllByTenant(org)
        List propDefGroupBindings   = PropertyDefinitionGroupBinding.findAllByOrg(org)

        List budgetCodes        = BudgetCode.findAllByOwner(org)
        List costItems          = CostItem.findAllByOwner(org) //subject of discussion!
        List costItemsECs       = CostItemElementConfiguration.findAllByForOrganisation(org)
        List invoices           = Invoice.findAllByOwner(org)
        List orderings          = Order.findAllByOwner(org)

        List dashboardDueDates  = DashboardDueDate.findAllByResponsibleOrg(org)
        List documents          = Doc.findAllByOwner(org)
        List pendingChanges     = PendingChange.findAllByOwner(org)
        List tasks              = Task.findAllByOrg(org)
        List tasksResp          = Task.findAllByResponsibleOrg(org)
        List systemProfilers    = SystemProfiler.findAllByContext(org)

        List facts              = Fact.findAllByInst(org)
        List readerNumbers      = ReaderNumber.findAllByOrg(org)
        List orgAccessPoints    = OrgAccessPoint.findAllByOrg(org)

        List surveyInfos        = SurveyInfo.findAllByOwner(org)
        List surveyProperties   = PropertyDefinition.getAllByDescrAndTenant(PropertyDefinition.SVY_PROP, org)
        List surveyResults      = SurveyResult.findAllByOwner(org)
        List surveyResultsParts = SurveyResult.findAllByParticipant(org)

        // collecting informations

        result.info = []

        //result.info << ['Links: Orgs', links, FLAG_BLOCKER]

        result.info << ['Identifikatoren', ids]
        result.info << ['Combos (out)', outgoingCombos]
        result.info << ['Combos (in)', incomingCombos, FLAG_BLOCKER]

        result.info << ['Typen', orgTypes]
        result.info << ['OrgRoles', orgLinks, FLAG_BLOCKER]
        result.info << ['Einstellungen', orgSettings]
        result.info << ['Nutzereinstellungen', userSettings, FLAG_BLOCKER]

        result.info << ['Adressen', addresses]
        result.info << ['Kontaktdaten', contacts]
        result.info << ['Personen', prsLinks, FLAG_BLOCKER]
        result.info << ['Personen (tenant)', persons, FLAG_BLOCKER]
        result.info << ['Nutzerzugehörigkeiten', affiliations, FLAG_BLOCKER]
        result.info << ['Dokumente', docContexts, FLAG_BLOCKER]   // delete ? docContext->doc
        result.info << ['Platformen', platforms, FLAG_BLOCKER]
        //result.info << ['TitleInstitutionProvider (inst)', tips, FLAG_BLOCKER]
        //result.info << ['TitleInstitutionProvider (provider)', tipsProviders, FLAG_BLOCKER]
        //result.info << ['TitleInstitutionProvider (provider)', tipsProviders, FLAG_SUBSTITUTE]

        result.info << ['Allgemeine Merkmale', customProperties]
        result.info << ['Private Merkmale', privateProperties]
        result.info << ['Merkmalsdefinitionen', propertyDefinitions, FLAG_BLOCKER]
        result.info << ['Merkmalsgruppen', propDefGroups, FLAG_BLOCKER]
        result.info << ['Merkmalsgruppen (gebunden)', propDefGroupBindings, FLAG_BLOCKER]

        result.info << ['BudgetCodes', budgetCodes, FLAG_BLOCKER]
        result.info << ['Kosten', costItems, FLAG_BLOCKER]
        result.info << ['Kosten-Konfigurationen', costItemsECs, FLAG_BLOCKER]
        result.info << ['Rechnungen', invoices, FLAG_BLOCKER]
        result.info << ['Aufträge', orderings, FLAG_BLOCKER]

        result.info << ['Dokumente (owner)', documents, FLAG_BLOCKER]
        result.info << ['DashboardDueDates (responsibility)', dashboardDueDates, FLAG_BLOCKER]
        result.info << ['Anstehende Änderungen', pendingChanges, FLAG_BLOCKER]
        result.info << ['Aufgaben (owner)', tasks, FLAG_BLOCKER]
        result.info << ['Aufgaben (responsibility)', tasksResp, FLAG_BLOCKER]
        result.info << ['SystemProfilers', systemProfilers]

        result.info << ['Facts', facts, FLAG_BLOCKER]
        result.info << ['ReaderNumbers', readerNumbers, FLAG_BLOCKER]
        result.info << ['OrgAccessPoints', orgAccessPoints, FLAG_BLOCKER]

        result.info << ['SurveyInfos', surveyInfos, FLAG_BLOCKER]
        result.info << ['Umfrage-Merkmale', surveyProperties, FLAG_BLOCKER]
        result.info << ['Umfrageergebnisse (owner)', surveyResults, FLAG_BLOCKER]
        result.info << ['Umfrageergebnisse (participant)', surveyResultsParts, FLAG_BLOCKER]

        // checking constraints and/or processing

        result.deletable = true

        //int count = 0
        //int constraint = 0

        result.info.each { it ->
            //count += it.get(1).size()

            if (it.size() > 2 && ! it.get(1).isEmpty() && it.get(2) == FLAG_SUBSTITUTE) {
                result.status = RESULT_SUBSTITUTE_NEEDED

                //if (it.get(0).equals('TitleInstitutionProvider (provider)')) { // ERMS-1512 workaound for data cleanup
                //    constraint = it.get(1).size()
                //}
            }

            if (! it.get(1).isEmpty() && it.size() == 3 && it.get(2) == FLAG_BLOCKER) {
                result.status = RESULT_BLOCKED
                result.deletable = false
            }
        }

        if (dryRun || ! result.deletable) {
            return result
        }
        else {
            Org.withTransaction { status ->

                try {
                    // TODO delete routine
                    // TODO delete routine
                    // TODO delete routine

                    // identifiers
                    org.ids.clear()
                    ids.each{ tmp -> tmp.delete() }

                    // outgoingCombos
                    org.outgoingCombos.clear()
                    outgoingCombos.each{ tmp -> tmp.delete() }

                    // orgTypes
                    //org.orgType.clear()
                    //orgTypes.each{ tmp -> tmp.delete() }

                    // orgSettings
                    orgSettings.each { tmp -> tmp.delete() }

                    // addresses
                    org.addresses.clear()
                    addresses.each{ tmp -> tmp.delete() }

                    // contacts
                    org.contacts.clear()
                    contacts.each{ tmp -> tmp.delete() }

                    // private properties
                    //org.privateProperties.clear()
                    //privateProperties.each { tmp -> tmp.delete() }

                    // custom properties
                    org.propertySet.clear()
                    customProperties.each { tmp -> // incomprehensible fix // ??
                        tmp.owner = null
                        tmp.save()
                    }
                    customProperties.each { tmp -> tmp.delete() }

                    // systemProfilers
                    systemProfilers.each { tmp -> tmp.delete() }

                    //tipsProviders.each { tmp ->
                    //    tmp.provider = replacement
                    //    tmp.save()
                    //}

                    // TODO delete routine
                    // TODO delete routine
                    // TODO delete routine

                    org.delete()
                    status.flush()

                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    log.error 'error while deleting org ' + org.id + ' .. rollback: ' + e.message
                    e.printStackTrace()
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                }
            }
        }

        result
    }

    /**
     * Deletes the given user; displays eventual attached objects which may cause conflicts
     * @param user the user to delete
     * @param replacement the user which should replace the deleted user and take his data
     * @param dryRun should the deletion avoided and only information be fetched?
     * @return a map returning the information about the user
     */
    Map<String, Object> deleteUser(User user, User replacement, boolean dryRun) {

        Map<String, Object> result = [:]

        // gathering references

        List userOrgs       = new ArrayList(user.affiliations)
        List userRoles      = new ArrayList(user.roles)
        List userSettings   = UserSetting.findAllWhere(user: user)

        List ddds = DashboardDueDate.findAllByResponsibleUser(user)

        List tasks = Task.executeQuery(
                'select x from Task x where x.creator = :user or x.responsibleUser = :user', [user: user])

        // collecting informations

        result.info = []

        result.info << ['Zugehörigkeiten', userOrgs]
        result.info << ['Rollen', userRoles]
        result.info << ['Einstellungen', userSettings]

        result.info << ['DashboardDueDate', ddds]
        result.info << ['Aufgaben', tasks, FLAG_SUBSTITUTE]

        // checking constraints and/or processing

        result.deletable = true

        result.info.each { it ->
            if (it.size() > 2 && ! it.get(1).isEmpty() && it.get(2) == FLAG_SUBSTITUTE) {
                result.status = RESULT_SUBSTITUTE_NEEDED
            }

            if (! it.get(1).isEmpty() && it.size() == 3 && it.get(2) == FLAG_BLOCKER) {
                result.status = RESULT_BLOCKED
                result.deletable = false
            }

            if (user.isLastInstAdmin()) {
                result.status = RESULT_CUSTOM
                result.deletable = false
            }
        }

        if (dryRun || ! result.deletable) {
            return result
        }
        else {
            User.withTransaction { status ->

                try {
                    // user orgs
                    user.affiliations.clear()
                    userOrgs.each { tmp -> tmp.delete() }

                    // user roles
                    user.roles.clear()
                    userRoles.each { tmp -> tmp.delete() }

                    // user settings
                    userSettings.each { tmp -> tmp.delete() }

                    ddds.each { tmp -> tmp.delete() }

                    tasks.each { tmp ->
                        if (tmp.creator.id == user.id) {
                            tmp.creator = replacement
                        }
                        if (tmp.responsibleUser?.id == user.id) {
                            tmp.responsibleUser = replacement
                        }
                        tmp.save()
                    }

                    user.delete()
                    status.flush()

                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    log.error 'error while deleting user ' + user.id + ' .. rollback: ' + e.message
                    e.printStackTrace()
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                }
            }
        }

        result
    }

    /**
     * Deletes the given package AND its attached objects; is a cleanup function for false package entries. Use this function
     * thus with VERY MUCH CARE!
     * @param pkg the package to delete
     * @return true if the deletion was successful, false otherwise
     */
    boolean deletePackage(Package pkg) {
        log.debug "processing package #${pkg.id}"
        Package.withTransaction { status ->
            try {
                //to be absolutely sure ...
                List<Subscription> subsConcerned = Subscription.executeQuery("select ie.subscription from IssueEntitlement ie join ie.tipp tipp where tipp.pkg = :pkg and tipp.pkg.name != '' and ie.status != :removed",[pkg:pkg,removed: RDStore.TIPP_STATUS_REMOVED])
                if(subsConcerned) {
                    log.info "issue entitlements detected on package to be deleted: ${subsConcerned} .. rollback"
                    status.setRollbackOnly()
                    return false
                }
                else {
                    //deleting IECoverages and IssueEntitlements marked deleted or where package data has disappeared
                    List<Long> iesConcerned = IssueEntitlement.executeQuery("select ie.id from IssueEntitlement ie join ie.tipp tipp where tipp.pkg = :pkg and (ie.status = :removed or tipp.pkg.name = '')",[pkg:pkg,deleted: RDStore.TIPP_STATUS_REMOVED])
                    if(iesConcerned) {
                        IssueEntitlementCoverage.executeUpdate("delete from IssueEntitlementCoverage ic where ic.issueEntitlement.id in :toDelete",[toDelete:iesConcerned])
                        PriceItem.executeUpdate("delete from PriceItem pc where pc.issueEntitlement.id in :toDelete",[toDelete:iesConcerned])
                        CostItem.executeUpdate("delete from CostItem ci where ci.issueEntitlement.id in :toDelete",[toDelete:iesConcerned])
                        IssueEntitlement.executeUpdate("delete from IssueEntitlement ie where ie.id in :toDelete",[toDelete:iesConcerned])
                    }
                    //deleting tipps
                    List<Long> tippsConcerned = TitleInstancePackagePlatform.findAllByPkg(pkg).collect { tipp -> tipp.id }
                    TIPPCoverage.executeUpdate("delete from TIPPCoverage tc where tc.tipp.id in :toDelete",[toDelete:tippsConcerned])
                    Identifier.executeUpdate("delete from Identifier id where id.tipp.id in :toDelete",[toDelete:tippsConcerned])
                    TitleInstancePackagePlatform.executeUpdate("delete from TitleInstancePackagePlatform tipp where tipp.id in :toDelete",[toDelete:tippsConcerned])
                    //deleting pending changes
                    PendingChange.findAllByPkg(pkg).each { tmp -> tmp.delete() }
                    //deleting orgRoles
                    OrgRole.findAllByPkg(pkg).each { tmp -> tmp.delete() }
                    //deleting (empty) subscription packages
                    SubscriptionPackage.findAllByPkg(pkg).each { tmp ->
                        CostItem.executeUpdate("delete from CostItem ci where ci.subPkg = :sp",[sp:tmp])
                        tmp.delete()
                    }
                    //deleting empty-running trackers
                    //GlobalRecordTracker.findAllByLocalOid(pkg.class.name+':'+pkg.id).each { tmp -> tmp.delete() }
                    pkg.delete()
                    status.flush()
                    return true
                }
            }
            catch(Exception e) {
                log.error 'error while deleting package ' + pkg.id + ' .. rollback: ' + e.getMessage()
                e.printStackTrace()
                status.setRollbackOnly()
                return false
            }
        }
    }

    /**
     * Deletes the given title duplicate after having rebased the depending issue entitlements to the replacement title
     * @param tipp the title duplicate to delete
     * @param replacement the replacement title record
     * @return true if the deletion was successful, false otherwise
     */
    boolean deleteTIPP(TitleInstancePackagePlatform tipp, TitleInstancePackagePlatform replacement) {
        log.debug "processing tipp #${tipp.id}"
        //rebasing subscriptions
        if(subscriptionService.rebaseSubscriptions(tipp,replacement)) {
            TitleInstancePackagePlatform.withTransaction { status ->
                try {
                    //deleting old TIPPs
                    tipp.delete()
                    status.flush()
                    return true
                }
                catch(Exception e) {
                    log.error 'error while deleting tipp ' + tipp.id + ' .. rollback: ' + e.getMessage()
                    e.printStackTrace()
                    status.setRollbackOnly()
                    return false
                }
            }
        }
        else {
            log.info 'error while rebasing subscriptions for tipp '+tipp.id+' ... rollback'
            return false
        }
    }

    /**
     * Use this method with VERY MUCH CARE!
     * Deletes a {@link Collection} of {@link TitleInstancePackagePlatform} objects WITH their depending objects ({@link TIPPCoverage} and {@link Identifier})
     * @param tipp the {@link Collection} of {@link TitleInstancePackagePlatform} to delete
     * @return the success flag
     */
    boolean deleteTIPPsCascaded(Collection<TitleInstancePackagePlatform> tippsToDelete) {
        log.debug "processing tipps (${tippsToDelete.collect { TitleInstancePackagePlatform tipp -> tipp.id}})"
        Map<String,Collection<TitleInstancePackagePlatform>> toDelete = [toDelete:tippsToDelete]
        Map<String,Collection<IssueEntitlement>> delIssueEntitlements = [toDelete:IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp in (:toDelete) and (ie.status = :removed or ie.status = null)', [toDelete: tippsToDelete, removed: RDStore.TIPP_STATUS_REMOVED])]
        TitleInstancePackagePlatform.withTransaction { status ->
            try {
                log.info("${PendingChange.executeUpdate('delete from PendingChange pc where pc.tippCoverage in (select tc from TIPPCoverage tc where tc.tipp in (:toDelete))',toDelete)} coverage pending changes deleted")
                log.info("${PendingChange.executeUpdate('delete from PendingChange pc where pc.priceItem in (select pi from PriceItem pi where pi.tipp in (:toDelete))',toDelete)} price item pending changes deleted")
                log.info("${TIPPCoverage.executeUpdate('delete from TIPPCoverage tc where tc.tipp in (:toDelete)',toDelete)} coverages deleted")
                log.info("${Identifier.executeUpdate('delete from Identifier i where i.tipp in (:toDelete)',toDelete)} identifiers deleted")
                log.info("${PriceItem.executeUpdate('delete from PriceItem pi where pi.tipp in (:toDelete)', toDelete)} price items deleted")
                log.info("${OrgRole.executeUpdate('delete from OrgRole oo where oo.tipp in (:toDelete)', toDelete)} org roles deleted")
                log.info("${TitleHistoryEventParticipant.executeUpdate('delete from TitleHistoryEventParticipant thep where thep.participant in (:toDelete)', toDelete)} title history event participants deleted")
                log.info("${TitleHistoryEvent.executeUpdate('delete from TitleHistoryEvent the where the.tipp in (:toDelete)', toDelete)} title history events deleted")
                log.info("${Fact.executeUpdate('delete from Fact f where f.relatedTitle in (:toDelete)', toDelete)} facts deleted")
                log.info("${PendingChange.executeUpdate('delete from PendingChange pc where pc.tipp in (:toDelete)', toDelete)} pending changes deleted")
                log.info("${Language.executeUpdate('delete from Language l where l.tipp in (:toDelete)', toDelete)} language entries deleted")
                log.info("${DeweyDecimalClassification.executeUpdate('delete from DeweyDecimalClassification ddc where ddc.tipp in (:toDelete)', toDelete)} DDC entrie deleted")
                if(delIssueEntitlements.toDelete.size() > 0) {
                    log.info("${IssueEntitlementCoverage.executeUpdate('delete from IssueEntitlementCoverage ic where ic.issueEntitlement in (:toDelete)',delIssueEntitlements)} issue entitlement coverages deleted")
                    log.info("${CostItem.executeUpdate('update CostItem ci set ci.issueEntitlement = null where ci.issueEntitlement in (:toDelete)', delIssueEntitlements)} issue entitlement costs nullified")
                    log.info("${IssueEntitlementGroupItem.executeUpdate('delete from IssueEntitlementGroupItem iegi where iegi.ie in (:toDelete)', delIssueEntitlements)} issue entitlement group items deleted")
                    log.info("${PriceItem.executeUpdate('delete from PriceItem pi where pi.issueEntitlement in (:toDelete)', delIssueEntitlements)} issue entitlement price items deleted")
                    log.info("${IssueEntitlement.executeUpdate('delete from IssueEntitlement ie where ie in (:toDelete)', delIssueEntitlements)} deleted issue entitlements cleared")
                }
                log.info("${TitleInstancePackagePlatform.executeUpdate('delete from TitleInstancePackagePlatform tipp where tipp in (:toDelete)',toDelete)} tipps cleared")
                return true
            }
            catch (Exception e) {
                log.error 'error while deleting tipp collection ' + tippsToDelete + ' .. rollback: ' + e.message
                e.printStackTrace()
                status.setRollbackOnly()
                return false
            }
        }
    }

    /**
     * Deletes the given issue entitlement with depending objects
     * @param ie the issue entitlement to be deleted
     * @return true if the deletion was successful, false otherwise
     */
    boolean deleteIssueEntitlement(IssueEntitlement ie) {
        log.debug "processing issue entitlement ${ie}"
        IssueEntitlement.withTransaction { status ->
            try {
                Map<String,IssueEntitlement> toDelete = [ie:ie]
                PriceItem.executeUpdate('delete from PriceItem pi where pi.issueEntitlement = :ie',toDelete)
                IssueEntitlementCoverage.executeUpdate('delete from IssueEntitlementCoverage ic where ic.issueEntitlement = :ie',toDelete)
                return true
            }
            catch (Exception e) {
                log.error 'error while deleting issue entitlement ' + ie + ' .. rollback: ' + e.message
                e.printStackTrace()
                status.setRollbackOnly()
                return false
            }
        }
    }

    /**
     * Deletes the given cost item and unsets eventual links. If it is the last item in a cost item group,
     * the group will be deleted as well for that it will not appear in dropdowns any more
     * @param params the parameter map containing the cost item id to delete and the tab which should be displayed after deletion
     * @return result status map: OK if succeeded, error otherwise
     */
    boolean deleteCostItem(CostItem ci) {
        if (ci) {
            Order order = ci.order
            Invoice invoice = ci.invoice
            Set<String> accessibleOrgs = [ci.owner.globalUID]
            if(ci.sub && ci.isVisibleForSubscriber) {
                accessibleOrgs << ci.sub.getSubscriber().globalUID
            }
            ci.order = null
            ci.invoice = null
            CostItem.findAllByCopyBase(ci).each { CostItem tmp ->
                tmp.copyBase = null
                tmp.save()
            }
            if (!CostItem.findByOrderAndIdNotEqual(order, ci.id))
                order.delete()
            if (!CostItem.findByInvoiceAndIdNotEqual(invoice, ci.id))
                invoice.delete()
            PendingChange.executeUpdate('delete from PendingChange pc where pc.costItem = :ci', [ci: ci])
            List<CostItemGroup> cigs = CostItemGroup.findAllByCostItem(ci)
            cigs.each { CostItemGroup tmp ->
                tmp.delete()
            }
            ci.delete()
            DeletedObject.withTransaction {
                DeletedObject.construct(ci, accessibleOrgs)
            }
            true
        }
        else false
    }

    /**
     * Removes the given ElasticSearch entry (document) from the index
     * @param id the id of the entry to remove
     * @param className the domain class index from which the entry should be removed
     */
    void deleteDocumentFromIndex(id, String className) {
        RestHighLevelClient esclient = ESWrapperService.getNewClient(true)
        Map es_indices = ESWrapperService.ES_Indices
        String es_index = es_indices.get(className)

        if (esclient) {
            try {
                DeleteRequest request = new DeleteRequest(es_index, id)
                esclient.delete(request, RequestOptions.DEFAULT)
            }
            catch (Exception e) {
                log.error("deleteDocumentFromIndex with id=${id} failed because: ")
                e.printStackTrace()
            }
            finally {
                esclient.close()
            }
        }
    }
}
