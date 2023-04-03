package changelogs

import de.laser.Address
import de.laser.Contact
import de.laser.DeletionService
import de.laser.DocContext
import de.laser.Identifier
import de.laser.Org
import de.laser.OrgRole
import de.laser.PersonRole
import de.laser.properties.OrgProperty
import grails.util.Holders
import org.springframework.transaction.TransactionStatus

databaseChangeLog = {

    //will not work yet
    DeletionService deletionService = Holders.grailsApplication.mainContext.getBean('deletionService')

    changeSet(author: "galffy (hand-coded)", id: "1680242679255-1") {
        grailsChange {
            change {
                Map<String, String> orgsToBeMerged = [
                        'org:a53bb056-198a-43ec-9100-d013f1e650b2':'org:558905e9-7ce9-4eac-bde5-ef5dd5d0cd34',
                        'org:e30e4c39-58f5-4217-a3b0-c9db804095ee':'org:b9245ca2-0407-4a8e-8c84-774f422d1bd2',
                        'org:328961d4-3f3c-4990-9aff-5c0510295f51':'org:4ede1d54-57ca-4f7f-aa46-cbee51bd440b',
                        'org:589a31e9-6f91-4755-8dd4-19325e0cab2a':'org:1a9145e0-8eb4-4950-a426-5742a834575a',
                        'org:17ce7780-df47-4916-b2de-9201cf66527b':'org:f320f003-1d17-42f1-9320-45117bf728bb',
                        'org:e92e4532-5130-48e3-a541-a218a9fb5d7c':'org:9acf4ffd-4798-4ecb-898f-8239078ddfe5',
                        'org:4ca1727e-43d4-4f38-a284-ab2f5cc5ee48':'org:46adca3d-2d0d-4508-921f-81c402bc3c8a',
                        'org:c45d3294-60f0-4e03-998a-119fa1078be4':'org:7a08730d-e11c-4545-bbd4-02879cbbfe86',
                        'org:119d807f-90a2-43c0-a5fe-83cdca4e99ec':'org:ac06387f-2e79-499a-9fa7-140d5395d72e'
                ]
                orgsToBeMerged.each { String targetKey, String replacalKey ->
                    Org target = Org.findByGlobalUID(targetKey), replacal = Org.findByGlobalUID(replacalKey)
                    if(target && replacal) {
                        println "${target} found, ${replacal} found"
                        OrgRole.executeUpdate('update OrgRole oo set oo.org = :replacal where oo.org = :target', [target: target, replacal: replacal])
                        PersonRole.executeUpdate('update PersonRole pr set pr.org = :replacal where pr.org = :target', [target: target, replacal: replacal])
                        Address.executeUpdate('update Address a set a.org = :replacal where a.org = :target', [target: target, replacal: replacal])
                        Contact.executeUpdate('update Contact c set c.org = :replacal where c.org = :target', [target: target, replacal: replacal])
                        Identifier.executeUpdate('update Identifier id set id.org = :replacal where id.org = :target and not exists (select id2 from Identifier id2 where id2.org = :replacal and id2.value = id.value and id2.ns = id.ns)', [target: target, replacal: replacal])
                        OrgProperty.executeUpdate('update OrgProperty op set op.owner = :replacal where op.owner = :target', [target: target, replacal: replacal])
                        DocContext.executeUpdate('update DocContext dc set dc.org = :replacal where dc.org = :target', [target: target, replacal: replacal])
                        DocContext.executeUpdate('update DocContext dc set dc.targetOrg = :replacal where dc.targetOrg = :target', [target: target, replacal: replacal])
                        deletionService.deleteOrganisation(target, replacal, false).toMapString()
                    }
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1680242679255-2") {
        grailsChange {
            change {
                sql.execute("delete from identifier where id_ns_fk in (select idns_id from identifier_namespace where idns_ns = 'global')")
                sql.execute("delete from identifier_namespace where idns_ns = 'global'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1680264063145-3") {
        grailsChange {
            change {
                sql.execute("update org set org_sortname = org_shortname where org_sortname is null and org_shortname is not null")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1680264063145-4") {
        dropColumn(columnName: "org_shortname", tableName: "org")
    }

}
