package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated
import com.k_int.properties.PropertyDefinition

/**Person private properties are used to store Person related settings and options only for specific memberships**/
class PersonProperty extends AbstractPropertyWithCalculatedLastUpdated {

    PropertyDefinition type
    Person owner
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id                   column: 'pp_id'
        version              column: 'pp_version'
        stringValue          column: 'pp_string_value'
        intValue             column: 'pp_int_value'
        decValue             column: 'pp_dec_value'
        refValue             column: 'pp_ref_value_rv_fk'
        urlValue             column: 'pp_url_value'
        note                 column: 'pp_note'
        dateValue            column: 'pp_date_value'
        type                 column: 'pp_type_fk', index: 'pp_type_idx'
        owner                column: 'pp_owner_fk', index:'pp_owner_idx'
        tenant               column: 'pp_tenant_fk', index: 'pp_tenant_fk'
        isPublic             column: 'pp_is_public'
        dateCreated          column: 'pp_date_created'
        lastUpdated          column: 'pp_last_updated'
        lastUpdatedCascading column: 'pp_last_updated_cascading'
    }

    static constraints = {
        importFrom AbstractPropertyWithCalculatedLastUpdated

        type    (blank:false)
        owner   (blank:false)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    static belongsTo = [
        type:   PropertyDefinition,
        owner:  Person
    ]

    @Override
    def afterDelete() {
        super.afterDeleteHandler()
    }
    @Override
    def afterInsert() {
        super.afterInsertHandler()
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }

    static findAllByDateValueBetweenForOrgAndIsNotPulbic(java.sql.Date dateValueFrom, java.sql.Date dateValueTo, Org org){
        executeQuery("SELECT distinct(s) FROM PersonProperty as s " +
            "WHERE (dateValue >= :fromDate and dateValue <= :toDate) " +
            "AND owner in (SELECT p FROM Person AS p WHERE p.tenant = :tenant AND p.isPublic = :public)" ,
            [fromDate:dateValueFrom,
            toDate:dateValueTo,
            tenant: org,
            public: false])
    }
}
