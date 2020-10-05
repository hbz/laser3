package de.laser.properties

import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import de.laser.RefdataValue
import com.k_int.kbplus.Subscription
import de.laser.ContextService
import de.laser.I10nTranslation
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.base.AbstractI10n
import de.laser.helper.SwissKnife
import de.laser.interfaces.CalculatedType
import grails.util.Holders
import groovy.util.logging.Log4j
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import javax.validation.UnexpectedTypeException

//import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class PropertyDefinition extends AbstractI10n implements Serializable, Comparable<PropertyDefinition> {

    static Log static_logger = LogFactory.getLog(PropertyDefinition)

    final static String CUSTOM_PROPERTY  = "CUSTOM_PROPERTY"
    final static String PRIVATE_PROPERTY = "PRIVATE_PROPERTY"

    final static String LIC_PROP    = 'License Property'
    final static String ORG_PROP    = 'Organisation Property'
    final static String ORG_CONF    = 'Organisation Config'
    final static String PRS_PROP    = 'Person Property'
    final static String PLA_PROP    = 'Platform Property'
    final static String SUB_PROP    = 'Subscription Property'
    final static String SUR_PROP    = 'Survey Property'

    //sorting is for German terms for the next three arrays; I10n is todo for later

    @Transient
    final static String[] AVAILABLE_CUSTOM_DESCR = [
            PRS_PROP,
            SUB_PROP,
            ORG_PROP,
            PLA_PROP,
            SUR_PROP,
            LIC_PROP
    ]
    @Transient
    final static String[] AVAILABLE_PRIVATE_DESCR = [
            PRS_PROP,
            SUB_PROP,
            ORG_PROP,
            PLA_PROP,
            SUR_PROP,
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

    // used for private properties
    Org tenant

    // allows multiple occurences
    boolean multipleOccurrence = false
    // mandatory
    boolean mandatory = false
    // indicates this object is created via current bootstrap
    boolean isHardData = false
    // indicates hard coded logic
    boolean isUsedForLogic = false

    Date dateCreated
    Date lastUpdated

    @Transient
    def contextService

    @Transient
    static def validTypes = [
            'class java.lang.Integer'             : ['de': 'Ganzzahl', 'en': 'Number' ],
            'class java.lang.String'              : ['de': 'Text', 'en': 'Text'],
            'class de.laser.RefdataValue'         : ['de': 'Referenzwert', 'en': 'Refdata'],
            'class java.math.BigDecimal'          : ['de': 'Dezimalzahl', 'en': 'Decimal'],
            'class java.util.Date'                : ['de': 'Datum', 'en': 'Date'],
            'class java.net.URL'                  : ['de': 'Url', 'en': 'Url']
    ]

    static hasMany = [
            propDefGroupItems: PropertyDefinitionGroupItem
    ]
    static mappedBy = [
            propDefGroupItems: 'propDef'
    ]

    static transients = ['descrClass', 'bigDecimalType', 'dateType', 'integerType', 'refdataValueType', 'stringType', 'URLType', 'implClassValueProperty'] // mark read-only accessor methods

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
                static_logger.debug('WARNING: tenant not found: ' + map.tenant + ', property "' + token + '" is handled as public')
            }

            PropertyDefinition pd

            if (tenant) {
                pd = PropertyDefinition.getByNameAndDescrAndTenant(token, category, tenant)
            } else {
                pd = PropertyDefinition.getByNameAndDescr(token, category)
            }

            if (!pd) {
                static_logger.debug("INFO: no match found; creating new property definition for (${token}, ${category}, ${type}), tenant: ${tenant}")

                boolean multipleOccurrence = (category == PropertyDefinition.SUR_PROP) ? false : multiple

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

    static PropertyDefinition getByNameAndDescr(String name, String descr) {

        List<PropertyDefinition> result = PropertyDefinition.findAllByNameIlikeAndDescrAndTenantIsNull(name, descr)

        if (result.size() == 0) {
            return null
        }
        else if (result.size() == 1) {
            return result[0]
        }
        else {
            static_logger.debug("WARNING: multiple matches found ( ${name}, ${descr}, tenant is null )")
            return result[0]
        }
    }

    static PropertyDefinition getByNameAndDescrAndTenant(String name, String descr, Org tenant) {

        List<PropertyDefinition> result = PropertyDefinition.findAllByNameIlikeAndDescrAndTenant(name, descr, tenant)

        if (result.size() == 0) {
            return null
        }
        else if (result.size() == 1) {
            return result[0]
        }
        else {
            static_logger.debug("WARNING: multiple matches found ( ${name}, ${descr}, ${tenant.id} )")
            return result[0]
        }
    }

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
            String classString = owner.getClass().toString() // TODO [ticket=2880]
            String ownerClassName = classString.substring(classString.lastIndexOf(".") + 1)
            boolean isPublic = false
            if(owner instanceof Subscription)
                isPublic = owner._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && owner.getConsortia()?.id == contextOrg.id
            else if(owner instanceof License)
                isPublic = owner._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && owner.getLicensingConsortium()?.id == contextOrg.id
            ownerClassName = "com.k_int.kbplus.${ownerClassName}Property"

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
            GenericOIDService genericOIDService = (GenericOIDService) Holders.grailsApplication.mainContext.getBean('genericOIDService')
            def obj = genericOIDService.resolveOID(params.oid)

            if (obj) {
                ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
                Map<String, Object> calcPropDefGroups = obj._getCalculatedPropDefGroups(contextService.getOrg())
                propDefsInCalcGroups = SwissKnife.getCalculatedPropertiesForPropDefGroups(calcPropDefGroups)
            }
        }

        List<PropertyDefinition> matches = []

        switch (I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())) {
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

        static_logger.debug("found property definitions: ${c1} -> ${c2} -> ${result.size()}")

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
            List<String> fq = ['de.laser.', 'com.k_int.kbplus.']
            fq.each {
                try {
                    if (! result) {
                        result = Class.forName(it + parts[0])?.name
                    }
                } catch(Exception e) {
                }
            }
        }
        result
    }

    @Deprecated
    String getImplClass(String customOrPrivate) {
        getImplClass(this.descr, customOrPrivate)
    }

    @Deprecated
    static String getImplClass(String descr, String customOrPrivate) {
        String result
        String[] parts = descr.split(" ")

        if (parts.size() >= 2) {
            if (parts[0] == "Organisation") {
                parts[0] = "Org"
            }
            String cp = 'com.k_int.kbplus.' + parts[0] + 'CustomProperty'
            String pp = 'com.k_int.kbplus.' + parts[0] + 'PrivateProperty'

            try {
                if (customOrPrivate.equalsIgnoreCase('custom') && Class.forName(cp)) {
                    result = cp
                }
                if (customOrPrivate.equalsIgnoreCase('private') && Class.forName(pp)) {
                    result = pp
                }
            } catch (Exception e) {

            }
        }
        result
    }

    int countUsages() {
        String table = this.descr.minus('com.k_int.kbplus.').minus('de.laser.').replace(" ","")
        if(this.descr == "Organisation Property")
            table = "OrgProperty"

        if (table) {
            int[] c = executeQuery("select count(c) from " + table + " as c where c.type.id = :type", [type:this.id])
            return c[0]
        }
        return 0
    }

    int countOwnUsages() {
        String table = this.descr.minus('com.k_int.kbplus.').minus('de.laser.').replace(" ","")
        String tenantFilter = 'and c.tenant.id = :ctx'
        Map<String,Long> filterParams = [type:this.id,ctx:contextService.org.id]
        if(this.descr == "Organisation Property")
            table = "OrgProperty"
        else if(this.descr == "Survey Property") {
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
        String locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())

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
        type == BigDecimal.toString() // introduced for refactoring -> BigDecimal.class.name
    }
    boolean isDateType() {
        type == Date.toString() // introduced for refactoring -> Date.class.name
    }
    boolean isIntegerType() {
        type == Integer.toString() // introduced for refactoring -> Integer.class.name
    }
    boolean isRefdataValueType() {
        type == RefdataValue.toString() // introduced for refactoring -> RefdataValue.class.name
    }
    boolean isStringType() {
        type == String.toString() // introduced for refactoring -> String.class.name
    }
    boolean isURLType() {
        type == URL.toString() // introduced for refactoring -> URL.class.name
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

