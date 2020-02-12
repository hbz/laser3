<%@ page import="de.laser.helper.RDStore;de.laser.helper.RDConstants;org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;com.k_int.kbplus.Org;com.k_int.kbplus.auth.User;com.k_int.kbplus.UserSettings;com.k_int.kbplus.RefdataValue" %>
<!doctype html>

<laser:serviceInjection />
<%
    User currentUser = contextService.getUser()
    String currentLang = 'de'
    String currentTheme = 'semanticUI'

    if (currentUser) {
        RefdataValue rdvLocale = currentUser?.getSetting(UserSettings.KEYS.LANGUAGE, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE))?.getValue()

        if (rdvLocale) {
            currentLang = rdvLocale.value
            org.springframework.web.servlet.LocaleResolver localeResolver = org.springframework.web.servlet.support.RequestContextUtils.getLocaleResolver(request)
            localeResolver.setLocale(request, response, new Locale(currentLang, currentLang.toUpperCase()))
        }

        RefdataValue rdvTheme = currentUser?.getSetting(UserSettings.KEYS.THEME, RefdataValue.getByValueAndCategory('semanticUI', RDConstants.USER_SETTING_THEME))?.getValue()

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
    <nav id="mainMenue" class="ui fixed inverted stackable menu">
        <div class="ui container">
            <g:link controller="home" action="index" aria-label="${message(code:'default.home.label')}" class="header item la-logo-item">
                <img alt="Logo Laser" class="logo" src="${resource(dir: 'images', file: 'laser.svg')}"/>
            </g:link>

            <sec:ifAnyGranted roles="ROLE_USER">

                <g:if test="${contextOrg}">
                    <div class="ui simple dropdown item">
                        ${message(code:'menu.public')}
                        <i class="dropdown icon"></i>

                        <div class="menu">
                                <g:link class="item" controller="package" action="index">${message(code:'menu.public.all_pkg')}</g:link>

                                <g:link class="item" controller="title" action="index">${message(code:'menu.public.all_titles')}</g:link>

                                <g:if test="${grailsApplication.config.feature.eBooks}">
                                    <a class="item" href="http://gokb.k-int.com/gokbLabs">${message(code:'menu.institutions.ebooks')}</a>
                                    <div class="divider"></div>
                                </g:if>

                                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_EDITOR">
                                    <semui:mainNavItem controller="organisation" action="index" message="menu.public.all_orgs" />
                                </sec:ifAnyGranted>

                                <semui:securedMainNavItem orgPerm="ORG_CONSORTIUM" affiliation="INST_USER" specRole="ROLE_ADMIN,ROLE_ORG_EDITOR"
                                                              controller="organisation" action="listInstitution" message="menu.public.all_insts" />

                                <g:link class="item" controller="organisation" action="listProvider">${message(code:'menu.public.all_provider')}</g:link>

                                <g:link class="item" controller="platform" action="list">${message(code:'menu.public.all_platforms')}</g:link>

                                <div class="divider"></div>

                                <semui:securedMainNavItemDisabled orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="package" action="compare" message="menu.public.comp_pkg" />

                                <div class="divider"></div>

                                <g:link class="item" controller="gasco">${message(code:'menu.public.gasco_monitor')}</g:link>

                                <a href="${message(code:'url.gokb.' + grailsApplication.config.getCurrentServer())}" class="item">GOKB</a>

                                <a href="${message(code:'url.ygor.' + grailsApplication.config.getCurrentServer())}" class="item">YGOR</a>
                        </div>
                    </div>

                    <div class="ui simple dropdown item">
                        ${message(code:'menu.my')}
                        <i class="dropdown icon"></i>

                        <div class="menu">

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="currentSubscriptions" message="menu.my.subscriptions" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="currentLicenses" message="menu.my.licenses" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="currentProviders" message="menu.my.providers" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="currentPlatforms" message="menu.my.platforms" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="currentPackages" message="menu.my.packages" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="currentTitles" message="menu.my.titles" />

                            <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="documents" message="menu.my.documents" />


                    <g:if test="${accessService.checkPerm('ORG_BASIC_MEMBER')}">
                        <div class="divider"></div>
                        <g:if test="${grailsApplication.config.featureSurvey}">
                        <semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" action="currentSurveys" message="menu.my.surveys" />
                        </g:if>
                        <g:else>
                            <semui:securedMainNavItem orgPerm="FAKE" affiliation="INST_EDITOR" controller="myInstitution" action="" message="menu.my.surveys" />
                        </g:else>
                    </g:if>

                            <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                                <g:if test="${grailsApplication.config.featureSurvey}">
                                <div class="divider"></div>
                                <semui:securedMainNavItem affiliation="INST_EDITOR" controller="survey" action="currentSurveysConsortia" message="menu.my.surveys" />
                                </g:if>
                                <g:else>
                                <div class="divider"></div>
                                <semui:securedMainNavItem orgPerm="FAKE" affiliation="INST_EDITOR" controller="myInstitution" action="" message="menu.my.surveys" />
                                </g:else>

                                <div class="divider"></div>

                                <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" specRole="ROLE_ADMIN,ROLE_ORG_EDITOR" action="manageMembers" message="menu.my.consortia" />

                                <semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" specRole="ROLE_ADMIN" action="manageConsortiaSubscriptions" message="menu.my.consortiaSubscriptions" />
                            </g:if>
                            <g:elseif test="${accessService.checkPerm('ORG_INST_COLLECTIVE')}">
                                <div class="divider"></div>

                                <semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" specRole="ROLE_ADMIN, ROLE_ORG_EDITOR" action="manageMembers" message="menu.my.departments" />
                            </g:elseif>

                            <div class="divider"></div>

                            <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_EDITOR" controller="myInstitution" action="emptySubscription" message="menu.institutions.emptySubscription" />

                            <semui:securedMainNavItemDisabled orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="subscription" action="compare" message="menu.my.comp_sub" />

                            <div class="divider"></div>

                            <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_EDITOR" controller="myInstitution" action="emptyLicense" message="license.add.blank" />

                            <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="licenseCompare" action="index" message="menu.my.comp_lic" />

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

                            <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="addressbook" message="menu.institutions.myAddressbook" />

                            <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="tasks" message="task.plural" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="changes" message="menu.institutions.todo" />

                            <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_EDITOR" controller="myInstitution" action="managePrivateProperties" message="menu.institutions.manage_props" />

                            <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="finance" message="menu.institutions.finance" />

                            <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_EDITOR" specRole="ROLE_ADMIN" controller="myInstitution" action="budgetCodes" message="menu.institutions.budgetCodes" />
                            <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_EDITOR" specRole="ROLE_ADMIN" controller="costConfiguration" action="index" message="menu.institutions.costConfiguration" />
                            <%--<semui:securedMainNavItemDisabled message="menu.institutions.financeImport" />--%>
                            <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_EDITOR" specRole="ROLE_ADMIN" controller="myInstitution" action="financeImport" message="menu.institutions.financeImport" />

                            <div class="divider"></div>

                            <g:set var="myInstNewAffils" value="${com.k_int.kbplus.auth.UserOrg.findAllByStatusAndOrg(0, contextService.getOrg(), [sort:'dateRequested']).size()}" />

                            <semui:securedMainNavItem affiliation="INST_ADM" controller="myInstitution" action="userList" message="menu.institutions.users" newAffiliationRequests="${myInstNewAffils}" />

                            <sec:ifAnyGranted roles="ROLE_YODA">
                                   <g:link class="item" controller="myInstitution" action="changeLog">${message(code:'menu.institutions.change_log')}</g:link>
                                <%--<semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" action="changeLog" message="menu.institutions.change_log" />--%>
                            </sec:ifAnyGranted>

                        </div>
                    </div>
                </g:if>

                <sec:ifAnyGranted roles="ROLE_ORG_MANAGER,ROLE_ADMIN,ROLE_GLOBAL_DATA,ROLE_STATISTICS_EDITOR">
                    <div class="ui simple dropdown item">
                        ${message(code:'menu.datamanager')}
                        <i class="dropdown icon"></i>

                        <div class="menu">
                            <sec:ifAnyGranted roles="ROLE_STATISTICS_EDITOR">
                                <g:link class="item" controller="usage"
                                        action="index">${message(code: 'menu.datamanager.manage_usage_stats', default: 'Manage Usage Stats')}</g:link>
                            </sec:ifAnyGranted>

                            <sec:ifAnyGranted roles="ROLE_ADMIN">
                                <g:link class="item" controller="dataManager" action="index">${message(code:'menu.datamanager.dash')}</g:link>
                                <g:link class="item" controller="dataManager"
                                        action="deletedTitles">${message(code: 'datamanager.deletedTitleManagement.label', default: 'Deleted Title management')}</g:link>
                            </sec:ifAnyGranted>

                            <sec:ifAnyGranted roles="ROLE_ORG_MANAGER,ROLE_ADMIN">
                                <g:link class="item" controller="dataManager"
                                        action="deletedOrgs">${message(code: 'datamanager.deletedOrgManagement.label', default: 'Deleted Org management')}</g:link>
                            </sec:ifAnyGranted>

                            <sec:ifAnyGranted roles="ROLE_ADMIN">
                                <div class="divider"></div>

                                <g:link class="item" controller="announcement" action="index">${message(code:'menu.datamanager.ann')}</g:link>
                                <g:link class="item" controller="package" action="list">${message(code:'menu.datamanager.searchPackages')}</g:link>
                                <g:link class="item" controller="platform" action="list">${message(code:'menu.datamanager.searchPlatforms')}</g:link>

                                <div class="divider"></div>

                                <%--<g:link class="item" controller="upload" action="reviewPackage">${message(code:'menu.datamanager.uploadPackage')}</g:link>--%>
                                <g:link class="item" controller="licenseImport" action="doImport">${message(code:'onix.import.license')}</g:link>

                                <div class="divider"></div>

                                <g:link class="item" controller="title" action="findTitleMatches">${message(code:'menu.datamanager.newTitle')}</g:link>
                                <g:link class="item" controller="license" action="create">${message(code:'license.template.new')}</g:link>
                                <g:link class="item" controller="platform" action="create">${message(code:'menu.datamanager.newPlatform')}</g:link>

                                <g:link class="item" controller="subscription" action="compare">${message(code:'menu.datamanager.compareSubscriptions')}</g:link>
                                <g:link class="item" controller="onixplLicenseCompare" action="index">${message(code:'menu.institutions.comp_onix')}</g:link>
                                <g:link class="item" controller="dataManager" action="changeLog">${message(code:'menu.datamanager.changelog')}</g:link><div class="divider"></div>
                            </sec:ifAnyGranted>

                            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_GLOBAL_DATA">
                                <g:link class="item" controller="globalDataSync" action="index" >${message(code:'menu.datamanager.global_data_sync')}</g:link>
                                <g:link class="item" controller="dataManager" action="checkPackageTIPPs">Tipps Check of GOKB and LAS:eR</g:link>
                            </sec:ifAnyGranted>

                            <sec:ifAnyGranted roles="ROLE_ADMIN">
                                <div class="divider"></div>
                                <%--<g:link class="item" controller="jasperReports" action="index">${message(code:'menu.datamanager.jasper_reports')}</g:link>--%>
                                <g:link class="item" controller="title" action="dmIndex">${message(code:'menu.datamanager.titles')}</g:link>
                                <div class="divider"></div>
                                <g:link class="item" controller="dataManager" action="listMailTemplates">Mail Templates</g:link>
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
                                <%--<g:set var="newAffiliationRequests" value="${com.k_int.kbplus.auth.UserOrg.findAllByStatus(0).size()}" />--%>
                                <%
                                    Object organisationService = grailsApplication.mainContext.getBean("organisationService")
                                    Map affilRequestMap = organisationService.getPendingRequests(contextService.getUser(), contextService.getOrg())
                                    int newAffiliationRequests = affilRequestMap?.pendingRequests?.size()
                                %>
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

                                    <div class="divider"></div>

                                    <g:link class="item" controller="admin" action="triggerHousekeeping" onclick="return confirm('${message(code:'confirm.start.HouseKeeping')}')">${message(code:'menu.admin.triggerHousekeeping')}</g:link>
                                    <g:link class="item" controller="admin" action="initiateCoreMigration" onclick="return confirm('${message(code:'confirm.start.CoreMigration')}')">${message(code:'menu.admin.coreMigration')}</g:link>
                                    <g:link class="item" controller="admin" action="dataCleanse" onclick="return confirm('${message(code:'confirm.start.DataCleaningNominalPlatforms')}')">Run Data Cleaning (Nominal Platforms)</g:link>
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
                            <g:link class="item" controller="admin" action="updateQASubscriptionDates">${message(code:'menu.admin.updateTestSubscriptionDates')}</g:link>
                            <% /* g:link class="item" controller="admin" action="juspSync">Run JUSP Sync</g:link */ %>
                            <g:link class="item" controller="admin" action="forceSendNotifications">${message(code:'menu.admin.sendNotifications')}</g:link>

                            <div class="divider"></div>

                            <div class="ui dropdown item">
                                ${message(code:'menu.admin.bulkOps')}
                               <i class="dropdown icon"></i>

                               <div class="menu">
                                    <g:link class="item" controller="admin" action="orgsExport">${message(code:'menu.admin.bulkOps.orgsExport')}</g:link>
                                    <g:link class="item" controller="admin" action="orgsImport">${message(code:'menu.admin.bulkOps.orgsImport')}</g:link>
                                    <g:link class="item" controller="yoda" action="makeshiftLaserOrgExport">${message(code:'menu.admin.exportBasicData')}</g:link>
                                </div>
                            </div>
                            <div class="divider"></div>

                            <g:link class="item" controller="admin" action="manageNamespaces">${message(code:'menu.admin.manageIdentifierNamespaces')}</g:link>
                            <g:link class="item" controller="admin" action="managePropertyDefinitions">${message(code:'menu.admin.managePropertyDefinitions')}</g:link>
                            <g:link class="item" controller="admin" action="manageSurveyPropertyDefinitions">${message(code:'menu.admin.manageSurveyPropertyDefinitions')}</g:link>
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
                                <i class="ui icon wrench"></i> ${message(code:'menu.yoda.engine')}
                                <i class="dropdown icon"></i>

                                <div class="menu">

                                    <g:link class="item" controller="yoda" action="settings">${message(code:'menu.yoda.systemSettings')}</g:link>
                                    <g:link class="item" controller="yoda" action="manageSystemMessage">${message(code: 'menu.admin.systemMessage')}</g:link>
                                    <g:link class="item" controller="yoda" action="appConfig">${message(code:'menu.yoda.appConfig')}</g:link>
                                    <g:link class="item" controller="yoda" action="appThreads">${message(code:'menu.yoda.appThreads')}</g:link>

                                    <g:link class="item" controller="yoda" action="systemProfiler">${message(code:'menu.yoda.systemProfiler')}</g:link>
                                    <g:link class="item" controller="yoda" action="activityProfiler">${message(code:'menu.yoda.activityProfiler')}</g:link>
                                    <g:link class="item" controller="yoda" action="quartzInfo">${message(code:'menu.yoda.quartzInfo')}</g:link>
                                    <g:link class="item" controller="yoda" action="cacheInfo">${message(code:'menu.yoda.cacheInfo')}</g:link>

                                    <g:link class="item" controller="yoda" action="appSecurity">${message(code:'menu.yoda.security')}</g:link>
                                    <g:link class="item" controller="yoda" action="userMatrix">${message(code:'menu.yoda.userMatrix')}</g:link>
                                    <g:link class="item" controller="yoda" action="userRoleDefinitions">${message(code:'menu.yoda.userRoleDefinitions')}</g:link>

                                    <%--<a class="item" href="${g.createLink(uri:'/monitoring')}">App Monitoring</a>--%>
                                </div>
                            </div>

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

                            <div class="ui dropdown item">
                                ${message(code:'menu.admin.dataManagement')}
                                <i class="dropdown icon"></i>

                                <div class="menu">
                                    <g:link class="item" controller="dataManager" action="listPlatformDuplicates">List Platform Duplicates</g:link>
                                    <g:link class="item" controller="package" action="getDuplicatePackages">List Package Duplicates</g:link>
                                    <g:link class="item" controller="dataManager" action="listDeletedTIPPS">List TIPP Duplicates and deleted TIPPs</g:link>
                                    <g:link class="item" controller="admin" action="titleRemap">Check Title GOKb IDs</g:link>
                                    <%--<g:link class="item" controller="admin" action="tippTransfer">${message(code:'menu.admin.tippTransfer')}</g:link>--%>
                                    <%--<g:link class="item" controller="admin" action="ieTransfer">${message(code:'menu.admin.ieTransfer')}</g:link>--%>
                                    <%--<g:link class="item" controller="admin" action="userMerge">${message(code:'menu.admin.userMerge')}</g:link>--%>
                                    <%--<g:link class="item" controller="admin" action="hardDeletePkgs">${message(code:'menu.admin.hardDeletePkgs')}</g:link>--%>
                                    <g:link class="item" controller="admin" action="manageDeletedObjects">${message(code: "menu.admin.deletedObjects")}</g:link>
                                    <g:link class="item" controller="admin" action="databaseStatistics">${message(code: "menu.admin.databaseStatistics")}</g:link>
                                    <g:link class="item" controller="admin" action="dataConsistency">${message(code: "menu.admin.dataConsistency")}</g:link>
                                </div>
                            </div>

                            <div class="ui dropdown item">
                                ${message(code:'menu.admin.syncManagement')}
                                <i class="dropdown icon"></i>
                                <div class="menu">
                                    <g:link class="item" controller="yoda" action="globalSync" onclick="return confirm('${message(code:'confirm.start.globalDataSync')}')">${message(code:'menu.yoda.globalDataSync')}</g:link>
                                    <g:link class="item" controller="yoda" action="manageGlobalSources">${message(code:'menu.yoda.manageGlobalSources')}</g:link>
                                    <g:link class="item" controller="yoda" action="pendingChanges">${message(code:'menu.yoda.pendingChanges')}</g:link>
                                    <g:link class="item" controller="yoda" action="retriggerPendingChanges">${message(code:'menu.yoda.retriggerPendingChanges')}</g:link>
                                    <g:link class="item" controller="yoda" action="getTIPPsWithoutGOKBId">${message(code:'menu.yoda.purgeTIPPsWithoutGOKBID')}</g:link>
                                    <g:link class="item" controller="yoda" action="getTIsWithoutGOKBId">${message(code:'menu.yoda.purgeTIsWithoutGOKBID')}</g:link>
                                    <g:link class="item" controller="yoda" action="titlesUpdate">${message(code:'menu.admin.bulkOps.titlesImport')}</g:link>
                                </div>
                            </div>

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
                                    <g:link class="item" controller="yoda" action="migrateCollectiveSubscriptions">Migrate Collective Subscriptions (0.20)</g:link>
                                    <g:link class="item" controller="yoda" action="migrateNatStatSettings">Migrate NatStat Settings (0.20)</g:link>
                                    <%--<g:link class="item" controller="yoda" action="remapOriginEditUrl">Remap OriginEditUrl (0.20)</g:link>--%>
                                    <g:link class="item" controller="yoda" action="checkIssueEntitlementPackages"><g:message code="menu.admin.checkIssueEntitlementPackages"/> (0.20)</g:link>
                                    <g:link class="item" controller="yoda" action="surveyCheck">Update Survey Status</g:link>
                                    <g:link class="item" controller="yoda" action="dbmFixPrivateProperties">Fix Private Properties</g:link>
                                    <g:link class="item" controller="yoda" action="replaceUserSettingDashboardReminderPeriod">Replace UserSetting Dashboard ReminderPeriod in Database</g:link>
                                    <g:link class="item" controller="yoda" action="cleanUpSurveys">Clean Up Surveys with Multi Term</g:link>
                                    <g:link class="item" controller="yoda" action="insertEditUris">Insert Edit URIs for GOKB Sources</g:link>

                                    <g:link class="item" controller="yoda" action="subscriptionCheck">${message(code:'menu.admin.subscriptionsCheck')}</g:link>
                                    <%--<g:link class="item" controller="yoda" action="updateLinks">${message(code:'menu.admin.updateLinks')}</g:link>--%>
                                    <%--<g:link class="item" controller="yoda" action="startDateCheck">${message(code:'menu.admin.startDatesCheck')}</g:link>--%>
                                    <%--<g:link class="item" controller="yoda" action="updateTaxRates">${message(code:'menu.admin.taxTypeCheck')}</g:link>--%>
                                    <%--<g:link class="item" controller="yoda" action="updateCustomerType">Kundentyp für alle Einrichtungen setzen</g:link>--%>
                                    <%--<g:link class="item" controller="yoda" action="showOldDocumentOwners">${message(code:'menu.admin.documentOwnerCheck')}</g:link>--%>
                                    <%--<g:link class="item" controller="yoda" action="generateBatchUID">${message(code:'menu.admin.batchUID')}</g:link>--%>
                                    <%--<g:link class="item" controller="yoda" action="makeshiftLaserOrgExport">${message(code:'menu.admin.exportBasicData')}</g:link>--%>
                                    <g:link class="item" controller="yoda" action="dropDeletedObjects">Drop deleted Objects from Database</g:link>
                                    <g:link class="item" controller="yoda" action="migratePackageIdentifiers">Remap Package Identifier Namespace</g:link>
                                    <%--<g:link class="item" controller="yoda" action="assignNoteOwners">Assign note owners for notes of subscriptions and licenses without owners</g:link>--%>
                                    <%--<g:link class="item" controller="yoda" action="correctCostsInLocalCurrency" params="[dryRun: true]">${message(code:'menu.admin.correctCostsInLocalCurrencyDryRun')}</g:link>
                                    <g:link class="item js-open-confirm-modal"
                                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.correctCostsInLocalCurrency')}"
                                            data-confirm-term-how="ok"
                                            controller="yoda" action="correctCostsInLocalCurrency" params="[dryRun: false]">${message(code:'menu.admin.correctCostsInLocalCurrencyDoIt')}</g:link>--%>

                                </div>
                            </div>

                            <div class="divider"></div>
                            <g:link class="item" controller="yoda" action="frontend">Frontend für Entwickler</g:link>

                        </div>

                    </div>
                </sec:ifAnyGranted>

                <div class="right menu la-right-menuPart">
                    <div id="mainSearch" class="ui category search spotlight">
                        <div class="ui icon input">
                            <input  aria-label="${message(code:'spotlight.search.placeholder')}" type="search" id="spotlightSearch" class="prompt" placeholder="${message(code:'spotlight.search.placeholder')}">
                            <i id="btn-search"  class="search icon"></i>
                        </div>
                        <div class="results" style="overflow-y:scroll;max-height: 400px;"></div>
                    </div>

                    <g:link controller="search" action="index"
                            class="la-search-advanced la-popup-tooltip la-delay"
                             data-content="${message(code: 'search.advancedSearch.tooltip')}">
                        <i class="large icons">
                            <i class="search icon"></i>
                            <i class="top grey right corner cog icon "></i>
                        </i>
                    </g:link>

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

                                <g:link class="item" controller="profile" action="index">${message(code:'menu.user.profile')}</g:link>
                                <g:link class="item" controller="profile" action="help">${message(code:'menu.user.help')}</g:link>
                                <g:link class="item" controller="profile" action="errorReport">${message(code:'menu.user.errorReport')}</g:link>
                                <g:link class="item" controller="profile" action="dsgvo">${message(code:'privacyNotice')}</g:link>

                                <div class="divider"></div>

                                <g:link class="item" controller="logout">${message(code:'menu.user.logout')}</g:link>
                                <div class="divider"></div>

                                <g:if test="${grailsApplication.metadata['app.version']}">
                                    <div class="header">Version: ${grailsApplication.metadata['app.version']} – ${grailsApplication.metadata['app.buildDate']}</div>
                                </g:if>
                                <div class="header">
                                    ${yodaService.getNumberOfActiveUsers()} Benutzer online
                                </div>
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
                            <span class="ui icon button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.cachedContent.tooltip')}" data-position="bottom right" data-variation="tiny">
                                <i class="hourglass end icon"></i>
                            </span>
                        </g:if>
                    </div>

                        <g:if test="${(controllerName=='yoda' && actionName=='frontend' ) || (controllerName=='subscription'|| controllerName=='license') && actionName=='show' && editable}">
                            <div class="item">
                                <g:if test="${user?.getSettingsValue(UserSettings.KEYS.SHOW_EDIT_MODE, RefdataValue.getByValueAndCategory('Yes', RDConstants.Y_N))?.value=='Yes'}">
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

                        <g:if test="${(controllerName=='yoda' && actionName=='frontend' ) || (controllerName=='subscription'|| controllerName=='license') && actionName=='show'}">

                            <r:script>
                                $(function(){
                                    <g:if test="${editable} || ${overwriteEditable}">
                                        <g:if test="${user?.getSettingsValue(UserSettings.KEYS.SHOW_EDIT_MODE, RefdataValue.getByValueAndCategory('Yes', RDConstants.Y_N))?.value == 'Yes'}">
                                            deckSaver.configs.editMode  = true;
                                        </g:if>
                                        <g:else>
                                            deckSaver.configs.editMode  = false;
                                        </g:else>
                                    </g:if>
                                    <g:else>
                                        deckSaver.configs.editMode  = false;
                                    </g:else>

                                    deckSaver.toggleEditableElements();
                                    $(".ui.toggle.button").click(function(){
                                        deckSaver.configs.editMode = !deckSaver.configs.editMode;
                                         $.ajax({
                                            url: '<g:createLink controller="ajax" action="toggleEditMode"/>',
                                                        data: {
                                                            showEditMode: deckSaver.configs.editMode
                                                        },
                                                        success: function(){
                                                            deckSaver.toggleEditableElements();
                                                        },
                                                        complete: function () {
                                                        }
                                         })
                                    });
                                })
                            </r:script>
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
                            $(document).ready(LaToggle.advanced.button.ready);
                        </script>
                        </g:if>
                </div>

            </div>
                    <%--semui:editableLabel editable="${editable}" /--%>

        </nav><!-- Context Bar -->
    </sec:ifAnyGranted><%-- ROLE_USER --%>

        <%-- global content container --%>
        <main class="ui main container ${visibilityContextOrgMenu} ">
            <g:layoutBody/>
        </main><!-- .main -->

        <sec:ifNotGranted roles="ROLE_USER">
            <!-- Footer -->
            <g:render template="/public/templates/footer" />
            <!-- Footer End -->
        </sec:ifNotGranted>

        <%-- global container for modals and ajax --%>
        <div id="dynamicModalContainer"></div>

        <%-- global loading indicator --%>
        <div id="loadingIndicator" style="display: none">
            <div class="ui text loader active">Loading</div>
        </div>

        <%-- global confirmation modal --%>
        <semui:confirmationModal  />

        <%-- <a href="#globalJumpMark" class="ui button icon" style="position:fixed;right:0;bottom:0;"><i class="angle up icon"></i></a> --%>

        <r:script>

        </r:script>

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

        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <semui:systemInfo />

            <div id="system-profiler" class="ui label hidden">
                <i class="clock icon"></i>
                <span></span>
            </div>
        </sec:ifAnyGranted>

        <script>
            $(document).ready(function() {
                $.ajax({
                    url: "${g.createLink(controller:'ajax', action:'notifyProfiler')}",
                    data: {uri: "${request.request.request.request.servletPath.replaceFirst('/grails','').replace('.dispatch','')}"},
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
    </body>
</html>
