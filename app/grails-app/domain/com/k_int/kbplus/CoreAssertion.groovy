package com.k_int.kbplus

import java.text.SimpleDateFormat

class CoreAssertion {

  Date startDate
  Date endDate

  Date dateCreated
  Date lastUpdated

  static belongsTo = [ tiinp : TitleInstitutionProvider ]

  static mapping = {
    id column:'ca_id'
    tiinp column:'ca_owner', index:'ca_owner'
    startDate column:'ca_start_date'
    endDate column:'ca_end_date'
    version column:'ca_ver'

    dateCreated column: 'ca_date_created'
    lastUpdated column: 'ca_last_updated'

  }

  static constraints = {
    endDate(nullable:true, blank:false)
    startDate(nullable:false, blank:false)
    startDate validator: {val,obj ->
      if(obj.endDate == null) return true;
      val = new java.sql.Timestamp(val.getTime());
      if(val > obj.endDate) return false;
    }
    endDate validator: {val,obj ->
      if ( val != null ) {
        val = new java.sql.Timestamp(val.getTime());
        if(val < obj.startDate) return false;
      }
    }

    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true, blank: false)
    dateCreated (nullable: true, blank: false)

  }

  @Override
  String toString(){
    String strFormat = 'yyyy-MM-dd'
    SimpleDateFormat formatter = new SimpleDateFormat(strFormat)
    return "${startDate?formatter.format(startDate):''} : ${endDate?formatter.format(endDate):''}"
  }

}
