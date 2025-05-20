package changelogs

import de.laser.IdentifierNamespace
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.properties.PropertyDefinitionGroup
import de.laser.remote.FTControl

Map substitutes = [
        'de.laser.Package'          : 'de.laser.wekb.Package',
        'de.laser.PackageVendor'    : 'de.laser.wekb.PackageVendor',
        'de.laser.Platform'         : 'de.laser.wekb.Platform',
        'de.laser.Provider'         : 'de.laser.wekb.Provider',
        'de.laser.ProviderLink'     : 'de.laser.wekb.ProviderLink',
        'de.laser.ProviderRole'     : 'de.laser.wekb.ProviderRole',
        'de.laser.Vendor'           : 'de.laser.wekb.Vendor',
        'de.laser.VendorLink'       : 'de.laser.wekb.VendorLink',
        'de.laser.VendorRole'       : 'de.laser.wekb.VendorRole',
]

Map substitutes2 = [
        'de.laser.TIPPCoverage'                 : 'de.laser.wekb.TIPPCoverage',
        'de.laser.TitleInstancePackagePlatform' : 'de.laser.wekb.TitleInstancePackagePlatform',
]

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

    changeSet(author: "klober (modified)", id: "1723461329740-5") {
        grailsChange {
            change {
                RefdataCategory rdc_new = RefdataCategory.findByDescAndIsHardData('license.arc.title.transfer.regulation', true)
                RefdataCategory rdc_old = RefdataCategory.findByDescAndIsHardData('license.arc.title.transfer.regulation', false)

                if (rdc_new && rdc_old) {
                    RefdataValue.findAllByOwner(rdc_old).each{ rdv ->
                        RefdataValue.executeUpdate(
                                'update RefdataValue set value = :value, owner = :owner where id = :id',
                                [value: rdv.value + ' (deprecated)', owner: rdc_new, id: rdv.id]
                        )
                    }
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: rdc_old.id])
                }
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1723461329740-6") {
        grailsChange {
            change {
                List<String> done = []

                substitutes.each{ k, v ->
                    List<Long> todo = FTControl.findAllByDomainClassName(k).collect{ it.id }
                    if (todo) {
                        done << ( k + ' -> ' + todo + ' = ' +
                                FTControl.executeUpdate('update FTControl set domainClassName = :v where id in (:todo)', [v: v, todo: todo])
                        )
                    }
                }
                String result = 'FTControl: ' + done.join(', ')
                confirm( result )
                changeSet.setComments( result )
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1723461329740-7") {
        grailsChange {
            change {
                List<String> done = []

                substitutes.each{ k, v ->
                    List<Long> todo = IdentifierNamespace.findAllByNsType(k).collect{ it.id }
                    if (todo) {
                        done << ( k + ' -> ' + todo + ' = ' +
                                IdentifierNamespace.executeUpdate('update IdentifierNamespace set nsType = :v where id in (:todo)', [v: v, todo: todo])
                        )
                    }
                }
                String result = 'IdentifierNamespace: ' + done.join(', ')
                confirm( result )
                changeSet.setComments( result )
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1723461329740-8") {
        grailsChange {
            change {
                List<String> done = []

                substitutes.each{ k, v ->
                    List<Long> todo = PropertyDefinitionGroup.findAllByOwnerType(k).collect{ it.id }
                    if (todo) {
                        done << ( k + ' -> ' + todo + ' = ' +
                                PropertyDefinitionGroup.executeUpdate('update PropertyDefinitionGroup set ownerType = :v where id in (:todo)', [v: v, todo: todo])
                        )
                    }
                }
                String result = 'PropertyDefinitionGroup: ' + done.join(', ')
                confirm( result )
                changeSet.setComments( result )
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1723461329740-9") {
        grailsChange {
            change {
                List<String> done = []

                substitutes2.each{ k, v ->
                    List<Long> todo = FTControl.findAllByDomainClassName(k).collect{ it.id }
                    if (todo) {
                        done << (k + ' -> ' + todo + ' = ' + FTControl.executeUpdate('update FTControl set domainClassName = :v where id in (:todo)', [v: v, todo: todo]))
                    }
                }
                String result = 'FTControl: ' + done.join(', ')
                confirm( result )
                changeSet.setComments( result )
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1723461329740-10") {
        grailsChange {
            change {
                List<String> done = []

                substitutes2.each{ k, v ->
                    List<Long> todo = IdentifierNamespace.findAllByNsType(k).collect{ it.id }
                    if (todo) {
                        done << (k + ' -> ' + todo + ' = ' + IdentifierNamespace.executeUpdate('update IdentifierNamespace set nsType = :v where id in (:todo)', [v: v, todo: todo]))
                    }
                }
                String result = 'IdentifierNamespace: ' + done.join(', ')
                confirm( result )
                changeSet.setComments( result )
            }
            rollback {}
        }
    }
}
