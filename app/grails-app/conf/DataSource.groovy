dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}

hibernate { // to hibernate 4
    cache.use_second_level_cache = true
    cache.use_query_cache = false  // LEGACY
    //cache.use_query_cache = true // hibernate 4
    //cache.region.factory_class = 'org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory' // hibernate4: CAUTION: USE FOR DB-MIGRATION-PLUGIN
    cache.region.factory_class = 'net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory' // LEGACY: CAUTION: USE FOR DB-MIGRATION-PLUGIN
}

// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "update"
            driverClassName = "com.mysql.jdbc.Driver"
            dialect = "org.hibernate.dialect.MySQL5Dialect"
            username = "laser"
            password = "laser"
            url = "jdbc:mysql://localhost/laser?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8"
            pooled = true
            properties {
                maxActive = -1
                minEvictableIdleTimeMillis=1800000
                timeBetweenEvictionRunsMillis=1800000
                numTestsPerEvictionRun=3
                testOnBorrow=true
                testWhileIdle=true
                testOnReturn=true
                validationQuery="select 1"
            }
        }
    }
    test {
        dataSource {
            dbCreate = "create-drop"
            driverClassName = "com.mysql.jdbc.Driver"
            dialect = "org.hibernate.dialect.MySQL5Dialect"
            username = "laser"
            password = "laser"
            url = "jdbc:mysql://localhost/laserTest?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8"
            pooled = true
            properties {
                maxActive = -1
                minEvictableIdleTimeMillis=1800000
                timeBetweenEvictionRunsMillis=1800000
                numTestsPerEvictionRun=3
                testOnBorrow=true
                testWhileIdle=true
                testOnReturn=true
                validationQuery="select 1"
            }
        }
    }
    production {
        dataSource {
            dbCreate = "none" // disabled due database migration plugin; overwritten on dev-server
            driverClassName = "com.mysql.jdbc.Driver"
            username = "laser"
            password = "laser"
            url = "jdbc:mysql://localhost/laser?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8"
            pooled = true
            dialect = "org.hibernate.dialect.MySQL5Dialect"
            properties {
                maxActive = -1
                minEvictableIdleTimeMillis=1800000
                timeBetweenEvictionRunsMillis=1800000
                numTestsPerEvictionRun=3
                testOnBorrow=true
                testWhileIdle=true
                testOnReturn=true
                validationQuery="select 1"
            }
        }
    }
}
