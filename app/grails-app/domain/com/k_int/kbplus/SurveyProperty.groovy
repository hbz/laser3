package com.k_int.kbplus

import de.laser.domain.AbstractI10nTranslatable
import de.laser.domain.I10nTranslation
import groovy.util.logging.Log4j
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import javax.validation.UnexpectedTypeException

@Log4j
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
            'class java.lang.Integer'            : ['de': 'Zahl', 'en': 'Number'],
            'class java.lang.String'             : ['de': 'Text', 'en': 'Text'],
            'class com.k_int.kbplus.RefdataValue': ['de': 'Referenzwert', 'en': 'Refdata'],
            'class java.math.BigDecimal'         : ['de': 'Dezimalzahl', 'en': 'Decimal'],
            'class java.util.Date'               : ['de': 'Datum', 'en': 'Date'],
            'class java.net.URL'                 : ['de': 'Url', 'en': 'Url']
    ]

    static constraints = {
        owner(nullable: true, blank: false)
        introduction(nullable: true, blank: false)
        comment(nullable: true, blank: false)
        explain(nullable: true, blank: false)
        hardData(nullable: false, blank: false, default: false)
        refdataCategory(nullable: true, blank: false)
    }

    static mapping = {
        id column: 'surpro_id'
        version column: 'surpro_version'

        name column: 'surpro_name'
        type column: 'surpro_type', type: 'text'
        explain column: 'surpro_explain', type: 'text'
        comment column: 'surpro_comment', type: 'text'
        introduction column: 'surpro_introduction', type: 'text'
        refdataCategory column: 'surpro_refdata_category'

        owner column: 'surpro_org_fk'

        dateCreated column: 'surpro_date_created'
        lastUpdated column: 'surpro_last_updated'
    }

    static getLocalizedValue(key) {
        def locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale().toString())

        //println locale
        if (SurveyProperty.validTypes.containsKey(key)) {
            return (SurveyProperty.validTypes.get(key)."${locale}") ?: SurveyProperty.validTypes.get(key)
        } else {
            return null
        }
    }

    def afterInsert() {
        I10nTranslation.createOrUpdateI10n(this, 'name', [de: this.name, en: this.name])
        I10nTranslation.createOrUpdateI10n(this, 'explain', [de: this.explain, en: this.explain])
        I10nTranslation.createOrUpdateI10n(this, 'introduction', [de: this.introduction, en: this.introduction])
        //I10nTranslation.createOrUpdateI10n(this, 'comment', [de: this.comment, en: this.comment])

    }

    def afterDelete() {
        def rc = this.getClass().getName()
        def id = this.getId()
        I10nTranslation.where { referenceClass == rc && referenceId == id }.deleteAll()
    }

    private static def typeIsValid(key) {
        if (validTypes.containsKey(key)) {
            return true;
        } else {
            log.error("Provided prop type ${key} is not valid. Allowed types are ${validTypes}")
            throw new UnexpectedTypeException()
        }
    }

    static def loc(String name, String typeClass, RefdataCategory rdc, String explain, String comment, String introduction, Org owner) {

        typeIsValid(typeClass)

        def prop = findWhere(
                name: name,
                type: typeClass,
                owner: owner
        )

        if (!prop) {
            log.debug("No SurveyProperty match for ${name} : ${typeClass} ( ${explain} ) @ ${owner?.name}. Creating new one ..")

            prop = new SurveyProperty(
                    name: name,
                    type: typeClass,
                    explain: explain ?: null,
                    comment: comment ?: null,
                    introduction: introduction ?: null,
                    refdataCategory: rdc?.desc,
                    owner: owner
            )
            prop.save(flush: true)
        }
        prop
    }
}
