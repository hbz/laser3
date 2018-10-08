package com.k_int.kbplus

import com.k_int.kbplus.auth.User

import javax.persistence.Transient

class UserSettings {

    final static SETTING_NOT_FOUND = "SETTING_NOT_FOUND"

    @Transient
    def genericOIDService

    static enum KEYS {
        PAGE_SIZE         (Long),
        DASHBOARD         (Org),
        SHOW_SIMPLE_VIEWS (RefdataValue, 'YN'),
        SHOW_INFO_ICON    (RefdataValue, 'YN')

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
    RefdataValue rdValue

    static mapping = {
        id         column:'us_id'
        version    column:'us_version'
        user       column:'us_user_fk'
        key        column:'us_key_enum'
        strValue   column:'us_string_value'
        rdValue    column:'us_refdata_value_fk'
    }

    static constraints = {
        user       (nullable: false)
        key        (nullable: false)
        strValue   (nullable: true)
        rdValue    (nullable: true)

        strValue(unique: ['user', 'key'])
        rdValue(unique: ['user', 'key'])
    }

    /*
        returns user depending setting for given key
     */
    static get(User user, KEYS key) {

        def uss = findWhere(user: user, key: key)
        uss ?: SETTING_NOT_FOUND
    }

    /*
        adds user depending setting (with value) for given key
     */
    static add(User user, KEYS key, def value) {

        def uss = new UserSettings(user: user, key: key)
        uss.setValue(value)
        uss.save(flush: true)

        uss
    }

    /*
        deletes user depending setting for given key
     */
    static delete(User user, KEYS key) {

        def uss = findWhere(user: user, key: key)
        uss.delete(flush: true)
    }

    /*
        gets parsed value by key.type
     */
    def getValue() {

        def result = null

        switch (key.type) {
            case Long:
                result = Long.parseLong(strValue)
                break
            case Org:
                result = Org.get(strValue)
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
