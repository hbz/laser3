package com.k_int.kbplus

import de.laser.domain.AbstractI10nTranslatable
import de.laser.domain.I10nTranslation
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient

class SurveyProperty extends AbstractI10nTranslatable {

    String name
    String type
    String explain
    String comment
    String introduction
    String refdataCategory

    Org owner

    Date dateCreated
    Date lastUpdated

    // indicates this object is created via current bootstrap
    boolean hardData

    @Transient
    static def validTypes = [
            'class java.lang.Integer'             : ['de': 'Zahl', 'en': 'Number'],
            'class java.lang.String'              : ['de': 'Text', 'en': 'Text'],
            'class com.k_int.kbplus.RefdataValue' : ['de': 'Referenzwert', 'en': 'Refdata'],
            'class java.math.BigDecimal'          : ['de': 'Dezimalzahl', 'en': 'Decimal'],
            'class java.util.Date'                : ['de': 'Datum', 'en': 'Date'],
            'class java.net.URL'                  : ['de': 'Url', 'en': 'Url']
    ]

    static constraints = {
        owner (nullable:true, blank:false)
        introduction (nullable:true, blank:false)
        comment (nullable:true, blank:false)
        explain (nullable:true, blank:false)
        hardData (nullable: false, blank: false, default: false)
        refdataCategory (nullable:true, blank:false)
    }

    static mapping = {
        id column: 'surPro_id'
        version column: 'surPro_version'

        name column: 'surPro_name'
        type column: 'surPro_type', type: 'text'
        explain column: 'surPro_explain', type: 'text'
        comment column: 'surPro_comment', type: 'text'
        introduction column: 'surPro_introduction', type: 'text'
        refdataCategory column: 'surPro_refdataCategory'

        owner column: 'surPro_org_fk'

        dateCreated column: 'surPro_dateCreated'
        lastUpdated column: 'surPro_lastUpdated'
    }

    static getLocalizedValue(key){
        def locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale().toString())

        //println locale
        if (SurveyProperty.validTypes.containsKey(key)) {
            return (SurveyProperty.validTypes.get(key)."${locale}") ?: SurveyProperty.validTypes.get(key)
        } else {
            return null
        }
    }

    def afterInsert() {
        I10nTranslation.createOrUpdateI10n(this, 'name',  [de: this.name, en: this.name])
        I10nTranslation.createOrUpdateI10n(this, 'explain', [de: this.explain, en: this.explain])
        I10nTranslation.createOrUpdateI10n(this, 'introduction', [de: this.introduction, en: this.introduction])
    }

    def afterDelete() {
        def rc = this.getClass().getName()
        def id = this.getId()
        I10nTranslation.where{referenceClass == rc && referenceId == id}.deleteAll()
    }
}
