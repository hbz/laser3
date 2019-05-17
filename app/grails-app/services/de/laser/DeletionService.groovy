package de.laser

import com.k_int.kbplus.*
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroupBinding
import org.codehaus.groovy.grails.commons.GrailsApplication

//@CompileStatic
class DeletionService {

    GrailsApplication grailsApplication

    static boolean DRY_RUN = true

    static Map<String, Object> deleteLicense(License lic, boolean dryRun) {

        def stats = [:]
        println "dryRun: ${dryRun}"

        List ref_instanceOf = License.findAllByInstanceOf(lic)

        List io     = IdentifierOccurrence.findAllByLic(lic)
        List oRoles = OrgRole.findAllByLic(lic)
        List pRoles = PersonRole.findAllByLic(lic)

        List tasks          = Task.findAllByLicense(lic)
        List docContexts    = DocContext.findAllByLicense(lic)
        List packages       = new ArrayList(lic.pkgs)  // Package

        List propDefGroupBindings   = PropertyDefinitionGroupBinding.findAllByLic(lic)
        List pendingChanges         = new ArrayList(lic.pendingChanges)
        AuditConfig ac              = AuditConfig.getConfig(lic)

        if (dryRun) {
            stats.referencedBy_instanceOf   = ref_instanceOf

            stats.IdentifierOccurrence      = io
            stats.OrgRole                   = oRoles
            stats.PersonRole                = pRoles // delete ? personRole->person

            stats.PrivateProperty           = lic.privateProperties
            stats.CustomProperty            = lic.customProperties

            // lic.derivedLicenses
            // lic.onixplLicense

            stats.PropertyDefinitionGroupBinding = propDefGroupBindings
            stats.Task                      = tasks
            stats.DocContext                = docContexts // delete ? docContext->doc
            stats.Package                   = packages

            stats.PendingChange             = pendingChanges
            stats.AuditConfig               = ac ? [ac] : []

        }
        else {

            if (ref_instanceOf) {
                return [
                        error: ['Es existieren Teilnehmerlizenzen.Diese müssen zuerst gelöscht werden.'],
                        referencedBy_instanceOf: ref_instanceOf
                ]
            }
        }

        stats
    }

    static Map<String, Object> deleteSubscription(Subscription sub, boolean dryRun) {

        def stats = [:]
        println "dryRun: ${dryRun}"

        List ref_instanceOf = Subscription.findAllByInstanceOf(sub)
        List ref_previousSubscription = Subscription.findAllByPreviousSubscription(sub)

        List io         = IdentifierOccurrence.findAllBySub(sub)
        List oRoles     = OrgRole.findAllBySub(sub)
        List pRoles     = PersonRole.findAllBySub(sub)

        List tasks          = Task.findAllBySubscription(sub)
        List docContexts    = DocContext.findAllBySubscription(sub)
        List subPkgs        = new ArrayList(sub.packages)
        List ies            = new ArrayList(sub.issueEntitlements)
        List costs          = new ArrayList(sub.costItems)
        List oapl           = new ArrayList(sub.oapl)

        List propDefGroupBindings   = PropertyDefinitionGroupBinding.findAllBySub(sub)
        List pendingChanges         = new ArrayList(sub.pendingChanges)
        AuditConfig ac              = AuditConfig.getConfig(sub)

        if (dryRun) {
            stats.referencedBy_instanceOf = ref_instanceOf
            stats.referencedBy_previousSubscription = ref_previousSubscription

            stats.IdentifierOccurrence  = io
            stats.OrgRole               = oRoles
            stats.PersonRole            = pRoles // delete ? personRole->person

            stats.PrivateProperty       = sub.privateProperties
            stats.CustomProperty        = sub.customProperties

            // sub.derivedSubscriptions

            stats.IssueEntitlement      = ies
            stats.Task                  = tasks
            stats.DocContext            = docContexts // delete ? docContext->doc
            stats.SubscriptionPackage   = subPkgs // SubscriptionPackage
            stats.CostItem              = costs
            stats.OrgAccessPointLink    = oapl

            stats.PropertyDefinitionGroupBinding    = propDefGroupBindings
            stats.PendingChange                     = pendingChanges
            stats.AuditConfig                       = ac ? [ac] : []

        }
        else {

            if (ref_instanceOf) {
                return [
                        error: ['Es existieren Teilnehmerverträge. Diese müssen zuerst gelöscht werden.'],
                        referencedBy_instanceOf: ref_instanceOf
                ]
            }
        }

        stats
    }

}