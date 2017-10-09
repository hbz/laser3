<%@ page import="org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes" %>
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

    <g:layoutHead/>

    <tmpl:/layouts/favicon />

    <r:layoutResources/>
</head>

<body>

    <script>
        dataLayer = [{
            'Institution': '${params.shortcode}',
            'UserDefaultOrg': '${user?.defaultDash?.shortcode}',
            'UserRole': 'ROLE_USER'
        }];
    </script>

    <div class="ui fixed inverted menu">
        <div class="ui container">
            <g:link controller="home" action="index" class="header item">LAS:eR</g:link>

            <sec:ifLoggedIn>
                <g:if test="${false}">
                <div class="ui simple dropdown item">
                    Data Explorer
                    <i class="dropdown icon"></i>

                    <div class="menu">
                        <a class="item" href="${createLink(uri: '/home/search')}">Search</a>
                        <g:link class="item" controller="package">Package</g:link>
                        <g:link class="item" controller="org">Organisations</g:link>
                        <g:link class="item" controller="platform">Platform</g:link>
                        <g:link class="item" controller="titleInstance">Title Instance</g:link>
                        <g:link class="item" controller="titleInstancePackagePlatform">Title Instance Package Platform</g:link>
                        <g:link class="item" controller="subscription">Subscriptions</g:link>
                        <g:link class="item" controller="license">Licenses</g:link>
                        <g:link class="item" controller="onixplLicenseDetails" action="list">ONIX-PL Licenses</g:link>
                    </div>
                </div>
                </g:if>
            </sec:ifLoggedIn>

            <sec:ifLoggedIn>
                <div class="ui simple dropdown item">
                    ${message(code:'menu.institutions')}
                    <i class="dropdown icon"></i>

                    <div class="menu">
                        <g:link class="item" controller="packageDetails" action="index">${message(code:'menu.institutions.all_pkg')}</g:link>
                        <g:link class="item" controller="titleDetails" action="index">${message(code:'menu.institutions.all_titles')}</g:link>
                        <g:link class="item" controller="packageDetails" action="compare">${message(code:'menu.institutions.comp_pkg')}</g:link>
                        <g:link class="item" controller="onixplLicenseCompare" action="index">${message(code:'menu.institutions.comp_onix')}</g:link>

                        <g:if test="${grailsApplication.config.feature.eBooks}">
                            <a class="item" href="http://gokb.k-int.com/gokbLabs">${message(code:'menu.institutions.ebooks')}</a>
                        </g:if>

                        <g:if test="${user}">
                            <div class="divider"></div>

                            <g:set var="usaf" value="${user.authorizedOrgs}" />
                            <g:if test="${usaf && usaf.size() > 0}">
                                <g:each in="${usaf}" var="org">
                                    <div class="item">
                                        ${org.name}
                                        <i class="dropdown icon"></i>

                                        <div class="menu">
                                            <g:link class="item" controller="myInstitutions" action="instdash"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.dash')}</g:link>
                                            <g:link class="item" controller="myInstitutions" action="todo"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.todo')}</g:link>
                                            <g:link class="item" controller="myInstitutions" action="currentLicenses"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.lic')}</g:link>
                                            <g:link class="item" controller="myInstitutions"
                                                        action="currentSubscriptions"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.subs')}</g:link>
                                            <g:link class="item" controller="myInstitutions"
                                                        action="currentTitles"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.ttls')}</g:link>
                                            <g:link class="item" controller="myInstitutions" action="addressbook"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.addressbook', default:'Addressbook')}</g:link>
                                            <g:link class="item" controller="myInstitutions" action="propertyRules"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.manage_props', default:'Manage Property Rules')}</g:link>
                                            <g:link class="item" controller="subscriptionDetails" action="compare"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.comp_sub')}</g:link>
                                            <g:link class="item" controller="licenseCompare" action="index"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.comp_lic')}</g:link>
                                            <g:link class="item" controller="myInstitutions" action="renewalsSearch"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.gen_renewals')}</g:link>
                                            <g:link class="item" controller="myInstitutions" action="renewalsUpload"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.imp_renew')}</g:link>
                                            <g:link class="item" controller="organisations" action="show"
                                                    params="${[id:org.id]}">${message(code:'menu.institutions.org_info')}</g:link>
                                            <g:link class="item" controller="subscriptionImport" action="generateImportWorksheet"
                                                    params="${[id:org.id]}">${message(code:'menu.institutions.sub_work')}</g:link>
                                            <g:link class="item" controller="subscriptionImport" action="importSubscriptionWorksheet"
                                                    params="${[id:org.id]}">${message(code:'menu.institutions.imp_sub_work')}</g:link>
                                            <g:link class="item" controller="myInstitutions" action="changeLog"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.change_log')}</g:link>
                                            <!--
                                            <g:link class="item" controller="myInstitutions" action="emptySubscription"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.emptySubscription')}</g:link>
                                            -->
                                            <g:if test="${grailsApplication.config.feature_finance}">
                                                <g:link class="item" controller="myInstitutions" action="financeImport"
                                                        params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.financeImport')}</g:link>
                                                <g:link class="item" controller="myInstitutions" action="finance"
                                                        params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.finance')}</g:link>
                                            </g:if>

                                            <g:link class="item" controller="myInstitutions" action="tipview"
                                                    params="${[shortcode:org.shortcode]}">${message(code:'menu.institutions.core_ttl')}</g:link>
                                        </div>
                                    </div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <li><span>${message(code:'menu.institutions.affiliation')}</span> <g:link controller="profile" action="index">${message(code:'menu.user.profile')}</g:link></li>
                            </g:else>
                        </g:if>
                        <div class="divider"></div>

                        <a class="item" href="${message(code:'help.location')}">${message(code:'menu.institutions.help')}</a>
                    </div>
                </div>
            </sec:ifLoggedIn>

            <sec:ifLoggedIn>
                <sec:ifAnyGranted roles="ROLE_ADMIN,KBPLUS_EDITOR">
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
                            <g:link class="item" controller="licenseDetails" action="create">${message(code:'license.new')}</g:link>
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
                        Admin Actions
                        <i class="dropdown icon"></i>

                        <div class="menu">
                            <g:link class="item" controller="admin" action="manageAffiliationRequests">Manage Affiliation Requests</g:link>
                            <g:link class="item" controller="admin" action="settings">System Settings</g:link>

                            <div class="item">
                                System Admin
                                <i class="dropdown icon"></i>

                                <div class="menu">
                                    <g:link class="item" controller="sysAdmin" action="appConfig">App Config</g:link>
                                    <g:link class="item" controller="sysAdmin" action="appInfo">App Info</g:link>
                                    <g:link class="item" controller="sysAdmin" action="logViewer">Log Viewer</g:link>
                                </div>
                            </div>

                            <div class="divider"></div>

                            <g:link class="item" controller="organisations" action="index">Manage Organisations</g:link>
                            <g:link class="item" controller="admin" action="showAffiliations">Show Affiliations</g:link>
                            <g:link class="item" controller="admin" action="allNotes">All Notes</g:link>
                            <g:link class="item" controller="userDetails" action="list">User Details</g:link>
                            <g:link class="item" controller="admin" action="forumSync">Run Forum Sync</g:link>
                            <g:link class="item" controller="admin" action="juspSync">Run JUSP Sync</g:link>
                            <g:link class="item" controller="admin" action="forceSendNotifications">Send Pending Notifications</g:link>

                            <div class="item">
                                Data Management Tasks
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

                            <g:link class="item" controller="admin" action="globalSync">Start Global Data Sync</g:link>
                            <g:link class="item" controller="admin" action="manageGlobalSources">Manage Global Sources</g:link>


                            <div class="item">
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

                            <div class="item">
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
                                    <g:link class="item" controller="admin" action="fullReset">Run Full ES Index Reset</g:link>
                                    <g:link class="item" controller="admin" action="esIndexUpdate">Start ES Index Update</g:link>
                                </div>
                            </div>
                        </div>
                    </div>
                </sec:ifAnyGranted>
            </sec:ifLoggedIn>

            <sec:ifLoggedIn>
                <sec:ifAnyGranted roles="ROLE_ADMIN,KBPLUS_EDITOR">
                    <g:if env="development">
                        <div class="ui simple dropdown item">
                            Demo **
                            <i class="dropdown icon"></i>

                            <div class="menu">
                                <g:link class="item" controller="address" action="index">Address Controller</g:link>
                                <g:link class="item" controller="cluster" action="index">Cluster Controller</g:link>
                                <g:link class="item" controller="contact" action="index">Contact Controller</g:link>
                                <g:link class="item" controller="person" action="index">Person Controller</g:link>

                                <div class="divider"></div>

                                <g:link class="item" controller="identifier" action="index">Identifier Controller</g:link>
                                <g:link class="item" controller="organisations" action="index">Organisations Controller</g:link>
                                <g:link class="item" controller="license" action="index">License Controller</g:link>
                                <g:link class="item" controller="package" action="index">Package Controller</g:link>
                                <g:link class="item" controller="subscription" action="index">Subscription Controller</g:link>
                                <g:link class="item" controller="titleInstance" action="index">Title Controller</g:link>
                            </div>
                        </div>
                    </g:if>
                </sec:ifAnyGranted>
            </sec:ifLoggedIn>

            <!-- TODO
            <sec:ifLoggedIn>
                <div class="right menu">
                    <div class="item">
                        <div class="ui icon input">
                            <input placeholder="Search .." type="text">
                            <i class="search link icon"></i>
                        </div>
                    </div>
-->
                    <!-- <a class="item dlpopover" href="#"><i class="icon-search icon-white"></i></a> -->
         <!--       </div>
            </sec:ifLoggedIn>
-->
            <div class="right menu">
                <sec:ifLoggedIn>
                    <g:if test="${user}">
                        <div class="ui simple dropdown item">
                            ${user.displayName}
                            <i class="dropdown icon"></i>

                            <div class="menu">
                                <g:link class="item" controller="profile" action="index">${message(code:'menu.user.profile')}</g:link>
                                <g:link class="item" controller="logout">${message(code:'menu.user.logout')}</g:link>
                            </div>
                        </div>
                    </g:if>
                </sec:ifLoggedIn>
                <sec:ifNotLoggedIn>
                    <g:link class="item" controller="myInstitutions" action="dashboard">${message(code:'menu.user.login')}</g:link>
                </sec:ifNotLoggedIn>
            </div>


        </div><!-- container -->
    </div><!-- inverted menu -->







   <div class="navbar-push"></div>

   <sec:ifLoggedIn>
     <g:if test="${user!=null && ( user.display==null || user.display=='' ) }">
       <div class="container">
         <bootstrap:alert class="alert-info">Your display name is not currently set in user preferences. Please <g:link controller="profile" action="index">update
            Your display name</g:link> as soon as possible.
         </bootstrap:alert>
       </div>
     </g:if>
   </sec:ifLoggedIn>


  <g:layoutBody/>

  <div id="Footer">

      <div class="clearfix"></div>

      <div class="footer-links container">
          <div class="row">

          </div>
      </div>
  </div>

  <r:layoutResources/>

  </body>
</html>
