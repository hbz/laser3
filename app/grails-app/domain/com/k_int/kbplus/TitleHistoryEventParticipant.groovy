package com.k_int.kbplus

class TitleHistoryEventParticipant {

    TitleHistoryEvent event
    TitleInstance participant
    String participantRole // in/out

    static belongsTo = [event: TitleHistoryEvent]
}