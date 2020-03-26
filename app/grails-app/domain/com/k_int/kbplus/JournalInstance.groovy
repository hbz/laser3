package com.k_int.kbplus

import de.laser.exceptions.CreationException
import de.laser.helper.RDConstants

class JournalInstance extends TitleInstance {

    static mapping = {
        includes TitleInstance.mapping
    }

    static constraints = {
    }

    static JournalInstance construct(Map<String,Object> params) throws CreationException {
        JournalInstance ji = new JournalInstance(params)
        ji.setGlobalUID()
        if(!ji.save())
            throw new CreationException(ji.errors)
        ji
    }

    String printTitleType() {
        RefdataValue.getByValueAndCategory('Journal', RDConstants.TITLE_MEDIUM).getI10n('value')
    }

}
