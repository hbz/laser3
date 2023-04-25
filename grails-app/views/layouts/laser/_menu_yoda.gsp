<%@ page import="de.laser.FormService" %>
<laser:serviceInjection />

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.yoda')} <i class="dropdown icon"></i>
    </a>

    <div class="menu" role="menu">
        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="index">${message(code:'default.dashboard')}</ui:link>
        <div class="divider"></div>

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

                <div class="divider"></div>
                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="appControllers">${message(code:'menu.yoda.appControllers')}</ui:link>
                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="userRoleMatrix">${message(code:'menu.yoda.userRoleMatrix')}</ui:link>
            </div>
        </div>

        <div class="item" role="menuitem" aria-haspopup="true">
            <div class="title">
                <i class="stopwatch icon"></i> Profiler <i class="dropdown icon"></i>
            </div>
            <div class="menu" role="menu">
                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="profilerLoadtime">${message(code:'menu.yoda.profilerLoadtime')}</ui:link>
                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="profilerActivity">${message(code:'menu.yoda.profilerActivity')}</ui:link>
                <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="profilerTimeline">${message(code:'menu.yoda.profilerTimeline')}</ui:link>
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
                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="reloadWekbProvider" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.reloadProvider')}</ui:link>
                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="reloadWekbPlatform" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.reloadPlatform')}</ui:link>
                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'identifier']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateIdentifiers')}</ui:link>
                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'editionStatement']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateEditionStatement')}</ui:link>
                        <%--<ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'medium', objType:'issueEntitlement']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateIEMedium')}</ui:link>--%>
                        <%--<ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'accessType', objType:'issueEntitlement']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.updateIEAccessType')}</ui:link>--%>
                        <ui:link generateElementId="true" class="item" role="menuitem" controller="yoda" action="updateData" params="[dataToLoad:'sortTitle']" onclick="return confirm('${message(code:'confirm.start.reload')}')">${message(code:'menu.yoda.regenerateSortTitle')}</ui:link>
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
                ${message(code:'menu.admin.bulkOps')} <i class="dropdown icon"></i>
            </div>
            <div class="menu" role="menu">
                <div class="item" role="menuitem">..</div>
            </div>
        </div>

        <div class="divider"></div>

        <div class="item" role="menuitem" aria-haspopup="true">
            <div class="title">
                ${message(code:'menu.admin.dataManagement')} <i class="dropdown icon"></i>
            </div>

            <div class="menu" role="menu">
                <ui:link generateElementId="true" class="item" role="menuitem" controller="package" action="getDuplicatePackages">List Package Duplicates</ui:link>
                <ui:link generateElementId="true" class="item" role="menuitem" controller="package" action="list">${message(code: 'myinst.packages')} - ${message(code: 'default.onlyDatabase')}</ui:link>
                <%--<ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="userMerge">${message(code:'menu.admin.userMerge')}</ui:link>--%>
                <%--<ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="hardDeletePkgs">${message(code:'menu.admin.hardDeletePkgs')}</ui:link>--%>
                <div class="divider"></div>

                <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="databaseInfo">${message(code: "menu.admin.databaseInfo")}</ui:link>
                <ui:link generateElementId="true" class="item" role="menuitem" controller="admin" action="databaseCollations">${message(code: "menu.admin.databaseCollations")}</ui:link>
                <div class="divider"></div>

                <ui:link generateElementId="true" class="item" role="menuitem" controller="stats" action="statsHome">${message(code:'menu.admin.statistics')}</ui:link>
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