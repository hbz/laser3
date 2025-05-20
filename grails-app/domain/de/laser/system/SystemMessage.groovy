package de.laser.system


import de.laser.utils.LocaleUtils

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
     * Messages of this type are displayed on top of each page
     */
    static final String TYPE_GLOBAL = "TYPE_GLOBAL"
    /**
     * Messages of this type are displayed on the dashboard
     */
    static final String TYPE_DASHBOARD = "TYPE_DASHBOARD"
    /**
     * Global announcements on the start page
     */
    static final String TYPE_STARTPAGE = "TYPE_STARTPAGE"


    String content_de
    String content_en
    String type
    boolean isActive = false

    SystemMessageCondition.CONFIG condition

    Date dateCreated
    Date lastUpdated

    static transients = ['localizedContent'] // mark read-only accessor methods

    static mapping = {
        id          column: 'sm_id'
        version     column: 'sm_version'

        content_de column: 'sm_content_de'
        content_en column: 'sm_content_en'
        type       column: 'sm_type'
        condition  column: 'sm_condition'
        isActive   column: 'sm_is_active'

        dateCreated column: 'sm_date_created'
        lastUpdated column: 'sm_last_updated'
    }

    static constraints = {
        content_de          (nullable:true,  blank:true)
        content_en          (nullable:true,  blank:true)
        type                (blank:false)
        condition           (nullable:true)
    }

    /**
     * Gets all available message types
     * @return a {@link List} of types
     */
    static getTypes() {
        [TYPE_GLOBAL, TYPE_DASHBOARD, TYPE_STARTPAGE]
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
     * Delivers the content according to the specified locale. This locale is specified by the {@link LocaleUtils}
     * @return the localized content of the system message (German or English)
     */
    String getLocalizedContent() {
        switch (LocaleUtils.getCurrentLang()) {
            case 'de':
                return content_de
                break
            case 'en':
                return content_en
                break
            default:
                return content_en
                break
        }
    }

    boolean isDisplayed() {
        // logic:
        // display message - isActive == true
        // hide message    - isDisplayed() == false

        if (condition) {
            ! SystemMessageCondition.isDone(condition)
        } else {
            true
        }
    }
}
