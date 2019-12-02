package com.k_int.kbplus

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

}
