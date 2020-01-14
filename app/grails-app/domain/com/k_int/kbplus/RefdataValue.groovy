package com.k_int.kbplus

import com.k_int.ClassUtils
import de.laser.domain.AbstractI10nTranslatable
import de.laser.domain.I10nTranslation
import groovy.transform.NotYetImplemented
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient

class RefdataValue extends AbstractI10nTranslatable implements Comparable<RefdataValue> {

    static Log static_logger = LogFactory.getLog(RefdataValue)

    @Transient
    def grailsApplication

    String value

    // N.B. This used to be ICON but in the 2.x series this was changed to be a css class which denotes an icon
    // Please stick with the change to store css classes in here and not explicit icons
    String icon
    //For cases were we want to present a specific group of values, eg License/Sub related
    String group

    // indicates this object is created via current bootstrap
    boolean isHardData

    // if manual ordering is wanted
    Long order

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
        owner:RefdataCategory
    ]

    static mapping = {
            cache   true
                    id column: 'rdv_id'
               version column: 'rdv_version'
                 owner column: 'rdv_owner', index: 'rdv_owner_value_idx'
                 value column: 'rdv_value', index: 'rdv_owner_value_idx'
                  icon column: 'rdv_icon'
                 group column: 'rdv_group'
              isHardData column: 'rdv_is_hard_data'
              order    column: 'rdv_order'

        dateCreated column: 'rdv_date_created'
        lastUpdated column: 'rdv_last_updated'

    }

    static constraints = {
        icon     (nullable:true)
        group    (nullable:true,  blank:false)
        isHardData (nullable:false, blank:false)
        order    (nullable:true,  blank: false)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    static RefdataValue construct(Map<String, Object> map) {

        String token     = map.get('token')
        String rdc       = map.get('rdc')
        boolean hardData = map.get('hardData')
        Map i10n         = map.get('i10n')

        RefdataCategory cat = RefdataCategory.findByDescIlike(rdc)
        if (! cat) {
            cat = RefdataCategory.construct([
                    token   : rdc,
                    hardData: false,
                    i10n    : [en: rdc, de: rdc],
            ])
        }

        RefdataValue rdv = findByOwnerAndValueIlike(cat, token)
        if (! rdv) {
            static_logger.debug("INFO: no match found; creating new refdata value for ( ${token} @ ${rdc}, ${i10n} )")
            rdv = new RefdataValue(owner: cat, value: token)
        }
        rdv.isHardData = hardData
        rdv.save(flush: true)

        I10nTranslation.createOrUpdateI10n(rdv, 'value', i10n)

        rdv
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

    // called from AjaxController.resolveOID2()
    static def refdataCreate(value) {
        // return new RefdataValue(value:value);
        return null;
    }

    static RefdataValue getByValueAndCategory(value, category) {

        RefdataValue.findByValueAndOwner(value, RefdataCategory.findByDesc(category))
    }

    static RefdataValue getByCategoryDescAndI10nValueDe(categoryName, value) {

        List<RefdataValue> data = RefdataValue.executeQuery("select rdv from RefdataValue as rdv, RefdataCategory as rdc, I10nTranslation as i10n "
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
    String toString() {
        value
    }

    /**
    * Equality should be decided like this, although we currently got duplicates
    * refdatavalue for same string value
    **/
    @Override
    boolean equals (Object o) {
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
        String rc = this.getClass().getName()
        def id = this.getId()
        I10nTranslation.where{referenceClass == rc && referenceId == id}.deleteAll()
    }
}
