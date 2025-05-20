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

    changeSet(author: "klober (modified)", id: "1739437462869-9") {
        grailsChange {
            change {
                sql.execute("update property_definition set pd_type = 'java.lang.Long' where pd_type = 'java.lang.Integer';")
                String c = "property_definition: java.lang.Integer to java.lang.Long -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-10") {
        grailsChange {
            change {
                sql.execute("update org_property set op_long_value = op_int_value where op_int_value is not null;")
                String c = "org_property -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-11") {
        grailsChange {
            change {
                sql.execute("update license_property set lp_long_value = lp_int_value where lp_int_value is not null;")
                String c = "license_property -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-12") {
        grailsChange {
            change {
                sql.execute("update person_property set pp_long_value = pp_int_value where pp_int_value is not null;")
                String c = "person_property -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-13") {
        grailsChange {
            change {
                sql.execute("update platform_property set plp_long_value = plp_int_value where plp_int_value is not null;")
                String c = "platform_property -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-14") {
        grailsChange {
            change {
                sql.execute("update provider_property set prp_long_value = prp_int_value where prp_int_value is not null;")
                String c = "provider_property -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-15") {
        grailsChange {
            change {
                sql.execute("update subscription_property set sp_long_value = sp_int_value where sp_int_value is not null;")
                String c = "subscription_property -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-16") {
        grailsChange {
            change {
                sql.execute("update vendor_property set vp_long_value = vp_int_value where vp_int_value is not null;")
                String c = "vendor_property -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-17") {
        grailsChange {
            change {
                sql.execute("update survey_result set surre_long_value = surre_int_value where surre_long_value is not null;")
                String c = "survey_result -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-18") {
        grailsChange {
            change {
                sql.executeUpdate("delete from property_definition_group_item where pde_property_definition_fk = (select pd_id from property_definition where pd_description = 'Person Property' and pd_name = 'Note' and pd_tenant_fk is null);")
                String c = "removed property_definition_group_item (fk=Person Property>Note) -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-19") {
        grailsChange {
            change {
                sql.executeUpdate("delete from property_definition where pd_description = 'Person Property' and pd_name = 'Note' and pd_tenant_fk is null;")
                String c = "removed property_definition (Person Property>Note) -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-20") {
        grailsChange {
            change {
                sql.executeUpdate("delete from property_definition_group_item where pde_property_definition_fk in (select pd_id from property_definition where pd_description = 'Organisation Config' and pd_tenant_fk is null);")
                String c = "removed property_definition_group_item (fk=Organisation Config) -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-21") {
        grailsChange {
            change {
                List c = []
                List pd = ['API Key', 'Public Journal Access', 'RequestorID', 'statslogin']
                pd.each {
                    sql.executeUpdate("delete from org_property where op_type_fk = (select pd_id from property_definition where pd_description = 'Organisation Config' and pd_name = :pd and pd_tenant_fk is null);", [pd: it])
                    c << "removed org_property (${it}) -> ${sql.getUpdateCount()}"
                }
                confirm(c.join(', '))
                changeSet.setComments(c.join(', '))
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-22") {
        grailsChange {
            change {
                sql.executeUpdate("delete from property_definition where pd_description = 'Organisation Config' and pd_tenant_fk is null;")
                String c = "removed property_definition (Organisation Config) -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-23") {
        grailsChange {
            change {
                sql.executeUpdate("delete from property_definition_group_item where pde_property_definition_fk in (select pd_id from property_definition where pd_description = 'System Config' and pd_tenant_fk is null);")
                String c = "removed property_definition_group_item (fk=System Config) -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-24") {
        grailsChange {
            change {
                sql.executeUpdate("delete from property_definition where pd_description = 'System Config' and pd_tenant_fk is null;")
                String c = "removed property_definition (System Config) -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1739437462869-25") {
        addColumn(tableName: "org") {
            column(name: "org_is_beta_tester", type: "boolean")
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-26") {
        grailsChange {
            change {
                sql.executeUpdate("update org set org_is_beta_tester = false")
                sql.executeUpdate("update org set org_is_beta_tester = true where org_name ilike 'hbz%' or org_name ilike '%backoffice'")
                String c = "set org_is_beta_tester = true -> ${sql.getUpdateCount()}"
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1739437462869-27") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "org_is_beta_tester", tableName: "org", validate: "true")
    }
}
