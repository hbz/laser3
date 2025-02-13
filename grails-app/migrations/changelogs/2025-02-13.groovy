package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1739437462869-1") {
        addColumn(tableName: "org_property") {
            column(name: "op_long_value", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1739437462869-2") {
        addColumn(tableName: "person_property") {
            column(name: "pp_long_value", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1739437462869-3") {
        addColumn(tableName: "platform_property") {
            column(name: "plp_long_value", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1739437462869-4") {
        addColumn(tableName: "provider_property") {
            column(name: "prp_long_value", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1739437462869-5") {
        addColumn(tableName: "license_property") {
            column(name: "lp_long_value", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1739437462869-6") {
        addColumn(tableName: "subscription_property") {
            column(name: "sp_long_value", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1739437462869-7") {
        addColumn(tableName: "vendor_property") {
            column(name: "vp_long_value", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1739437462869-8") {
        addColumn(tableName: "survey_result") {
            column(name: "surre_long_value", type: "int8")
        }
    }
}
