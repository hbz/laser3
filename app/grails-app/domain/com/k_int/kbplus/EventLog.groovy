package com.k_int.kbplus

class EventLog {

  String event
  String message
  Date tstp

  static mapping = {
                id column:'el_id'
             event column:'el_event'
           message column:'el_msg', type:'text'
              tstp column:'el_tstp'
  }

  static constraints = {
  }
}
