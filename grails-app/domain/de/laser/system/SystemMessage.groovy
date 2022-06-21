package de.laser.system


import de.laser.utils.LocaleUtils
import org.springframework.context.i18n.LocaleContextHolder

/**
 * This class represents - next to {@link SystemAnnouncement}s - a messaging channel to users. A system message has nonetheless different purposes and can be shown on different places:
 * <ul>
 *     <li>on the landing page</li>
 *     <li>on top of every page</li>
 * </ul>
 * Those are messages / notifications for immediate display such as appointments or maintenance alerts which should be present at all time
 */
class SystemMessage {

    /**
     * Messages of this type are showing up on top of each page, to be used for alerts
     */
    static final String TYPE_ATTENTION = "TYPE_ATTENTION"
    /**
     * Messages of this type are global announcements
     */
    static final String TYPE_STARTPAGE_NEWS = "TYPE_STARTPAGE_NEWS"

    String content_de
    String content_en
    String type
    boolean isActive = false

    Date dateCreated
    Date lastUpdated

    static transients = ['localizedContent'] // mark read-only accessor methods

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
        type        (blank:false)
    }

    /**
     * Gets all available message types
     * @return a {@link List} of types
     */
    static getTypes() {
        [TYPE_ATTENTION, TYPE_STARTPAGE_NEWS]
    }

    /**
     * Retrieves all active messages of a given type
     * @param type the type to search for
     * @return a {@link List} of active messages for the given type
     */
    static getActiveMessages(String type) {
        SystemMessage.executeQuery(
                'select sm from SystemMessage sm where sm.isActive = true and sm.type = :type order by sm.lastUpdated desc', [
                type: type
        ])
    }

    /**
     * Delivers the content according to the specified locale. This locale is specified by the {@link LocaleContextHolder}
     * @return the localized content of the system message (German or English)
     */
    String getLocalizedContent() {
        switch (LocaleUtils.getCurrentLang()) {
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
