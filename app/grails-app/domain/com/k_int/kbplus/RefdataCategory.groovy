package com.k_int.kbplus

import de.laser.domain.AbstractI10nTranslatable
import de.laser.domain.I10nTranslation
import groovy.transform.NotYetImplemented
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient

class RefdataCategory extends AbstractI10nTranslatable {

    static Log static_logger = LogFactory.getLog(RefdataCategory)

    @Transient
    public static final ORG_STATUS = 'OrgStatus'
    @Transient
    public static final PKG_SCOPE = "Package.Scope"
    @Transient
    public static final PKG_LIST_STAT = "Package.ListStatus"
    @Transient
    public static final PKG_FIXED = "Package.Fixed"
    @Transient
    public static final PKG_BREAKABLE = "Package.Breakable"
    @Transient
    public static final PKG_CONSISTENT = 'Package.Consistent'
    @Transient
    public static final PKG_TYPE = 'Package.Type'
    @Transient
    public static final TI_STATUS = 'TitleInstanceStatus'
    @Transient
    public static final LIC_STATUS = 'License Status'
    @Transient
    public static final LIC_TYPE = 'License Type'
    @Transient
    public static final TIPP_STATUS = 'TIPP Status'
    @Transient
    public static final TI_TYPE = 'Title Type'
    @Transient
    public static final PKG_PAYMENTTYPE = 'Package Payment Type'
    @Transient
    public static final PKG_GLOBAL = 'Package Global'
     @Transient
    public static final IE_ACCEPT_STATUS = 'IE Accept Status'

    String desc

    // indicates this object is created via current bootstrap
    boolean isHardData

    Date dateCreated
    Date lastUpdated

    static mapping = {
            cache   true
              id column: 'rdc_id'
         version column: 'rdc_version'
            desc column: 'rdc_description', index:'rdc_description_idx'
        isHardData column: 'rdc_is_hard_data'

        dateCreated column: 'rdc_date_created'
        lastUpdated column: 'rdc_last_updated'
    }

    static constraints = {
        isHardData (nullable:false, blank:false)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    static RefdataCategory construct(Map<String, Object> map) {

        String token     = map.get('token')
        boolean hardData = map.get('hardData')
        Map i10n         = map.get('i10n')

        RefdataCategory rdc = RefdataCategory.findByDescIlike(token) // todo: case sensitive token

        if (! rdc) {
            static_logger.debug("INFO: no match found; creating new refdata category for ( ${token}, ${i10n} )")
            rdc = new RefdataCategory(desc:token) // todo: token
        }

        rdc.isHardData = hardData
        rdc.save(flush: true)

        I10nTranslation.createOrUpdateI10n(rdc, 'desc', i10n) // todo: token

        rdc
    }

    static RefdataCategory getByI10nDesc(desc) {

        I10nTranslation i10n = I10nTranslation.findByReferenceClassAndReferenceFieldAndValueDeIlike(
                RefdataCategory.class.name, 'desc', "${desc}"
        )
        RefdataCategory rdc = RefdataCategory.get(i10n?.referenceId)

        rdc
    }

    @Deprecated
    static RefdataValue lookupOrCreate(String category_name, String icon, String value) {
        RefdataCategory cat = RefdataCategory.findByDescIlike(category_name);
        if (! cat) {
            cat = new RefdataCategory(desc:category_name).save(flush: true);
        }

        RefdataValue result = RefdataValue.findByOwnerAndValueIlike(cat, value)

        if (! result) {
            new RefdataValue(owner:cat, value:value).save(flush:true);
            result = RefdataValue.findByOwnerAndValue(cat, value);
        }

        result.icon = icon
        result
    }

  static def refdataFind(params) {
      def result = []
      def matches = I10nTranslation.refdataFindHelper(
              params.baseClass,
              'desc',
              params.q,
              LocaleContextHolder.getLocale()
      )
      matches.each { it ->
          result.add([id: "${it.id}", text: "${it.getI10n('desc')}"])
      }
      result
  }

  /**
   * Returns a list containing category depending refdata_values.
   * 
   * @param category_name
   * @return ArrayList
   */
  static List<RefdataValue> getAllRefdataValues(category_name) {
      //println("RefdataCategory.getAllRefdataValues(" + category_name + ")")

      /*
      def result = RefdataValue.findAllByOwner(
          RefdataCategory.findByDesc(category_name)
          ).collect {[
              id:    it.id.toString(),
              value: it.value.toString(),
              owner: it.owner.getId(),
              group: it.group.toString(),
              icon:  it.icon.toString()
              ]}
      */
//      RefdataValue.findAllByOwner( RefdataCategory.findByDesc(category_name)).sort{a,b -> a.getI10n('value').compareToIgnoreCase b.getI10n('value')}
      String i10value = LocaleContextHolder.getLocale().getLanguage() == Locale.GERMAN.getLanguage() ? 'valueDe' : 'valueEn'

      RefdataValue.executeQuery("""select rdv from RefdataValue as rdv, RefdataCategory as rdc, I10nTranslation as i10n
               where rdv.owner = rdc and rdc.desc = ? 
               and i10n.referenceId = rdv.id 
               and i10n.referenceClass = 'com.k_int.kbplus.RefdataValue' and i10n.referenceField = 'value'
               order by i10n.${i10value}"""
              , ["${category_name}"] )
  }

    static getAllRefdataValuesWithI10nExplanation(String category_name, Map sort) {
        List<RefdataValue> refdatas = RefdataValue.findAllByOwner(RefdataCategory.findByDesc(category_name),sort)
        return fetchData(refdatas)
    }

    static getAllRefdataValuesWithI10nExplanation(String category_name) {
        List<RefdataValue> refdatas = getAllRefdataValues(category_name)
        return fetchData(refdatas)
    }

    private static List fetchData(refdatas) {
        List result = []
        refdatas.each { rd ->
            String explanation
            I10nTranslation translation = I10nTranslation.findByReferenceClassAndReferenceFieldAndReferenceId(rd.class.name,'expl',rd.id)
            switch(I10nTranslation.decodeLocale(LocaleContextHolder.getLocale().toString())) {
                case "de": explanation = translation.valueDe
                    break
                case "en": explanation = translation.valueEn
                    break
                case "fr": explanation = translation.valueFr
                    break
            }
            result.add(id:rd.id,value:rd.getI10n('value'),expl:explanation)
        }
        result
    }

    def afterInsert() {
        I10nTranslation.createOrUpdateI10n(this, 'desc', [de: this.desc, en: this.desc])
    }

    def afterDelete() {
        def rc = this.getClass().getName()
        def id = this.getId()
        I10nTranslation.where{referenceClass == rc && referenceId == id}.deleteAll()
    }
}
