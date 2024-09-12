package de.laser

import de.laser.storage.BeanStore
import grails.gorm.transactions.Transactional
import grails.gsp.PageRenderer

@Transactional
class HelpService {

    ContextService contextService

    List<String> getActiveMappings() {

        [
            'dev_frontend',
            'finance_subFinancialData',
            'myInstitution_financeImport',
            'myInstitution_subscriptionImport',
            'subscription_show'
        ]
    }

    String getMapping(String controllerName, String actionName) {
        controllerName + '_' + actionName
    }

    boolean isActiveMapped(String controllerName, String actionName) {
        String pattern = getMapping(controllerName, actionName)
        getActiveMappings().contains(pattern)
    }

//    String getFlyoutContent(String controllerName, String actionName) {
//        try {
//            String pattern = getMapping(controllerName, actionName)
//            PageRenderer gpr = BeanStore.getGroovyPageRenderer()
//            gpr.render(template: '/help/flyouts/' + pattern)
//        }
//        catch (Exception e) {
//            e.getMessage()
//        }
//    }
}