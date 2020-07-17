package com.k_int.properties

import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated
import de.laser.ContextService
import de.laser.base.AbstractI10nOverride
import de.laser.I10nTranslation
import de.laser.helper.SwissKnife
import grails.util.Holders
import groovy.util.logging.Log4j
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import javax.validation.UnexpectedTypeException

//import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class PropertyDefinition extends AbstractI10nOverride implements Serializable, Comparable<PropertyDefinition> {

    static Log static_logger = LogFactory.getLog(PropertyDefinition)

    @Transient
    final static String CUSTOM_PROPERTY  = "CUSTOM_PROPERTY"
    @Transient
    final static String PRIVATE_PROPERTY = "PRIVATE_PROPERTY"

    @Transient
    final static String LIC_PROP    = 'License Property'
    @Transient
    final static String ORG_PROP    = 'Organisation Property'
    @Transient
    final static String ORG_CONF    = 'Organisation Config'
    @Transient
    final static String PRS_PROP    = 'Person Property'
    @Transient
    final static String PLA_PROP    = 'Platform Property'
    @Transient
    final static String SUB_PROP    = 'Subscription Property'
    @Transient
    final static String SUR_PROP    = 'Survey Property'

    //sorting is for German terms for the next three arrays; I10n is todo for later

    @Transient
    final static String[] AVAILABLE_CUSTOM_DESCR = [
            PRS_PROP,
            ORG_PROP,
            SUB_PROP,
            PLA_PROP,
            SUR_PROP,
            LIC_PROP
    ]
    @Transient
    final static String[] AVAILABLE_PRIVATE_DESCR = [
            PRS_PROP,
            ORG_PROP,
            SUB_PROP,
            PLA_PROP,
            SUR_PROP,
            LIC_PROP
    ]

    @Transient
    final static String[] AVAILABLE_GROUPS_DESCR = [
            SUB_PROP,
            ORG_PROP,
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

    //Map keys can change and they wont affect any of the functionality
    @Deprecated
    @Transient
    static def validTypes = ["Number":  Integer.toString(), 
                             "Text":    String.toString(), 
                             "Refdata": RefdataValue.toString(), 
                             "Decimal": BigDecimal.toString(),
                             "Date":    Date.toString(),
                             "Url":     URL.toString()]

    @Transient
    static def validTypes2 = [
            'class java.lang.Integer'             : ['de': 'Ganzzahl', 'en': 'Number'],
            'class java.lang.String'              : ['de': 'Text', 'en': 'Text'],
            'class com.k_int.kbplus.RefdataValue' : ['de': 'Referenzwert', 'en': 'Refdata'],
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
        tenant              (nullable: true,  blank: true)
        multipleOccurrence  (nullable: true,  blank: true)
        mandatory           (blank: false)
        isHardData          (blank: false)
        isUsedForLogic      (blank: false)
        lastUpdated         (nullable: true, blank: false)
        dateCreated         (nullable: true, blank: false)
    }

    static PropertyDefinition construct(Map<String, Object> map) {

        String token        = map.get('token') // name
        String category     = map.get('category') // descr
        String type         = map.get('type')
        String rdc          = map.get('rdc') // refdataCategory
        boolean hardData    = new Boolean( map.get('hardData') )
        boolean mandatory   = new Boolean( map.get('mandatory') )
        boolean multiple    = new Boolean( map.get('multiple') )
        boolean logic       = new Boolean( map.get('logic') )
        Org tenant          = map.get('tenant') ? Org.findByGlobalUID(map.get('tenant')) : null
        Map i10n            = map.get('i10n')  ?: [
                name_de: token,
                name_en: token,
                //descr_de: category,
                //descr_en: category,
                expl_de: null,
                expl_en: null
        ]

        typeIsValid(type)

        if (map.tenant && !tenant) {
            static_logger.debug('WARNING: tenant not found: ' + map.tenant + ', property "' + token + '" is handled as public')
        }

        PropertyDefinition pd

        if (tenant) {
            pd = PropertyDefinition.getByNameAndDescrAndTenant(token, category, tenant)
        }
        else {
            pd = PropertyDefinition.getByNameAndDescr(token, category)
        }

        if (! pd) {
            static_logger.debug("INFO: no match found; creating new property definition for (${token}, ${category}, ${type}), tenant: ${tenant}")

            boolean multipleOccurrence = (category == PropertyDefinition.SUR_PROP) ? false : multiple

            pd = new PropertyDefinition(
                    name:               token,
                    descr:              category,
                    type:               type,
                    refdataCategory:    rdc,
                    multipleOccurrence: multipleOccurrence,
                    mandatory:          mandatory,
                    isUsedForLogic:     logic,
                    tenant:             tenant
            )

            // TODO .. which attributes can change for existing pds ?
        }

        pd.name_de = i10n.get('name_de') ?: null
        pd.name_en = i10n.get('name_en') ?: null

        pd.expl_de = i10n.get('expl_de') ?: null
        pd.expl_en = i10n.get('expl_en') ?: null

        pd.isHardData = hardData
        pd.save(flush: true)

        // I10nTranslation.createOrUpdateI10n(pd, 'descr', descr)

        pd
    }

    static PropertyDefinition getByNameAndDescr(String name, String descr) {

        List result = PropertyDefinition.findAllByNameIlikeAndDescrAndTenantIsNull(name, descr)

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

        List result = PropertyDefinition.findAllByNameIlikeAndDescrAndTenant(name, descr, tenant)

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
        findAllByDescrAndTenantIsNull(descr)
    }

    static List<PropertyDefinition> getAllByDescrAndTenant(String descr, Org tenant) {
        findAllByDescrAndTenant(descr, tenant)
    }

    static List<PropertyDefinition> getAllByDescrAndMandatory(String descr, boolean mandatory) {
        findAllByDescrAndMandatoryAndTenantIsNull(descr, mandatory)
    }

    static List<PropertyDefinition> getAllByDescrAndMandatoryAndTenant(String descr, boolean mandatory, Org tenant) {
        findAllByDescrAndMandatoryAndTenant(descr, mandatory, tenant)
    }

    private static def typeIsValid(String key) {
        if (validTypes2.containsKey(key)) {
            return true;
        } else {
            log.error("Provided prop type ${key} is not valid. Allowed types are ${validTypes2}")
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
        String classString = owner.getClass().toString()
        def ownerClassName = classString.substring(classString.lastIndexOf(".") + 1)
        boolean isPublic

        //if(!owner.hasProperty("privateProperties")) {
            ownerClassName = "com.k_int.kbplus.${ownerClassName}Property"
            if (flag == PropertyDefinition.CUSTOM_PROPERTY) {
                isPublic = true
            }
            else if (flag == PropertyDefinition.PRIVATE_PROPERTY) {
                isPublic = false
            }
        /*}
        else {
            if (flag == PropertyDefinition.CUSTOM_PROPERTY) {
                ownerClassName = "com.k_int.kbplus.${ownerClassName}CustomProperty"
            }
            else if (flag == PropertyDefinition.PRIVATE_PROPERTY) {
                ownerClassName = "com.k_int.kbplus.${ownerClassName}PrivateProperty"
            }
        }*/

        //def newProp = Class.forName(ownerClassName).newInstance(type: type, owner: owner)
        def newProp = (new GroovyClassLoader()).loadClass(ownerClassName).newInstance(type: type, owner: owner, isPublic: isPublic, tenant: contextOrg)
        newProp.setNote("")

        /*
        if (flag == PropertyDefinition.CUSTOM_PROPERTY) {
            owner.customProperties.add(newProp)
        }
        else if (flag == PropertyDefinition.PRIVATE_PROPERTY) {
            owner.privateProperties.add(newProp)
        } */

        newProp.save(flush:true)
        GrailsHibernateUtil.unwrapIfProxy(newProp)
    }

    static def refdataFind(params) {
        def result = []
        def propDefsInCalcGroups = []

        if (params.oid) {
            GenericOIDService genericOIDService = (GenericOIDService) Holders.grailsApplication.mainContext.getBean('genericOIDService')
            def obj = genericOIDService.resolveOID(params.oid)

            if (obj) {
                ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
                Map<String, Object> calcPropDefGroups = obj.getCalculatedPropDefGroups(contextService.getOrg())
                propDefsInCalcGroups = SwissKnife.getCalculatedPropertiesForPropDefGroups(calcPropDefGroups)
            }
        }

        List<PropertyDefinition> matches = []

        switch (I10nTranslation.decodeLocale(LocaleContextHolder.getLocale().toString())) {
            case 'en':
                matches = PropertyDefinition.findAllByDescrAndName_enIlike(params.desc, "%${params.q}%")
                break
            case 'de':
                matches = PropertyDefinition.findAllByDescrAndName_deIlike(params.desc, "%${params.q}%")
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

            result = Class.forName('com.k_int.kbplus.' + parts[0])?.name
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
        String table = this.descr.minus('com.k_int.kbplus.').replace(" ","")
        if(this.descr == "Organisation Property")
            table = "OrgProperty"

        if (table) {
            int[] c = executeQuery("select count(c) from " + table + " as c where c.type.id = :type", [type:this.id])
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
    List<PropertyDefinition> qparams = [this]
    String qry = 'select c.owner from ' + cls + " as c where c.type = ?"
    return PropertyDefinition.executeQuery(qry,qparams); 
  }

  @Transient
  def countOccurrences(String cls) {
    List<PropertyDefinition> qparams = [this]
    String qry = 'select count(c) from ' + cls + " as c where c.type = ?"
    return (PropertyDefinition.executeQuery(qry,qparams))[0]; 
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
        log.debug("Remove")
        PropertyDefinition.executeUpdate('delete from com.k_int.kbplus.LicenseProperty c where c.type = ?', [this])
        PropertyDefinition.executeUpdate('delete from com.k_int.kbplus.SubscriptionProperty c where c.type = ?', [this])
        PropertyDefinition.executeUpdate('delete from com.k_int.kbplus.OrgProperty c where c.type = ?', [this])
        PropertyDefinition.executeUpdate('delete from com.k_int.kbplus.PersonProperty c where c.type = ?', [this])
        this.delete();
    }

    /* tmp only */
    static Map<String, Object> getAvailablePropertyDescriptions() {
        return [
                "com.k_int.kbplus.Org"      : PropertyDefinition.ORG_PROP,
                "com.k_int.kbplus.License"  : PropertyDefinition.LIC_PROP,
                "com.k_int.kbplus.Person"   : PropertyDefinition.PRS_PROP
        ]
    }

    static getLocalizedValue(key){
        String locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale().toString())

        //println locale
        if (PropertyDefinition.validTypes2.containsKey(key)) {
            return (PropertyDefinition.validTypes2.get(key)."${locale}") ?: PropertyDefinition.validTypes2.get(key)
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
}

