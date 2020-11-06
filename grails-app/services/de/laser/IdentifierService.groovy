package de.laser

import com.k_int.kbplus.GenericOIDService
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class IdentifierService {

    GenericOIDService genericOIDService

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

    void deleteIdentifier(GrailsParameterMap params) {
        def owner = genericOIDService.resolveOID(params.owner)
        def target = genericOIDService.resolveOID(params.target)
        if (owner && target) {
            if (target."${Identifier.getAttributeName(owner)}"?.id == owner.id) {
                log.debug("Identifier deleted: ${params}")
                target.delete()
            }
        }
    }

}
