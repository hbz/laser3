<%@ page import="de.laser.system.SystemSetting; de.laser.UserSetting; de.laser.RefdataValue; de.laser.helper.ProfilerUtils; de.laser.helper.ServerUtils; de.laser.helper.RDStore;de.laser.helper.RDConstants;org.grails.web.util.GrailsApplicationAttributes;de.laser.Org;de.laser.auth.User;de.laser.system.SystemMessage" %>
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
<g:set var="myInstNewAffils" scope="page" />

<tmpl:/layouts/initVars />

<!--[if lt IE 7]> <html class="no-js lt-ie9 lt-ie8 lt-ie7" lang="${currentLang}"> <![endif]-->
<!--[if IE 7]><html class="no-js lt-ie9 lt-ie8" lang="${currentLang}"> <![endif]-->
<!--[if IE 8]><html class="no-js lt-ie9" lang="${currentLang}"> <![endif]-->
<!--[if gt IE 8]><!--><html class="no-js" lang="${currentLang}"> <!--<![endif]-->

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title><g:layoutTitle default="${meta(name: 'app.name')}"/></title>
    <meta name="description" content="">
    <meta name="viewport" content="initial-scale = 1.0">

    <asset:stylesheet src="${currentTheme}.css"/>%{-- dont move --}%

    <script>
        <g:render template="/templates/javascript/laser.js" />%{-- dont move --}%
        <g:render template="/templates/javascript/dict.js" />%{-- dont move --}%
    </script>
    <asset:javascript src="base.js"/>%{-- dont move --}%

    <g:layoutHead/>

    <tmpl:/layouts/favicon />
</head>

<body class="${controllerName}_${actionName}" id="globalJumpMark">

    <g:if test="${currentServer == ServerUtils.SERVER_DEV}">
        <div class="ui green label big la-server-label" aria-label="Sie befinden sich im Developer-System">
            <span>DEV</span>
        </div>
    </g:if>
    <g:if test="${currentServer == ServerUtils.SERVER_QA}">
        <div class="ui red label big la-server-label">
            <span>QA</span>
        </div>
    </g:if>
    <g:set var="visibilityContextOrgMenu" value="la-hide-context-orgMenu" />
    <nav aria-label="${message(code:'wcag.label.mainMenu')}">
        <div id="mainMenue" class="ui fixed inverted stackable menu" role="menubar" >
            <div class="ui container" role="none">
                <g:link elementId="home-index" role="menuitem" controller="home" action="index" aria-label="${message(code:'default.home.label')}" class="header item la-logo-item">
                    <img alt="Logo Laser" class="logo" src="${resource(dir: 'images', file: 'laser.svg')}"/>
                </g:link>

                <sec:ifAnyGranted roles="ROLE_USER">

                    <g:if test="${contextOrg}">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            ${message(code:'menu.public')}
                            <i aria-hidden="true" class="dropdown icon"></i>

                            <div class="menu" role="menu">
                                    <g:link elementId="package-index" class="item" role="menuitem" controller="package" action="index">${message(code:'menu.public.all_pkg')}</g:link>
                                    <g:link elementId="title-index"  class="item" role="menuitem" controller="title" action="index">${message(code:'menu.public.all_titles')}</g:link>

                                    <g:if test="${grailsApplication.config.feature.eBooks}">
                                        <a id="gokbLabs" class="item" role="menuitem" href="http://gokb.k-int.com/gokbLabs">${message(code:'menu.institutions.ebooks')}</a>
                                        <div class="divider"></div>
                                    </g:if>

                                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_EDITOR">
                                        <semui:mainNavItem elementId="organisation-index" role="menuitem" controller="organisation" action="index" message="menu.public.all_orgs" />
                                    </sec:ifAnyGranted>

                                    <semui:securedMainNavItem elementId="organisation-listInstitution" role="menuitem" orgPerm="ORG_CONSORTIUM" affiliation="INST_USER" specRole="ROLE_ADMIN,ROLE_ORG_EDITOR"
                                                                  controller="organisation" action="listInstitution" message="menu.public.all_insts" />

                                    <g:link elementId="organisation-listProvider" class="item" role="menuitem" controller="organisation" action="listProvider">${message(code:'menu.public.all_providers')}</g:link>
                                    <g:link elementId="platform-list" class="item" role="menuitem" controller="platform" action="list">${message(code:'menu.public.all_platforms')}</g:link>

                                    <div class="divider"></div>

                                    <g:link elementId="gasco" class="item" role="menuitem" controller="gasco">${message(code:'menu.public.gasco_monitor')}</g:link>

                                    <a id="gokb" href="${message(code:'url.gokb.' + currentServer)}" class="item" role="menuitem">GOKB</a>

                                    <a id="ygor" href="${message(code:'url.ygor.' + currentServer)}" class="item" role="menuitem">YGOR</a>
                            </div>
                        </div>

                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            ${message(code:'menu.my')}
                            <i aria-hidden="true" class="dropdown icon"></i>

                            <div class="menu" role="menu">

                                <semui:securedMainNavItem elementId="myInstitution-currentSubscriptions" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentSubscriptions" message="menu.my.subscriptions" />
                                <semui:securedMainNavItem elementId="myInstitution-currentLicenses" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentLicenses" message="menu.my.licenses" />
                                <semui:securedMainNavItem elementId="myInstitution-currentProviders" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentProviders" message="menu.my.providers" />
                                <semui:securedMainNavItem elementId="myInstitution-currentPlatforms" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentPlatforms" message="menu.my.platforms" />
                                <semui:securedMainNavItem elementId="myInstitution-currentPackages" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentPackages" message="menu.my.packages" />
                                <semui:securedMainNavItem elementId="myInstitution-currentTitles" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentTitles" message="menu.my.titles" />
                                <semui:securedMainNavItem elementId="myInstitution-documents" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="documents" message="menu.my.documents" />

                        <g:if test="${accessService.checkPerm('ORG_BASIC_MEMBER')}">
                            <div class="divider"></div>
                            <semui:securedMainNavItem elementId="myInstitution-currentSurveys" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentSurveys" message="menu.my.surveys" />
                        </g:if>

                                <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                                    <div class="divider"></div>
                                    <semui:securedMainNavItem elementId="survey-currentSurveysConsortia" role="menuitem" affiliation="INST_USER" controller="survey" action="currentSurveysConsortia" message="menu.my.surveys" />

                                    <div class="divider"></div>

                                    <semui:securedMainNavItem elementId="myInstitution-manageMembers" role="menuitem" affiliation="INST_USER" controller="myInstitution" specRole="ROLE_ADMIN,ROLE_ORG_EDITOR" action="manageMembers" message="menu.my.consortia" />
                                    <semui:securedMainNavItem elementId="myInstitution-manageConsortiaSubscriptions" role="menuitem" affiliation="INST_USER" controller="myInstitution" specRole="ROLE_ADMIN" action="manageConsortiaSubscriptions" message="menu.my.consortiaSubscriptions" />
                                </g:if>


                                <div class="divider"></div>

                                <semui:securedMainNavItem elementId="compare-compareSubscriptions" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="compare" action="compareSubscriptions" message="menu.my.comp_sub" />

                                <div class="divider"></div>

                                <semui:securedMainNavItem elementId="compare-compareLicenses" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="compare" action="compareLicenses" message="menu.my.comp_lic" />

                            </div>
                        </div>


                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            ${message(code:'menu.institutions.myInst')}
                            <i aria-hidden="true" class="dropdown icon"></i>

                            <div class="menu" role="menu">
                                <semui:securedMainNavItem elementId="myInstitution-dashboard" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="dashboard" message="menu.institutions.dash" />

                                <g:link elementId="organisation-show" class="item" role="menuitem" controller="organisation" action="show" params="[id: contextOrg?.id]">${message(code:'menu.institutions.org_info')}</g:link>

                                <semui:securedMainNavItem elementId="myInstitution-addressbook" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="addressbook" message="menu.institutions.myAddressbook" />
                                <semui:securedMainNavItem elementId="myInstitution-tasks" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="tasks" message="task.plural" />
                                <semui:securedMainNavItem elementId="myInstitution-reporting" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="reporting" message="menu.institutions.reporting" />
                                <semui:securedMainNavItem elementId="myInstitution-changes" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="changes" message="menu.institutions.changes" />
                                <semui:securedMainNavItem elementId="myInstitution-managePrivatePropertyDefinitions" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="managePrivatePropertyDefinitions" message="menu.institutions.manage_props" />
                                <semui:securedMainNavItem elementId="myInstitution-finance" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="finance" message="menu.institutions.finance" />
                                <semui:securedMainNavItem elementId="myInstitution-budgetCodes" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" specRole="ROLE_ADMIN" controller="myInstitution" action="budgetCodes" message="menu.institutions.budgetCodes" />
                                <semui:securedMainNavItem elementId="costConfiguration-index" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" specRole="ROLE_ADMIN" controller="costConfiguration" action="index" message="menu.institutions.costConfiguration" />
                                <%--<semui:securedMainNavItem elementId="" "Disabled role="menuitem" message="menu.institutions.financeImport" />--%>
                                <semui:securedMainNavItem elementId="myInstitution-financeImport" role="menuitem" orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_EDITOR" specRole="ROLE_ADMIN" controller="myInstitution" action="financeImport" message="menu.institutions.financeImport" />

                                <div class="divider"></div>

                                <semui:securedMainNavItem elementId="myInstitution-userList" role="menuitem" affiliation="INST_ADM" controller="myInstitution" action="userList" message="menu.institutions.users" newAffiliationRequests="${myInstNewAffils.size()}" />

                                <sec:ifAnyGranted roles="ROLE_YODA">
                                       <g:link elementId="myInstitution-changeLog" class="item" role="menuitem" controller="myInstitution" action="changeLog">${message(code:'menu.institutions.change_log')}</g:link>
                                    <%--<semui:securedMainNavItem elementId="myInstitution-changeLog" affiliation="INST_EDITOR" controller="myInstitution" action="changeLog" message="menu.institutions.change_log" />--%>
                                </sec:ifAnyGranted>

                            </div>
                        </div>
                    </g:if>

                    <sec:ifAnyGranted roles="ROLE_ORG_MANAGER,ROLE_ADMIN,ROLE_GLOBAL_DATA,ROLE_STATISTICS_EDITOR">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            ${message(code:'menu.datamanager')}
                            <i aria-hidden="true" class="dropdown icon"></i>

                            <div class="menu" role="menu">
                                <sec:ifAnyGranted roles="ROLE_STATISTICS_EDITOR">
                                    <g:link elementId="usage-index" class="item" role="menuitem" controller="usage"
                                            action="index">${message(code: 'menu.datamanager.manage_usage_stats')}</g:link>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <g:link elementId="dataManager-index" class="item" role="menuitem" controller="dataManager" action="index">${message(code:'menu.datamanager.dash')}</g:link>
                                    <g:link elementId="dataManager-deletedTitles" class="item" role="menuitem" controller="dataManager"
                                            action="deletedTitles">${message(code: 'datamanager.deletedTitleManagement.label')}</g:link>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ORG_MANAGER,ROLE_ADMIN">
                                    <g:link elementId="dataManager-deletedOrgs" class="item" role="menuitem" controller="dataManager"
                                            action="deletedOrgs">${message(code: 'datamanager.deletedOrgManagement.label')}</g:link>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <div class="divider"></div>

                                    <g:link elementId="package-list" class="item" role="menuitem" controller="package" action="list">${message(code:'menu.datamanager.searchPackages')}</g:link>
                                    <g:link elementId="platform-list" class="item" role="menuitem" controller="platform" action="list">${message(code:'menu.datamanager.searchPlatforms')}</g:link>

                                    <div class="divider"></div>

                                    <%--<g:link elementId="upload-reviewPackage" class="item" role="menuitem" controller="upload" action="reviewPackage">${message(code:'menu.datamanager.uploadPackage')}</g:link>--%>
                                    <g:link elementId="licenseImport-doImport" class="item" role="menuitem" controller="licenseImport" action="doImport">${message(code:'onix.import.license')}</g:link>

                                    <div class="divider"></div>

                                    <g:link elementId="title-findTitleMatches" class="item" role="menuitem" controller="title" action="findTitleMatches">${message(code:'menu.datamanager.newTitle')}</g:link>
                                    <%--<g:link elementId="license-create" class="item" role="menuitem" controller="license" action="create">${message(code:'license.template.new')}</g:link>--%>
                                    <%--<g:link elementId="platform-create" class="item" role="menuitem" controller="platform" action="create">${message(code:'menu.datamanager.newPlatform')}</g:link>--%>
                                    <g:link elementId="subscription-compare" class="item" role="menuitem" controller="subscription" action="compare">${message(code:'menu.datamanager.compareSubscriptions')}</g:link>
                                    <g:link elementId="onixplLicenseCompare-index" class="item" role="menuitem" controller="onixplLicenseCompare" action="index">${message(code:'menu.institutions.comp_onix')}</g:link>
                                    <g:link elementId="dataManager-changeLog" class="item" role="menuitem" controller="dataManager" action="changeLog">${message(code:'menu.datamanager.changelog')}</g:link>
                                    <div class="divider"></div>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_GLOBAL_DATA">
                                    <g:link elementId="globalDataSync-index" class="item" role="menuitem" controller="globalDataSync" action="index" >${message(code:'menu.datamanager.global_data_sync')}</g:link>
                                    <g:link elementId="dataManager-checkPackageTIPPs" class="item" role="menuitem" controller="dataManager" action="checkPackageTIPPs">Tipps Check of GOKB and LAS:eR</g:link>
                                </sec:ifAnyGranted>

                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <div class="divider"></div>
                                    <%--<g:link elementId="jasperReports-index" class="item" role="menuitem" controller="jasperReports" action="index">${message(code:'menu.datamanager.jasper_reports')}</g:link>--%>
                                    <g:link elementId="title-dmIndex" class="item" role="menuitem" controller="title" action="dmIndex">${message(code:'menu.datamanager.titles')}</g:link>

                                    <div class="divider"></div>
                                    <g:link elementId="dataManager-listMailTemplates" class="item" role="menuitem" controller="dataManager" action="listMailTemplates">Mail Templates</g:link>
                                </sec:ifAnyGranted>
                            </div>
                        </div>
                    </sec:ifAnyGranted>

                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            ${message(code:'menu.admin')}
                            <i aria-hidden="true" class="dropdown icon"></i>

                            <div class="menu" role="menu">
                                <g:link elementId="profile-errorOverview" class="item" role="menuitem" controller="profile" action="errorOverview">
                                    ${message(code: "menu.user.errorReport")}
                                    <g:if test="${newTickets.size() > 0}">
                                        <div class="ui floating red circular label">${newTickets.size()}</div>
                                    </g:if>
                                </g:link>

                                <g:link elementId="admin-manageAffiliationRequests" class="item" role="menuitem" controller="admin" action="manageAffiliationRequests">
                                    ${message(code: "menu.institutions.affiliation_requests")}
                                    <%--<g:set var="newAffiliationRequests" value="${de.laser.auth.UserOrg.findAllByStatus(0).size()}" />--%>
                                    <%
                                        Object organisationService = grailsApplication.mainContext.getBean("organisationService")
                                        Map affilRequestMap = organisationService.getPendingRequests(contextUser, contextOrg)
                                        int newAffiliationRequests = affilRequestMap?.pendingRequests?.size()
                                    %>
                                    <g:if test="${newAffiliationRequests > 0}">
                                        <div class="ui floating red circular label">${newAffiliationRequests}</div>
                                    </g:if>
                                </g:link>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    ${message(code:'menu.admin.sysAdmin')}
                                    <i aria-hidden="true" class="dropdown icon"></i>

                                    <div class="menu" role="menu">
                                        <g:link elementId="yoda-appInfo" class="item" role="menuitem" controller="yoda" action="appInfo">${message(code:'menu.admin.appInfo')}</g:link>
                                        <g:link elementId="admin-systemEvents" class="item" controller="admin" action="systemEvents">${message(code:'menu.admin.systemEvents')}</g:link>

                                        <div class="divider"></div>

                                        <g:link elementId="admin-triggerHousekeeping" class="item" role="menuitem" controller="admin" action="triggerHousekeeping" onclick="return confirm('${message(code:'confirm.start.HouseKeeping')}')">${message(code:'menu.admin.triggerHousekeeping')}</g:link>
                                        <g:link elementId="admin-initiateCoreMigration" class="item" role="menuitem" controller="admin" action="initiateCoreMigration" onclick="return confirm('${message(code:'confirm.start.CoreMigration')}')">${message(code:'menu.admin.coreMigration')}</g:link>
                                        <g:link elementId="admin-dataCleanse" class="item" role="menuitem" controller="admin" action="dataCleanse" onclick="return confirm('${message(code:'confirm.start.DataCleaningNominalPlatforms')}')">Run Data Cleaning (Nominal Platforms)</g:link>
                                    </div>
                                </div>
                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <i class="ui icon code branch"></i>
                                    <span class="text">Developer</span>
                                    <i aria-hidden="true" class="dropdown icon"></i>

                                    <div class="menu" role="menu">
                                        <g:link elementId="dev-frontend" class="item" role="menuitem" controller="dev" action="frontend">Frontend</g:link>
                                    </div>
                                </div>

                                <g:link elementId="admin-systemMessages" class="item" role="menuitem" controller="admin" action="systemMessages">${message(code: 'menu.admin.systemMessage')}</g:link>
                                <g:link elementId="admin-systemAnnouncements" class="item" role="menuitem" controller="admin" action="systemAnnouncements">${message(code:'menu.admin.announcements')}</g:link>
                                <g:link elementId="admin-serverDifferences" class="item" role="menuitem" controller="admin" action="serverDifferences">${message(code:'menu.admin.serverDifferences')}</g:link>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    ${message(code:'org.plural.label')}
                                    <i aria-hidden="true" class="dropdown icon"></i>

                                    <div class="menu" role="menu">
                                        <g:link elementId="organisation-index" class="item" role="menuitem" controller="organisation" action="index">${message(code:'menu.admin.allOrganisations')}</g:link>
                                        <g:link elementId="admin-manageOrganisations" class="item" role="menuitem" controller="admin" action="manageOrganisations">${message(code:'menu.admin.manageOrganisations')}</g:link>
                                    </div>
                                </div>

                                <g:link elementId="user-list" class="item" role="menuitem" controller="user" action="list">${message(code:'menu.institutions.users')}</g:link>
                                <g:link elementId="admin-showAffiliations" class="item" role="menuitem" controller="admin" action="showAffiliations">${message(code:'menu.admin.showAffiliations')}</g:link>
                                <g:link elementId="usage-index" class="item" role="menuitem" controller="usage">${message(code:'menu.admin.manageUsageStats')}</g:link>
                                <g:link elementId="admin-updateQASubscriptionDates" class="item" role="menuitem" controller="admin" action="updateQASubscriptionDates">${message(code:'menu.admin.updateTestSubscriptionDates')}</g:link>
                                <% /* g:link  elementId="admin-juspSync" class="item" role="menuitem" controller="admin" action="juspSync">Run JUSP Sync</g:link */ %>
                                <g:link elementId="admin-forceSendNotifications" class="item" role="menuitem" controller="admin" action="forceSendNotifications">${message(code:'menu.admin.sendNotifications')}</g:link>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    ${message(code:'menu.admin.bulkOps')}
                                   <i aria-hidden="true" class="dropdown icon"></i>

                                   <div class="menu" role="menu">
                                        <g:link elementId="admin-orgsExport" class="item" role="menuitem" controller="admin" action="orgsExport">${message(code:'menu.admin.bulkOps.orgsExport')}</g:link>
                                        <g:link elementId="admin-orgsImport" class="item" role="menuitem" controller="admin" action="orgsImport">${message(code:'menu.admin.bulkOps.orgsImport')}</g:link>
                                        <g:link elementId="yoda-makeshiftLaserOrgExport" class="item" role="menuitem" controller="yoda" action="makeshiftLaserOrgExport">${message(code:'menu.admin.exportBasicData')}</g:link>
                                        <g:link elementId="admin-titleEnrichment" class="item" role="menuitem" controller="admin" action="titleEnrichment">Title Enrichment</g:link>
                                    </div>
                                </div>
                                <div class="divider"></div>

                                <g:link elementId="admin-manageNamespaces" class="item" role="menuitem" controller="admin" action="manageNamespaces">${message(code:'menu.admin.manageIdentifierNamespaces')}</g:link>
                                <g:link elementId="admin-managePropertyDefinitions" class="item" role="menuitem" controller="admin" action="managePropertyDefinitions">${message(code:'menu.admin.managePropertyDefinitions')}</g:link>
                                <%--<g:link elementId="admin-managePropertyGroups" class="item" role="menuitem" controller="admin" action="managePropertyGroups">${message(code:'menu.institutions.manage_prop_groups')}</g:link>--%> <%-- property groups are always private?? --%>
                                <g:link elementId="admin-manageRefdatas" class="item" role="menuitem" controller="admin" action="manageRefdatas">${message(code:'menu.admin.manageRefdatas')}</g:link>
                                <g:link elementId="admin-manageContentItems" class="item" role="menuitem" controller="admin" action="manageContentItems">${message(code:'menu.admin.manageContentItems')}</g:link>

                                <div class="divider"></div>

                                <g:link elementId="stats-statsHome" class="item" role="menuitem" controller="stats" action="statsHome">${message(code:'menu.admin.statistics')}</g:link>
                               %{-- <g:link elementId="jasperReports-uploadReport" class="item" role="menuitem" controller="jasperReports" action="uploadReport">Upload Report Definitions</g:link>--}%

                            </div>
                        </div>
                    </sec:ifAnyGranted>

                    <sec:ifAnyGranted roles="ROLE_YODA">
                        <div class="ui dropdown item" role="menuitem" aria-haspopup="true">
                            ${message(code:'menu.yoda')}
                            <i aria-hidden="true" class="dropdown icon"></i>

                            <div class="menu" role="menu">

                                <g:link elementId="yoda-dashboard" class="item" role="menuitem" controller="yoda" action="dashboard">Dashboard</g:link>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    <i class="ui icon keyboard outline"></i>${message(code:'menu.yoda.engine')}
                                    <i aria-hidden="true" class="dropdown icon"></i>

                                    <div class="menu" role="menu">

                                        <g:link elementId="yoda-settings" class="item" role="menuitem" controller="yoda" action="settings">${message(code:'menu.yoda.systemSettings')}</g:link>
                                        <g:link elementId="admin-systemEvents" class="item" role="menuitem" controller="admin" action="systemEvents">${message(code:'menu.admin.systemEvents')}</g:link>
                                        <g:link elementId="yoda-appConfig" class="item" role="menuitem" controller="yoda" action="appConfig">${message(code:'menu.yoda.appConfig')}</g:link>
                                        <g:link elementId="yoda-appThreads" class="item" role="menuitem" controller="yoda" action="appThreads">${message(code:'menu.yoda.appThreads')}</g:link>

                                        <g:link elementId="yoda-systemProfiler" class="item" role="menuitem" controller="yoda" action="systemProfiler"><i class="stopwatch icon"></i>${message(code:'menu.yoda.systemProfiler')}</g:link>
                                        <g:link elementId="yoda-activityProfiler" class="item" role="menuitem" controller="yoda" action="activityProfiler"><i class="stopwatch icon"></i>${message(code:'menu.yoda.activityProfiler')}</g:link>
                                        <g:link elementId="yoda-timelineProfiler" class="item" role="menuitem" controller="yoda" action="timelineProfiler"><i class="stopwatch icon"></i>${message(code:'menu.yoda.timelineProfiler')}</g:link>

                                        <g:link elementId="yoda-quartzInfo" class="item" role="menuitem" controller="yoda" action="quartzInfo">${message(code:'menu.yoda.quartzInfo')}</g:link>
                                        <g:link elementId="yoda-cacheInfo" class="item" role="menuitem" controller="yoda" action="cacheInfo">${message(code:'menu.yoda.cacheInfo')}</g:link>

                                        <g:link elementId="yoda-appSecurity" class="item" role="menuitem" controller="yoda" action="appSecurity">${message(code:'menu.yoda.security')}</g:link>
                                        <g:link elementId="yoda-userMatrix" class="item" role="menuitem" controller="yoda" action="userMatrix">${message(code:'menu.yoda.userMatrix')}</g:link>
                                        <g:link elementId="yoda-userRoleDefinitions" class="item" role="menuitem" controller="yoda" action="userRoleDefinitions">${message(code:'menu.yoda.userRoleDefinitions')}</g:link>

                                        <%--<a class="item" role="menuitem" href="${g.createLink(uri:'/monitoring')}">App Monitoring</a>--%>
                                    </div>
                                </div>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    Fällige Termine
                                    <i aria-hidden="true" class="dropdown icon"></i>
                                    <div class="menu" role="menu">
                                        <g:link elementId="yoda-dueDates_updateDashboardDB" class="item" role="menuitem" controller="yoda" action="dueDates_updateDashboardDB">${message(code:'menu.admin.updateDashboardTable')}</g:link>
                                        <g:link elementId="yoda-dueDates_sendAllEmails" class="item" role="menuitem" controller="yoda" action="dueDates_sendAllEmails">${message(code:'menu.admin.sendEmailsForDueDates')}</g:link>
                                    </div>
                                </div>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    ${message(code:'menu.admin.dataManagement')}
                                    <i aria-hidden="true" class="dropdown icon"></i>

                                    <div class="menu" role="menu">
                                        <g:link elementId="dataManager-listPlatformDuplicates" class="item" role="menuitem" controller="dataManager" action="listPlatformDuplicates">List Platform Duplicates</g:link>
                                        <g:link elementId="package-getDuplicatePackages" class="item" role="menuitem" controller="package" action="getDuplicatePackages">List Package Duplicates</g:link>
                                        <g:link elementId="dataManager-listDeletedTIPPS" class="item" role="menuitem" controller="dataManager" action="listDeletedTIPPS">List TIPP Duplicates and deleted TIPPs</g:link>
                                        <g:link elementId="admin-listDuplicateTitles" class="item" role="menuitem" controller="admin" action="listDuplicateTitles">Check Title GOKb IDs</g:link>
                                        <%--<g:link elementId="admin-tippTransfer" class="item" role="menuitem" controller="admin" action="tippTransfer">${message(code:'menu.admin.tippTransfer')}</g:link>--%>
                                        <%--<g:link elementId="admin-ieTransfer" class="item" role="menuitem" controller="admin" action="ieTransfer">${message(code:'menu.admin.ieTransfer')}</g:link>--%>
                                        <%--<g:link elementId="admin-userMerge" class="item" role="menuitem" controller="admin" action="userMerge">${message(code:'menu.admin.userMerge')}</g:link>--%>
                                        <%--<g:link elementId="admin-hardDeletePkgs" class="item" role="menuitem" controller="admin" action="hardDeletePkgs">${message(code:'menu.admin.hardDeletePkgs')}</g:link>--%>
                                        <g:link elementId="yoda-checkLicenseSubscriptionLinks" class="item" role="menuitem" controller="yoda" action="checkLicenseSubscriptionLinks">${message(code:'menu.admin.bulkOps.checkLicenseLinks')}</g:link>
                                        <g:link elementId="admin-manageDeletedObjects" class="item" role="menuitem" controller="admin" action="manageDeletedObjects">${message(code: "menu.admin.deletedObjects")}</g:link>
                                        <g:link elementId="admin-databaseStatistics" class="item" role="menuitem" controller="admin" action="databaseStatistics">${message(code: "menu.admin.databaseStatistics")}</g:link>
                                        <g:link elementId="admin-dataConsistency" class="item" role="menuitem" controller="admin" action="dataConsistency">${message(code: "menu.admin.dataConsistency")}</g:link>
                                        <g:link elementId="admin-fileConsistency" class="item" role="menuitem" controller="admin" action="fileConsistency">${message(code: "menu.admin.fileConsistency")}</g:link>
                                    </div>
                                </div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    ${message(code:'menu.admin.syncManagement')}
                                    <i aria-hidden="true" class="dropdown icon"></i>
                                    <div class="menu" role="menu">
                                        <g:link elementId="yoda-globalSync" class="item" role="menuitem" controller="yoda" action="globalSync" onclick="return confirm('${message(code:'confirm.start.globalDataSync')}')">${message(code:'menu.yoda.globalDataSync')}</g:link>
                                        <g:link elementId="yoda-manageGlobalSources" class="item" role="menuitem" controller="yoda" action="manageGlobalSources">${message(code:'menu.yoda.manageGlobalSources')}</g:link>
                                        <g:link elementId="yoda-getTIPPsWithoutGOKBId" class="item" role="menuitem" controller="yoda" action="getTIPPsWithoutGOKBId">${message(code:'menu.yoda.purgeTIPPsWithoutGOKBID')}</g:link>
                                        <g:link elementId="yoda-getTIsWithoutGOKBId" class="item" role="menuitem" controller="yoda" action="getTIsWithoutGOKBId">${message(code:'menu.yoda.purgeTIsWithoutGOKBID')}</g:link>
                                        <g:link elementId="yoda-titlesUpdate" class="item" role="menuitem" controller="yoda" action="titlesUpdate">${message(code:'menu.admin.bulkOps.checkLicenseLinks')}</g:link>
                                    </div>
                                </div>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    ElasticSearch
                                    <i aria-hidden="true" class="dropdown icon"></i>
                                    <div class="menu" role="menu">
                                        <g:link elementId="yoda-fullReset" class="item" role="menuitem" controller="yoda" action="fullReset" onclick="return confirm('${message(code:'confirm.start.resetESIndex')}')">${message(code:'menu.yoda.resetESIndex')}</g:link>
                                        <g:link elementId="yoda-esIndexUpdate" class="item" role="menuitem" controller="yoda" action="esIndexUpdate" onclick="return confirm('${message(code:'confirm.start.ESUpdateIndex')}')">${message(code:'menu.yoda.updateESIndex')}</g:link>
                                        <%--<g:link elementId="yoda-logViewer" class="item" role="menuitem" controller="yoda" action="logViewer">Log Viewer</g:link>--%>
                                        <g:link elementId="yoda-manageESSources" class="item" role="menuitem" controller="yoda" action="manageESSources" >Manage ES Source</g:link>
                                        <g:link elementId="yoda-manageFTControl" class="item" role="menuitem" controller="yoda" action="manageFTControl" >Manage FTControl</g:link>
                                        <div class="divider"></div>
                                        <g:link elementId="yoda-killDataloadService" class="item" role="menuitem" controller="yoda" action="killDataloadService" >Kill ES Update Index</g:link>
                                    </div>
                                </div>

                                <div class="divider"></div>

                                <div class="item" role="menuitem" aria-haspopup="true">
                                    ${message(code:'menu.admin.dataMigration')}
                                    <i aria-hidden="true" class="dropdown icon"></i>
                                    <div class="menu" role="menu">
                                        <g:link elementId="yoda-migrateCollectiveSubscriptions" class="item" role="menuitem" controller="yoda" action="migrateCollectiveSubscriptions">Migrate Collective Subscriptions (0.20)</g:link>
                                        <g:link elementId="yoda-migrateNatStatSettings" class="item" role="menuitem" controller="yoda" action="migrateNatStatSettings">Migrate NatStat Settings (0.20)</g:link>
                                        <%--<g:link elementId="yoda-remapOriginEditUrl" class="item" role="menuitem" controller="yoda" action="remapOriginEditUrl">Remap OriginEditUrl (0.20)</g:link>--%>
                                        <g:link elementId="yoda-checkIssueEntitlementPackages" class="item" role="menuitem" controller="yoda" action="checkIssueEntitlementPackages"><g:message code="menu.admin.checkIssueEntitlementPackages"/> (0.20)</g:link>
                                        <g:link elementId="yoda-dbmFixPrivateProperties" class="item" role="menuitem" controller="yoda" action="dbmFixPrivateProperties">Fix Private Properties</g:link>
                                        <g:link elementId="yoda-replaceUserSettingDashboardReminderPeriod" class="item" role="menuitem" controller="yoda" action="replaceUserSettingDashboardReminderPeriod">Replace UserSetting Dashboard ReminderPeriod in Database</g:link>
                                        <g:link elementId="yoda-cleanUpSurveys" class="item" role="menuitem" controller="yoda" action="cleanUpSurveys">Clean Up Surveys with Multi Term</g:link>
                                        <g:link elementId="yoda-insertEditUris" class="item" role="menuitem" controller="yoda" action="insertEditUris">Insert Edit URIs for GOKB Sources</g:link>

                                        <g:link elementId="yoda-surveyCheck" class="item" role="menuitem" controller="yoda" action="surveyCheck">Update Survey Status</g:link>
                                        <g:link elementId="yoda-subscriptionCheck" class="item" role="menuitem" controller="yoda" action="subscriptionCheck">${message(code:'menu.admin.subscriptionsCheck')}</g:link>
                                        <%--<g:link elementId="yoda-updateLinks" class="item" role="menuitem" controller="yoda" action="updateLinks">${message(code:'menu.admin.updateLinks')}</g:link>--%>
                                        <%--<g:link elementId="yoda-startDateCheck" class="item" role="menuitem" controller="yoda" action="startDateCheck">${message(code:'menu.admin.startDatesCheck')}</g:link>--%>
                                        <%--<g:link elementId="yoda-updateTaxRates" class="item" role="menuitem" controller="yoda" action="updateTaxRates">${message(code:'menu.admin.taxTypeCheck')}</g:link>--%>
                                        <%--<g:link elementId="yoda-updateCustomerType" class="item" role="menuitem" controller="yoda" action="updateCustomerType">Kundentyp für alle Einrichtungen setzen</g:link>--%>
                                        <%--<g:link elementId="yoda-showOldDocumentOwners" class="item" role="menuitem" controller="yoda" action="showOldDocumentOwners">${message(code:'menu.admin.documentOwnerCheck')}</g:link>--%>
                                        <%--<g:link elementId="yoda-generateBatchUID" class="item" role="menuitem" controller="yoda" action="generateBatchUID">${message(code:'menu.admin.batchUID')}</g:link>--%>
                                        <%--<g:link elementId="yoda-makeshiftLaserOrgExport" class="item" role="menuitem" controller="yoda" action="makeshiftLaserOrgExport">${message(code:'menu.admin.exportBasicData')}</g:link>--%>
                                        <g:link elementId="yoda-dropDeletedObjects" class="item" role="menuitem" controller="yoda" action="dropDeletedObjects">Drop deleted Objects from Database</g:link>
                                        <g:link elementId="yoda-migratePackageIdentifiers" class="item" role="menuitem" controller="yoda" action="migratePackageIdentifiers">Remap Package Identifier Namespace</g:link>
                                        <%--<g:link elementId="yoda-assignNoteOwners" class="item" role="menuitem" controller="yoda" action="assignNoteOwners">Assign note owners for notes of subscriptions and licenses without owners</g:link>--%>
                                        <%--<g:link elementId="yoda-correctCostsInLocalCurrency" class="item" role="menuitem" controller="yoda" action="correctCostsInLocalCurrency" params="[dryRun: true]">${message(code:'menu.admin.correctCostsInLocalCurrencyDryRun')}</g:link>
                                        <g:link elementId="yoda-correctCostsInLocalCurrency" class="item role="menuitem" js-open-confirm-modal"
                                                data-confirm-tokenMsg = "${message(code: 'confirmation.content.correctCostsInLocalCurrency')}"
                                                data-confirm-term-how="ok"
                                                controller="yoda" action="correctCostsInLocalCurrency" params="[dryRun: false]">${message(code:'menu.admin.correctCostsInLocalCurrencyDoIt')}</g:link>--%>

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

                        <g:link elementId="search-index" controller="search" action="index"
                                class="la-search-advanced la-popup-tooltip la-delay"
                                 data-content="${message(code: 'search.advancedSearch.tooltip')}">
                            <i class="large icons">
                                <i class="search icon"></i>
                                <i class="top grey right corner cog icon "></i>
                            </i>
                        </g:link>

                        <g:if test="${contextUser}">
                            <div class="ui dropdown item la-noBorder" role="menuitem" aria-haspopup="true">
                                ${contextUser.displayName}
                                <i aria-hidden="true" class="dropdown icon"></i>

                                <div class="menu" role="menu">

                                    <g:set var="usaf" value="${contextUser.authorizedOrgs}" />
                                    <g:if test="${usaf && usaf.size() > 0}">
                                        <g:each in="${usaf}" var="org">
                                            <g:if test="${org.id == contextOrg?.id}">
                                                <g:link elementId="myInstitution-switchContext" class="item active" role="menuitem" controller="myInstitution" action="switchContext" params="${[oid:"${org.class.name}:${org.id}"]}">${org.name}</g:link>
                                            </g:if>
                                            <g:else>
                                                <g:link elementId="myInstitution-switchContext" class="item" role="menuitem" controller="myInstitution" action="switchContext" params="${[oid:"${org.class.name}:${org.id}"]}">${org.name}</g:link>
                                            </g:else>
                                        </g:each>
                                    </g:if>

                                    <div class="divider"></div>

                                    <g:link elementId="profile-index" class="item" role="menuitem" controller="profile" action="index">${message(code:'menu.user.profile')}</g:link>
                                    <g:link elementId="profile-help" class="item" role="menuitem" controller="profile" action="help">${message(code:'menu.user.help')}</g:link>
                                    <g:link elementId="profile-dsgvo" class="item" role="menuitem" controller="profile" action="dsgvo">${message(code:'privacyNotice')}</g:link>

                                    <div class="divider"></div>

                                    <g:link elementId="logout" class="item" role="menuitem" controller="logout">${message(code:'menu.user.logout')}</g:link>
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
                        <g:link elementId="logout"  class="item" controller="logout">${message(code:'menu.user.logout')}</g:link>
                    </sec:ifLoggedIn>
                </sec:ifNotGranted>

            </div><!-- container -->

        </div><!-- main menu -->
    </nav>

    <sec:ifAnyGranted roles="ROLE_USER">
        <g:set var="visibilityContextOrgMenu" value="la-show-context-orgMenu" />
        <nav class="ui fixed  stackable  menu la-contextBar" aria-label="${message(code:'wcag.label.modeNavigation')}" >
            <div class="ui container">
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
                                    <button class="ui icon toggle button la-toggle-controls la-popup-tooltip la-delay"  data-content="${message(code:'statusbar.hideButtons.tooltip')}"  data-position="bottom right">
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
                            <asset:script type="text/javascript">
                                var LaToggle = {};
                                LaToggle.advanced = {};
                                LaToggle.advanced.button = {};

                                // ready event
                                LaToggle.advanced.button.ready = function() {
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

                                LaToggle.advanced.button.ready();
                            </asset:script>
                        </g:if>
                </div>

            </div>

        </nav><!-- Context Bar -->
    </sec:ifAnyGranted><%-- ROLE_USER --%>

        <%-- global content container --%>
        <main class="ui main container ${visibilityContextOrgMenu} ">

            <%-- system messages --%>
            <g:set var="systemMessages" value="${SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION)}" />
            <g:if test="${systemMessages}">
                <div class="ui segment center aligned orange">
                    <strong>SYSTEMMELDUNG</strong>

                    <g:each in="${systemMessages}" var="message">
                        <div style="padding-top:1em">
                            <% println message.getLocalizedContent() %>
                        </div>
                    </g:each>

                </div>
            </g:if>

            %{-- content --}%

            <g:layoutBody/>

        </main><!-- .main -->

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

        %{-- <a href="#globalJumpMark" class="ui button icon" style="position:fixed;right:0;bottom:0;"><i class="angle up icon"></i></a> --}%

        %{-- decksaver --}%

        <g:if test="${(controllerName=='dev' && actionName=='frontend' ) || (controllerName=='subscription'|| controllerName=='license') && actionName=='show'}">
            <asset:script type="text/javascript">

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

                $(document).ready(function() {
                    deckSaver.configs.ajaxUrl = '<g:createLink controller="ajax" action="toggleEditMode"/>';
                    deckSaver.go();
                })
            </asset:script>
        </g:if>

        %{-- maintenance --}%

        <g:if test="${SystemSetting.findByName('MaintenanceMode')?.value == 'true'}">
            <div id="maintenance">
                <div class="ui segment center aligned inverted orange">
                    <h3 class="ui header">${message(code:'system.maintenanceMode.header')}</h3>

                    ${message(code:'system.maintenanceMode.message')}

                <div>
            </div>
        </g:if>

        %{-- ajax login --}%

        <g:if test="${controllerName != 'login'}">
            <g:render template="/templates/ajax/login" />
        </g:if>

        %{-- system info --}%

        <% if(! flash.redirectFrom) { flash.clear() } %>

        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <semui:systemInfo />

            <div id="system-profiler" class="ui label hidden">
                <i class="clock icon"></i>
                <span></span>
            </div>
        </sec:ifAnyGranted>

        %{-- javascript loading --}%

        <asset:javascript src="${currentTheme}.js"/>%{-- dont move --}%

        <asset:deferredScripts/>%{-- dont move --}%

        %{-- profiler --}%

        <script>
            $(document).ready(function() {
                $.ajax({
                    url: "${g.createLink(controller:'ajax', action:'notifyProfiler')}",
                    <%--data: {uri: "${request.request.request.request.servletPath.replaceFirst('/grails','').replace('.dispatch','')}"},--%>
                    data: {uri: "${ ProfilerUtils.generateKey( webRequest )}"},
                    success: function (data) {
                        var $sp = $('#system-profiler')
                        if ($sp) {
                            if (data.delta > 0) {
                                $sp.removeClass('hidden').find('span').empty().append(data.delta + ' ms')
                            }
                        }
                    }
                })
            })
        </script>

        %{-- jsqtk --}%

        <sec:ifAnyGranted roles="ROLE_YODA">
            <div id="system-jsqtk" class="ui button">
                <i class="terminal icon"></i>
                <span>jsqtk.go()</span>
            </div>
            <script>
                $(document).ready(function() {
                    jsqtk.go()
                    $('#system-jsqtk').on('click', function(){
                        jsqtk.go()
                    })
                })
            </script>
        </sec:ifAnyGranted>

    </body>
</html>
