<%@ page import="de.laser.helper.ServerUtils; com.k_int.kbplus.auth.User;com.k_int.kbplus.UserSettings;com.k_int.kbplus.auth.UserOrg;com.k_int.kbplus.SystemTicket" %>
<%@ page import="com.k_int.kbplus.RefdataValue;de.laser.helper.RDStore;de.laser.helper.RDConstants;" %>
<%@ page import="org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;org.springframework.web.servlet.LocaleResolver;org.springframework.web.servlet.support.RequestContextUtils;" %>

<laser:serviceInjection />

<%
    // -- part 1
    // -- set in semanticUI.gsp (scope: page)

    currentServer   = ServerUtils.getCurrentServer()
    currentUser     = contextService.getUser()
    currentLang     = 'de'
    currentTheme    = 'semanticUI'

    if (currentUser) {
        RefdataValue rdvLocale = currentUser.getSetting(UserSettings.KEYS.LANGUAGE, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE))?.getValue()

        if (rdvLocale) {
            currentLang = rdvLocale.value
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request)
            localeResolver.setLocale(request, response, new Locale(currentLang, currentLang.toUpperCase()))
        }

        RefdataValue rdvTheme = currentUser.getSetting(UserSettings.KEYS.THEME, RefdataValue.getByValueAndCategory('semanticUI', RDConstants.USER_SETTING_THEME))?.getValue()

        if (rdvTheme) {
            currentTheme = rdvTheme.value
        }
    }

    // -- part 2
    // -- set in semanticUI.gsp (scope: page)

    contextOrg          = contextService.getOrg()
    contextUser         = contextService.getUser()
    contextMemberships  = contextService.getMemberships()

    // -- part 3
    // -- set in semanticUI.gsp (scope: page)

    newTickets      = SystemTicket.getNew()
    myInstNewAffils = UserOrg.findAllByStatusAndOrg(0, contextOrg, [sort: 'dateRequested'])

%>