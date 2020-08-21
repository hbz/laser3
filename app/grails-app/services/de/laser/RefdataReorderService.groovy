package de.laser

import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue
import de.laser.helper.RDConstants
import grails.transaction.Transactional

@Transactional
class RefdataReorderService {

    /*
        This bootstrapped method should capsulate every reordering queries so that no manual database migration scripts needs to be executed
     */
    void reorderRefdata() {
        //semesters: take the order of insertion and make then the ID ascending
        List semesters = RefdataValue.findAllByOwnerAndValueNotEqual(RefdataCategory.getByDesc(RDConstants.SEMESTER),'semester.not.applicable',[sort:'id', order:'asc'])
        RefdataValue.executeUpdate('update RefdataValue rdv set rdv.order = 0 where rdv.value = :value',[value:'semester.not.applicable'])
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
        //currencies: defined by external
        List currencies = RefdataValue.findAllByOwnerAndValueNotEqual(RefdataCategory.getByDesc('Currency'),'EUR',[sort:'value',order:'asc'])
        RefdataValue.executeUpdate('update RefdataValue rdv set rdv.order = 0 where rdv.value = :value',[value:'EUR'])
        order = 10
        currencies.each { RefdataValue c ->
            c.order = order
            c.save()
            order += 10
        }
    }
}
