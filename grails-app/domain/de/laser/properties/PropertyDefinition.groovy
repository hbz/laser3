package de.laser.properties

import com.k_int.kbplus.GenericOIDService
import de.laser.License
import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.SurveyResult
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.base.AbstractI10n
import de.laser.helper.LocaleHelper
import de.laser.storage.BeanStore
import de.laser.interfaces.CalculatedType
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import grails.web.servlet.mvc.GrailsParameterMap

import javax.persistence.Transient
import javax.validation.UnexpectedTypeException

//import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This is the class reflecting the type of a Subscription/License/Org/Platform/PersonProperty. This is not the type of value the property can contain, see {@link AbstractPropertyWithCalculatedLastUpdated} for the value types!
 * Property definitions may be general (the properties of such types are thus general, sometimes called custom) or private (properties of such types are private).
 * The difference between general/custom and private properties is that general properties may be visible by every organisation accessing the owner object (their visibility is configurable) whereas private ones are viewable
 * only by the tenant {@link Org}.
 * Important note: private property definitions may be defined by each tenant individually by frontend;
 * general property definitions are hard-coded (/src/main/webapp/setup/PropertyDefinition.csv) as they should be instance-independent and survive database resets. This is the same procedure as with reference data categories
 * and their values
 * @see SubscriptionProperty
 * @see LicenseProperty
 * @see OrgProperty
 * @see PlatformProperty
 * @see PersonProperty
 * @see AbstractPropertyWithCalculatedLastUpdated
 */
@Slf4j
class PropertyDefinition extends AbstractI10n implements Serializable, Comparable<PropertyDefinition> {

    /**
     * general property viewable by everyone if public flag is true
     */
    final static String CUSTOM_PROPERTY  = "CUSTOM_PROPERTY"
    /**
     * private property; view and use restricted to the tenant organisation
     */
    final static String PRIVATE_PROPERTY = "PRIVATE_PROPERTY"

    final static String LIC_PROP    = 'License Property'
    final static String ORG_PROP    = 'Organisation Property'
    final static String ORG_CONF    = 'Organisation Config'
    final static String PRS_PROP    = 'Person Property'
    final static String PLA_PROP    = 'Platform Property'
    final static String SUB_PROP    = 'Subscription Property'
    final static String SVY_PROP    = 'Survey Property'

    //sorting is for German terms for the next three arrays; I10n is todo for later

    @Transient
    final static String[] AVAILABLE_CUSTOM_DESCR = [
            PRS_PROP,
            SUB_PROP,
            ORG_PROP,
            PLA_PROP,
            SVY_PROP,
            LIC_PROP
    ]
    @Transient
    final static String[] AVAILABLE_PRIVATE_DESCR = [
            PRS_PROP,
            SUB_PROP,
            ORG_PROP,
            PLA_PROP,
            SVY_PROP,
            LIC_PROP
    ]

    @Transient
    final static String[] AVAILABLE_GROUPS_DESCR = [
            ORG_PROP,
            SUB_PROP,
            PLA_PROP,
            LIC_PROP
    ]

    String name
    String name_de
    String name_en

    String expl_de
    String expl_en

    String descr
    String type
    String refdataCategory

    /**
     * used for private properties; marks the owner who can define and see properties of this type
     */
    Org tenant

    /**
     * allows multiple occurences within an object
     */
    boolean multipleOccurrence = false
    /**
     * mandatory, i.e. the property must exist in the object
     */
    boolean mandatory = false
    /**
     * indicates this object is created via current bootstrap (=> defined in /src/mail/webapp/setup/PropertyDefinition.csv)
     */
    boolean isHardData = false
    /**
     * indicates hard coded logic, i.e. the property is relevant for app behavior
     */
    boolean isUsedForLogic = false

    Date dateCreated
    Date lastUpdated

//    @Transient
//    def contextService

    @Transient
    static def validTypes = [
            'java.util.Date'                : ['de': 'Datum', 'en': 'Date'],
            'java.math.BigDecimal'          : ['de': 'Dezimalzahl', 'en': 'Decimal'],
            'java.lang.Integer'             : ['de': 'Ganzzahl', 'en': 'Number' ],
            'de.laser.RefdataValue'         : ['de': 'Referenzwert', 'en': 'Refdata'],
            'java.lang.String'              : ['de': 'Text', 'en': 'Text'],
            'java.net.URL'                  : ['de': 'Url', 'en': 'Url']
    ]

    static hasMany = [
            propDefGroupItems: PropertyDefinitionGroupItem
    ]
    static mappedBy = [
            propDefGroupItems: 'propDef'
    ]

    static transients = ['descrClass', 'bigDecimalType', 'dateType', 'integerType', 'refdataValueType', 'stringType', 'URLType', 'implClass', 'implClassValueProperty'] // mark read-only accessor methods

    static mapping = {
                    cache  true
                      id column: 'pd_id'
                   descr column: 'pd_description', index: 'td_new_idx', type: 'text'
                    name column: 'pd_name',        index: 'td_new_idx'
                 name_de column: 'pd_name_de'
                 name_en column: 'pd_name_en'
                 expl_de column: 'pd_explanation_de', type: 'text'
                 expl_en column: 'pd_explanation_en', type: 'text'
                    type column: 'pd_type',        index: 'td_type_idx'
         refdataCategory column: 'pd_rdc',         index: 'td_type_idx'
                  tenant column: 'pd_tenant_fk',   index: 'pd_tenant_idx'
      multipleOccurrence column: 'pd_multiple_occurrence'
               mandatory column: 'pd_mandatory'
                isHardData column: 'pd_hard_data'
          isUsedForLogic column: 'pd_used_for_logic'
                      sort name: 'desc'
        lastUpdated     column: 'pd_last_updated'
        dateCreated     column: 'pd_date_created'

        propDefGroupItems cascade: 'all', batchSize: 10
    }

    static constraints = {
        name                (blank: false)
        name_de             (nullable: true, blank: false)
        name_en             (nullable: true, blank: false)
        expl_de             (nullable: true, blank: false)
        expl_en             (nullable: true, blank: false)
        descr               (nullable: true,  blank: false)
        type                (blank: false)
        refdataCategory     (nullable: true)
        tenant              (nullable: true)
        lastUpdated         (nullable: true)
        dateCreated         (nullable: true)
    }

    /**
     * Factory method to create a new property definition
     * @param map the configuration {@link Map} containing the parameters of the new property
     * @return the new property definition object
     */
    static PropertyDefinition construct(Map<String, Object> map) {

        withTransaction {
            String token    = map.get('token') // name
            String category = map.get('category') // descr
            String type     = map.get('type')
            String rdc      = map.get('rdc') // refdataCategory

            boolean hardData    = new Boolean(map.get('hardData'))
            boolean mandatory   = new Boolean(map.get('mandatory'))
            boolean multiple    = new Boolean(map.get('multiple'))
            boolean logic       = new Boolean(map.get('logic'))

            Org tenant          = map.get('tenant') ? Org.findByGlobalUID(map.get('tenant')) : null
            Map i10n = map.get('i10n') ?: [
                    name_de: token,
                    name_en: token,
                    //descr_de: category,
                    //descr_en: category,
                    expl_de: null,
                    expl_en: null
            ]

            PropertyDefinition.typeIsValid(type)

            if (map.tenant && !tenant) {
                log.debug('WARNING: tenant not found: ' + map.tenant + ', property "' + token + '" is handled as public')
            }

            PropertyDefinition pd

            if (tenant) {
                pd = PropertyDefinition.getByNameAndDescrAndTenant(token, category, tenant)
            } else {
                pd = PropertyDefinition.getByNameAndDescr(token, category)
            }

            if (!pd) {
                log.debug("INFO: no match found; creating new property definition for (${token}, ${category}, ${type}), tenant: ${tenant}")

                boolean multipleOccurrence = (category == PropertyDefinition.SVY_PROP) ? false : multiple

                pd = new PropertyDefinition(
                        name: token,
                        descr: category,
                        type: type,
                        refdataCategory: rdc,
                        multipleOccurrence: multipleOccurrence,
                        mandatory: mandatory,
                        isUsedForLogic: logic,
                        tenant: tenant
                )

                // TODO .. which attributes can change for existing pds ?
            }

            pd.name_de = i10n.get('name_de') ?: null
            pd.name_en = i10n.get('name_en') ?: null

            pd.expl_de = i10n.get('expl_de') ?: null
            pd.expl_en = i10n.get('expl_en') ?: null

            pd.isHardData = hardData
            pd.save()

            // I10nTranslation.createOrUpdateI10n(pd, 'descr', descr)

            pd
        }
    }

    /**
     * Retrieves a general property definition by its name and object type (description)
     * @param name the name of the property definition being searched
     * @param descr the object type - one of the constants {@link #SUB_PROP}, {@link #LIC_PROP}, {@link #ORG_PROP}, {@link #PRS_PROP}, {@link #PLA_PROP} or {@link #SVY_PROP}
     * @return a property definition; if multiple matches are found, a warning is generated and the first result is being returned
     */
    static PropertyDefinition getByNameAndDescr(String name, String descr) {

        List<PropertyDefinition> result = PropertyDefinition.findAllByNameIlikeAndDescrAndTenantIsNull(name, descr)

        if (result.size() == 0) {
            return null
        }
        else if (result.size() == 1) {
            return result[0]
        }
        else {
            log.debug("WARNING: multiple matches found ( ${name}, ${descr}, tenant is null )")
            return result[0]
        }
    }

    /**
     * Retrieves a private property definition by its name, object type (description) and tenant
     * @param name the name of the property definition being searched
     * @param descr the object type - one of the constants {@link #SUB_PROP}, {@link #LIC_PROP}, {@link #ORG_PROP}, {@link #PRS_PROP}, {@link #PLA_PROP} or {@link #SVY_PROP}
     * @param tenant the {@link Org} whose property definition should be retrieved
     * @return a property definition; if multiple matches are found, a warning is generated and the first result is being returned
     */
    static PropertyDefinition getByNameAndDescrAndTenant(String name, String descr, Org tenant) {

        List<PropertyDefinition> result = PropertyDefinition.findAllByNameIlikeAndDescrAndTenant(name, descr, tenant)

        if (result.size() == 0) {
            return null
        }
        else if (result.size() == 1) {
            return result[0]
        }
        else {
            log.debug("WARNING: multiple matches found ( ${name}, ${descr}, ${tenant.id} )")
            return result[0]
        }
    }

    /**
     * Retrieves all
     * @param descr
     * @return
     */
    static List<PropertyDefinition> getAllByDescr(String descr) {
        PropertyDefinition.findAllByDescrAndTenantIsNull(descr)
    }

    static List<PropertyDefinition> getAllByDescrAndTenant(String descr, Org tenant) {
        PropertyDefinition.findAllByDescrAndTenant(descr, tenant)
    }

    static List<PropertyDefinition> getAllByDescrAndMandatory(String descr, boolean mandatory) {
        PropertyDefinition.findAllByDescrAndMandatoryAndTenantIsNull(descr, mandatory)
    }

    static List<PropertyDefinition> getAllByDescrAndMandatoryAndTenant(String descr, boolean mandatory, Org tenant) {
        PropertyDefinition.findAllByDescrAndMandatoryAndTenant(descr, mandatory, tenant)
    }

    private static def typeIsValid(String key) {
        if (validTypes.containsKey(key)) {
            return true;
        } else {
            log.error("Provided prop type ${key} is not valid. Allowed types are ${validTypes}")
            throw new UnexpectedTypeException()
        }
    }

    /**
     * Called from AjaxController.addCustomPropertyValue()
     * Called from AjaxController.addPrivatePropertyValue()
     *
     * @param owner: The class that will hold the property, e.g License
     */
    static AbstractPropertyWithCalculatedLastUpdated createGenericProperty(String flag, def owner, PropertyDefinition type, Org contextOrg) {

        withTransaction {
            String classString = owner.class.name
            String ownerClassName = classString.substring(classString.lastIndexOf(".") + 1)
            boolean isPublic = false
            if(owner instanceof Subscription)
                isPublic = owner._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && owner.getConsortia()?.id == contextOrg.id
            else if(owner instanceof License)
                isPublic = owner._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && owner.getLicensingConsortium()?.id == contextOrg.id
            ownerClassName = "de.laser.properties.${ownerClassName}Property"

            def newProp = (new GroovyClassLoader()).loadClass(ownerClassName).newInstance(type: type, owner: owner, isPublic: isPublic, tenant: contextOrg)
            newProp.setNote("")

            newProp.save()
            (AbstractPropertyWithCalculatedLastUpdated) GrailsHibernateUtil.unwrapIfProxy(newProp)
        }
    }

    static def refdataFind(GrailsParameterMap params) {
        List<Map<String, Object>> result = []
        List<PropertyDefinition> propDefsInCalcGroups = []

        if (params.oid) {
            GenericOIDService genericOIDService = BeanStore.getGenericOIDService()
            def obj = genericOIDService.resolveOID(params.oid)

            if (obj) {
                Map<String, Object> calcPropDefGroups = obj.getCalculatedPropDefGroups(BeanStore.getContextService().getOrg())

                calcPropDefGroups.global.each { it ->
                    List<PropertyDefinition> tmp = it.getPropertyDefinitions()
                    propDefsInCalcGroups.addAll(tmp)
                }
                calcPropDefGroups.local.each {it ->
                    List<PropertyDefinition> tmp = it[0].getPropertyDefinitions()
                    propDefsInCalcGroups.addAll(tmp)
                }
                calcPropDefGroups.member.each {it ->
                    List<PropertyDefinition> tmp = it[0].getPropertyDefinitions()
                    propDefsInCalcGroups.addAll(tmp)
                }

                if (calcPropDefGroups.orphanedProperties) {
                    propDefsInCalcGroups.addAll(calcPropDefGroups.orphanedProperties)
                }

                propDefsInCalcGroups.unique()
            }
        }

        List<PropertyDefinition> matches = []

        switch (LocaleHelper.getCurrentLang()) {
            case 'en':
                String query = "select pd from PropertyDefinition pd where pd.descr = :descr and lower(pd.name_en) like :name"
                matches = PropertyDefinition.executeQuery( query, [descr: params.desc, name: "%${params.q.toLowerCase()}%"])
                break
            case 'de':
                String query = "select pd from PropertyDefinition pd where pd.descr = :descr and lower(pd.name_de) like :name"
                matches = PropertyDefinition.executeQuery( query, [descr: params.desc, name: "%${params.q.toLowerCase()}%"])
                break
        }

        int c1 = matches.size()
        matches.removeAll(propDefsInCalcGroups)
        int c2 = matches.size()

        matches.each { it ->
            if (params.tenant.equals(it.getTenant()?.id?.toString())) {
                result.add([id: "${it.id}", text: "${it.getI10n('name')}"])
            }
        }

        log.debug("found property definitions: ${c1} -> ${c2} -> ${result.size()}")

        result
    }

    String getDescrClass() {
        getDescrClass(this.descr)
    }

    static String getDescrClass(String descr) {
        String result
        String[] parts = descr.split(" ")

        if (parts.size() >= 2) {
            if (parts[0] == "Organisation") {
                parts[0] = "Org"
            }
            try {
                result = Class.forName('de.laser.' + parts[0])?.name
            } catch(Exception e) {
            }
        }
        result
    }

    String getImplClass() {
        if(descr.contains("Organisation"))
            OrgProperty.class.name
        else if (descr.contains("Survey"))
            SurveyResult.class.name
        else 'de.laser.properties.'+descr.replace(" ","")
    }

    int countUsages() {
        String table = this.descr.replace(" ","")
        if(this.descr == PropertyDefinition.ORG_PROP) {
            table = "OrgProperty"
        } else if(this.descr == PropertyDefinition.SVY_PROP)
            table = "SurveyResult"

        if (table) {
            int[] c = executeQuery("select count(c) from " + table + " as c where c.type.id = :type", [type:this.id])
            return c[0]
        }
        return 0
    }

    int countOwnUsages() {
        String table = this.descr.replace(" ","")
        String tenantFilter = 'and c.tenant.id = :ctx'
        Map<String,Long> filterParams = [type:this.id,ctx:BeanStore.getContextService().getOrg().id]
        if (this.descr == PropertyDefinition.ORG_PROP) {
            table = "OrgProperty"
        } else if(this.descr == PropertyDefinition.SVY_PROP) {
            table = "SurveyResult"
            tenantFilter = ''
            filterParams.remove("ctx")
        }

        if (table) {
            int[] c = executeQuery("select count(c) from " + table + " as c where c.type.id = :type "+tenantFilter, filterParams)
            return c[0]
        }
        return 0
    }


  @Transient
  def getOccurrencesOwner(String[] cls){
    def all_owners = []
    cls.each{
        all_owners.add(getOccurrencesOwner(it)) 
    }
    return all_owners
  }

  @Transient
  def getOccurrencesOwner(String cls){
    String qry = 'select c.owner from ' + cls + " as c where c.type = :type"
    return PropertyDefinition.executeQuery(qry, [type: this])
  }

  @Transient
  def countOccurrences(String cls) {
    String qry = 'select count(c) from ' + cls + " as c where c.type = :type"
    return (PropertyDefinition.executeQuery(qry, [type: this]))[0] ?: 0
  }

  @Transient
  int countOccurrences(String[] cls){
    int total_count = 0
    cls.each{
        total_count += countOccurrences(it)
    }
    return total_count
  }

    @Transient
    void removeProperty() {
        log.debug("removeProperty")

        withTransaction {
            PropertyDefinition.executeUpdate('delete from LicenseProperty c where c.type = :self', [self: this])
            PropertyDefinition.executeUpdate('delete from OrgProperty c where c.type = :self', [self: this])
            PropertyDefinition.executeUpdate('delete from PersonProperty c where c.type = :self', [self: this])
            PropertyDefinition.executeUpdate('delete from PlatformProperty c where c.type = :self', [self: this])
            PropertyDefinition.executeUpdate('delete from SubscriptionProperty c where c.type = :self', [self: this])
            PropertyDefinition.executeUpdate('delete from SurveyResult c where c.type = :self', [self: this])

            this.delete()
        }
    }

    static String getLocalizedValue(String key){
        String locale = LocaleHelper.getCurrentLang()

        if (PropertyDefinition.validTypes.containsKey(key)) {
            return (PropertyDefinition.validTypes.get(key)."${locale}") ?: PropertyDefinition.validTypes.get(key)
        } else {
            return null
        }
    }

    static List<PropertyDefinition> findAllPublicAndPrivateOrgProp(Org contextOrg){
        PropertyDefinition.findAll( "from PropertyDefinition as pd where pd.descr in :defList and (pd.tenant is null or pd.tenant = :tenant) order by pd.name_de asc", [
                        defList: [PropertyDefinition.ORG_PROP],
                        tenant: contextOrg
                    ])
    }

    static List<PropertyDefinition> findAllPublicAndPrivateProp(List propertyDefinitionList, Org contextOrg){
        PropertyDefinition.findAll( "from PropertyDefinition as pd where pd.descr in :defList and (pd.tenant is null or pd.tenant = :tenant) order by pd.name_de asc", [
                        defList: propertyDefinitionList,
                        tenant: contextOrg
                    ])
    }

    int compareTo(PropertyDefinition pd) {
        String a = this.getI10n('name') ?:''
        String b = pd.getI10n('name') ?:''
        return a.toLowerCase()?.compareTo(b.toLowerCase())
    }

    boolean isBigDecimalType() {
        type == BigDecimal.class.name
    }
    boolean isDateType() {
        type == Date.class.name
    }
    boolean isIntegerType() {
        type == Integer.class.name
    }
    boolean isRefdataValueType() {
        type == RefdataValue.class.name
    }
    boolean isStringType() {
        type == String.class.name
    }
    boolean isURLType() {
        type == URL.class.name
    }

    String getImplClassValueProperty(){
        if( isIntegerType() )   { return "intValue" }
        if( isStringType() )    { return "stringValue" }
        if( isBigDecimalType() ){ return "decValue" }
        if( isDateType() )      { return "dateValue" }
        if( isURLType() )       { return "urlValue" }
        if( isRefdataValueType()) { return "refValue"}
    }
}

