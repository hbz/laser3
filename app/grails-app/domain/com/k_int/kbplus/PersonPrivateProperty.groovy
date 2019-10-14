package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import com.k_int.properties.PropertyDefinition

/**Person private properties are used to store Person related settings and options only for specific memberships**/
class PersonPrivateProperty extends PrivateProperty {

    PropertyDefinition type
    Person owner

    Date dateCreated
    Date lastUpdated

    static mapping = {
        includes AbstractProperty.mapping

        id      column:'ppp_id'
        version column:'ppp_version'
        type    column:'ppp_type_fk'
        owner   column:'ppp_owner_fk', index:'ppp_owner_idx'
        
        dateCreated column: 'ppp_date_created'
        lastUpdated column: 'ppp_last_updated'
        
    }

    static constraints = {
        importFrom AbstractProperty

        type    (nullable:false, blank:false)
        owner   (nullable:false, blank:false)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    static belongsTo = [
        type:   PropertyDefinition,
        owner:  Person
    ]
    static findAllByDateValueBetweenForOrgAndIsNotPulbic(java.sql.Date dateValueFrom, java.sql.Date dateValueTo, Org org){
        executeQuery("SELECT distinct(s) FROM PersonPrivateProperty as s " +
            "WHERE (dateValue >= :fromDate and dateValue <= :toDate) " +
            "AND owner in (SELECT p FROM Person AS p WHERE p.tenant = :tenant AND p.isPublic = :public)" ,
            [fromDate:dateValueFrom,
            toDate:dateValueTo,
            tenant: org,
            public: false])
    }
}
