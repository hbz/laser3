package com.k_int.kbplus

import javax.persistence.Transient

class TitleHistoryEvent {

  Date eventDate
  Set participants

  Date dateCreated
  Date lastUpdated

  static hasMany = [ participants:TitleHistoryEventParticipant ]
  static mappedBy = [ participants:'event' ]

  static mapping = {
    participants  batchSize: 10

    dateCreated column: 'the_date_created'
    lastUpdated column: 'the_last_updated'
  }

  static constraints = {
    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true)
    dateCreated (nullable: true)
  }

  @Transient 
  boolean inRole(String role, TitleInstance t) {
    boolean result = false
    participants.each { p ->
      if ( ( p.participant.id == t.id ) && ( p.participantRole == role ) )
        result = true
    }
    return result
  }

  @Transient 
  List<TitleInstance> fromTitles() {
    participants.findAll{it.participantRole=='from'}.collect{ it.participant }
  }

  @Transient
  List<TitleInstance> toTitles() {
    participants.findAll{it.participantRole=='to'}.collect{ it.participant }
  }
}
