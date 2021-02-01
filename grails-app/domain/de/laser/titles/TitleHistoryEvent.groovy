package de.laser.titles

import de.laser.TitleInstancePackagePlatform

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
  boolean inRole(String role, TitleInstancePackagePlatform t) {
    boolean result = false
    participants.each { p ->
      if ( ( p.participant.id == t.id ) && ( p.participantRole == role ) )
        result = true
    }
    return result
  }

  @Transient 
  List<TitleInstancePackagePlatform> fromTitles() {
    participants.findAll{it.participantRole=='from'}.collect{ it.participant }
  }

  @Transient
  List<TitleInstancePackagePlatform> toTitles() {
    participants.findAll{it.participantRole=='to'}.collect{ it.participant }
  }
}
