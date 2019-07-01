package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.User
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.helper.RDStore
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
            result.info << ['Dokumente', docs, 'teal']
            result.info << ['Links', links, 'blue']
            result.info << ['Anstehende Änderungen', pendingChanges, 'teal']
            result.info << ['Reminder', reminders]
            result.info << ['Umfrageergebnisse', surveyResults, 'teal']
            result.info << ['Tickets', systemTickets, 'teal']
            result.info << ['Aufgaben', tasks, 'teal']
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
}