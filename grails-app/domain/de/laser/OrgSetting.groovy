package de.laser


import de.laser.auth.Role
import de.laser.helper.RDConstants
import de.laser.annotations.RefdataAnnotation

class OrgSetting {

    def genericOIDService

    final static SETTING_NOT_FOUND = "SETTING_NOT_FOUND"
    //in order of display
    final static SETTING_TABS = ['general', 'api', 'ezb', 'natstat', 'oamonitor']

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

    @RefdataAnnotation(cat = RefdataAnnotation.GENERIC)
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

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    // only these settings are editable by orgs themselves
    static List<OrgSetting.KEYS> getEditableSettings() {
        [
                OrgSetting.KEYS.EZB_SERVER_ACCESS,
                OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS,
                OrgSetting.KEYS.NATSTAT_SERVER_ACCESS,
                OrgSetting.KEYS.NATSTAT_SERVER_API_KEY,
                OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID
        ]
    }

    /*
        returns user depending setting for given key
        or SETTING_NOT_FOUND if not
     */
    static def get(Org org, KEYS key) {

        OrgSetting oss = findWhere(org: org, key: key)
        oss ?: SETTING_NOT_FOUND
    }

    /*
        adds new org depending setting (with value) for given key
     */
    static OrgSetting add(Org org, KEYS key, def value) {

        withTransaction {
            def oss = new OrgSetting(org: org, key: key)
            oss.setValue(value)
            oss.save()

            oss
        }
    }

    /*
        deletes org depending setting for given key
     */
    static void delete(Org org, KEYS key) {

        withTransaction {
            OrgSetting oss = findWhere(org: org, key: key)
            oss?.delete()
        }
    }

    /*
        gets parsed value by key.type
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

    /*
        sets value by key.type
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
