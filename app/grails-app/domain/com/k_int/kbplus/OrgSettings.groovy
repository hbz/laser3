package com.k_int.kbplus

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import de.laser.helper.RefdataAnnotation
import javax.persistence.Transient

class OrgSettings {

    final static SETTING_NOT_FOUND = "SETTING_NOT_FOUND"

    @Transient
    def genericOIDService

    static enum KEYS {
        API_LEVEL       (String),
        API_KEY         (String),
        API_PASSWORD    (String),
        CUSTOMER_TYPE   (Role),
        GASCO_ENTRY              (RefdataValue, 'YN'),
        OA2020_SERVER_ACCESS     (RefdataValue, 'YN'),
        STATISTICS_SERVER_ACCESS (RefdataValue, 'YN')

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

    @RefdataAnnotation(cat = RefdataAnnotation.GENERIC)
    RefdataValue rdValue
    String       strValue
    Role         roleValue

    static mapping = {
        id         column:'os_id'
        version    column:'os_version'
        org        column:'os_org_fk', index: 'os_org_idx'
        key        column:'os_key_enum'
        rdValue    column:'os_rv_fk'
        strValue   column:'os_string_value'
        roleValue  column:'os_role_fk'
    }

    static constraints = {
        org        (nullable: false, unique: 'key')
        key        (nullable: false, unique: 'org')
        strValue   (nullable: true)
        rdValue    (nullable: true)
        roleValue  (nullable: true)
    }

    static List<OrgSettings.KEYS> getEditableSettings() {

        [OrgSettings.KEYS.STATISTICS_SERVER_ACCESS, OrgSettings.KEYS.OA2020_SERVER_ACCESS]
    }

    /*
        returns user depending setting for given key
        or SETTING_NOT_FOUND if not
     */
    static get(Org org, KEYS key) {

        def oss = findWhere(org: org, key: key)
        oss ?: SETTING_NOT_FOUND
    }

    /*
        adds new org depending setting (with value) for given key
     */
    static add(Org org, KEYS key, def value) {

        def oss = new OrgSettings(org: org, key: key)
        oss.setValue(value)
        oss.save(flush: true)

        oss
    }

    /*
        deletes org depending setting for given key
     */
    static delete(Org org, KEYS key) {

        def oss = findWhere(org: org, key: key)
        oss?.delete(flush: true)
    }

    /*
        gets parsed value by key.type
     */
    def getValue() {

        def result = null

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
        save(flush: true)
    }
}
