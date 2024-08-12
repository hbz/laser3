package changelogs

import de.laser.RefdataCategory
import de.laser.RefdataValue

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1723461329740-1") {
        grailsChange {
            change {
                RefdataCategory cr = RefdataCategory.findByDesc('cluster.role')
                if (cr) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: cr])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: cr.id])
                }

                RefdataCategory ct1 = RefdataCategory.findByDesc('cluster.type')
                if (ct1) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: ct1])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: ct1.id])
                }

                RefdataCategory ct2 = RefdataCategory.findByDesc('Cluster Type')
                if (ct2) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: ct2])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: ct2.id])
                }
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1723461329740-2") {
        grailsChange {
            change {
                RefdataCategory tf = RefdataCategory.findByDesc('transform.format')
                if (tf) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: tf])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: tf.id])
                }

                RefdataCategory tt = RefdataCategory.findByDesc('transform.type')
                if (tt) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: tt])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: tt.id])
                }
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1723461329740-3") {
        grailsChange {
            change {
                RefdataCategory ot = RefdataCategory.findByDesc('OrgType')
                if (ot) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: ot])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: ot.id])
                }
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1723461329740-4") {
        grailsChange {
            change {
                RefdataCategory pg1 = RefdataCategory.findByDesc('Package.Global')
                if (pg1) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: pg1])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: pg1.id])
                }

                RefdataCategory pg2 = RefdataCategory.findByDesc('Package Global')
                if (pg2) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: pg2])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: pg2.id])
                }

                RefdataCategory ppt1 = RefdataCategory.findByDesc('Package.PaymentType')
                if (ppt1) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: ppt1])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: ppt1.id])
                }

                RefdataCategory ppt2 = RefdataCategory.findByDesc('Package Payment Type')
                if (ppt2) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: ppt2])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: ppt2.id])
                }
            }
            rollback {}
        }
    }
}
