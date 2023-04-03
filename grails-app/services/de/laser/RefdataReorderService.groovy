package de.laser


import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional

/**
 * This service handles reference data reordering upon system startup. Customised orderings may be defined in the method below
 */
@Transactional
class RefdataReorderService {

    /**
     * This bootstrapped method should capsulate every reordering queries so that no manual database migration scripts needs to be executed
     * !!! Be careful when using rdv.order.
     * This overwrites the sorting, so it may be sorted according to German values. Then the display is wrongly sorted in English!!!
     */
    void reorderRefdata() {
        //semesters: take the order of insertion and make then the ID ascending
        List semesters = RefdataValue.findAllByOwnerAndOrderIsNull(RefdataCategory.getByDesc(RDConstants.SEMESTER),[sort:'id', order:'asc'])
        //RefdataValue.executeUpdate('update RefdataValue rdv set rdv.order = 0 where rdv.value = :value',[value:'semester.not.applicable'])
        int order = 10
        semesters.each { RefdataValue s ->
            s.order = order
            s.save()
            order += 10
        }
        //price categories: take the order of insertion and make then the ID ascending
        //I do not use the getAllRefdataValues because this does the ordering in an incorrect way
        List priceCategories = RefdataValue.executeQuery('select rdv from RefdataValue rdv join rdv.owner rdc where rdc.desc in (:priceCategories) order by rdv.id asc',[priceCategories:[RDConstants.CATEGORY_A_F,RDConstants.CATEGORY_BAUTABELLEN,RDConstants.CATEGORY_EUROMONITOR,RDConstants.CATEGORY_IGI,RDConstants.CATEGORY_JURIS,RDConstants.CATEGORY_UNWTO,RDConstants.CATEGORY_WORLD_BANK]])
        order = 0
        priceCategories.each { RefdataValue pc ->
            pc.order = order
            pc.save()
            order += 10
        }
        //number types: defined by external
        RefdataValue.executeUpdate('update RefdataValue rdv set rdv.order = 0 where rdv.value = :value',[value:'Students'])
        RefdataValue.executeUpdate('update RefdataValue rdv set rdv.order = 10 where rdv.value = :value',[value:'Scientific staff'])
        RefdataValue.executeUpdate('update RefdataValue rdv set rdv.order = 20 where rdv.value = :value',[value:'User'])
        RefdataValue.executeUpdate('update RefdataValue rdv set rdv.order = 30 where rdv.value = :value',[value:'Population'])
        List currencies = RefdataValue.findAllByOwner(RefdataCategory.getByDesc('Currency'),[sort:'value',order:'asc'])
        order = 40
        //currencies: defined by external
        currencies.each { RefdataValue c ->
            switch(c.value) {
                case 'EUR': c.order = 0
                    break
                case 'GBP': c.order = 10
                    break
                case 'USD': c.order = 20
                    break
                case 'CHF': c.order = 30
                    break
                default: c.order = order
                    order += 10
                    break
            }
            c.save()
        }

        //dbs: order by key (question number)
        order = 10
        RefdataValue.executeQuery('select rdv from RefdataValue rdv join rdv.owner rdc where rdc.desc = :dbs order by rdv.value asc', [dbs: RDConstants.DBS_SUBJECT_GROUP]).eachWithIndex { RefdataValue rdv, int i ->
            rdv.order = i*order
            rdv.save()
        }

        //ToDo Order of cost.item.elements

        List list = []
        list.add([owner: 'concurrent.access',       sortToEnd: ['Other', 'Not Specified']])
        list.add([owner: 'confidentiality',         sortToEnd: ['Unknown']])
        list.add([owner: 'cost.item.category',      sortToEnd: ['Other']])
        list.add([owner: 'cost.item.status',        sortToEnd: ['Other']])
        list.add([owner: 'funder.type',             sortToEnd: ['Other Territorial Authorit', 'Other Public Sector Funder', 'Other Religious Communities']])
        list.add([owner: 'indemnification',         sortToEnd: ['Other', 'Unknown']])
        list.add([owner: 'invoicing',               sortToEnd: ['Other']])
        list.add([owner: 'library.network',         sortToEnd: ['No Network']])
        list.add([owner: 'library.type',            sortToEnd: ['Sonstige', 'keine Angabe']])
        list.add([owner: 'license.arc.payment.note',sortToEnd: ['No Hosting fee']])
        list.add([owner: 'license.arc.title.transfer.regulation',   sortToEnd: ['No Regulation']])
        list.add([owner: 'license.oa.corresponding.author.identification', sortToEnd: ['Other']])
        list.add([owner: 'license.statistics.format', sortToEnd: ['Other']])
        list.add([owner: 'license.statistics.user.creds', sortToEnd: ['Other', 'None']])
        list.add([owner: 'license.oa.type',         sortToEnd: ['No Open Access']])
        list.add([owner: 'license.remote.access',   sortToEnd: ['No']])
        list.add([owner: 'license.status',          sortToEnd: ['Unknown']])
        list.add([owner: 'org.type',                sortToEnd: ['Other']])
        list.add([owner: 'package.breakable',       sortToEnd: ['Yes', 'No', 'Unknown']])
        list.add([owner: 'package.consistent',      sortToEnd: ['Yes', 'No', 'Unknown']])
        list.add([owner: 'package.fixed',           sortToEnd: ['Unknown']])
        list.add([owner: 'package.list.status',     sortToEnd: ['Unknown']])
        list.add([owner: 'package.scope',           sortToEnd: ['Unknown']])
        list.add([owner: 'permissions',             sortToEnd: ['Unknown']])
        list.add([owner: 'subscription.resource',   sortToEnd: ['other']])
        list.add([owner: 'package.fixed',           sortToEnd: ['Yes', 'No', 'Unknown']])
        list.add([owner: 'termination.condition',   sortToEnd: ['Other', 'Unknown']])
        list.add([owner: 'tipp.delayed.oa',         sortToEnd: ['Yes', 'No', 'Unknown']])
        list.add([owner: 'tipp.hybrid.oa',          sortToEnd: ['Yes', 'No', 'Unknown']])
        list.add([owner: 'tipp.payment.type',       sortToEnd: ['Unknown']])
        list.add([owner: 'tipp.status',             sortToEnd: ['Unknown']])
        list.add([owner: 'title.status',            sortToEnd: ['Unknown']])
        list.add([owner: 'y.n.o',                   sortToEnd: ['Yes', 'No', 'Other', 'Unknown']])
        list.add([owner: 'y.n.u',                   sortToEnd: ['Yes', 'No', 'Unknown']])

        list.each{
            RefdataCategory owner = RefdataCategory.getByDesc(it.owner)
            if (owner) {
                RefdataValue.executeUpdate('update RefdataValue rdv set rdv.order = :order where rdv.owner = :owner',[order: 0.longValue(), owner: owner])
                Long orderNr = 90
                it.sortToEnd.reverse().each{ sortToEnd ->
                    RefdataValue.executeUpdate('update RefdataValue rdv set rdv.order = :order where rdv.owner = :owner and rdv.value = :value',[order: orderNr, owner: owner, value: sortToEnd])
                    orderNr = orderNr -10
                }
            }
        }
    }
}
