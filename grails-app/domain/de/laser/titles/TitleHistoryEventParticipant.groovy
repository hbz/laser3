package de.laser.titles

import de.laser.wekb.TitleInstancePackagePlatform

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
        event       column: 'thep_event_fk',         index: 'thep_event_idx'
        participant column: 'thep_participant_fk',   index: 'thep_participant_idx'
    participantRole column: 'thep_participant_role'

        dateCreated column: 'thep_date_created'
        lastUpdated column: 'thep_last_updated'
    }

    static constraints = {
        lastUpdated (nullable: true)
    }
}