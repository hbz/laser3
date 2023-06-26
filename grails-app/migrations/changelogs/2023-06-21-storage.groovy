package changelogs

import de.laser.Address
import de.laser.Contact
import de.laser.Doc
import de.laser.DocContext
import de.laser.Identifier
import de.laser.Org
import de.laser.OrgRole
import de.laser.OrgSetting
import de.laser.properties.OrgProperty
import de.laser.storage.RDStore
import de.laser.traces.DeletedObject

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1686720364944-1") {
        grailsChange {
            change {
                int migrated = 0
                Org.withTransaction {
                    Set<Org> orgsMarkedAsDeleted = Org.executeQuery('select o from Org o where o.status = :deleted and ' +
                            'not exists(select oo from OrgRole oo where oo.org = o and oo.roleType in (:subscriber)) and not exists(select surre from SurveyResult surre where surre.participant = o or surre.owner = o) and '+
                            'not exists(select pr from PersonRole pr where pr.org = o)',
                            [deleted: RDStore.ORG_STATUS_DELETED, subscriber: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER]])
                    orgsMarkedAsDeleted.each { Org o ->
                        DeletedObject.construct(o)
                    }
                    Address.executeUpdate('delete from Address a where a.org in (:toDelete)', [toDelete: orgsMarkedAsDeleted])
                    Contact.executeUpdate('delete from Contact c where c.org in (:toDelete)', [toDelete: orgsMarkedAsDeleted])
                    DocContext.executeUpdate('delete from DocContext dc where dc.org in (:toDelete) or dc.targetOrg in (:toDelete)', [toDelete: orgsMarkedAsDeleted])
                    Doc.executeUpdate('delete from Doc d where d.owner in (:toDelete)', [toDelete: orgsMarkedAsDeleted])
                    Identifier.executeUpdate('delete from Identifier id where id.org in (:toDelete)', [toDelete: orgsMarkedAsDeleted])
                    OrgRole.executeUpdate('delete from OrgRole oo where oo.org in (:toDelete) and oo.roleType not in (:subscriber)', [toDelete: orgsMarkedAsDeleted, subscriber: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER]])
                    OrgProperty.executeUpdate('delete from OrgProperty op where op.owner in (:toDelete)', [toDelete: orgsMarkedAsDeleted])
                    OrgSetting.executeUpdate('delete from OrgSetting os where os.org in (:toDelete)', [toDelete: orgsMarkedAsDeleted])
                    migrated = Org.executeUpdate('delete from Org o where o in (:toDelete)', [toDelete: orgsMarkedAsDeleted])
                }
                confirm("BeanStore.getDeletionService().deleteOrg(o): ${migrated}")
                changeSet.setComments("BeanStore.getDeletionService().deleteOrg(o): ${migrated}")
            }
            rollback {}
        }
    }
}
