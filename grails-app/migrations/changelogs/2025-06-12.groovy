package changelogs

import de.laser.AuditConfig
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.wekb.Package

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1749717983219-1") {
        grailsChange {
            change {
                String query = "delete from customer_identifier a using customer_identifier b where a.cid_platform_fk = b.cid_platform_fk and a.cid_customer_fk = b.cid_customer_fk and a.cid_id < b.cid_id"
                int cnt = sql.executeUpdate(query)
                confirm("${query}: ${cnt} duplicates deleted")
                changeSet.setComments("${query}: ${cnt} duplicates deleted")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1749717983219-2") {
        addUniqueConstraint(columnNames: "cid_customer_fk, cid_platform_fk", constraintName: "UKba7e04ae630f5879b0b794d7e2ef", tableName: "customer_identifier")
    }

    changeSet(author: "galffy (hand-coded)", id: "1749717983219-3") {
        grailsChange {
            change {
                int cnt = 0
                Set<Long> parentSubs = AuditConfig.executeQuery("select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection' order by ac.referenceId asc")
                parentSubs.eachWithIndex { Long subId, int i ->
                    //println "now processing ${subId}, record ${i} out of ${parentSubs.size()} records"
                    Map<String, Long> parentParams = [subId: subId]
                    Set<Package> parentPkgs = Package.executeQuery("select sp.pkg from SubscriptionPackage sp where sp.subscription.id = :subId", parentParams)
                    parentPkgs.each { Package pkg ->
                        parentParams.pkg = pkg
                        Set<Subscription> missingMemberSubs = Subscription.executeQuery("select s from Subscription s where s.instanceOf.id = :subId and not exists(select sp from SubscriptionPackage sp where sp.subscription = s and sp.pkg = :pkg)", parentParams)
                        if(missingMemberSubs) {
                            missingMemberSubs.each { Subscription member ->
                                cnt++
                                //println "adding package ${pkg.id} to ${member.id} from ${subId}"
                                SubscriptionPackage sp = new SubscriptionPackage(subscription: member, pkg: pkg)
                                sp.save()
                            }
                        }
                    }
                }
                confirm("${cnt} missing subscription packages reinserted")
                changeSet.setComments("${cnt} missing subscription packages reinserted")
            }
            rollback {}
        }
    }

}
