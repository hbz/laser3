package com.k_int.kbplus

import javax.persistence.Transient

class GlobalRecordInfo {

  GlobalRecordSource source
  String identifier
  String desc
  String name
  Long rectype
  Date ts
  Set trackers 
  byte[] record
  RefdataValue kbplusCompliant

  static hasMany = [ trackers : GlobalRecordTracker ]
  static mappedBy = [ trackers : 'owner']


  static mapping = {
                   id column:'gri_id'
              version column:'gri_version'
               source column:'gri_source_fk'
                   ts column:'gri_timestamp'
           identifier column:'gri_identifier'
                 name column:'gri_name', type:'text'
                 desc column:'gri_desc', type:'text'
              rectype column:'gri_rectype'
               record column:'gri_record', length:(1024*1024*64)// , type:'blob' // , length:(1024*1024*64)
      kbplusCompliant column:'gri_kbplus_compliant'
  }

  static constraints = {
                  name(nullable:true, blank:false, maxSize:2048)
                  desc(nullable:true, blank:false)
               rectype(nullable:true, blank:false)
                    ts(nullable:true, blank:false)
       kbplusCompliant(nullable:true, blank:false)
  }

  transient String getDisplayRectype() {
    def result=""
    switch ( rectype ) {
      case 0:
        result = 'Package'
        break;
    }
    result
  }

}
