package de.laser

//@Transactional
class YpsService {

    ContextService contextService

    String getNavTemplatePath() {
        return contextService.getOrg().isCustomerType_Administration() ? 'nav_yps' : 'nav'
    }

}
