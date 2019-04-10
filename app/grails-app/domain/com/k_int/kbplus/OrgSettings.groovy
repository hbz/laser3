package com.k_int.kbplus

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
        CUSTOMER_TYPE   (RefdataValue, 'system.customer.type')

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
    String       strValue

    @RefdataAnnotation(cat = RefdataAnnotation.GENERIC)
    RefdataValue rdValue

    static mapping = {
        id         column:'os_id'
        version    column:'os_version'
        org        column:'os_org_fk', index: 'os_org_idx'
        key        column:'os_key_enum'
        strValue   column:'os_string_value'
        rdValue    column:'os_rv_fk'
    }

    static constraints = {
        org        (nullable: false, unique: 'key')
        key        (nullable: false, unique: 'org')
        strValue   (nullable: true)
        rdValue    (nullable: true)
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
        oss.delete(flush: true)
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
            default:
                strValue = (value ? value.toString() : null)
                break
        }
        save(flush: true)
    }
}
