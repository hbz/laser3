package com.k_int.properties

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.abstract_domain.AbstractProperty
import de.laser.domain.I10nTranslatableAbstract
import de.laser.domain.I10nTranslation
import groovy.util.logging.*
import org.springframework.context.i18n.LocaleContextHolder
import javax.persistence.Transient
import javax.validation.UnexpectedTypeException

@Log4j
class PropertyDefinition extends I10nTranslatableAbstract {

    @Transient
    final static TRUE  = true
    @Transient
    final static FALSE = false

    @Transient
    final static CUSTOM_PROPERTY  = "CUSTOM_PROPERTY"
    @Transient
    final static PRIVATE_PROPERTY = "PRIVATE_PROPERTY"


    @Transient
    final static String LIC_PROP    = 'License Property'
    @Transient
    final static String LIC_OA_PROP = 'License Property: Open Access'
    @Transient
    final static String LIC_ARC_PROP = 'License Property: Archive'
    @Transient
    final static String ORG_CONF    = 'Organisation Config'
    @Transient
    final static String SYS_CONF    = 'System Config'
    @Transient
    final static String PRS_PROP    = 'Person Property'
    @Transient
    final static String ORG_PROP    = 'Organisation Property'

    @Transient
    final static String[] AVAILABLE_CUSTOM_DESCR = [
            LIC_PROP,
            LIC_OA_PROP,
            LIC_ARC_PROP,
            ORG_CONF,
            SYS_CONF,
            PRS_PROP,
            ORG_PROP
    ]
    @Transient
    final static String[] AVAILABLE_PRIVATE_DESCR = [
            LIC_PROP,
            PRS_PROP,
            ORG_PROP
    ]

    String name
    String descr
    String type
    String refdataCategory

    // used for private properties
    Org tenant

    // allows multiple occurences
    boolean multipleOccurrence
    // mandatory
    boolean mandatory
    // indicates this object is created via front-end and still not hard coded in bootstrap.groovy
    boolean softData

    //Map keys can change and they wont affect any of the functionality
    @Transient
    static def validTypes = ["Number":  Integer.toString(), 
                             "Text":    String.toString(), 
                             "Refdata": RefdataValue.toString(), 
                             "Decimal": BigDecimal.toString(),
                             "Date":    Date.toString()]

    static mapping = {
                      id column: 'pd_id'
                   descr column: 'pd_description', index: 'td_new_idx'
                    name column: 'pd_name',        index: 'td_new_idx'
                    type column: 'pd_type',        index: 'td_type_idx'
         refdataCategory column: 'pd_rdc',         index: 'td_type_idx'
                  tenant column: 'pd_tenant_fk'
      multipleOccurrence column: 'pd_multiple_occurrence'
               mandatory column: 'pd_mandatory'
                softData column: 'pd_soft_data'
                      sort name: 'desc'
    }

    static constraints = {
        name                (nullable: false, blank: false)
        descr               (nullable: true,  blank: false)
        type                (nullable: false, blank: false)
        refdataCategory     (nullable: true)
        tenant              (nullable: true,  blank: true)
        multipleOccurrence  (nullable: true,  blank: true,  default: false)
        mandatory           (nullable: false, blank: false, default: false)
        softData            (nullable: false, blank: false, default: false)
    }

    private static def typeIsValid(value) {
        if (validTypes.containsValue(value)) {
            return true;
        } else {
            log.error("Provided prop type ${value.getClass()} is not valid. Allowed types are ${validTypes}")
            throw new UnexpectedTypeException()
        }
    }

    /*
    static def lookupOrCreateProp(id, owner){
        if(id instanceof String){
            id = id.toLong()
        }
        def type = get(id)
        createCustomProperty(owner, type)
    }
    */

    /**
     * Called from AjaxController.addCustomPropertyValue()
     * Called from AjaxController.addPrivatePropertyValue()
     *
     * @param owner: The class that will hold the property, e.g License
     */
    static AbstractProperty createGenericProperty(def flag, def owner, PropertyDefinition type) {
        String classString = owner.getClass().toString()
        def ownerClassName = classString.substring(classString.lastIndexOf(".") + 1)

        if (flag == PropertyDefinition.CUSTOM_PROPERTY) {
            ownerClassName = "com.k_int.kbplus.${ownerClassName}CustomProperty"
        }
        else if (flag == PropertyDefinition.PRIVATE_PROPERTY) {
            ownerClassName = "com.k_int.kbplus.${ownerClassName}PrivateProperty"
        }

        def newProp = Class.forName(ownerClassName).newInstance(type: type, owner: owner)
        newProp.setNote("")

        if (flag == PropertyDefinition.CUSTOM_PROPERTY) {
            owner.customProperties.add(newProp)
        }
        else if (flag == PropertyDefinition.PRIVATE_PROPERTY) {
            owner.privateProperties.add(newProp)
        }

        newProp.save(flush:true)
        newProp
    }

    static def lookupOrCreate(name, typeClass, descr, multipleOccurence, mandatory, Org tenant) {
        typeIsValid(typeClass)

        def type = findWhere(
                name:   name,
                descr:  descr,
                tenant: tenant
        )

        if (!type) {
            log.debug("No PropertyDefinition match for ${name} : ${descr} @ ${tenant?.name}. Creating new ..")

            type = new PropertyDefinition(
                    name:   name,
                    descr:  descr,
                    type:   typeClass,
                    // refdataCategory:    rdc,
                    multipleOccurrence: (multipleOccurence ? true : false),
                    mandatory:          (mandatory ? true : false),
                    // TODO softData: true,
                    tenant: tenant
            )
            type.save(flush:true)
        }
        type
    }

    static def refdataFind(params) {
        def result = []
        
        def matches = I10nTranslation.refdataFindHelper(
                params.baseClass,
                'name',
                params.q,
                LocaleContextHolder.getLocale()
        )
        matches.each { it ->
            // used for private properties
            def tenantMatch = (params.tenant.equals(it.getTenant()?.id?.toString()))

            if (tenantMatch) {
                if (params.desc && params.desc != "*") {
                    if (it.getDescr() == params.desc) {
                        result.add([id: "${it.id}", text: "${it.getI10n('name')}"])
                    }
                } else {
                    result.add([id: "${it.id}", text: "${it.getI10n('name')}"])
                }
            }
        }
        result
    }

    def countUsages() {
        def table
        // TODO : refactoring

        if (this.descr == "License Property") {
            table = "LicensePrivateProperty"
        }
        else if (this.descr == "Person Property") {
            table = "PersonPrivateProperty"
        }
        else if (this.descr == "Org Property") {
            table = "OrgPrivateProperty"
        }

        if (table) {
            def c = PropertyDefinition.executeQuery("select count(c) from " + table + " as c where c.type = ?", [this])
            return c[0]
        }
        return 0
    }

    def afterInsert() {
        I10nTranslation.createOrUpdateI10n(this, 'name',  [de: this.name, en: this.name])
        I10nTranslation.createOrUpdateI10n(this, 'descr', [de: this.descr, en: this.descr])
    }

    def afterDelete() {
        def rc = this.getClass().getName()
        def id = this.getId()
        I10nTranslation.where{referenceClass == rc && referenceId == id}.deleteAll()
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
    def qparams = [this]
    def qry = 'select c.owner from ' + cls + " as c where c.type = ?"
    return PropertyDefinition.executeQuery(qry,qparams); 
  }

  @Transient
  def countOccurrences(String cls) {
    def qparams = [this]
    def qry = 'select count(c) from ' + cls + " as c where c.type = ?"
    return (PropertyDefinition.executeQuery(qry,qparams))[0]; 
  }
  @Transient
  def countOccurrences(String[] cls){
    def total_count = 0
    cls.each{
        total_count += countOccurrences(it)
    }
    return total_count
  }
    @Transient
    def removeProperty() {
        log.debug("Remove");
        PropertyDefinition.executeUpdate('delete from com.k_int.kbplus.LicenseCustomProperty c where c.type = ?', [this])
        PropertyDefinition.executeUpdate('delete from com.k_int.kbplus.LicensePrivateProperty c where c.type = ?', [this])
        PropertyDefinition.executeUpdate('delete from com.k_int.kbplus.SubscriptionCustomProperty c where c.type = ?', [this])
        PropertyDefinition.executeUpdate('delete from com.k_int.kbplus.OrgCustomProperty c where c.type = ?', [this])
        PropertyDefinition.executeUpdate('delete from com.k_int.kbplus.OrgPrivateProperty c where c.type = ?', [this])
        PropertyDefinition.executeUpdate('delete from com.k_int.kbplus.PersonPrivateProperty c where c.type = ?', [this])
        this.delete();
    }

    /* tmp only */
    static getAvailablePropertyDescriptions() {
        return [
                "com.k_int.kbplus.Org"      : PropertyDefinition.ORG_PROP,
                "com.k_int.kbplus.License"  : PropertyDefinition.LIC_PROP,
                "com.k_int.kbplus.Person"   : PropertyDefinition.PRS_PROP
        ]
    }
}

