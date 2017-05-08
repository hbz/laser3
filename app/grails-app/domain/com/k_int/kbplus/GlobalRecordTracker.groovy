package com.k_int.kbplus

import javax.persistence.Transient
 

class GlobalRecordTracker {

  GlobalRecordInfo owner
  String localOid
  String identifier
  String name
  Boolean autoAcceptTippAddition
  Boolean autoAcceptTippDelete
  Boolean autoAcceptTippUpdate
  Boolean autoAcceptPackageUpdate


  static mapping = {
                         id column:'grt_id'
                    version column:'grt_version'
                     source column:'grt_owner_fk'
                 identifier column:'grt_identifier'
                   localOid column:'grt_local_oid'
                       name column:'grt_name', type:'text'
     autoAcceptTippAddition column:'grt_auto_tipp_add'
       autoAcceptTippDelete column:'grt_auto_tipp_del'
       autoAcceptTippUpdate column:'grt_auto_tipp_update'
    autoAcceptPackageUpdate column:'grt_auto_pkg_update'
  }

  static constraints = {
                         name(nullable:true, blank:false, maxSize:2048)
                     localOid(nullable:true, blank:false)
       autoAcceptTippAddition(nullable:true, blank:false)
         autoAcceptTippDelete(nullable:true, blank:false)
         autoAcceptTippUpdate(nullable:true, blank:false)
      autoAcceptPackageUpdate(nullable:true, blank:false)
  }

}
