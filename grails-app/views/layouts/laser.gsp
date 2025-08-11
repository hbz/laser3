<%@ page import="de.laser.api.v0.ApiManager; de.laser.ui.Icon; org.springframework.web.servlet.support.RequestContextUtils; de.laser.config.ConfigMapper; de.laser.CustomerTypeService; de.laser.helper.Profiler; de.laser.utils.AppUtils; grails.util.Environment; de.laser.system.SystemActivityProfiler; de.laser.FormService; de.laser.system.SystemSetting; de.laser.UserSetting; de.laser.RefdataValue; de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.Org;de.laser.auth.User;de.laser.system.SystemMessage; org.grails.orm.hibernate.cfg.GrailsHibernateUtil" %>
<!doctype html>

<laser:serviceInjection />
<g:set var="currentServer" scope="page" value="${AppUtils.getCurrentServer()}" />
<g:set var="currentLang" scope="page" />
<g:set var="currentTheme" scope="page" />
<g:set var="contextUser" scope="page" value="${contextService.getUser()}" />

<%
    currentLang     = 'de'
    currentTheme    = 'laser'

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
    <meta name="viewport" content="initial-scale = 1.0">

    <asset:stylesheet src="${currentTheme}.css"/>%{-- dont move --}%

    <laser:javascript src="base.js"/>%{-- dont move --}%
    <script data-type="fixed">
        <g:render template="/templates/jspc/jspc.js" />%{-- g:render; dont move --}%
    </script>

    <g:layoutHead/>

    <g:render template="/layouts/favicon" />
    <style>
        main > nav.buttons > .button { display: none; }
    </style>
</head>

<body class="${controllerName}_${actionName}">
    %{-- skip to main content, bypass menu block (for screen reader) related to https://www.w3.org/TR/WCAG20-TECHS/G1.html--}%
    <ui:skipLink />

    %{-- main menu --}%
    <nav id="mainMenue" class="ui fixed inverted menu la-js-verticalNavi" role="menubar">
        <div class="ui container" role="none">
            <ui:link addItemAttributes="true" controller="home" aria-label="${message(code:'default.home.label')}" class="header item la-logo-item">
                <img alt="Logo Laser" class="logo" src="${resource(dir: 'images', file: 'laser.svg')}"/>
            </ui:link>

            %{-- menu: public, my objects, my institution --}%

            <g:if test="${contextService.getOrg()}">
                <g:if test="${contextService.getOrg().isCustomerType_Support()}">
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

            %{-- menu: devDocs --}%

            <sec:ifAnyGranted roles="ROLE_ADMIN">
                <g:if test="${currentServer in [AppUtils.LOCAL, AppUtils.DEV]}">
                    <laser:render template="/layouts/laser/menu_devDocs" />
                </g:if>
            </sec:ifAnyGranted>

            <div class="right menu la-right-menuPart">

                %{-- menu: global search --}%

                <div role="search" id="mainSearch" class="ui category search spotlight">
                    <div class="ui icon input">
                        <input id="spotlightSearch" class="prompt" type="search" placeholder="${message(code:'spotlight.search.placeholder')}"
                               aria-label="${message(code:'spotlight.search.placeholder')}">
                        <i class="${Icon.SYM.SEARCH}" id="btn-search"></i>
                    </div>
                    <div class="results" style="overflow-y:scroll;max-height: 400px;"></div>
                </div>

                        <ui:link addItemAttributes="true" class="icon la-search-advanced la-popup-tooltip" controller="search" action="index"
                                 data-content="${message(code: 'search.advancedSearch.tooltip')}">
                            <i class="${Icon.SYM.SEARCH_ADVANCED} large"></i>
                        </ui:link>

                %{-- menu: user --}%

                <g:if test="${contextUser}">
                    <laser:render template="/layouts/laser/menu_user" />
                </g:if>
            </div>

            <sec:ifNotGranted roles="ROLE_USER">
                <sec:ifLoggedIn>
                    <ui:link addItemAttributes="true" controller="logout">${message(code:'menu.user.logout')}</ui:link>
                </sec:ifLoggedIn>
            </sec:ifNotGranted>

        </div><!-- container -->

    </nav><!-- main menu -->

        %{-- context bar --}%

        <sec:ifAnyGranted roles="ROLE_USER">
            <laser:render template="/layouts/laser/newContextBar" />
        </sec:ifAnyGranted>

        %{-- global content container --}%

        <div class="pusher">

            %{-- system server indicator --}%
            <laser:render template="/templates/system/serverIndicator" />

            <main id="mainContent" class="ui main container hidden">
                <sec:ifAnyGranted roles="ROLE_ADMIN">%{-- TMP ONLY --}%
                    <g:if test="${currentServer in [AppUtils.LOCAL, AppUtils.DEV] && (institution || contextOrg || orgInstance)}">
                        <div id="dev-tmp-help">
                            institution               : ${institution?.getName()} <br />
                            contextOrg                : ${contextOrg?.getName()} <br />
                            orgInstance               : ${orgInstance?.getName()} <br />
                            inContextOrg              : <strong>${inContextOrg}</strong> <br />
                            institutionalView         : <strong>${institutionalView}</strong> <br />
                            consortialView            : <strong>${consortialView}</strong>
                        </div>
                        <style>
                        #dev-tmp-help {
                            position: absolute;
                            top: 10px;
                            right: 0;
                            height: 5em;
                            padding: 0.25em 1em;
                            border: 1px solid red;
                            overflow: hidden;
                            z-index: 0;
                        }
                        #dev-tmp-help:hover {
                            height: auto;
                            background-color: #f4f8f9;
                            z-index: 1111;
                        }
                        </style>
                    </g:if>
                </sec:ifAnyGranted>

                %{-- systemMessages: TYPE_GLOBAL --}%

                <laser:render template="/templates/system/messages" model="${[type: SystemMessage.TYPE_GLOBAL]}"/>

                %{-- content --}%

                <g:layoutBody/>

                %{-- system info --}%

                <sec:ifAnyGranted roles="ROLE_ADMIN">
                    <div id="system-profiler" class="ui label hidden la-debugInfos">
                        <i class="clock outline icon"></i> <span></span>
                    </div>
                </sec:ifAnyGranted>
                <button style="display:none" class="ui icon button huge la-scrollTopButton" id="la-js-topButton">
                    <i class="angle double up icon"></i>
                </button>
            </main>
        </div>

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

        %{-- system maintenance mode --}%

        <g:if test="${SystemSetting.findByName('MaintenanceMode').value == 'true'}">
            <laser:render template="/templates/system/maintenanceMode" />
        </g:if>

        %{-- ??? --}%

        <% if(! flash.redirectFrom) { flash.clear() } %>

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

                $('#la-js-topButton').on('click', function() {
                    scrollToTop()
                });
                // Function to scroll up
                function scrollToTop() {
                    $('html, body').animate({ scrollTop: 0 }, 'slow');
                }
                // show button only if scrolling down on long sites
                $(window).scroll(function() {
                    if ( $(this).scrollTop() > 200 ) {
                        $('button#la-js-topButton').stop().fadeTo('slow',1);
                    } else {
                        $('button#la-js-topButton').stop().fadeTo('slow',0);
                    }
                });
                if ( $('#system-profiler').length || $('#showDebugInfo').length ) {

                }
                else{
                    $('button#la-js-topButton').css("bottom","10px");
                }
            })
        </script>
    </body>
</html>
