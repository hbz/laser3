package com.k_int.kbplus

class GlobalRecordTracker {

  GlobalRecordInfo owner
  String localOid
  String identifier
  String uuid
  String name
  Boolean autoAcceptTippAddition
  Boolean autoAcceptTippDelete
  Boolean autoAcceptTippUpdate
  Boolean autoAcceptPackageUpdate

  Date dateCreated
  Date lastUpdated

  static mapping = {
                         id column:'grt_id'
                    version column:'grt_version'
                      owner column:'grt_owner_fk',  index:'grt_owner_idx'
                 identifier column:'grt_identifier'
                       uuid column:'grt_uuid'
                   localOid column:'grt_local_oid'
                       name column:'grt_name', type:'text'
     autoAcceptTippAddition column:'grt_auto_tipp_add'
       autoAcceptTippDelete column:'grt_auto_tipp_del'
       autoAcceptTippUpdate column:'grt_auto_tipp_update'
    autoAcceptPackageUpdate column:'grt_auto_pkg_update'

      dateCreated column: 'grt_date_created'
      lastUpdated column: 'grt_last_updated'
  }

  static constraints = {
                         name(nullable:true, blank:false, maxSize:2048)
                     localOid(nullable:true, blank:false)
       autoAcceptTippAddition(nullable:false, blank:false)
                         uuid(nullable:true, blank:false)
         autoAcceptTippDelete(nullable:false, blank:false)
         autoAcceptTippUpdate(nullable:false, blank:false)
      autoAcceptPackageUpdate(nullable:false, blank:false)

      // Nullable is true, because values are already in the database
      lastUpdated (nullable: true, blank: false)
      dateCreated (nullable: true, blank: false)
  }

}
