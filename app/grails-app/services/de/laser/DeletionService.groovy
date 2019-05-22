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
            result.'Referenzen: Teilnehmer'   = ref_instanceOf

            result.'Aufgaben'           = tasks
            result.'Merkmalsgruppen'    = propDefGroupBindings
            result.'Lizenzen'           = subs
            result.'Vererbungskonfigurationen'  = ac ? [ac] : []

            // lic.onixplLicense

            result.'Identifikatoren'         = ios
            result.'Dokumente'               = docContexts   // delete ? docContext->doc
            result.'Organisationen'          = oRoles
            result.'Personen'                = pRoles        // delete ? personRole->person
            result.'Pakete'                  = packages
            result.'Anstehende Änderungen'   = pendingChanges
            result.'Private Merkmale'        = lic.privateProperties
            result.'Allgemeine Merkmale'     = lic.customProperties
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
                }
                catch (Exception e) {
                    println 'error while deleting license ' + lic.id + ' .. rollback'
                    println e.message
                    status.setRollbackOnly()
                    result = [status: RESULT_ERROR]
                }
                result = [status: RESULT_SUCCESS]
            }
        }

        result
    }

    static Map<String, Object> deleteSubscription(Subscription sub, boolean dryRun) {

        Map<String, Object> result = [:]

        List ref_instanceOf = Subscription.findAllByInstanceOf(sub)
        List ref_previousSubscription = Subscription.findAllByPreviousSubscription(sub)

        List tasks                  = Task.findAllBySubscription(sub)
        List propDefGroupBindings   = PropertyDefinitionGroupBinding.findAllBySub(sub)
        AuditConfig ac              = AuditConfig.getConfig(sub)

        List ios            = new ArrayList(sub.ids)
        List docContexts    = new ArrayList(sub.documents)
        List oRoles         = new ArrayList(sub.orgRelations)
        List pRoles         = new ArrayList(sub.prsLinks)
        List subPkgs        = new ArrayList(sub.packages)
        List pendingChanges = new ArrayList(sub.pendingChanges)
        List ies            = new ArrayList(sub.issueEntitlements)
        List costs          = new ArrayList(sub.costItems)
        List oapl           = new ArrayList(sub.oapl)
        List privateProps   = new ArrayList(sub.privateProperties)
        List customProps    = new ArrayList(sub.customProperties)

        if (dryRun) {
            result.'Referenzen: Teilnehmer' = ref_instanceOf
            result.'Referenzen: Nachfolger' = ref_previousSubscription

            result.'Aufgaben'                   = tasks
            result.'Merkmalsgruppen'            = propDefGroupBindings
            result.'Vererbungskonfigurationen'  = ac ? [ac] : []

            result.'Identifikatoren'     = ios
            result.'Dokumente'           = docContexts   // delete ? docContext->doc
            result.'Organisationen'      = oRoles
            result.'Personen'            = pRoles        // delete ? personRole->person
            result.'Pakete'              = subPkgs
            result.'Anstehende Änderungen' = pendingChanges
            result.'IssueEntitlements'   = ies
            result.'Kostenposten'        = costs
            result.'OrgAccessPointLink'  = oapl
            result.'Private Merkmale'    = sub.privateProperties
            result.'Allgemeine Merkmale' = sub.customProperties
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
                    sub.issueEntitlements.clear()
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
                }
                catch (Exception e) {
                    println 'error while deleting subscription ' + sub.id + ' .. rollback'
                    println e.message
                    status.setRollbackOnly()
                    result = [status: RESULT_ERROR]
                }
                result = [status: RESULT_SUCCESS]
            }
        }

        result
    }

    static Map<String, Object> deleteUser(User user, boolean dryRun) {

        Map<String, Object> result = [:]

        List userOrgs       = new ArrayList(user.affiliations)
        List userRoles      = new ArrayList(user.roles)
        List userFolder     = UserFolder.findAllWhere(user: user)
        List userSettings   = UserSettings.findAllWhere(user: user)
        List userTransforms = UserTransforms.findAllWhere(user: user)

        List costItems = CostItem.executeQuery(
                'select x from CostItem x where x.createdBy = :user or x.lastUpdatedBy = :user', [user: user])
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
            result.'Zugehörigkeiten'        = userOrgs
            result.'Rollen'                 = userRoles
            result.'Folder'                 = userFolder  // impl. FolderItem ?
            result.'Einstellungen'          = userSettings
            result.'Transforms'             = userTransforms  // impl. Transforms ?

            result.'Kosten'                 = costItems
            result.'Kostenkonfigurationen'  = ciecs
            result.'DashboardDueDate'       = ddds
            result.'Dokumente'              = docs
            result.'Links'                  = links
            result.'Anstehende Änderungen'  = pendingChanges
            result.'Reminder'               = reminders
            result.'Umfrageergebnisse'      = surveyResults
            result.'Tickets'                = systemTickets
            result.'Aufgaben'               = tasks
        }
        else {
            return // TODO

            User.withTransaction { status ->

                try {

                    // user orgs
                    user.affiliations.clear()
                    userOrgs.each { tmp -> tmp.delete() }

                    // user roles
                    user.roles.clear()
                    userOrgs.each { tmp -> tmp.delete() }

                    // user folder
                    userFolder.each { tmp -> tmp.delete() }

                    // user settings
                    userSettings.each { tmp -> tmp.delete() }

                    // user transforms
                    userTransforms.each { tmp -> tmp.delete() }

                    costItems.each { tmp -> null /* tmp.delete() */ }
                    ciecs.each { tmp -> null /* tmp.delete() */ }

                    // dashboard due date
                    ddds.each { tmp -> tmp.delete() }

                    docs.each { tmp -> null /* tmp.delete() */ }
                    links.each { tmp -> null /* tmp.delete() */ }
                    pendingChanges.each { tmp -> null /* tmp.delete() */ }
                    reminders.each { tmp -> null /* tmp.delete() */ }
                    surveyResults.each { tmp -> null /* tmp.delete() */ }
                    systemTickets.each { tmp -> null /* tmp.delete() */ }
                    tasks.each { tmp -> null /* tmp.delete() */ }

                    //user.delete()
                    //status.flush()
                }
                catch (Exception e) {
                    println 'error while deleting user ' + user.id + ' .. rollback'
                    println e.message
                    status.setRollbackOnly()
                    result = [status: RESULT_ERROR]
                }
                result = [status: RESULT_SUCCESS]
            }
        }

        result
    }
}