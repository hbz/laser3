package com.k_int.kbplus

import de.laser.base.AbstractI10n
import de.laser.I10nTranslation
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient

class RefdataValue extends AbstractI10n implements Comparable<RefdataValue> {

    static Log static_logger = LogFactory.getLog(RefdataValue)

    @Transient
    def grailsApplication

    String value
    String value_de
    String value_en

    String expl_de
    String expl_en

    // N.B. This used to be ICON but in the 2.x series this was changed to be a css class which denotes an icon
    // Please stick with the change to store css classes in here and not explicit icons
    String icon
    //For cases were we want to present a specific group of values, eg License/Sub related
    String group

    // indicates this object is created via current bootstrap
    boolean isHardData = false

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
              value_de column: 'rdv_value_de', index:'rdv_value_de_idx'
              value_en column: 'rdv_value_en', index:'rdv_value_en_idx'
               expl_de column: 'rdv_explanation_de', type: 'text'
               expl_en column: 'rdv_explanation_en', type: 'text'
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
        isHardData (blank:false)
        order    (nullable:true,  blank: false)
        value_de (nullable: true, blank: false)
        value_en (nullable: true, blank: false)
        expl_de  (nullable: true, blank: false)
        expl_en  (nullable: true, blank: false)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    static RefdataValue construct(Map<String, Object> map) {

        String token     = map.get('token')
        String rdc       = map.get('rdc')
        boolean hardData = new Boolean( map.get('hardData') )
        Map i10n         = map.get('i10n')

        RefdataCategory cat = RefdataCategory.findByDescIlike(rdc)
        if (! cat) {
            cat = RefdataCategory.construct([
                    token   : rdc,
                    hardData: false,
                    i10n    : [desc_de: rdc, desc_en: rdc],
            ])
        }

        RefdataValue rdv = findByOwnerAndValueIlike(cat, token)
        if (! rdv) {
            static_logger.debug("INFO: no match found; creating new refdata value for ( ${token} @ ${rdc}, ${i10n} )")
            rdv = new RefdataValue(owner: cat, value: token)
        }

        rdv.value_de = i10n.get('value_de') ?: null
        rdv.value_en = i10n.get('value_en') ?: null

        rdv.expl_de = i10n.get('expl_de') ?: null
        rdv.expl_en = i10n.get('expl_en') ?: null

        rdv.isHardData = hardData
        rdv.save(flush: true)

        rdv
    }

    static def refdataFind(params) {
        def result = []
        List<RefdataValue> matches = []

        if(! params.q) {
            matches = RefdataValue.findAll()
        }
        else {
            switch (I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())) {
                case 'en':
                    matches = RefdataValue.findAllByValue_enIlike("%${params.q}%")
                    break
                case 'de':
                    matches = RefdataValue.findAllByValue_deIlike("%${params.q}%")
                    break
            }
        }
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

    static RefdataValue getByValue(String value) {

        RefdataValue.findByValueIlike(value)
    }

    static RefdataValue getByValueAndCategory(String value, String category) {

        RefdataValue.findByValueIlikeAndOwner(value, RefdataCategory.findByDescIlike(category))
    }

    static RefdataValue getByCategoryDescAndI10nValueDe(String categoryName, String value) {
        if (!categoryName || !value) {
            return null
        }
        String query = "select rdv from RefdataValue as rdv, RefdataCategory as rdc where rdv.owner = rdc and rdc.desc = :category and rdv.value_de = :value_de"
        List<RefdataValue> data = RefdataValue.executeQuery( query, [category: categoryName, value_de: value] )

        return (data.size() > 0) ? data[0] : null
    }

    static RefdataValue getByCategoriesDescAndI10nValueDe(List categoryNames, String value) {
        if (!categoryNames || !value) {
            return null
        }
        String query = "select rdv from RefdataValue as rdv, RefdataCategory as rdc where rdv.owner = rdc and rdc.desc in (:categories) and rdv.value_de = :value_de"
        List<RefdataValue> data = RefdataValue.executeQuery( query, [categories: categoryNames, value_de: value] )

        return (data.size() > 0) ? data[0] : null
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
        //def obj = ClassUtils.deproxy(o)
        def obj = GrailsHibernateUtil.unwrapIfProxy(o)
        if (obj != null) {
            if ( obj instanceof RefdataValue ) {
                return obj.id == id
            }
        }
        return false
    }
}
