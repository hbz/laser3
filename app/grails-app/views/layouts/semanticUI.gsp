<%@ page import="de.laser.helper.RDStore;org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;com.k_int.kbplus.Org;com.k_int.kbplus.auth.User;com.k_int.kbplus.UserSettings;com.k_int.kbplus.RefdataValue" %>
<!doctype html>

<laser:serviceInjection />
<%
    User currentUser = contextService.getUser()
    String currentLang = 'de'
    String currentTheme = 'semanticUI'

    if (currentUser) {
        RefdataValue rdvLocale = currentUser?.getSetting(UserSettings.KEYS.LANGUAGE, RefdataValue.getByValueAndCategory('de', 'Language'))?.getValue()

        if (rdvLocale) {
            currentLang = rdvLocale.value
            org.springframework.web.servlet.LocaleResolver localeResolver = org.springframework.web.servlet.support.RequestContextUtils.getLocaleResolver(request)
            localeResolver.setLocale(request, response, new Locale(currentLang, currentLang.toUpperCase()))
        }

        RefdataValue rdvTheme = currentUser?.getSetting(UserSettings.KEYS.THEME, RefdataValue.getByValueAndCategory('semanticUI', 'User.Settings.Theme'))?.getValue()

        if (rdvTheme) {
            currentTheme = rdvTheme.value
        }
    }
%>

<!--[if lt IE 7]> <html class="no-js lt-ie9 lt-ie8 lt-ie7" lang="${currentLang}"> <![endif]-->
<!--[if IE 7]><html class="no-js lt-ie9 lt-ie8" lang="${currentLang}"> <![endif]-->
<!--[if IE 8]><html class="no-js lt-ie9" lang="${currentLang}"> <![endif]-->
<!--[if gt IE 8]><!--><html class="no-js" lang="${currentLang}"> <!--<![endif]-->

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title><g:layoutTitle default="${meta(name: 'app.name')}"/></title>
    <meta name="description" content="">
    <meta name="viewport" content="initial-scale = 1.0">

    <r:require modules="${currentTheme}" />

    <script>
        var gspLocale = "${message(code:'default.locale.label')}";
        var gspDateFormat = "${message(code:'default.date.format.notime').toLowerCase()}";
    </script>

    <g:layoutHead/>

    <tmpl:/layouts/favicon />

    <r:layoutResources/>

</head>

<body class="${controllerName}_${actionName}" id="globalJumpMark">

    <g:set var="contextOrg" value="${contextService.getOrg()}" />
    <g:set var="contextUser" value="${contextService.getUser()}" />
    <g:set var="contextMemberships" value="${contextService.getMemberships()}" />

    <g:if test="${grailsApplication.config.getCurrentServer() == contextService.SERVER_DEV}">
        <div class="ui green label big la-server-label">
            <span>DEV</span>
        </div>
    </g:if>
    <g:if test="${grailsApplication.config.getCurrentServer() == contextService.SERVER_QA}">
        <div class="ui red label big la-server-label">
            <span>QA</span>
        </div>
    </g:if>
    <g:set var="visibilityContextOrgMenu" value="la-hide-context-orgMenu"></g:set>
    <nav class="ui fixed inverted stackable menu">
        <div class="ui container">
            <g:link controller="home" action="index" class="header item la-logo-item">
                <img alt="Logo Laser" class="logo" src="${resource(dir: 'images', file: 'laser.svg')}"/>
            </g:link>

            <sec:ifAnyGranted roles="ROLE_USER">

                <g:if test="${false}">
                    <div class="ui simple dropdown item">
                        Data Explorer
                        <i class="dropdown icon"></i>

                        <div class="menu">
                            <a class="item" href="${createLink(uri: '/home/search')}">Search</a>
                            <g:link class="item" controller="package">Package</g:link>
                            <g:link class="item" controller="organisation">Organisations</g:link>
                            <g:link class="item" controller="platform">Platform</g:link>
                            <g:link class="item" controller="title">Title Instance</g:link>
                            <g:link class="item" controller="tipp">Title Instance Package Platform</g:link>
                            <g:link class="item" controller="subscription">Subscriptions</g:link>
                            <g:link class="item" controller="license">Licenses</g:link>
                            <g:link class="item" controller="onixplLicense" action="list">ONIX-PL Licenses</g:link>
                        </div>
                    </div>
                </g:if>

                <g:if test="${contextOrg}">
                    <div class="ui simple dropdown item">
                        ${message(code:'menu.public')}
                        <i class="dropdown icon"></i>

                        <div class="menu">
                                <g:link class="item" controller="package" action="index">${message(code:'menu.public.all_pkg')}</g:link>

                                <g:link class="item" controller="title" action="index">${message(code:'menu.public.all_titles')}</g:link>

                                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_EDITOR">
                                    <g:link class="item" controller="organisation" action="index">${message(code:'menu.public.all_orgs')}</g:link>
                                </sec:ifAnyGranted>

                                <g:if test="${RDStore.OT_CONSORTIUM.id in  contextService.org.getallOrgTypeIds()}">
                                    <g:link class="item" controller="organisation" action="listInstitution">${message(code:'menu.public.all_insts')}</g:link>
                                </g:if>

                                <g:link class="item" controller="organisation" action="listProvider">${message(code:'menu.public.all_provider')}</g:link>

                                <g:link class="item" controller="platform" action="list">${message(code:'menu.public.all_platforms')}</g:link>

                                <g:link class="item" controller="gasco">${message(code:'menu.public.gasco_monitor')}</g:link>

                            <%--<div class="divider"></div>

                            <g:link class="item" controller="myInstitution" action="currentTitles">${message(code:'menu.my.titles')}</g:link>
                            <g:link class="item" controller="myInstitution" action="tipview">${message(code:'menu.institutions.myCoreTitles')}</g:link>
                            --%>
                            <div class="divider"></div>

                            <g:if test="${grailsApplication.config.feature.eBooks}">
                                <a class="item" href="http://gokb.k-int.com/gokbLabs">${message(code:'menu.institutions.ebooks')}</a>
                                <div class="divider"></div>
                            </g:if>

                            <g:link class="item" controller="package" action="compare">${message(code:'menu.public.comp_pkg')}</g:link>
                        </div>
                    </div>

                    <div class="ui simple dropdown item">
                        ${message(code:'menu.my')}
                        <i class="dropdown icon"></i>

                        <div class="menu">

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="currentSubscriptions" message="menu.my.subscriptions" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="currentLicenses" message="menu.my.licenses" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="currentProviders" message="menu.my.providers" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="currentTitles" message="menu.my.titles" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="documents" message="menu.my.documents" />

                            <g:if test="${RDStore.OT_CONSORTIUM.id in  contextService.org.getallOrgTypeIds()}">
                                <div class="divider"></div>

                                <semui:securedMainNavItem affiliation="INST_ADM" controller="myInstitution" action="manageConsortia" message="menu.my.consortia" />

                                <semui:securedMainNavItem affiliation="INST_ADM" controller="myInstitution" action="manageConsortiaSubscriptions" message="menu.my.consortiaSubscriptions" />
                            </g:if>

                            <%--<semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="tipview" message="menu.institutions.myCoreTitles" />--%>

                            <div class="divider"></div>

                            <semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" action="emptySubscription" message="menu.institutions.emptySubscription" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="subscription" action="compare" message="menu.my.comp_sub" />

                            <%--<g:link class="item" controller="subscriptionImport" action="generateImportWorksheet"
                                    params="${[id:contextOrg?.id]}">${message(code:'menu.institutions.sub_work')}</g:link>
                            <g:link class="item" controller="subscriptionImport" action="importSubscriptionWorksheet"
                                    params="${[id:contextOrg?.id]}">${message(code:'menu.institutions.imp_sub_work')}</g:link>--%>

                            <div class="divider"></div>

                            <semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" action="emptyLicense" message="license.add.blank" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="licenseCompare" action="index" message="menu.my.comp_lic" />


                            <%--
                            <div class="divider"></div>
                            <g:link class="item" controller="subscription" action="compare">${message(code:'menu.my.comp_sub')}</g:link>

                            <g:link class="item" controller="myInstitution" action="renewalsSearch">${message(code:'menu.institutions.gen_renewals')}</g:link>
                            <g:link class="item" controller="myInstitution" action="renewalsUpload">${message(code:'menu.institutions.imp_renew')}</g:link>

                            <g:link class="item" controller="subscriptionImport" action="generateImportWorksheet"
                                    params="${[id:contextOrg?.id]}">${message(code:'menu.institutions.sub_work')}</g:link>
                            <g:link class="item" controller="subscriptionImport" action="importSubscriptionWorksheet"
                                    params="${[id:contextOrg?.id]}">${message(code:'menu.institutions.imp_sub_work')}</g:link>--%>
                        </div>
                    </div>


                    <div class="ui simple dropdown item">
                        ${message(code:'menu.institutions.myInst')}
                        <i class="dropdown icon"></i>

                        <div class="menu">
                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="dashboard" message="menu.institutions.dash" />

                            <g:link class="item" controller="organisation" action="show" params="[id: contextOrg?.id]">${message(code:'menu.institutions.org_info')}</g:link>

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="tasks" message="menu.institutions.tasks" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="changes" message="menu.institutions.todo" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="addressbook" message="menu.institutions.addressbook" />

                            <g:set var="myInstNewAffils" value="${com.k_int.kbplus.auth.UserOrg.findAllByStatusAndOrg(0, contextService.getOrg(), [sort:'dateRequested']).size()}" />

                            <semui:securedMainNavItem affiliation="INST_ADM" controller="organisation" action="users" params="[id: contextOrg?.id]"
                                                      message="menu.institutions.users" newAffiliationRequests="${myInstNewAffils}" />

                            <semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" action="managePrivateProperties" message="menu.institutions.manage_private_props" />
                            <semui:securedMainNavItem affiliation="INST_EDITOR"  controller="myInstitution" action="managePropertyGroups" message="menu.institutions.manage_prop_groups" />

                            <g:if test="${grailsApplication.config.feature_finance}">
                                <semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" action="finance" message="menu.institutions.finance" />
                                <semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" action="budgetCodes" message="menu.institutions.budgetCodes" />
                                <semui:securedMainNavItem affiliation="INST_ADM" controller="costConfiguration" action="index" message="menu.institutions.costConfiguration" />
                                <%--<semui:securedMainNavItemDisabled message="menu.institutions.financeImport" />--%>
                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" action="financeImport" message="menu.institutions.financeImport" />
                                </sec:ifAnyGranted>
                            </g:if>

                            <sec:ifAnyGranted roles="ROLE_YODA">
                                <div class="divider"></div>

                                <g:link class="item" controller="myInstitution" action="changeLog">${message(code:'menu.institutions.change_log')}</g:link>
                                <%--<semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" action="changeLog" message="menu.institutions.change_log" />--%>
                            </sec:ifAnyGranted>

                        </div>
                    </div>
                </g:if>

                <sec:ifAnyGranted roles="ROLE_ORG_MANAGER,ROLE_DATAMANAGER,ROLE_ADMIN,ROLE_GLOBAL_DATA,ROLE_STATISTICS_EDITOR">
                    <div class="ui simple dropdown item">
                        ${message(code:'menu.datamanager')}
                        <i class="dropdown icon"></i>

                        <div class="menu">
                            <sec:ifAnyGranted roles="ROLE_STATISTICS_EDITOR">
                                <g:link class="item" controller="usage"
                                        action="index">${message(code: 'menu.datamanager.manage_usage_stats', default: 'Manage Usage Stats')}</g:link>
                            </sec:ifAnyGranted>


                            <sec:ifAnyGranted roles="ROLE_DATAMANAGER,ROLE_ADMIN">
                                <g:link class="item" controller="dataManager" action="index">${message(code:'menu.datamanager.dash')}</g:link>
                                <g:link class="item" controller="dataManager"
                                        action="deletedTitles">${message(code: 'datamanager.deletedTitleManagement.label', default: 'Deleted Title management')}</g:link>
                            </sec:ifAnyGranted>

                            <sec:ifAnyGranted roles="ROLE_ORG_MANAGER,ROLE_ADMIN">
                                <g:link class="item" controller="dataManager"
                                        action="deletedOrgs">${message(code: 'datamanager.deletedOrgManagement.label', default: 'Deleted Org management')}</g:link>
                            </sec:ifAnyGranted>

                            <sec:ifAnyGranted roles="ROLE_DATAMANAGER,ROLE_ADMIN">
                                <div class="divider"></div>

                                <g:link class="item" controller="announcement" action="index">${message(code:'menu.datamanager.ann')}</g:link>
                                <g:link class="item" controller="package" action="list">${message(code:'menu.datamanager.searchPackages')}</g:link>
                                <g:link class="item" controller="platform" action="list">${message(code:'menu.datamanager.searchPlatforms')}</g:link>

                                <div class="divider"></div>

                                <g:link class="item" controller="upload" action="reviewPackage">${message(code:'menu.datamanager.uploadPackage')}</g:link>
                                <g:link class="item" controller="licenseImport" action="doImport">${message(code:'onix.import.license')}</g:link>

                                <div class="divider"></div>

                                <g:link class="item" controller="title" action="findTitleMatches">${message(code:'menu.datamanager.newTitle')}</g:link>
                                <g:link class="item" controller="license" action="create">${message(code:'license.template.new')}</g:link>
                                <g:link class="item" controller="platform" action="create">${message(code:'menu.datamanager.newPlatform')}</g:link>

                                <g:link class="item" controller="subscription" action="compare">${message(code:'menu.datamanager.compareSubscriptions')}</g:link>
                                <g:link class="item" controller="subscriptionImport" action="generateImportWorksheet">${message(code:'menu.datamanager.sub_work')}</g:link>
                                <g:link class="item" controller="subscriptionImport" action="importSubscriptionWorksheet" params="${[dm:'true']}">${message(code:'menu.datamanager.imp_sub_work')}</g:link>
                                <g:link class="item" controller="onixplLicenseCompare" action="index">${message(code:'menu.institutions.comp_onix')}</g:link>
                                <g:link class="item" controller="dataManager" action="changeLog">${message(code:'menu.datamanager.changelog')}</g:link><div class="divider"></div>
                            </sec:ifAnyGranted>

                            <sec:ifAnyGranted roles="ROLE_DATAMANAGER,ROLE_ADMIN,ROLE_GLOBAL_DATA">
                                <g:link class="item" controller="globalDataSync" action="index" >${message(code:'menu.datamanager.global_data_sync')}</g:link>
                                <g:link class="item" controller="dataManager" action="checkPackageTIPPs">Tipps Check of GOKB and LAS:eR</g:link>
                            </sec:ifAnyGranted>

                            <sec:ifAnyGranted roles="ROLE_DATAMANAGER,ROLE_ADMIN">
                                <div class="divider"></div>
                                <g:link class="item" controller="jasperReports" action="index">${message(code:'menu.datamanager.jasper_reports')}</g:link>
                                <g:link class="item" controller="title" action="dmIndex">${message(code:'menu.datamanager.titles')}</g:link>
                            </sec:ifAnyGranted>
                        </div>
                    </div>
                </sec:ifAnyGranted>

                <sec:ifAnyGranted roles="ROLE_ADMIN">
                    <div class="ui simple dropdown item">
                        ${message(code:'menu.admin')}
                        <i class="dropdown icon"></i>

                        <div class="menu">
                            <g:link class="item" controller="profile" action="errorOverview">
                                ${message(code: "menu.user.errorReport")}
                                <g:set var="newTickets" value="${com.k_int.kbplus.SystemTicket.getNew().size()}" />
                                <g:if test="${newTickets > 0}">
                                    <div class="ui floating red circular label">${newTickets}</div>
                                </g:if>
                            </g:link>

                            <g:link class="item" controller="admin" action="manageAffiliationRequests">
                                ${message(code: "menu.institutions.affiliation_requests")}
                                <g:set var="newAffiliationRequests" value="${com.k_int.kbplus.auth.UserOrg.findAllByStatus(0).size()}" />
                                <g:if test="${newAffiliationRequests > 0}">
                                    <div class="ui floating red circular label">${newAffiliationRequests}</div>
                                </g:if>
                            </g:link>

                            <div class="ui dropdown item">
                                ${message(code:'menu.admin.sysAdmin')}
                                <i class="dropdown icon"></i>

                                <div class="menu">
                                    <g:link class="item" controller="yoda" action="appInfo">${message(code:'menu.admin.appInfo')}</g:link>
                                    <g:link class="item" controller="admin" action="systemEvents">${message(code:'menu.admin.systemEvents')}</g:link>
                                    <g:link class="item" controller="admin" action="eventLog">Event Log (old)</g:link>

                                    <div class="divider"></div>

                                    <g:link class="item" controller="admin" action="triggerHousekeeping" onclick="return confirm('${message(code:'confirm.start.HouseKeeping')}')">${message(code:'menu.admin.triggerHousekeeping')}</g:link>
                                    <g:link class="item" controller="admin" action="initiateCoreMigration" onclick="return confirm('${message(code:'confirm.start.CoreMigration')}')">${message(code:'menu.admin.coreMigration')}</g:link>
                                    <g:if test="${grailsApplication.config.feature.issnl}">
                                        <g:link class="item" controller="admin" action="uploadIssnL">Upload ISSN to ISSN-L File</g:link>
                                    </g:if>
                                    <g:link class="item" controller="admin" action="dataCleanse" onclick="return confirm('${message(code:'confirm.start.DataCleaningNominalPlatforms')}')">Run Data Cleaning (Nominal Platforms)</g:link>
                                    <%-- <g:link class="item" controller="admin" action="titleAugment" onclick="return confirm('${message(code:'confirm.start.DataCleaningTitleAugment')}')">Run Data Cleaning (Title Augment)</g:link> --%>
                                </div>
                            </div>

                            <div class="divider"></div>

                            <div class="ui dropdown item">
                                ${message(code:'org.plural.label')}
                                <i class="dropdown icon"></i>

                                <div class="menu">
                                    <g:link class="item" controller="organisation" action="index">${message(code:'menu.admin.allOrganisations')}</g:link>
                                    <g:link class="item" controller="admin" action="manageOrganisations">${message(code:'menu.admin.manageOrganisations')}</g:link>
                                </div>
                            </div>

                            <g:link class="item" controller="user" action="list">${message(code:'menu.institutions.users')}</g:link>
                            <g:link class="item" controller="admin" action="showAffiliations">${message(code:'menu.admin.showAffiliations')}</g:link>
                            <g:link class="item" controller="usage">${message(code:'menu.admin.manageUsageStats')}</g:link>
                            <% /* g:link class="item" controller="admin" action="forumSync">Run Forum Sync</g:link */ %>
                            <% /* g:link class="item" controller="admin" action="juspSync">Run JUSP Sync</g:link */ %>
                            <g:link class="item" controller="admin" action="forceSendNotifications">${message(code:'menu.admin.sendNotifications')}</g:link>

                            <div class="ui dropdown item">
                                ${message(code:'menu.admin.dataManagement')}
                                <i class="dropdown icon"></i>

                                <div class="menu">
                                    <g:link class="item" controller="dataManager" action="expungeDeletedTitles" onclick="return confirm('${message(code:'confirm.expunge.deleted.titles')}')">Expunge Deleted Titles</g:link>
                                    <g:link class="item" controller="dataManager" onclick="return confirm('${message(code:'confirm.expunge.deleted.tipps')}')" action="expungeDeletedTIPPS">Expunge Deleted TIPPS</g:link>
                                    <g:link class="item" controller="admin" action="titleMerge">${message(code:'menu.admin.titleMerge')}</g:link>
                                    <g:link class="item" controller="admin" action="tippTransfer">${message(code:'menu.admin.tippTransfer')}</g:link>
                                    <g:link class="item" controller="admin" action="ieTransfer">${message(code:'menu.admin.ieTransfer')}</g:link>
                                    <g:link class="item" controller="admin" action="userMerge">${message(code:'menu.admin.userMerge')}</g:link>
                                    <g:link class="item" controller="admin" action="hardDeletePkgs">${message(code:'menu.admin.hardDeletePkgs')}</g:link>
                                    <g:link class="item" controller="admin" action="dataConsistency">${message(code: "menu.admin.dataConsistency")}</g:link>
                                </div>
                            </div>

                            <div class="divider"></div>

                            <div class="ui dropdown item">
                                ${message(code:'menu.admin.bulkOps')}
                               <i class="dropdown icon"></i>

                               <div class="menu">
                                    <g:link class="item" controller="admin" action="orgsExport">${message(code:'menu.admin.bulkOps.orgsExport')}</g:link>
                                    <g:link class="item" controller="admin" action="orgsImport">${message(code:'menu.admin.bulkOps.orgsImport')}</g:link>
                                    <g:link class="item" controller="admin" action="titlesImport">${message(code:'menu.admin.bulkOps.titlesImport')}</g:link>
                                    <g:link class="item" controller="admin" action="financeImport">${message(code:'menu.admin.bulkOps.financeImport')}</g:link>
                                </div>
                            </div>
                            <div class="divider"></div>

                            <g:link class="item" controller="admin" action="manageNamespaces">${message(code:'menu.admin.manageIdentifierNamespaces')}</g:link>
                            <g:link class="item" controller="admin" action="managePropertyDefinitions">${message(code:'menu.admin.managePropertyDefinitions')}</g:link>
                            <g:link class="item" controller="admin" action="managePropertyGroups">${message(code:'menu.institutions.manage_prop_groups')}</g:link>
                            <g:link class="item" controller="admin" action="manageRefdatas">${message(code:'menu.admin.manageRefdatas')}</g:link>
                            <g:link class="item" controller="admin" action="manageContentItems">${message(code:'menu.admin.manageContentItems')}</g:link>

                            <div class="divider"></div>

                            <g:link class="item" controller="stats" action="statsHome">${message(code:'menu.admin.statistics')}</g:link>
                           %{-- <g:link class="item" controller="jasperReports" action="uploadReport">Upload Report Definitions</g:link>--}%

                        </div>
                    </div>
                </sec:ifAnyGranted>

                <sec:ifAnyGranted roles="ROLE_YODA">
                    <div class="ui simple dropdown item">
                        ${message(code:'menu.yoda')}
                        <i class="dropdown icon"></i>

                        <div class="menu">

                            <g:link class="item" controller="yoda" action="dashboard">Dashboard</g:link>

                            <div class="ui dropdown item">
                                Dagobah
                                <i class="dropdown icon"></i>

                                <div class="menu">

                                    <g:link class="item" controller="yoda" action="settings">${message(code:'menu.yoda.systemSettings')}</g:link>
                                    <g:link class="item" controller="yoda" action="manageSystemMessage">${message(code: 'menu.admin.systemMessage')}</g:link>
                                    <g:link class="item" controller="yoda" action="appConfig">${message(code:'menu.yoda.appConfig')}</g:link>


                                    <g:link class="item" controller="yoda" action="profiler">${message(code:'menu.yoda.profiler')}</g:link>
                                    <g:link class="item" controller="yoda" action="quartzInfo">${message(code:'menu.yoda.quartzInfo')}</g:link>
                                    <g:link class="item" controller="yoda" action="cacheInfo">${message(code:'menu.yoda.cacheInfo')}</g:link>

                                    <g:link class="item" controller="yoda" action="appSecurity">${message(code:'menu.yoda.security')}</g:link>
                                    <g:link class="item" controller="yoda" action="userMatrix">${message(code:'menu.yoda.userMatrix')}</g:link>
                                    <g:link class="item" controller="yoda" action="userRoleDefinitions">${message(code:'menu.yoda.userRoleDefinitions')}</g:link>

                                    <%--<a class="item" href="${g.createLink(uri:'/monitoring')}">App Monitoring</a>--%>
                                </div>
                            </div>

                            <div class="divider"></div>

                            <g:link class="item" controller="yoda" action="pendingChanges">${message(code:'menu.yoda.pendingChanges')}</g:link>

                            <div class="divider"></div>

                            <div class="ui dropdown item">
                                Fällige Termine
                                <i class="dropdown icon"></i>
                                <div class="menu">
                                    <g:link class="item" controller="yoda" action="dueDates_updateDashboardDB">${message(code:'menu.admin.updateDashboardTable')}</g:link>
                                    <g:link class="item" controller="yoda" action="dueDates_sendAllEmails">${message(code:'menu.admin.sendEmailsForDueDates')}</g:link>
                                </div>
                            </div>

                            <div class="divider"></div>

                            <g:link class="item" controller="yoda" action="globalSync" onclick="return confirm('${message(code:'confirm.start.globalDataSync')}')">${message(code:'menu.yoda.globalDataSync')}</g:link>
                            <g:link class="item" controller="yoda" action="manageGlobalSources">${message(code:'menu.yoda.manageGlobalSources')}</g:link>

                            <div class="divider"></div>

                            <g:link class="item" controller="yoda" action="fullReset" onclick="return confirm('${message(code:'confirm.start.resetESIndex')}')">${message(code:'menu.yoda.resetESIndex')}</g:link>
                            <g:link class="item" controller="yoda" action="esIndexUpdate" onclick="return confirm('${message(code:'confirm.start.ESUpdateIndex')}')">${message(code:'menu.yoda.updateESIndex')}</g:link>
                            <%--<g:link class="item" controller="yoda" action="logViewer">Log Viewer</g:link>--%>
                            <g:link class="item" controller="yoda" action="manageESSources" >Manage ES Source</g:link>

                            <div class="divider"></div>

                            <div class="ui dropdown item">
                                ${message(code:'menu.admin.dataMigration')}
                                <i class="dropdown icon"></i>
                                <div class="menu">
                                    <%--<g:link class="item" controller="yoda" action="subscriptionCheck">${message(code:'menu.admin.subscriptionsCheck')}</g:link>--%>
                                    <%--<g:link class="item" controller="yoda" action="updateLinks">${message(code:'menu.admin.updateLinks')}</g:link>--%>
                                    <%--<g:link class="item" controller="yoda" action="startDateCheck">${message(code:'menu.admin.startDatesCheck')}</g:link>--%>
                                    <g:link class="item" controller="yoda" action="updateTaxRates">${message(code:'menu.admin.taxTypeCheck')}</g:link>
                                    <g:link class="item" controller="yoda" action="dbmFixPrivateProperties">Fix Private Properties</g:link>
                                    <g:link class="item" controller="yoda" action="updateCustomerType">Kundentyp (Konsorte) für alle Einrichtungen setzen</g:link>
                                    <%--<g:link class="item" controller="yoda" action="showOldDocumentOwners">${message(code:'menu.admin.documentOwnerCheck')}</g:link>--%>
                                    <g:link class="item" controller="yoda" action="generateBatchUID">${message(code:'menu.admin.batchUID')}</g:link>
                                    <g:link class="item" controller="yoda" action="makeshiftLaserOrgExport">${message(code:'menu.admin.exportBasicData')}</g:link>
                                </div>
                            </div>

                            <div class="divider"></div>
                            <g:link class="item" controller="yoda" action="frontend" >Frontend für Entwickler</g:link>

                        </div>

                    </div>
                </sec:ifAnyGranted>

                <div class="right menu">
                    <div id="mainSearch" class="ui category search">
                        <div class="ui icon input">
                            <input  type="search" id="spotlightSearch" class="prompt" placeholder="${message(code:'spotlight.search.placeholder')}">
                            <i id="btn-search"  class="search icon"></i>
                        </div>
                        <div class="results" style="overflow-y:scroll;max-height: 400px;"></div>
                    </div>

                    <g:if test="${contextUser}">
                        <div class="ui simple dropdown item la-noBorder">
                            ${contextUser.displayName}
                            <i class="dropdown icon"></i>

                            <div class="menu">

                                <g:set var="usaf" value="${contextUser.authorizedOrgs}" />
                                <g:if test="${usaf && usaf.size() > 0}">
                                    <g:each in="${usaf}" var="org">
                                        <g:if test="${org.id == contextOrg?.id}">
                                            <g:link class="item active" controller="myInstitution" action="switchContext" params="${[oid:"${org.class.name}:${org.id}"]}">${org.name}</g:link>
                                        </g:if>
                                        <g:else>
                                            <g:link class="item" controller="myInstitution" action="switchContext" params="${[oid:"${org.class.name}:${org.id}"]}">${org.name}</g:link>
                                        </g:else>
                                    </g:each>
                                </g:if>

                                <div class="divider"></div>

                                <g:link class="item" controller="profile" action="properties">${message(code: 'menu.user.properties', default: 'Properties and Refdatas')}</g:link>

                                <div class="divider"></div>

                                <g:link class="item" controller="profile" action="index">${message(code:'menu.user.profile')}</g:link>
                                <g:link class="item" controller="profile" action="help">${message(code:'menu.user.help')}</g:link>
                                <g:link class="item" controller="profile" action="errorReport">${message(code:'menu.user.errorReport')}</g:link>
                                <a href="https://www.hbz-nrw.de/datenschutz" class="item" target="_blank" >${message(code:'dse')}</a>

                                <div class="divider"></div>

                                <g:link class="item" controller="logout">${message(code:'menu.user.logout')}</g:link>
                                <div class="divider"></div>
                                <g:if test="${grailsApplication.metadata['app.version']}">
                                    <div class="header">Version: ${grailsApplication.metadata['app.version']} – ${grailsApplication.metadata['app.buildDate']}</div>
                                </g:if>
                            </div>
                        </div>
                    </g:if>
                </div>

            </sec:ifAnyGranted><%-- ROLE_USER --%>

            <sec:ifNotGranted roles="ROLE_USER">
                <sec:ifLoggedIn>
                    <g:link class="item" controller="logout">${message(code:'menu.user.logout')}</g:link>
                </sec:ifLoggedIn>
            </sec:ifNotGranted>

        </div><!-- container -->

    </nav><!-- main menu -->

    <sec:ifAnyGranted roles="ROLE_USER">
        <g:set var="visibilityContextOrgMenu" value="la-show-context-orgMenu"></g:set>
        <nav class="ui fixed  stackable  menu la-contextBar"  >
            <div class="ui container">
                <div class="ui sub header item la-context-org">${contextOrg?.name}</div>
                <div class="right menu la-advanced-view">
                    <div class="item">
                        <g:if test="${cachedContent}">
                            <button class="ui icon button" data-tooltip="${message(code:'statusbar.cachedContent.tooltip')}" data-position="bottom right" data-variation="tiny">
                                <i class="hourglass end icon green"></i>
                            </button>
                        </g:if>
                    </div>

                        <g:if test="${controllerName=='subscription' && actionName=='show'}">
                            <div class="item">
                                <g:if test="${user?.getSettingsValue(UserSettings.KEYS.SHOW_EDIT_MODE, RefdataValue.getByValueAndCategory('Yes','YN'))?.value=='Yes'}">
                                    <button class="ui icon toggle button la-toggle-controls" data-tooltip="${message(code:'statusbar.showButtons.tooltip')}" data-position="bottom right" data-variation="tiny">
                                        <i class="pencil alternate icon"></i>
                                    </button>
                                </g:if>
                                <g:else>
                                    <button class="ui icon toggle button active la-toggle-controls"  data-tooltip="${message(code:'statusbar.hideButtons.tooltip')}"  data-position="bottom right" data-variation="tiny">
                                        <i class="pencil alternate slash icon"></i>
                                    </button>
                                </g:else>

                            <r:script>
                                $(function(){
                                     <g:if test="${user?.getSettingsValue(UserSettings.KEYS.SHOW_EDIT_MODE, RefdataValue.getByValueAndCategory('Yes','YN'))?.value=='Yes'}">
                                        var editMode = true;
                                    </g:if>
                                    <g:else>
                                        var editMode = false;
                                    </g:else>
                                    $(".ui.toggle.button").click(function(){
                                        editMode = !editMode;
                                        $.ajax({
                                            url: '<g:createLink controller="ajax" action="toggleEditMode"/>',
                                            data: {
                                                showEditMode: editMode
                                            },
                                            success: function(){
                                                toggleEditableElements()
                                            }
                                        })
                                    });
                                    function toggleEditableElements(){
                                        // for future handling on other views
                                        // 1. add class 'hidden' via markup to all cards that might be toggled
                                        // 2. add class 'la-js-hideable' to all cards that might be toggled
                                        // 3. add class 'la-js-dont-hide-this-card' to markup that is rendered only in case of card has content, like to a table <th>

                                        var toggleButton = $(".ui.toggle.button");
                                        var toggleIcon = $(".ui.toggle.button .icon");
                                        $(".table").trigger('reflow');

                                        if (  editMode) {
                                            // show Contoll Elements
                                            $('.card').not('.ui.modal .card').removeClass('hidden');
                                            $('.la-js-hide-this-card').removeClass('hidden');
                                            $('.ui .form').not('.ui.modal .ui.form').removeClass('hidden');
                                            $('#collapseableSubDetails').find('.button').removeClass('hidden');
                                            $(toggleButton).removeAttr("data-tooltip","${message(code:'statusbar.hideButtons.tooltip')}");
                                            $(toggleButton).attr("data-tooltip","${message(code:'statusbar.showButtons.tooltip')}");
                                            $(toggleIcon ).removeClass( "slash" );
                                            $(toggleButton).addClass('active');
                                            var enableXeditable = function(cssClass){
                                                var selection = $(cssClass).not('.ui.modal' + ' ' + cssClass);
                                                selection.editable('option', 'disabled', false);
                                            }
                                            enableXeditable ('.xEditableValue');
                                            enableXeditable ('.xEditable');
                                            enableXeditable ('.xEditableDatepicker');
                                            enableXeditable ('.xEditableManyToOne');
                                        }
                                        else {
                                            // hide Contoll Elements
                                            $('.card').not('.ui.modal .card').removeClass('hidden');
                                            $('.card.la-js-hideable').not( ":has(.la-js-dont-hide-this-card)" ).addClass('hidden');
                                            $('.la-js-hide-this-card').addClass('hidden');
                                            $('.ui.form').not('.ui.modal .ui.form').addClass('hidden');
                                            $('#collapseableSubDetails').not('.ui.modal').find('.button').not('.ui.modal .button, .la-js-dont-hide-button').addClass('hidden');
                                            $(toggleButton).removeAttr();
                                            $(toggleButton).attr("data-tooltip","${message(code:'statusbar.hideButtons.tooltip')}");
                                            $( toggleIcon ).addClass( "slash" );
                                            $(toggleButton).removeClass('active');
                                            // hide all the x-editable
                                            var diableXeditable = function(cssClass){
                                                var selection = $(cssClass).not('.ui.modal' + ' ' + cssClass);
                                                selection.editable('option', 'disabled', true);
                                            }
                                            diableXeditable ('.xEditableValue');
                                            diableXeditable ('.xEditable');
                                            diableXeditable ('.xEditableDatepicker');
                                            diableXeditable ('.xEditableManyToOne');
                                        }
                                    }
                                    toggleEditableElements();
                                });

                            </r:script>
                            </div>
                            </g:if>
                            <g:if test="${(params.mode)}">
                                <div class="item">
                                    <g:if test="${params.mode=='advanced'}">
                                        <div class="ui toggle la-toggle-advanced button" data-tooltip="${message(code:'statusbar.showAdvancedView.tooltip')}" data-position="bottom right" data-variation="tiny">
                                            <i class="icon plus square"></i>
                                    </g:if>
                                    <g:else>
                                        <div class="ui toggle la-toggle-advanced button" data-tooltip="${message(code:'statusbar.showBasicView.tooltip')}" data-position="bottom right" data-variation="tiny">
                                            <i class="icon plus square green slash"></i>
                                    </g:else>
                                </div>



                            <script>
                                var LaToggle = {};
                                LaToggle.advanced = {};
                                LaToggle.advanced.button = {};

                                // ready event
                                LaToggle.advanced.button.ready = function() {

                                    // selector cache
                                    var
                                        $button = $('.button.la-toggle-advanced'),

                                        // alias
                                        handler = {
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
                                        }
                                    ;
                                    $button
                                        .on('click', handler.activate)
                                    ;
                                };

                                // attach ready event
                                $(document)
                                    .ready(LaToggle.advanced.button.ready)
                                ;
                            </script>

                    </div>

                    </div>
                            </g:if>
                    <%--semui:editableLabel editable="${editable}" /--%>
            </div>
        </div>
        </nav><!-- Context Bar -->
    </sec:ifAnyGranted><%-- ROLE_USER --%>
        <%-- global content container --%>
        <main class="ui main container ${visibilityContextOrgMenu} ">
            <g:layoutBody/>
        </main><!-- .main -->

        <footer id="Footer">
            <div class="clearfix"></div>
            <div class="footer-links container">
                <div class="row"></div>
            </div>
        </footer>

        <%-- global container for modals and ajax --%>
        <div id="dynamicModalContainer"></div>

        <%-- global loading indicator --%>
        <div id="loadingIndicator" style="display: none">
            <div class="ui text loader active">Loading</div>
        </div>

        <%-- global confirmation modal --%>
        <semui:confirmationModal  />

        <%-- <a href="#globalJumpMark" class="ui button icon" style="position:fixed;right:0;bottom:0;"><i class="angle up icon"></i></a> --%

        <%-- maintenance --%>
        <g:if test="${com.k_int.kbplus.SystemMessage.findAllByShowNowAndOrg(true, contextOrg) || com.k_int.kbplus.SystemMessage.findAllByShowNowAndOrgIsNull(true)}">
            <div id="maintenance">
                <div class="ui segment center aligned inverted orange">
                    <strong>ACHTUNG:</strong>

                    <div class="ui list">
                        <g:each in="${com.k_int.kbplus.SystemMessage.findAllByShowNow(true)}" var="message">
                            <div class="item">
                                <g:if test="${message.org}">
                                    <g:if test="${contextOrg.id == message.org.id}">
                                        ${message.text}
                                    </g:if>
                                </g:if>
                                <g:else>
                                    ${message.text}
                                </g:else>
                            </div>
                        </g:each>
                    </div>
                </div>
            </div>
        </g:if>

        <r:layoutResources/>

        <% if(! flash.redirectFrom) { flash.clear() } %>
    </body>
</html>
