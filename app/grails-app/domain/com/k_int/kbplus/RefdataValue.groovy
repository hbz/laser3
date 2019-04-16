package com.k_int.kbplus
import com.k_int.ClassUtils
import de.laser.domain.AbstractI10nTranslatable
import de.laser.domain.I10nTranslation
import org.springframework.context.i18n.LocaleContextHolder
import javax.persistence.Transient

class RefdataValue extends AbstractI10nTranslatable implements Comparable<RefdataValue> {

    @Transient
    def grailsApplication

    String value

    // N.B. This used to be ICON but in the 2.x series this was changed to be a css class which denotes an icon
    // Please stick with the change to store css classes in here and not explicit icons
    String icon
    //For cases were we want to present a specific group of values, eg License/Sub related
    String group

    // indicates this object is created via current bootstrap
    boolean hardData

    // if manual ordering is wanted
    Long order

    static belongsTo = [
        owner:RefdataCategory
    ]

    static mapping = {
                    id column: 'rdv_id'
               version column: 'rdv_version'
                 owner column: 'rdv_owner', index: 'rdv_entry_idx'
                 value column: 'rdv_value', index: 'rdv_entry_idx'
                  icon column: 'rdv_icon'
                 group column: 'rdv_group'
              hardData column: 'rdv_hard_data'
              order    column: 'rdv_order'

    }

    static constraints = {
        icon     (nullable:true)
        group    (nullable:true,  blank:false)
        hardData (nullable:false, blank:false, default:false)
        order    (nullable:true,  blank: false)
    }

    /**
     * Create RefdataValue and matching I10nTranslation.
     * Create RefdataCategory, if needed.
     *
     * Call this from bootstrap
     *
     * @param category_name
     * @param i10n
     * @param hardData = only true if called from bootstrap
     * @return
     */
    static def loc(String category_name, Map i10n, def hardData) {

        // avoid harddata reset @ RefdataCategory.loc
        def cat = RefdataCategory.findByDescIlike(category_name)
        if (! cat) {
            cat = new RefdataCategory(desc:category_name)
            cat.save(flush: true)

            I10nTranslation.createOrUpdateI10n(cat, 'desc', [:])
        }

        def rdvValue = i10n['key'] ?: i10n['en']

        def result = findByOwnerAndValueIlike(cat, rdvValue)
        if (! result) {
            result = new RefdataValue(owner: cat, value: rdvValue)
        }
        result.hardData = hardData
        result.save(flush: true)

        I10nTranslation.createOrUpdateI10n(result, 'value', i10n)

        result
    }

    // Call this from code
    static def loc(String category_name, Map i10n) {
        def hardData = false
        loc(category_name, i10n, hardData)
    }

    static def refdataFind(params) {
        def result = []
        def matches = I10nTranslation.refdataFindHelper(
                params.baseClass,
                'value',
                params.q,
                LocaleContextHolder.getLocale()
        )
        matches.each { it ->
            result.add([id: "${it.class.name}:${it.id}", text: "${it.getI10n('value')}"])
        }
        matches
    }

    static def refdataCreate(value) {
        // return new RefdataValue(value:value);
        return null;
    }

    static def getByValueAndCategory(value, category) {

        RefdataValue.findByValueAndOwner(value, RefdataCategory.findByDesc(category))
    }

    static def getByCategoryDescAndI10nValueDe(categoryName, value) {

        def data = RefdataValue.executeQuery("select rdv from RefdataValue as rdv, RefdataCategory as rdc, I10nTranslation as i10n "
                    + " where rdv.owner = rdc and rdc.desc = ? "
                    + " and i10n.referenceId = rdv.id and i10n.valueDe = ?"
                    + " and i10n.referenceClass = 'com.k_int.kbplus.RefdataValue' and i10n.referenceField = 'value'"
                    , ["${categoryName}", "${value}"] )

        if (data.size() > 0) {
            return data[0]
        }

        null
    }

    int compareTo(RefdataValue rdv) {

        def a = rdv.order  ?: 0
        def b = this.order ?: 0

        if (a && b) {
            return a <=> b
        }
        else if (a && !b) {
            return 1
        }
        else if (!a && b) {
            return -1
        }
        else {
            return this.getI10n('value')?.compareTo(rdv.getI10n('value'))
        }
    }

    // still provide OLD mapping for string compares and such stuff
    public String toString() {
        value
    }

    /**
    * Equality should be decided like this, although we currently got duplicates
    * refdatavalue for same string value
    **/
    @Override
    public boolean equals (Object o) {
        def obj = ClassUtils.deproxy(o)
        if (obj != null) {
            if ( obj instanceof RefdataValue ) {
                return obj.id == id
            }
        }
        return false
    }

    def afterInsert() {
        I10nTranslation.createOrUpdateI10n(this, 'value', [de: this.value, en: this.value])
    }

    def afterDelete() {
        def rc = this.getClass().getName()
        def id = this.getId()
        I10nTranslation.where{referenceClass == rc && referenceId == id}.deleteAll()
    }
}
