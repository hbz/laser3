package de.laser


import grails.gorm.transactions.Transactional

@Transactional
class IdentifierService {

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
}
