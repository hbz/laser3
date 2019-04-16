package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import de.laser.domain.I10nTranslation
import grails.util.Holders
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import java.text.SimpleDateFormat

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

    boolean pickAndChoose

    ArrayList orgIDs

    static hasMany = [
            documents: DocContext,
            surveyProperties: SurveyConfigProperties
    ]

    static constraints = {
        subscription (nullable:true, blank:false)
        surveyProperty (nullable:true, blank:false)
        orgIDs (nullable:true, blank:false)
        header(nullable:true, blank:false)
        comment  (nullable:true, blank:false)
        pickAndChoose (nullable:true, blank:false)
        documents (nullable:true, blank:false)
    }

    static mapping = {
        id column: 'surconf_id'
        version column: 'surconf_version'

        type column: 'surconf_type'
        header column: 'surconf_header'
        comment  column: 'surconf_comment'
        pickAndChoose column: 'surconf_pickAndChoose'

        orgIDs column: 'surconf_org_ids'

        dateCreated column: 'surconf_date_created'
        lastUpdated column: 'surconf_last_updated'

        surveyInfo column: 'surconf_surinfo_fk'
        subscription column: 'surconf_sub_fk'
        surveyProperty column: 'surconf_surprop_fk'

        configOrder column: 'surconf_config_order'
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

    def getCurrentDocs(){

        return documents.findAll {it.status?.value != 'Deleted'}
    }

    def getConfigNameShort(){

        if(type == 'Subscription'){
            return subscription?.name
        }
        else
        {
            return surveyProperty?.getI10n('name')
        }
    }

    def getConfigName(){

        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null, LocaleContextHolder.getLocale()))

        if(type == 'Subscription'){
            return subscription?.name + ' - ' + subscription?.status?.getI10n('value') + ' ' +
                     (subscription?.startDate ? '(' : '') + sdf.format(subscription?.startDate) +
                         (subscription?.endDate ? ' - ' : '') +  sdf.format(subscription?.endDate) +
                          (subscription?.startDate ? ')' : '')

        }
        else
        {
            return surveyProperty?.getI10n('name')
        }
    }


}
