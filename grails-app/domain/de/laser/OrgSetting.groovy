package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.auth.Role
import de.laser.storage.RDConstants

/**
 * This class represents organisation-wide configuration settings, see the enum {@link OrgSetting.KEYS} for the possible settings.
 * All of them trigger further functionality; the {@link OrgSetting.KEYS#CUSTOMER_TYPE} for example is a key setting and the
 * distinction factor between institutions and (other) organisations (see {@link Org} for the definition of both). Organisations
 * do not have a CUSTOMER_TYPE while institutions mandatorily do have one; they are at least ORG_BASIC_MEMBERs. See {@link Role} for
 * the possible customer types to be granted.
 * UserSetting is a class with the same functionality for users
 * @see Org
 * @see OrgSetting.KEYS
 * @see Role
 * @see UserSetting
 * @see de.laser.auth.User
 */
class OrgSetting {

    public static final SETTING_NOT_FOUND = "SETTING_NOT_FOUND"

    /**
     * The settings for an {@link Org} which can be configured
     */
    static enum KEYS {
        API_LEVEL                   (String),
        API_KEY                     (String),
        API_PASSWORD                (String),
        CUSTOMER_TYPE               (Role),
        EZB_SERVER_ACCESS           (RefdataValue, RDConstants.Y_N),
        GASCO_ENTRY                 (RefdataValue, RDConstants.Y_N),
        OAMONITOR_SERVER_ACCESS     (RefdataValue, RDConstants.Y_N),
        NATSTAT_SERVER_ACCESS       (RefdataValue, RDConstants.Y_N),
        NATSTAT_SERVER_API_KEY      (String),
        NATSTAT_SERVER_REQUESTOR_ID (String),
        LASERSTAT_SERVER_KEY        (String)

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

    Org          org
    KEYS         key

    Date dateCreated
    Date lastUpdated

    @RefdataInfo(cat = RefdataInfo.GENERIC)
    RefdataValue rdValue
    String       strValue
    Role         roleValue

    static transients = ['value'] // mark read-only accessor methods

    static mapping = {
        id         column:'os_id'
        version    column:'os_version'
        org        column:'os_org_fk', index: 'os_org_idx'
        key        column:'os_key_enum'
        rdValue    column:'os_rv_fk'
        strValue   column:'os_string_value'
        roleValue  column:'os_role_fk'

        lastUpdated column: 'os_last_updated'
        dateCreated column: 'os_date_created'
    }

    static constraints = {
        org        (unique: 'key')
        key        (unique: 'org')
        strValue   (nullable: true)
        rdValue    (nullable: true)
        roleValue  (nullable: true)
        lastUpdated (nullable: true)
    }

    /**
     * Gets a list of settings for display on the settings.gsp page.
     * Only these settings are editable by the institutions themselves
     * @return a {@link List} of {@link OrgSetting.KEYS} which are editable
     */
    static List<OrgSetting.KEYS> getEditableSettings() {
        [
                OrgSetting.KEYS.EZB_SERVER_ACCESS,
                OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS,
                OrgSetting.KEYS.NATSTAT_SERVER_ACCESS,
                OrgSetting.KEYS.NATSTAT_SERVER_API_KEY,
                OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID
        ]
    }

    /**
     * Returns organisation depending setting for given key or SETTING_NOT_FOUND if not
     * @return the organisation setting if found, SETTING_NOT_FOUND constant otherwise
     */
    static def get(Org org, KEYS key) {

        OrgSetting oss = findWhere(org: org, key: key)
        oss ?: SETTING_NOT_FOUND
    }

    /**
     * Adds a new organisation depending setting (with value) for the given key
     * @return the new organisation setting
     */
    static OrgSetting add(Org org, KEYS key, def value) {

        withTransaction {
            OrgSetting oss = new OrgSetting(org: org, key: key)
            oss.setValue(value)
            oss.save()

            oss
        }
    }

    /**
     * Deletes the organisation depending setting for the given key
     */
    static void delete(Org org, KEYS key) {

        withTransaction {
            OrgSetting oss = findWhere(org: org, key: key)
            oss?.delete()
        }
    }

    /**
     * Gets parsed value by {@link OrgSetting.KEYS#type}
     * @return the value of the organisation setting
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
            case RefdataValue:
                result = rdValue
                break
            case Role:
                result = roleValue
                break
            default:
                result = strValue
                break
        }
        result
    }

    /**
     * Sets the value for the organisation setting by {@link OrgSetting.KEYS#type}
     */
    def setValue(def value) {

        withTransaction {
            switch (key.type) {
                case RefdataValue:
                    rdValue = value
                    break
                case Role:
                    roleValue = value
                    break
                default:
                    strValue = (value ? value.toString() : null)
                    break
            }
            save()
        }
    }
}
