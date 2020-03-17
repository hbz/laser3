package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.User
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.domain.SystemProfiler
import de.laser.helper.RDConstants
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient

//@CompileStatic
class DeletionService {

    GrailsApplication grailsApplication
    def ESWrapperService

    static boolean DRY_RUN                  = true

    static String RESULT_BLOCKED            = 'RESULT_BLOCKED'
    static String RESULT_SUCCESS            = 'RESULT_SUCCESS'
    static String RESULT_ERROR              = 'RESULT_ERROR'
    static String RESULT_SUBSTITUTE_NEEDED  = 'RESULT_SUBSTITUTE_NEEDED'

    static String FLAG_BLOCKER      = 'red'
    static String FLAG_WARNING      = 'yellow'
    static String FLAG_SUBSTITUTE   = 'blue'

    static Map<String, Object> deleteLicense(License lic, boolean dryRun) {

        Map<String, Object> result = [:]

        // gathering references

        List links = Links.where { objectType == lic.class.name && (source == lic.id || destination == lic.id) }.findAll()

        List ref_instanceOf         = License.findAllByInstanceOf(lic)

        List tasks                  = Task.findAllByLicense(lic)
        List propDefGroupBindings   = PropertyDefinitionGroupBinding.findAllByLic(lic)
        List subs                   = Subscription.findAllByOwner(lic)
        AuditConfig ac              = AuditConfig.getConfig(lic)

        List ids            = new ArrayList(lic.ids)
        List docContexts    = new ArrayList(lic.documents)
        List oRoles         = new ArrayList(lic.orgLinks)
        List pRoles         = new ArrayList(lic.prsLinks)
        List packages       = new ArrayList(lic.pkgs)  // Package
        List pendingChanges = new ArrayList(lic.pendingChanges)
        List privateProps   = new ArrayList(lic.privateProperties)
        List customProps    = new ArrayList(lic.customProperties)

        // collecting informations

        result.info = []
        result.info << ['Referenzen: Teilnehmer', ref_instanceOf, FLAG_BLOCKER]

        result.info << ['Links: Verträge', links]
        result.info << ['Aufgaben', tasks]
        result.info << ['Merkmalsgruppen', propDefGroupBindings]
        result.info << ['Lizenzen', subs]
        result.info << ['Vererbungskonfigurationen', ac ? [ac] : []]

        // lic.onixplLicense

        result.info << ['Identifikatoren', ids]
        result.info << ['Dokumente', docContexts]  // delete ? docContext->doc
        result.info << ['Organisationen', oRoles]
        result.info << ['Personen', pRoles]     // delete ? personRole->person
        result.info << ['Pakete', packages]
        result.info << ['Anstehende Änderungen', pendingChanges]
        result.info << ['Private Merkmale', lic.privateProperties]
        result.info << ['Allgemeine Merkmale', lic.customProperties]

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
                            tmp2.save(flush:true)
                        }
                    }
                    // org roles
                    oRoles.each{ tmp ->
                        List changeList = OrgRole.findAllBySharedFrom(tmp)
                        changeList.each { tmp2 ->
                            tmp2.sharedFrom = null
                            tmp2.save(flush:true)
                        }
                    }
                    // custom properties
                    customProps.each{ tmp ->
                        List changeList = LicenseCustomProperty.findAllByInstanceOf(tmp)
                        changeList.each { tmp2 ->
                            tmp2.instanceOf = null
                            tmp2.save(flush:true)
                        }
                    }
                    // packages
                    packages.each{ tmp ->
                        tmp.license = null
                        tmp.save(flush:true)
                    }
                    // subscription
                    subs.each{ tmp ->
                        tmp.owner = null
                        tmp.save(flush:true)
                    }

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
                    ids.each{ tmp -> tmp.delete() }

                    // documents
                    lic.documents.clear()
                    docContexts.each { tmp -> tmp.delete() }

                    // org roles
                    lic.orgLinks.clear()
                    oRoles.each { tmp -> tmp.delete() }

                    // person roles
                    lic.prsLinks.clear()
                    pRoles.each { tmp -> tmp.delete() }

                    // pending changes
                    lic.pendingChanges.clear()
                    pendingChanges.each { tmp -> tmp.delete() }

                    // private properties
                    lic.privateProperties.clear()
                    privateProps.each { tmp -> tmp.delete() }

                    // custom properties
                    lic.customProperties.clear()
                    /*customProps.each { tmp -> // incomprehensible fix
                        tmp.owner = null
                        tmp.save()
                    }*/
                    customProps.each { tmp -> tmp.delete() }

                    lic.delete()

                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    println 'error while deleting license ' + lic.id + ' .. rollback'
                    println e.message
                    e.printStackTrace()
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                    result.license = lic //needed for redirection
                }
            }
        }

        result
    }

    static Map<String, Object> deleteSubscription(Subscription sub, boolean dryRun) {

        Map<String, Object> result = [:]

        // gathering references

        List ref_instanceOf = Subscription.findAllByInstanceOf(sub)
        List ref_previousSubscription = Subscription.findAllByPreviousSubscription(sub)

        List links = Links.where { objectType == sub.class.name && (source == sub.id || destination == sub.id) }.findAll()

        List tasks                  = Task.findAllBySubscription(sub)
        List propDefGroupBindings   = PropertyDefinitionGroupBinding.findAllBySub(sub)
        AuditConfig ac              = AuditConfig.getConfig(sub)

        List ids            = new ArrayList(sub.ids)
        List docContexts    = new ArrayList(sub.documents)
        List oRoles         = new ArrayList(sub.orgRelations)
        List pRoles         = new ArrayList(sub.prsLinks)
        List subPkgs        = new ArrayList(sub.packages)
        List pendingChanges = new ArrayList(sub.pendingChanges)

        List ies            = IssueEntitlement.where { subscription == sub }.findAll()
                            // = new ArrayList(sub.issueEntitlements)

        List costs          = new ArrayList(sub.costItems)
        List oapl           = new ArrayList(sub.packages?.oapls)
        List privateProps   = new ArrayList(sub.privateProperties)
        List customProps    = new ArrayList(sub.customProperties)
        List surveys        = SurveyConfig.findAllBySubscription(sub)

        // collecting informations

        result.info = []

        result.info << ['Referenzen: Teilnehmer', ref_instanceOf, FLAG_BLOCKER]
        result.info << ['Referenzen: Nachfolger', ref_previousSubscription]

        result.info << ['Links: Lizenzen', links]
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
        result.info << ['Kostenposten', costs, FLAG_BLOCKER]
        result.info << ['OrgAccessPointLink', oapl]
        result.info << ['Private Merkmale', sub.privateProperties]
        result.info << ['Allgemeine Merkmale', sub.customProperties]
        result.info << ['Umfragen', surveys, FLAG_WARNING]

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
                        List changeList = SubscriptionCustomProperty.findAllByInstanceOf(tmp)
                        changeList.each { tmp2 ->
                            tmp2.instanceOf = null
                            tmp2.save()
                        }
                    }

                    ref_previousSubscription.each{ tmp ->
                        tmp.previousSubscription = null
                        tmp.save(flush: true)
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
                    oRoles.each { tmp -> tmp.delete() }

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
                    sub.pendingChanges.clear()
                    pendingChanges.each { tmp -> tmp.delete() }

                    // issue entitlements
                    // sub.issueEntitlements.clear()
                    ies.each { tmp ->

                        tmp.coverages?.each{ coverage ->
                            coverage.delete()
                        }
                        tmp.delete()
                    }

                    // private properties
                    sub.privateProperties.clear()
                    privateProps.each { tmp -> tmp.delete() }

                    // custom properties
                    sub.customProperties.clear()
                    customProps.each { tmp -> // incomprehensible fix
                        tmp.owner = null
                        tmp.save()
                    }
                    customProps.each { tmp -> tmp.delete() }

                    // ----- keep foreign object, change state
                    // ----- keep foreign object, change state

                    costs.each{ tmp ->
                        tmp.costItemStatus = RefdataValue.getByValueAndCategory('Deleted', RDConstants.COST_ITEM_STATUS)
                        tmp.sub = null
                        tmp.subPkg = null
                        tmp.issueEntitlement = null
                        tmp.save()
                    }

                    sub.delete()
                    status.flush()

                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    println 'error while deleting subscription ' + sub.id + ' .. rollback'
                    println e.message
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                }
            }
        }

        result
    }

    static Map<String, Object> deleteOrganisation(Org org, Org replacement, boolean dryRun) {

        Map<String, Object> result = [:]

        // gathering references

        List links = Links.where { objectType == org.class.name && (source == org.id || destination == org.id) }.findAll()

        List ids            = new ArrayList(org.ids)
        List outgoingCombos = new ArrayList(org.outgoingCombos)
        List incomingCombos = new ArrayList(org.incomingCombos)

        List orgTypes      = new ArrayList(org.orgType)
        List orgLinks      = new ArrayList(org.links)
        List orgSettings   = OrgSettings.findAllWhere(org: org)
        List userSettings  = UserSettings.findAllWhere(orgValue: org)

        List addresses      = new ArrayList(org.addresses)
        List contacts       = new ArrayList(org.contacts)
        List prsLinks       = new ArrayList(org.prsLinks)
        List persons        = Person.findAllByTenant(org)
        List affiliations   = new ArrayList(org.affiliations)
        List docContexts    = new ArrayList(org.documents)
        List platforms      = new ArrayList(org.platforms)
        List tips           = TitleInstitutionProvider.findAllByInstitution(org)
        List tipsProviders  = TitleInstitutionProvider.findAllByProvider(org)

        List customProperties       = new ArrayList(org.customProperties)
        List privateProperties      = new ArrayList(org.privateProperties)
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
        List systemMessages     = SystemMessage.findAllByOrg(org)
        List systemProfilers    = SystemProfiler.findAllByContext(org)

        List facts              = Fact.findAllByInst(org)
        List readerNumbers      = ReaderNumber.findAllByOrg(org)
        List orgAccessPoints    = OrgAccessPoint.findAllByOrg(org)
        List orgTitleStats      = OrgTitleStats.findAllByOrg(org)

        List surveyInfos        = SurveyInfo.findAllByOwner(org)
        List surveyProperties   = SurveyProperty.findAllByOwner(org)
        List surveyResults      = SurveyResult.findAllByOwner(org)
        List surveyResultsParts = SurveyResult.findAllByParticipant(org)

        // collecting informations

        result.info = []

        result.info << ['Links: Orgs', links, FLAG_BLOCKER]

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
        result.info << ['TitleInstitutionProvider (inst)', tips, FLAG_BLOCKER]
        result.info << ['TitleInstitutionProvider (provider)', tipsProviders, FLAG_BLOCKER]
        //result.info << ['TitleInstitutionProvider (provider)', tipsProviders, FLAG_SUBSTITUTE]

        result.info << ['Allgemeine Merkmale', customProperties]
        result.info << ['Private Merkmale', privateProperties]
        result.info << ['Merkmalsdefinitionen', propertyDefinitions, FLAG_BLOCKER]
        result.info << ['Merkmalsgruppen', propDefGroups, FLAG_BLOCKER]
        result.info << ['Merkmalsgruppen (gebunden)', propDefGroupBindings, FLAG_BLOCKER]

        result.info << ['BudgetCodes', budgetCodes, FLAG_BLOCKER]
        result.info << ['Kostenposten', costItems, FLAG_BLOCKER]
        result.info << ['Kostenposten-Konfigurationen', costItemsECs, FLAG_BLOCKER]
        result.info << ['Rechnungen', invoices, FLAG_BLOCKER]
        result.info << ['Aufträge', orderings, FLAG_BLOCKER]

        result.info << ['Dokumente (owner)', documents, FLAG_BLOCKER]
        result.info << ['DashboardDueDates (responsibility)', dashboardDueDates, FLAG_BLOCKER]
        result.info << ['Anstehende Änderungen', pendingChanges, FLAG_BLOCKER]
        result.info << ['Aufgaben (owner)', tasks, FLAG_BLOCKER]
        result.info << ['Aufgaben (responsibility)', tasksResp, FLAG_BLOCKER]
        result.info << ['SystemMessages', systemMessages, FLAG_BLOCKER]
        result.info << ['SystemProfilers', systemProfilers, FLAG_BLOCKER]

        result.info << ['Facts', facts, FLAG_BLOCKER]
        result.info << ['ReaderNumbers', readerNumbers, FLAG_BLOCKER]
        result.info << ['OrgAccessPoints', orgAccessPoints, FLAG_BLOCKER]
        result.info << ['OrgTitleStats', orgTitleStats, FLAG_BLOCKER]

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
                    org.privateProperties.clear()
                    privateProperties.each { tmp -> tmp.delete() }

                    // custom properties
                    org.customProperties.clear()
                    customProperties.each { tmp -> // incomprehensible fix // ??
                        tmp.owner = null
                        tmp.save()
                    }
                    customProperties.each { tmp -> tmp.delete() }


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
                    println 'error while deleting org ' + org.id + ' .. rollback'
                    println e.message
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                }
            }
        }

        result
    }

    static Map<String, Object> deleteUser(User user, User replacement, boolean dryRun) {

        Map<String, Object> result = [:]

        // gathering references

        List userOrgs       = new ArrayList(user.affiliations)
        List userRoles      = new ArrayList(user.roles)
        List userFolder     = UserFolder.findAllWhere(user: user)
        List userSettings   = UserSettings.findAllWhere(user: user)

        List ciecs = CostItemElementConfiguration.executeQuery(
                'select x from CostItemElementConfiguration x where x.createdBy = :user or x.lastUpdatedBy = :user', [user: user])

        List ddds = DashboardDueDate.findAllByResponsibleUser(user)

        List docs = Doc.executeQuery(
                'select x from Doc x where x.creator = :user or x.user = :user', [user: user])
        List links = Links.executeQuery(
                'select x from Links x where x.createdBy = :user or x.lastUpdatedBy = :user', [user: user])

        List pendingChanges = PendingChange.findAllByUser(user)

        List surveyResults = SurveyResult.findAllByUser(user)

        List systemTickets = SystemTicket.findAllByAuthor(user)

        List tasks = Task.executeQuery(
                'select x from Task x where x.creator = :user or x.responsibleUser = :user', [user: user])

        // collecting informations

        result.info = []

        result.info << ['Zugehörigkeiten', userOrgs]
        result.info << ['Rollen', userRoles]
        result.info << ['Folder', userFolder]
        result.info << ['Einstellungen', userSettings]

        result.info << ['Kostenkonfigurationen', ciecs, FLAG_SUBSTITUTE]
        result.info << ['DashboardDueDate', ddds]
        result.info << ['Dokumente', docs, FLAG_SUBSTITUTE]
        result.info << ['Links', links, FLAG_SUBSTITUTE]
        result.info << ['Anstehende Änderungen', pendingChanges, FLAG_SUBSTITUTE]
        result.info << ['Umfrageergebnisse', surveyResults, FLAG_SUBSTITUTE]
        result.info << ['Tickets', systemTickets, FLAG_SUBSTITUTE]
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

                    // user folder
                    userFolder.each { tmp -> tmp.delete() }

                    // user settings
                    userSettings.each { tmp -> tmp.delete() }

                    // cost item element configurations
                    ciecs.each { tmp ->
                        tmp.lastUpdatedBy = replacement
                        tmp.createdBy = replacement
                        tmp.save()
                    }

                    ddds.each { tmp -> tmp.delete() }

                    // docs
                    docs.each { tmp ->
                        if (tmp.creator?.id == user.id) {
                            tmp.creator = replacement
                        }
                        if (tmp.user?.id == user.id) {
                            tmp.user = replacement
                        }
                        tmp.save()
                    }

                    links.each { tmp ->
                        tmp.lastUpdatedBy = replacement
                        tmp.createdBy = replacement
                        tmp.save()
                    }

                    pendingChanges.each { tmp ->
                        tmp.user = replacement
                        tmp.save()
                    }

                    surveyResults.each { tmp ->
                        tmp.user = replacement
                        tmp.save()
                    }

                    systemTickets.each { tmp ->
                        tmp.author = replacement
                        tmp.save()
                    }

                    tasks.each { tmp ->
                        tmp.responsibleUser = replacement
                        tmp.creator = replacement
                        tmp.save()
                    }

                    user.delete()
                    status.flush()

                    result.status = RESULT_SUCCESS
                }
                catch (Exception e) {
                    println 'error while deleting user ' + user.id + ' .. rollback'
                    println e.message
                    status.setRollbackOnly()
                    result.status = RESULT_ERROR
                }
            }
        }

        result
    }

    static boolean deletePackage(Package pkg) {
        println "processing package #${pkg.id}"
        Package.withTransaction { status ->
            try {
                //to be absolutely sure ...
                List<Subscription> subsConcerned = Subscription.executeQuery('select ie.subscription from IssueEntitlement ie join ie.tipp tipp where tipp.pkg = :pkg',[pkg:pkg])
                if(subsConcerned) {
                    println 'issue entitlements detected on package to be deleted .. rollback'
                    status.setRollbackOnly()
                    return false
                }
                //deleting tipps
                TitleInstancePackagePlatform.findAllByPkg(pkg).each { tmp -> tmp.delete() }
                //deleting pending changes
                PendingChange.findAllByPkg(pkg).each { tmp -> tmp.delete() }
                //deleting orgRoles
                OrgRole.findAllByPkg(pkg).each { tmp -> tmp.delete() }
                //deleting (empty) subscription packages
                SubscriptionPackage.findAllByPkg(pkg).each { tmp -> tmp.delete() }
                //deleting empty-running trackers
                GlobalRecordTracker.findByLocalOid(pkg.class.name+':'+pkg.id).delete()
                pkg.delete()
                status.flush()
                return true
            }
            catch(Exception e) {
                println 'error while deleting package ' + pkg.id + ' .. rollback'
                println e.message
                status.setRollbackOnly()
                return false
            }
        }
    }

    boolean deleteTIPP(TitleInstancePackagePlatform tipp, TitleInstancePackagePlatform replacement) {
        println "processing tipp #${tipp.id}"
        TitleInstancePackagePlatform.withTransaction { status ->
            try {
                //rebasing subscriptions
                IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.tipp = :replacement where ie.tipp = :old',[old:tipp,replacement:replacement])
                //deleting old TIPPs
                tipp.delete()
                status.flush()
                return true
            }
            catch(Exception e) {
                println 'error while deleting tipp ' + tipp.id + ' .. rollback'
                println e.message
                status.setRollbackOnly()
                return false
            }
        }
    }

    def deleteDocumentFromIndex(id)
    {
        def es_index = ESWrapperService.getESSettings().indexName
        RestHighLevelClient esclient = ESWrapperService.getClient()

        DeleteRequest request = new DeleteRequest(es_index, id)
        DeleteResponse deleteResponse = esclient.delete(request, RequestOptions.DEFAULT);
        esclient.close()
    }
}
