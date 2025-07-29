package de.laser.storage

import de.laser.BootStrapService
import de.laser.CustomerTypeService
import de.laser.ESWrapperService
import de.laser.FileCryptService
import de.laser.GenericOIDService
import de.laser.GlobalSourceSyncService
import de.laser.GokbService
import de.laser.HelpService
import de.laser.MailSendService
import de.laser.PendingChangeService
import de.laser.AccessService
import de.laser.AuditService
import de.laser.CacheService
import de.laser.CascadingUpdateService
import de.laser.ContextService
import de.laser.DeletionService
import de.laser.EscapeService
import de.laser.FinanceService
import de.laser.LicenseService
import de.laser.LinksGenerationService
import de.laser.OrganisationService
import de.laser.PropertyService
import de.laser.ProviderService
import de.laser.ShareService
import de.laser.SubscriptionService
import de.laser.SubscriptionsQueryService
import de.laser.SurveyService
import de.laser.SystemService
import de.laser.UserService
import de.laser.VendorService
import de.laser.WekbNewsService
import de.laser.WorkflowService
import de.laser.YodaService
import de.laser.ctrl.FinanceControllerService
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugins.mail.MailService
import grails.util.Holders
import grails.web.mapping.UrlMappingsHolder
import groovy.transform.CompileStatic
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.context.MessageSource

import javax.sql.DataSource

/**
 * This class contains loading pipes for services and configuration parameters
 */
@CompileStatic
class BeanStore {

    /**
     * Gets and loads the given bean from the application context holder
     * @param bean the bean name to load
     * @return the defined context bean
     */
    static def get(String bean) {
        Holders.grailsApplication.mainContext.getBean(bean)
    }

    // -- Grails --

    /**
     * Returns the application tag library
     * @return the {@link ApplicationTagLib} holding for the application context
     */
    static ApplicationTagLib getApplicationTagLib() {
        Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
    }

    /**
     * Returns the principal data source bean
     * @return the currently holding principal {@link DataSource}
     */
    static DataSource getDataSource() {
        Holders.grailsApplication.mainContext.getBean('dataSource') as DataSource
    }

    /**
     * Returns the secondary storage data source bean
     * @return the currently holding secondary storage {@link DataSource}
     */
    static DataSource getStorageDataSource() {
        Holders.grailsApplication.mainContext.getBean('dataSource_storage') as DataSource
    }

    /**
     * Returns the Groovy page renderer
     * @return the currently valid {@link PageRenderer}
     */
    static PageRenderer getGroovyPageRenderer() {
        Holders.grailsApplication.mainContext.getBean('groovyPageRenderer') as PageRenderer
    }

    /**
     * Returns the message source loader which enabled access to the message resource bundle
     * @return the {@link MessageSource} container
     */
    static MessageSource getMessageSource() {
        MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource') as MessageSource

        // in progress
//        if (false) {
//            messageSource.metaClass.getMessage = { String code ->
//                getMessageSource().getMessage(code, null, code, de.laser.utils.LocaleUtils.getCurrentLocale())
//            }
//            messageSource.metaClass.getMessage = { String code, Object[] args ->
//                getMessageSource().getMessage(code, args, code, de.laser.utils.LocaleUtils.getCurrentLocale())
//            }
//            messageSource.metaClass.getMessage = { MessageSourceResolvable resolvable ->
//                getMessageSource().getMessage(resolvable, de.laser.utils.LocaleUtils.getCurrentLocale())
//            }
//        }
        messageSource
    }

    /**
     * Returns the Spring security service, containing principal security and access rights checkup methods
     * @return the currently valid {@link SpringSecurityService}
     */
    static SpringSecurityService getSpringSecurityService() {
        Holders.grailsApplication.mainContext.getBean('springSecurityService') as SpringSecurityService
    }

    /**
     * Returns the Grails URL mappings holder
     * @return the currently valid {@link UrlMappingsHolder}
     */
    static UrlMappingsHolder getUrlMappingsHolder() {
        Holders.grailsApplication.mainContext.getBean('grailsUrlMappingsHolder') as UrlMappingsHolder
    }

    // -- Laser --

    /**
     * @return the currently holding {@link AccessService}
     */
    static AccessService getAccessService() {
        Holders.grailsApplication.mainContext.getBean('accessService') as AccessService
    }

    /**
     * @return the currently holding {@link AuditService}
     */
    static AuditService getAuditService() {
        Holders.grailsApplication.mainContext.getBean('auditService') as AuditService
    }

    /**
     * @return the currently holding {@link BootStrapService}
     */
    static BootStrapService getBootStrapService() {
        Holders.grailsApplication.mainContext.getBean('bootStrapService') as BootStrapService
    }

    /**
     * @return the currently holding {@link CacheService}
     */
    static CacheService getCacheService() {
        Holders.grailsApplication.mainContext.getBean('cacheService') as CacheService
    }

    /**
     * @return the currently holding {@link CascadingUpdateService}
     */
    static CascadingUpdateService getCascadingUpdateService() {
        Holders.grailsApplication.mainContext.getBean('cascadingUpdateService') as CascadingUpdateService
    }

    /**
     * @return the currently holding {@link ContextService}
     */
    static ContextService getContextService() {
        Holders.grailsApplication.mainContext.getBean('contextService') as ContextService
    }

    /**
     * @return the currently holding {@link CustomerTypeService}
     */
    static CustomerTypeService getCustomerTypeService() {
        Holders.grailsApplication.mainContext.getBean('customerTypeService') as CustomerTypeService
    }

    /**
     * @return the currently holding {@link DeletionService}
     */
    static DeletionService getDeletionService() {
        Holders.grailsApplication.mainContext.getBean('deletionService') as DeletionService
    }

    /**
     * @return the currently holding {@link EscapeService}
     */
    static EscapeService getEscapeService() {
        Holders.grailsApplication.mainContext.getBean('escapeService') as EscapeService
    }

    /**
     * @return the currently holding {@link ESWrapperService}
     */
    static ESWrapperService getESWrapperService() {
        Holders.grailsApplication.mainContext.getBean('ESWrapperService') as ESWrapperService
    }

    static FileCryptService getFileCryptService() {
        Holders.grailsApplication.mainContext.getBean('fileCryptService') as FileCryptService
    }

    /**
     * @return the currently holding {@link FinanceControllerService}
     */
    static FinanceControllerService getFinanceControllerService() {
        Holders.grailsApplication.mainContext.getBean('financeControllerService') as FinanceControllerService
    }

    /**
     * @return the currently holding {@link FinanceService}
     */
    static FinanceService getFinanceService() {
        Holders.grailsApplication.mainContext.getBean('financeService') as FinanceService
    }

    /**
     * @return the currently holding {@link GenericOIDService}
     */
    static GenericOIDService getGenericOIDService() {
        Holders.grailsApplication.mainContext.getBean('genericOIDService') as GenericOIDService
    }

    /**
     * @return the currently holding {@link GlobalSourceSyncService}
     */
    static GlobalSourceSyncService getGlobalSourceSyncService() {
        Holders.grailsApplication.mainContext.getBean('globalSourceSyncService') as GlobalSourceSyncService
    }

    /**
     * @return the currently holding {@link GokbService}
     */
    static GokbService getGokbService() {
        Holders.grailsApplication.mainContext.getBean('gokbService') as GokbService
    }

    static HelpService getHelpService() {
        Holders.grailsApplication.mainContext.getBean('helpService') as HelpService
    }

    /**
     * @return the currently holding {@link LicenseService}
     */
    static LicenseService getLicenseService() {
        Holders.grailsApplication.mainContext.getBean('licenseService') as LicenseService
    }

    /**
     * @return the currently holding {@link LinksGenerationService}
     */
    static LinksGenerationService getLinksGenerationService() {
        Holders.grailsApplication.mainContext.getBean('linksGenerationService') as LinksGenerationService
    }

    /**
     * @return the currently holding {@link MailService}
     */
    static MailService getMailService() {
        Holders.grailsApplication.mainContext.getBean('mailService') as MailService
    }

    /**
     * @return the currently holding {@link MailSendService}
     */
    static MailSendService getMailSendService() {
        Holders.grailsApplication.mainContext.getBean('mailSendService') as MailSendService
    }

    /**
     * @return the currently holding {@link OrganisationService}
     */
    static OrganisationService getOrganisationService() {
        Holders.grailsApplication.mainContext.getBean('organisationService') as OrganisationService
    }

    /**
     * @return the currently holding {@link PendingChangeService}
     */
    static PendingChangeService getPendingChangeService() {
        Holders.grailsApplication.mainContext.getBean('pendingChangeService') as PendingChangeService
    }

    static ProviderService getProviderService() {
        Holders.grailsApplication.mainContext.getBean('providerService') as ProviderService
    }

    /**
     * @return the currently holding {@link PropertyService}
     */
    static PropertyService getPropertyService() {
        Holders.grailsApplication.mainContext.getBean('propertyService') as PropertyService
    }

    /**
     * @return the currently holding {@link ShareService}
     */
    static ShareService getShareService() {
        Holders.grailsApplication.mainContext.getBean('shareService') as ShareService
    }

    /**
     * @return the currently holding {@link SubscriptionService}
     */
    static SubscriptionService getSubscriptionService() {
        Holders.grailsApplication.mainContext.getBean('subscriptionService') as SubscriptionService
    }

    /**
     * @return the currently holding {@link SubscriptionsQueryService}
     */
    static SubscriptionsQueryService getSubscriptionsQueryService() {
        Holders.grailsApplication.mainContext.getBean('subscriptionsQueryService') as SubscriptionsQueryService
    }

    /**
     * @return the currently holding {@link SurveyService}
     */
    static SurveyService getSurveyService() {
        Holders.grailsApplication.mainContext.getBean('surveyService') as SurveyService
    }

    /**
     * @return the currently holding {@link SystemService}
     */
    static SystemService getSystemService() {
        Holders.grailsApplication.mainContext.getBean('systemService') as SystemService
    }

    /**
     * @return the currently holding {@link UserService}
     */
    static UserService getUserService() {
        Holders.grailsApplication.mainContext.getBean('userService') as UserService
    }

    static VendorService getVendorService() {
        Holders.grailsApplication.mainContext.getBean('vendorService') as VendorService
    }

    static WekbNewsService getWekbNewsService() {
        Holders.grailsApplication.mainContext.getBean('wekbNewsService') as WekbNewsService
    }

    /**
     * @return the currently holding {@link WorkflowService}
     */
    static WorkflowService getWorkflowService() {
        Holders.grailsApplication.mainContext.getBean('workflowService') as WorkflowService
    }

    /**
     * @return the currently holding {@link YodaService}
     */
    static YodaService getYodaService() {
        Holders.grailsApplication.mainContext.getBean('yodaService') as YodaService
    }
}
