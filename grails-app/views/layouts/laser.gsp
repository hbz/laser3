<%@ page import="de.laser.helper.Profiler; de.laser.utils.AppUtils; grails.util.Environment; de.laser.system.SystemActivityProfiler; de.laser.FormService; de.laser.system.SystemSetting; de.laser.UserSetting; de.laser.RefdataValue; de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.Org;de.laser.auth.User;de.laser.system.SystemMessage; org.grails.orm.hibernate.cfg.GrailsHibernateUtil" %>
<!doctype html>

<laser:serviceInjection />
<g:set var="currentServer" scope="page" />
<g:set var="currentLang" scope="page" />
<g:set var="currentTheme" scope="page" />
<g:set var="contextOrg" scope="page" />
<g:set var="contextUser" scope="page" />
<tmpl:/layouts/initVars />

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
        <g:render template="/templates/jspc/jspc.dict.js" />%{-- g:render; dont move --}%
    </script>

    <g:layoutHead/>

    <tmpl:/layouts/favicon />
</head>

<body class="${controllerName}_${actionName}">

    <g:if test="${currentServer == AppUtils.LOCAL}">
        <div class="ui yellow label big la-server-label" aria-label="${message(code:'ariaLabel.serverIdentification.local')}"></div>
    </g:if>
    <g:if test="${currentServer == AppUtils.DEV}">
        <div class="ui green label big la-server-label" aria-label="${message(code:'ariaLabel.serverIdentification.dev')}"></div>
    </g:if>
    <g:if test="${currentServer == AppUtils.QA}">
        <div class="ui red label big la-server-label" aria-label="${message(code:'ariaLabel.serverIdentification.qa')}"></div>
    </g:if>

    <g:set var="visibilityContextOrgMenu" value="la-hide-context-orgMenu" />
%{--    <nav aria-label="${message(code:'wcag.label.mainMenu')}">--}%
        <div id="mainMenue" class="ui fixed inverted  menu la-js-verticalNavi" role="menubar" >
            <div class="ui container" role="none">
                <ui:link generateElementId="true" role="menuitem" controller="home" aria-label="${message(code:'default.home.label')}" class="header item la-logo-item">
                    <img alt="Logo Laser" class="logo" src="${resource(dir: 'images', file: 'laser.svg')}"/>
                </ui:link>

                <sec:ifAnyGranted roles="ROLE_USER">

                    <g:if test="${contextOrg}">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                                ${message(code:'menu.public')} <i class="dropdown icon"></i>
                            </a>
                            <div class="menu" role="menu">

                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="package" action="index">${message(code:'menu.public.all_pkg')}</ui:link>
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="title" action="index">${message(code:'menu.public.all_titles')}</ui:link>

                                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_EDITOR">
                                        <ui:link generateElementId="true" role="menuitem" controller="organisation" action="index">${message(code:'menu.public.all_orgs')}</ui:link>
                                    </sec:ifAnyGranted>

                                    <g:if test="${accessService.checkPermAffiliationX('ORG_CONSORTIUM','INST_USER','ROLE_ADMIN,ROLE_ORG_EDITOR')}">
                                        <ui:link generateElementId="true" role="menuitem" controller="organisation" action="listInstitution">${message(code:'menu.public.all_insts')}</ui:link>
                                    </g:if>
                                    <g:elseif test="${accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_USER')}">
                                        <ui:link generateElementId="true" role="menuitem" controller="organisation" action="listConsortia">${message(code:'menu.public.all_cons')}</ui:link>
                                    </g:elseif>

                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="organisation" action="listProvider">${message(code:'menu.public.all_providers')}</ui:link>
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="platform" action="list">${message(code:'menu.public.all_platforms')}</ui:link>

                                    <div class="divider"></div>
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="gasco">${message(code:'menu.public.gasco_monitor')}</ui:link>

                                    <a id="wekb" href="${message(code:'url.wekb.' + currentServer)}" target="_blank" class="item" role="menuitem"><i class="ui icon la-gokb"></i> we:kb</a>
                                    <a id="ygor" href="${message(code:'url.ygor.' + currentServer)}" target="_blank" class="item" role="menuitem">YGOR</a>
                            </div>
                        </div>

                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                                ${message(code:'menu.my')} <i class="dropdown icon"></i>
                            </a>
                            <div class="menu" role="menu">

                                <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentSubscriptions" message="menu.my.subscriptions" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentLicenses" message="menu.my.licenses" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentProviders" message="menu.my.providers" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentPlatforms" message="menu.my.platforms" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentPackages" message="menu.my.packages" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentTitles" message="menu.my.titles" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="documents" message="menu.my.documents" />

                                <div class="divider"></div>
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="subscriptionsManagement" message="menu.my.subscriptionsManagement" />

                                <g:if test="${accessService.checkPerm('ORG_BASIC_MEMBER')}">
                                    <div class="divider"></div>
                                    <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentSurveys" message="menu.my.surveys" />
                                </g:if>

                                <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">

                                    <g:if test="${workflowService.isAccessibleForCurrentUser()}"><!-- TODO: workflows-permissions -->
                                        <div class="divider"></div>
                                        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentWorkflows" message="menu.my.workflows" />
                                    </g:if>

                                    <div class="divider"></div>
                                    <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="survey" action="workflowsSurveysConsortia" message="menu.my.surveys" />

                                    <div class="divider"></div>
                                    <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" specRole="ROLE_ADMIN,ROLE_ORG_EDITOR" action="manageMembers" message="menu.my.insts" />
                                    <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" specRole="ROLE_ADMIN" action="manageConsortiaSubscriptions" message="menu.my.consortiaSubscriptions" />
                                </g:if>
                                <g:elseif test="${accessService.checkPerm('ORG_BASIC_MEMBER')}">
                                    <div class="divider"></div>
                                    <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentConsortia" message="menu.my.consortia" />
                                </g:elseif>

                                <div class="divider"></div>
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="compare" action="compareSubscriptions" message="menu.my.comp_sub" />

                                <div class="divider"></div>
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="compare" action="compareLicenses" message="menu.my.comp_lic" />

                            </div>
                        </div>

                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                                ${message(code:'menu.institutions.myInst')} <i class="dropdown icon"></i>
                            </a>

                            <div class="menu" role="menu">
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="dashboard" message="menu.institutions.dash" />

                                <ui:link generateElementId="true" class="item" role="menuitem" controller="organisation" action="show" params="[id: contextOrg?.id]">${message(code:'menu.institutions.org_info')}</ui:link>

                                <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="addressbook" message="menu.institutions.myAddressbook" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="tasks" message="task.plural" />
                                <%--<ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="changes" message="menu.institutions.changes" />--%>
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="managePrivatePropertyDefinitions" message="menu.institutions.manage_props" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="finance" message="menu.institutions.finance" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" specRole="ROLE_ADMIN" controller="myInstitution" action="budgetCodes" message="menu.institutions.budgetCodes" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" specRole="ROLE_ADMIN" controller="costConfiguration" action="index" message="menu.institutions.costConfiguration" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_EDITOR" specRole="ROLE_ADMIN" controller="myInstitution" action="financeImport" message="menu.institutions.financeImport" />

                                <div class="divider"></div>
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_ADM" controller="myInstitution" action="users" message="menu.institutions.users" />
                                <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="reporting" message="menu.institutions.reporting" />
                            </div>
                        </div>
                    </g:if>

                    <sec:ifAnyGranted roles="ROLE_ORG_MANAGER,ROLE_ADMIN,ROLE_STATISTICS_EDITOR">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                                ${message(code:'menu.datamanager')} <i class="dropdown icon"></i>
                            </a>

                            <div class="menu" role="menu">
                                <sec:ifAnyGranted roles="ROLE_STATISTICS_EDITOR">
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="usage"
                                            action="index">${message(code: 'menu.datamanager.manage_usage_stats')}</ui:link>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="dataManager" action="index">${message(code:'default.dashboard')}</ui:link>
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="dataManager"
                                            action="deletedTitles">${message(code: 'datamanager.deletedTitleManagement.label')}</ui:link>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ORG_MANAGER,ROLE_ADMIN">
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="dataManager"
                                            action="deletedOrgs">${message(code: 'datamanager.deletedOrgManagement.label')}</ui:link>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <div class="divider"></div>

                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="package" action="list">${message(code:'menu.datamanager.searchPackages')}</ui:link>
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="platform" action="list">${message(code:'menu.datamanager.searchPlatforms')}</ui:link>

                                    <div class="divider"></div>

                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="subscription" action="compare">${message(code:'menu.datamanager.compareSubscriptions')}</ui:link>
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="onixplLicenseCompare" action="index">${message(code:'menu.institutions.comp_onix')}</ui:link>
                                    <div class="divider"></div>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="dataManager" action="checkPackageTIPPs">Tipps Check of we:kb and LAS:eR</ui:link>
                                    <div class="divider"></div>
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="dataManager" action="listMailTemplates">Mail Templates</ui:link>
                                </sec:ifAnyGranted>
                            </div>
                        </div>
                    </sec:ifAnyGranted>

                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                                ${message(code:'menu.admin')} <i class="dropdown icon"></i>
                            </a>

                            <div class="menu" role="menu">
                                <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="index">${message(code:'default.dashboard')}</ui:link>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        <i class="ui icon keyboard outline"></i> ${message(code:'menu.admin.sysAdmin')} <i class="dropdown icon"></i>
                                    </div>

                                    <div class="menu" role="menu">

                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="appInfo">${message(code:'menu.admin.appInfo')}</ui:link>
                                        <ui:link generateElementId="true" class="item" controller="admin" action="systemEvents">${message(code:'menu.admin.systemEvents')}</ui:link>
                                        <ui:link generateElementId="true" class="item" controller="admin" action="testMailSending">Test Mail Sending</ui:link>

                                        <div class="divider"></div>

                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="triggerHousekeeping" onclick="return confirm('${message(code:'confirm.start.HouseKeeping')}')">${message(code:'menu.admin.triggerHousekeeping')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="initiateCoreMigration" onclick="return confirm('${message(code:'confirm.start.CoreMigration')}')">${message(code:'menu.admin.coreMigration')}</ui:link>
                                        %{--<ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="dataCleanse" onclick="return confirm('${message(code:'confirm.start.DataCleaningNominalPlatforms')}')">Run Data Cleaning (Nominal Platforms)</ui:link>--}%
                                    </div>
                                </div>

                                <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="systemMessages"><i class="icon exclamation circle"></i>${message(code: 'menu.admin.systemMessage')}</ui:link>
                                <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="systemAnnouncements"><i class="icon flag"></i>${message(code:'menu.admin.announcements')}</ui:link>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'org.plural.label')} <i class="dropdown icon"></i>
                                    </div>

                                    <div class="menu" role="menu">
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="organisation" action="index">${message(code:'menu.admin.allOrganisations')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="manageOrganisations">${message(code:'menu.admin.manageOrganisations')}</ui:link>
                                    </div>
                                </div>

                                <ui:link generateElementId="true" class="item" role="menuitem" controller="user" action="list">${message(code:'menu.institutions.users')}</ui:link>
                                <ui:link generateElementId="true" class="item" role="menuitem" controller="usage">${message(code:'menu.admin.manageUsageStats')}</ui:link>
                                <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="forceSendNotifications">${message(code:'menu.admin.sendNotifications')}</ui:link>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'menu.admin.bulkOps')} <i class="dropdown icon"></i>
                                    </div>

                                   <div class="menu" role="menu">
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="titleEnrichment">Title Enrichment</ui:link>
                                    </div>
                                </div>
                                <div class="divider"></div>

                                <sec:ifAnyGranted roles="ROLE_ADMIN"><!-- TODO: workflows-permissions -->
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="manageWorkflows">${message(code:'menu.admin.manageWorkflows')}</ui:link>
                                </sec:ifAnyGranted>

                                <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="manageNamespaces">${message(code:'menu.admin.manageIdentifierNamespaces')}</ui:link>
                                <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="managePropertyDefinitions">${message(code:'menu.admin.managePropertyDefinitions')}</ui:link>
                                <%--<ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="managePropertyGroups">${message(code:'menu.institutions.manage_prop_groups')}</ui:link>--%> <%-- property groups are always private?? --%>
                                <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="manageRefdatas">${message(code:'menu.admin.manageRefdatas')}</ui:link>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        <i class="ui icon code branch"></i> <span class="text">Developer</span> <i class="dropdown icon"></i>
                                    </div>

                                    <div class="menu" role="menu">
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="dev" action="frontend">Frontend</ui:link>
                                    </div>
                                </div>

                                <div class="divider"></div>

                                <ui:link generateElementId="true" class="item" role="menuitem" controller="stats" action="statsHome">${message(code:'menu.admin.statistics')}</ui:link>
                            </div>
                        </div>
                    </sec:ifAnyGranted>

                    <sec:ifAnyGranted roles="ROLE_YODA">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                                ${message(code:'menu.yoda')} <i class="dropdown icon"></i>
                            </a>

                            <div class="menu" role="menu">

                                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="index">${message(code:'default.dashboard')}</ui:link>

                                <div class="item " role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        <i class="ui icon keyboard outline"></i> ${message(code:'menu.yoda.engine')} <i class="dropdown icon"></i>
                                    </div>

                                    <div class="menu" role="menu">

                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="systemSettings"><i class="icon toggle on"></i>${message(code:'menu.yoda.systemSettings')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="systemEvents">${message(code:'menu.admin.systemEvents')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="systemConfiguration">${message(code:'menu.yoda.systemConfiguration')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="systemThreads">${message(code:'menu.yoda.systemThreads')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="systemQuartz">${message(code:'menu.yoda.systemQuartz')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="systemCache">${message(code:'menu.yoda.systemCache')}</ui:link>

                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="profilerLoadtime"><i class="stopwatch icon"></i>${message(code:'menu.yoda.profilerLoadtime')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="profilerActivity"><i class="stopwatch icon"></i>${message(code:'menu.yoda.profilerActivity')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="profilerTimeline"><i class="stopwatch icon"></i>${message(code:'menu.yoda.profilerTimeline')}</ui:link>

                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="appControllers">${message(code:'menu.yoda.appControllers')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="userRoleMatrix">${message(code:'menu.yoda.userRoleMatrix')}</ui:link>

                                    </div>
                                </div>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'myinst.dash.due_dates.label')} <i class="dropdown icon"></i>
                                    </div>
                                    <div class="menu" role="menu">
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="dueDates_updateDashboardDB">${message(code:'menu.admin.updateDashboardTable')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="dueDates_sendAllEmails">${message(code:'menu.admin.sendEmailsForDueDates')}</ui:link>
                                    </div>
                                </div>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        Statistik <i class="dropdown icon"></i>
                                    </div>
                                    <div class="menu" role="menu">

                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="statsSync">${message(code:'menu.admin.stats.sync')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="manageStatsSources">Übersicht der Statistik-Cursor</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="fetchStats" params="[(FormService.FORM_SERVICE_TOKEN): formService.getNewToken(), incremental: true]">${message(code:'menu.admin.stats.fetch.incremental')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="fetchStats" params="[(FormService.FORM_SERVICE_TOKEN): formService.getNewToken(), incremental: false]">${message(code:'menu.admin.stats.fetch')}</ui:link>
                                    </div>
                                </div>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'menu.admin.syncManagement')} <i class="dropdown icon"></i>
                                    </div>
                                    <div class="menu" role="menu">
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="globalSync" onclick="return confirm('${message(code:'confirm.start.globalDataSync')}')">${message(code:'menu.yoda.globalDataSync')}</ui:link>
                                        <div class="item" role="menuitem" aria-haspopup="true">
                                            <div class="title">
                                                ${message(code:'menu.admin.syncManagement.reload')} <i class="dropdown icon"></i>
                                            </div>
                                            <div class="menu" role="menu">
                                                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'identifier']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateIdentifiers')}</ui:link>
                                                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'editionStatement']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateEditionStatement')}</ui:link>
                                                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'iemedium']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateIEMedium')}</ui:link>
                                                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'ddc']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateDDC')}</ui:link>
                                                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'language']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateLanguage')}</ui:link>
                                                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'accessType']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateAccessType')}</ui:link>
                                                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'openAccess']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateOpenAccess')}</ui:link>
                                                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'titleNamespace']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateTitleNamespace')}</ui:link>
                                            </div>
                                        </div>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="manageGlobalSources">${message(code:'menu.yoda.manageGlobalSources')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="getTIPPsWithoutGOKBId">${message(code:'menu.yoda.purgeTIPPsWithoutGOKBID')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="expungeRemovedTIPPs" onclick="return confirm('${message(code:'confirmation.content.deleteTIPPsWithoutGOKBId')}')">${message(code:'menu.yoda.expungeRemovedTIPPs')}</ui:link>
                                        <%--<ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="matchPackageHoldings">${message(code:'menu.admin.bulkOps.matchPackageHoldings')}</ui:link>--%>
                                    </div>
                                </div>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'elasticsearch.label')} <i class="dropdown icon"></i>
                                    </div>
                                    <div class="menu" role="menu">
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="esIndexUpdate" onclick="return confirm('${message(code:'confirm.start.ESUpdateIndex')}')">${message(code:'menu.yoda.updateESIndex')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="manageESSources">Manage ES Source</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="manageFTControl">Manage FTControl</ui:link>
                                        <div class="divider"></div>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="fullReset" onclick="return confirm('${message(code:'confirm.start.resetESIndex')}')">${message(code:'menu.yoda.resetESIndex')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="killDataloadService">Kill ES Update Index</ui:link>
                                        <div class="divider"></div>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="createESIndices">Create ES Indices</ui:link>
                                    </div>
                                </div>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'menu.admin.dataManagement')} <i class="dropdown icon"></i>
                                    </div>

                                    <div class="menu" role="menu">
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="dataManager" action="listPlatformDuplicates">List Platform Duplicates</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="package" action="getDuplicatePackages">List Package Duplicates</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="dataManager" action="listDeletedTIPPS">List TIPP Duplicates and deleted TIPPs</ui:link>
                                        <%--<ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="userMerge">${message(code:'menu.admin.userMerge')}</ui:link>--%>
                                        <%--<ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="hardDeletePkgs">${message(code:'menu.admin.hardDeletePkgs')}</ui:link>--%>

                                        <div class="divider"></div>

                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="databaseInfo">${message(code: "menu.admin.databaseInfo")}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="databaseCollations">${message(code: "menu.admin.databaseCollations")}</ui:link>
                                        <div class="divider"></div>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="dataConsistency">${message(code: "menu.admin.dataConsistency")}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="fileConsistency">${message(code: "menu.admin.fileConsistency")}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="manageDeletedObjects">${message(code: "menu.admin.deletedObjects")}</ui:link>
                                    </div>
                                </div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'menu.admin.dataMigration')} <i class="dropdown icon"></i>
                                    </div>
                                    <div class="menu" role="menu">
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="checkOrgLicRoles"><g:message code="menu.admin.checkOrgLicRoles"/></ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="dbmFixPrivateProperties">Fix Private Properties</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="surveyCheck">Update Survey Status</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="subscriptionCheck">${message(code:'menu.admin.subscriptionsCheck')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="freezeSubscriptionHoldings">${message(code:'menu.admin.freezeSubscriptionHoldings')}</ui:link>
                                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="dropDeletedObjects">Drop deleted Objects from Database</ui:link>
                                        <%--<ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="correctCostsInLocalCurrency" params="[dryRun: true]">${message(code:'menu.admin.correctCostsInLocalCurrencyDryRun')}</ui:link>
                                        <ui:link generateElementId="true" class="item role="menuitem" js-open-confirm-modal"
                                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.correctCostsInLocalCurrency')}"
                                                data-confirm-term-how="ok"
                                                controller="yoda" action="correctCostsInLocalCurrency" params="[dryRun: false]">${message(code:'menu.admin.correctCostsInLocalCurrencyDoIt')}</ui:link>--%>

                                    </div>
                                </div>
                            </div>

                        </div>
                    </sec:ifAnyGranted>

                    <div class="right menu la-right-menuPart">
                        <div role="search" id="mainSearch" class="ui category search spotlight">
                            <div class="ui icon input">
                                <input  aria-label="${message(code:'spotlight.search.placeholder')}" type="search" id="spotlightSearch" class="prompt" placeholder="${message(code:'spotlight.search.placeholder')}">
                                <i id="btn-search" class="search icon"></i>
                            </div>
                            <div class="results" style="overflow-y:scroll;max-height: 400px;"></div>
                        </div>

                        <ui:link generateElementId="true" controller="search" action="index"
                                class="la-search-advanced la-popup-tooltip la-delay"
                                 data-content="${message(code: 'search.advancedSearch.tooltip')}">
                            <i class="large icons">
                                <i class="search icon"></i>
                                <i class="top right grey corner cog icon "></i>
                            </i>
                        </ui:link>

                        <g:if test="${contextUser}">
                            <div class="ui dropdown item la-noBorder" role="menuitem" aria-haspopup="true">
                                <a class="title">
                                    ${contextUser.displayName} <i class="dropdown icon"></i>
                                </a>

                                <div class="menu" role="menu">

                                    <g:set var="usaf" value="${contextUser.getAffiliationOrgs()}" />
                                    <g:if test="${usaf && usaf.size() > 0}">
                                        <g:each in="${usaf}" var="orgRaw">
                                            <g:set var="org" value="${(Org) GrailsHibernateUtil.unwrapIfProxy(orgRaw)}"></g:set>
                                            <g:if test="${org.id == contextOrg?.id}">
                                                <ui:link generateElementId="true" class="item active" role="menuitem" controller="myInstitution" action="switchContext" params="${[oid:"${org.class.name}:${org.id}"]}">${org.name}</ui:link>
                                            </g:if>
                                            <g:else>
                                                <ui:link generateElementId="true" class="item" role="menuitem" controller="myInstitution" action="switchContext" params="${[oid:"${org.class.name}:${org.id}"]}">${org.name}</ui:link>
                                            </g:else>
                                        </g:each>
                                    </g:if>

                                    <div class="divider"></div>

                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="profile" action="index">${message(code:'profile.user')}</ui:link>
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="profile" action="help">${message(code:'menu.user.help')}</ui:link>
                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="profile" action="dsgvo">${message(code:'privacyNotice')}</ui:link>

                                    <div class="divider"></div>

                                    <ui:link generateElementId="true" class="item" role="menuitem" controller="logout">${message(code:'menu.user.logout')}</ui:link>
                                    <div class="divider"></div>
                                    <div class="header">Version: ${AppUtils.getMeta('info.app.version')} – ${AppUtils.getMeta('info.app.build.date')}</div>
                                    <div class="header">
                                        ${SystemActivityProfiler.getNumberOfActiveUsers()} Benutzer online
                                    </div>
                                </div>
                            </div>
                        </g:if>
                    </div>

                </sec:ifAnyGranted><%-- ROLE_USER --%>

                <sec:ifNotGranted roles="ROLE_USER">
                    <sec:ifLoggedIn>
                        <ui:link generateElementId="true" class="item" controller="logout">${message(code:'menu.user.logout')}</ui:link>
                    </sec:ifLoggedIn>
                </sec:ifNotGranted>

            </div><!-- container -->

        </div><!-- main menu -->
%{--    </nav>--}%

    <sec:ifAnyGranted roles="ROLE_USER">
        <g:set var="visibilityContextOrgMenu" value="la-show-context-orgMenu" />
        <nav class="ui fixed menu la-contextBar" aria-label="${message(code:'wcag.label.modeNavigation')}" >
            <div class="ui container">
                <button class="ui button big la-menue-button la-modern-button" style="display:none"><i class="bars icon"></i></button>
                <div class="ui sub header item la-context-org">${contextOrg?.name}</div>

                <div class="right menu la-advanced-view">
                    <div class="item">
                        <g:if test="${flagContentCache}">
                            <span class="ui icon button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.flagContentCache.tooltip')}" data-position="bottom right" data-variation="tiny">
                                <i class="hourglass end icon"></i>
                            </span>
                        </g:if>
                        <g:if test="${flagContentGokb}">
                            <span class="ui icon button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.flagContentGokb.tooltip')}" data-position="bottom right" data-variation="tiny">
                                <i class="cloud icon"></i>
                            </span>
                        </g:if>
                        <g:if test="${flagContentElasticsearch}">
                            <span class="ui icon button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.flagContentElasticsearch.tooltip')}" data-position="bottom right" data-variation="tiny">
                                <i class="cloud icon"></i>
                            </span>
                        </g:if>
                    </div>

                        <g:if test="${(controllerName=='dev' && actionName=='frontend' ) || (controllerName=='subscription'|| controllerName=='license') && actionName=='show' && (editable || accessService.checkPermAffiliationX('ORG_INST,ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN'))}">
                            <div class="item">
                                <g:if test="${user?.getSettingsValue(UserSetting.KEYS.SHOW_EDIT_MODE, RefdataValue.getByValueAndCategory('Yes', RDConstants.Y_N))?.value=='Yes'}">
                                    <button class="ui icon toggle active  button la-toggle-controls la-popup-tooltip la-delay" data-content="${message(code:'statusbar.showButtons.tooltip')}" data-position="bottom right">
                                        <i class="pencil alternate icon"></i>
                                    </button>
                                </g:if>
                                <g:else>
                                    <button class="ui icon toggle blue button la-modern-button la-toggle-controls la-popup-tooltip la-delay"  data-content="${message(code:'statusbar.hideButtons.tooltip')}"  data-position="bottom right">
                                        <i class="pencil alternate slash icon"></i>
                                    </button>
                                </g:else>
                            </div>
                        </g:if>


                        <g:if test="${(params.mode)}">
                            <div class="item">
                                <g:if test="${params.mode=='advanced'}">
                                    <div class="ui toggle la-toggle-advanced button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.showAdvancedView.tooltip')}" data-position="bottom right">
                                        <i class="icon plus square"></i>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div class="ui toggle la-toggle-advanced button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.showBasicView.tooltip')}" data-position="bottom right">
                                        <i class="icon plus square green slash"></i>
                                    </div>
                                </g:else>
                            </div>

                            <laser:script file="${this.getGroovyPageFileName()}">
                                JSPC.app.LaToggle = {};
                                JSPC.app.LaToggle.advanced = {};
                                JSPC.app.LaToggle.advanced.button = {};

                                // ready event
                                JSPC.app.LaToggle.advanced.button.ready = function() {
                                    // selector cache
                                    var $button = $('.button.la-toggle-advanced');
                                    var handler = {
                                        activate: function() {
                                            $icon = $(this).find('.icon');
                                            if ($icon.hasClass("slash")) {
                                                $icon.removeClass("slash");
                                                window.location.href = "<g:createLink action="${actionName}" params="${params + ['mode':'advanced']}" />";
                                            }
                                             else {
                                                $icon.addClass("slash");
                                                window.location.href = "<g:createLink action="${actionName}" params="${params + ['mode':'basic']}" />" ;
                                            }
                                        }
                                    };
                                    $button.on('click', handler.activate);
                                };

                                JSPC.app.LaToggle.advanced.button.ready();
                            </laser:script>
                        </g:if>

                        <g:if test="${controllerName == 'survey' && (actionName == 'currentSurveysConsortia' || actionName == 'workflowsSurveysConsortia')}">
                            <div class="item">
                                <g:if test="${actionName == 'workflowsSurveysConsortia'}">
                                    <g:link action="currentSurveysConsortia" controller="survey" class="ui button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.change.currentSurveysConsortiaView.tooltip')}" data-position="bottom right">
                                        <i class="exchange icon"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link action="workflowsSurveysConsortia" controller="survey" class="ui button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.change.workflowsSurveysConsortiaView.tooltip')}" data-position="bottom right">
                                        <i class="exchange icon"></i>
                                    </g:link>
                                </g:else>

                            </div>
                        </g:if>
                        <g:if test="${(controllerName=='subscription' && actionName=='show') || (controllerName=='dev' && actionName=='frontend')}">
                            <div class="item">
                                <button class="ui button blue la-modern-button la-help-panel-button"><i class="info circle large icon"></i></button>
                            </div>
                        </g:if>
                </div>

            </div>

        </nav><!-- Context Bar -->
    </sec:ifAnyGranted><%-- ROLE_USER --%>

    %{-- global content container --}%
        <div class="pusher">
            <main class="ui main container ${visibilityContextOrgMenu} hidden la-js-mainContent">

                %{-- system messages --}%

                <g:if test="${SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION)}">
                    <div id="systemMessages" class="ui message large warning">
                        <laser:render template="/templates/systemMessages" model="${[systemMessages: SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION)]}" />
                    </div>
                </g:if>
                <g:else>
                    <div id="systemMessages" class="ui message large warning hidden"></div>
                </g:else>

                %{-- content --}%

                <g:layoutBody/>

            </main><!-- .main -->
        </div>

        %{-- footer --}%

        <sec:ifNotGranted roles="ROLE_USER">
            <!-- Footer -->
            <laser:render template="/public/templates/footer" />
            <!-- Footer End -->
        </sec:ifNotGranted>

        %{-- global container for modals and ajax --}%

        <div id="dynamicModalContainer"></div>

        %{-- global loading indicator --}%

        <div id="loadingIndicator" style="display: none">
            <div class="ui inline medium text loader active">Aktualisiere Daten ..</div>
        </div>

        %{-- global confirmation modal --}%

        <ui:confirmationModal  />

        %{-- decksaver --}%

        <g:if test="${(controllerName=='dev' && actionName=='frontend' ) || (controllerName=='subscription'|| controllerName=='license') && actionName=='show'}">
            <laser:script file="${this.getGroovyPageFileName()}">
                <g:if test="${editable} || ${accessService.checkPermAffiliationX('ORG_INST,ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN')}">
                    <g:if test="${user?.getSettingsValue(UserSetting.KEYS.SHOW_EDIT_MODE, RefdataValue.getByValueAndCategory('Yes', RDConstants.Y_N))?.value == 'Yes'}">
                        deckSaver.configs.editMode  = true;
                    </g:if>
                    <g:else>
                        deckSaver.configs.editMode  = false;
                    </g:else>
                </g:if>
                <g:else>
                    deckSaver.configs.editMode  = false;
                </g:else>

                deckSaver.configs.ajaxUrl = '<g:createLink controller="ajax" action="toggleEditMode"/>';
                deckSaver.go();
            </laser:script>
        </g:if>

        %{-- maintenance --}%

        <div id="maintenance" class="${SystemSetting.findByName('MaintenanceMode').value != 'true' ? 'hidden' : ''}">
            <div class="ui segment center aligned inverted yellow">
                <h3 class="ui header"><i class="icon cogs"></i> ${message(code:'system.maintenanceMode.header')}</h3>

                ${message(code:'system.maintenanceMode.message')}
            </div>
        </div>

        %{-- system info --}%

        <% if(! flash.redirectFrom) { flash.clear() } %>

        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <ui:systemInfo />

            <div id="system-profiler" class="ui label hidden">
                <i class="clock icon"></i>
                <span></span>
            </div>
        </sec:ifAnyGranted>

        %{-- ajax login --}%

        <g:if test="${controllerName != 'login'}">
            <laser:render template="/templates/ajax/login" />
        </g:if>

        %{-- javascript loading --}%

        <laser:javascript src="${currentTheme}.js"/>%{-- dont move --}%

        <laser:scriptBlock/>%{-- dont move --}%

        %{-- profiler, jstk --}%

        <script data-type="fixed">
            $(document).ready(function() {
                system.profiler("${ Profiler.generateKey( webRequest )}");

                <g:if test="${Environment.isDevelopmentMode()}">
                    jstk.go();
                    console.log(JSPC);
                </g:if>
            })
        </script>
    </body>
</html>
