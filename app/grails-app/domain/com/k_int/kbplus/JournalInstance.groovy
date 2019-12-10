package com.k_int.kbplus

class JournalInstance extends TitleInstance {

    static mapping = {
        includes TitleInstance.mapping
    }

    static constraints = {
    }

    static JournalInstance construct(Map<String,Object> params) {
        JournalInstance ji = new JournalInstance(params)
        ji.setGlobalUID()
        ji
    }

    String printTitleType() {
        RefdataValue.getByValueAndCategory('Journal',RefdataCategory.TI_MEDIUM).getI10n('value')
    }

}
