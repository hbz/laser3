package de.laser.domain

import org.springframework.context.i18n.LocaleContextHolder

class SystemMessage {

    static final String TYPE_OVERLAY        = "TYPE_OVERLAY"
    static final String TYPE_STARTPAGE_NEWS = "TYPE_STARTPAGE_NEWS"

    String content_de
    String content_en
    String type
    boolean isActive = false

    Date dateCreated
    Date lastUpdated

    static mapping = {
        id          column: 'sm_id'
        version     column: 'sm_version'

        content_de  column: 'sm_content_de'
        content_en  column: 'sm_content_en'
        type        column: 'sm_type'
        isActive    column: 'sm_is_active'
        dateCreated column: 'sm_date_created'
        lastUpdated column: 'sm_last_updated'
    }

    static constraints = {
        content_de  (nullable:true,  blank:true)
        content_en  (nullable:true,  blank:true)
        type        (nullable:false, blank:false)
        isActive    (nullable:false, blank:false)
    }

    static getTypes() {
        [TYPE_OVERLAY, TYPE_STARTPAGE_NEWS]
    }

    static getActiveMessages(String type) {
        SystemMessage.executeQuery(
                'select sm from SystemMessage sm where sm.isActive = true and sm.type = :type order by sm.lastUpdated desc', [
                type: type
        ])
    }

    String getLocalizedContent() {
        switch (I10nTranslation.decodeLocale(LocaleContextHolder.getLocale().toString())) {
            case 'de':
                return content_de
                break;
            case 'en':
                return content_en
                break
            default:
                return content_en
                break
        }
    }
}
