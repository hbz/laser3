<%@ page import="de.laser.utils.AppUtils; de.laser.UserSetting; de.laser.RefdataValue; de.laser.auth.User; de.laser.auth.UserOrg; de.laser.storage.RDStore; de.laser.storage.RDConstants;" %>
<%@ page import="org.grails.web.util.GrailsApplicationAttributes;org.springframework.web.servlet.LocaleResolver;org.springframework.web.servlet.support.RequestContextUtils;" %>

<laser:serviceInjection />

<%
    // -- part 1
    // -- set in semanticUI.gsp (scope: page)

    currentServer   = AppUtils.getCurrentServer()
    currentUser     = contextService.getUser()
    currentLang     = 'de'
    currentTheme    = 'laser'

    if (currentUser) {
        RefdataValue rdvLocale = currentUser.getSetting(UserSetting.KEYS.LANGUAGE, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE))?.getValue()

        if (rdvLocale) {
            currentLang = rdvLocale.value
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request)
            localeResolver.setLocale(request, response, new Locale(currentLang, currentLang.toUpperCase()))
        }

        RefdataValue rdvTheme = currentUser.getSetting(UserSetting.KEYS.THEME, RefdataValue.getByValueAndCategory('laser', RDConstants.USER_SETTING_THEME))?.getValue()
        if (rdvTheme) {
            currentTheme = rdvTheme.value
        }
    }

    // -- part 2
    // -- set in semanticUI.gsp (scope: page)

    contextOrg          = contextService.getOrg()
    contextUser         = contextService.getUser()
    contextMemberships  = contextService.getMemberships()

%>