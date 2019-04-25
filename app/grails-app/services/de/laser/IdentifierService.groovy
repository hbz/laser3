package de.laser

import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Person
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.Package
import grails.transaction.Transactional

@Transactional
class IdentifierService {

    void checkNullUIDs() {
        List persons = Person.findAllByGlobalUIDIsNull()
        persons.each { person ->
            log.debug("Da identificator pro persona ${person.id}")
            person.setGlobalUID()
            person.save(flush: true)
        }
        List orgs = Org.findAllByGlobalUIDIsNull()
        orgs.each { org ->
            log.debug("Da identificator pro societati ${org.id}")
            org.setGlobalUID()
            org.save(flush:true)
        }
        List subs = Subscription.findAllByGlobalUIDIsNull()
        subs.each { sub ->
            log.debug("Da identificator pro subscriptioni ${sub.id}")
            sub.setGlobalUID()
            sub.save(flush:true)
        }
        List licenses = License.findAllByGlobalUIDIsNull()
        licenses.each { lic ->
            log.debug("Da identificator pro contracto ${lic.id}")
            lic.setGlobalUID()
            lic.save(flush:true)
        }
        List packages = Package.findAllByGlobalUIDIsNull()
        packages.each { pkg ->
            log.debug("Da identificator pro ballo ${pkg.id}")
            pkg.setGlobalUID()
            pkg.save(flush:true)
        }
    }

}
