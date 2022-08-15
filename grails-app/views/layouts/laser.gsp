<%@ page import="de.laser.FormService; de.laser.system.SystemSetting; de.laser.UserSetting; de.laser.RefdataValue; de.laser.helper.ProfilerUtils; de.laser.helper.ServerUtils; de.laser.helper.RDStore;de.laser.helper.RDConstants;org.grails.web.util.GrailsApplicationAttributes;de.laser.Org;de.laser.auth.User;de.laser.system.SystemMessage" %>
<!doctype html>

<laser:serviceInjection />
<g:set var="currentServer" scope="page" />
<g:set var="currentUser" scope="page" />
<g:set var="currentLang" scope="page" />
<g:set var="currentTheme" scope="page" />
<g:set var="contextOrg" scope="page" />
<g:set var="contextUser" scope="page" />
<g:set var="contextMemberships" scope="page" />
<g:set var="newTickets" scope="page" />
<tmpl:/layouts/initVars />

<html lang="${currentLang}">

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title><g:layoutTitle default="${meta(name: 'app.name')}"/></title>
    <meta name="description" content="">
    <meta name="viewport" content="initial-scale = 1.0">

    <asset:stylesheet src="${currentTheme}.css"/>%{-- dont move --}%

    <laser:javascript src="base.js"/>%{-- dont move --}%
    <script data-type="fix">
        <g:render template="/templates/javascript/jspc.js" />%{-- dont move --}%
        <g:render template="/templates/javascript/jspc.dict.js" />%{-- dont move --}%
    </script>

    <g:layoutHead/>

    <tmpl:/layouts/favicon />
</head>

<body class="${controllerName}_${actionName}">

    <g:if test="${currentServer == ServerUtils.SERVER_LOCAL}">
        <div class="ui yellow label big la-server-label" aria-label="${message(code:'ariaLabel.serverIdentification.local')}"></div>
    </g:if>
    <g:if test="${currentServer == ServerUtils.SERVER_DEV}">
        <div class="ui green label big la-server-label" aria-label="${message(code:'ariaLabel.serverIdentification.dev')}"></div>
    </g:if>
    <g:if test="${currentServer == ServerUtils.SERVER_QA}">
        <div class="ui red label big la-server-label" aria-label="${message(code:'ariaLabel.serverIdentification.qa')}"></div>
    </g:if>

    <g:set var="visibilityContextOrgMenu" value="la-hide-context-orgMenu" />
%{--    <nav aria-label="${message(code:'wcag.label.mainMenu')}">--}%
        <div id="mainMenue" class="ui fixed inverted  menu la-js-verticalNavi" role="menubar" >
            <div class="ui container" role="none">
                <semui:link generateElementId="true" role="menuitem" controller="home" aria-label="${message(code:'default.home.label')}" class="header item la-logo-item">
                    <img alt="Logo Laser" class="logo" src="${resource(dir: 'images', file: 'laser.svg')}"/>
                </semui:link>

                <sec:ifAnyGranted roles="ROLE_USER">

                    <g:if test="${contextOrg}">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                            ${message(code:'menu.public')}
                                <i  class="dropdown icon"></i>
                            </a>
                            <div class="menu" role="menu">
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="package" action="index">${message(code:'menu.public.all_pkg')}</semui:link>
                                    <semui:link generateElementId="true"  class="item" role="menuitem" controller="title" action="index">${message(code:'menu.public.all_titles')}</semui:link>


                                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_EDITOR">
                                        <semui:mainNavItem generateElementId="true" role="menuitem" controller="organisation" action="index" message="menu.public.all_orgs" />
                                    </sec:ifAnyGranted>

                                    <g:if test="${accessService.checkPermAffiliationX('ORG_CONSORTIUM','INST_USER','ROLE_ADMIN,ROLE_ORG_EDITOR')}">
                                        <semui:link generateElementId="true" role="menuitem" controller="organisation" action="listInstitution">${message(code:'menu.public.all_insts')}</semui:link>
                                    </g:if>
                                    <g:elseif test="${accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_USER')}">
                                        <semui:link generateElementId="true" role="menuitem" controller="organisation" action="listConsortia">${message(code:'menu.public.all_cons')}</semui:link>
                                    </g:elseif>

                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="organisation" action="listProvider">${message(code:'menu.public.all_providers')}</semui:link>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="platform" action="list">${message(code:'menu.public.all_platforms')}</semui:link>

                                    <div class="divider"></div>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="gasco">${message(code:'menu.public.gasco_monitor')}</semui:link>

                                    <a id="wekb" href="${message(code:'url.wekb.' + currentServer)}" target="_blank" class="item" role="menuitem"><i class="ui icon la-gokb"></i> we:kb</a>
                                    <a id="ygor" href="${message(code:'url.ygor.' + currentServer)}" target="_blank" class="item" role="menuitem">YGOR</a>
                            </div>
                        </div>

                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                                ${message(code:'menu.my')}
                                <i  class="dropdown icon"></i>
                            </a>
                            <div class="menu" role="menu">

                                <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentSubscriptions" message="menu.my.subscriptions" />
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentLicenses" message="menu.my.licenses" />
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentProviders" message="menu.my.providers" />
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentPlatforms" message="menu.my.platforms" />
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentPackages" message="menu.my.packages" />
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentTitles" message="menu.my.titles" />
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="documents" message="menu.my.documents" />

                                <div class="divider"></div>
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="subscriptionsManagement" message="menu.my.subscriptionsManagement" />


                                <g:if test="${accessService.checkPerm('ORG_BASIC_MEMBER')}">
                                    <div class="divider"></div>
                                    <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentSurveys" message="menu.my.surveys" />
                                </g:if>

                                <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">

                                    <sec:ifAnyGranted roles="ROLE_ADMIN"><!-- TODO: reporting-permissions -->
                                        <div class="divider"></div>
                                        <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentWorkflows" message="menu.my.workflows" />
                                    </sec:ifAnyGranted>

                                    <div class="divider"></div>
                                    <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="survey" action="workflowsSurveysConsortia" message="menu.my.surveys" />

                                    <div class="divider"></div>
                                    <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" specRole="ROLE_ADMIN,ROLE_ORG_EDITOR" action="manageMembers" message="menu.my.insts" />
                                    <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" specRole="ROLE_ADMIN" action="manageConsortiaSubscriptions" message="menu.my.consortiaSubscriptions" />
                                </g:if>
                                <g:elseif test="${accessService.checkPerm('ORG_BASIC_MEMBER')}">
                                    <div class="divider"></div>
                                    <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentConsortia" message="menu.my.consortia" />
                                </g:elseif>

                                <div class="divider"></div>
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="compare" action="compareSubscriptions" message="menu.my.comp_sub" />

                                <div class="divider"></div>
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="compare" action="compareLicenses" message="menu.my.comp_lic" />

                            </div>
                        </div>


                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                                ${message(code:'menu.institutions.myInst')}
                                <i  class="dropdown icon"></i>
                            </a>

                            <div class="menu" role="menu">
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="dashboard" message="menu.institutions.dash" />

                                <semui:link generateElementId="true" class="item" role="menuitem" controller="organisation" action="show" params="[id: contextOrg?.id]">${message(code:'menu.institutions.org_info')}</semui:link>

                                <semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="addressbook" message="menu.institutions.myAddressbook" />
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="tasks" message="task.plural" />
                                <%--<semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="changes" message="menu.institutions.changes" />--%>
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="managePrivatePropertyDefinitions" message="menu.institutions.manage_props" />
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="finance" message="menu.institutions.finance" />
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" specRole="ROLE_ADMIN" controller="myInstitution" action="budgetCodes" message="menu.institutions.budgetCodes" />
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" specRole="ROLE_ADMIN" controller="costConfiguration" action="index" message="menu.institutions.costConfiguration" />
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_EDITOR" specRole="ROLE_ADMIN" controller="myInstitution" action="financeImport" message="menu.institutions.financeImport" />

                                <div class="divider"></div>
                                <semui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_ADM" controller="myInstitution" action="users" message="menu.institutions.users" />

                                <semui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="reporting" message="menu.institutions.reporting" />

                                <sec:ifAnyGranted roles="ROLE_YODA">
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="myInstitution" action="changeLog">${message(code:'menu.institutions.change_log')}</semui:link>
                                    <%--<semui:securedMainNavItem generateElementId="true" affiliation="INST_EDITOR" controller="myInstitution" action="changeLog" message="menu.institutions.change_log" />--%>
                                </sec:ifAnyGranted>

                            </div>
                        </div>
                    </g:if>

                    <sec:ifAnyGranted roles="ROLE_ORG_MANAGER,ROLE_ADMIN,ROLE_GLOBAL_DATA,ROLE_STATISTICS_EDITOR">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                                ${message(code:'menu.datamanager')}
                                <i  class="dropdown icon"></i>
                            </a>

                            <div class="menu" role="menu">
                                <sec:ifAnyGranted roles="ROLE_STATISTICS_EDITOR">
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="usage"
                                            action="index">${message(code: 'menu.datamanager.manage_usage_stats')}</semui:link>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="dataManager" action="index">${message(code:'menu.datamanager.dash')}</semui:link>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="dataManager"
                                            action="deletedTitles">${message(code: 'datamanager.deletedTitleManagement.label')}</semui:link>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ORG_MANAGER,ROLE_ADMIN">
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="dataManager"
                                            action="deletedOrgs">${message(code: 'datamanager.deletedOrgManagement.label')}</semui:link>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <div class="divider"></div>

                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="package" action="list">${message(code:'menu.datamanager.searchPackages')}</semui:link>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="platform" action="list">${message(code:'menu.datamanager.searchPlatforms')}</semui:link>

                                    <div class="divider"></div>

                                    <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="upload" action="reviewPackage">${message(code:'menu.datamanager.uploadPackage')}</semui:link>--%>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="licenseImport" action="doImport">${message(code:'onix.import.license')}</semui:link>

                                    <div class="divider"></div>

                                    <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="license" action="create">${message(code:'license.template.new')}</semui:link>--%>
                                    <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="platform" action="create">${message(code:'menu.datamanager.newPlatform')}</semui:link>--%>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="subscription" action="compare">${message(code:'menu.datamanager.compareSubscriptions')}</semui:link>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="onixplLicenseCompare" action="index">${message(code:'menu.institutions.comp_onix')}</semui:link>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="dataManager" action="changeLog">${message(code:'menu.datamanager.changelog')}</semui:link>
                                    <div class="divider"></div>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_GLOBAL_DATA">
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="dataManager" action="checkPackageTIPPs">Tipps Check of we:kb and LAS:eR</semui:link>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <div class="divider"></div>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="title" action="dmIndex">${message(code:'menu.datamanager.titles')}</semui:link>

                                    <div class="divider"></div>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="dataManager" action="listMailTemplates">Mail Templates</semui:link>
                                </sec:ifAnyGranted>
                            </div>
                        </div>
                    </sec:ifAnyGranted>

                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                                ${message(code:'menu.admin')}
                                <i  class="dropdown icon"></i>
                            </a>

                            <div class="menu" role="menu">
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="profile" action="errorOverview">
                                    ${message(code: "menu.user.errorReport")}
                                    <g:if test="${newTickets.size() > 0}">
                                        <div class="ui floating red circular label">${newTickets.size()}</div>
                                    </g:if>
                                </semui:link>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'menu.admin.sysAdmin')}
                                        <i  class="dropdown icon"></i>
                                    </div>

                                    <div class="menu" role="menu">
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="appInfo">${message(code:'menu.admin.appInfo')}</semui:link>
                                        <semui:link generateElementId="true" class="item" controller="admin" action="systemEvents">${message(code:'menu.admin.systemEvents')}</semui:link>
                                        <semui:link generateElementId="true" class="item" controller="admin" action="testMailSending">Test Mail Sending</semui:link>

                                        <div class="divider"></div>

                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="triggerHousekeeping" onclick="return confirm('${message(code:'confirm.start.HouseKeeping')}')">${message(code:'menu.admin.triggerHousekeeping')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="initiateCoreMigration" onclick="return confirm('${message(code:'confirm.start.CoreMigration')}')">${message(code:'menu.admin.coreMigration')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="dataCleanse" onclick="return confirm('${message(code:'confirm.start.DataCleaningNominalPlatforms')}')">Run Data Cleaning (Nominal Platforms)</semui:link>
                                    </div>
                                </div>
                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        <i class="ui icon code branch"></i>
                                        <span class="text">Developer</span>
                                        <i  class="dropdown icon"></i>
                                    </div>

                                    <div class="menu" role="menu">
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="dev" action="frontend">Frontend</semui:link>
                                    </div>
                                </div>

                                <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="systemMessages"><i class="icon quote left"></i>${message(code: 'menu.admin.systemMessage')}</semui:link>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="systemAnnouncements"><i class="icon envelope"></i>${message(code:'menu.admin.announcements')}</semui:link>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="serverDifferences">${message(code:'menu.admin.serverDifferences')}</semui:link>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'org.plural.label')}
                                        <i  class="dropdown icon"></i>
                                    </div>

                                    <div class="menu" role="menu">
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="organisation" action="index">${message(code:'menu.admin.allOrganisations')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="manageOrganisations">${message(code:'menu.admin.manageOrganisations')}</semui:link>
                                    </div>
                                </div>

                                <semui:link generateElementId="true" class="item" role="menuitem" controller="user" action="list">${message(code:'menu.institutions.users')}</semui:link>

                                <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="showAffiliations">${message(code:'menu.admin.showAffiliations')}</semui:link>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="usage">${message(code:'menu.admin.manageUsageStats')}</semui:link>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="updateQASubscriptionDates">${message(code:'menu.admin.updateTestSubscriptionDates')}</semui:link>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="forceSendNotifications">${message(code:'menu.admin.sendNotifications')}</semui:link>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'menu.admin.bulkOps')}
                                        <i  class="dropdown icon"></i>
                                    </div>

                                   <div class="menu" role="menu">
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="orgsExport">${message(code:'menu.admin.bulkOps.orgsExport')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="orgsImport">${message(code:'menu.admin.bulkOps.orgsImport')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="makeshiftLaserOrgExport">${message(code:'menu.admin.exportBasicData')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="titleEnrichment">Title Enrichment</semui:link>
                                    </div>
                                </div>
                                <div class="divider"></div>

                                <sec:ifAnyGranted roles="ROLE_ADMIN"><!-- TODO: reporting-permissions -->
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="manageWorkflows">${message(code:'menu.admin.manageWorkflows')}</semui:link>
                                </sec:ifAnyGranted>

                                <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="manageNamespaces">${message(code:'menu.admin.manageIdentifierNamespaces')}</semui:link>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="managePropertyDefinitions">${message(code:'menu.admin.managePropertyDefinitions')}</semui:link>
                                <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="managePropertyGroups">${message(code:'menu.institutions.manage_prop_groups')}</semui:link>--%> <%-- property groups are always private?? --%>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="manageRefdatas">${message(code:'menu.admin.manageRefdatas')}</semui:link>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="manageContentItems">${message(code:'menu.admin.manageContentItems')}</semui:link>

                                <div class="divider"></div>

                                <semui:link generateElementId="true" class="item" role="menuitem" controller="stats" action="statsHome">${message(code:'menu.admin.statistics')}</semui:link>
                            </div>
                        </div>
                    </sec:ifAnyGranted>

                    <sec:ifAnyGranted roles="ROLE_YODA">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            <a class="title">
                                ${message(code:'menu.yoda')}
                                <i  class="dropdown icon"></i>
                            </a>

                            <div class="menu" role="menu">

                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="index">Dashboard</semui:link>

                                <div class="item " role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        <i class="ui icon keyboard outline"></i>
                                        ${message(code:'menu.yoda.engine')}
                                        <i  class="dropdown icon"></i>
                                    </div>

                                    <div class="menu" role="menu">

                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="settings"><i class="icon toggle on"></i>${message(code:'menu.yoda.systemSettings')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="systemEvents">${message(code:'menu.admin.systemEvents')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="appConfig">${message(code:'menu.yoda.appConfig')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="appThreads">${message(code:'menu.yoda.appThreads')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="quartzInfo">${message(code:'menu.yoda.quartzInfo')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="cacheInfo">${message(code:'menu.yoda.cacheInfo')}</semui:link>

                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="systemProfiler"><i class="stopwatch icon"></i>${message(code:'menu.yoda.systemProfiler')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="activityProfiler"><i class="stopwatch icon"></i>${message(code:'menu.yoda.activityProfiler')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="timelineProfiler"><i class="stopwatch icon"></i>${message(code:'menu.yoda.timelineProfiler')}</semui:link>

                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="appSecurity">${message(code:'menu.yoda.appSecurity')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="userMatrix">${message(code:'menu.yoda.userMatrix')}</semui:link>

                                        <%--<a class="item" role="menuitem" href="${g.createLink(uri:'/monitoring')}">App Monitoring</a>--%>
                                    </div>
                                </div>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        Fällige Termine
                                        <i  class="dropdown icon"></i>
                                    </div>
                                    <div class="menu" role="menu">
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="dueDates_updateDashboardDB">${message(code:'menu.admin.updateDashboardTable')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="dueDates_sendAllEmails">${message(code:'menu.admin.sendEmailsForDueDates')}</semui:link>
                                    </div>
                                </div>
                                <div class="divider"></div>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="statsSync">${message(code:'menu.admin.stats.sync')}</semui:link>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="manageStatsSources">SUSHI-Quellen bearbeiten</semui:link>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="fetchStats" params="[(FormService.FORM_SERVICE_TOKEN): formService.getNewToken(), incremental: true]">${message(code:'menu.admin.stats.fetch.incremental')}</semui:link>
                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="fetchStats" params="[(FormService.FORM_SERVICE_TOKEN): formService.getNewToken(), incremental: false]">${message(code:'menu.admin.stats.fetch')}</semui:link>
                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'menu.admin.syncManagement')}
                                        <i  class="dropdown icon"></i>
                                    </div>
                                    <div class="menu" role="menu">
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="globalSync" onclick="return confirm('${message(code:'confirm.start.globalDataSync')}')">${message(code:'menu.yoda.globalDataSync')}</semui:link>
                                        <div class="item" role="menuitem" aria-haspopup="true">
                                            <div class="title">
                                                ${message(code:'menu.admin.syncManagement.reload')}
                                                <i class="dropdown icon"></i>
                                            </div>
                                            <div class="menu" role="menu">
                                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'identifier']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateIdentifiers')}</semui:link>
                                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'editionStatement']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateEditionStatement')}</semui:link>
                                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'iemedium']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateIEMedium')}</semui:link>
                                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'ddc']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateDDC')}</semui:link>
                                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'language']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateLanguage')}</semui:link>
                                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'accessType']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateAccessType')}</semui:link>
                                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'openAccess']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateOpenAccess')}</semui:link>
                                                <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'titleNamespace']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateTitleNamespace')}</semui:link>
                                            </div>
                                        </div>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="manageGlobalSources">${message(code:'menu.yoda.manageGlobalSources')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="getTIPPsWithoutGOKBId">${message(code:'menu.yoda.purgeTIPPsWithoutGOKBID')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="expungeRemovedTIPPs">${message(code:'menu.yoda.expungeRemovedTIPPs')}</semui:link>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="matchPackageHoldings">${message(code:'menu.admin.bulkOps.matchPackageHoldings')}</semui:link>--%>
                                    </div>
                                </div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ElasticSearch
                                        <i  class="dropdown icon"></i>
                                    </div>
                                    <div class="menu" role="menu">
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="esIndexUpdate" onclick="return confirm('${message(code:'confirm.start.ESUpdateIndex')}')">${message(code:'menu.yoda.updateESIndex')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="manageESSources">Manage ES Source</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="manageFTControl">Manage FTControl</semui:link>
                                        <div class="divider"></div>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="fullReset" onclick="return confirm('${message(code:'confirm.start.resetESIndex')}')">${message(code:'menu.yoda.resetESIndex')}</semui:link>
                                        <div class="divider"></div>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="killDataloadService">Kill ES Update Index</semui:link>
                                        <div class="divider"></div>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="createESIndices">Create ES Indices</semui:link>
                                    </div>
                                </div>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'menu.admin.dataManagement')}
                                        <i  class="dropdown icon"></i>
                                    </div>

                                    <div class="menu" role="menu">
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="dataManager" action="listPlatformDuplicates">List Platform Duplicates</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="package" action="getDuplicatePackages">List Package Duplicates</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="dataManager" action="listDeletedTIPPS">List TIPP Duplicates and deleted TIPPs</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="listDuplicateTitles">Check Title we:kb IDs</semui:link>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="tippTransfer">${message(code:'menu.admin.tippTransfer')}</semui:link>--%>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="ieTransfer">${message(code:'menu.admin.ieTransfer')}</semui:link>--%>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="userMerge">${message(code:'menu.admin.userMerge')}</semui:link>--%>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="hardDeletePkgs">${message(code:'menu.admin.hardDeletePkgs')}</semui:link>--%>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="manageDeletedObjects">${message(code: "menu.admin.deletedObjects")}</semui:link>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="databaseCollations">${message(code: "menu.admin.databaseCollations")}</semui:link>--%>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="databaseStatistics">${message(code: "menu.admin.databaseStatistics")}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="dataConsistency">${message(code: "menu.admin.dataConsistency")}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="fileConsistency">${message(code: "menu.admin.fileConsistency")}</semui:link>
                                    </div>
                                </div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <div class="title">
                                        ${message(code:'menu.admin.dataMigration')}
                                        <i  class="dropdown icon"></i>
                                    </div>
                                    <div class="menu" role="menu">
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="checkOrgLicRoles"><g:message code="menu.admin.checkOrgLicRoles"/></semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="dbmFixPrivateProperties">Fix Private Properties</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="replaceUserSettingDashboardReminderPeriod">Replace UserSetting Dashboard ReminderPeriod in Database</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="cleanUpSurveys">Clean Up Surveys with Multi Term</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="insertEditUris">Insert Edit URIs for we:kb Sources</semui:link>

                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="surveyCheck">Update Survey Status</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="subscriptionCheck">${message(code:'menu.admin.subscriptionsCheck')}</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="freezeSubscriptionHoldings">${message(code:'menu.admin.freezeSubscriptionHoldings')}</semui:link>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateTaxRates">${message(code:'menu.admin.taxTypeCheck')}</semui:link>--%>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateCustomerType">Kundentyp für alle Einrichtungen setzen</semui:link>--%>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="showOldDocumentOwners">${message(code:'menu.admin.documentOwnerCheck')}</semui:link>--%>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="generateBatchUID">${message(code:'menu.admin.batchUID')}</semui:link>--%>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="makeshiftLaserOrgExport">${message(code:'menu.admin.exportBasicData')}</semui:link>--%>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="dropDeletedObjects">Drop deleted Objects from Database</semui:link>
                                        <semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="migratePackageIdentifiers">Remap Package Identifier Namespace</semui:link>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="assignNoteOwners">Assign note owners for notes of subscriptions and licenses without owners</semui:link>--%>
                                        <%--<semui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="correctCostsInLocalCurrency" params="[dryRun: true]">${message(code:'menu.admin.correctCostsInLocalCurrencyDryRun')}</semui:link>
                                        <semui:link generateElementId="true" class="item role="menuitem" js-open-confirm-modal"
                                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.correctCostsInLocalCurrency')}"
                                                data-confirm-term-how="ok"
                                                controller="yoda" action="correctCostsInLocalCurrency" params="[dryRun: false]">${message(code:'menu.admin.correctCostsInLocalCurrencyDoIt')}</semui:link>--%>

                                    </div>
                                </div>
                            </div>

                        </div>
                    </sec:ifAnyGranted>

                    <div class="right menu la-right-menuPart">
                        <div role="search" id="mainSearch" class="ui category search spotlight">
                            <div class="ui icon input">
                                <input  aria-label="${message(code:'spotlight.search.placeholder')}" type="search" id="spotlightSearch" class="prompt" placeholder="${message(code:'spotlight.search.placeholder')}">
                                <i id="btn-search"  class="search icon"></i>
                            </div>
                            <div class="results" style="overflow-y:scroll;max-height: 400px;"></div>
                        </div>

                        <semui:link generateElementId="true" controller="search" action="index"
                                class="la-search-advanced la-popup-tooltip la-delay"
                                 data-content="${message(code: 'search.advancedSearch.tooltip')}">
                            <i class="large icons">
                                <i class="search icon"></i>
                                <i class="top right grey corner cog icon "></i>
                            </i>
                        </semui:link>

                        <g:if test="${contextUser}">
                            <div class="ui dropdown item la-noBorder" role="menuitem" aria-haspopup="true">
                                <a class="title">
                                    ${contextUser.displayName}
                                    <i  class="dropdown icon"></i>
                                </a>

                                <div class="menu" role="menu">

                                    <g:set var="usaf" value="${contextUser.authorizedOrgs}" />
                                    <g:if test="${usaf && usaf.size() > 0}">
                                        <g:each in="${usaf}" var="org">
                                            <g:if test="${org.id == contextOrg?.id}">
                                                <semui:link generateElementId="true" class="item active" role="menuitem" controller="myInstitution" action="switchContext" params="${[oid:"${org.class.name}:${org.id}"]}">${org.name}</semui:link>
                                            </g:if>
                                            <g:else>
                                                <semui:link generateElementId="true" class="item" role="menuitem" controller="myInstitution" action="switchContext" params="${[oid:"${org.class.name}:${org.id}"]}">${org.name}</semui:link>
                                            </g:else>
                                        </g:each>
                                    </g:if>

                                    <div class="divider"></div>

                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="profile" action="index">${message(code:'profile.user')}</semui:link>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="profile" action="help">${message(code:'menu.user.help')}</semui:link>
                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="profile" action="dsgvo">${message(code:'privacyNotice')}</semui:link>

                                    <div class="divider"></div>

                                    <semui:link generateElementId="true" class="item" role="menuitem" controller="logout">${message(code:'menu.user.logout')}</semui:link>
                                    <div class="divider"></div>

                                    <g:if test="${grailsApplication.metadata['info.app.version']}">
                                        <div class="header">Version: ${grailsApplication.metadata['info.app.version']} – ${grailsApplication.metadata['info.app.build.date']}</div>
                                    </g:if>
                                    <div class="header">
                                        ${systemService.getNumberOfActiveUsers()} Benutzer online
                                    </div>
                                </div>
                            </div>
                        </g:if>
                    </div>

                </sec:ifAnyGranted><%-- ROLE_USER --%>

                <sec:ifNotGranted roles="ROLE_USER">
                    <sec:ifLoggedIn>
                        <semui:link generateElementId="true" class="item" controller="logout">${message(code:'menu.user.logout')}</semui:link>
                    </sec:ifLoggedIn>
                </sec:ifNotGranted>

            </div><!-- container -->

        </div><!-- main menu -->
%{--    </nav>--}%

    <sec:ifAnyGranted roles="ROLE_USER">
        <g:set var="visibilityContextOrgMenu" value="la-show-context-orgMenu" />
        <nav class="ui fixed menu la-contextBar" aria-label="${message(code:'wcag.label.modeNavigation')}" >
            <div class="ui container">
                <button class="ui button big la-menue-button"><i class="bars icon"></i></button>
                <div class="ui sub header item la-context-org">${contextOrg?.name}</div>

                <div class="right menu la-advanced-view">
                    <div class="item">
                        <g:if test="${cachedContent}">
                            <span class="ui icon button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.cachedContent.tooltip')}" data-position="bottom right" data-variation="tiny">
                                <i class="hourglass end icon"></i>
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

                <div id="systemMessages" class="ui message warning hidden"></div>

                %{-- content --}%

                <g:layoutBody/>

            </main><!-- .main -->
        </div>

        %{-- footer --}%

        <sec:ifNotGranted roles="ROLE_USER">
            <!-- Footer -->
            <g:render template="/public/templates/footer" />
            <!-- Footer End -->
        </sec:ifNotGranted>

        %{-- global container for modals and ajax --}%

        <div id="dynamicModalContainer"></div>

        %{-- global loading indicator --}%

        <div id="loadingIndicator" style="display: none">
            <div class="ui inline medium text loader active">Aktualisiere Daten ..</div>
        </div>

        %{-- global confirmation modal --}%

        <semui:confirmationModal  />

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
            <semui:systemInfo />

            <div id="system-profiler" class="ui label hidden">
                <i class="clock icon"></i>
                <span></span>
            </div>
        </sec:ifAnyGranted>

        %{-- ajax login --}%

        <g:if test="${controllerName != 'login'}">
            <g:render template="/templates/ajax/login" />
        </g:if>

        %{-- javascript loading --}%

        <laser:javascript src="${currentTheme}.js"/>%{-- dont move --}%

        <laser:scriptBlock/>%{-- dont move --}%

        %{-- profiler, jsqtk --}%

        <script data-type="fix">
            $(document).ready(function() {
                sys.profiler("${ ProfilerUtils.generateKey( webRequest )}");

                <g:if test="${ServerUtils.getCurrentServer() != ServerUtils.SERVER_PROD}">
                    jsqtk.go();
                    console.log('[ JSPC ]:', JSPC);
                </g:if>
            })
        </script>
    </body>
</html>
