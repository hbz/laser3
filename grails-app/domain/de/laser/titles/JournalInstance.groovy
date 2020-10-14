package de.laser.titles

import de.laser.exceptions.CreationException
import de.laser.helper.RDStore

class JournalInstance extends TitleInstance {

    static mapping = {
        includes TitleInstance.mapping
    }

    static constraints = {
    }

    static JournalInstance construct(Map<String,Object> params) throws CreationException {
        withTransaction {
            JournalInstance ji = new JournalInstance(params)
            ji.setGlobalUID()
            if(!ji.save())
                throw new CreationException(ji.errors)
            ji
        }
    }

    String printTitleType() {
        RDStore.TITLE_TYPE_JOURNAL.getI10n('value')
    }
}
