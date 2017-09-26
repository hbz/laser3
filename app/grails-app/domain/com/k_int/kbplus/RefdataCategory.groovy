package com.k_int.kbplus

import de.laser.domain.I10nTranslatableAbstract
import de.laser.domain.I10nTranslation
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient

class RefdataCategory extends I10nTranslatableAbstract {

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

    String desc

    // indicates this object is created via front-end
    boolean softData

    static mapping = {
              id column: 'rdc_id'
         version column: 'rdc_version'
            desc column: 'rdc_description', index:'rdc_description_idx'
        softData column: 'rdv_soft_data'
    }

    static constraints = {
        softData (nullable:false, blank:false, default:false)
    }

    /**
     * Create RefdataCategory and matching I10nTranslation
     *
     * @param category_name
     * @param rdcValues
     * @return
     */
    static def locCategory(String category_name, Map rdcValues) {

        def cat = RefdataCategory.findByDescIlike(category_name)
        if (! cat) {
            cat = new RefdataCategory(desc:category_name).save()
        }

        I10nTranslation.createOrUpdateI10n(cat, 'desc', rdcValues)

        cat
    }

    /**
     * Create RefdataValue and matching I10nTranslation.
     * Create needed RefdataCategory without I10nTranslation, if not found
     *
     * @param category_name
     * @param rdvValues
     * @return
     */
    static def locRefdataValue(String category_name, Map rdvValues) {

        def cat = RefdataCategory.findByDescIlike(category_name)
        if (! cat) {
            cat = new RefdataCategory(desc:category_name).save()
        }

        def result = RefdataValue.findByOwnerAndValueIlike(cat, rdvValues['en'])
        if (! result) {
            result = new RefdataValue(owner: cat, value: rdvValues['en']).save(flush: true)
        }

        I10nTranslation.createOrUpdateI10n(result, 'value', rdvValues)

        result
    }

    @Deprecated
    static def lookupOrCreate(String category_name, String value) {
        def cat = RefdataCategory.findByDescIlike(category_name);
        if (! cat) {
            cat = new RefdataCategory(desc:category_name).save();
        }

        def result = RefdataValue.findByOwnerAndValueIlike(cat, value)

        if (! result) {
            new RefdataValue(owner:cat, value:value).save(flush:true);
            result = RefdataValue.findByOwnerAndValue(cat, value);
        }

        result
    }

    static def lookupOrCreate(String category_name, String icon, String value) {
        def result = lookupOrCreate(category_name, value)
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
  static getAllRefdataValues(category_name) {
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
      def result = RefdataValue.findAllByOwner( RefdataCategory.findByDesc(category_name))
      result
  }
}
