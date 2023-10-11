<%@ page import="org.springframework.web.servlet.support.RequestContextUtils; de.laser.config.ConfigMapper; de.laser.CustomerTypeService; de.laser.helper.Profiler; de.laser.utils.AppUtils; grails.util.Environment; de.laser.system.SystemActivityProfiler; de.laser.FormService; de.laser.system.SystemSetting; de.laser.UserSetting; de.laser.RefdataValue; de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.Org;de.laser.auth.User;de.laser.system.SystemMessage; org.grails.orm.hibernate.cfg.GrailsHibernateUtil" %>
<!doctype html>

<laser:serviceInjection />
<g:set var="currentServer" scope="page" />
<g:set var="currentLang" scope="page" />
<g:set var="currentTheme" scope="page" />
<g:set var="contextOrg" scope="page" />
<g:set var="contextUser" scope="page" />

<%
    currentServer   = AppUtils.getCurrentServer()
    currentLang     = 'de'
    currentTheme    = 'laser'

    contextUser     = contextService.getUser()
    contextOrg      = contextService.getOrg()

    if (contextUser) {
        RefdataValue rdvLocale = contextUser.getSetting(UserSetting.KEYS.LANGUAGE, RDStore.LANGUAGE_DE)?.getValue()

        if (rdvLocale) {
            currentLang = rdvLocale.value
            (RequestContextUtils.getLocaleResolver(request)).setLocale(request, response, new Locale(currentLang, currentLang.toUpperCase()))
        }

        RefdataValue rdvTheme = contextUser.getSetting(UserSetting.KEYS.THEME, RefdataValue.getByValueAndCategory('laser', RDConstants.USER_SETTING_THEME))?.getValue()
        if (rdvTheme) {
            currentTheme = rdvTheme.value
        }
    }
%>

<html lang="${currentLang}">

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title><g:layoutTitle default="${meta(name: 'app.name')}"/></title>
    <meta name="description" content="">
    <meta name="viewport" content="initial-scale = 1.0">

    <asset:stylesheet src="${currentTheme}.css"/>%{-- dont move --}%

    <laser:javascript src="base.js"/>%{-- dont move --}%
    <script data-type="fixed">
        <g:render template="/templates/jspc/jspc.js" />%{-- g:render; dont move --}%
    </script>

    <g:layoutHead/>

    <g:render template="/layouts/favicon" />
</head>

<body class="${controllerName}_${actionName}">

    %{-- system server indicator --}%

    <laser:render template="/templates/system/serverIndicator" />

    %{-- main menu --}%

    <g:set var="visibilityContextOrgMenu" value="la-hide-context-orgMenu" />
        <div id="mainMenue" class="ui fixed inverted menu la-js-verticalNavi" role="menubar">
            <div class="ui container" role="none">
                <ui:link addItemAttributes="true" controller="home" aria-label="${message(code:'default.home.label')}" class="header item la-logo-item">
                    <img alt="Logo Laser" class="logo" src="${resource(dir: 'images', file: 'laser.svg')}"/>
                </ui:link>

                <sec:ifAnyGranted roles="ROLE_USER">

                    %{-- menu: public, my objects, my institution --}%

                    <g:if test="${contextOrg}">
                        <g:if test="${contextOrg.isCustomerType_Support()}">
                            <laser:render template="/layouts/laser/menu_support" />
                        </g:if>
                        <g:else>
                            <laser:render template="/layouts/laser/menu_user_public" />
                            <laser:render template="/layouts/laser/menu_user_myObjects" />
                            <laser:render template="/layouts/laser/menu_user_myInstitution" />
                        </g:else>
                    </g:if>

                    %{-- menu: admin --}%

                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <laser:render template="/layouts/laser/menu_admin" />
                    </sec:ifAnyGranted>

                    %{-- menu: yoda --}%

                    <sec:ifAnyGranted roles="ROLE_YODA">
                        <laser:render template="/layouts/laser/menu_yoda" />
                    </sec:ifAnyGranted>

                    <div class="right menu la-right-menuPart">

                        %{-- menu: global search --}%

                        <div role="search" id="mainSearch" class="ui category search spotlight">
                            <div class="ui icon input">
                                <input id="spotlightSearch" class="prompt" type="search" placeholder="${message(code:'spotlight.search.placeholder')}"
                                       aria-label="${message(code:'spotlight.search.placeholder')}">
                                <i class="search icon" id="btn-search"></i>
                            </div>
                            <div class="results" style="overflow-y:scroll;max-height: 400px;"></div>
                        </div>

                        <ui:link addItemAttributes="true" class="la-search-advanced la-popup-tooltip la-delay" controller="search" action="index"
                                 data-content="${message(code: 'search.advancedSearch.tooltip')}">
                            <i class="large icons">
                                <i class="search icon"></i>
                                <i class="top right grey corner cog icon"></i>
                            </i>
                        </ui:link>

                        %{-- menu: context menu --}%

                        <g:if test="${contextUser}">
                            <div class="ui dropdown item la-noBorder" role="menuitem" aria-haspopup="true">
                                <a class="title">
                                    ${contextUser.displayName} <i class="dropdown icon"></i>
                                </a>

                                <div class="menu" role="menu">
                                    <ui:link addItemAttributes="true" controller="profile" action="index">${message(code:'profile.user')}</ui:link>
                                    <ui:link addItemAttributes="true" controller="profile" action="help">${message(code:'menu.user.help')}</ui:link>
                                    <ui:link addItemAttributes="true" controller="profile" action="dsgvo">${message(code:'privacyNotice')}</ui:link>

                                    <div class="divider"></div>

                                    <ui:link addItemAttributes="true" controller="logout">${message(code:'menu.user.logout')}</ui:link>
                                    <div class="divider"></div>
                                    <div class="header">Version: ${AppUtils.getMeta('info.app.version')} – ${AppUtils.getMeta('info.app.build.date')}</div>
                                    <div class="header">
                                        ${SystemActivityProfiler.getNumberOfActiveUsers()} Benutzer online
                                    </div>
                                </div>
                            </div>
                        </g:if>
                    </div>

                </sec:ifAnyGranted>

                <sec:ifNotGranted roles="ROLE_USER">
                    <sec:ifLoggedIn>
                        <ui:link addItemAttributes="true" controller="logout">${message(code:'menu.user.logout')}</ui:link>
                    </sec:ifLoggedIn>
                </sec:ifNotGranted>

            </div><!-- container -->

        </div><!-- main menu -->

        %{-- context bar --}%

        <sec:ifAnyGranted roles="ROLE_USER">
            <g:if test="${AppUtils.isPreviewOnly()}">
                <laser:render template="/layouts/laser/newContextBar" />
            </g:if>
            <g:else>
                <laser:render template="/layouts/laser/contextBar" />
            </g:else>

        </sec:ifAnyGranted>

        %{-- global content container --}%

        <div class="pusher">
            <main class="ui main container ${visibilityContextOrgMenu} hidden la-js-mainContent">

                %{-- system messages --}%

                <g:if test="${SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION)}">
                    <div id="systemMessages" class="ui message large warning">
                        <laser:render template="/templates/system/messages" model="${[systemMessages: SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION)]}" />
                    </div>
                </g:if>
                <g:else>
                    <div id="systemMessages" class="ui message large warning hidden"></div>
                </g:else>

                %{-- content --}%

                <g:layoutBody/>

                %{-- system info --}%

                <sec:ifAnyGranted roles="ROLE_ADMIN">
                    <g:if test="${ConfigMapper.getShowSystemInfo()}">
                        <laser:render template="/templates/system/info" />
                    </g:if>

                    <div id="system-profiler" class="ui label hidden la-debugInfos">
                        <i class="clock icon"></i> <span></span>
                    </div>
                </sec:ifAnyGranted>

            </main>
        </div>

        %{-- footer --}%

        <sec:ifNotGranted roles="ROLE_USER">
            <laser:render template="/public/footer" />
        </sec:ifNotGranted>

        %{-- global container for modals and ajax --}%

        <div id="dynamicModalContainer"></div>

        %{-- global page dimmer --}%

        <div id="globalPageDimmer" class="ui page dimmer"></div>

        %{-- global loading indicator --}%

        <div id="globalLoadingIndicator">
            <div class="ui inline medium text loader active">Aktualisiere Daten ..</div>
        </div>

        %{-- global confirmation modal --}%

        <ui:confirmationModal  />

        %{-- decksaver --}%

        <g:if test="${(controllerName=='dev' && actionName=='frontend' ) || (controllerName=='subscription'|| controllerName=='license') && actionName=='show'}">
            <laser:script file="${this.getGroovyPageFileName()}">
                <%
                    boolean isDeckSaverEditMode = contextUser.getSettingsValue(UserSetting.KEYS.SHOW_EDIT_MODE, RDStore.YN_YES).value == 'Yes' &&
                                                  (editable || contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC ))
                %>
                deckSaver.configs.editMode = ${isDeckSaverEditMode};
                deckSaver.configs.ajaxUrl = '<g:createLink controller="ajax" action="toggleEditMode"/>';
                deckSaver.go();
            </laser:script>
        </g:if>

        %{-- system maintenance mode --}%

        <laser:render template="/templates/system/maintenanceMode" />

        %{-- ??? --}%

        <% if(! flash.redirectFrom) { flash.clear() } %>

        %{-- ajax login --}%

        <g:if test="${controllerName != 'home'}">
            <laser:render template="/templates/system/ajaxLogin" />
        </g:if>

        %{-- javascript loading --}%

        <laser:javascript src="${currentTheme}.js"/>%{-- dont move --}%

        <laser:scriptBlock/>%{-- dont move --}%

        %{-- profiler, why --}%

        <script data-type="fixed">
            $(document).ready(function() {
                system.profiler("${ Profiler.generateKey( webRequest )}");
                $('.ui.flyout').prependTo('body');

                <g:if test="${currentServer == AppUtils.LOCAL}">
                console.log(JSPC);
                why.info(false);
                </g:if>

                JSPC.app.workaround_targetBlank = function(e) { e.stopPropagation() }
            })
        </script>
    </body>
</html>
