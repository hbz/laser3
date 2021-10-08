package de.laser.properties

import de.laser.Org
import de.laser.Person
import de.laser.RefdataValue
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated

/**
 * The class's name is what it does: a property (implicitly private) to a {@link de.laser.Person}.
 * Unlike other properties, they are only visible by the owner organisation itself.
 */
class PersonProperty extends AbstractPropertyWithCalculatedLastUpdated {

    PropertyDefinition type
    boolean isPublic = false

    String           stringValue
    Integer          intValue
    BigDecimal       decValue
    RefdataValue     refValue
    URL              urlValue
    String           note = ""
    Date             dateValue
    Org              tenant

    Person owner

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static mapping = {
        id                   column: 'pp_id'
        version              column: 'pp_version'
        stringValue          column: 'pp_string_value', type: 'text'
        intValue             column: 'pp_int_value'
        decValue             column: 'pp_dec_value'
        refValue             column: 'pp_ref_value_rv_fk'
        urlValue             column: 'pp_url_value'
        note                 column: 'pp_note', type: 'text'
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
        stringValue (nullable: true)
        intValue    (nullable: true)
        decValue    (nullable: true)
        refValue    (nullable: true)
        urlValue    (nullable: true)
        note        (nullable: true)
        dateValue   (nullable: true)

        dateCreated (nullable: true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static belongsTo = [
        type:   PropertyDefinition,
        owner:  Person
    ]

    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }
    @Override
    def afterInsert() {
        super.afterInsertHandler()
    }
    @Override
    def beforeUpdate(){
        super.beforeUpdateHandler()
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }
    @Override
    def afterDelete() {
        super.afterDeleteHandler()
    }

    /**
     * Retrieves properties by their date value in the range between two given dates and a given organisation to whom the property's contact is linked
     * @param dateValueFrom the range start date
     * @param dateValueTo the range end date
     * @param org the {@link Org} to whom the contact is linked
     * @return a {@link List} of person properties matching the date range and owner org
     */
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
