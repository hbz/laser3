package de.laser.titles

import de.laser.exceptions.CreationException
import de.laser.helper.RDStore
import org.springframework.dao.DuplicateKeyException

/**
 * Represents a database instance
 */
class DatabaseInstance extends TitleInstance{

    static mapping = {
        includes TitleInstance.mapping
    }

    static constraints = {
    }

    /**
     * Creates a new database instance if it not exists with the given parameter map. The key to check against is {@link #gokbId}
     * @param params the parameter {@link Map}
     * @return the new database instance if it not exists, the existing one if it does
     * @throws CreationException
     */
    static DatabaseInstance construct(Map<String,Object> params) throws CreationException {
        withTransaction {
            DatabaseInstance dbi = new DatabaseInstance(params)
            dbi.setGlobalUID()
            try {
                if(!dbi.save())
                    throw new CreationException(dbi.errors)
            }
            catch (DuplicateKeyException ignored) {
                log.info("database instance exists, returning existing one")
                dbi = TitleInstance.findByGokbId(params.gokbId) as DatabaseInstance
            }
            dbi
        }
    }

    /**
     * Outputs the title type as string, i.e. for icons
     * @return the title type {@link de.laser.RefdataValue}
     */
    String printTitleType() {
        RDStore.TITLE_TYPE_DATABASE.getI10n('value')
    }
}
