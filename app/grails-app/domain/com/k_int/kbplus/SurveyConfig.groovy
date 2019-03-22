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

    Date dateCreated
    Date lastUpdated



    static hasMany = [
            docs: Doc,
            surveyProperties: SurveyConfigProperties
    ]

    static constraints = {
        docs (nullable:true, blank:false)
        subscription (nullable:true, blank:false)
        surveyProperty (nullable:true, blank:false)
    }

    static mapping = {
        id column: 'surConf_id'
        version column: 'surConf_version'

        type column: 'surConf_type'

        dateCreated column: 'surConf_dateCreated'
        lastUpdated column: 'surConf_lastUpdated'

        surveyInfo column: 'surConf_surInfo_fk'
        subscription column: 'surConf_sub_fk'
        surveyProperty column: 'surConf_surProp_fk'

        configOrder column: 'surConf_configOrder'
    }

    @Transient
    static def validTypes = [
            'Subscription'             : ['de': 'Lizenz', 'en': 'Subscription'],
            'SurveyProperty'              : ['de': 'Umfrage-Merkmal', 'en': 'Survey-Property']
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
