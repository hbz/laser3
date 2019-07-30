package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.domain.SystemProfiler
import org.codehaus.groovy.grails.commons.GrailsApplication

//@CompileStatic
class DeletionService {

    GrailsApplication grailsApplication

    static boolean DRY_RUN          = true
    static String RESULT_SUCCESS    = 'RESULT_SUCCESS'
    static String RESULT_QUIT       = 'RESULT_QUIT'
    static String RESULT_ERROR      = 'RESULT_ERROR'

    static Map<String, Object> deleteLicense(License lic, boolean dryRun) {

        Map<String, Object> result = [:]

        List ref_instanceOf         = License.findAllByInstanceOf(lic)

        List links                  = Links.where { objectType == lic.class.name &&
                                        (source == lic.id || destination == lic.id) }.findAll()

        List tasks                  = Task.findAllByLicense(lic)
        List propDefGroupBindings   = PropertyDefinitionGroupBinding.findAllByLic(lic)
        List subs                   = Subscription.findAllByOwner(lic)
        AuditConfig ac              = AuditConfig.getConfig(lic)

        List ios            = new ArrayList(lic.ids)
        List docContexts    = new ArrayList(lic.documents)
        List oRoles         = new ArrayList(lic.orgLinks)
        List pRoles         = new ArrayList(lic.prsLinks)
        List packages       = new ArrayList(lic.pkgs)  // Package
        List pendingChanges = new ArrayList(lic.pendingChanges)
        List privateProps   = new ArrayList(lic.privateProperties)
        List customProps    = new ArrayList(lic.customProperties)

        if (dryRun) {
            result.info = []
            result.info << ['Referenzen: Teilnehmer', ref_instanceOf, 'red']

            result.info << ['Links: Verträge', links]
            result.info << ['Aufgaben', tasks]
            result.info << ['Merkmalsgruppen', propDefGroupBindings]
            result.info << ['Lizenzen', subs]
            result.info << ['Vererbungskonfigurationen', ac ? [ac] : []]

            // lic.onixplLicense

            result.info << ['Identifikatoren', ios]
            result.info << ['Dokumente', docContexts]  // delete ? docContext->doc
            result.info << ['Organisationen', oRoles]
            result.info << ['Personen', pRoles]     // delete ? personRole->person
            result.info << ['Pakete', packages]
            result.info << ['Anstehende Änderungen', pendingChanges]
            result.info << ['Private Merkmale', lic.privateProperties]
            result.info << ['Allgemeine Merkmale', lic.customProperties]
        }
        else if (ref_instanceOf) {

            result = [status: RESULT_QUIT, referencedBy_instanceOf: ref_instanceOf]
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
                        List changeList = LicenseCustomProperty.findAllByInstanceOf(tmp)
                        changeList.each { tmp2 ->
                            tmp2.instanceOf = null
                            tmp2.save()
                        }
                    }
                    // packages
                    packages.each{ tmp ->
                        tmp.license = null
                        tmp.save()
                    }
                    // subscription
                    subs.each{ tmp ->
                        tmp.owner = null
                        tmp.save()
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
                    ios.each{ tmp -> tmp.delete() }

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
                    customProps.each { tmp -> // incomprehensible fix
                        tmp.owner = null
                        tmp.save()
                    }
                    customProps.each { tmp -> tmp.delete() }

                    lic.delete()
                    status.flush()

                    result = [status: RESULT_SUCCESS]
                }
                catch (Exception e) {
                    println 'error while deleting license ' + lic.id + ' .. rollback'
                    println e.message
                    status.setRollbackOnly()
                    result = [status: RESULT_ERROR]
                }
            }
        }

        result
    }

    static Map<String, Object> deleteSubscription(Subscription sub, boolean dryRun) {

        Map<String, Object> result = [:]

        List ref_instanceOf = Subscription.findAllByInstanceOf(sub)
        List ref_previousSubscription = Subscription.findAllByPreviousSubscription(sub)

        List links                  = Links.where { objectType == sub.class.name &&
                                        (source == sub.id || destination == sub.id) }.findAll()

        List tasks                  = Task.findAllBySubscription(sub)
        List propDefGroupBindings   = PropertyDefinitionGroupBinding.findAllBySub(sub)
        AuditConfig ac              = AuditConfig.getConfig(sub)

        List ios            = new ArrayList(sub.ids)
        List docContexts    = new ArrayList(sub.documents)
        List oRoles         = new ArrayList(sub.orgRelations)
        List pRoles         = new ArrayList(sub.prsLinks)
        List subPkgs        = new ArrayList(sub.packages)
        List pendingChanges = new ArrayList(sub.pendingChanges)

        List ies            = IssueEntitlement.where { subscription == sub }.findAll()
                            // = new ArrayList(sub.issueEntitlements)

        List costs          = new ArrayList(sub.costItems)
        List oapl           = new ArrayList(sub.oapl)
        List privateProps   = new ArrayList(sub.privateProperties)
        List customProps    = new ArrayList(sub.customProperties)

        if (dryRun) {
            result.info = []

            result.info << ['Referenzen: Teilnehmer', ref_instanceOf, 'red']
            result.info << ['Referenzen: Nachfolger', ref_previousSubscription]

            result.info << ['Links: Lizenzen', links]
            result.info << ['Aufgaben', tasks]
            result.info << ['Merkmalsgruppen', propDefGroupBindings]
            result.info << ['Vererbungskonfigurationen', ac ? [ac] : []]

            result.info << ['Identifikatoren', ios]
            result.info << ['Dokumente', docContexts]   // delete ? docContext->doc
            result.info << ['Organisationen', oRoles]
            result.info << ['Personen', pRoles]       // delete ? personRole->person
            result.info << ['Pakete', subPkgs]
            result.info << ['Anstehende Änderungen', pendingChanges]
            result.info << ['IssueEntitlements', ies]
            result.info << ['Kostenposten', costs, 'yellow']
            result.info << ['OrgAccessPointLink', oapl]
            result.info << ['Private Merkmale', sub.privateProperties]
            result.info << ['Allgemeine Merkmale', sub.customProperties]
        }
        else if (ref_instanceOf) {

            result = [status: RESULT_QUIT, referencedBy_instanceOf: ref_instanceOf]
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
                    ios.each{ tmp -> tmp.delete() }

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
                    subPkgs.each{ tmp -> tmp.delete() }

                    // pending changes
                    sub.pendingChanges.clear()
                    pendingChanges.each { tmp -> tmp.delete() }

                    // issue entitlements
                    // sub.issueEntitlements.clear()
                    ies.each { tmp -> tmp.delete() }

                    // org access point link
                    sub.oapl.clear()
                    oapl.each { tmp -> tmp.delete() }

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
                        tmp.costItemStatus = RefdataValue.getByValueAndCategory('Deleted','CostItemStatus')
                        tmp.sub = null
                        tmp.subPkg = null
                        tmp.issueEntitlement = null
                        tmp.save()
                    }

                    sub.delete()
                    status.flush()

                    result = [status: RESULT_SUCCESS]
                }
                catch (Exception e) {
                    println 'error while deleting subscription ' + sub.id + ' .. rollback'
                    println e.message
                    status.setRollbackOnly()
                    result = [status: RESULT_ERROR]
                }
            }
        }

        result
    }

    static Map<String, Object> deleteOrganisation(Org org, boolean dryRun) {

        Map<String, Object> result = [:]

        List links          = Links.where { objectType == org.class.name &&
                (source == org.id || destination == org.id) }.findAll()

        List ios            = new ArrayList(org.ids)
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
        List costItems          = CostItem.findAllByOwner(org)
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

        if (dryRun) {
            result.info = []

            result.info << ['Links: Orgs', links]

            result.info << ['Identifikatoren', ios]
            result.info << ['Combos (out)', outgoingCombos]
            result.info << ['Combos (in)', incomingCombos]

            result.info << ['OrgTypes', orgTypes]
            result.info << ['OrgLinks', orgLinks]
            result.info << ['Einstellungen', orgSettings]
            result.info << ['Nutzer-Einstellungen', userSettings]

            result.info << ['Adressen', addresses]
            result.info << ['Kontaktdaten', contacts]
            result.info << ['Personen', prsLinks]
            result.info << ['Personen (tenant)', persons]
            result.info << ['Zugehörigkeiten', affiliations]
            result.info << ['Dokumente', docContexts]   // delete ? docContext->doc
            result.info << ['Platformen', platforms]
            result.info << ['TitleInstitutionProvider (inst)', tips]
            result.info << ['TitleInstitutionProvider (provider)', tipsProviders]

            result.info << ['Allgemeine Merkmale', customProperties]
            result.info << ['Private Merkmale', privateProperties]
            result.info << ['Merkmalsdefinitionen', propertyDefinitions]
            result.info << ['Merkmalsgruppen', propDefGroups]
            result.info << ['Merkmalsgruppenzuweisungen', propDefGroupBindings]

            result.info << ['BudgetCodes', budgetCodes]
            result.info << ['CostItems', costItems]
            result.info << ['CostItemElementConfigurations', costItemsECs]
            result.info << ['Invoices', invoices]
            result.info << ['Orders', orderings]

            result.info << ['Dokumente (owner)', documents]
            result.info << ['DashboardDueDates (responsibility)', dashboardDueDates]
            result.info << ['Anstehende Änderungen', pendingChanges]
            result.info << ['Tasks (owner)', tasks]
            result.info << ['Tasks (responsibility)', tasksResp]
            result.info << ['SystemMessages', systemMessages]
            result.info << ['SystemProfilers', systemProfilers]

            result.info << ['Facts', facts]
            result.info << ['ReaderNumbers', readerNumbers]
            result.info << ['OrgAccessPoints', orgAccessPoints]
            result.info << ['OrgTitleStats', orgTitleStats]

            result.info << ['SurveyInfos', surveyInfos]
            result.info << ['SurveyProperties', surveyProperties]
            result.info << ['SurveyResults (owner)', surveyResults]
            result.info << ['SurveyResults (participant)', surveyResultsParts]
        }

        result
    }

    static Map<String, Object> deleteUser(User user, User replacement, boolean dryRun) {

        Map<String, Object> result = [:]

        List userOrgs       = new ArrayList(user.affiliations)
        List userRoles      = new ArrayList(user.roles)
        List userFolder     = UserFolder.findAllWhere(user: user)
        List userSettings   = UserSettings.findAllWhere(user: user)
        List userTransforms = UserTransforms.findAllWhere(user: user)

        //List costItems = CostItem.executeQuery(
        //        'select x from CostItem x where x.createdBy = :user or x.lastUpdatedBy = :user', [user: user])
        List ciecs = CostItemElementConfiguration.executeQuery(
                'select x from CostItemElementConfiguration x where x.createdBy = :user or x.lastUpdatedBy = :user', [user: user])

        List ddds = DashboardDueDate.findAllByResponsibleUser(user)

        List docs = Doc.executeQuery(
                'select x from Doc x where x.creator = :user or x.user = :user', [user: user])
        List links = Links.executeQuery(
                'select x from Links x where x.createdBy = :user or x.lastUpdatedBy = :user', [user: user])

        List pendingChanges = PendingChange.findAllByUser(user)

        List reminders = new ArrayList(user.reminders)

        List surveyResults = SurveyResult.findAllByUser(user)

        List systemTickets = SystemTicket.findAllByAuthor(user)

        List tasks = Task.executeQuery(
                'select x from Task x where x.creator = :user or x.responsibleUser = :user', [user: user])

        if (dryRun) {
            result.info = []

            result.info << ['Zugehörigkeiten', userOrgs]
            result.info << ['Rollen', userRoles]
            result.info << ['Folder', userFolder]
            result.info << ['Einstellungen', userSettings]
            result.info << ['Transforms', userTransforms]

            //result.info << ['Kosten', costItems, 'blue']
            result.info << ['Kostenkonfigurationen', ciecs, 'blue']
            result.info << ['DashboardDueDate', ddds]
            result.info << ['Dokumente', docs, 'blue']
            result.info << ['Links', links, 'blue']
            result.info << ['Anstehende Änderungen', pendingChanges, 'blue']
            result.info << ['Reminder', reminders]
            result.info << ['Umfrageergebnisse', surveyResults, 'blue']
            result.info << ['Tickets', systemTickets, 'blue']
            result.info << ['Aufgaben', tasks, 'blue']
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

                    // user transforms
                    userTransforms.each { tmp -> tmp.delete() }

                    // cost items
                    costItems.each { tmp ->
                        tmp.lastUpdatedBy = replacement
                        tmp.createdBy = replacement
                        tmp.save()
                    }

                    // cost item element configurations
                    ciecs.each { tmp ->
                        tmp.lastUpdatedBy = replacement
                        tmp.createdBy = replacement
                        tmp.save()
                    }

                    ddds.each { tmp -> tmp.delete() }

                    // docs
                    docs.each { tmp ->
                        if (tmp.creator.id == user.id) {
                            tmp.creator = replacement
                        }
                        if (tmp.user.id == user.id) {
                            tmp.user = replacement
                        }
                        tmp.save()
                    }
                    docs.each { tmp -> tmp.delete() }

                    links.each { tmp ->
                        tmp.lastUpdatedBy = replacement
                        tmp.createdBy = replacement
                        tmp.save()
                    }

                    pendingChanges.each { tmp ->
                        tmp.user = replacement
                        tmp.save()
                    }

                    reminders.each { tmp -> tmp.delete() }

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

                    result = [status: RESULT_SUCCESS]
                }
                catch (Exception e) {
                    println 'error while deleting user ' + user.id + ' .. rollback'
                    println e.message
                    status.setRollbackOnly()
                    result = [status: RESULT_ERROR]
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
}