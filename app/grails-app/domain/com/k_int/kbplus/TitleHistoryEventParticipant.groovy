package com.k_int.kbplus

import javax.persistence.Transient

class TitleHistoryEventParticipant {

    TitleHistoryEvent event
    TitleInstance participant
    String participantRole // in/out

    static belongsTo = [event: TitleHistoryEvent]
}