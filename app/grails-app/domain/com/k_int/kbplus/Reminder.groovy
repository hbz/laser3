package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.helper.RefdataAnnotation

/**
 * @author Ryan@k-int.com
 */
class Reminder {

    User    user     //Linked to
    Boolean active //Is in use or disabled via user
    Integer amount   //e.g. 3 days before
    Date lastUpdated
    Date lastRan  //i.e. successful email operation

    @RefdataAnnotation(cat = '?')
    RefdataValue  reminderMethod   //email

    @RefdataAnnotation(cat = '?')
    RefdataValue  unit     //day, week, month

    @RefdataAnnotation(cat = '?')
    RefdataValue  trigger  //Subscription manual renewal date

    static constraints = {
        reminderMethod  nullable: false, blank: false
        unit    nullable: false, blank: false
        trigger nullable: false, blank: false
        amount  nullable: false, blank: false
        active  nullable: false, blank: false
        lastRan nullable: true, blank:false
    }

    static mapping = {
        sort lastUpdated: "desc"
    }

    static  belongsTo = User
}
