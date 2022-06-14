package de.laser.titles

import de.laser.TitleInstancePackagePlatform

/**
 * This class reflects an event when one title changed into another. Recorded are the old and the new names of the title instance
 */
class TitleHistoryEvent {

  Date eventDate
  String from
  String to
  //Set participants

  Date dateCreated
  Date lastUpdated

  static belongsTo = [ tipp: TitleInstancePackagePlatform ]

  static mapping = {
    //participants  batchSize: 10
    eventDate   column: 'the_event_date'
    from        column: 'the_from', type: 'text'
    to          column: 'the_to', type: 'text'
    tipp        column: 'the_tipp_fk'
    dateCreated column: 'the_date_created'
    lastUpdated column: 'the_last_updated'
  }

  static constraints = {
    from        (nullable: true) //backwards-compatibility until old values have been migrated
    to          (nullable: true) //backwards-compatibility until old values have been migrated
    tipp        (nullable: true) //backwards-compatibility until old values have been migrated
    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true)
    dateCreated (nullable: true)
  }

  /*
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
   */
}
