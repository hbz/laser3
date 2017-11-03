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
     * Create RefdataCategory and matching I10nTranslation.
     * Softdata flag will be removed, if RefdataValue is found.
     *
     * @param category_name
     * @param i10n
     * @return
     */
    static def loc(String category_name, Map i10n) {

        def result = RefdataCategory.findByDescIlike(category_name)
        if (! result) {
            result = new RefdataCategory(desc:category_name)
        }
        result.softData = false
        result.save(flush: true)

        I10nTranslation.createOrUpdateI10n(result, 'desc', i10n)

        result
    }

    @Deprecated
    static def lookupOrCreate(String category_name, String value) {
        def cat = RefdataCategory.findByDescIlike(category_name);
        if (! cat) {
            cat = new RefdataCategory(desc:category_name).save(flush: true);
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

    def afterInsert() {
        I10nTranslation.createOrUpdateI10n(this, 'desc', [de: this.desc, en: this.desc])
    }

    def afterDelete() {
        def rc = this.getClass().getName()
        def id = this.getId()
        I10nTranslation.where{referenceClass == rc && referenceId == id}.deleteAll()
    }
}
