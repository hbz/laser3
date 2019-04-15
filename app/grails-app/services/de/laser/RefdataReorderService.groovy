package de.laser

import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue
import grails.transaction.Transactional

@Transactional
class RefdataReorderService {

    /*
        This bootstrapped method should capsulate every reordering queries so that no manual database migration scripts needs to be executed
     */
    void reorderRefdata() {
        //semesters: take the order of insertion and make then the ID ascending
        List semesters = RefdataValue.findAllByOwnerAndValueNotEqual(RefdataCategory.findByDesc('Semester'),'semester.not.applicable',[sort:'id',order:'asc'])
        RefdataValue.executeUpdate('update RefdataValue rdv set rdv.order = 0 where rdv.value = :value',[value:'semester.not.applicable'])
        int order = 10
        semesters.each { s ->
            s.order = order
            s.save()
            order += 10
        }
    }
}
