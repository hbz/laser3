package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.helper.RefdataAnnotation
import org.springframework.context.MessageSource

import javax.persistence.Transient

class PendingChange {

    @Transient
    def genericOIDService

    final static PROP_LICENSE       = 'license'
    final static PROP_PKG           = 'pkg'
    final static PROP_SUBSCRIPTION  = 'subscription'

    final static MSG_LI01 = 'pendingChange.message_LI01'
    final static MSG_LI02 = 'pendingChange.message_LI02'
    final static MSG_SU01 = 'pendingChange.message_SU01'
    final static MSG_SU02 = 'pendingChange.message_SU02'

    @Transient
    MessageSource messageSource

    Subscription subscription
    License license
    SystemObject systemObject
    Package pkg
    Date ts
    Org owner
    String oid
    String changeDoc
    String msgToken
    String msgParams

    @Deprecated
    String desc

    @RefdataAnnotation(cat = 'PendingChangeStatus')
    RefdataValue status

    Date actionDate
    User user


    static mapping = {
        systemObject column:'pc_sys_obj'
        subscription column:'pc_sub_fk'
            license column:'pc_lic_fk'
                pkg column:'pc_pkg_fk'
                oid column:'pc_oid', index:'pending_change_oid_idx'
          changeDoc column:'pc_change_doc', type:'text'
           msgToken column:'pc_msg_token'
          msgParams column:'pc_msg_doc', type:'text'
                 ts column:'pc_ts'
              owner column:'pc_owner'
               desc column:'pc_desc', type:'text'
             status column:'pc_status_rdv_fk'
         actionDate column:'pc_action_date'
               user column:'pc_action_user_fk'
               sort "ts":"asc"
    }

    static constraints = {
        systemObject(nullable:true, blank:false);
        subscription(nullable:true, blank:false);
        license(nullable:true, blank:false);
        changeDoc(nullable:true, blank:false);
        msgToken(nullable:true, blank:false)
        msgParams(nullable:true, blank:false)
        pkg(nullable:true, blank:false);
        ts(nullable:true, blank:false);
        owner(nullable:true, blank:false);
        oid(nullable:true, blank:false);
        desc(nullable:true, blank:false);
        status(nullable:true, blank:false);
        actionDate(nullable:true, blank:false);
        user(nullable:true, blank:false);
    }

    def resolveOID() {
        genericOIDService.resolveOID(oid)
    }

    def getMessage() {

    }

    def getParsedParams() {

        def locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
        def parsedParams = grails.converters.JSON.parse(msgParams)

        // def value type

        def type = parsedParams[0]
        parsedParams.removeAt(0)

        // find attr translation

        def prefix = ''

        if (msgToken in ['pendingChange.message_LI01']) {
            prefix = 'license.'
        }
        if (msgToken in ['pendingChange.message_SU01']) {
            prefix = 'subscription.'
        }

        if (prefix) {
            def parsed
            try {
                parsed = messageSource.getMessage(prefix + parsedParams[0], null, locale)
            }
            catch (Exception e1) {
                try {
                    parsed = messageSource.getMessage(prefix + parsedParams[0] + '.label', null, locale)
                }
                catch (Exception e2) {
                    parsed = prefix + parsedParams[0]
                }
            }
            parsedParams[0] = parsed
        }

        // resolve oid id for custom properties

        if (msgToken in ['pendingChange.message_LI02', 'pendingChange.message_SU02']) {

            def pd = genericOIDService.resolveOID(parsedParams[0])
            if (pd) {
                parsedParams[0] = pd.getI10n('name')
            }
        }

        // parse values

        if (type == 'rdv') {
            def rdv1 = genericOIDService.resolveOID(parsedParams[1])
            def rdv2 = genericOIDService.resolveOID(parsedParams[2])

            parsedParams[1] = rdv1.getI10n('value')
            parsedParams[2] = rdv2.getI10n('value')
        }
        else if (type == 'date') {
            //def sdf = new java.text.SimpleDateFormat(messageSource.getMessage('default.date.format', null, locale))
            //TODO JSON @ Wed Jan 03 00:00:00 CET 2018

            //def date1 = parsedParams[1] ? sdf.parse(parsedParams[1]) : null
            //def date2 = parsedParams[2] ? sdf.parse(parsedParams[2]) : null

            //parsedParams[1] = date1
            //parsedParams[2] = date2
        }

        parsedParams
    }
}
