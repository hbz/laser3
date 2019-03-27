package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import de.laser.domain.I10nTranslation
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient

class SurveyConfig {


    Integer configOrder

    Subscription subscription
    SurveyProperty surveyProperty

    SurveyInfo surveyInfo

    String type
    String header
    String comment

    Date dateCreated
    Date lastUpdated

    ArrayList orgIDs

    static hasMany = [
            docs: Doc,
            surveyProperties: SurveyConfigProperties
    ]

    static constraints = {
        docs (nullable:true, blank:false)
        subscription (nullable:true, blank:false)
        surveyProperty (nullable:true, blank:false)
        orgIDs (nullable:true, blank:false)
        header(nullable:true, blank:false)
        comment  (nullable:true, blank:false)
    }

    static mapping = {
        id column: 'surconf_id'
        version column: 'surconf_version'

        type column: 'surconf_type'
        header column: 'surconf_header'
        comment  column: 'surconf_comment'

        orgIDs column: 'surconf_org_ids'

        dateCreated column: 'surconf_dateCreated'
        lastUpdated column: 'surconf_lastUpdated'

        surveyInfo column: 'surconf_surinfo_fk'
        subscription column: 'surconf_sub_fk'
        surveyProperty column: 'surconf_surprop_fk'

        configOrder column: 'surconf_config_order_fk'
    }

    @Transient
    static def validTypes = [
            'Subscription'             : ['de': 'Lizenz', 'en': 'Subscription'],
            'SurveyProperty'              : ['de': 'Abfrage-Merkmal', 'en': 'Survey-Property']
    ]

    static getLocalizedValue(key){
        def locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale().toString())

        //println locale
        if (SurveyConfig.validTypes.containsKey(key)) {
            return (SurveyConfig.validTypes.get(key)."${locale}") ?: SurveyConfig.validTypes.get(key)
        } else {
            return null
        }
    }


}
