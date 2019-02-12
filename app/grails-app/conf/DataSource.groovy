//import org.grails.plugin.hibernate.filter.HibernateFilterDomainConfiguration

dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}

hibernate {
    default_schema                  = "public"
    cache.use_second_level_cache    = true
    cache.use_query_cache           = false // LEGACY
    //cache.region.factory_class      = 'org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory' // hibernate4: CAUTION: USE FOR DB-MIGRATION-PLUGIN
    cache.region.factory_class    = 'net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory' // LEGACY: CAUTION: USE FOR DB-MIGRATION-PLUGIN
}

// environment specific settings
environments {
    development {
        dataSource {
            dbCreate        = "update"
            driverClassName = "org.postgresql.Driver"
            dialect         = "org.hibernate.dialect.PostgreSQLDialect"
            username        = "laser"
            password        = "laser"
            url             = "jdbc:postgresql://localhost:5432/laser"
            //configClass     = HibernateFilterDomainConfiguration.class
            pooled          = true
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
            dbCreate        = "create-drop"
            driverClassName = "org.postgresql.Driver"
            dialect         = "org.hibernate.dialect.PostgreSQLDialect"
            username        = "laser"
            password        = "laser"
            url             = "jdbc:postgresql://localhost:5432/laser"
            //configClass     = HibernateFilterDomainConfiguration.class
            pooled          = true
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
            dbCreate        = "none" // disabled due database migration plugin; overwritten on dev-server
            driverClassName = "org.postgresql.Driver"
            dialect         = "org.hibernate.dialect.PostgreSQLDialect"
            username        = "laser"
            password        = "laser"
            url             = "jdbc:postgresql://localhost:5432/laser"
            //configClass     = HibernateFilterDomainConfiguration.class
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
