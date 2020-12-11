package de.laser.titles

import de.laser.exceptions.CreationException
import de.laser.helper.RDStore
import org.springframework.dao.DuplicateKeyException

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
            try {
                if(!ji.save())
                    throw new CreationException(ji.errors)
            }
            catch(DuplicateKeyException ignored) {
                log.info("journal instance found, returning existing one ...")
                ji = TitleInstance.findByGokbId(params.gokbId) as JournalInstance
            }
            ji
        }
    }

    String printTitleType() {
        RDStore.TITLE_TYPE_JOURNAL.getI10n('value')
    }
}
