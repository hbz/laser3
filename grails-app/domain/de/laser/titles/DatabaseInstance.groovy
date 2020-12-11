package de.laser.titles

import de.laser.exceptions.CreationException
import de.laser.helper.RDStore
import org.springframework.dao.DuplicateKeyException

class DatabaseInstance extends TitleInstance{

    static mapping = {
        includes TitleInstance.mapping
    }

    static constraints = {
    }

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

    String printTitleType() {
        RDStore.TITLE_TYPE_DATABASE.getI10n('value')
    }
}
