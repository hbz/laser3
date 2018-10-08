package com.k_int.kbplus

import com.k_int.kbplus.auth.User

import javax.persistence.Transient

class UserSettings {

    final static SETTING_NOT_FOUND = "SETTING_NOT_FOUND"

    @Transient
    def genericOIDService

    static enum KEYS {
        PAGE_SIZE (Long),
        DASHBOARD (Org),
        SHOW_SIMPLE_VIEWS (RefdataValue, 'YN')

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
        key        column:'us_key'
        strValue   column:'us_string_value'
        rdValue    column:'us_refdata_value_fk'
    }

    static constraints = {
        user       (nullable: false)
        key        (nullable: false)
        strValue   (nullable: true)
        rdValue    (nullable: true)
    }

    static get(User user, KEYS key) {

        def uss = findWhere(user: user, key: key)
        uss ?: SETTING_NOT_FOUND
    }

    static add(User user, KEYS key, def value) {

        def uss = new UserSettings(user: user, key: key)
        uss.setValue(value)
        uss.save(flush: true)

        uss
    }

    static delete(User user, KEYS key) {

        def uss = findWhere(user: user, key: key)
        uss.delete(flush: true)
    }

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

    def setValue(def value) {

        switch (key.type) {
            case RefdataValue:
                rdValue = value
                break
            default:
                strValue = (value ? value.toString() : null)
                break
        }
    }
}
