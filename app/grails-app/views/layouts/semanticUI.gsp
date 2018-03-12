<%@ page import="org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;com.k_int.kbplus.Org" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<!doctype html>

<!--[if lt IE 7]> <html class="no-js lt-ie9 lt-ie8 lt-ie7" lang="en"> <![endif]-->
<!--[if IE 7]>    <html class="no-js lt-ie9 lt-ie8" lang="en"> <![endif]-->
<!--[if IE 8]>    <html class="no-js lt-ie9" lang="en"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js" lang="en"> <!--<![endif]-->

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title><g:layoutTitle default="${meta(name: 'app.name')}"/></title>
    <meta name="description" content="">
    <meta name="viewport" content="initial-scale = 1.0">

    <r:require modules="semanticUI" />

    <script type="text/javascript">
        var gspLocale = "${message(code:'default.locale.label', default:'en')}";
        var gspDateFormat = "${message(code:'default.date.format.notime', default:'yyyy-mm-dd').toLowerCase()}";
    </script>

    <g:layoutHead/>

    <tmpl:/layouts/favicon />

    <r:layoutResources/>
</head>

<body id="jumpMark_top" class="${controllerName}_${actionName}">

    <g:set var="contextOrg" value="${contextService.getOrg()}" />
    <g:set var="contextUser" value="${contextService.getUser()}" />
    <g:set var="contextMemberships" value="${contextService.getMemberships()}" />

    <div class="ui fixed inverted menu">
        <div class="ui container">
            <g:link controller="home" action="index" class="header item">LAS:eR</g:link>
            <%-- <img class="logo" src="${resource(dir: 'images', file: 'laser-logo.png')}" alt="laser-logo" width="100" height="26"/> --%>
            <sec:ifLoggedIn>
                <g:if test="${false}">
                <div class="ui simple dropdown item">
                    Data Explorer
                    <i class="dropdown icon"></i>

                    <div class="menu">
                        <a class="item" href="${createLink(uri: '/home/search')}">Search</a>
                        <g:link class="item" controller="packageDetails">Package</g:link>
                        <g:link class="item" controller="organisations">Organisations</g:link>
                        <g:link class="item" controller="platform">Platform</g:link>
                        <g:link class="item" controller="titleDetails">Title Instance</g:link>
                        <g:link class="item" controller="tipp">Title Instance Package Platform</g:link>
                        <g:link class="item" controller="subscriptionDetails">Subscriptions</g:link>
                        <g:link class="item" controller="licenseDetails">Licenses</g:link>
                        <g:link class="item" controller="onixplLicenseDetails" action="list">ONIX-PL Licenses</g:link>
                    </div>
                </div>
                </g:if>
            </sec:ifLoggedIn>

            <% /*
            <sec:ifLoggedIn>
                <div class="ui simple dropdown item">
                    Public
                    <i class="dropdown icon"></i>

                    <div class="menu">
                        <g:link class="item" controller="packageDetails" action="index">${message(code:'menu.institutions.all_pkg')}</g:link>
                        <g:link class="item" controller="titleDetails" action="index">${message(code:'menu.institutions.all_titles')}</g:link>
                        <g:link class="item" controller="packageDetails" action="compare">${message(code:'menu.institutions.comp_pkg')}</g:link>
                        <g:link class="item" controller="onixplLicenseCompare" action="index">${message(code:'menu.institutions.comp_onix')}</g:link>

                        <g:if test="${grailsApplication.config.feature.eBooks}">
                            <a class="item" href="http://gokb.k-int.com/gokbLabs">${message(code:'menu.institutions.ebooks')}</a>
                        </g:if>

                        <a class="item" href="${message(code:'help.location')}">${message(code:'menu.institutions.help')}</a>
                    </div>
                </div>
            </sec:ifLoggedIn>
            */ %>

            <g:if test="${user}">
                <g:if test="${contextOrg}">
                    <sec:ifLoggedIn>
                        <div class="ui simple dropdown item">
                            ${message(code:'menu.institutions.titlesAndPackages')}
                            <i class="dropdown icon"></i>

                            <div class="menu">
                                <g:link class="item" controller="packageDetails" action="index">${message(code:'menu.institutions.all_pkg')}</g:link>
                                <g:link class="item" controller="titleDetails" action="index">${message(code:'menu.institutions.all_titles')}</g:link>
                                <g:link class="item" controller="organisations" action="index">${message(code:'menu.institutions.all_orgs')}</g:link>

                                <div class="divider"></div>

                                <g:link class="item" controller="myInstitution" action="currentTitles">${message(code:'menu.institutions.myTitles')}</g:link>
                                <g:link class="item" controller="myInstitution" action="tipview">${message(code:'menu.institutions.myCoreTitles')}</g:link>

                                <div class="divider"></div>

                                <g:if test="${grailsApplication.config.feature.eBooks}">
                                    <a class="item" href="http://gokb.k-int.com/gokbLabs">${message(code:'menu.institutions.ebooks')}</a>
                                    <div class="divider"></div>
                                </g:if>

                                <g:link class="item" controller="packageDetails" action="compare">${message(code:'menu.institutions.comp_pkg')}</g:link>
                                <g:link class="item" controller="onixplLicenseCompare" action="index">${message(code:'menu.institutions.comp_onix')}</g:link>

                                <div class="divider"></div>

                                <a class="item" href="${message(code:'laser.help.href')}">${message(code:'menu.institutions.help')}</a>
                            </div>
                        </div>
                    </sec:ifLoggedIn>

                    <sec:ifLoggedIn>
                        <div class="ui simple dropdown item">
                            ${message(code:'menu.institutions.subs')}
                            <i class="dropdown icon"></i>

                            <div class="menu">
                                <g:link class="item" controller="myInstitution" action="currentSubscriptions">${message(code:'menu.institutions.mySubs')}</g:link>

                                <sec:ifAnyGranted roles="INST_USER">
                                    <div class="divider"></div>

                                    <g:link class="item" controller="myInstitution" action="emptySubscription">${message(code:'menu.institutions.emptySubscription')}</g:link>

                                    <div class="divider"></div>
                                </sec:ifAnyGranted>

                                <g:link class="item" controller="subscriptionDetails" action="compare">${message(code:'menu.institutions.comp_sub')}</g:link>

                            %{--<g:link class="item" controller="myInstitution" action="renewalsSearch">${message(code:'menu.institutions.gen_renewals')}</g:link>--}%
                            %{--<g:link class="item" controller="myInstitution" action="renewalsUpload">${message(code:'menu.institutions.imp_renew')}</g:link>--}%

                                <g:link class="item" controller="subscriptionImport" action="generateImportWorksheet"
                                        params="${[id:contextOrg?.id]}">${message(code:'menu.institutions.sub_work')}</g:link>
                                <g:link class="item" controller="subscriptionImport" action="importSubscriptionWorksheet"
                                        params="${[id:contextOrg?.id]}">${message(code:'menu.institutions.imp_sub_work')}</g:link>
                            </div>
                        </div>
                    </sec:ifLoggedIn>

                    <sec:ifLoggedIn>
                        <div class="ui simple dropdown item">
                            ${message(code:'menu.institutions.lic')}
                            <i class="dropdown icon"></i>

                            <div class="menu">
                                <g:link class="item" controller="myInstitution" action="currentLicenses">${message(code:'menu.institutions.myLics')}</g:link>

                                <div class="divider"></div>

                                <g:link class="item" controller="licenseCompare" action="index">${message(code:'menu.institutions.comp_lic')}</g:link>
                            </div>
                        </div>
                    </sec:ifLoggedIn>

                    <sec:ifLoggedIn>
                        <div class="ui simple dropdown item">
                            ${message(code:'menu.institutions.myInst')}
                            <i class="dropdown icon"></i>

                            <div class="menu">
                                <g:link class="item" controller="myInstitution" action="dashboard">${message(code:'menu.institutions.dash')}</g:link>
                                <g:link class="item" controller="organisations" action="show" params="[id: contextOrg?.id]">${message(code:'menu.institutions.org_info')}</g:link>

                                <g:link class="item" controller="myInstitution" action="tasks">${message(code:'menu.institutions.tasks')}</g:link>
                                <g:link class="item" controller="myInstitution" action="changes">${message(code:'menu.institutions.todo')}</g:link>

                                <g:link class="item" controller="myInstitution" action="addressbook">${message(code:'menu.institutions.addressbook', default:'Addressbook')}</g:link>
                                <g:link class="item" controller="myInstitution" action="managePrivateProperties">${message(code:'menu.institutions.manage_props', default:'Manage Property Rules')}</g:link>

                                <g:link class="item" controller="myInstitution" action="changeLog">${message(code:'menu.institutions.change_log')}</g:link>

                                <g:if test="${grailsApplication.config.feature_finance}">
                                    <g:link class="item" controller="myInstitution" action="finance">${message(code:'menu.institutions.finance')}</g:link>
                                    <g:link class="item" controller="myInstitution" action="financeImport">${message(code:'menu.institutions.financeImport')}</g:link>
                                </g:if>

                            </div>
                        </div>
                    </sec:ifLoggedIn>
                </g:if>
            </g:if>

            <sec:ifLoggedIn>
                <sec:ifAnyGranted roles="ROLE_DATAMANAGER,ROLE_ADMIN">
                    <div class="ui simple dropdown item">
                        ${message(code:'menu.datamanager')}
                        <i class="dropdown icon"></i>

                        <div class="menu">
                            <g:link class="item" controller="dataManager" action="index">${message(code:'menu.datamanager.dash')}</g:link>

                            <div class="divider"></div>

                            <g:link class="item" controller="announcement" action="index">${message(code:'menu.datamanager.ann')}</g:link>
                            <g:link class="item" controller="packageDetails" action="list">${message(code:'menu.datamanager.searchPackages')}</g:link>
                            <g:link class="item" controller="platform" action="list">${message(code:'menu.datamanager.searchPlatforms')}</g:link>

                            <div class="divider"></div>

                            <g:link class="item" controller="upload" action="reviewPackage">${message(code:'menu.datamanager.uploadPackage')}</g:link>
                            <g:link class="item" controller="licenseImport" action="doImport">${message(code:'onix.import.license')}</g:link>

                            <div class="divider"></div>

                            <g:link class="item" controller="titleDetails" action="findTitleMatches">${message(code:'menu.datamanager.newTitle')}</g:link>
                            <g:link class="item" controller="licenseDetails" action="create">${message(code:'license.template.new')}</g:link>
                            <g:link class="item" controller="platform" action="create">${message(code:'menu.datamanager.newPlatform')}</g:link>

                            <g:link class="item" controller="subscriptionDetails" action="compare">${message(code:'menu.datamanager.compareSubscriptions')}</g:link>
                            <g:link class="item" controller="subscriptionImport" action="generateImportWorksheet">${message(code:'menu.datamanager.sub_work')}</g:link>
                            <g:link class="item" controller="subscriptionImport" action="importSubscriptionWorksheet" params="${[dm:'true']}">${message(code:'menu.datamanager.imp_sub_work')}</g:link>
                            <g:link class="item" controller="dataManager" action="changeLog">${message(code:'menu.datamanager.changelog')}</g:link>

                            <div class="divider"></div>

                            <g:link class="item" controller="globalDataSync" action="index">${message(code:'menu.datamanager.global_data_sync')}</g:link>

                            <div class="divider"></div>

                            <g:link class="item" controller="jasperReports" action="index">${message(code:'menu.datamanager.jasper_reports')}</g:link>
                            <g:link class="item" controller="titleDetails" action="dmIndex">${message(code:'menu.datamanager.titles')}</g:link>
                        </div>
                    </div>
                </sec:ifAnyGranted>
            </sec:ifLoggedIn>

            <sec:ifLoggedIn>
                <sec:ifAnyGranted roles="ROLE_ADMIN">
                    <div class="ui simple dropdown item">
                        Admin
                        <i class="dropdown icon"></i>

                        <div class="menu">
                            <g:link class="item" controller="admin" action="manageAffiliationRequests">
                                Manage Affiliation Requests
                                <g:set var="newAffiliationRequests" value="${com.k_int.kbplus.auth.UserOrg.findAllByStatus(0, [sort:'dateRequested']).size()}" />
                                <g:if test="${newAffiliationRequests > 0}">
                                    <div class="ui floating red circular label">${newAffiliationRequests}</div>
                                </g:if>
                            </g:link>

                            <div class="ui dropdown item">
                                System Admin
                                <i class="dropdown icon"></i>

                                <div class="menu">
                                    <g:link class="item" controller="yoda" action="settings">System Settings</g:link>
                                    <g:link class="item" controller="yoda" action="appConfig">App Config</g:link>
                                    <g:link class="item" controller="yoda" action="appInfo">App Info</g:link>
                                    <g:link class="item" controller="yoda" action="appSecurity">App Security</g:link>

                                    <div class="divider"></div>

                                    <g:link class="item" controller="yoda" action="globalSync">Start Global Data Sync</g:link>
                                    <g:link class="item" controller="yoda" action="manageGlobalSources">Manage Global Sources</g:link>

                                    <div class="divider"></div>

                                    <g:link class="item" controller="yoda" action="fullReset">Run Full ES Index Reset</g:link>
                                    <g:link class="item" controller="yoda" action="esIndexUpdate">Start ES Index Update</g:link>
                                    <%--<g:link class="item" controller="yoda" action="logViewer">Log Viewer</g:link>--%>
                                </div>
                            </div>

                            <div class="divider"></div>

                            <g:link class="item" controller="organisations" action="index">Manage Organisations</g:link>
                            <g:link class="item" controller="admin" action="showAffiliations">Show Affiliations</g:link>
                            <g:link class="item" controller="admin" action="allNotes">All Notes</g:link>
                            <g:link class="item" controller="userDetails" action="list">User Details</g:link>
                            <g:link class="item" controller="admin" action="statsSync">Run Stats Sync</g:link>
                            <% /* g:link class="item" controller="admin" action="forumSync">Run Forum Sync</g:link */ %>
                            <% /* g:link class="item" controller="admin" action="juspSync">Run JUSP Sync</g:link */ %>
                            <g:link class="item" controller="admin" action="forceSendNotifications">Send Pending Notifications</g:link>

                            <div class="ui dropdown item">
                                Data Management
                                <i class="dropdown icon"></i>

                                <div class="menu">
                                    <g:link class="item" controller="dataManager" action="expungeDeletedTitles" onclick="return confirm('You are about to permanently delete all titles with a status of ‘Deleted’. This will also delete any TIPPs and IEs that are attached to this title. Only click OK if you are absolutely sure you wish to proceed')">Expunge Deleted Titles</g:link>
                                    <g:link class="item" controller="dataManager" onclick="return confirm('This will only delete TIPPs that are not attached to current IEs.')" action="expungeDeletedTIPPS">Expunge Deleted TIPPS</g:link>
                                    <g:link class="item" controller="admin" action="titleMerge">Title Merge</g:link>
                                    <g:link class="item" controller="admin" action="tippTransfer">TIPP Transfer</g:link>
                                    <g:link class="item" controller="admin" action="ieTransfer">IE Transfer</g:link>
                                    <g:link class="item" controller="admin" action="userMerge">User Merge</g:link>
                                    <g:link class="item" controller="admin" action="hardDeletePkgs">Package Delete</g:link>
                                </div>
                            </div>

                            <div class="divider"></div>

                            <div class="ui dropdown item">
                               Bulk Operations
                               <i class="dropdown icon"></i>

                               <div class="menu">
                                    <g:link class="item" controller="admin" action="orgsExport">Bulk Export Organisations</g:link>
                                    <g:link class="item" controller="admin" action="orgsImport">Bulk Load Organisations</g:link>
                                    <g:link class="item" controller="admin" action="titlesImport">Bulk Load/Update Titles</g:link>
                                    <g:link class="item" controller="admin" action="financeImport">Bulk Load Financial Transaction</g:link>
                                </div>
                            </div>

                            <div class="divider"></div>

                            <g:link class="item" controller="admin" action="manageNamespaces">${message(code:'menu.admin.manageIdentifierNamespaces')}</g:link>
                            <g:link class="item" controller="admin" action="managePropertyDefinitions">${message(code:'menu.admin.managePropertyDefinitions')}</g:link>
                            <g:link class="item" controller="admin" action="manageRefdatas">${message(code:'menu.admin.manageRefdatas')}</g:link>
                            <g:link class="item" controller="admin" action="manageContentItems">${message(code:'menu.admin.manageContentItems')}</g:link>

                            <div class="divider"></div>

                            <g:link class="item" controller="stats" action="statsHome">Statistics</g:link>
                            <g:link class="item" controller="jasperReports" action="uploadReport">Upload Report Definitions</g:link>

                            <div class="ui dropdown item">
                                Batch tasks
                                <i class="dropdown icon"></i>

                                <div class="menu">
                                    <g:link class="item" controller="admin" action="triggerHousekeeping">Trigger Housekeeping</g:link>
                                    <g:link class="item" controller="admin" action="initiateCoreMigration">Initiate Core Migration</g:link>
                                    <g:if test="${grailsApplication.config.feature.issnl}">
                                        <g:link class="item" controller="admin" action="uploadIssnL">Upload ISSN to ISSN-L File</g:link>
                                    </g:if>
                                    <g:link class="item" controller="admin" action="dataCleanse">Run Data Cleaning (Nominal Platforms)</g:link>
                                    <g:link class="item" controller="admin" action="titleAugment">Run Data Cleaning (Title Augment)</g:link>
                                    <%--<g:link class="item" controller="yoda" action="fullReset">Run Full ES Index Reset</g:link>--%>
                                    <%--<g:link class="item" controller="yoda" action="esIndexUpdate">Start ES Index Update</g:link>--%>
                                </div>
                            </div>
                        </div>
                    </div>
                </sec:ifAnyGranted>
            </sec:ifLoggedIn>

<% /* TODO
            <sec:ifLoggedIn>
                <div class="right menu">
                    <div class="item">
                        <div class="ui icon input">
                            <input placeholder="Search .." type="text">
                            <i class="search link icon"></i>
                        </div>
                    </div>
                    <a class="item dlpopover" href="#"><i class="icon-search icon-white"></i></a>
                </div>
            </sec:ifLoggedIn>
 */ %>
            <div class="right menu">
                <sec:ifLoggedIn>
                    <g:if test="${user}">
                        <div class="ui simple dropdown item la-noBorder">
                            ${user.displayName}
                            <i class="dropdown icon"></i>

                            <div class="menu">

                                <g:set var="usaf" value="${user.authorizedOrgs}" />
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
                                <g:link class="item" controller="logout">${message(code:'menu.user.logout')}</g:link>
                            </div>
                        </div>
                    </g:if>
                </sec:ifLoggedIn>
                <sec:ifNotLoggedIn>
                    <g:link class="item" controller="myInstitution" action="dashboard">${message(code:'menu.user.login')}</g:link>
                </sec:ifNotLoggedIn>
            </div>


        </div><!-- container -->

    </div><!-- main menu -->
    <div class="ui fixed menu la-contextBar"  >
        <div class="ui container">
            <div class="ui sub header item la-context-org">${contextOrg?.name}</div>

            <div class="right menu la-advanced-view">



                <g:if test="${ (params.mode)}">

                            <div class="ui slider item checkbox"  id="la-advanced">
                                <input type="checkbox" tabindex="0" >
                                <label>${message(code:'profile.advancedView')}</label>
                            </div>

                            <script>
                                $(document).ready(function() {

                                    <% if (params.mode=='advanced') { %>
                                        $('#la-advanced').addClass('checked')
                                    <% } %>
                                    $('#la-advanced').checkbox(
                                        <% if (params.mode=='basic') { %> 'uncheck' <% } %>
                                        <% if (params.mode=='advanced') { %> 'check' <% } %>
                                        )
                                        .checkbox({

                                             onChecked: function () {
                                                 window.location.href = "<g:createLink action="${actionName}" params="${params + ['mode':'advanced']}" />"

                                            },
                                            onUnchecked: function () {
                                                window.location.href = "<g:createLink action="${actionName}" params="${params + ['mode':'basic']}" />"
                                            }


                                            });



                                })
                            </script>
                </g:if>
                <semui:editableLabel editable="${editable}" />
            </div>
        </div>
    </div><!-- Context Bar -->


    <div class="ui right aligned sub header">${contextOrg?.name}</div>
    <div class="navbar-push"></div>

        <sec:ifLoggedIn>
            <g:if test="${user!=null && ( user.display==null || user.display=='' ) }">
                <div>
                    <bootstrap:alert class="alert-info">Your display name is not currently set in user preferences. Please <g:link controller="profile" action="index">update
                        Your display name</g:link> as soon as possible.
                    </bootstrap:alert>
                </div>
            </g:if>
        </sec:ifLoggedIn>

        <div class="ui main container">

            <g:layoutBody/>
        </div><!-- .main -->

        <div id="Footer">
            <div class="clearfix"></div>
            <div class="footer-links container">
                <div class="row"></div>
            </div>
        </div>

        <r:layoutResources/>

        <% if(! flash.redirectFrom) { flash.clear() } %>
    </body>
</html>
