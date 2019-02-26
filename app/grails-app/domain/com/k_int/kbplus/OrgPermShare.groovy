package com.k_int.kbplus

import com.k_int.kbplus.auth.*
import de.laser.helper.RefdataAnnotation

class OrgPermShare {

  Perm perm

  @RefdataAnnotation(cat = '?')
  RefdataValue rdv

  static mapping = {
    cache true
    perm  column: 'perm_id'
    rdv   column: 'rdv_id'
  }

  static constraints = {
    perm blank: false, nullable:false
    rdv blank: false, nullable:false
  }

  static def assertPermShare(perm, rdv) {
    def result = OrgPermShare.findByPermAndRdv(perm,rdv);

    if ( result == null ) {
      result = new OrgPermShare(perm:perm, rdv:rdv).save(failOnError:true,flush:true);
    }

    result
  }
}
