package de.laser


import de.laser.storage.RDConstants
import grails.gorm.transactions.Transactional

/**
 * This service handles reference data reordering upon system startup. Customised orderings may be defined in the method below
 */
@Transactional
class RefdataReorderService {

    /**
     * This bootstrapped method should capsule every reordering queries so that no manual database migration scripts needs to be executed
     * !!! Be careful when using rdv.order.
     * This overwrites the sorting, so it may be sorted according to German values. Then the display is wrongly sorted in English!!!
     */
    void reorderRefdata() {
        //country: Germany, Austria and Switzerland first
        long order = 10
        List countries = RefdataValue.executeQuery("select rdv from RefdataValue rdv join rdv.owner rdc where rdc.desc = :country order by rdv.value_de asc", [country: RDConstants.COUNTRY])
        countries.eachWithIndex { RefdataValue ct, long i ->
            switch(ct.value) {
                case 'DE': ct.order = 0
                    break
                case 'AT': ct.order = 10
                    break
                case 'CH': ct.order = 20
                    break
                default: ct.order = i*order+30
                    break
            }
            ct.save()
        }
        //address type: billing address first
        order = 10
        List addressTypes = RefdataValue.executeQuery("select rdv from RefdataValue rdv join rdv.owner rdc where rdc.desc = :addressType order by rdv.value_de asc", [addressType: RDConstants.ADDRESS_TYPE])
        addressTypes.eachWithIndex { RefdataValue at, long i ->
            if(at.value == 'Billing address')
                at.order = 0
            else {
                at.order = i*order+10
            }
            at.save()
        }
        //semesters
        List<String> semesters = ['w17/18', 's18', 'w18/19', 's19', 'w19/20', 's20', 'w20/21', 's21', 'w21/22', 's22', 'w22/23', 's23', 'w23/24', 's24', 'w24/25',
                's25', 'w25/26', 's26', 'w26/27', 's27', 'w27/28', 's28', 'w28/29', 's29', 'w29/30', 's30', 'w30/31', 's31']
        order = 0
        semesters.each { String s ->
            RefdataValue semester = RefdataValue.getByValueAndCategory(s, RDConstants.SEMESTER)
            semester.order = order
            semester.save()
            order += 10
        }
        //price categories: take the order of insertion and make then the ID ascending
        //I do not use the getAllRefdataValues because this does the ordering in an incorrect way
        Set<String> catDescs = [RDConstants.CATEGORY_A_F,
                                RDConstants.CATEGORY_BAUTABELLEN,
                                RDConstants.CATEGORY_DETAIL,
                                RDConstants.CATEGORY_EUROMONITOR,
                                RDConstants.CATEGORY_INSIDE_DIN,
                                RDConstants.CATEGORY_IGI,
                                RDConstants.CATEGORY_JURIS,
                                RDConstants.CATEGORY_MEINUNTERRICHT,
                                RDConstants.CATEGORY_PNAS,
                                RDConstants.CATEGORY_SCIENTIFIC,
                                RDConstants.CATEGORY_SCOPUS,
                                RDConstants.CATEGORY_TOTAL_MATERIA,
                                RDConstants.CATEGORY_ULLMANNS,
                                RDConstants.CATEGORY_UNWTO,
                                RDConstants.CATEGORY_WORLD_BANK]
        List priceCategories = RefdataValue.executeQuery('select rdv from RefdataValue rdv join rdv.owner rdc where rdc.desc in (:priceCategories) order by rdv.id asc',[priceCategories:catDescs])
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

        //ddc: order by key (DDC)
        order = 10
        RefdataValue.executeQuery('select rdv from RefdataValue rdv join rdv.owner rdc where rdc.desc = :ddc order by rdv.value asc', [ddc: RDConstants.DDC]).eachWithIndex { RefdataValue rdv, int i ->
            rdv.order = i*order
            rdv.save()
        }

        //lang_iso: German and English first
        order = 10
        RefdataValue.executeQuery("select rdv from RefdataValue rdv join rdv.owner rdc where rdc.desc = :langIso order by rdv.value asc", [langIso: RDConstants.LANGUAGE_ISO]).eachWithIndex{ RefdataValue rdv, int i ->
            switch(rdv.value) {
                case 'ger': rdv.order = 10
                    break
                case 'eng': rdv.order = 20
                    break
                case 'fre': rdv.order = 30
                    break
                default: rdv.order = i*order+40
                    break
            }
            rdv.save()
        }

        //simuser.count: numerical order, then the textual ones
        order = 150
        RefdataValue.executeQuery("select rdv from RefdataValue rdv join rdv.owner rdc where rdc.desc = :simuserCnt", [simuserCnt: RDConstants.SIM_USER_NUMBER]).eachWithIndex{ RefdataValue rdv, int i ->
            try {
                long numericOrder = Long.parseLong(rdv.value)
                rdv.order = numericOrder
            }
            catch (NumberFormatException e) {
                rdv.order = order
                order += 10
            }
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
