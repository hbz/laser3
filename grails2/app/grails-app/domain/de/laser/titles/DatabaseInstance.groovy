package de.laser.titles

import de.laser.exceptions.CreationException
import de.laser.helper.RDStore

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
            if(!dbi.save())
                throw new CreationException(dbi.errors)
            dbi
        }
    }

    String printTitleType() {
        RDStore.TITLE_TYPE_DATABASE.getI10n('value')
    }
}
