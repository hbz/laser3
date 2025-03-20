package de.laser.finance

import de.laser.Org
import de.laser.base.AbstractI10n
import de.laser.storage.BeanStore
import de.laser.utils.LocaleUtils

import javax.persistence.Transient
import javax.validation.UnexpectedTypeException

class CostInformationDefinition extends AbstractI10n {

    static final String COST_INFORMATION = 'Cost Information'

    String name
    String name_de
    String name_en

    String expl_de
    String expl_en

    String type
    String refdataCategory

    Date dateCreated
    Date lastUpdated

    /**
     * used for private properties; marks the owner who can define and see properties of this type
     */
    Org tenant
    /**
     * indicates this object is created via current bootstrap (=> defined in /src/mail/webapp/setup/CostInformationDefinition.csv)
     */
    boolean isHardData = false

    static mapping = {
        id column: 'cif_id'
        version column: 'cif_version'
        name column: 'cif_name', index: 'cif_name_idx'
        name_de column: 'cif_name_de'
        name_en column: 'cif_name_en'
        expl_de column: 'cif_explanation_de', type: 'text'
        expl_en column: 'cif_explanation_en', type: 'text'
        type column: 'cif_type', index: 'cif_type_idx'
        tenant column: 'cif_tenant_fk', index: 'cif_tenant_idx'
        isHardData column: 'cif_is_hard_data'
        lastUpdated     column: 'cif_last_updated'
        dateCreated     column: 'cif_date_created'
        sort name_de: 'asc'
    }

    static constraints = {
        name                (blank: false)
        name_de             (nullable: true, blank: false)
        name_en             (nullable: true, blank: false)
        expl_de             (nullable: true, blank: false)
        expl_en             (nullable: true, blank: false)
        type                (blank: false)
        refdataCategory     (nullable: true)
        tenant              (nullable: true)
    }

    @Transient
    static def validTypes = [
            //'java.util.Date'                : ['de': 'Datum', 'en': 'Date'],
            //'java.math.BigDecimal'          : ['de': 'Dezimalzahl', 'en': 'Decimal'],
            //'java.lang.Long'                : ['de': 'Ganzzahl', 'en': 'Number' ],
            'de.laser.RefdataValue'         : ['de': 'Referenzwert', 'en': 'Refdata'],
            'java.lang.String'              : ['de': 'Text', 'en': 'Text'],
            //'java.net.URL'                  : ['de': 'Url', 'en': 'Url']
    ]

    /**
     * Factory method to create a new cost information definition
     * @param map the configuration {@link Map} containing the parameters of the new cost information
     * @return the new cost information definition object
     */
    static CostInformationDefinition construct(Map<String, Object> map) {

        withTransaction {
            String token    = map.get('token') // name
            String type     = map.get('type')
            String rdc      = map.get('rdc') // refdataCategory

            boolean hardData    = Boolean.parseBoolean(map.get('hardData') as String)

            if (! validTypes.containsKey( type )) {
                log.error("Provided cost information type ${type} is not valid. Allowed types are ${validTypes}")
                throw new UnexpectedTypeException()
            }

            Org tenant = map.get('tenant') ? Org.findByGlobalUID(map.get('tenant')) : null
            Map i10n = map.get('i10n') ?: [
                    name_de: token,
                    name_en: token,
                    expl_de: null,
                    expl_en: null
            ]
            if (map.tenant && !tenant) {
                log.debug('WARNING: tenant not found: ' + map.tenant + ', property "' + token + '" is handled as public')
            }

            CostInformationDefinition cif

            if (tenant) {
                cif = CostInformationDefinition.findByNameAndTenant(token, tenant)
            } else {
                cif = CostInformationDefinition.findByName(token)
            }

            if (!cif) {
                log.debug("INFO: no match found; creating new cost information definition for (${token}, ${type}), tenant: ${tenant}")

                cif = new CostInformationDefinition(
                        name: token,
                        type: type,
                        refdataCategory: rdc,
                        tenant: tenant
                )
            }

            cif.name_de = i10n.get('name_de') ?: null
            cif.name_en = i10n.get('name_en') ?: null

            cif.expl_de = i10n.get('expl_de') ?: null
            cif.expl_en = i10n.get('expl_en') ?: null

            cif.isHardData = hardData
            cif.save()

            cif
        }
    }

    /**
     * Counts how many properties are defined of the current type
     * @return the count how many properties of the current type are attached to the respective objects
     */
    int countUsages() {
        int[] c = executeQuery("select count(*) from CostItem as c where c.costInformationDefinition.id = :type", [type:this.id])
        return c[0]
    }

    /**
     * Counts how many properties are defined of the current type which are owned by the context institution
     * @return the count how many properties of the current type are attached to the respective objects and belonging to the context institution
     */
    int countOwnUsages() {
        String tenantFilter = 'and c.owner.id = :ctx'

        Map<String,Long> filterParams = [type:this.id, ctx: BeanStore.getContextService().getOrg().id]

        int[] c = executeQuery("select count(*) from CostItem as c where c.costInformationDefinition.id = :type " + tenantFilter, filterParams)
        return c[0]
    }

    static String getLocalizedValue(String key){
        String lang = LocaleUtils.getCurrentLang()

        switch (key) {
            case 'java.lang.String': 'Text'
                break
            case 'de.laser.RefdataValue': lang == 'de' ? 'Referenzwert' : 'Refdata'
                break
        }
    }
}
