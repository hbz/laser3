package com.k_int.kbplus

class TitleHistoryEventParticipant {

    TitleHistoryEvent event
    TitleInstance participant
    String participantRole // in/out

    Date dateCreated
    Date lastUpdated

    static belongsTo = [event: TitleHistoryEvent]

    static mapping = {
        dateCreated column: 'thep_date_created'
        lastUpdated column: 'thep_last_updated'
    }

    static constraints = {
        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }
}