<%@ page import="de.laser.storage.RDStore; de.laser.utils.AppUtils; de.laser.UserSetting; de.laser.RefdataValue; de.laser.auth.User; de.laser.storage.RDConstants;" %>
<%@ page import="org.springframework.web.servlet.LocaleResolver;org.springframework.web.servlet.support.RequestContextUtils;" %>

<laser:serviceInjection />

<%
    // -- (scope: page) set in laser.gsp

    currentServer   = AppUtils.getCurrentServer()
    currentLang     = 'de'
    currentTheme    = 'laser'

    contextUser         = contextService.getUser()
    contextOrg          = contextService.getOrg()

    if (contextUser) {
        RefdataValue rdvLocale = contextUser.getSetting(UserSetting.KEYS.LANGUAGE, RDStore.LANGUAGE_DE)?.getValue()

        if (rdvLocale) {
            currentLang = rdvLocale.value
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request)
            localeResolver.setLocale(request, response, new Locale(currentLang, currentLang.toUpperCase()))
        }

        RefdataValue rdvTheme = contextUser.getSetting(UserSetting.KEYS.THEME, RefdataValue.getByValueAndCategory('laser', RDConstants.USER_SETTING_THEME))?.getValue()
        if (rdvTheme) {
            currentTheme = rdvTheme.value
        }
    }
%>