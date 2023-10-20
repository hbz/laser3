package de.laser.system

import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import de.laser.storage.BeanStore

/**
 * This class keeps track of the active users for a given time point.
 * It reflects overall activity of users and permits to record when the system is most used
 */
class SystemActivityProfiler {

    static final String CACHE_KEY_ACTIVE_USER = 'SystemActivityProfiler/activeUser'

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

    /**
     * Records an active user for three minutes
     * @param user the {@link User} whose activity should be recorded
     */
    static void addActiveUser(User user) {
        if (user) {
            EhcacheWrapper ttl1800 = BeanStore.getCacheService().getTTL1800Cache(CACHE_KEY_ACTIVE_USER)
            ttl1800.put(user.id.encodeAsMD5() as String, System.currentTimeMillis())
        }
    }

    /**
     * Removes a given user from the activity tracker cache
     * @param user the {@link User} to be removed
     */
    static void removeActiveUser(User user) {
        if (user) {
            EhcacheWrapper ttl1800 = BeanStore.getCacheService().getTTL1800Cache(CACHE_KEY_ACTIVE_USER)
            ttl1800.remove(user.id.encodeAsMD5() as String)
        }
    }

    /**
     * Gets all currently registered users who have been active since the given timespan
     * @param ms the number of milliseconds since last activity
     * @return a {@link List} of user hash keys who have been active
     */
    static List<String> getActiveUsers(long ms) {
        EhcacheWrapper ttl1800 = BeanStore.getCacheService().getTTL1800Cache( CACHE_KEY_ACTIVE_USER )
        List result = []
        ttl1800.getKeys().each{ k ->
            try {
                String key = k.replaceFirst( CACHE_KEY_ACTIVE_USER + EhcacheWrapper.SEPARATOR, '' )
                if (System.currentTimeMillis() - (ttl1800.get(key) as Long) <= ms) {
                    result.add( key )
                }
            } catch (Exception e) {
            }
        }
        result
    }

    /**
     * Gets the number of active users in the last ten minutes
     * @return the number of registered (= cached) sessions
     * @see #getActiveUsers
     */
    static int getNumberOfActiveUsers() {
        getActiveUsers( (1000 * 60 * 10) ).size() // 10 minutes
    }
}
