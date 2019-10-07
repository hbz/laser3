package com.k_int.kbplus

class EventLog {

  String event
  String message
  Date tstp

  Date dateCreated
  Date lastUpdated

  static mapping = {
                id column:'el_id'
             event column:'el_event'
           message column:'el_msg', type:'text'
              tstp column:'el_tstp'

      lastUpdated column: 'lastUpdated'
      dateCreated column: 'dateCreated'
  }

  static constraints = {
      // Nullable is true, because values are already in the database
      lastUpdated (nullable: true, blank: false)
      dateCreated (nullable: true, blank: false)
  }
}
