package com.k_int.kbplus

import de.laser.domain.AbstractI10nOverride
import de.laser.domain.I10nTranslation
import de.laser.helper.RDConstants
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient

class RefdataCategory extends AbstractI10nOverride {

    static Log static_logger = LogFactory.getLog(RefdataCategory)

    @Transient
    public static final ORG_STATUS = RDConstants.ORG_STATUS
    @Transient
    public static final PKG_SCOPE = RDConstants.PACKAGE_SCOPE
    @Transient
    public static final PKG_LIST_STAT = RDConstants.PACKAGE_LIST_STATUS
    @Transient
    public static final PKG_FIXED = RDConstants.PACKAGE_FIXED
    @Transient
    public static final PKG_BREAKABLE = RDConstants.PACKAGE_BREAKABLE
    @Transient
    public static final PKG_CONSISTENT = RDConstants.PACKAGE_CONSISTENT
    @Transient
    public static final PKG_TYPE = RDConstants.PACKAGE_TYPE
    @Transient
    public static final TI_STATUS = 'TitleInstanceStatus'
    @Transient
    public static final LIC_STATUS = RDConstants.LICENSE_STATUS
    @Transient
    public static final LIC_TYPE = RDConstants.LICENSE_TYPE
    @Transient
    public static final TIPP_STATUS = 'TIPP Status'
    @Transient
    public static final TI_TYPE = 'Title Type'
    @Transient
    public static final PKG_PAYMENTTYPE = 'Package Payment Type'
    @Transient
    public static final PKG_GLOBAL = 'Package Global'
     @Transient
    public static final IE_ACCEPT_STATUS = RDConstants.IE_ACCEPT_STATUS

    String desc
    String desc_de
    String desc_en

    // indicates this object is created via current bootstrap
    boolean isHardData

    Date dateCreated
    Date lastUpdated

    static mapping = {
            cache   true
              id column: 'rdc_id'
         version column: 'rdc_version'
            desc column: 'rdc_description', index:'rdc_description_idx'
         desc_de column: 'rdc_description_de', index:'rdc_description_de_idx'
         desc_en column: 'rdc_description_en', index:'rdc_description_en_idx'
        isHardData column: 'rdc_is_hard_data'
        dateCreated column: 'rdc_date_created'
        lastUpdated column: 'rdc_last_updated'
    }

    static constraints = {
        isHardData (nullable:false, blank:false)

        // Nullable is true, because values are already in the database
        desc_de (nullable: true, blank: false)
        desc_en (nullable: true, blank: false)
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    static RefdataCategory construct(Map<String, Object> map) {

        String token     = map.get('token')
        boolean hardData = new Boolean( map.get('hardData') )
        Map i10n         = map.get('i10n')

        RefdataCategory rdc = RefdataCategory.findByDescIlike(token) // todo: case sensitive token

        if (! rdc) {
            static_logger.debug("INFO: no match found; creating new refdata category for ( ${token}, ${i10n} )")
            rdc = new RefdataCategory(desc:token) // todo: token
        }

        rdc.desc_de = i10n.get('de') ?: null
        rdc.desc_en = i10n.get('en') ?: null

        rdc.isHardData = hardData
        rdc.save(flush: true)

        rdc
    }

    @Deprecated
    static RefdataValue lookupOrCreate(String category_name, String icon, String value) {
        RefdataCategory cat = RefdataCategory.findByDescIlike(category_name);
        if (! cat) {
            cat = new RefdataCategory(desc:category_name, desc_de: cat.desc, desc_en: cat.desc).save(flush: true);
        }

        RefdataValue result = RefdataValue.findByOwnerAndValueIlike(cat, value)

        if (! result) {
            new RefdataValue(owner:cat, value:value).save(flush:true);
            result = RefdataValue.findByOwnerAndValueIlike(cat, value);
        }

        result.icon = icon
        result
    }

  static def refdataFind(params) {
      def result = []
      List<RefdataCategory> matches = []

      if(! params.q) {
          matches = RefdataCategory.findAll()
      }
      else {
          switch (I10nTranslation.decodeLocale(LocaleContextHolder.getLocale().toString())) {
              case 'en':
                  matches = RefdataCategory.findAllByDesc_enIlike("%${params.q}%")
                  break
              case 'de':
                  matches = RefdataCategory.findAllByDesc_deIlike("%${params.q}%")
                  break
          }
      }

      matches.each { it ->
          result.add([id: "${it.id}", text: "${it.getI10n('desc')}"])
      }
      result
  }

    static RefdataCategory getByDesc(String desc) {

        RefdataCategory.findByDescIlike(desc)
    }

  /**
   * Returns a list containing category depending refdata_values.
   * 
   * @param category_name
   * @return ArrayList
   */
  static List<RefdataValue> getAllRefdataValues(category_name) {
      String i10value = LocaleContextHolder.getLocale().getLanguage() == Locale.GERMAN.getLanguage() ? 'value_de' : 'value_en'

      RefdataValue.executeQuery(
              "select rdv from RefdataValue as rdv, RefdataCategory as rdc where rdv.owner = rdc and rdc.desc = ? order by rdv.${i10value}"
              , ["${category_name}"] )
  }

    static getAllRefdataValuesWithI10nExplanation(String category_name, Map sort) {
        List<RefdataValue> refdatas = RefdataValue.findAllByOwner(RefdataCategory.findByDescIlike(category_name),sort)
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
}
