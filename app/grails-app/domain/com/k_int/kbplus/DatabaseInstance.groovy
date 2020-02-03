package com.k_int.kbplus

import de.laser.helper.RDConstants

class DatabaseInstance extends TitleInstance{

    static mapping = {
        includes TitleInstance.mapping
    }

    static constraints = {
    }

    static DatabaseInstance construct(Map<String,Object> params) {
        DatabaseInstance dbi = new DatabaseInstance(params)
        dbi.setGlobalUID()
        dbi
    }

    String printTitleType() {
        RefdataValue.getByValueAndCategory('Database', RDConstants.TITLE_MEDIUM).getI10n('value')
    }

}
