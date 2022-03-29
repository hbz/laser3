package de.laser.helper

import com.k_int.kbplus.GenericOIDService
import de.laser.AccessService
import de.laser.CacheService
import de.laser.ContextService
import de.laser.FinanceService
import de.laser.LicenseService
import de.laser.LinksGenerationService
import de.laser.OrganisationService
import de.laser.SubscriptionsQueryService
import de.laser.SystemService
import de.laser.ctrl.FinanceControllerService
import grails.gsp.PageRenderer
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.context.MessageSource

import javax.sql.DataSource

class BeanStore {

    // -- Grails --

    static ApplicationTagLib getApplicationTagLib() {
        Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
    }
    static DataSource getDataSource() {
        Holders.grailsApplication.mainContext.getBean('dataSource') as DataSource
    }
    static PageRenderer getGroovyPageRenderer() {
        Holders.grailsApplication.mainContext.getBean('groovyPageRenderer') as PageRenderer
    }
    static MessageSource getMessageSource() {
        // PluginAwareResourceBundleMessageSource
        Holders.grailsApplication.mainContext.getBean('messageSource') as MessageSource
    }

    // -- Laser --

    static AccessService getAccessService() {
        Holders.grailsApplication.mainContext.getBean('accessService') as AccessService
    }
    static CacheService getCacheService() {
        Holders.grailsApplication.mainContext.getBean('cacheService') as CacheService
    }
    static ContextService getContextService() {
        Holders.grailsApplication.mainContext.getBean('contextService') as ContextService
    }
    static FinanceControllerService getFinanceControllerService() {
        Holders.grailsApplication.mainContext.getBean('financeControllerService') as FinanceControllerService
    }
    static FinanceService getFinanceService() {
        Holders.grailsApplication.mainContext.getBean('financeService') as FinanceService
    }
    static GenericOIDService getGenericOIDService() {
        Holders.grailsApplication.mainContext.getBean('genericOIDService') as GenericOIDService
    }
    static LicenseService getLicenseService() {
        Holders.grailsApplication.mainContext.getBean('licenseService') as LicenseService
    }
    static LinksGenerationService getLinksGenerationService() {
        Holders.grailsApplication.mainContext.getBean('linksGenerationService') as LinksGenerationService
    }
    static OrganisationService getOrganisationService() {
        Holders.grailsApplication.mainContext.getBean('organisationService') as OrganisationService
    }
    static SubscriptionsQueryService getSubscriptionsQueryService() {
        Holders.grailsApplication.mainContext.getBean('subscriptionsQueryService') as SubscriptionsQueryService
    }
    static SystemService getSystemService() {
        Holders.grailsApplication.mainContext.getBean('systemService') as SystemService
    }
}
