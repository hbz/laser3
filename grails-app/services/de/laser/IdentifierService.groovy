package de.laser

import com.k_int.kbplus.GenericOIDService
import de.laser.ctrl.LicenseControllerService
import de.laser.helper.RDStore
import grails.gorm.transactions.Transactional
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

/**
 * This service manages generic identifier-related calls
 */
@Transactional
class IdentifierService {

    ContextService contextService
    GenericOIDService genericOIDService
    LicenseControllerService licenseControllerService
    MessageSource messageSource
    SubscriptionService subscriptionService

    @Deprecated
    void checkNullUIDs() {
        List<Person> persons = Person.findAllByGlobalUIDIsNull()
        persons.each { Person person ->
            log.debug("Da identificator pro persona ${person.id}")
            person.setGlobalUID()
            person.save()
        }

        List<Org> orgs = Org.findAllByGlobalUIDIsNull()
        orgs.each { Org org ->
            log.debug("Da identificator pro societate ${org.id}")
            org.setGlobalUID()
            org.save()
        }

        List<Subscription> subs = Subscription.findAllByGlobalUIDIsNull()
        subs.each { Subscription sub ->
            log.debug("Da identificator pro subscriptione ${sub.id}")
            sub.setGlobalUID()
            sub.save()
        }

        List<License> licenses = License.findAllByGlobalUIDIsNull()
        licenses.each { License lic ->
            log.debug("Da identificator pro contracto ${lic.id}")
            lic.setGlobalUID()
            lic.save()
        }

        List<Package> packages = Package.findAllByGlobalUIDIsNull()
        packages.each { Package pkg ->
            log.debug("Da identificator pro ballo ${pkg.id}")
            pkg.setGlobalUID()
            pkg.save()
        }
    }

    /**
     * Deletes the given identifier from the given owner. If the identifier is
     * one of the core institution namespaces, it will be unset instead of deleted
     * @param ownerKey the owner OID from which the identifier should be removed
     * @param targetKey the identifier OID to remove
     */
    void deleteIdentifier(String ownerKey, String targetKey) {
        def owner = genericOIDService.resolveOID(ownerKey)
        def target = genericOIDService.resolveOID(targetKey)
        if (owner && target) {
            if (target.ns.ns in IdentifierNamespace.CORE_ORG_NS) {
                target.value = ''
                target.note = ''
                target.save()
            }
            else {
                if (target."${Identifier.getAttributeName(owner)}"?.id == owner.id) {
                    log.debug("Identifier deleted: ${owner}, ${target}")
                    target.delete()
                }
            }
        }
    }

    /**
     * This is a wrapper method to prepare the display of an object's identifiers in the identifier table
     * @param object the object whose identifiers should be displayed
     * @param contextOrg the context institution whose consortial access should be checked
     * @return a parameter map for the object ID table
     */
    Map<String, Object> prepareIDsForTable(object, Org contextOrg = contextService.getOrg()) {
        boolean objIsOrgAndInst = object instanceof Org && object.getAllOrgTypeIds().contains(RDStore.OT_INSTITUTION.id)
        Locale locale = LocaleContextHolder.getLocale()
        String lang = I10nTranslation.decodeLocale(locale)
        List<IdentifierNamespace> nsList = IdentifierNamespace.executeQuery('select idns from IdentifierNamespace idns where (idns.nsType = :objectType or idns.nsType = null) and idns.isFromLaser = true order by idns.name_'+lang+' asc',[objectType:object.class.name])
        Map<String, SortedSet> objectIds = [:]
        if(!objIsOrgAndInst && object.hasProperty("gokbId") && object.gokbId) {
            SortedSet idSet = new TreeSet()
            idSet << object.gokbId
            objectIds.put(messageSource.getMessage('org.wekbId.label', null, locale), idSet)
        }
        if(object.globalUID) {
            SortedSet idSet = new TreeSet()
            idSet << object.globalUID
            objectIds.put(messageSource.getMessage('globalUID.label', null, locale), idSet)
        }
        if(object.hasProperty("ids")) {
            object.ids.each { Identifier ident ->
                String key = ident.ns.getI10n('name') ?: ident.ns.ns
                SortedSet<Identifier> idsOfNamespace = objectIds.get(key)
                if(!idsOfNamespace)
                    idsOfNamespace = new TreeSet<Identifier>()
                idsOfNamespace << ident
                objectIds.put(key, idsOfNamespace)
            }
        }
        int count = 0
        objectIds.values().each { SortedSet idSet ->
            count += idSet.size()
        }
        boolean showConsortiaFunctions = false
        if(object instanceof Subscription)
            showConsortiaFunctions = subscriptionService.showConsortiaFunctions(contextOrg, object)
        else if(object instanceof License)
            showConsortiaFunctions = licenseControllerService.showConsortiaFunctions(contextOrg, object)
        [objIsOrgAndInst: objIsOrgAndInst, count: count, objectIds: objectIds, nsList: nsList, editable: true, object: object, showConsortiaFunctions: showConsortiaFunctions]
    }

}
