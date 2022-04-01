package de.laser.system

import de.laser.auth.User
import de.laser.helper.BeanStore
import de.laser.helper.EhcacheWrapper

/**
 * This class keeps track of the active users for a given time point.
 * It reflects overall activity of users and permits to record when the system is most used
 */
class SystemActivityProfiler {

    static final String KEY_ACTIVE_USER = 'systemActivityProfiler/activeUser'

    Integer userCount
    Date dateCreated

    static mapping = {
        id          column:'sap_id'
        version     false
        userCount   column:'sap_user_count'
        dateCreated column:'sap_date_created'
    }

    static constraints = { }

    /**
     * This is a cronjob-triggered method to record the next sample of users
     */
    static void update() {
        withTransaction {
            int userCount = getNumberOfActiveUsers()
            if (userCount > 0) {
                new SystemActivityProfiler(userCount: userCount).save()
            }
        }
    }

    static void addActiveUser(User user) {
        EhcacheWrapper cache = BeanStore.getCacheService().getTTL1800Cache( KEY_ACTIVE_USER )
        cache.put(user.id.encodeAsMD5() as String, System.currentTimeMillis())
    }
    static void removeActiveUser(User user) {
        EhcacheWrapper cache = BeanStore.getCacheService().getTTL1800Cache( KEY_ACTIVE_USER )
        cache.remove(user.id.encodeAsMD5() as String)
    }

    static List<String> getActiveUsers(long ms) {
        EhcacheWrapper cache = BeanStore.getCacheService().getTTL1800Cache( KEY_ACTIVE_USER )
        List result = []
        cache.getKeys().each{ k ->
            String key = k.replaceFirst( KEY_ACTIVE_USER + EhcacheWrapper.SEPARATOR, '' )
            if (System.currentTimeMillis() - (cache.get(key) as Long) <= ms) {
                result.add( key )
            }
        }
        result
    }

    static int getNumberOfActiveUsers() {
        getActiveUsers( (1000 * 60 * 10) ).size() // 10 minutes
    }
}
