package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.auth.User
import de.laser.storage.RDConstants

/**
 * This class represents settings for a user; it ensures preferences of the user using the system.
 * These settings are used in various places in the system; they control the look-and-feel on the one hand,
 * reminders and alerting periods on the other. See the enum {@link UserSetting.KEYS} for the full list of
 * configurable settings
 * OrgSetting is a class with the same functionality for organisations
 * @see Org
 * @see OrgSetting
 */
class UserSetting {

    public static final SETTING_NOT_FOUND = "SETTING_NOT_FOUND"
    public static final DEFAULT_REMINDER_PERIOD = 14

    /**
     * The settings for an {@link User} which can be configured
     */
    static enum KEYS {
        PAGE_SIZE                                   (Integer),
        DASHBOARD                                   (Org),
        THEME                                       (RefdataValue, RDConstants.USER_SETTING_THEME),
        DASHBOARD_TAB                               (RefdataValue, RDConstants.USER_SETTING_DASHBOARD_TAB),
        DASHBOARD_ITEMS_TIME_WINDOW                 (Integer),
        LANGUAGE                                    (RefdataValue, RDConstants.LANGUAGE),
        LANGUAGE_OF_EMAILS                          (RefdataValue, RDConstants.LANGUAGE),
        SHOW_SIMPLE_VIEWS                           (RefdataValue, RDConstants.Y_N),
        SHOW_EXTENDED_FILTER                        (RefdataValue, RDConstants.Y_N),
        SHOW_INFO_ICON                              (RefdataValue, RDConstants.Y_N),
        SHOW_EDIT_MODE                              (RefdataValue, RDConstants.Y_N),

        REMIND_CC_EMAILADDRESS                      (String),
        NOTIFICATION_CC_EMAILADDRESS                (String),

        IS_NOTIFICATION_BY_EMAIL                    (RefdataValue, RDConstants.Y_N),
        IS_NOTIFICATION_CC_BY_EMAIL                 (RefdataValue, RDConstants.Y_N),
        IS_NOTIFICATION_FOR_SURVEYS_START           (RefdataValue, RDConstants.Y_N),
        IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH           (RefdataValue, RDConstants.Y_N),
        IS_NOTIFICATION_FOR_SYSTEM_MESSAGES         (RefdataValue, RDConstants.Y_N),

        IS_REMIND_BY_EMAIL                          (RefdataValue, RDConstants.Y_N),
        IS_REMIND_CC_BY_EMAIL                       (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD    (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE         (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP     (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP    (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_LICENSE_CUSTOM_PROP           (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_LIZENSE_PRIVATE_PROP          (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_ORG_CUSTOM_PROP               (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_ORG_PRIVATE_PROP              (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_PERSON_PRIVATE_PROP           (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_TASKS                         (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_SURVEYS_NOT_MANDATORY_ENDDATE (RefdataValue, RDConstants.Y_N),
        IS_REMIND_FOR_SURVEYS_MANDATORY_ENDDATE     (RefdataValue, RDConstants.Y_N),

        REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD  (Integer),
        REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE       (Integer),
        REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP   (Integer),
        REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP  (Integer),
        REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP         (Integer),
        REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP        (Integer),
        REMIND_PERIOD_FOR_ORG_CUSTOM_PROP             (Integer),
        REMIND_PERIOD_FOR_ORG_PRIVATE_PROP            (Integer),
        REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP         (Integer),
        REMIND_PERIOD_FOR_TASKS                       (Integer),
        REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE     (Integer),
        REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE         (Integer)

        KEYS(type, rdc) {
            this.type = type
            this.rdc = rdc
        }
        KEYS(type) {
            this.type = type
        }

        public def type
        public def rdc
    }

    User         user
    KEYS         key
    String       strValue
    Org          orgValue

    Date dateCreated
    Date lastUpdated

    @RefdataInfo(cat = RefdataInfo.GENERIC)
    RefdataValue rdValue

    static transients = ['value'] // mark read-only accessor methods

    static mapping = {
        id         column:'us_id'
        version    column:'us_version'
        user       column:'us_user_fk', index: 'us_user_idx'
        key        column:'us_key_enum'
        strValue   column:'us_string_value'
        rdValue    column:'us_rv_fk'
        orgValue   column:'us_org_fk'

        dateCreated column: 'us_date_created'
        lastUpdated column: 'us_last_updated'
    }

    static constraints = {
        user       (unique: 'key')
        key        (unique: 'user')
        strValue   (nullable: true)
        rdValue    (nullable: true)
        orgValue   (nullable: true)
        lastUpdated (nullable: true)
    }

    /**
     * Returns the user depending setting for the given key or SETTING_NOT_FOUND if not
     * @return the user setting if found, SETTING_NOT_FOUND constant otherwise
     */
    static def get(User user, KEYS key) {

        UserSetting uss = findWhere(user: user, key: key)
        uss ?: SETTING_NOT_FOUND
    }

    /**
     * Adds a new user depending setting (with value) for the given key
     * @return the new user setting
     */
    static UserSetting add(User user, KEYS key, def value) {

        withTransaction {
            UserSetting uss = new UserSetting(user: user, key: key)
            uss.setValue(value)
            uss.save()

            uss
        }
    }

    /**
     * Deletes the given user depending setting for the given key
     */
    static void delete(User user, KEYS key) {

        withTransaction {
            UserSetting uss = findWhere(user: user, key: key)
            uss?.delete()
        }
    }

    /**
     * Gets the parsed value depending on {@link UserSetting.KEYS#type}
     */
    def getValue() {

        def result

        switch (key.type) {
            case Integer:
                result = strValue? Integer.parseInt(strValue) : null
                break
            case Long:
                result = strValue ? Long.parseLong(strValue) : null
                break
            case Org:
                result = orgValue
                break
            case RefdataValue:
                result = rdValue
                break
            default:
                result = strValue
                break
        }
        result
    }

    /**
     * Sets the value by the given {@link UserSetting.KEYS#type}
     */
    def setValue(def value) {

        withTransaction {
            switch (key.type) {
                case Org:
                    orgValue = value
                    break
                case RefdataValue:
                    rdValue = value
                    break
                default:
                    strValue = (value ? value.toString() : null)
                    break
            }
            save()
        }
    }
}
