<%@ page import="de.laser.ui.Icon" %>
<laser:serviceInjection />

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.admin')} <i class="dropdown icon"></i>
    </a>
    <div class="menu" role="menu">
        <ui:link addItemAttributes="true" controller="admin" action="index">${message(code:'default.dashboard')}</ui:link>
        <div class="divider"></div>

        <div class="item" role="menuitem" aria-haspopup="true">
            <div class="title">
                <i class="icon keyboard outline"></i> ${message(code:'menu.admin.sysAdmin')} <i class="dropdown icon"></i>
            </div>
            <div class="menu" role="menu">
                <ui:link addItemAttributes="true" controller="admin" action="appInfo">${message(code:'menu.admin.appInfo')}</ui:link>
                <ui:link addItemAttributes="true" controller="admin" action="systemEvents">${message(code:'menu.admin.systemEvents')}</ui:link>
            </div>
        </div>

        <ui:link addItemAttributes="true" controller="admin" action="systemMessages"><i class="icon exclamation circle"></i>${message(code: 'menu.admin.systemMessage')}</ui:link>
        <ui:link addItemAttributes="true" controller="admin" action="systemAnnouncements"><i class="${Icon.ANNOUNCEMENT}"></i>${message(code:'menu.admin.announcements')}</ui:link>
        <div class="divider"></div>

        <div class="item" role="menuitem" aria-haspopup="true">
            <div class="title">
                ${message(code:'org.plural.label')} <i class="dropdown icon"></i>
            </div>
            <div class="menu" role="menu">
                <ui:link addItemAttributes="true" controller="organisation" action="index">${message(code:'menu.admin.allOrganisations')}</ui:link>
                <ui:link addItemAttributes="true" controller="admin" action="manageOrganisations">${message(code:'menu.admin.manageOrganisations')}</ui:link>
                <ui:link addItemAttributes="true" controller="admin" action="mergeProviders">${message(code:'menu.admin.mergeProviders')}</ui:link>
                <ui:link addItemAttributes="true" controller="admin" action="mergeVendors">${message(code:'menu.admin.mergeVendors')}</ui:link>
            </div>
        </div>

        <ui:link addItemAttributes="true" controller="user" action="list">${message(code:'menu.institutions.users')}</ui:link>
        <div class="divider"></div>

        <ui:link addItemAttributes="true" controller="admin" action="manageNamespaces">${message(code:'menu.admin.manageIdentifierNamespaces')}</ui:link>
        <ui:link addItemAttributes="true" controller="admin" action="managePropertyDefinitions">${message(code:'menu.admin.managePropertyDefinitions')}</ui:link>
        <ui:link addItemAttributes="true" controller="admin" action="manageRefdatas">${message(code:'menu.admin.manageRefdatas')}</ui:link>
        <div class="divider"></div>

        <div class="item" role="menuitem" aria-haspopup="true">
            <div class="title">
                <i class="${Icon.SYM.EMAIL}"></i> ${message(code:'menu.yoda.mail.label')} <i class="dropdown icon"></i>
            </div>
            <div class="menu" role="menu">
                <ui:link addItemAttributes="true" controller="admin" action="listMailTemplates">${message(code:'menu.admin.manageMailTemplates')}</ui:link>
                <ui:link addItemAttributes="true" controller="admin" action="sendMail">${message(code:'menu.admin.sendMail')}</ui:link>
            </div>
        </div>
        <div class="divider"></div>

        <ui:link addItemAttributes="true" controller="usage">${message(code:'menu.admin.manageUsageStats')}</ui:link>
        <div class="divider"></div>

        <ui:link addItemAttributes="true" controller="admin" action="missingPermantTitlesInSubs">${message(code:'menu.admin.missingPermantTitlesInSubs')}</ui:link>
        <ui:link addItemAttributes="true" controller="admin" action="packageLaserVsWekb">${message(code:'menu.admin.packageLaserVsWekb')}</ui:link>
    </div>
</div>