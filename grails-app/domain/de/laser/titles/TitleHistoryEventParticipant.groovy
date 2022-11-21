package de.laser.titles

import de.laser.TitleInstancePackagePlatform

/**
 * This class represents a participant {@link TitleInstancePackagePlatform} of a {@link TitleHistoryEvent}
 */
class TitleHistoryEventParticipant {

    TitleHistoryEvent event
    TitleInstancePackagePlatform participant
    String participantRole // in/out

    Date dateCreated
    Date lastUpdated

    static belongsTo = [event: TitleHistoryEvent]

    static mapping = {
        id          column: 'thep_id'
        version     column: 'thep_version'
        event       column: 'thep_event_fk'
        participant column: 'thep_participant_fk'
    participantRole column: 'thep_participant_role'

        dateCreated column: 'thep_date_created'
        lastUpdated column: 'thep_last_updated'
    }

    static constraints = {
        lastUpdated (nullable: true)
    }
}