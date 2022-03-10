package de.laser.titles

import de.laser.exceptions.CreationException
import de.laser.helper.RDStore
import org.springframework.dao.DuplicateKeyException

/**
 * Represents a(n) (e)journal instance
 */
class JournalInstance extends TitleInstance {

    static mapping = {
        includes TitleInstance.mapping
    }

    static constraints = {
    }

    /**
     * Creates a new journal instance if it not exists with the given parameter map. The key to check against is {@link #gokbId}
     * @param params the parameter {@link Map}
     * @return the new journal instance if it not exists, the existing one if it does
     * @throws CreationException
     */
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

    /**
     * Outputs the title type as string, i.e. for icons
     * @return the title type {@link de.laser.RefdataValue}
     */
    String printTitleType() {
        RDStore.TITLE_TYPE_JOURNAL.getI10n('value')
    }
}
