package changelogs

import de.laser.addressbook.Address
import de.laser.RefdataValue

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1726645546369-1") {
        grailsChange {
            change {
                String query = 'select a from Address a where ((a.street_1 is not null or a.street_2 is not null) and a.pob is not null) or (a.city is not null and a.pobCity is not null) or (a.zipcode is not null and a.pobZipcode is not null)'
                Set<Address> mixedAddresses = Address.executeQuery(query)
                int cnt = 0
                mixedAddresses.each { Address a ->
                    Address aNew = new Address(name: a.name, region: a.region, country: a.country, tenant: a.tenant, additionFirst: a.additionFirst, additionSecond: a.additionSecond)
                    if(a.org)
                        aNew.org = a.org
                    else if(a.provider)
                        aNew.provider = a.provider
                    else if(a.vendor)
                        aNew.vendor = a.vendor
                    a.type.each { RefdataValue type ->
                        aNew.addToType(type)
                    }
                    ['pob', 'pobCity', 'pobZipcode'].each { String pobField ->
                        aNew[pobField] = a[pobField]
                        a[pobField] = null
                    }
                    if(!aNew.save())
                        println aNew.errors.getAllErrors().toListString()
                    if(!a.save())
                        println a.errors.getAllErrors().toListString()
                    cnt++
                }
                confirm("${query}: ${cnt}")
                changeSet.setComments("${query}: ${cnt}")
            }
            rollback {}
        }
    }
}
